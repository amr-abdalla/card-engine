package io.github.amrabdalla.yugioh.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.helper.HelperCard;
import io.github.amrabdalla.cardengine.card.zone.CreatureZone;
import io.github.amrabdalla.cardengine.card.zone.HelperZone;
import io.github.amrabdalla.cardengine.duelist.core.Duelist;
import io.github.amrabdalla.cardengine.game.Game;
import io.github.amrabdalla.cardengine.game.GameContext;
import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;
import io.github.amrabdalla.yugioh.YugiohRules;
import io.github.amrabdalla.yugioh.card.BattlePosition;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.card.YugiohSpell;
import io.github.amrabdalla.yugioh.card.YugiohTrap;
import io.github.amrabdalla.yugioh.duelist.YugiohDuelist;
import io.github.amrabdalla.yugioh.turn.YugiohPhase;
import io.github.amrabdalla.yugioh.turn.YugiohPhaseID;
import io.github.amrabdalla.yugioh.turn.YugiohTurnFactory;

/**
 * Drives a duel on top of the engine's Game and holds every rule that needs to see the whole
 * board: what may be done in which phase, what a summon costs, who may attack whom, and when the
 * duel is over. The cards themselves stay unaware of all of it.
 *
 * Every action that breaks a rule is refused with an IllegalStateException naming the rule.
 */
public class YugiohGame
{
	private final Game game;
	private final YugiohDuelist duelist1;
	private final YugiohDuelist duelist2;

	private boolean normalSummonUsed;
	private boolean drawnThisTurn;
	private boolean over;
	private YugiohDuelist winner;

	public YugiohGame(YugiohDuelist duelist1, YugiohDuelist duelist2)
	{
		if (duelist1 == null || duelist2 == null)
		{
			throw new IllegalArgumentException("A duel needs two duelists");
		}

		if (duelist1 == duelist2)
		{
			throw new IllegalArgumentException("A duelist cannot duel themselves");
		}

		this.duelist1 = duelist1;
		this.duelist2 = duelist2;
		this.game = new Game(duelist1, duelist2, YugiohTurnFactory.getInstance().createTurn());
	}

	// ---------------------------------------------------------------- state

	public Game getEngineGame()
	{
		return game;
	}

	public GameContext getContext()
	{
		return game.getContext();
	}

	public YugiohDuelist getCurrentDuelist()
	{
		return asYugiohDuelist(game.getCurrentDuelist());
	}

	public YugiohDuelist getOpponentDuelist()
	{
		return asYugiohDuelist(game.getOpponentDuelist());
	}

	public int getTurnNumber()
	{
		return game.getTurnNumber();
	}

	public TurnPhase getCurrentPhase()
	{
		return game.getCurrenTurnPhase();
	}

	public YugiohPhaseID getCurrentPhaseID()
	{
		return ((YugiohPhase) getCurrentPhase()).getPhaseID();
	}

	public boolean hasUsedNormalSummon()
	{
		return normalSummonUsed;
	}

	public boolean hasDrawnThisTurn()
	{
		return drawnThisTurn;
	}

	public boolean isOver()
	{
		return over;
	}

	public Optional<YugiohDuelist> getWinner()
	{
		return Optional.ofNullable(winner);
	}

	public List<YugiohMonster> getMonstersOnField(YugiohDuelist duelist)
	{
		List<YugiohMonster> monsters = new ArrayList<>();

		for (CreatureZone zone : duelist.getCreatureZones())
		{
			Optional<Card> occupied = zone.getOccupiedCard();

			if (occupied.isPresent() && occupied.get() instanceof YugiohMonster monster)
			{
				monsters.add(monster);
			}
		}

		return monsters;
	}

	// ----------------------------------------------------------- turn flow

	public int drawForTurn()
	{
		requireDuelInProgress();

		TurnPhase phase = getCurrentPhase();

		if (phase.drawCount() <= 0)
		{
			throw new IllegalStateException("Cards are only drawn for the turn during the draw phase, but it is the " + getCurrentPhaseID() + " phase");
		}

		if (drawnThisTurn)
		{
			throw new IllegalStateException("The draw for this turn has already been taken");
		}

		YugiohDuelist duelist = getCurrentDuelist();
		int required = phase.drawCount();
		int drawn = duelist.draw(required);
		drawnThisTurn = true;

		if (drawn < required)
		{
			// Running out of cards to draw loses the duel.
			endDuel(opponentOf(duelist));
		}

		return drawn;
	}

	public void nextPhase()
	{
		requireDuelInProgress();

		int turnBeforeAdvancing = game.getTurnNumber();
		game.moveToNextPhase();

		if (game.getTurnNumber() != turnBeforeAdvancing)
		{
			startNewTurn();
		}
	}

	public void endTurn()
	{
		requireDuelInProgress();

		game.endTurn();
		startNewTurn();
	}

	private void startNewTurn()
	{
		normalSummonUsed = false;
		drawnThisTurn = false;

		for (YugiohMonster monster : getMonstersOnField(duelist1))
		{
			monster.resetAttackFlag();
		}

		for (YugiohMonster monster : getMonstersOnField(duelist2))
		{
			monster.resetAttackFlag();
		}
	}

	// ------------------------------------------------------------ summoning

	public void normalSummon(YugiohMonster monster, int zoneIndex)
	{
		tributeSummon(monster, zoneIndex, List.of());
	}

	public void tributeSummon(YugiohMonster monster, int zoneIndex, List<YugiohMonster> tributes)
	{
		requireDuelInProgress();
		requireSummonsAllowed();
		requireNormalSummonAvailable();

		if (monster == null)
		{
			throw new IllegalStateException("No monster was given to summon");
		}

		YugiohDuelist duelist = getCurrentDuelist();
		requireInHand(duelist, monster, "Monster \"" + monster.getName() + "\"");

		List<YugiohMonster> offered = tributes == null ? List.of() : tributes;
		requireTributesAreValid(duelist, monster, offered);

		CreatureZone zone = creatureZone(duelist, zoneIndex);

		// The zone check runs before anything is paid, so a refused summon costs nothing. A zone
		// that is about to be emptied by one of this summon's own tributes counts as free.
		if (zone.isOccupied() && !isOccupiedByAnyOf(zone, offered))
		{
			throw new IllegalStateException("Monster zone " + zoneIndex + " is already occupied");
		}

		for (YugiohMonster tribute : offered)
		{
			sendFromFieldToDiscard(duelist, tribute);
		}

		placeSummonedMonster(duelist, monster, zone, BattlePosition.ATTACK);
		afterAction();
	}

	public void setMonster(YugiohMonster monster, int zoneIndex)
	{
		requireDuelInProgress();
		requireSummonsAllowed();
		requireNormalSummonAvailable();

		if (monster == null)
		{
			throw new IllegalStateException("No monster was given to set");
		}

		YugiohDuelist duelist = getCurrentDuelist();
		requireInHand(duelist, monster, "Monster \"" + monster.getName() + "\"");

		if (monster.getRequiredTributes() != 0)
		{
			throw new IllegalStateException("Monster \"" + monster.getName() + "\" is level " + monster.getLevel() + " and needs " + monster.getRequiredTributes() + " tribute(s), which setting does not support");
		}

		CreatureZone zone = creatureZone(duelist, zoneIndex);

		if (zone.isOccupied())
		{
			throw new IllegalStateException("Monster zone " + zoneIndex + " is already occupied");
		}

		placeSummonedMonster(duelist, monster, zone, BattlePosition.SET);
		afterAction();
	}

	private void placeSummonedMonster(YugiohDuelist duelist, YugiohMonster monster, CreatureZone zone, BattlePosition position)
	{
		if (!monster.canPlay(zone))
		{
			throw new IllegalStateException("Monster \"" + monster.getName() + "\" cannot be placed in that zone");
		}

		duelist.getHand().remove(monster);
		monster.setBattlePosition(position);
		monster.resetAttackFlag();
		monster.play(zone);

		normalSummonUsed = true;
	}

	private void requireTributesAreValid(YugiohDuelist duelist, YugiohMonster monster, List<YugiohMonster> offered)
	{
		int required = monster.getRequiredTributes();

		if (offered.size() != required)
		{
			throw new IllegalStateException("Monster \"" + monster.getName() + "\" is level " + monster.getLevel() + " and needs exactly " + required + " tribute(s), but " + offered.size() + " were offered");
		}

		Set<YugiohMonster> distinct = Collections.newSetFromMap(new IdentityHashMap<>());

		for (YugiohMonster tribute : offered)
		{
			if (tribute == null)
			{
				throw new IllegalStateException("A tribute cannot be null");
			}

			if (!distinct.add(tribute))
			{
				throw new IllegalStateException("Monster \"" + tribute.getName() + "\" cannot be tributed twice for the same summon");
			}

			if (!controls(duelist, tribute))
			{
				throw new IllegalStateException("Monster \"" + tribute.getName() + "\" cannot be tributed because it is not on your field");
			}
		}
	}

	// --------------------------------------------------------- spells/traps

	public void setHelperCard(HelperCard card, int zoneIndex)
	{
		requireDuelInProgress();

		// Setting a spell or trap is a main phase action, same as activating one.
		if (!getCurrentPhase().allowsHelperCardsActivation())
		{
			throw new IllegalStateException("Spells and traps can only be set during the main phase, but it is the " + getCurrentPhaseID() + " phase");
		}

		if (card == null)
		{
			throw new IllegalStateException("No card was given to set");
		}

		YugiohDuelist duelist = getCurrentDuelist();
		requireInHand(duelist, card, describe(card));

		HelperZone zone = helperZone(duelist, zoneIndex);

		if (zone.isOccupied())
		{
			throw new IllegalStateException("Spell/trap zone " + zoneIndex + " is already occupied");
		}

		if (!card.canPlay(zone))
		{
			throw new IllegalStateException(describe(card) + " cannot be placed in that zone");
		}

		duelist.getHand().remove(card);
		card.play(zone);

		if (card instanceof YugiohTrap trap)
		{
			trap.markSet(getTurnNumber());
		}

		afterAction();
	}

	public void activateSpell(YugiohSpell spell, Optional<Card> target)
	{
		requireDuelInProgress();

		if (!getCurrentPhase().allowsHelperCardsActivation())
		{
			throw new IllegalStateException("Spells can only be activated during the main phase, but it is the " + getCurrentPhaseID() + " phase");
		}

		if (spell == null)
		{
			throw new IllegalStateException("No spell was given to activate");
		}

		YugiohDuelist duelist = getCurrentDuelist();
		boolean fromHand = duelist.getHand().contains(spell);
		HelperZone zone = findHelperZone(duelist, spell);

		if (!fromHand && zone == null)
		{
			throw new IllegalStateException("Spell \"" + spell.getName() + "\" is neither in your hand nor set on your field");
		}

		Optional<Card> effectTarget = target == null ? Optional.<Card>empty() : target;

		if (!spell.canActivateEffect(getContext(), effectTarget))
		{
			throw new IllegalStateException("Spell \"" + spell.getName() + "\" cannot resolve against the given target");
		}

		spell.activateEffect(getContext(), effectTarget);

		if (zone != null)
		{
			zone.remove(spell);
		}

		duelist.discard(spell);
		afterAction();
	}

	public void activateTrap(YugiohTrap trap, Optional<Card> target)
	{
		requireDuelInProgress();

		if (!getCurrentPhase().allowsTrapsActivation())
		{
			throw new IllegalStateException("Traps cannot be activated during the " + getCurrentPhaseID() + " phase");
		}

		if (trap == null)
		{
			throw new IllegalStateException("No trap was given to activate");
		}

		// Either duelist may activate a trap they have set, not only the turn player.
		YugiohDuelist owner = findHelperZoneOwner(trap);

		if (owner == null)
		{
			if (duelist1.getHand().contains(trap) || duelist2.getHand().contains(trap))
			{
				throw new IllegalStateException("Trap \"" + trap.getName() + "\" cannot be activated from the hand, it has to be set first");
			}

			throw new IllegalStateException("Trap \"" + trap.getName() + "\" is not set on either duelist's field");
		}

		if (!trap.canActivate(getTurnNumber()))
		{
			throw new IllegalStateException("Trap \"" + trap.getName() + "\" was set on turn " + trap.getSetTurn() + " and cannot be activated before turn " + (trap.getSetTurn() + 1));
		}

		Optional<Card> effectTarget = target == null ? Optional.<Card>empty() : target;

		if (!trap.canActivateEffect(getContext(), effectTarget))
		{
			throw new IllegalStateException("Trap \"" + trap.getName() + "\" cannot resolve against the given target");
		}

		trap.activateEffect(getContext(), effectTarget);

		findHelperZone(owner, trap).remove(trap);
		owner.discard(trap);
		afterAction();
	}

	// --------------------------------------------------------------- battle

	public BattleResult declareAttack(YugiohMonster attacker, YugiohMonster target)
	{
		requireDuelInProgress();
		requireAttacksAllowed();

		YugiohDuelist attackingDuelist = getCurrentDuelist();
		YugiohDuelist defendingDuelist = getOpponentDuelist();

		requireCanDeclareAttack(attackingDuelist, attacker);

		if (target == null)
		{
			throw new IllegalStateException("No monster was given to attack");
		}

		if (!controls(defendingDuelist, target))
		{
			throw new IllegalStateException("Monster \"" + target.getName() + "\" cannot be attacked because it is not on your opponent's field");
		}

		BattleResult result = BattleResolver.resolveAttack(attacker, target);
		attacker.markAttacked();

		if (result.attackerDestroyed())
		{
			attacker.markDead();
		}

		if (result.defenderDestroyed())
		{
			target.markDead();
		}

		applyBattleDamage(attackingDuelist, defendingDuelist, result);
		afterAction();

		return result;
	}

	public BattleResult declareDirectAttack(YugiohMonster attacker)
	{
		requireDuelInProgress();
		requireAttacksAllowed();

		YugiohDuelist attackingDuelist = getCurrentDuelist();
		YugiohDuelist defendingDuelist = getOpponentDuelist();

		requireCanDeclareAttack(attackingDuelist, attacker);

		if (!getMonstersOnField(defendingDuelist).isEmpty())
		{
			throw new IllegalStateException("A direct attack is not allowed while your opponent controls a monster");
		}

		BattleResult result = BattleResolver.resolveDirectAttack(attacker);
		attacker.markAttacked();

		applyBattleDamage(attackingDuelist, defendingDuelist, result);
		afterAction();

		return result;
	}

	private void applyBattleDamage(YugiohDuelist attackingDuelist, YugiohDuelist defendingDuelist, BattleResult result)
	{
		if (result.damageToAttackingDuelist() > 0)
		{
			attackingDuelist.takeDamage(result.damageToAttackingDuelist());
		}

		if (result.damageToDefendingDuelist() > 0)
		{
			defendingDuelist.takeDamage(result.damageToDefendingDuelist());
		}
	}

	private void requireAttacksAllowed()
	{
		if (!getCurrentPhase().allowsAttacks())
		{
			throw new IllegalStateException("Attacks can only be declared during the battle phase, but it is the " + getCurrentPhaseID() + " phase");
		}

		if (getTurnNumber() <= YugiohRules.FIRST_TURN_NUMBER)
		{
			throw new IllegalStateException("Attacks are not allowed on the first turn of the duel");
		}
	}

	private void requireCanDeclareAttack(YugiohDuelist attackingDuelist, YugiohMonster attacker)
	{
		if (attacker == null)
		{
			throw new IllegalStateException("No monster was given to attack with");
		}

		if (!controls(attackingDuelist, attacker))
		{
			throw new IllegalStateException("Monster \"" + attacker.getName() + "\" cannot attack because it is not on your field");
		}

		if (attacker.hasAttackedThisTurn())
		{
			throw new IllegalStateException("Monster \"" + attacker.getName() + "\" has already attacked this turn");
		}

		if (attacker.getBattlePosition() != BattlePosition.ATTACK)
		{
			throw new IllegalStateException("Monster \"" + attacker.getName() + "\" is in " + attacker.getBattlePosition() + " position and cannot attack");
		}

		if (!attacker.canDeclareAttack())
		{
			throw new IllegalStateException("Monster \"" + attacker.getName() + "\" cannot declare an attack");
		}
	}

	// --------------------------------------------- cleanup and end of duel

	private void afterAction()
	{
		sweepDestroyedMonsters(duelist1);
		sweepDestroyedMonsters(duelist2);
		checkLifePoints();
	}

	private void sweepDestroyedMonsters(YugiohDuelist duelist)
	{
		for (CreatureZone zone : duelist.getCreatureZones())
		{
			Optional<Card> occupied = zone.getOccupiedCard();

			if (occupied.isEmpty() || !(occupied.get() instanceof Creature creature) || !creature.isDead())
			{
				continue;
			}

			zone.remove(occupied.get());
			duelist.discard(occupied.get());
			creature.triggerDeathEvents();
		}
	}

	private void checkLifePoints()
	{
		boolean duelist1IsOut = duelist1.getLifePoints() <= 0;
		boolean duelist2IsOut = duelist2.getLifePoints() <= 0;

		if (duelist1IsOut && duelist2IsOut)
		{
			// Both dropping to zero at once is a draw: the duel ends with nobody winning.
			over = true;
			winner = null;
		}
		else if (duelist1IsOut)
		{
			endDuel(duelist2);
		}
		else if (duelist2IsOut)
		{
			endDuel(duelist1);
		}
	}

	private void endDuel(YugiohDuelist winningDuelist)
	{
		over = true;
		winner = winningDuelist;
	}

	private void requireDuelInProgress()
	{
		if (over)
		{
			throw new IllegalStateException("The duel is over and no further actions can be taken");
		}
	}

	// -------------------------------------------------------------- helpers

	private void requireSummonsAllowed()
	{
		if (!getCurrentPhase().allowsSummons())
		{
			throw new IllegalStateException("Monsters can only be summoned during the main phase, but it is the " + getCurrentPhaseID() + " phase");
		}
	}

	private void requireNormalSummonAvailable()
	{
		if (normalSummonUsed)
		{
			throw new IllegalStateException("Only one monster can be normal summoned or set per turn");
		}
	}

	private void requireInHand(YugiohDuelist duelist, Card card, String description)
	{
		if (!duelist.getHand().contains(card))
		{
			throw new IllegalStateException(description + " is not in your hand");
		}
	}

	private boolean controls(YugiohDuelist duelist, YugiohMonster monster)
	{
		return findCreatureZone(duelist, monster) != null;
	}

	private void sendFromFieldToDiscard(YugiohDuelist duelist, YugiohMonster monster)
	{
		CreatureZone zone = findCreatureZone(duelist, monster);

		if (zone == null)
		{
			throw new IllegalStateException("Monster \"" + monster.getName() + "\" is not on your field");
		}

		zone.remove(monster);
		duelist.discard(monster);
	}

	private CreatureZone findCreatureZone(YugiohDuelist duelist, Card card)
	{
		for (CreatureZone zone : duelist.getCreatureZones())
		{
			if (zone.getOccupiedCard().orElse(null) == card)
			{
				return zone;
			}
		}

		return null;
	}

	private HelperZone findHelperZone(YugiohDuelist duelist, Card card)
	{
		for (HelperZone zone : duelist.getHelperZones())
		{
			if (zone.getOccupiedCard().orElse(null) == card)
			{
				return zone;
			}
		}

		return null;
	}

	private YugiohDuelist findHelperZoneOwner(Card card)
	{
		if (findHelperZone(duelist1, card) != null)
		{
			return duelist1;
		}

		if (findHelperZone(duelist2, card) != null)
		{
			return duelist2;
		}

		return null;
	}

	private boolean isOccupiedByAnyOf(CreatureZone zone, List<YugiohMonster> monsters)
	{
		Card occupied = zone.getOccupiedCard().orElse(null);

		for (YugiohMonster monster : monsters)
		{
			if (occupied == monster)
			{
				return true;
			}
		}

		return false;
	}

	private CreatureZone creatureZone(YugiohDuelist duelist, int index)
	{
		List<CreatureZone> zones = new ArrayList<>(duelist.getCreatureZones());

		if (index < 0 || index >= zones.size())
		{
			throw new IllegalStateException("Monster zone index " + index + " is out of range, this duelist has " + zones.size() + " monster zones");
		}

		return zones.get(index);
	}

	private HelperZone helperZone(YugiohDuelist duelist, int index)
	{
		List<HelperZone> zones = new ArrayList<>(duelist.getHelperZones());

		if (index < 0 || index >= zones.size())
		{
			throw new IllegalStateException("Spell/trap zone index " + index + " is out of range, this duelist has " + zones.size() + " spell/trap zones");
		}

		return zones.get(index);
	}

	private YugiohDuelist asYugiohDuelist(Duelist duelist)
	{
		if (duelist == duelist1)
		{
			return duelist1;
		}

		if (duelist == duelist2)
		{
			return duelist2;
		}

		throw new IllegalStateException("The duel does not know this duelist");
	}

	private YugiohDuelist opponentOf(YugiohDuelist duelist)
	{
		return duelist == duelist1 ? duelist2 : duelist1;
	}

	private String describe(Card card)
	{
		if (card instanceof YugiohSpell spell)
		{
			return "Spell \"" + spell.getName() + "\"";
		}

		if (card instanceof YugiohTrap trap)
		{
			return "Trap \"" + trap.getName() + "\"";
		}

		if (card instanceof YugiohMonster monster)
		{
			return "Monster \"" + monster.getName() + "\"";
		}

		return "Card " + card;
	}
}

package io.github.amrabdalla.yugioh.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.helper.HelperCard;
import io.github.amrabdalla.cardengine.card.zone.CreatureZone;
import io.github.amrabdalla.cardengine.card.zone.HelperZone;
import io.github.amrabdalla.cardengine.duelist.core.Duelist;
import io.github.amrabdalla.cardengine.game.Game;
import io.github.amrabdalla.cardengine.game.GameContext;
import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;
import io.github.amrabdalla.yugioh.card.BattlePosition;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.card.YugiohSpell;
import io.github.amrabdalla.yugioh.card.YugiohTrap;
import io.github.amrabdalla.yugioh.game.BattleResolver.BattleOutcome;
import io.github.amrabdalla.yugioh.turn.YugiohTurnFactory;

/**
 * Orchestrates a basic Yu-Gi-Oh! duel on top of the generic card engine.
 *
 * Enforced rules:
 * - Turn structure: Draw, Main, Battle phases
 * - One normal summon (or set) per turn, with tributes for level 5+ monsters
 * - ATK/DEF battle resolution with battle damage to life points
 * - No attacks on the very first turn of the duel
 * - Each monster may attack once per turn
 * - Spells activate from hand or set; traps must be set and cannot be
 *   activated during the turn they were set
 * - A duelist loses at 0 life points, or when unable to draw (deck-out)
 */
public class YugiohGame
{
	private final Game game;
	private final Duelist duelist1;
	private final Duelist duelist2;

	private boolean normalSummonUsedThisTurn;
	private boolean drawnThisTurn;
	private Duelist winner;

	public YugiohGame(Duelist duelist1, Duelist duelist2)
	{
		this.duelist1 = duelist1;
		this.duelist2 = duelist2;
		this.game = new Game(duelist1, duelist2, YugiohTurnFactory.getInstance().createYugiohTurn());
	}

	// ------------------------------------------------------------------
	// Accessors
	// ------------------------------------------------------------------

	public Duelist getCurrentDuelist()
	{
		return game.getCurrentDuelist();
	}

	public Duelist getOpponentDuelist()
	{
		return game.getOpponentDuelist();
	}

	public GameContext getContext()
	{
		return game.getContext();
	}

	public TurnPhase getCurrentPhase()
	{
		return game.getCurrenTurnPhase();
	}

	public int getTurnNumber()
	{
		return game.getTurnNumber();
	}

	public boolean isOver()
	{
		return winner != null;
	}

	public Optional<Duelist> getWinner()
	{
		return Optional.ofNullable(winner);
	}

	// ------------------------------------------------------------------
	// Turn flow
	// ------------------------------------------------------------------

	/**
	 * Performs the current duelist's draw for the turn. If the deck cannot
	 * supply the required cards, the duelist loses by deck-out.
	 */
	public int drawForTurn()
	{
		requireNotOver();
		int required = getCurrentPhase().drawCount();
		require(required > 0, "The current phase does not allow drawing");
		require(!drawnThisTurn, "Already drawn this turn");

		int drawn = getCurrentDuelist().draw(required);
		drawnThisTurn = true;

		if (drawn < required)
		{
			winner = getOpponentDuelist();
		}

		return drawn;
	}

	public void nextPhase()
	{
		requireNotOver();
		int turnBefore = game.getTurnNumber();
		game.moveToNextPhase();

		if (game.getTurnNumber() != turnBefore)
		{
			onTurnEnded();
		}
	}

	public void endTurn()
	{
		requireNotOver();
		game.endTurn();
		onTurnEnded();
	}

	private void onTurnEnded()
	{
		normalSummonUsedThisTurn = false;
		drawnThisTurn = false;
		resetAttackFlags(duelist1);
		resetAttackFlags(duelist2);
	}

	private void resetAttackFlags(Duelist duelist)
	{
		for (CreatureZone zone : duelist.getCreatureZones())
		{
			zone.getOccupiedCard().ifPresent(card ->
			{
				if (card instanceof YugiohMonster monster)
				{
					monster.resetAttackFlag();
				}
			});
		}
	}

	// ------------------------------------------------------------------
	// Summoning
	// ------------------------------------------------------------------

	public void normalSummon(YugiohMonster monster, int zoneIndex, BattlePosition position)
	{
		summon(monster, zoneIndex, position, List.of());
	}

	public void tributeSummon(YugiohMonster monster, int zoneIndex, BattlePosition position, List<YugiohMonster> tributes)
	{
		summon(monster, zoneIndex, position, tributes);
	}

	/**
	 * Sets a monster face-down. Counts as the turn's normal summon.
	 */
	public void setMonster(YugiohMonster monster, int zoneIndex, List<YugiohMonster> tributes)
	{
		summon(monster, zoneIndex, BattlePosition.SET, tributes);
	}

	private void summon(YugiohMonster monster, int zoneIndex, BattlePosition position, List<YugiohMonster> tributes)
	{
		requireNotOver();
		require(getCurrentPhase().allowsSummons(), "The current phase does not allow summons");
		require(!normalSummonUsedThisTurn, "Only one normal summon or set per turn");

		Duelist duelist = getCurrentDuelist();
		require(duelist.getHand().contains(monster), "The monster must be in the hand");
		require(tributes.size() == monster.getRequiredTributes(),
				"A level " + monster.getLevel() + " monster requires "
						+ monster.getRequiredTributes() + " tribute(s), got " + tributes.size());
		require(new HashSet<>(tributes).size() == tributes.size(), "Tributes must be distinct monsters");

		for (YugiohMonster tribute : tributes)
		{
			require(findCreatureZoneOf(duelist, tribute) != null, "Tributes must be on your own field");
		}

		CreatureZone zone = creatureZoneAt(duelist, zoneIndex);
		boolean zoneAvailable = !zone.isOccupied()
				|| zone.getOccupiedCard().map(occupant -> tributes.contains(occupant)).orElse(false);
		require(zoneAvailable, "The chosen monster zone is occupied");

		for (YugiohMonster tribute : tributes)
		{
			CreatureZone tributeZone = findCreatureZoneOf(duelist, tribute);
			tributeZone.remove(tribute);
			duelist.discard(tribute);
		}

		duelist.getHand().remove(monster);
		monster.setPosition(position);
		monster.play(zone);
		normalSummonUsedThisTurn = true;
	}

	// ------------------------------------------------------------------
	// Spells and traps
	// ------------------------------------------------------------------

	/**
	 * Places a spell or trap card face-down in one of the current duelist's
	 * spell/trap zones.
	 */
	public void setHelperCard(HelperCard card, int zoneIndex)
	{
		requireNotOver();
		require(getCurrentPhase().allowsHelperCardsActivation(),
				"The current phase does not allow playing spell or trap cards");

		Duelist duelist = getCurrentDuelist();
		require(duelist.getHand().contains(card), "The card must be in the hand");

		HelperZone zone = helperZoneAt(duelist, zoneIndex);
		require(zone.canAccept(card), "The chosen spell/trap zone is occupied");

		duelist.getHand().remove(card);
		card.play(zone);

		if (card instanceof YugiohTrap trap)
		{
			trap.markSet(game.getTurnNumber());
		}
	}

	/**
	 * Activates a spell from the current duelist's hand or from one of their
	 * spell/trap zones. The spell is sent to the discard pile afterwards.
	 */
	public void activateSpell(YugiohSpell spell, Optional<Card> target)
	{
		requireNotOver();
		require(getCurrentPhase().allowsHelperCardsActivation(),
				"The current phase does not allow spell activation");

		Duelist owner = getCurrentDuelist();
		boolean inHand = owner.getHand().contains(spell);
		HelperZone zone = inHand ? null : findHelperZoneOf(owner, spell);
		require(inHand || zone != null, "The spell must be in your hand or set on your field");
		require(spell.getEffect().canApply(spell, target, getContext()),
				"The spell's effect cannot be applied");

		spell.getEffect().apply(spell, target, getContext());

		if (inHand)
		{
			owner.getHand().remove(spell);
		}
		else
		{
			zone.remove(spell);
		}

		owner.discard(spell);
		afterAction();
	}

	/**
	 * Activates a set trap belonging to either duelist. A trap cannot be
	 * activated during the turn it was set. The trap is sent to its owner's
	 * discard pile afterwards.
	 */
	public void activateTrap(YugiohTrap trap, Optional<Card> target)
	{
		requireNotOver();
		require(getCurrentPhase().allowsTrapsActivation(),
				"The current phase does not allow trap activation");

		Duelist owner = helperOwnerOf(trap);
		require(owner != null, "The trap must be set on the field");
		require(trap.canActivate(game.getTurnNumber()),
				"A trap cannot be activated during the turn it was set");
		require(trap.getEffect().canApply(trap, target, getContext()),
				"The trap's effect cannot be applied");

		trap.getEffect().apply(trap, target, getContext());

		findHelperZoneOf(owner, trap).remove(trap);
		owner.discard(trap);
		afterAction();
	}

	// ------------------------------------------------------------------
	// Battle
	// ------------------------------------------------------------------

	public void declareAttack(YugiohMonster attacker, YugiohMonster target)
	{
		validateAttackDeclaration(attacker);

		Duelist defender = getOpponentDuelist();
		require(findCreatureZoneOf(defender, target) != null, "The target must be on the opponent's field");

		BattleOutcome outcome = BattleResolver.resolve(attacker, target);
		attacker.markAttacked();
		applyBattleDamage(outcome);
		afterAction();
	}

	public void declareDirectAttack(YugiohMonster attacker)
	{
		validateAttackDeclaration(attacker);

		Duelist defender = getOpponentDuelist();
		require(!controlsMonsters(defender), "Cannot attack directly while the opponent controls monsters");

		BattleOutcome outcome = BattleResolver.resolveDirectAttack(attacker);
		attacker.markAttacked();
		applyBattleDamage(outcome);
		afterAction();
	}

	private void validateAttackDeclaration(YugiohMonster attacker)
	{
		requireNotOver();
		require(getCurrentPhase().allowsAttacks(), "The current phase does not allow attacks");
		require(game.getTurnNumber() > 1, "No attacks are allowed on the first turn of the duel");
		require(findCreatureZoneOf(getCurrentDuelist(), attacker) != null, "The attacker must be on your field");
		require(attacker.getPosition() == BattlePosition.ATTACK, "Only attack-position monsters can attack");
		require(!attacker.hasAttackedThisTurn(), "This monster has already attacked this turn");
	}

	private void applyBattleDamage(BattleOutcome outcome)
	{
		if (outcome.damageToAttackingDuelist() > 0)
		{
			getCurrentDuelist().takeDamage(outcome.damageToAttackingDuelist());
		}

		if (outcome.damageToDefendingDuelist() > 0)
		{
			getOpponentDuelist().takeDamage(outcome.damageToDefendingDuelist());
		}
	}

	// ------------------------------------------------------------------
	// State-based cleanup and win conditions
	// ------------------------------------------------------------------

	private void afterAction()
	{
		cleanupDestroyedMonsters();
		checkLifePoints();
	}

	private void cleanupDestroyedMonsters()
	{
		cleanupDestroyedMonsters(duelist1);
		cleanupDestroyedMonsters(duelist2);
	}

	private void cleanupDestroyedMonsters(Duelist duelist)
	{
		for (CreatureZone zone : duelist.getCreatureZones())
		{
			Optional<Card> occupant = zone.getOccupiedCard();

			if (occupant.isPresent() && occupant.get() instanceof Creature creature && creature.isDead())
			{
				zone.remove(occupant.get());
				creature.triggerDeathEvents();
				duelist.discard(occupant.get());
			}
		}
	}

	private void checkLifePoints()
	{
		if (winner != null)
		{
			return;
		}

		if (duelist1.getLifePoints() <= 0)
		{
			winner = duelist2;
		}
		else if (duelist2.getLifePoints() <= 0)
		{
			winner = duelist1;
		}
	}

	// ------------------------------------------------------------------
	// Helpers
	// ------------------------------------------------------------------

	private CreatureZone creatureZoneAt(Duelist duelist, int zoneIndex)
	{
		List<CreatureZone> zones = new ArrayList<>(duelist.getCreatureZones());
		require(zoneIndex >= 0 && zoneIndex < zones.size(), "Invalid monster zone index: " + zoneIndex);
		return zones.get(zoneIndex);
	}

	private HelperZone helperZoneAt(Duelist duelist, int zoneIndex)
	{
		List<HelperZone> zones = new ArrayList<>(duelist.getHelperZones());
		require(zoneIndex >= 0 && zoneIndex < zones.size(), "Invalid spell/trap zone index: " + zoneIndex);
		return zones.get(zoneIndex);
	}

	private CreatureZone findCreatureZoneOf(Duelist duelist, Card card)
	{
		for (CreatureZone zone : duelist.getCreatureZones())
		{
			if (zone.getOccupiedCard().filter(occupant -> occupant == card).isPresent())
			{
				return zone;
			}
		}

		return null;
	}

	private HelperZone findHelperZoneOf(Duelist duelist, Card card)
	{
		for (HelperZone zone : duelist.getHelperZones())
		{
			if (zone.getOccupiedCard().filter(occupant -> occupant == card).isPresent())
			{
				return zone;
			}
		}

		return null;
	}

	private Duelist helperOwnerOf(Card card)
	{
		if (findHelperZoneOf(duelist1, card) != null)
		{
			return duelist1;
		}

		if (findHelperZoneOf(duelist2, card) != null)
		{
			return duelist2;
		}

		return null;
	}

	private boolean controlsMonsters(Duelist duelist)
	{
		for (CreatureZone zone : duelist.getCreatureZones())
		{
			if (zone.isOccupied())
			{
				return true;
			}
		}

		return false;
	}

	private void requireNotOver()
	{
		require(!isOver(), "The duel is already over");
	}

	private static void require(boolean condition, String message)
	{
		if (!condition)
		{
			throw new IllegalStateException(message);
		}
	}
}

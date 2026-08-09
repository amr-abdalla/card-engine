package io.github.amrabdalla.yugioh.tests;

import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.advanceTo;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.cardIn;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.creatureZone;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.duelistHolding;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.helperZone;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.monster;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.passTurnBackAround;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.spell;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.trap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.effect.core.EffectFactory;
import io.github.amrabdalla.cardengine.card.effect.core.EffectID;
import io.github.amrabdalla.yugioh.YugiohRules;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.card.YugiohSpell;
import io.github.amrabdalla.yugioh.card.YugiohTrap;
import io.github.amrabdalla.yugioh.duelist.YugiohDuelist;
import io.github.amrabdalla.yugioh.effect.DestroyTargetMonster;
import io.github.amrabdalla.yugioh.effect.InflictDamage;
import io.github.amrabdalla.yugioh.game.YugiohGame;
import io.github.amrabdalla.yugioh.turn.YugiohPhaseID;

public class SpellTrapTest
{
	private static final int ATTACK_BOOST = 800;
	private static final int BURN_DAMAGE = 1000;

	private YugiohMonster myMonster;
	private YugiohMonster opponentMonster;
	private YugiohSpell boostSpell;
	private YugiohSpell secondBoostSpell;
	private YugiohSpell burnSpell;
	private YugiohTrap destroyTrap;

	private YugiohDuelist duelist1;
	private YugiohDuelist duelist2;
	private YugiohGame game;

	@BeforeEach
	void setUp()
	{
		myMonster = monster("Celtic Guardian", 4, 1400, 1200);
		opponentMonster = monster("Battle Ox", 4, 1700, 1000);

		boostSpell = spell("Sword of Deep-Seated", EffectFactory.getInstance().getEffect(EffectID.ModifyTargetAttack, ATTACK_BOOST));
		secondBoostSpell = spell("Legendary Sword", EffectFactory.getInstance().getEffect(EffectID.ModifyTargetAttack, ATTACK_BOOST));
		burnSpell = spell("Ookazi", new InflictDamage(BURN_DAMAGE));
		destroyTrap = trap("Trap Hole", new DestroyTargetMonster());

		duelist1 = duelistHolding("Yugi", myMonster, boostSpell, secondBoostSpell, burnSpell, destroyTrap);
		duelist2 = duelistHolding("Kaiba", opponentMonster);

		game = new YugiohGame(duelist1, duelist2);
	}

	private void toMainPhase()
	{
		advanceTo(game, YugiohPhaseID.MAIN);
	}

	@Test
	void aSpellFromTheHandAppliesItsEffectAndGoesToTheDiscardPile()
	{
		toMainPhase();
		game.normalSummon(myMonster, 0);

		int attackBefore = myMonster.getAttack();
		game.activateSpell(boostSpell, Optional.of(myMonster));

		assertEquals(attackBefore + ATTACK_BOOST, myMonster.getAttack(), "The spell should raise the monster's ATK");
		assertFalse(duelist1.getHand().contains(boostSpell), "An activated spell should leave the hand");
		assertTrue(duelist1.getDiscardPile().contains(boostSpell), "A used spell goes to its owner's discard pile");
	}

	@Test
	void aBurnSpellTakesLifePointsOffTheOpponent()
	{
		toMainPhase();
		game.activateSpell(burnSpell, Optional.empty());

		assertEquals(YugiohRules.STARTING_LIFE_POINTS - BURN_DAMAGE, duelist2.getLifePoints(), "The opponent should lose the burn damage");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist1.getLifePoints(), "The duelist activating the burn takes no damage");
		assertTrue(duelist1.getDiscardPile().contains(burnSpell), "A used spell goes to its owner's discard pile");
	}

	@Test
	void aSpellThatNeedsATargetIsRefusedWithoutOne()
	{
		toMainPhase();

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.activateSpell(boostSpell, Optional.empty()));

		assertTrue(error.getMessage().contains("cannot resolve"), "The refusal should say the spell cannot resolve: " + error.getMessage());
		assertTrue(duelist1.getHand().contains(boostSpell), "A refused spell stays in hand");
		assertFalse(duelist1.getDiscardPile().contains(boostSpell), "A refused spell is not discarded");
	}

	@Test
	void aSpellIsRefusedAgainstAnInvalidTarget()
	{
		toMainPhase();

		Card notACreature = burnSpell;
		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.activateSpell(boostSpell, Optional.of(notACreature)));

		assertTrue(error.getMessage().contains("cannot resolve"), "The refusal should say the spell cannot resolve: " + error.getMessage());
		assertTrue(duelist1.getHand().contains(boostSpell), "A refused spell stays in hand");
	}

	@Test
	void aSpellIsRefusedOutsideTheMainPhase()
	{
		assertEquals(YugiohPhaseID.DRAW, game.getCurrentPhaseID(), "A duel starts in the draw phase");

		IllegalStateException drawPhaseError = assertThrows(IllegalStateException.class, () -> game.activateSpell(burnSpell, Optional.empty()));
		assertTrue(drawPhaseError.getMessage().contains("main phase"), "The refusal should name the main phase: " + drawPhaseError.getMessage());

		advanceTo(game, YugiohPhaseID.BATTLE);

		assertThrows(IllegalStateException.class, () -> game.activateSpell(burnSpell, Optional.empty()), "Spells cannot be activated in the battle phase");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist2.getLifePoints(), "No burn damage should have been dealt");
	}

	@Test
	void aSetSpellCanBeActivatedOnALaterTurn()
	{
		toMainPhase();
		game.setHelperCard(boostSpell, 0);

		assertSame(boostSpell, cardIn(helperZone(duelist1, 0)), "The set spell should occupy the spell/trap zone");
		assertFalse(duelist1.getHand().contains(boostSpell), "A set spell should leave the hand");

		passTurnBackAround(game);

		toMainPhase();
		game.normalSummon(myMonster, 0);

		int attackBefore = myMonster.getAttack();
		game.activateSpell(boostSpell, Optional.of(myMonster));

		assertEquals(attackBefore + ATTACK_BOOST, myMonster.getAttack(), "The set spell should still apply its effect");
		assertFalse(helperZone(duelist1, 0).isOccupied(), "The spell zone should be free after the spell resolves");
		assertTrue(duelist1.getDiscardPile().contains(boostSpell), "A used spell goes to its owner's discard pile");
	}

	@Test
	void aTrapCannotBeActivatedFromTheHand()
	{
		toMainPhase();

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.activateTrap(destroyTrap, Optional.empty()));

		assertTrue(error.getMessage().contains("from the hand"), "The refusal should say a trap has to be set first: " + error.getMessage());
		assertTrue(duelist1.getHand().contains(destroyTrap), "A refused trap stays in hand");
	}

	@Test
	void aTrapCannotBeActivatedOnTheTurnItWasSet()
	{
		toMainPhase();
		game.setHelperCard(destroyTrap, 0);

		assertTrue(destroyTrap.isSet(), "The trap should be marked as set");
		assertEquals(1, destroyTrap.getSetTurn(), "The trap should remember it was set on turn 1");
		assertFalse(destroyTrap.canActivate(game.getTurnNumber()), "A trap is not live on the turn it was set");

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.activateTrap(destroyTrap, Optional.of(myMonster)));

		assertTrue(error.getMessage().contains("cannot be activated before turn 2"), "The refusal should name the turn the trap becomes live: " + error.getMessage());
		assertSame(destroyTrap, cardIn(helperZone(duelist1, 0)), "A refused trap stays on the field");
	}

	@Test
	void aSetTrapDestroysAMonsterOnALaterTurn()
	{
		AtomicBoolean deathEventFired = new AtomicBoolean(false);
		opponentMonster.addDeathEvent(() -> deathEventFired.set(true));

		toMainPhase();
		game.setHelperCard(destroyTrap, 0);
		game.endTurn();

		toMainPhase();
		game.normalSummon(opponentMonster, 0);

		assertTrue(destroyTrap.canActivate(game.getTurnNumber()), "The trap should be live on the turn after it was set");

		// The trap belongs to the duelist whose turn it is not, which is the point of a trap.
		game.activateTrap(destroyTrap, Optional.of(opponentMonster));

		assertTrue(opponentMonster.isDead(), "The trap should destroy the targeted monster");
		assertFalse(creatureZone(duelist2, 0).isOccupied(), "The destroyed monster should be swept off the field");
		assertTrue(duelist2.getDiscardPile().contains(opponentMonster), "The destroyed monster goes to its own owner's discard pile");
		assertTrue(deathEventFired.get(), "Sweeping a destroyed monster should trigger its death events");
		assertFalse(helperZone(duelist1, 0).isOccupied(), "The trap zone should be free after the trap resolves");
		assertTrue(duelist1.getDiscardPile().contains(destroyTrap), "A used trap goes to its owner's discard pile");
	}

	@Test
	void aSetTrapCanBeActivatedDuringTheBattlePhase()
	{
		toMainPhase();
		game.setHelperCard(destroyTrap, 0);
		game.endTurn();

		toMainPhase();
		game.normalSummon(opponentMonster, 0);
		advanceTo(game, YugiohPhaseID.BATTLE);

		game.activateTrap(destroyTrap, Optional.of(opponentMonster));

		assertTrue(opponentMonster.isDead(), "A trap can be activated in the battle phase");
		assertTrue(duelist1.getDiscardPile().contains(destroyTrap), "A used trap goes to its owner's discard pile");
	}

	@Test
	void settingIntoAnOccupiedSpellTrapZoneIsRefused()
	{
		toMainPhase();
		game.setHelperCard(boostSpell, 0);

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.setHelperCard(secondBoostSpell, 0));

		assertTrue(error.getMessage().contains("occupied"), "The refusal should say the zone is occupied: " + error.getMessage());
		assertSame(boostSpell, cardIn(helperZone(duelist1, 0)), "The card already set should stay in the zone");
		assertTrue(duelist1.getHand().contains(secondBoostSpell), "A refused set leaves the card in hand");
	}

	@Test
	void aSpellCannotBeActivatedWithoutAGameContext()
	{
		// The engine's context-free target(Card) cannot carry out a Yu-Gi-Oh! activation, so it is
		// refused outright rather than resolving half of the effect.
		assertThrows(UnsupportedOperationException.class, () -> boostSpell.target(myMonster), "A spell cannot be resolved without a game context");
		assertThrows(UnsupportedOperationException.class, () -> destroyTrap.target(myMonster), "A trap cannot be resolved without a game context");
	}
}

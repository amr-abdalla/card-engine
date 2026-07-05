package io.github.amrabdalla.yugioh.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.deckOf;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.gameWith;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.toMainPhase;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.amrabdalla.cardengine.card.effect.impl.ModifyTargetAttack;
import io.github.amrabdalla.yugioh.YugiohRules;
import io.github.amrabdalla.yugioh.card.BattlePosition;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.card.YugiohSpell;
import io.github.amrabdalla.yugioh.card.YugiohTrap;
import io.github.amrabdalla.yugioh.effect.DestroyTargetMonster;
import io.github.amrabdalla.yugioh.effect.InflictDamage;
import io.github.amrabdalla.yugioh.game.YugiohGame;

public class SpellTrapTest
{
	private YugiohMonster warrior;
	private YugiohMonster invader;
	private YugiohSpell powerSpell;
	private YugiohSpell burnSpell;
	private YugiohTrap destructionTrap;
	private YugiohGame game;

	@BeforeEach
	void setup()
	{
		warrior = new YugiohMonster("Stone Warrior", 4, 1800, 1200);
		invader = new YugiohMonster("Invader", 4, 1600, 1400);
		powerSpell = new YugiohSpell("Power Charm", new ModifyTargetAttack(700));
		burnSpell = new YugiohSpell("Fireball", new InflictDamage(500));
		destructionTrap = new YugiohTrap("Pitfall", new DestroyTargetMonster());

		game = gameWith(deckOf(warrior, powerSpell, burnSpell, destructionTrap), deckOf(invader));
	}

	@Test
	void testSpellFromHandBoostsMonsterAndIsDiscarded()
	{
		toMainPhase(game);
		game.normalSummon(warrior, 0, BattlePosition.ATTACK);

		game.activateSpell(powerSpell, Optional.of(warrior));

		assertEquals(1800 + 700, warrior.getAttack(), "The spell should raise the monster's ATK");
		assertFalse(game.getCurrentDuelist().getHand().contains(powerSpell),
				"The spell should leave the hand");
		assertTrue(game.getCurrentDuelist().getDiscardPile().contains(powerSpell),
				"The spell should be discarded after resolving");
	}

	@Test
	void testBurnSpellDamagesOpponent()
	{
		toMainPhase(game);

		game.activateSpell(burnSpell, Optional.empty());

		assertEquals(YugiohRules.STARTING_LIFE_POINTS - 500,
				game.getOpponentDuelist().getLifePoints(),
				"The burn spell should damage the opponent's life points");
	}

	@Test
	void testSpellWithInvalidTargetRejected()
	{
		toMainPhase(game);

		assertThrows(IllegalStateException.class,
				() -> game.activateSpell(powerSpell, Optional.empty()),
				"A targeting spell without a target should be rejected");
	}

	@Test
	void testSpellRejectedOutsideMainPhase()
	{
		assertThrows(IllegalStateException.class,
				() -> game.activateSpell(burnSpell, Optional.empty()),
				"Spells should not activate during the draw phase");
	}

	@Test
	void testSetSpellCanBeActivatedLater()
	{
		toMainPhase(game);
		game.setHelperCard(burnSpell, 0);
		game.endTurn();
		game.endTurn();
		toMainPhase(game);

		game.activateSpell(burnSpell, Optional.empty());

		assertEquals(YugiohRules.STARTING_LIFE_POINTS - 500,
				game.getOpponentDuelist().getLifePoints());
		assertFalse(new java.util.ArrayList<>(game.getCurrentDuelist().getHelperZones()).get(0).isOccupied(),
				"The spell should leave its zone after resolving");
		assertTrue(game.getCurrentDuelist().getDiscardPile().contains(burnSpell));
	}

	@Test
	void testTrapCannotActivateFromHand()
	{
		toMainPhase(game);

		assertThrows(IllegalStateException.class,
				() -> game.activateTrap(destructionTrap, Optional.of(warrior)),
				"An unset trap should not be activatable");
	}

	@Test
	void testTrapCannotActivateOnTheTurnItWasSet()
	{
		toMainPhase(game);
		game.setHelperCard(destructionTrap, 0);

		assertThrows(IllegalStateException.class,
				() -> game.activateTrap(destructionTrap, Optional.of(warrior)),
				"A trap should not activate during the turn it was set");
	}

	@Test
	void testTrapDestroysMonsterOnLaterTurn()
	{
		// Turn 1: duelist 1 sets the trap
		toMainPhase(game);
		game.setHelperCard(destructionTrap, 0);
		game.endTurn();

		// Turn 2: duelist 2 summons; duelist 1 answers with the set trap
		toMainPhase(game);
		game.normalSummon(invader, 0, BattlePosition.ATTACK);

		game.activateTrap(destructionTrap, Optional.of(invader));

		assertTrue(invader.isDead(), "The trap should destroy the targeted monster");
		assertTrue(game.getCurrentDuelist().getDiscardPile().contains(invader),
				"The destroyed monster should be discarded");
		assertTrue(game.getOpponentDuelist().getDiscardPile().contains(destructionTrap),
				"The used trap should go to its owner's discard pile");
		assertFalse(game.getCurrentDuelist().getCreatureZones().iterator().next().isOccupied(),
				"The destroyed monster's zone should be freed");
	}

	@Test
	void testSetHelperCardIntoOccupiedZoneRejected()
	{
		toMainPhase(game);
		game.setHelperCard(destructionTrap, 0);

		assertThrows(IllegalStateException.class,
				() -> game.setHelperCard(burnSpell, 0),
				"Setting into an occupied spell/trap zone should be rejected");
	}
}

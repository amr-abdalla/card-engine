package io.github.amrabdalla.yugioh.tests;

import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.advanceTo;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.duelistHolding;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.duelistWithDeck;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.monster;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.spell;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.amrabdalla.yugioh.YugiohRules;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.card.YugiohSpell;
import io.github.amrabdalla.yugioh.duelist.YugiohDuelist;
import io.github.amrabdalla.yugioh.effect.InflictDamage;
import io.github.amrabdalla.yugioh.game.YugiohGame;
import io.github.amrabdalla.yugioh.turn.YugiohPhaseID;

public class GameFlowTest
{
	private static final int DECK_SIZE = 10;
	private static final int OPENING_HAND = 5;

	private YugiohGame gameWithDecks()
	{
		return new YugiohGame(duelistWithDeck("Yugi", DECK_SIZE, OPENING_HAND), duelistWithDeck("Kaiba", DECK_SIZE, OPENING_HAND));
	}

	@Test
	void aDuelistStartsWithTheYugiohDefaults()
	{
		YugiohDuelist duelist = duelistWithDeck("Yugi", 40, YugiohRules.OPENING_HAND_SIZE);

		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist.getLifePoints(), "A duelist starts on 8000 life points");
		assertEquals(YugiohRules.MONSTER_ZONE_COUNT, duelist.getCreatureZones().size(), "A duelist has five monster zones");
		assertEquals(YugiohRules.SPELL_TRAP_ZONE_COUNT, duelist.getHelperZones().size(), "A duelist has five spell/trap zones");
		assertEquals(YugiohRules.OPENING_HAND_SIZE, duelist.getHand().size(), "A duelist opens on five cards");
		assertEquals(40 - YugiohRules.OPENING_HAND_SIZE, duelist.getDeckSize(), "The opening hand comes out of the deck");
	}

	@Test
	void phasesProgressAndTheTurnRotatesToTheOtherDuelist()
	{
		YugiohGame game = gameWithDecks();
		YugiohDuelist first = game.getCurrentDuelist();
		YugiohDuelist second = game.getOpponentDuelist();

		assertEquals(YugiohPhaseID.DRAW, game.getCurrentPhaseID(), "A turn opens on the draw phase");
		assertEquals(1, game.getTurnNumber(), "The duel starts on turn 1");

		game.nextPhase();
		assertEquals(YugiohPhaseID.MAIN, game.getCurrentPhaseID(), "The draw phase is followed by the main phase");
		assertEquals(1, game.getTurnNumber(), "Moving between phases does not change the turn");

		game.nextPhase();
		assertEquals(YugiohPhaseID.BATTLE, game.getCurrentPhaseID(), "The main phase is followed by the battle phase");
		assertSame(first, game.getCurrentDuelist(), "The turn belongs to the same duelist throughout");

		game.nextPhase();
		assertEquals(YugiohPhaseID.DRAW, game.getCurrentPhaseID(), "Moving past the last phase starts the next turn");
		assertEquals(2, game.getTurnNumber(), "The turn number goes up when the turn rolls over");
		assertSame(second, game.getCurrentDuelist(), "The other duelist takes turn 2");

		game.nextPhase();
		game.nextPhase();
		game.nextPhase();
		assertEquals(3, game.getTurnNumber(), "A third turn follows");
		assertSame(first, game.getCurrentDuelist(), "The turn comes back round to the first duelist");
	}

	@Test
	void endingATurnStartsTheNextOneFromWhicheverPhaseItIsIn()
	{
		YugiohGame game = gameWithDecks();
		YugiohDuelist first = game.getCurrentDuelist();

		advanceTo(game, YugiohPhaseID.MAIN);
		game.endTurn();

		assertEquals(YugiohPhaseID.DRAW, game.getCurrentPhaseID(), "A new turn opens on the draw phase");
		assertEquals(2, game.getTurnNumber(), "Ending a turn moves on to the next one");
		assertFalse(first == game.getCurrentDuelist(), "The other duelist takes the next turn");
	}

	@Test
	void exactlyOneCardIsDrawnPerTurn()
	{
		YugiohGame game = gameWithDecks();
		YugiohDuelist duelist = game.getCurrentDuelist();

		int handBefore = duelist.getHand().size();
		int deckBefore = duelist.getDeckSize();

		assertEquals(YugiohRules.CARDS_DRAWN_PER_TURN, game.drawForTurn(), "One card is drawn for the turn");
		assertEquals(handBefore + 1, duelist.getHand().size(), "The drawn card should be in hand");
		assertEquals(deckBefore - 1, duelist.getDeckSize(), "The drawn card should have left the deck");
		assertTrue(game.hasDrawnThisTurn(), "The turn's draw should be marked as taken");
	}

	@Test
	void theTurnsDrawCannotBeTakenTwice()
	{
		YugiohGame game = gameWithDecks();
		game.drawForTurn();

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.drawForTurn());

		assertTrue(error.getMessage().contains("already been taken"), "The refusal should say the draw was already taken: " + error.getMessage());
		assertEquals(OPENING_HAND + 1, game.getCurrentDuelist().getHand().size(), "The second draw should not have gone through");
	}

	@Test
	void cardsCannotBeDrawnOutsideTheDrawPhase()
	{
		YugiohGame game = gameWithDecks();
		advanceTo(game, YugiohPhaseID.MAIN);

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.drawForTurn());

		assertTrue(error.getMessage().contains("draw phase"), "The refusal should name the draw phase: " + error.getMessage());
	}

	@Test
	void theDrawIsAvailableAgainOnTheNextTurn()
	{
		YugiohGame game = gameWithDecks();
		game.drawForTurn();
		game.endTurn();

		assertFalse(game.hasDrawnThisTurn(), "The draw should be available again on a new turn");

		YugiohDuelist second = game.getCurrentDuelist();
		int handBefore = second.getHand().size();

		assertEquals(1, game.drawForTurn(), "The second duelist draws for their own turn");
		assertEquals(handBefore + 1, second.getHand().size(), "The drawn card should be in the second duelist's hand");
	}

	@Test
	void theNormalSummonBecomesAvailableAgainOnTheNextTurn()
	{
		YugiohMonster mine = monster("Celtic Guardian", 4, 1400, 1200);
		YugiohGame game = new YugiohGame(duelistHolding("Yugi", mine), duelistHolding("Kaiba"));

		advanceTo(game, YugiohPhaseID.MAIN);
		game.normalSummon(mine, 0);

		assertTrue(game.hasUsedNormalSummon(), "The normal summon should be marked as used");

		game.endTurn();

		assertFalse(game.hasUsedNormalSummon(), "The normal summon should be available again on a new turn");
	}

	@Test
	void reducingLifePointsToZeroWinsTheDuel()
	{
		YugiohSpell overkill = spell("Final Flame", new InflictDamage(YugiohRules.STARTING_LIFE_POINTS + 1999));
		YugiohDuelist duelist1 = duelistHolding("Yugi", overkill);
		YugiohDuelist duelist2 = duelistHolding("Kaiba");
		YugiohGame game = new YugiohGame(duelist1, duelist2);

		advanceTo(game, YugiohPhaseID.MAIN);
		game.activateSpell(overkill, Optional.empty());

		assertEquals(0, duelist2.getLifePoints(), "Life points are clamped at zero rather than going negative");
		assertTrue(game.isOver(), "A duelist on zero life points loses the duel");
		assertSame(duelist1, game.getWinner().orElse(null), "The duelist who dealt the damage wins");
	}

	@Test
	void noFurtherActionsAreAcceptedOnceTheDuelIsOver()
	{
		YugiohSpell overkill = spell("Final Flame", new InflictDamage(YugiohRules.STARTING_LIFE_POINTS));
		YugiohMonster spare = monster("Celtic Guardian", 4, 1400, 1200);
		YugiohDuelist duelist1 = duelistHolding("Yugi", overkill, spare);
		YugiohGame game = new YugiohGame(duelist1, duelistHolding("Kaiba"));

		advanceTo(game, YugiohPhaseID.MAIN);
		game.activateSpell(overkill, Optional.empty());

		assertTrue(game.isOver(), "The duel should be over");

		assertThrows(IllegalStateException.class, () -> game.nextPhase(), "Phases cannot be advanced after the duel is over");
		assertThrows(IllegalStateException.class, () -> game.endTurn(), "Turns cannot be ended after the duel is over");
		assertThrows(IllegalStateException.class, () -> game.normalSummon(spare, 0), "Monsters cannot be summoned after the duel is over");
		assertThrows(IllegalStateException.class, () -> game.drawForTurn(), "Cards cannot be drawn after the duel is over");
	}

	@Test
	void failingToDrawLosesTheDuelByDeckOut()
	{
		// duelistHolding deals the whole deck into the opening hand, so there is nothing left to draw.
		YugiohDuelist duelist1 = duelistHolding("Yugi", monster("Celtic Guardian", 4, 1400, 1200));
		YugiohDuelist duelist2 = duelistWithDeck("Kaiba", DECK_SIZE, OPENING_HAND);
		YugiohGame game = new YugiohGame(duelist1, duelist2);

		assertEquals(0, duelist1.getDeckSize(), "The first duelist should have an empty deck");
		assertEquals(0, game.drawForTurn(), "Nothing can be drawn from an empty deck");

		assertTrue(game.isOver(), "Failing to draw loses the duel");
		assertSame(duelist2, game.getWinner().orElse(null), "The opponent of the decked-out duelist wins");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist1.getLifePoints(), "A deck-out is a loss without any life point change");
	}

	@Test
	void lifePointsClampAtZeroAndRejectNegativeAmounts()
	{
		YugiohDuelist duelist = duelistWithDeck("Yugi", DECK_SIZE, OPENING_HAND);

		duelist.takeDamage(YugiohRules.STARTING_LIFE_POINTS + 500);
		assertEquals(0, duelist.getLifePoints(), "Damage should never push life points below zero");

		duelist.gainLifePoints(1200);
		assertEquals(1200, duelist.getLifePoints(), "Gaining life points adds to the total");

		assertThrows(IllegalArgumentException.class, () -> duelist.takeDamage(-1), "Negative damage is not a legal amount");
		assertThrows(IllegalArgumentException.class, () -> duelist.gainLifePoints(-1), "Negative healing is not a legal amount");
	}
}

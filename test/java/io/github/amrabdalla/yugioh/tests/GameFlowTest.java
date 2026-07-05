package io.github.amrabdalla.yugioh.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.deckOf;

import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.amrabdalla.cardengine.duelist.core.Duelist;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.duelist.YugiohDuelist;
import io.github.amrabdalla.yugioh.game.YugiohGame;
import io.github.amrabdalla.yugioh.turn.YugiohPhaseID;

public class GameFlowTest
{
	private Duelist duelist1;
	private Duelist duelist2;
	private YugiohGame game;

	private static Duelist duelistWithDeckRemaining(int handSize, int deckRemaining)
	{
		java.util.List<io.github.amrabdalla.cardengine.card.core.Card> cards = new java.util.ArrayList<>();

		for (int i = 0; i < handSize + deckRemaining; i++)
		{
			cards.add(new YugiohMonster("Filler " + i, 4, 1000, 1000));
		}

		return new YugiohDuelist(cards, new Random(YugiohTestSupport.RANDOM_SEED), handSize);
	}

	@BeforeEach
	void setup()
	{
		duelist1 = duelistWithDeckRemaining(2, 3);
		duelist2 = duelistWithDeckRemaining(2, 3);
		game = new YugiohGame(duelist1, duelist2);
	}

	@Test
	void testPhaseProgressionAndTurnRotation()
	{
		assertEquals(YugiohPhaseID.DRAW.ordinal(), game.getCurrentPhase().getID());
		assertSame(duelist1, game.getCurrentDuelist());
		assertEquals(1, game.getTurnNumber());

		game.nextPhase();
		assertEquals(YugiohPhaseID.MAIN.ordinal(), game.getCurrentPhase().getID());

		game.nextPhase();
		assertEquals(YugiohPhaseID.BATTLE.ordinal(), game.getCurrentPhase().getID());

		game.nextPhase(); // wraps into the next turn
		assertEquals(YugiohPhaseID.DRAW.ordinal(), game.getCurrentPhase().getID());
		assertSame(duelist2, game.getCurrentDuelist());
		assertEquals(2, game.getTurnNumber());
	}

	@Test
	void testEndTurnSwitchesDuelistFromAnyPhase()
	{
		game.nextPhase(); // main phase
		game.endTurn();

		assertSame(duelist2, game.getCurrentDuelist());
		assertNotSame(game.getCurrentDuelist(), game.getOpponentDuelist());
		assertEquals(YugiohPhaseID.DRAW.ordinal(), game.getCurrentPhase().getID());
		assertEquals(2, game.getTurnNumber());
	}

	@Test
	void testDrawPhaseDrawsExactlyOneCard()
	{
		int handBefore = duelist1.getHand().size();
		int deckBefore = duelist1.getDeckSize();

		int drawn = game.drawForTurn();

		assertEquals(1, drawn);
		assertEquals(handBefore + 1, duelist1.getHand().size());
		assertEquals(deckBefore - 1, duelist1.getDeckSize());
	}

	@Test
	void testCannotDrawTwiceInOneTurn()
	{
		game.drawForTurn();

		assertThrows(IllegalStateException.class, game::drawForTurn,
				"Only one draw per turn should be allowed");
	}

	@Test
	void testCannotDrawOutsideDrawPhase()
	{
		game.nextPhase(); // main phase

		assertThrows(IllegalStateException.class, game::drawForTurn,
				"Drawing should only happen in the draw phase");
	}

	@Test
	void testDrawIsAvailableAgainNextTurn()
	{
		game.drawForTurn();
		game.endTurn();
		game.drawForTurn(); // duelist 2's draw

		assertEquals(3, duelist2.getHand().size());
	}

	@Test
	void testDeckOutLosesTheDuel()
	{
		Duelist emptyDeckDuelist = duelistWithDeckRemaining(2, 0);
		Duelist opponent = duelistWithDeckRemaining(2, 3);
		game = new YugiohGame(emptyDeckDuelist, opponent);

		int drawn = game.drawForTurn();

		assertEquals(0, drawn);
		assertTrue(game.isOver(), "Failing to draw should end the duel");
		assertSame(opponent, game.getWinner().orElseThrow(),
				"The opponent should win on deck-out");
		assertThrows(IllegalStateException.class, game::nextPhase,
				"No further actions should be allowed after the duel ends");
	}
}

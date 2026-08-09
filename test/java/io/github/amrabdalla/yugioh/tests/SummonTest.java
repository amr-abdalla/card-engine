package io.github.amrabdalla.yugioh.tests;

import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.advanceTo;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.cardIn;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.creatureZone;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.duelistHolding;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.monster;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.passTurnBackAround;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.amrabdalla.yugioh.YugiohRules;
import io.github.amrabdalla.yugioh.card.BattlePosition;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.duelist.YugiohDuelist;
import io.github.amrabdalla.yugioh.game.YugiohGame;
import io.github.amrabdalla.yugioh.turn.YugiohPhaseID;

public class SummonTest
{
	private YugiohMonster smallMonster;
	private YugiohMonster otherSmallMonster;
	private YugiohMonster secondTribute;
	private YugiohMonster levelFiveMonster;
	private YugiohMonster levelSevenMonster;
	private YugiohMonster opponentMonster;

	private YugiohDuelist duelist1;
	private YugiohDuelist duelist2;
	private YugiohGame game;

	@BeforeEach
	void setUp()
	{
		smallMonster = monster("Beta Magnet Warrior", 4, 1700, 1600);
		otherSmallMonster = monster("Gamma Magnet Warrior", 4, 1500, 1800);
		secondTribute = monster("Alpha Magnet Warrior", 4, 1400, 1700);
		levelFiveMonster = monster("Summoned Skull", 6, 2500, 1200);
		levelSevenMonster = monster("Blue-Eyes White Dragon", 8, 3000, 2500);
		opponentMonster = monster("Battle Ox", 4, 1700, 1000);

		duelist1 = duelistHolding("Yugi", smallMonster, otherSmallMonster, secondTribute, levelFiveMonster, levelSevenMonster);
		duelist2 = duelistHolding("Kaiba", opponentMonster);

		game = new YugiohGame(duelist1, duelist2);
	}

	private void toMainPhase()
	{
		advanceTo(game, YugiohPhaseID.MAIN);
	}

	@Test
	void normalSummonPlacesTheMonsterAndTakesItOutOfTheHand()
	{
		toMainPhase();
		game.normalSummon(smallMonster, 0);

		assertSame(smallMonster, cardIn(creatureZone(duelist1, 0)), "The monster should be in the monster zone it was summoned to");
		assertFalse(duelist1.getHand().contains(smallMonster), "A summoned monster should leave the hand");
		assertEquals(BattlePosition.ATTACK, smallMonster.getBattlePosition(), "A normal summon puts the monster in attack position");
		assertFalse(smallMonster.isFaceDown(), "A normal summoned monster is face-up");
	}

	@Test
	void onlyOneNormalSummonIsAllowedPerTurn()
	{
		toMainPhase();
		game.normalSummon(smallMonster, 0);

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.normalSummon(otherSmallMonster, 1));

		assertTrue(error.getMessage().contains("one monster"), "The refusal should name the one-summon-per-turn rule: " + error.getMessage());
		assertTrue(duelist1.getHand().contains(otherSmallMonster), "A refused summon should leave the card in hand");
	}

	@Test
	void normalSummonIsAllowedAgainOnTheNextTurn()
	{
		toMainPhase();
		game.normalSummon(smallMonster, 0);
		passTurnBackAround(game);

		toMainPhase();
		game.normalSummon(otherSmallMonster, 1);

		assertSame(otherSmallMonster, cardIn(creatureZone(duelist1, 1)), "The second monster should be summoned on the following turn");
	}

	@Test
	void normalSummonIsRefusedDuringTheDrawPhase()
	{
		assertEquals(YugiohPhaseID.DRAW, game.getCurrentPhaseID(), "A duel starts in the draw phase");

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.normalSummon(smallMonster, 0));

		assertTrue(error.getMessage().contains("main phase"), "The refusal should name the main phase: " + error.getMessage());
	}

	@Test
	void normalSummonIsRefusedDuringTheBattlePhase()
	{
		advanceTo(game, YugiohPhaseID.BATTLE);

		assertThrows(IllegalStateException.class, () -> game.normalSummon(smallMonster, 0), "Monsters cannot be summoned in the battle phase");
	}

	@Test
	void normalSummonIsRefusedWhenTheMonsterIsNotInHand()
	{
		toMainPhase();
		YugiohMonster notOwned = monster("Dark Magician", 4, 1500, 1200);

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.normalSummon(notOwned, 0));

		assertTrue(error.getMessage().contains("not in your hand"), "The refusal should say the card is not in hand: " + error.getMessage());
	}

	@Test
	void normalSummonIsRefusedIntoAnOccupiedZone()
	{
		toMainPhase();
		game.normalSummon(smallMonster, 0);
		passTurnBackAround(game);

		toMainPhase();
		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.normalSummon(otherSmallMonster, 0));

		assertTrue(error.getMessage().contains("occupied"), "The refusal should say the zone is occupied: " + error.getMessage());
		assertSame(smallMonster, cardIn(creatureZone(duelist1, 0)), "The monster already in the zone should stay there");
	}

	@Test
	void tributeRequirementsFollowTheMonsterLevel()
	{
		assertEquals(0, YugiohRules.requiredTributes(1), "Level 1 needs no tribute");
		assertEquals(0, YugiohRules.requiredTributes(4), "Level 4 needs no tribute");
		assertEquals(1, YugiohRules.requiredTributes(5), "Level 5 needs one tribute");
		assertEquals(1, YugiohRules.requiredTributes(6), "Level 6 needs one tribute");
		assertEquals(2, YugiohRules.requiredTributes(7), "Level 7 needs two tributes");
		assertEquals(2, YugiohRules.requiredTributes(12), "Level 12 needs two tributes");

		assertEquals(0, smallMonster.getRequiredTributes(), "A level 4 monster needs no tribute");
		assertEquals(1, levelFiveMonster.getRequiredTributes(), "A level 6 monster needs one tribute");
		assertEquals(2, levelSevenMonster.getRequiredTributes(), "A level 8 monster needs two tributes");

		assertThrows(IllegalArgumentException.class, () -> YugiohRules.requiredTributes(0), "Level 0 is not a legal monster level");
	}

	@Test
	void aHighLevelMonsterCannotBeSummonedWithoutItsTributes()
	{
		toMainPhase();

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.normalSummon(levelFiveMonster, 0));

		assertTrue(error.getMessage().contains("tribute"), "The refusal should name the missing tributes: " + error.getMessage());
		assertTrue(duelist1.getHand().contains(levelFiveMonster), "A refused summon should leave the card in hand");
	}

	@Test
	void oneTributeIsConsumedIntoTheDiscardPile()
	{
		toMainPhase();
		game.normalSummon(smallMonster, 0);
		passTurnBackAround(game);

		toMainPhase();
		game.tributeSummon(levelFiveMonster, 0, List.of(smallMonster));

		assertSame(levelFiveMonster, cardIn(creatureZone(duelist1, 0)), "The tribute summoned monster takes the freed zone");
		assertTrue(duelist1.getDiscardPile().contains(smallMonster), "The tribute should be in the discard pile");
		assertFalse(duelist1.getHand().contains(levelFiveMonster), "The summoned monster should leave the hand");
	}

	@Test
	void twoTributesAreConsumedForALevelSevenOrHigherMonster()
	{
		toMainPhase();
		game.normalSummon(smallMonster, 0);
		passTurnBackAround(game);

		toMainPhase();
		game.normalSummon(secondTribute, 1);
		passTurnBackAround(game);

		toMainPhase();
		game.tributeSummon(levelSevenMonster, 0, List.of(smallMonster, secondTribute));

		assertSame(levelSevenMonster, cardIn(creatureZone(duelist1, 0)), "The tribute summoned monster should be on the field");
		assertFalse(creatureZone(duelist1, 1).isOccupied(), "The second tribute's zone should be empty");
		assertTrue(duelist1.getDiscardPile().contains(smallMonster), "The first tribute should be in the discard pile");
		assertTrue(duelist1.getDiscardPile().contains(secondTribute), "The second tribute should be in the discard pile");
	}

	@Test
	void theTributeCountMustMatchExactly()
	{
		toMainPhase();
		game.normalSummon(smallMonster, 0);
		passTurnBackAround(game);

		toMainPhase();
		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.tributeSummon(levelSevenMonster, 1, List.of(smallMonster)));

		assertTrue(error.getMessage().contains("exactly 2"), "The refusal should state the exact tribute count: " + error.getMessage());
		assertFalse(duelist1.getDiscardPile().contains(smallMonster), "A refused summon should not consume the tribute");
	}

	@Test
	void theSameMonsterCannotBeTributedTwice()
	{
		toMainPhase();
		game.normalSummon(smallMonster, 0);
		passTurnBackAround(game);

		toMainPhase();
		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.tributeSummon(levelSevenMonster, 1, List.of(smallMonster, smallMonster)));

		assertTrue(error.getMessage().contains("twice"), "The refusal should say the monster was offered twice: " + error.getMessage());
	}

	@Test
	void aTributeMustBeOnYourOwnField()
	{
		game.endTurn();

		toMainPhase();
		game.normalSummon(opponentMonster, 0);
		game.endTurn();

		toMainPhase();
		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.tributeSummon(levelFiveMonster, 0, List.of(opponentMonster)));

		assertTrue(error.getMessage().contains("not on your field"), "The refusal should say the tribute is not yours: " + error.getMessage());
		assertSame(opponentMonster, cardIn(creatureZone(duelist2, 0)), "The opponent's monster should stay on their field");
	}

	@Test
	void aSetMonsterIsFaceDownAndUsesUpTheNormalSummon()
	{
		toMainPhase();
		game.setMonster(smallMonster, 0);

		assertSame(smallMonster, cardIn(creatureZone(duelist1, 0)), "The set monster should be in the monster zone");
		assertTrue(smallMonster.isFaceDown(), "A set monster is face-down");
		assertEquals(BattlePosition.SET, smallMonster.getBattlePosition(), "A set monster is in face-down defence position");
		assertFalse(duelist1.getHand().contains(smallMonster), "A set monster should leave the hand");

		assertThrows(IllegalStateException.class, () -> game.normalSummon(otherSmallMonster, 1), "Setting a monster uses up the normal summon for the turn");
	}

	@Test
	void aMonsterThatNeedsTributesCannotBeSet()
	{
		toMainPhase();

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.setMonster(levelFiveMonster, 0));

		assertTrue(error.getMessage().contains("tribute"), "The refusal should name the tribute requirement: " + error.getMessage());
		assertFalse(creatureZone(duelist1, 0).isOccupied(), "A refused set should leave the zone empty");
		assertTrue(duelist1.getHand().contains(levelFiveMonster), "A refused set leaves the card in hand");
	}
}

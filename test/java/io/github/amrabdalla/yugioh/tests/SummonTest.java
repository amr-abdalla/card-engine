package io.github.amrabdalla.yugioh.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.deckOf;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.gameWith;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.toMainPhase;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.amrabdalla.cardengine.card.zone.CreatureZone;
import io.github.amrabdalla.yugioh.card.BattlePosition;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.game.YugiohGame;

public class SummonTest
{
	private YugiohMonster warrior;
	private YugiohMonster scout;
	private YugiohMonster knight;
	private YugiohMonster dragon;
	private YugiohGame game;

	@BeforeEach
	void setup()
	{
		warrior = new YugiohMonster("Stone Warrior", 4, 1800, 1200);
		scout = new YugiohMonster("Swift Scout", 3, 1000, 800);
		knight = new YugiohMonster("Storm Knight", 6, 2400, 2000);
		dragon = new YugiohMonster("Ancient Dragon", 8, 3000, 2500);

		YugiohMonster opponentMonster = new YugiohMonster("Opposing Golem", 4, 1500, 1500);

		game = gameWith(deckOf(warrior, scout, knight, dragon), deckOf(opponentMonster));
		toMainPhase(game);
	}

	private CreatureZone zoneAt(int index)
	{
		return new ArrayList<>(game.getCurrentDuelist().getCreatureZones()).get(index);
	}

	@Test
	void testNormalSummonPlacesMonsterOnField()
	{
		game.normalSummon(warrior, 0, BattlePosition.ATTACK);

		assertTrue(zoneAt(0).getOccupiedCard().filter(card -> card == warrior).isPresent(),
				"Warrior should occupy monster zone 0");
		assertFalse(game.getCurrentDuelist().getHand().contains(warrior),
				"Warrior should no longer be in the hand");
		assertEquals(BattlePosition.ATTACK, warrior.getPosition(), "Warrior should be in attack position");
	}

	@Test
	void testOnlyOneNormalSummonPerTurn()
	{
		game.normalSummon(warrior, 0, BattlePosition.ATTACK);

		assertThrows(IllegalStateException.class,
				() -> game.normalSummon(scout, 1, BattlePosition.ATTACK),
				"A second normal summon in the same turn should be rejected");
	}

	@Test
	void testNormalSummonAllowedAgainNextTurn()
	{
		game.normalSummon(warrior, 0, BattlePosition.ATTACK);
		game.endTurn();
		game.endTurn();
		toMainPhase(game);

		game.normalSummon(scout, 1, BattlePosition.ATTACK);
		assertTrue(zoneAt(1).getOccupiedCard().filter(card -> card == scout).isPresent(),
				"Scout should be summoned on a later turn");
	}

	@Test
	void testSummonRejectedOutsideMainPhase()
	{
		game.nextPhase(); // battle phase

		assertThrows(IllegalStateException.class,
				() -> game.normalSummon(warrior, 0, BattlePosition.ATTACK),
				"Summoning should only be allowed in the main phase");
	}

	@Test
	void testSummonRejectedWhenNotInHand()
	{
		YugiohMonster stranger = new YugiohMonster("Stranger", 4, 1000, 1000);

		assertThrows(IllegalStateException.class,
				() -> game.normalSummon(stranger, 0, BattlePosition.ATTACK),
				"A monster outside the hand cannot be summoned");
	}

	@Test
	void testLevelFiveOrSixRequiresOneTribute()
	{
		assertThrows(IllegalStateException.class,
				() -> game.normalSummon(knight, 0, BattlePosition.ATTACK),
				"A level 6 monster requires a tribute");
	}

	@Test
	void testLevelSevenOrHigherRequiresTwoTributes()
	{
		game.normalSummon(warrior, 0, BattlePosition.ATTACK);
		game.endTurn();
		game.endTurn();
		toMainPhase(game);

		assertThrows(IllegalStateException.class,
				() -> game.tributeSummon(dragon, 1, BattlePosition.ATTACK, List.of(warrior)),
				"A level 8 monster requires two tributes, not one");
	}

	@Test
	void testTributeSummonConsumesTributes()
	{
		game.normalSummon(warrior, 0, BattlePosition.ATTACK);
		game.endTurn();
		game.endTurn();
		toMainPhase(game);

		game.tributeSummon(knight, 0, BattlePosition.ATTACK, List.of(warrior));

		assertTrue(zoneAt(0).getOccupiedCard().filter(card -> card == knight).isPresent(),
				"Knight should occupy the freed zone");
		assertTrue(game.getCurrentDuelist().getDiscardPile().contains(warrior),
				"The tribute should be in the discard pile");
	}

	@Test
	void testTributeMustBeOnOwnField()
	{
		assertThrows(IllegalStateException.class,
				() -> game.tributeSummon(knight, 0, BattlePosition.ATTACK, List.of(scout)),
				"A monster still in the hand cannot be used as tribute");
	}

	@Test
	void testSetMonsterIsFaceDownAndCountsAsNormalSummon()
	{
		game.setMonster(warrior, 0, List.of());

		assertEquals(BattlePosition.SET, warrior.getPosition(), "Set monster should be face-down");
		assertTrue(warrior.isFaceDown());
		assertThrows(IllegalStateException.class,
				() -> game.normalSummon(scout, 1, BattlePosition.ATTACK),
				"Setting a monster should consume the normal summon");
	}

	@Test
	void testSummonIntoOccupiedZoneRejected()
	{
		game.normalSummon(warrior, 0, BattlePosition.ATTACK);
		game.endTurn();
		game.endTurn();
		toMainPhase(game);

		assertThrows(IllegalStateException.class,
				() -> game.normalSummon(scout, 0, BattlePosition.ATTACK),
				"Summoning into an occupied zone should be rejected");
	}
}

package io.github.amrabdalla.yugioh.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.deckOf;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.gameWith;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.toMainPhase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.amrabdalla.yugioh.YugiohRules;
import io.github.amrabdalla.yugioh.card.BattlePosition;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.game.YugiohGame;

public class BattleTest
{
	private YugiohMonster strong; // duelist 2's monster
	private YugiohMonster weak;   // duelist 1's monster
	private YugiohMonster second; // duelist 1's second monster
	private YugiohGame game;

	@BeforeEach
	void setup()
	{
		weak = new YugiohMonster("Small Soldier", 3, 1200, 800);
		second = new YugiohMonster("Backup Soldier", 3, 1100, 900);
		strong = new YugiohMonster("Great Ogre", 4, 1800, 1000);

		game = gameWith(deckOf(weak, second), deckOf(strong));
	}

	/**
	 * Turn 1: duelist 1 summons according to the given position, ends turn.
	 * Turn 2: duelist 2 summons {@code strong} and enters the battle phase.
	 */
	private void setupBattlefield(BattlePosition duelist1Position)
	{
		toMainPhase(game);
		game.normalSummon(weak, 0, duelist1Position);
		game.endTurn();

		toMainPhase(game);
		game.normalSummon(strong, 0, BattlePosition.ATTACK);
		game.nextPhase(); // battle phase
	}

	@Test
	void testNoAttacksOnFirstTurn()
	{
		toMainPhase(game);
		game.normalSummon(weak, 0, BattlePosition.ATTACK);
		game.nextPhase(); // battle phase, still turn 1

		assertThrows(IllegalStateException.class,
				() -> game.declareDirectAttack(weak),
				"No attacks should be allowed on the first turn of the duel");
	}

	@Test
	void testAttackDestroysWeakerAttackPositionMonster()
	{
		setupBattlefield(BattlePosition.ATTACK);

		game.declareAttack(strong, weak);

		assertTrue(weak.isDead(), "The weaker monster should be destroyed");
		assertTrue(game.getOpponentDuelist().getDiscardPile().contains(weak),
				"The destroyed monster should be in its owner's discard pile");
		assertFalse(game.getOpponentDuelist().getCreatureZones().iterator().next().isOccupied(),
				"The destroyed monster's zone should be empty");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS - (1800 - 1200),
				game.getOpponentDuelist().getLifePoints(),
				"The defending duelist should take the ATK difference as battle damage");
		assertFalse(strong.isDead(), "The winning monster should survive");
	}

	@Test
	void testAttackingStrongerMonsterDestroysAttacker()
	{
		setupBattlefield(BattlePosition.ATTACK);
		game.endTurn();
		YugiohTestSupport.toBattlePhase(game); // duelist 1's battle phase

		game.declareAttack(weak, strong);

		assertTrue(weak.isDead(), "The attacker should be destroyed when attacking a stronger monster");
		assertFalse(strong.isDead(), "The stronger defender should survive");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS - (1800 - 1200),
				game.getCurrentDuelist().getLifePoints(),
				"The attacking duelist should take the ATK difference as damage");
	}

	@Test
	void testEqualAttackDestroysBothWithoutDamage()
	{
		YugiohMonster mirrorA = new YugiohMonster("Mirror A", 4, 1500, 1000);
		YugiohMonster mirrorB = new YugiohMonster("Mirror B", 4, 1500, 1000);
		game = gameWith(deckOf(mirrorA), deckOf(mirrorB));

		toMainPhase(game);
		game.normalSummon(mirrorA, 0, BattlePosition.ATTACK);
		game.endTurn();
		toMainPhase(game);
		game.normalSummon(mirrorB, 0, BattlePosition.ATTACK);
		game.nextPhase();

		game.declareAttack(mirrorB, mirrorA);

		assertTrue(mirrorA.isDead(), "Both monsters should be destroyed on equal ATK");
		assertTrue(mirrorB.isDead(), "Both monsters should be destroyed on equal ATK");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, game.getCurrentDuelist().getLifePoints());
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, game.getOpponentDuelist().getLifePoints());
	}

	@Test
	void testAttackOnDefensePositionDealsNoBattleDamage()
	{
		setupBattlefield(BattlePosition.DEFENSE);

		game.declareAttack(strong, weak); // 1800 ATK vs 800 DEF

		assertTrue(weak.isDead(), "The defending monster should be destroyed");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, game.getOpponentDuelist().getLifePoints(),
				"Destroying a defense-position monster should deal no battle damage");
	}

	@Test
	void testAttackOnHigherDefenseDamagesAttackingDuelist()
	{
		YugiohMonster wall = new YugiohMonster("Great Wall", 4, 0, 2500);
		game = gameWith(deckOf(wall), deckOf(strong));

		toMainPhase(game);
		game.normalSummon(wall, 0, BattlePosition.DEFENSE);
		game.endTurn();
		toMainPhase(game);
		game.normalSummon(strong, 0, BattlePosition.ATTACK);
		game.nextPhase();

		game.declareAttack(strong, wall); // 1800 ATK vs 2500 DEF

		assertFalse(wall.isDead(), "A defender with higher DEF should survive");
		assertFalse(strong.isDead(), "The attacker should survive against a defense-position monster");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS - (2500 - 1800),
				game.getCurrentDuelist().getLifePoints(),
				"The attacking duelist should take the DEF difference as damage");
	}

	@Test
	void testAttackFlipsFaceDownMonster()
	{
		toMainPhase(game);
		game.setMonster(weak, 0, java.util.List.of());
		game.endTurn();

		toMainPhase(game);
		game.normalSummon(strong, 0, BattlePosition.ATTACK);
		game.nextPhase();

		game.declareAttack(strong, weak);

		assertFalse(weak.isFaceDown(), "An attacked set monster should be flipped face-up");
	}

	@Test
	void testDirectAttackReducesLifePoints()
	{
		toMainPhase(game);
		game.endTurn(); // duelist 1 plays nothing

		toMainPhase(game);
		game.normalSummon(strong, 0, BattlePosition.ATTACK);
		game.nextPhase();

		game.declareDirectAttack(strong);

		assertEquals(YugiohRules.STARTING_LIFE_POINTS - 1800,
				game.getOpponentDuelist().getLifePoints(),
				"A direct attack should deal the full ATK as damage");
	}

	@Test
	void testDirectAttackBlockedWhileOpponentControlsMonsters()
	{
		setupBattlefield(BattlePosition.ATTACK);

		assertThrows(IllegalStateException.class,
				() -> game.declareDirectAttack(strong),
				"Direct attacks should be blocked while the opponent controls monsters");
	}

	@Test
	void testMonsterMayOnlyAttackOncePerTurn()
	{
		toMainPhase(game);
		game.endTurn();

		toMainPhase(game);
		game.normalSummon(strong, 0, BattlePosition.ATTACK);
		game.nextPhase();

		game.declareDirectAttack(strong);
		assertThrows(IllegalStateException.class,
				() -> game.declareDirectAttack(strong),
				"A monster should only attack once per turn");
	}

	@Test
	void testAttackFlagResetsNextTurn()
	{
		toMainPhase(game);
		game.endTurn();

		toMainPhase(game);
		game.normalSummon(strong, 0, BattlePosition.ATTACK);
		game.nextPhase();
		game.declareDirectAttack(strong);

		game.endTurn(); // to duelist 1
		game.endTurn(); // back to duelist 2
		YugiohTestSupport.toBattlePhase(game);

		game.declareDirectAttack(strong);
		assertEquals(YugiohRules.STARTING_LIFE_POINTS - 2 * 1800,
				game.getOpponentDuelist().getLifePoints(),
				"The monster should be able to attack again on its next turn");
	}

	@Test
	void testDefensePositionMonsterCannotAttack()
	{
		setupBattlefield(BattlePosition.ATTACK);
		game.endTurn();

		toMainPhase(game);
		game.endTurn(); // skip, weak stays in attack... reposition scenario below

		// duelist 2's turn again: summon a defender and try to attack with it
		toMainPhase(game);
		YugiohMonster defender = new YugiohMonster("Crouching Guard", 4, 1400, 1600);
		game.getCurrentDuelist().getHand().add(defender);
		game.normalSummon(defender, 1, BattlePosition.DEFENSE);
		game.nextPhase();

		assertThrows(IllegalStateException.class,
				() -> game.declareAttack(defender, weak),
				"A defense-position monster should not be able to attack");
	}

	@Test
	void testWinByReducingLifePointsToZero()
	{
		// duelist 2 starts with only 1000 LP
		io.github.amrabdalla.cardengine.duelist.core.Duelist duelist1 =
				YugiohTestSupport.duelistWithHand(deckOf(strong));
		io.github.amrabdalla.cardengine.duelist.core.Duelist duelist2 =
				new io.github.amrabdalla.cardengine.duelist.impl.ConcreteDuelist(
						deckOf(weak), new java.util.Random(YugiohTestSupport.RANDOM_SEED),
						YugiohRules.MONSTER_ZONE_COUNT, YugiohRules.SPELL_TRAP_ZONE_COUNT, 1, 1000);
		game = new YugiohGame(duelist1, duelist2);

		toMainPhase(game);
		game.normalSummon(strong, 0, BattlePosition.ATTACK);
		game.endTurn();

		toMainPhase(game);
		game.endTurn(); // duelist 2 plays nothing

		YugiohTestSupport.toBattlePhase(game);
		game.declareDirectAttack(strong); // 1800 >= 1000

		assertEquals(0, duelist2.getLifePoints(), "Life points should not go below zero");
		assertTrue(game.isOver(), "The duel should be over");
		assertEquals(duelist1, game.getWinner().orElseThrow(), "Duelist 1 should win");
		assertThrows(IllegalStateException.class, game::endTurn,
				"No further actions should be allowed after the duel ends");
	}
}

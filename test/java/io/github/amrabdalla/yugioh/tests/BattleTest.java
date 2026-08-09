package io.github.amrabdalla.yugioh.tests;

import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.advanceTo;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.creatureZone;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.duelistHolding;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.monster;
import static io.github.amrabdalla.yugioh.tests.YugiohTestSupport.passTurnBackAround;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.github.amrabdalla.yugioh.YugiohRules;
import io.github.amrabdalla.yugioh.card.BattlePosition;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.duelist.YugiohDuelist;
import io.github.amrabdalla.yugioh.game.YugiohGame;
import io.github.amrabdalla.yugioh.turn.YugiohPhaseID;

public class BattleTest
{
	private YugiohDuelist duelist1;
	private YugiohDuelist duelist2;
	private YugiohGame game;

	private void startDuel(YugiohMonster mine, YugiohMonster theirs)
	{
		duelist1 = mine == null ? duelistHolding("Yugi") : duelistHolding("Yugi", mine);
		duelist2 = theirs == null ? duelistHolding("Kaiba") : duelistHolding("Kaiba", theirs);

		game = new YugiohGame(duelist1, duelist2);
	}

	/**
	 * Turn 1: the first duelist summons. Turn 2: the second duelist summons or sets. Turn 3: the
	 * first duelist reaches the battle phase, where attacks are legal again.
	 */
	private void summonBothThenReachBattlePhase(YugiohMonster mine, YugiohMonster theirs, boolean setTheirs)
	{
		advanceTo(game, YugiohPhaseID.MAIN);
		game.normalSummon(mine, 0);
		game.endTurn();

		advanceTo(game, YugiohPhaseID.MAIN);

		if (setTheirs)
		{
			game.setMonster(theirs, 0);
		}
		else
		{
			game.normalSummon(theirs, 0);
		}

		game.endTurn();

		advanceTo(game, YugiohPhaseID.BATTLE);
	}

	@Test
	void noAttacksAreAllowedOnTheFirstTurnOfTheDuel()
	{
		YugiohMonster mine = monster("Gemini Elf", 4, 1900, 900);
		startDuel(mine, null);

		advanceTo(game, YugiohPhaseID.MAIN);
		game.normalSummon(mine, 0);
		advanceTo(game, YugiohPhaseID.BATTLE);

		assertEquals(YugiohRules.FIRST_TURN_NUMBER, game.getTurnNumber(), "This should still be the first turn");

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.declareDirectAttack(mine));

		assertTrue(error.getMessage().contains("first turn"), "The refusal should name the first-turn rule: " + error.getMessage());
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist2.getLifePoints(), "No damage should have been dealt");
	}

	@Test
	void theStrongerAttackerDestroysTheDefenderAndDamagesItsController()
	{
		YugiohMonster mine = monster("Summoned Skull", 4, 2500, 1200);
		YugiohMonster theirs = monster("Battle Ox", 4, 1700, 1000);
		startDuel(mine, theirs);
		summonBothThenReachBattlePhase(mine, theirs, false);

		game.declareAttack(mine, theirs);

		assertTrue(theirs.isDead(), "The weaker monster should be destroyed");
		assertFalse(creatureZone(duelist2, 0).isOccupied(), "The destroyed monster should be swept off the field");
		assertTrue(duelist2.getDiscardPile().contains(theirs), "The destroyed monster belongs in its owner's discard pile");
		assertFalse(mine.isDead(), "The attacker should survive");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS - 800, duelist2.getLifePoints(), "The defending duelist takes the ATK difference");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist1.getLifePoints(), "The attacking duelist takes no damage");
	}

	@Test
	void losingAnAttackDestroysTheAttackerAndDamagesItsController()
	{
		YugiohMonster mine = monster("Battle Ox", 4, 1700, 1000);
		YugiohMonster theirs = monster("Summoned Skull", 4, 2500, 1200);
		startDuel(mine, theirs);
		summonBothThenReachBattlePhase(mine, theirs, false);

		game.declareAttack(mine, theirs);

		assertTrue(mine.isDead(), "The attacker loses and is destroyed");
		assertFalse(creatureZone(duelist1, 0).isOccupied(), "The destroyed attacker should be swept off the field");
		assertTrue(duelist1.getDiscardPile().contains(mine), "The destroyed attacker belongs in its owner's discard pile");
		assertFalse(theirs.isDead(), "The defender should survive");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS - 800, duelist1.getLifePoints(), "The attacking duelist takes the ATK difference");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist2.getLifePoints(), "The defending duelist takes no damage");
	}

	@Test
	void equalAttackDestroysBothMonstersWithoutDamage()
	{
		YugiohMonster mine = monster("Mirror Knight", 4, 1600, 1000);
		YugiohMonster theirs = monster("Mirror Knight Token", 4, 1600, 1000);
		startDuel(mine, theirs);
		summonBothThenReachBattlePhase(mine, theirs, false);

		game.declareAttack(mine, theirs);

		assertTrue(mine.isDead(), "Equal ATK destroys the attacker");
		assertTrue(theirs.isDead(), "Equal ATK destroys the defender");
		assertFalse(creatureZone(duelist1, 0).isOccupied(), "The attacker's zone should be empty");
		assertFalse(creatureZone(duelist2, 0).isOccupied(), "The defender's zone should be empty");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist1.getLifePoints(), "Neither duelist takes damage");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist2.getLifePoints(), "Neither duelist takes damage");
	}

	@Test
	void beatingADefendingMonsterDestroysItWithoutDamage()
	{
		YugiohMonster mine = monster("Summoned Skull", 4, 2500, 1200);
		YugiohMonster theirs = monster("Giant Soldier of Stone", 3, 1300, 2000);
		startDuel(mine, theirs);
		summonBothThenReachBattlePhase(mine, theirs, true);

		game.declareAttack(mine, theirs);

		assertTrue(theirs.isDead(), "ATK above DEF destroys the defending monster");
		assertTrue(duelist2.getDiscardPile().contains(theirs), "The destroyed monster belongs in its owner's discard pile");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist2.getLifePoints(), "Beating a defending monster deals no damage");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist1.getLifePoints(), "Beating a defending monster deals no damage");
	}

	@Test
	void attackingIntoAHigherDefenceDamagesTheAttackingDuelist()
	{
		YugiohMonster mine = monster("Battle Ox", 4, 1700, 1000);
		YugiohMonster theirs = monster("Mystical Elf", 4, 800, 2000);
		startDuel(mine, theirs);
		summonBothThenReachBattlePhase(mine, theirs, true);

		game.declareAttack(mine, theirs);

		assertFalse(mine.isDead(), "Neither monster is destroyed when DEF is higher than ATK");
		assertFalse(theirs.isDead(), "Neither monster is destroyed when DEF is higher than ATK");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS - 300, duelist1.getLifePoints(), "The attacking duelist takes the difference");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist2.getLifePoints(), "The defending duelist takes no damage");
	}

	@Test
	void attackingASetMonsterFlipsItFaceUp()
	{
		YugiohMonster mine = monster("Battle Ox", 4, 1700, 1000);
		YugiohMonster theirs = monster("Mystical Elf", 4, 800, 2000);
		startDuel(mine, theirs);
		summonBothThenReachBattlePhase(mine, theirs, true);

		assertTrue(theirs.isFaceDown(), "The defender starts face-down");

		game.declareAttack(mine, theirs);

		assertFalse(theirs.isFaceDown(), "Being attacked flips a set monster face-up");
		assertEquals(BattlePosition.DEFENSE, theirs.getBattlePosition(), "A flipped set monster is in face-up defence position");
	}

	@Test
	void aDirectAttackTakesTheFullAttackOffTheOpponent()
	{
		YugiohMonster mine = monster("Gemini Elf", 4, 1900, 900);
		startDuel(mine, null);

		advanceTo(game, YugiohPhaseID.MAIN);
		game.normalSummon(mine, 0);
		passTurnBackAround(game);

		advanceTo(game, YugiohPhaseID.BATTLE);
		game.declareDirectAttack(mine);

		assertEquals(YugiohRules.STARTING_LIFE_POINTS - 1900, duelist2.getLifePoints(), "A direct attack deals the attacker's full ATK");
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist1.getLifePoints(), "The attacking duelist takes no damage");
	}

	@Test
	void aDirectAttackIsBlockedWhileTheOpponentControlsAMonster()
	{
		YugiohMonster mine = monster("Gemini Elf", 4, 1900, 900);
		YugiohMonster theirs = monster("Battle Ox", 4, 1700, 1000);
		startDuel(mine, theirs);
		summonBothThenReachBattlePhase(mine, theirs, false);

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.declareDirectAttack(mine));

		assertTrue(error.getMessage().contains("direct attack"), "The refusal should name the direct attack rule: " + error.getMessage());
		assertEquals(YugiohRules.STARTING_LIFE_POINTS, duelist2.getLifePoints(), "No damage should have been dealt");
	}

	@Test
	void aMonsterCanOnlyAttackOncePerTurnAndTheFlagResetsNextTurn()
	{
		YugiohMonster mine = monster("Gemini Elf", 4, 1900, 900);
		startDuel(mine, null);

		advanceTo(game, YugiohPhaseID.MAIN);
		game.normalSummon(mine, 0);
		passTurnBackAround(game);

		advanceTo(game, YugiohPhaseID.BATTLE);
		game.declareDirectAttack(mine);

		assertTrue(mine.hasAttackedThisTurn(), "The monster should be marked as having attacked");

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.declareDirectAttack(mine));

		assertTrue(error.getMessage().contains("already attacked"), "The refusal should name the one-attack rule: " + error.getMessage());
		assertEquals(YugiohRules.STARTING_LIFE_POINTS - 1900, duelist2.getLifePoints(), "The second attack should not have gone through");

		passTurnBackAround(game);

		assertFalse(mine.hasAttackedThisTurn(), "The attack flag should reset at the start of the next turn");

		advanceTo(game, YugiohPhaseID.BATTLE);
		game.declareDirectAttack(mine);

		assertEquals(YugiohRules.STARTING_LIFE_POINTS - 3800, duelist2.getLifePoints(), "The monster should be able to attack again on the next turn");
	}

	@Test
	void aSetMonsterCannotDeclareAnAttack()
	{
		YugiohMonster mine = monster("Gemini Elf", 4, 1900, 900);
		startDuel(mine, null);

		advanceTo(game, YugiohPhaseID.MAIN);
		game.setMonster(mine, 0);
		passTurnBackAround(game);

		advanceTo(game, YugiohPhaseID.BATTLE);

		assertFalse(mine.canDeclareAttack(), "A face-down monster cannot attack");

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.declareDirectAttack(mine));

		assertTrue(error.getMessage().contains("SET"), "The refusal should name the battle position: " + error.getMessage());
	}

	@Test
	void aDefencePositionMonsterCannotDeclareAnAttack()
	{
		YugiohMonster mine = monster("Battle Ox", 4, 1700, 1000);
		YugiohMonster theirs = monster("Mystical Elf", 4, 800, 2000);
		startDuel(mine, theirs);
		summonBothThenReachBattlePhase(mine, theirs, true);

		// Attacking it flips it into face-up defence position without destroying it.
		game.declareAttack(mine, theirs);

		assertEquals(BattlePosition.DEFENSE, theirs.getBattlePosition(), "The monster should now be in face-up defence position");

		game.endTurn();
		advanceTo(game, YugiohPhaseID.BATTLE);

		assertFalse(theirs.canDeclareAttack(), "A defence position monster cannot attack");
		assertFalse(theirs.canAttack(mine), "The engine's canAttack should agree that a defence position monster cannot attack");

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.declareAttack(theirs, mine));

		assertTrue(error.getMessage().contains("DEFENSE"), "The refusal should name the battle position: " + error.getMessage());
	}

	@Test
	void aMonsterCannotAttackACardTheOpponentDoesNotControl()
	{
		YugiohMonster mine = monster("Gemini Elf", 4, 1900, 900);
		YugiohMonster theirs = monster("Battle Ox", 4, 1700, 1000);
		startDuel(mine, theirs);
		summonBothThenReachBattlePhase(mine, theirs, false);

		YugiohMonster stranger = monster("Not On The Field", 4, 1000, 1000);

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.declareAttack(mine, stranger));

		assertTrue(error.getMessage().contains("opponent's field"), "The refusal should say the target is not on the opponent's field: " + error.getMessage());
	}

	@Test
	void aMonstersDefenceIsWhatGenericEngineCodeSeesAsHitPoints()
	{
		YugiohMonster wall = monster("Mystical Elf", 4, 800, 2000);

		assertEquals(2000, wall.getDefense(), "DEF is the monster's defence value");
		assertEquals(2000, wall.getMaxHP(), "Generic engine code reads a monster's DEF through the HP fields");
		assertEquals(2000, wall.getCurrentHP(), "Generic engine code reads a monster's DEF through the HP fields");
		assertEquals(800, wall.getAttack(), "ATK is the monster's attack value");

		wall.modifyDefenseValue(500);

		assertEquals(2500, wall.getDefense(), "Modifying DEF should change the defence value");
		assertEquals(2500, wall.getMaxHP(), "Modifying DEF should stay visible through the HP fields");
		assertEquals(2500, wall.getCurrentHP(), "Modifying DEF should stay visible through the HP fields");

		wall.modifyAttackValue(200);

		assertEquals(1000, wall.getAttack(), "Modifying ATK should change the attack value");
		assertEquals(2500, wall.getDefense(), "Modifying ATK should leave DEF alone");
	}

	@Test
	void attacksAreRefusedOutsideTheBattlePhase()
	{
		YugiohMonster mine = monster("Gemini Elf", 4, 1900, 900);
		startDuel(mine, null);

		advanceTo(game, YugiohPhaseID.MAIN);
		game.normalSummon(mine, 0);
		passTurnBackAround(game);

		advanceTo(game, YugiohPhaseID.MAIN);

		IllegalStateException error = assertThrows(IllegalStateException.class, () -> game.declareDirectAttack(mine));

		assertTrue(error.getMessage().contains("battle phase"), "The refusal should name the battle phase: " + error.getMessage());
	}
}

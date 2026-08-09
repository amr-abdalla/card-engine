package io.github.amrabdalla.yugioh.game;

import io.github.amrabdalla.yugioh.card.BattlePosition;
import io.github.amrabdalla.yugioh.card.YugiohMonster;

/**
 * Works out the outcome of one battle. The resolver holds no state and applies nothing: it neither
 * destroys monsters nor moves life points, it only reports what the outcome is so that the
 * orchestrator can apply it.
 *
 * The single exception is the flip of a face-down defender, which is part of resolving the battle
 * itself - the ATK/DEF comparison below is made against the revealed position.
 */
public final class BattleResolver
{
	private BattleResolver()
	{
		// prevent instantiation
	}

	public static BattleResult resolveAttack(YugiohMonster attacker, YugiohMonster defender)
	{
		if (attacker == null || defender == null)
		{
			throw new IllegalArgumentException("A battle needs both an attacker and a defender");
		}

		defender.flip();

		if (defender.getBattlePosition() == BattlePosition.ATTACK)
		{
			return resolveAgainstAttackPosition(attacker, defender);
		}

		return resolveAgainstDefensePosition(attacker, defender);
	}

	public static BattleResult resolveDirectAttack(YugiohMonster attacker)
	{
		if (attacker == null)
		{
			throw new IllegalArgumentException("A direct attack needs an attacker");
		}

		return new BattleResult(false, false, 0, attacker.getAttack());
	}

	private static BattleResult resolveAgainstAttackPosition(YugiohMonster attacker, YugiohMonster defender)
	{
		int attackerAttack = attacker.getAttack();
		int defenderAttack = defender.getAttack();

		if (attackerAttack > defenderAttack)
		{
			return new BattleResult(false, true, 0, attackerAttack - defenderAttack);
		}

		if (attackerAttack < defenderAttack)
		{
			return new BattleResult(true, false, defenderAttack - attackerAttack, 0);
		}

		return new BattleResult(true, true, 0, 0);
	}

	private static BattleResult resolveAgainstDefensePosition(YugiohMonster attacker, YugiohMonster defender)
	{
		int attackerAttack = attacker.getAttack();
		int defenderDefense = defender.getDefense();

		// Beating a defending monster destroys it but deals no damage: piercing is out of scope.
		if (attackerAttack > defenderDefense)
		{
			return new BattleResult(false, true, 0, 0);
		}

		if (attackerAttack < defenderDefense)
		{
			return new BattleResult(false, false, defenderDefense - attackerAttack, 0);
		}

		return BattleResult.NOTHING_HAPPENS;
	}
}

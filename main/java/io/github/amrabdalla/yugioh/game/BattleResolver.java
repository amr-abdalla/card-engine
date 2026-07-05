package io.github.amrabdalla.yugioh.game;

import io.github.amrabdalla.yugioh.card.BattlePosition;
import io.github.amrabdalla.yugioh.card.YugiohMonster;

/**
 * Resolves a battle between two monsters according to the basic
 * Yu-Gi-Oh! battle rules:
 *
 * Against an attack-position monster, ATK values are compared. The lower
 * monster is destroyed and its owner takes the difference as battle damage.
 * A tie destroys both monsters with no damage.
 *
 * Against a defense-position monster, the attacker's ATK is compared to the
 * defender's DEF. If ATK is higher the defender is destroyed with no battle
 * damage (no piercing). If DEF is higher the attacker survives and its owner
 * takes the difference. A tie has no consequence.
 *
 * A face-down (set) monster is flipped face-up into defense position before
 * the battle is resolved.
 */
public final class BattleResolver
{
	/**
	 * The result of a battle: which monsters were destroyed and how much
	 * battle damage each duelist takes.
	 */
	public record BattleOutcome(boolean attackerDestroyed,
	                            boolean defenderDestroyed,
	                            int damageToAttackingDuelist,
	                            int damageToDefendingDuelist)
	{
	}

	private BattleResolver()
	{
		// prevent instantiation
	}

	public static BattleOutcome resolve(YugiohMonster attacker, YugiohMonster defender)
	{
		defender.flip();

		if (defender.getPosition() == BattlePosition.ATTACK)
		{
			return resolveAgainstAttackPosition(attacker, defender);
		}
		else
		{
			return resolveAgainstDefensePosition(attacker, defender);
		}
	}

	public static BattleOutcome resolveDirectAttack(YugiohMonster attacker)
	{
		return new BattleOutcome(false, false, 0, attacker.getAttack());
	}

	private static BattleOutcome resolveAgainstAttackPosition(YugiohMonster attacker, YugiohMonster defender)
	{
		int difference = attacker.getAttack() - defender.getAttack();

		if (difference > 0)
		{
			defender.markDead();
			return new BattleOutcome(false, true, 0, difference);
		}

		if (difference < 0)
		{
			attacker.markDead();
			return new BattleOutcome(true, false, -difference, 0);
		}

		attacker.markDead();
		defender.markDead();
		return new BattleOutcome(true, true, 0, 0);
	}

	private static BattleOutcome resolveAgainstDefensePosition(YugiohMonster attacker, YugiohMonster defender)
	{
		int difference = attacker.getAttack() - defender.getDefense();

		if (difference > 0)
		{
			defender.markDead();
			return new BattleOutcome(false, true, 0, 0);
		}

		if (difference < 0)
		{
			return new BattleOutcome(false, false, -difference, 0);
		}

		return new BattleOutcome(false, false, 0, 0);
	}
}

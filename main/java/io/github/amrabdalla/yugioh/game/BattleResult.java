package io.github.amrabdalla.yugioh.game;

/**
 * What a single battle worked out to, before any of it is applied to the board. The damage figures
 * are named after the roles in the battle: the attacking duelist is the one who declared it.
 */
public record BattleResult(boolean attackerDestroyed, boolean defenderDestroyed, int damageToAttackingDuelist, int damageToDefendingDuelist)
{
	public static final BattleResult NOTHING_HAPPENS = new BattleResult(false, false, 0, 0);
}

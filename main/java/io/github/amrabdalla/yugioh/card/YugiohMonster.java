package io.github.amrabdalla.yugioh.card;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.creature.impl.AbstractCreature;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.yugioh.YugiohRules;

/**
 * A Yu-Gi-Oh! monster card.
 *
 * The engine models a creature's durability as HP, while a Yu-Gi-Oh! monster's durability is its
 * DEF, so DEF is stored in the inherited HP fields: generic engine code that reads getMaxHP() or
 * getCurrentHP() still sees a meaningful value. Battles between monsters are not resolved by
 * subtracting HP though - BattleResolver compares ATK against ATK or DEF and destroys outright.
 */
public class YugiohMonster extends AbstractCreature
{
	private final String name;
	private final int level;
	private BattlePosition battlePosition;
	private boolean attackedThisTurn;

	public YugiohMonster(String name, int level, int attack, int defense, Optional<Effect> effect)
	{
		if (name == null || name.isBlank())
		{
			throw new IllegalArgumentException("A monster must have a name");
		}

		if (level < YugiohRules.MINIMUM_MONSTER_LEVEL)
		{
			throw new IllegalArgumentException("Monster level must be at least " + YugiohRules.MINIMUM_MONSTER_LEVEL + " but was " + level);
		}

		if (attack < 0)
		{
			throw new IllegalArgumentException("Monster ATK cannot be negative: " + attack);
		}

		if (defense < 0)
		{
			throw new IllegalArgumentException("Monster DEF cannot be negative: " + defense);
		}

		if (effect == null)
		{
			throw new IllegalArgumentException("Effect must not be null, use Optional.empty() instead");
		}

		this.name = name;
		this.level = level;
		this.effect = effect;
		this.battlePosition = BattlePosition.ATTACK;
		this.attackedThisTurn = false;

		this.baseAttack = attack;
		this.baseMaxHP = defense;
		this.currentHP = defense;
	}

	public String getName()
	{
		return name;
	}

	public int getLevel()
	{
		return level;
	}

	public int getDefense()
	{
		return getMaxHP();
	}

	public void modifyDefenseValue(int value)
	{
		additionalMaxHP += value;
		currentHP += value;
	}

	public int getRequiredTributes()
	{
		return YugiohRules.requiredTributes(level);
	}

	public BattlePosition getBattlePosition()
	{
		return battlePosition;
	}

	/**
	 * Set by the orchestrator when the monster is summoned or set. Changing position afterwards is
	 * a separate rule that is out of scope for this rule set.
	 */
	public void setBattlePosition(BattlePosition battlePosition)
	{
		if (battlePosition == null)
		{
			throw new IllegalArgumentException("Battle position must not be null");
		}

		this.battlePosition = battlePosition;
	}

	public void flip()
	{
		if (battlePosition == BattlePosition.SET)
		{
			battlePosition = BattlePosition.DEFENSE;
		}
	}

	public boolean isFaceDown()
	{
		return battlePosition == BattlePosition.SET;
	}

	public boolean hasAttackedThisTurn()
	{
		return attackedThisTurn;
	}

	public void markAttacked()
	{
		attackedThisTurn = true;
	}

	public void resetAttackFlag()
	{
		attackedThisTurn = false;
	}

	/**
	 * The attacker-side half of attack legality: face-up attack position, still on the field and
	 * not yet used this turn. Whether the chosen target is legal is a board-level question that
	 * the orchestrator answers.
	 */
	public boolean canDeclareAttack()
	{
		return !isDead && !attackedThisTurn && battlePosition == BattlePosition.ATTACK;
	}

	@Override
	public boolean canAttack(Creature creature)
	{
		return canDeclareAttack();
	}
}

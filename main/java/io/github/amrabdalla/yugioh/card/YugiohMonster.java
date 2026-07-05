package io.github.amrabdalla.yugioh.card;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.creature.impl.AbstractCreature;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.yugioh.YugiohRules;

/**
 * A Yu-Gi-Oh! monster card. Builds on the engine's {@link AbstractCreature},
 * adding a name, a level, a defense stat and a battle position.
 *
 * Battles between monsters are resolved by comparing ATK/DEF values (see
 * {@code BattleResolver}), not by the engine's HP-based combat. The engine's
 * HP fields are mapped to the monster's defense so the card remains usable
 * by generic engine code.
 */
public class YugiohMonster extends AbstractCreature
{
	private final String name;
	private final int level;
	private final int baseDefense;
	private int additionalDefense;
	private BattlePosition position = BattlePosition.ATTACK;
	private boolean attackedThisTurn;

	public YugiohMonster(String name, int level, int attack, int defense)
	{
		this(name, level, attack, defense, Optional.empty());
	}

	public YugiohMonster(String name, int level, int attack, int defense, Optional<Effect> effect)
	{
		if (level < 1)
		{
			throw new IllegalArgumentException("Monster level must be at least 1");
		}

		this.name = name;
		this.level = level;
		this.baseAttack = attack;
		this.baseDefense = defense;
		this.baseMaxHP = defense;
		this.currentHP = defense;
		this.effect = effect == null ? Optional.empty() : effect;
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
		return baseDefense + additionalDefense;
	}

	public void modifyDefenseValue(int value)
	{
		additionalDefense += value;
	}

	public int getRequiredTributes()
	{
		return YugiohRules.requiredTributes(level);
	}

	public BattlePosition getPosition()
	{
		return position;
	}

	public void setPosition(BattlePosition position)
	{
		this.position = position;
	}

	public boolean isFaceDown()
	{
		return position == BattlePosition.SET;
	}

	/**
	 * Flips a face-down (set) monster face-up into defense position,
	 * e.g. when it is attacked.
	 */
	public void flip()
	{
		if (isFaceDown())
		{
			position = BattlePosition.DEFENSE;
		}
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

	@Override
	public boolean canAttack(Creature target)
	{
		return position == BattlePosition.ATTACK && !attackedThisTurn && !isDead();
	}
}

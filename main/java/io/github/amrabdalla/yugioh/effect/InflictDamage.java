package io.github.amrabdalla.yugioh.effect;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.game.GameContext;

/**
 * Inflicts a fixed amount of damage to the opponent of the current duelist.
 * Typical effect for "burn" spell cards, so it is intended to be activated
 * during the owner's own turn.
 */
public class InflictDamage implements Effect
{
	private final int amount;

	public InflictDamage(int amount)
	{
		if (amount < 0)
		{
			throw new IllegalArgumentException("Damage amount must not be negative");
		}

		this.amount = amount;
	}

	@Override
	public boolean canApply(Card owner, Optional<Card> target, GameContext context)
	{
		return true;
	}

	@Override
	public void apply(Card owner, Optional<Card> target, GameContext context)
	{
		context.getOpponentDuelist().takeDamage(amount);
	}
}

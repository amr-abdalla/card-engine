package io.github.amrabdalla.yugioh.effect;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.game.GameContext;

/**
 * Burn damage: takes a fixed amount off the opponent's life points. No target is needed.
 *
 * The opponent is read from the context, which resolves it relative to the duelist whose turn it
 * is - so this is meant for cards the turn player activates.
 */
public class InflictDamage implements Effect
{
	private final int amount;

	public InflictDamage(int amount)
	{
		if (amount < 0)
		{
			throw new IllegalArgumentException("Burn damage cannot be negative: " + amount);
		}

		this.amount = amount;
	}

	public int getAmount()
	{
		return amount;
	}

	@Override
	public boolean canApply(Card owner, Optional<Card> target, GameContext context)
	{
		return context != null && context.getOpponentDuelist() != null;
	}

	@Override
	public void apply(Card owner, Optional<Card> target, GameContext context)
	{
		context.getOpponentDuelist().takeDamage(amount);
	}
}

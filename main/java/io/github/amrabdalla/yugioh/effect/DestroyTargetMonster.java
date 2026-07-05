package io.github.amrabdalla.yugioh.effect;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.game.GameContext;

/**
 * Unconditionally destroys the targeted monster.
 * Typical effect for removal traps and spells.
 */
public class DestroyTargetMonster implements Effect
{
	@Override
	public boolean canApply(Card owner, Optional<Card> target, GameContext context)
	{
		if (target.isEmpty())
		{
			return false;
		}

		if (!(target.get() instanceof Creature targetCreature))
		{
			return false;
		}

		return !targetCreature.isDead();
	}

	@Override
	public void apply(Card owner, Optional<Card> target, GameContext context)
	{
		Creature targetCreature = (Creature) target.get();
		targetCreature.markDead();
	}
}

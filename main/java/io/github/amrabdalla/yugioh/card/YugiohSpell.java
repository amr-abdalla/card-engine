package io.github.amrabdalla.yugioh.card;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.card.helper.HelperCard;
import io.github.amrabdalla.cardengine.card.zone.CardZone;
import io.github.amrabdalla.cardengine.card.zone.HelperZone;
import io.github.amrabdalla.cardengine.game.GameContext;

/**
 * A normal spell card: it resolves once and is then sent to its owner's discard pile.
 *
 * Activation goes through activateEffect(GameContext, Optional), the context-aware pair added to
 * HelperCard, because a Yu-Gi-Oh! spell routinely has to reach the duelists themselves (burn
 * damage) and not just its target. The engine's original context-free target(Card) cannot express
 * that, so it is left unsupported rather than given a half-working implementation that would
 * silently skip the parts of an effect it cannot reach.
 */
public class YugiohSpell implements HelperCard
{
	private final String name;
	private final Effect effect;

	public YugiohSpell(String name, Effect effect)
	{
		if (name == null || name.isBlank())
		{
			throw new IllegalArgumentException("A spell must have a name");
		}

		if (effect == null)
		{
			throw new IllegalArgumentException("A spell must have an effect");
		}

		this.name = name;
		this.effect = effect;
	}

	public String getName()
	{
		return name;
	}

	public Effect getEffect()
	{
		return effect;
	}

	@Override
	public void play(CardZone cardZone)
	{
		cardZone.accept(this);
	}

	@Override
	public Boolean canPlay(CardZone cardZone)
	{
		return cardZone instanceof HelperZone;
	}

	@Override
	public void target(Card card)
	{
		throw new UnsupportedOperationException("Spell \"" + name + "\" must be activated with a game context, see activateEffect(GameContext, Optional)");
	}

	@Override
	public boolean canTarget(Card card)
	{
		throw new UnsupportedOperationException("Spell \"" + name + "\" must be checked with a game context, see canActivateEffect(GameContext, Optional)");
	}

	@Override
	public void activateEffect(GameContext context, Optional<Card> target)
	{
		effect.apply(this, target, context);
	}

	@Override
	public boolean canActivateEffect(GameContext context, Optional<Card> target)
	{
		return effect.canApply(this, target, context);
	}
}

package io.github.amrabdalla.yugioh.card;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.card.helper.HelperCard;
import io.github.amrabdalla.cardengine.card.zone.CardZone;
import io.github.amrabdalla.cardengine.card.zone.HelperZone;

/**
 * A Yu-Gi-Oh! spell card. Can be activated from the hand or after being set
 * in a spell/trap zone, during a phase that allows helper card activation.
 * Activation is orchestrated by {@code YugiohGame}, which supplies the
 * {@code GameContext} required by the effect system.
 */
public class YugiohSpell implements HelperCard
{
	private final String name;
	private final Effect effect;

	public YugiohSpell(String name, Effect effect)
	{
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
		throw new UnsupportedOperationException(
				"Targets are supplied when the spell is activated through YugiohGame");
	}

	@Override
	public boolean canTarget(Card card)
	{
		return effect.canApply(this, Optional.ofNullable(card), null);
	}
}

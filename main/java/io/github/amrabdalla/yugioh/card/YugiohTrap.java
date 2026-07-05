package io.github.amrabdalla.yugioh.card;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.card.helper.HelperCard;
import io.github.amrabdalla.cardengine.card.zone.CardZone;
import io.github.amrabdalla.cardengine.card.zone.HelperZone;

/**
 * A Yu-Gi-Oh! trap card. Must be set in a spell/trap zone first and cannot
 * be activated during the turn in which it was set. Activation is
 * orchestrated by {@code YugiohGame}.
 */
public class YugiohTrap implements HelperCard
{
	private static final int NOT_SET = -1;

	private final String name;
	private final Effect effect;
	private int setOnTurn = NOT_SET;

	public YugiohTrap(String name, Effect effect)
	{
		if (effect == null)
		{
			throw new IllegalArgumentException("A trap must have an effect");
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

	public void markSet(int turnNumber)
	{
		setOnTurn = turnNumber;
	}

	public boolean isSet()
	{
		return setOnTurn != NOT_SET;
	}

	/**
	 * A trap can only be activated after the turn in which it was set.
	 */
	public boolean canActivate(int currentTurnNumber)
	{
		return isSet() && currentTurnNumber > setOnTurn;
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
				"Targets are supplied when the trap is activated through YugiohGame");
	}

	@Override
	public boolean canTarget(Card card)
	{
		return effect.canApply(this, Optional.ofNullable(card), null);
	}
}

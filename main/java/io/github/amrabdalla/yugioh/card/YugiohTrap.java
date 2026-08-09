package io.github.amrabdalla.yugioh.card;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.card.helper.HelperCard;
import io.github.amrabdalla.cardengine.card.zone.CardZone;
import io.github.amrabdalla.cardengine.card.zone.HelperZone;
import io.github.amrabdalla.cardengine.game.GameContext;

/**
 * A normal trap card. A trap has to be set face-down first and only becomes live on a later turn,
 * so it remembers the turn it was set on.
 *
 * Like YugiohSpell it resolves through activateEffect(GameContext, Optional); see that class for
 * why the context-free target(Card) is left unsupported.
 */
public class YugiohTrap implements HelperCard
{
	private static final int NOT_SET = -1;

	private final String name;
	private final Effect effect;
	private int setTurn = NOT_SET;

	public YugiohTrap(String name, Effect effect)
	{
		if (name == null || name.isBlank())
		{
			throw new IllegalArgumentException("A trap must have a name");
		}

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

	public void markSet(int turn)
	{
		if (turn < 1)
		{
			throw new IllegalArgumentException("A trap cannot be set on turn " + turn);
		}

		this.setTurn = turn;
	}

	public boolean isSet()
	{
		return setTurn != NOT_SET;
	}

	public int getSetTurn()
	{
		return setTurn;
	}

	public boolean canActivate(int currentTurn)
	{
		return isSet() && currentTurn > setTurn;
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
		throw new UnsupportedOperationException("Trap \"" + name + "\" must be activated with a game context, see activateEffect(GameContext, Optional)");
	}

	@Override
	public boolean canTarget(Card card)
	{
		throw new UnsupportedOperationException("Trap \"" + name + "\" must be checked with a game context, see canActivateEffect(GameContext, Optional)");
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

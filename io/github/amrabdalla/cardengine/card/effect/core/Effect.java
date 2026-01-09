package io.github.amrabdalla.cardengine.card.effect.core;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.game.GameContext;

public interface Effect 
{
	boolean canApply(Card owner, Optional<Card> target, GameContext context);
	void apply(Card owner, Optional<Card> target, GameContext context);
}

package io.github.amrabdalla.cardengine.card.helper;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.game.GameContext;

public interface HelperCard extends Card
{
	public void target(Card card);
	public boolean canTarget(Card card);

	/*
	 * A helper card's effect frequently needs to reach beyond its target: to the duelists, the
	 * current phase or the turn number. target(Card) cannot express that, so a helper card is
	 * resolved through the same context-aware pair a Creature already uses. Keeping both shapes
	 * lets an implementation that only ever touches its target keep using target(Card).
	 */
	public void activateEffect(GameContext context, Optional<Card> target);
	public boolean canActivateEffect(GameContext context, Optional<Card> target);
}

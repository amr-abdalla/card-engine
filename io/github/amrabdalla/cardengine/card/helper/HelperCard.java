package io.github.amrabdalla.cardengine.card.helper;

import io.github.amrabdalla.cardengine.card.core.Card;

public interface HelperCard extends Card
{
	public void target(Card card);
	public boolean canTarget(Card card);
}

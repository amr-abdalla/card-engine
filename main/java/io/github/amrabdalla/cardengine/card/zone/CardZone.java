package io.github.amrabdalla.cardengine.card.zone;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;

public interface CardZone 
{
	public boolean isOccupied();
	public Optional<Card> getOccupiedCard();
	public boolean canAccept(Card card);
	public void accept(Card card);
	public void remove(Card card);
}

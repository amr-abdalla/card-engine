package io.github.amrabdalla.cardengine.duelist.core;

import java.util.Collection;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.zone.CreatureZone;
import io.github.amrabdalla.cardengine.card.zone.HelperZone;

public interface Duelist
{
	public Collection<Card> getHand();
	public void shuffleDeck();
	public int draw(int count);
	public Collection<Card> getDiscardPile();
	public Collection<CreatureZone> getCreatureZones();
	public Collection<HelperZone> getHelperZones();
	public int getLifePoints();
	public void takeDamage(int amount);
	public void gainLifePoints(int amount);
	public void discard(Card card);
	public int getDeckSize();
}

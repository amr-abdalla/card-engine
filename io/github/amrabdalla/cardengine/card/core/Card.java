package io.github.amrabdalla.cardengine.card.core;

import io.github.amrabdalla.cardengine.card.zone.CardZone;

public interface Card 
{
	public void play(CardZone cardZone);
	public Boolean canPlay(CardZone cardZone);
}

package io.github.amrabdalla.cardengine.zone.impl;


import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.zone.AbstractCardZone;
import io.github.amrabdalla.cardengine.card.zone.CreatureZone;

public class ConcreteCreatureZone extends AbstractCardZone implements CreatureZone
{
	public boolean canAccept(Card card) 
	{
        return !isOccupied() && card instanceof Creature;
	}		
}

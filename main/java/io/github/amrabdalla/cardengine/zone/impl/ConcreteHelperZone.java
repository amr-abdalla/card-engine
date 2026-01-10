package io.github.amrabdalla.cardengine.zone.impl;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.helper.HelperCard;
import io.github.amrabdalla.cardengine.card.zone.AbstractCardZone;
import io.github.amrabdalla.cardengine.card.zone.HelperZone;

public class ConcreteHelperZone extends AbstractCardZone implements HelperZone
{
	public boolean canAccept(Card card) 
	{
        return !isOccupied() && card instanceof HelperCard;
	}
}

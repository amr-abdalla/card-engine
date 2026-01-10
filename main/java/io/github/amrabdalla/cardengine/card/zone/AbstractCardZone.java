package io.github.amrabdalla.cardengine.card.zone;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;

public abstract class AbstractCardZone 
{
    protected Card occupiedCard;

    public boolean isOccupied()
    {
        return occupiedCard != null;
    }

    public Optional<Card> getOccupiedCard()
    {
        return Optional.ofNullable(occupiedCard);
    }

    public void accept(Card card)
    {
        this.occupiedCard = card;
    }

    public void remove(Card card)
    {
        if (occupiedCard == card) 
        {
            occupiedCard = null;
        }
    }
}

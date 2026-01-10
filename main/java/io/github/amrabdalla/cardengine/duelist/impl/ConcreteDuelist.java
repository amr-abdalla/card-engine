package io.github.amrabdalla.cardengine.duelist.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.zone.CreatureZone;
import io.github.amrabdalla.cardengine.card.zone.HelperZone;
import io.github.amrabdalla.cardengine.duelist.core.Duelist;
import io.github.amrabdalla.cardengine.zone.impl.ConcreteCreatureZone;
import io.github.amrabdalla.cardengine.zone.impl.ConcreteHelperZone;

public class ConcreteDuelist implements Duelist
{
	protected List<Card> hand;
	protected Deque<Card> deck;
	protected Deque<Card> discardPile;
	protected CreatureZone[] creatureZones;
	protected HelperZone[] helperZones;
	protected int lifePoints;
	private final Random random;

	public ConcreteDuelist(Collection<Card> deck, Random random, int creatureZoneCount, int helperZoneCount, int startingHand, int startingLifePoints)
	{
		this.random = random;
		
		this.deck = new ArrayDeque<>(deck);	
		shuffleDeck();
		
		creatureZones = new CreatureZone[creatureZoneCount];
		Arrays.setAll(creatureZones, i -> new ConcreteCreatureZone());

		helperZones = new HelperZone[helperZoneCount];
		Arrays.setAll(helperZones, i -> new ConcreteHelperZone());
		
		hand = new ArrayList<>();	
		draw(startingHand);
		
		discardPile = new ArrayDeque<>();
		
		lifePoints = startingLifePoints;
	}
	
	public Collection<Card> getHand() 
	{
		return hand;
	}

	public void shuffleDeck() 
	{
	    List<Card> temp = new ArrayList<>(deck);
	    Collections.shuffle(temp, random);

	    deck.clear();
	    deck.addAll(temp);

	}

	public int draw(int count) 
	{
	    int drawn = 0;

		for(int i = 0; i < count; i++)
		{
			if (deck.isEmpty())
			{
				break;
			}
			
			hand.add(deck.pop());
			drawn++;
		}

		return drawn;
	}

	public Collection<Card> getDiscardPile() 
	{
		return discardPile;
	}
	
	public Collection<CreatureZone> getCreatureZones() 
	{
		return Arrays.asList(creatureZones);
	}

	public Collection<HelperZone> getHelperZones() 
	{
		return Arrays.asList(helperZones);
	}

	public int getLifePoints() 
	{
		return lifePoints;
	}

}

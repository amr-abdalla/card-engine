package io.github.amrabdalla.cardengine.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.creature.impl.ConcreteCreature;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.card.effect.core.EffectFactory;
import io.github.amrabdalla.cardengine.card.effect.core.EffectID;
import io.github.amrabdalla.cardengine.duelist.core.Duelist;
import io.github.amrabdalla.cardengine.duelist.impl.ConcreteDuelist;
import io.github.amrabdalla.cardengine.game.Game;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.Random;

public class CreatureTest 
{	
	private final int _creatureZoneCount = 3;
	private final int _helperZoneCount = 3;
	private final int _startingHand = 2;
	private final int _lifePoints = 1000;
	private final long _randomSeed = 0;

	private final int _weakAttack = 250;
	private final int _weakHP = 1000;
	private final int _midAttack = 500;
	private final int _midHP = 2000;
	private final int _strongAttack = 1000;
	private final int _strongHP = 3000;

	private Game game;
	
	Collection<Card> getDummyDeck()
	{
		Effect effect = EffectFactory.getInstance().getEffect(EffectID.DestroyTargetCreature, _strongHP);
		Creature weakCreature = new ConcreteCreature(_weakAttack, _weakHP, Optional.of(effect));
		Creature midCreature = new ConcreteCreature(_midAttack, _midHP, null);
		Creature strongCreature = new ConcreteCreature(_strongAttack, _strongHP, null);
		
		return Arrays.asList(weakCreature, midCreature, strongCreature);
	}
	
    @BeforeEach
    void setup() 
    {
    	Random random = new Random(_randomSeed);
    	Duelist duelist1 = new ConcreteDuelist(getDummyDeck(), random, _creatureZoneCount, _helperZoneCount, _startingHand, _lifePoints);
    	Duelist duelist2 = new ConcreteDuelist(getDummyDeck(), random, _creatureZoneCount, _helperZoneCount, _startingHand, _lifePoints);
    	game = new Game(duelist1, duelist2);
    }

    @Test
    void testAttack() 
    {
    	Collection<Card> hand = game.getCurrentDuelist().getHand();

    	Iterator<Card> handIterator = hand.iterator();
    	    	
    	if (!handIterator.hasNext())
    	{
    		// throw assertion error here
    		return;
    	}
    	
    	Card first = hand.iterator().next();
    	
    	if (! (first instanceof Creature))
    	{
			// throw assertion error here
    		return;
    	}	
    	
    	Creature strongCreature = (Creature) first;
		assertTrue(strongCreature.getAttack() == _strongAttack);
		strongCreature.play(game.getCurrentDuelist().getCreatureZones().iterator().next());	
		game.endTurn();
		
    	hand = game.getCurrentDuelist().getHand();
    	handIterator = hand.iterator();
    	
    	if (!handIterator.hasNext())
    	{
    		// throw assertion error here
    		return;
    	}

    	first = hand.iterator().next();
    	
    	if (! (first instanceof Creature))
    	{
			// throw assertion error here
    		return;
    	}	
    	
    	Creature weakCreature = (Creature) first;
		assertTrue(weakCreature.getAttack() == _weakAttack);
		assertTrue(weakCreature.canAttack(strongCreature));	
		weakCreature.attack(strongCreature);
		assertTrue(strongCreature.getCurrentHP() == strongCreature.getMaxHP() - weakCreature.getAttack());
    }
    
    @Test
    void testEffects() 
    {
    	Collection<Card> hand = game.getCurrentDuelist().getHand();

    	Iterator<Card> handIterator = hand.iterator();
    	    	
    	if (!handIterator.hasNext())
    	{
    		// throw assertion error here
    		return;
    	}
    	
    	Card first = hand.iterator().next();
    	
    	if (! (first instanceof Creature))
    	{
			// throw assertion error here
    		return;
    	}	
    	
    	Creature strongCreature = (Creature) first;
		assertTrue(strongCreature.getAttack() == _strongAttack);
		strongCreature.play(game.getCurrentDuelist().getCreatureZones().iterator().next());	
		game.endTurn();
		
    	hand = game.getCurrentDuelist().getHand();
    	handIterator = hand.iterator();
    	
    	if (!handIterator.hasNext())
    	{
    		// throw assertion error here
    		return;
    	}

    	first = hand.iterator().next();
    	
    	if (! (first instanceof Creature))
    	{
			// throw assertion error here
    		return;
    	}	
    	
    	Creature weakCreature = (Creature) first;
		assertTrue(weakCreature.getAttack() == _weakAttack);
		assertTrue(weakCreature.canActivateEffect(game.getContext(), Optional.of(strongCreature)));
		weakCreature.activateEffect(game.getContext(), Optional.of(strongCreature));
		assertTrue(strongCreature.isDead());
    }

}

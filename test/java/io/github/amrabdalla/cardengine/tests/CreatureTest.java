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

public class CreatureTest {

    private final int creatureZoneCount = 3;
    private final int helperZoneCount = 3;
    private final int startingHand = 2;
    private final int lifePoints = 1000;
    private final long randomSeed = 0;

    private final int weakAttack = 250;
    private final int weakHP = 1000;
    private final int midAttack = 500;
    private final int midHP = 2000;
    private final int strongAttack = 1000;
    private final int strongHP = 3000;

    private Game game;

    private Collection<Card> getDummyDeck() {
        Effect effect = EffectFactory.getInstance().getEffect(EffectID.DestroyTargetCreature, strongHP);
        Creature weakCreature = new ConcreteCreature(weakAttack, weakHP, Optional.of(effect));
        Creature midCreature = new ConcreteCreature(midAttack, midHP, null);
        Creature strongCreature = new ConcreteCreature(strongAttack, strongHP, null);

        return Arrays.asList(weakCreature, midCreature, strongCreature);
    }

    @BeforeEach
    void setup() {
        Random random = new Random(randomSeed);
        Duelist duelist1 = new ConcreteDuelist(getDummyDeck(), random, creatureZoneCount, helperZoneCount, startingHand, lifePoints);
        Duelist duelist2 = new ConcreteDuelist(getDummyDeck(), random, creatureZoneCount, helperZoneCount, startingHand, lifePoints);
        game = new Game(duelist1, duelist2);
    }

    private Creature getFirstCreatureInHand(Duelist duelist) 
    {
        Collection<Card> hand = duelist.getHand();
        Iterator<Card> handIterator = hand.iterator();

        assertTrue(handIterator.hasNext(), "Expected a card in hand, but hand was empty");

        Card first = handIterator.next();
        assertTrue(first instanceof Creature, "Expected a creature, but card is a " + first.getClass());

        return (Creature) first;
    }

    @Test
    void testAttack() 
    {
        Creature strongCreature = getFirstCreatureInHand(game.getCurrentDuelist());
        assertEquals(strongAttack, strongCreature.getAttack(), "Strong creature attack mismatch");
        strongCreature.play(game.getCurrentDuelist().getCreatureZones().iterator().next());
        game.endTurn();

        Creature weakCreature = getFirstCreatureInHand(game.getCurrentDuelist());
        assertEquals(weakAttack, weakCreature.getAttack(), "Weak creature attack mismatch");
        assertTrue(weakCreature.canAttack(strongCreature), "Weak creature should be able to attack strong creature");

        weakCreature.attack(strongCreature);
        assertEquals(strongCreature.getMaxHP() - weakCreature.getAttack(),
                     strongCreature.getCurrentHP(),
                     "Strong creature HP after attack mismatch");
    }

    @Test
    void testEffects() 
    {
        Creature strongCreature = getFirstCreatureInHand(game.getCurrentDuelist());
        assertEquals(strongAttack, strongCreature.getAttack(), "Strong creature attack mismatch");
        strongCreature.play(game.getCurrentDuelist().getCreatureZones().iterator().next());
        game.endTurn();

        Creature weakCreature = getFirstCreatureInHand(game.getCurrentDuelist());
        assertEquals(weakAttack, weakCreature.getAttack(), "Weak creature attack mismatch");
        assertTrue(weakCreature.canActivateEffect(game.getContext(), Optional.of(strongCreature)),
                   "Weak creature should be able to activate effect on strong creature");

        weakCreature.activateEffect(game.getContext(), Optional.of(strongCreature));
        assertTrue(strongCreature.isDead(), "Strong creature should be dead after effect activation");
    }
}

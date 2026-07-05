package io.github.amrabdalla.yugioh.duelist;

import java.util.Collection;
import java.util.Random;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.duelist.impl.ConcreteDuelist;
import io.github.amrabdalla.yugioh.YugiohRules;

/**
 * A duelist configured with the standard Yu-Gi-Oh! parameters:
 * 8000 life points, 5 monster zones, 5 spell/trap zones and a
 * starting hand of 5 cards.
 */
public class YugiohDuelist extends ConcreteDuelist
{
	public YugiohDuelist(Collection<Card> deck, Random random)
	{
		this(deck, random, YugiohRules.STARTING_HAND_SIZE);
	}

	public YugiohDuelist(Collection<Card> deck, Random random, int startingHand)
	{
		super(deck,
		      random,
		      YugiohRules.MONSTER_ZONE_COUNT,
		      YugiohRules.SPELL_TRAP_ZONE_COUNT,
		      startingHand,
		      YugiohRules.STARTING_LIFE_POINTS);
	}
}

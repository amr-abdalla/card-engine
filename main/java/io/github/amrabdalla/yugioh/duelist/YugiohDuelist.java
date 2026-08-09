package io.github.amrabdalla.yugioh.duelist;

import java.util.Collection;
import java.util.Random;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.duelist.impl.ConcreteDuelist;
import io.github.amrabdalla.yugioh.YugiohRules;

/**
 * A duelist configured with the Yu-Gi-Oh! defaults: 8000 life points, five monster zones and five
 * spell/trap zones.
 */
public class YugiohDuelist extends ConcreteDuelist
{
	private final String name;

	public YugiohDuelist(String name, Collection<Card> deck, Random random)
	{
		this(name, deck, random, YugiohRules.OPENING_HAND_SIZE);
	}

	/**
	 * The opening hand size is configurable so a duel can be set up with an exact, known hand
	 * instead of whatever the shuffle produced.
	 */
	public YugiohDuelist(String name, Collection<Card> deck, Random random, int startingHand)
	{
		super(deck, random, YugiohRules.MONSTER_ZONE_COUNT, YugiohRules.SPELL_TRAP_ZONE_COUNT, startingHand, YugiohRules.STARTING_LIFE_POINTS);

		if (name == null || name.isBlank())
		{
			throw new IllegalArgumentException("A duelist must have a name");
		}

		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	@Override
	public String toString()
	{
		return name;
	}
}

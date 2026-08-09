package io.github.amrabdalla.yugioh.tests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.card.zone.CardZone;
import io.github.amrabdalla.cardengine.card.zone.CreatureZone;
import io.github.amrabdalla.cardengine.card.zone.HelperZone;
import io.github.amrabdalla.yugioh.card.YugiohMonster;
import io.github.amrabdalla.yugioh.card.YugiohSpell;
import io.github.amrabdalla.yugioh.card.YugiohTrap;
import io.github.amrabdalla.yugioh.duelist.YugiohDuelist;
import io.github.amrabdalla.yugioh.game.YugiohGame;
import io.github.amrabdalla.yugioh.turn.YugiohPhaseID;

/**
 * Shared set-up for the Yu-Gi-Oh! tests. Every duelist built here is deterministic: either the
 * whole deck is dealt into the opening hand, so the shuffle cannot move a card the test needs, or
 * the deck holds interchangeable cards the test only ever counts.
 */
public final class YugiohTestSupport
{
	public static final long RANDOM_SEED = 20240607L;

	private YugiohTestSupport()
	{
		// prevent instantiation
	}

	public static YugiohMonster monster(String name, int level, int attack, int defense)
	{
		return new YugiohMonster(name, level, attack, defense, Optional.empty());
	}

	public static YugiohSpell spell(String name, Effect effect)
	{
		return new YugiohSpell(name, effect);
	}

	public static YugiohTrap trap(String name, Effect effect)
	{
		return new YugiohTrap(name, effect);
	}

	/**
	 * A duelist holding exactly the given cards. The whole deck is dealt into the opening hand, so
	 * the deck is left empty - such a duelist must not be asked to draw.
	 */
	public static YugiohDuelist duelistHolding(String name, Card... cards)
	{
		List<Card> deck = Arrays.asList(cards);

		return new YugiohDuelist(name, deck, new Random(RANDOM_SEED), deck.size());
	}

	/**
	 * A duelist with a deck of interchangeable vanilla monsters, for the tests that only care how
	 * many cards move between deck and hand.
	 */
	public static YugiohDuelist duelistWithDeck(String name, int deckSize, int startingHand)
	{
		List<Card> deck = new ArrayList<>();

		for (int i = 0; i < deckSize; i++)
		{
			deck.add(monster(name + " filler " + i, 4, 1000, 1000));
		}

		return new YugiohDuelist(name, deck, new Random(RANDOM_SEED), startingHand);
	}

	public static void advanceTo(YugiohGame game, YugiohPhaseID phase)
	{
		while (game.getCurrentPhaseID() != phase)
		{
			game.nextPhase();
		}
	}

	/**
	 * Ends the current turn and the opponent's, so the turn comes back round to the duelist who
	 * was active when this was called.
	 */
	public static void passTurnBackAround(YugiohGame game)
	{
		game.endTurn();
		game.endTurn();
	}

	public static CreatureZone creatureZone(YugiohDuelist duelist, int index)
	{
		return new ArrayList<>(duelist.getCreatureZones()).get(index);
	}

	public static HelperZone helperZone(YugiohDuelist duelist, int index)
	{
		return new ArrayList<>(duelist.getHelperZones()).get(index);
	}

	public static Card cardIn(CardZone zone)
	{
		return zone.getOccupiedCard().orElse(null);
	}
}

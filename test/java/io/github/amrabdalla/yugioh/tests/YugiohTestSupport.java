package io.github.amrabdalla.yugioh.tests;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.duelist.core.Duelist;
import io.github.amrabdalla.yugioh.duelist.YugiohDuelist;
import io.github.amrabdalla.yugioh.game.YugiohGame;

/**
 * Shared helpers for the Yu-Gi-Oh! test suites. Duelists are created with
 * their entire deck drawn into the hand so tests can play specific card
 * instances deterministically, regardless of shuffling.
 */
final class YugiohTestSupport
{
	static final long RANDOM_SEED = 0;

	private YugiohTestSupport()
	{
	}

	/** A duelist holding every card of the given deck in hand. */
	static Duelist duelistWithHand(Collection<Card> cards)
	{
		return new YugiohDuelist(cards, new Random(RANDOM_SEED), cards.size());
	}

	static YugiohGame gameWith(Collection<Card> handOfDuelist1, Collection<Card> handOfDuelist2)
	{
		return new YugiohGame(duelistWithHand(handOfDuelist1), duelistWithHand(handOfDuelist2));
	}

	/** Advances from the draw phase to the main phase (without drawing). */
	static void toMainPhase(YugiohGame game)
	{
		game.nextPhase();
	}

	/** Advances from the draw phase to the battle phase (without drawing). */
	static void toBattlePhase(YugiohGame game)
	{
		game.nextPhase();
		game.nextPhase();
	}

	static List<Card> deckOf(Card... cards)
	{
		return new ArrayList<>(List.of(cards));
	}
}

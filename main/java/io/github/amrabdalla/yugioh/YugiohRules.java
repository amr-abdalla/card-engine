package io.github.amrabdalla.yugioh;

/**
 * The numeric constants of the basic Yu-Gi-Oh! rule set, kept in one place so that neither the
 * engine nor the rest of the Yu-Gi-Oh! layer has to hard-code them.
 */
public final class YugiohRules
{
	public static final int STARTING_LIFE_POINTS = 8000;
	public static final int MONSTER_ZONE_COUNT = 5;
	public static final int SPELL_TRAP_ZONE_COUNT = 5;
	public static final int OPENING_HAND_SIZE = 5;
	public static final int CARDS_DRAWN_PER_TURN = 1;

	public static final int MINIMUM_MONSTER_LEVEL = 1;
	public static final int HIGHEST_LEVEL_WITHOUT_TRIBUTE = 4;
	public static final int HIGHEST_LEVEL_WITH_ONE_TRIBUTE = 6;

	public static final int FIRST_TURN_NUMBER = 1;

	private YugiohRules()
	{
		// prevent instantiation
	}

	public static int requiredTributes(int level)
	{
		if (level < MINIMUM_MONSTER_LEVEL)
		{
			throw new IllegalArgumentException("Monster level must be at least " + MINIMUM_MONSTER_LEVEL + " but was " + level);
		}

		if (level <= HIGHEST_LEVEL_WITHOUT_TRIBUTE)
		{
			return 0;
		}

		if (level <= HIGHEST_LEVEL_WITH_ONE_TRIBUTE)
		{
			return 1;
		}

		return 2;
	}
}

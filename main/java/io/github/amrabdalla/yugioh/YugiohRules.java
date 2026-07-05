package io.github.amrabdalla.yugioh;

/**
 * Constants and static rule helpers for the basic Yu-Gi-Oh! rule set.
 */
public final class YugiohRules
{
	public static final int STARTING_LIFE_POINTS = 8000;
	public static final int MONSTER_ZONE_COUNT = 5;
	public static final int SPELL_TRAP_ZONE_COUNT = 5;
	public static final int STARTING_HAND_SIZE = 5;
	public static final int MAX_TRIBUTE_FREE_LEVEL = 4;
	public static final int MAX_SINGLE_TRIBUTE_LEVEL = 6;

	private YugiohRules()
	{
		// prevent instantiation
	}

	/**
	 * Number of tributes required to normal summon a monster of the given level.
	 */
	public static int requiredTributes(int level)
	{
		if (level <= MAX_TRIBUTE_FREE_LEVEL)
		{
			return 0;
		}

		if (level <= MAX_SINGLE_TRIBUTE_LEVEL)
		{
			return 1;
		}

		return 2;
	}
}

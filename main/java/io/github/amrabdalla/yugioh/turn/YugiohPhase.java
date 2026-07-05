package io.github.amrabdalla.yugioh.turn;

import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;

/**
 * A parameterized turn phase for the basic Yu-Gi-Oh! turn:
 * Draw phase (draw 1), Main phase (summons + spell/trap plays) and
 * Battle phase (attacks). Traps may be activated in the Main and
 * Battle phases.
 */
public class YugiohPhase extends TurnPhase
{
	private final YugiohPhaseID id;
	private final boolean allowsAttacks;
	private final boolean allowsSummons;
	private final boolean allowsHelperCardsActivation;
	private final boolean allowsTrapsActivation;
	private final int drawCount;

	private YugiohPhase(YugiohPhaseID id,
	                    boolean allowsAttacks,
	                    boolean allowsSummons,
	                    boolean allowsHelperCardsActivation,
	                    boolean allowsTrapsActivation,
	                    int drawCount)
	{
		this.id = id;
		this.allowsAttacks = allowsAttacks;
		this.allowsSummons = allowsSummons;
		this.allowsHelperCardsActivation = allowsHelperCardsActivation;
		this.allowsTrapsActivation = allowsTrapsActivation;
		this.drawCount = drawCount;
	}

	public static YugiohPhase drawPhase()
	{
		return new YugiohPhase(YugiohPhaseID.DRAW, false, false, false, false, 1);
	}

	public static YugiohPhase mainPhase()
	{
		return new YugiohPhase(YugiohPhaseID.MAIN, false, true, true, true, 0);
	}

	public static YugiohPhase battlePhase()
	{
		return new YugiohPhase(YugiohPhaseID.BATTLE, true, false, false, true, 0);
	}

	public YugiohPhaseID getPhaseID()
	{
		return id;
	}

	@Override
	public int getID()
	{
		return id.ordinal();
	}

	@Override
	public boolean allowsAttacks()
	{
		return allowsAttacks;
	}

	@Override
	public boolean allowsTrapsActivation()
	{
		return allowsTrapsActivation;
	}

	@Override
	public boolean allowsSummons()
	{
		return allowsSummons;
	}

	@Override
	public boolean allowsHelperCardsActivation()
	{
		return allowsHelperCardsActivation;
	}

	@Override
	public int drawCount()
	{
		return drawCount;
	}
}

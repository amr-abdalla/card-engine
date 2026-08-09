package io.github.amrabdalla.yugioh.turn;

import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;
import io.github.amrabdalla.yugioh.YugiohRules;

/**
 * The three phases of a basic Yu-Gi-Oh! turn. They differ only in which actions they permit, so
 * one parameterised class with a factory per phase says it more clearly than three subclasses.
 */
public class YugiohPhase extends TurnPhase
{
	private final YugiohPhaseID phaseID;
	private final boolean allowsAttacks;
	private final boolean allowsTrapsActivation;
	private final boolean allowsSummons;
	private final boolean allowsHelperCardsActivation;
	private final int drawCount;

	private YugiohPhase(YugiohPhaseID phaseID, boolean allowsAttacks, boolean allowsTrapsActivation, boolean allowsSummons, boolean allowsHelperCardsActivation, int drawCount)
	{
		this.phaseID = phaseID;
		this.allowsAttacks = allowsAttacks;
		this.allowsTrapsActivation = allowsTrapsActivation;
		this.allowsSummons = allowsSummons;
		this.allowsHelperCardsActivation = allowsHelperCardsActivation;
		this.drawCount = drawCount;
	}

	public static YugiohPhase drawPhase()
	{
		return new YugiohPhase(YugiohPhaseID.DRAW, false, false, false, false, YugiohRules.CARDS_DRAWN_PER_TURN);
	}

	public static YugiohPhase mainPhase()
	{
		return new YugiohPhase(YugiohPhaseID.MAIN, false, true, true, true, 0);
	}

	public static YugiohPhase battlePhase()
	{
		return new YugiohPhase(YugiohPhaseID.BATTLE, true, true, false, false, 0);
	}

	public YugiohPhaseID getPhaseID()
	{
		return phaseID;
	}

	@Override
	public int getID()
	{
		return phaseID.ordinal();
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

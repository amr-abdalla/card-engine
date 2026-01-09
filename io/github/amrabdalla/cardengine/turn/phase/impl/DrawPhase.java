package io.github.amrabdalla.cardengine.turn.phase.impl;

import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;

public class DrawPhase extends TurnPhase
{
	@Override
	public int getID() 
	{
		return PhaseID.Draw.ordinal();
	}

	@Override
	public boolean allowsAttacks() {
		return false;
	}

	@Override
	public boolean allowsTrapsActivation() {
		return false;
	}

	@Override
	public int drawCount() {
		return 1;
	}

	@Override
	public boolean allowsSummons() {
		return false;
	}

	@Override
	public boolean allowsHelperCardsActivation() {
		return false;
	}

}

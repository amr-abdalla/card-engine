package io.github.amrabdalla.cardengine.turn.phase.impl;

import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;

public class MainPhase extends TurnPhase
{
	@Override
	public int getID() {
		return PhaseID.Main.ordinal();
	}

	@Override
	public boolean allowsAttacks() {
		return true;
	}

	@Override
	public boolean allowsTrapsActivation() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean allowsSummons() {
		return true;
	}

	@Override
	public boolean allowsHelperCardsActivation() {
		return true;
	}

	@Override
	public int drawCount() {
		return 0;
	}

}

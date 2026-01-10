package io.github.amrabdalla.cardengine.turn.impl;

import java.util.List;

import io.github.amrabdalla.cardengine.turn.core.Turn;
import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;

public class SampleTurn extends Turn
{
	private final List<TurnPhase> phases;
	
	public SampleTurn(List<TurnPhase> phases)
	{
		 if (phases == null || phases.isEmpty())
		 {
			 throw new IllegalArgumentException("Turn must have at least one phase");
		 }
	     
		 this.phases = phases;
	}
	
	@Override
	protected List<TurnPhase> getPhases() 
	{
		return phases;
	}
}

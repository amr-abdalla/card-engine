package io.github.amrabdalla.cardengine.turn.core;

import java.util.List;

import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;

public abstract class Turn 
{
	private int currentPhaseIndex;
		
	protected abstract List<TurnPhase> getPhases();
	
	public TurnPhase getCurrentPhase() 
	{
		return getPhases().get(currentPhaseIndex);
	}
	
	public void moveToNextPhase()
	{
		if (currentPhaseIndex + 1 < getPhases().size())
		{
			currentPhaseIndex++;
		}
		else
		{
			endTurn();
		}
	}
	
	public void endTurn() 
	{
		currentPhaseIndex = 0;
	}	
	
}

package io.github.amrabdalla.cardengine.game;

import java.util.List;

import io.github.amrabdalla.cardengine.duelist.core.Duelist;
import io.github.amrabdalla.cardengine.turn.core.Turn;
import io.github.amrabdalla.cardengine.turn.factory.TurnFactory;
import io.github.amrabdalla.cardengine.turn.impl.SampleTurn;
import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;

public class Game 
{
	private Duelist duelist1;
	private Duelist duelist2;
	private Turn currentTurn;
	private Duelist currentDuelist;
	private GameContext gameContext;
	
	public Game(Duelist duelist1, Duelist duelist2)
	{
		this.duelist1 = duelist1;
		this.duelist2 = duelist2;
		
		List<TurnPhase> turnPhases = TurnFactory.getInstance().getSampleTurnPhases();
		currentTurn = new SampleTurn(turnPhases);
		currentDuelist = getNextDuelist();
		gameContext = new GameContext(this);
	}
	
	private Duelist getNextDuelist()
	{
		if (currentDuelist == null)
		{
			return getStartingDuelist();
		}
		else 
		{
			return getOppositeDuelist();
		}
	}

	private Duelist getStartingDuelist() 
	{
		return duelist1;
	}

	private Duelist getOppositeDuelist() 
	{
		if (currentDuelist == duelist1)
		{
			return duelist2;
		}
		else 
		{
			return duelist1;
		}
	}
	
	public Duelist getCurrentDuelist()
	{
		return currentDuelist;
	}
	
	public TurnPhase getCurrenTurnPhase()
	{
		return currentTurn.getCurrentPhase();
	}
}

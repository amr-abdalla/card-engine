package io.github.amrabdalla.cardengine.game;

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
	private int turnNumber;

	public Game(Duelist duelist1, Duelist duelist2)
	{
		this(duelist1, duelist2, new SampleTurn(TurnFactory.getInstance().getSampleTurnPhases()));
	}

	public Game(Duelist duelist1, Duelist duelist2, Turn turn)
	{
		if (turn == null)
		{
			throw new IllegalArgumentException("A game must be given a turn structure");
		}

		this.duelist1 = duelist1;
		this.duelist2 = duelist2;

		currentTurn = turn;
		currentDuelist = getNextDuelist();
		gameContext = new GameContext(this);
		turnNumber = 1;
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
	
	public Duelist getOpponentDuelist()
	{
		return getOppositeDuelist();
	}

	public TurnPhase getCurrenTurnPhase()
	{
		return currentTurn.getCurrentPhase();
	}

	public int getTurnNumber()
	{
		return turnNumber;
	}

	public void moveToNextPhase()
	{
		if (currentTurn.isLastPhase())
		{
			endTurn();
		}
		else
		{
			currentTurn.moveToNextPhase();
		}
	}

	public void endTurn()
	{
		currentTurn.endTurn();
		currentDuelist = getNextDuelist();
		turnNumber++;
	}
	
	public GameContext getContext()
	{
		return gameContext;
	}
}

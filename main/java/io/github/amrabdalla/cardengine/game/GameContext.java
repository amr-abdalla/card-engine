package io.github.amrabdalla.cardengine.game;

import io.github.amrabdalla.cardengine.duelist.core.Duelist;
import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;

public class GameContext 
{
	private final Game game;

	public GameContext(Game game) 
	{
		this.game = game;
	}

    public TurnPhase getCurrenTurnPhase() 
    {
    	return game.getCurrenTurnPhase();
    }

    public Duelist getCurrentDuelist()
    {
    	return game.getCurrentDuelist();
    }
}

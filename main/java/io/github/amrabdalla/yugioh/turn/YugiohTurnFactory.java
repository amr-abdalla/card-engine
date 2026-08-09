package io.github.amrabdalla.yugioh.turn;

import java.util.Arrays;
import java.util.List;

import io.github.amrabdalla.cardengine.turn.core.Turn;
import io.github.amrabdalla.cardengine.turn.impl.SampleTurn;
import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;

public class YugiohTurnFactory
{
	private static final YugiohTurnFactory instance = new YugiohTurnFactory();

	private YugiohTurnFactory()
	{
		// prevent external instantiation
	}

	public static YugiohTurnFactory getInstance()
	{
		return instance;
	}

	public List<TurnPhase> getYugiohTurnPhases()
	{
		return Arrays.asList(YugiohPhase.drawPhase(), YugiohPhase.mainPhase(), YugiohPhase.battlePhase());
	}

	/*
	 * SampleTurn is simply a turn backed by a list of phases, which is exactly what a Yu-Gi-Oh!
	 * turn is, so it is reused here instead of adding an identical Turn subclass to the engine.
	 */
	public Turn createTurn()
	{
		return new SampleTurn(getYugiohTurnPhases());
	}
}

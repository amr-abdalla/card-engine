package io.github.amrabdalla.cardengine.turn.factory;

import java.util.Arrays;
import java.util.List;

import io.github.amrabdalla.cardengine.turn.phase.TurnPhase;
import io.github.amrabdalla.cardengine.turn.phase.impl.DrawPhase;
import io.github.amrabdalla.cardengine.turn.phase.impl.MainPhase;

public class TurnFactory 
{
    private static final TurnFactory instace = new TurnFactory();

    private TurnFactory()
    {
        // prevent external instantiation
    }

    public static TurnFactory getInstance()
    {
        return instace;
    }

    public List<TurnPhase> getSampleTurnPhases()
    {
        return Arrays.asList(new DrawPhase(), new MainPhase());
    }
}

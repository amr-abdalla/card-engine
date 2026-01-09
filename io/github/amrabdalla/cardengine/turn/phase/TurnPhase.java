package io.github.amrabdalla.cardengine.turn.phase;

public abstract class TurnPhase 
{
	public abstract int getID();
	public abstract boolean allowsAttacks();
	public abstract boolean allowsTrapsActivation();
	public abstract boolean allowsSummons();
	public abstract boolean allowsHelperCardsActivation();

	public abstract int drawCount();

	@Override
	public boolean equals(Object obj) 
	{
		if (this == obj) return true;
		if (obj == null || getClass() != obj.getClass()) return false;
		
		TurnPhase other = (TurnPhase) obj;
		return getID() == other.getID();
	}
}

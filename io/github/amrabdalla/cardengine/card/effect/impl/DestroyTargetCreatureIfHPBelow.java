package io.github.amrabdalla.cardengine.card.effect.impl;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.game.GameContext;

public class DestroyTargetCreatureIfHPBelow implements Effect
{
	private int threshold;
	
	public DestroyTargetCreatureIfHPBelow(int threshold)
	{
		this.threshold = threshold;
	}
	
	@Override
	public boolean canApply(Card owner, Optional<Card> target, GameContext context) 
	{	
		if (target.isEmpty())
		{
			return false;
		}
			
		if (!(target.get() instanceof Creature targetCreature))
		{
			return false;
		}
				
		return targetCreature.getCurrentHP() <= threshold;				
	}

	@Override
	public void apply(Card owner, Optional<Card> target, GameContext context) 
	{
		Creature targetCreature = (Creature) target.get();		
		targetCreature.markDead();
	}

}

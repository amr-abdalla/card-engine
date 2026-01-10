package io.github.amrabdalla.cardengine.card.effect.impl;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.game.GameContext;

public class ModifyTargetAttack implements Effect
{
	private int amount;
	
	public ModifyTargetAttack(int amount)
	{
		this.amount = amount;
	}
	
	@Override
	public boolean canApply(Card owner, Optional<Card> target, GameContext context) 
	{
		if (target.isEmpty())
		{
			return false;
		}
			
		return (target.get() instanceof Creature);
	}

	@Override
	public void apply(Card owner, Optional<Card> target, GameContext context) 
	{
		Creature targetCreature = (Creature) target.get();		
		targetCreature.modifyAttackValue(amount);
	}
	
}

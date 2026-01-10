package io.github.amrabdalla.cardengine.card.creature.impl;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;

public class ConcreteCreature extends AbstractCreature implements Creature
{	
	public ConcreteCreature(int baseAttack, int baseMaxHP, Optional<Effect> effect) 
	{
		this.baseAttack = baseAttack;
		this.baseMaxHP = baseMaxHP;
		this.currentHP = baseMaxHP;
		
		if (effect == null)
		{
		    System.out.println("WARNING: effect is null. Consider using Optional.empty() instead.");
		}	
		
		this.effect = effect;
	}
}

package io.github.amrabdalla.cardengine.card.creature.impl;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;

public class SimpleCreature extends AbstractCreature implements Creature
{	
	public SimpleCreature(int baseAttack, int baseMaxHP, Optional<Effect> effect) 
	{
		this.baseAttack = baseAttack;
		this.baseMaxHP = baseMaxHP;
		this.currentHP = baseMaxHP;
		this.effect = effect;
	}
}

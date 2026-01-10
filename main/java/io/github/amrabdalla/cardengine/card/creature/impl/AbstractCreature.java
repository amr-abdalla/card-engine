package io.github.amrabdalla.cardengine.card.creature.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.card.creature.Creature;
import io.github.amrabdalla.cardengine.card.effect.core.Effect;
import io.github.amrabdalla.cardengine.card.zone.CardZone;
import io.github.amrabdalla.cardengine.card.zone.CreatureZone;
import io.github.amrabdalla.cardengine.game.GameContext;

public abstract class AbstractCreature implements Creature
{
	protected int baseAttack;
	protected int additionalAttack;
	protected int currentHP;
	protected int baseMaxHP;
	protected int additionalMaxHP;
	protected Optional<Effect> effect;
	protected List<Runnable> deathEvents = new ArrayList<>();
	protected boolean isDead = false;
	
	public void attack(Creature creature)
	{
		creature.takeDamageFrom(this);
	}
	
	public boolean canAttack(Creature creature)
	{
		return true;
	}
		
	public void activateEffect(GameContext context, Optional<Card> target)
	{	
		effect.get().apply(this, target, context);
	}
	
	public boolean canActivateEffect(GameContext context, Optional<Card> target)
	{
		return effect.isPresent() && effect.get().canApply(this, target, context);
	}	
	
	public int getMaxHP()
	{
		return baseMaxHP + additionalMaxHP;
	}
	
	public int getCurrentHP()
	{
		return currentHP;
	}
	
	public int takeDamageFrom(Card other)
	{
		if (other instanceof Creature creature)
		{
			currentHP -= creature.getAttack();
		}
		
		if (currentHP <= 0)
		{
			markDead();
		}
		
		return currentHP;
	}
	
	public void markDead() 
	{
		isDead = true;
	}
	
	public void triggerDeathEvents() 
	{
		for (Runnable deathEvent : deathEvents)
		{
			deathEvent.run();
		}
	}
	
	public void addDeathEvent(Runnable deathEvent) 
	{
		deathEvents.add(deathEvent);
	}
	
	public void removeDeathEvent(Runnable deathEvent) 
	{
		deathEvents.remove(deathEvent);
	}
	
	public boolean isDead() 
	{
		return isDead;
	}
	
	public int getAttack()
	{
		return baseAttack + additionalAttack;
	}

	public void play(CardZone cardZone) 
	{
		cardZone.accept(this);
	}

	public Boolean canPlay(CardZone cardZone) 
	{
		return cardZone instanceof CreatureZone;
	}	
	
	public void modifyAttackValue(int value) 
	{
		additionalAttack += value;
	}
}

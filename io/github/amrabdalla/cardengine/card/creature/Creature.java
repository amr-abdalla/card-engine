package io.github.amrabdalla.cardengine.card.creature;

import java.util.Optional;

import io.github.amrabdalla.cardengine.card.core.Card;
import io.github.amrabdalla.cardengine.game.GameContext;

public interface Creature extends Card
{
	public void attack(Creature creature);
	public boolean canAttack(Creature creature);
	public void activateEffect(GameContext context, Optional<Card> target);
	public boolean canActivateEffect(GameContext context, Optional<Card> target);
	public int getMaxHP();
	public int getCurrentHP();
	public int takeDamageFrom(Card other);
	public int getAttack();
	public void triggerDeathEvents();
	public boolean isDead();
	public void markDead();
	public void addDeathEvent(Runnable deathEvent);
	public void removeDeathEvent(Runnable deathEvent);
	public void modifyAttackValue(int value);
}

package io.github.amrabdalla.cardengine.card.effect.core;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.IntFunction;

import io.github.amrabdalla.cardengine.card.effect.impl.DestroyTargetCreatureIfHPBelow;
import io.github.amrabdalla.cardengine.card.effect.impl.ModifyTargetAttack;

public class EffectFactory 
{
    private static final EffectFactory instace = new EffectFactory();
    
    private final Map<EffectID, IntFunction<Effect>> effectsMap = new EnumMap<>(EffectID.class);

    private EffectFactory()
    {
    	registerEffects();
    }

    public static EffectFactory getInstance()
    {
        return instace;
    }
    
    public Effect getEffect(EffectID id, int attachedValue)
    {
        IntFunction<Effect> factory = effectsMap.get(id);

        if (factory == null) 
        {
        	throw new IllegalArgumentException("No effect registered for ID: " + id);
        }

        return factory.apply(attachedValue);
    }
    
    private void registerEffects() 
    {
    	effectsMap.put(EffectID.ModifyTargetAttack, ModifyTargetAttack::new);
        effectsMap.put(EffectID.DestroyTargetCreature, DestroyTargetCreatureIfHPBelow::new);
    }

}

package com.alfa.chaotictower.magic.dark;

import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellContext;

/**
 * Dark Magic: Weight
 * Makes the opponent's next blocks spawn at 2x scale (larger and heavier).
 * Duration: 10 seconds — blocks spawned during this period are oversized.
 * The actual scaling is handled by BlockFactory/PlayingScreen by checking
 * player.isWeighted().
 */
public class WeightSpell extends Spell {

    public WeightSpell() {
        super("Weight", "Opponent gets giant blocks", false, 10f);
    }

    @Override
    public void apply(SpellContext context) {
        context.target.setWeighted(true);
    }

    @Override
    public void remove(SpellContext context) {
        context.target.setWeighted(false);
    }
}

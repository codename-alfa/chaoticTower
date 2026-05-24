package com.alfa.chaotictower.magic.dark;

import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellContext;


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

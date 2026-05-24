package com.alfa.chaotictower.magic.dark;

import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellContext;


public class SpeedUpSpell extends Spell {

    public SpeedUpSpell() {
        super("Speed Up", "Opponent's blocks fall faster", false, 10f);
    }

    @Override
    public void apply(SpellContext context) {
        context.target.setSpedUp(true);
    }

    @Override
    public void remove(SpellContext context) {
        context.target.setSpedUp(false);
    }
}

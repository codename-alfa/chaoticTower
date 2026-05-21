package com.alfa.chaotictower.magic.dark;

import com.alfa.chaotictower.magic.Spell;
import com.alfa.chaotictower.magic.SpellContext;

/**
 * Dark Magic: Speed Up
 * Forces the opponent's blocks to fall at 3x speed for 10 seconds.
 * The actual speed override is handled in SoftDropCommand / InputHandler
 * by checking player.isSpedUp().
 */
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

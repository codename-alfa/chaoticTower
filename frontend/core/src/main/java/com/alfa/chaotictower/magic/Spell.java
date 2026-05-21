package com.alfa.chaotictower.magic;

import com.alfa.chaotictower.entity.Player;
import com.alfa.chaotictower.screen.PlayingScreen;

/**
 * Base class for all magic spells in the game.
 * Light spells help the caster; Dark spells hinder the opponent.
 */
public abstract class Spell {

    private final String name;
    private final String description;
    private final boolean isLight;  // true = Light (buff), false = Dark (debuff)
    private final float duration;   // 0 = instant, >0 = timed effect in seconds

    public Spell(String name, String description, boolean isLight, float duration) {
        this.name = name;
        this.description = description;
        this.isLight = isLight;
        this.duration = duration;
    }

    /**
     * Apply the spell effect.
     * For Light spells: target == caster.
     * For Dark spells: target == opponent.
     */
    public abstract void apply(SpellContext context);

    /**
     * Remove the spell effect when it expires (for timed spells).
     * Override in subclasses that have ongoing effects.
     */
    public void remove(SpellContext context) {
        // Default: no-op for instant spells
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isLight() { return isLight; }
    public float getDuration() { return duration; }
    public boolean isInstant() { return duration <= 0; }
}

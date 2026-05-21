package com.alfa.chaotictower.magic;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.entity.Player;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages spell availability, active effects, and cooldowns for all players.
 * Spells are granted based on tower height milestones.
 */
public class SpellManager {

    /** Height interval between spell grants (in Box2D world units). */
    private static final float SPELL_GRANT_INTERVAL = 4.0f;

    private final int playerCount;

    /** Available (uncast) spell per player. null = no spell available. */
    private final Spell[] availableSpell;

    /** Height at which the next spell will be granted per player. */
    private final float[] nextSpellHeight;

    /** Currently active timed effects per player (effects ON them). */
    private final List<ActiveSpell>[] activeEffects;

    /** All spell definitions (Light and Dark). */
    private final List<Spell> lightSpells = new ArrayList<>();
    private final List<Spell> darkSpells  = new ArrayList<>();

    private int lightIndex = 0;
    private int darkIndex  = 0;

    @SuppressWarnings("unchecked")
    public SpellManager(int playerCount) {
        this.playerCount = playerCount;
        availableSpell = new Spell[playerCount];
        nextSpellHeight = new float[playerCount];
        activeEffects = new List[playerCount];

        for (int i = 0; i < playerCount; i++) {
            nextSpellHeight[i] = SPELL_GRANT_INTERVAL;
            activeEffects[i] = new ArrayList<>();
        }
    }

    public void registerLightSpell(Spell spell) { lightSpells.add(spell); }
    public void registerDarkSpell(Spell spell)  { darkSpells.add(spell); }

    /**
     * Called every frame. Checks height milestones and updates timed effects.
     */
    public void update(float delta, Player[] players, float[] maxHeights, World world, Array<Block> activeBlocks) {
        // Grant spells based on height
        for (int i = 0; i < players.length; i++) {
            if (availableSpell[i] == null && maxHeights[i] >= nextSpellHeight[i]) {
                // Auto-grant: alternate light/dark based on height milestone count
                int milestone = (int)(maxHeights[i] / SPELL_GRANT_INTERVAL);
                if (milestone % 2 == 1 && !darkSpells.isEmpty() && playerCount > 1) {
                    availableSpell[i] = darkSpells.get(darkIndex % darkSpells.size());
                    darkIndex++;
                } else if (!lightSpells.isEmpty()) {
                    availableSpell[i] = lightSpells.get(lightIndex % lightSpells.size());
                    lightIndex++;
                }
                nextSpellHeight[i] += SPELL_GRANT_INTERVAL;
            }
        }

        // Update active timed effects
        for (int i = 0; i < playerCount; i++) {
            List<ActiveSpell> effects = activeEffects[i];
            for (int j = effects.size() - 1; j >= 0; j--) {
                ActiveSpell as = effects.get(j);
                as.remaining -= delta;
                if (as.remaining <= 0) {
                    as.spell.remove(as.context);
                    effects.remove(j);
                }
            }
        }
    }

    /**
     * Cast the available spell for the given player.
     * @param casterIndex 0-based player index
     * @param players array of all players
     * @return the spell that was cast, or null if none available
     */
    public Spell castSpell(int casterIndex, Player[] players, World world, Array<Block> activeBlocks) {
        Spell spell = availableSpell[casterIndex];
        if (spell == null) return null;

        Player caster = players[casterIndex];
        Player target;

        if (spell.isLight()) {
            target = caster; // Light spells target self
        } else {
            // Dark spells target opponent (in 2P) or self (in 1P, as penalty)
            target = (playerCount > 1) ? players[1 - casterIndex] : caster;
        }

        SpellContext context = new SpellContext(caster, target, world, activeBlocks);
        spell.apply(context);

        if (!spell.isInstant()) {
            int targetIndex = spell.isLight() ? casterIndex : (playerCount > 1 ? 1 - casterIndex : casterIndex);
            activeEffects[targetIndex].add(new ActiveSpell(spell, context, spell.getDuration()));
        }

        availableSpell[casterIndex] = null;
        return spell;
    }

    /** Returns the available spell for the player, or null. */
    public Spell getAvailableSpell(int playerIndex) {
        return (playerIndex >= 0 && playerIndex < availableSpell.length) ? availableSpell[playerIndex] : null;
    }

    /** Check if a specific effect type is active on a player. */
    public boolean hasActiveEffect(int playerIndex, Class<? extends Spell> spellType) {
        if (playerIndex < 0 || playerIndex >= playerCount) return false;
        for (ActiveSpell as : activeEffects[playerIndex]) {
            if (spellType.isInstance(as.spell)) return true;
        }
        return false;
    }

    /** Inner class tracking an active timed spell effect. */
    private static class ActiveSpell {
        final Spell spell;
        final SpellContext context;
        float remaining;

        ActiveSpell(Spell spell, SpellContext context, float remaining) {
            this.spell = spell;
            this.context = context;
            this.remaining = remaining;
        }
    }
}

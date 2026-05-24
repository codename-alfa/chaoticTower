package com.alfa.chaotictower.magic;

import com.alfa.chaotictower.entity.Block;
import com.alfa.chaotictower.entity.Player;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;


public class SpellManager {
    
    private static final float SPELL_GRANT_INTERVAL = 4.0f;

    private final int playerCount;

    
    private final Spell[] availableSpell;

    
    private final float[] nextSpellHeight;

    
    private final List<ActiveSpell>[] activeEffects;

    
    private final float[] spellCooldown;

    
    private final List<Spell> lightSpells = new ArrayList<>();
    private final List<Spell> darkSpells  = new ArrayList<>();
    private final List<Spell> allSpells   = new ArrayList<>();

    private final java.util.Random random = new java.util.Random();

    private int lightIndex = 0;
    private int darkIndex  = 0;

    @SuppressWarnings("unchecked")
    public SpellManager(int playerCount) {
        this.playerCount = playerCount;
        availableSpell = new Spell[playerCount];
        nextSpellHeight = new float[playerCount];
        activeEffects = new List[playerCount];
        spellCooldown = new float[playerCount];

        for (int i = 0; i < playerCount; i++) {
            nextSpellHeight[i] = SPELL_GRANT_INTERVAL;
            activeEffects[i] = new ArrayList<>();
            spellCooldown[i] = 0.0f;
        }
    }

    public void registerLightSpell(Spell spell) {
        lightSpells.add(spell);
        allSpells.add(spell);
    }

    public void registerDarkSpell(Spell spell) {
        darkSpells.add(spell);
        allSpells.add(spell);
    }

    public Spell getRandomSpell() {
        if (allSpells.isEmpty()) return null;
        return allSpells.get(random.nextInt(allSpells.size()));
    }

    
    public void update(float delta, Player[] players, float[] maxHeights, World world, Array<Block> activeBlocks) {
        
        if (playerCount != 2) return;

        
        for (int i = 0; i < players.length; i++) {
            if (maxHeights[i] >= nextSpellHeight[i]) {
                
                if (availableSpell[i] == null && spellCooldown[i] <= 0) {
                    availableSpell[i] = getRandomSpell();
                }
                nextSpellHeight[i] += SPELL_GRANT_INTERVAL;
            }
        }

        
        for (int i = 0; i < playerCount; i++) {
            if (spellCooldown[i] > 0) {
                spellCooldown[i] -= delta;
                if (spellCooldown[i] <= 0) {
                    spellCooldown[i] = 0.0f;
                    
                    availableSpell[i] = getRandomSpell();
                }
            }
        }

        
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

    
    public Spell castSpell(int casterIndex, Player[] players, World world, Array<Block> activeBlocks) {
        if (playerCount != 2) return null; 

        Spell spell = availableSpell[casterIndex];
        if (spell == null) return null;

        
        if (spellCooldown[casterIndex] > 0) return null;

        Player caster = players[casterIndex];
        Player target;

        if (spell.isLight()) {
            target = caster; 
        } else {
            
            target = players[1 - casterIndex];
        }

        SpellContext context = new SpellContext(caster, target, world, activeBlocks);
        spell.apply(context);

        if (!spell.isInstant()) {
            int targetIndex = spell.isLight() ? casterIndex : 1 - casterIndex;
            activeEffects[targetIndex].add(new ActiveSpell(spell, context, spell.getDuration()));
        }

        availableSpell[casterIndex] = null;
        spellCooldown[casterIndex] = 15.0f; 
        return spell;
    }

    
    public Spell getAvailableSpell(int playerIndex) {
        return (playerIndex >= 0 && playerIndex < availableSpell.length) ? availableSpell[playerIndex] : null;
    }

    
    public float getSpellCooldown(int playerIndex) {
        if (playerIndex >= 0 && playerIndex < spellCooldown.length) {
            return spellCooldown[playerIndex];
        }
        return 0.0f;
    }

    
    public boolean isOnCooldown(int playerIndex) {
        return getSpellCooldown(playerIndex) > 0.0f;
    }

    
    public boolean hasActiveEffect(int playerIndex, Class<? extends Spell> spellType) {
        if (playerIndex < 0 || playerIndex >= playerCount) return false;
        for (ActiveSpell as : activeEffects[playerIndex]) {
            if (spellType.isInstance(as.spell)) return true;
        }
        return false;
    }

    
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

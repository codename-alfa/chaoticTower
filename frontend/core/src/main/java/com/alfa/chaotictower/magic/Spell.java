package com.alfa.chaotictower.magic;

import com.alfa.chaotictower.entity.Player;
import com.alfa.chaotictower.screen.PlayingScreen;


public abstract class Spell {

    private final String name;
    private final String description;
    private final boolean isLight;  
    private final float duration;   

    public Spell(String name, String description, boolean isLight, float duration) {
        this.name = name;
        this.description = description;
        this.isLight = isLight;
        this.duration = duration;
    }

    
    public abstract void apply(SpellContext context);

    
    public void remove(SpellContext context) {
        
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isLight() { return isLight; }
    public float getDuration() { return duration; }
    public boolean isInstant() { return duration <= 0; }
}

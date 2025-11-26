package com.alecalbright.skyrimbuildanalyzer.model;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class Character {
    private final String name;
    private double health;
    private final double maxHealth;
    private double stamina;
    private final double maxStamina;
    private double magicka;
    private final double maxMagicka;

    // Skyrim skills are represented by levels 0-100
    private int oneHandedSkill;
    private int twoHandedSkill;
    private int archerySkill;
    private int sneakSkill;

    private final Weapon weapon;

    public Character(String name, double maxHealth, double maxStamina, double maxMagicka, Weapon weapon) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.maxStamina = maxStamina;
        this.maxMagicka = maxMagicka;
        this.weapon = weapon;
        
        this.health = maxHealth;
        this.stamina = maxStamina;
        this.magicka = maxMagicka;
        
        this.oneHandedSkill = 0;
        this.twoHandedSkill = 0;
        this.archerySkill = 0;
        this.sneakSkill = 0;

    }

    public boolean isAlive(){
        return health > 0;
    }

    public void takeDamage(double damage){
        this.health -= damage;
        if(this.health < 0){
            this.health = 0;
        }
    }

    public void reset() {
        this.health = maxHealth;
        this.stamina = maxStamina;
        this.magicka = maxMagicka;
    }

    public double calculateDamage(){
        return weapon.getBaseDamage();
    }

    @Override
    public String toString(){
        return String.format("%s [HP: %.0f/%.0f, Weapon: %s]", name, health, maxHealth, weapon.getName());
    }
}



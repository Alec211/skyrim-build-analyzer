package com.alecalbright.skyrimbuildanalyzer.model;

import java.util.HashSet;
import java.util.Set;

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

    private int oneHandedSkill;
    private int twoHandedSkill;
    private int archerySkill;
    private int sneakSkill;

    private Set<Perk> perks;

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

        this.perks = new HashSet<>();
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

    public double calculateDamage(boolean isSneakAttack, boolean isCritical){
        double damage = weapon.getBaseDamage() * getDamageBoostMultiplier();

        if (isSneakAttack) {
            damage *= getSneakAttackMultiplier();
        }
        if (isCritical) {
            damage *= getCriticalDamageMultiplier();
        }

        return damage;
    }

    public void addPerk(Perk perk){
        this.perks.add(perk);
    }

    public boolean hasPerk(Perk perk){
        return this.perks.contains(perk);
    }

    public Set<Perk> getPerks(){
        return Set.copyOf(perks);
    }

    public double getDamageBoostMultiplier(){
        double multiplier = 1.0;
        for (Perk perk : perks) {
            if (perk.getCategory() == PerkCategory.DAMAGE_BOOST) {
                multiplier *= perk.getMultiplier();
            }
        }
        return multiplier;
    }

    public double getSneakAttackMultiplier(){
        double highest = 1.0;
        for (Perk perk : perks) {
            if (perk.getCategory() == PerkCategory.SNEAK_ATTACK && perk.getMultiplier() > highest) {
                highest = perk.getMultiplier();
            }
        }
        return highest;
    }

    public double getCriticalChance(){
        double chance = 0.05;
        for (Perk perk : perks) {
            if (perk.getCategory() == PerkCategory.CRITICAL) {
                if (perk == Perk.CRITICAL_SHOT) chance += 0.15;
                if (perk == Perk.CRITICAL_CHARGE) chance += 0.10;
            }
        }
        return Math.min(chance, 0.50);
    }

    public double getCriticalDamageMultiplier(){
        double multiplier = 1.5;
        if (hasPerk(Perk.CRITICAL_SHOT)) {
            multiplier = 2.0;
        }
        return multiplier;
    }

    public boolean canSneakAttack(){
        return sneakSkill >= 50 && getSneakAttackMultiplier() > 1.0;
    }

    public double getPerkDamageMultiplier(){
        return getDamageBoostMultiplier() * getSneakAttackMultiplier();
    }

    @Override
    public String toString(){
        return String.format("%s [HP: %.0f/%.0f, Weapon: %s]", name, health, maxHealth, weapon.getName());
    }
}



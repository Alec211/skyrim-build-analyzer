package com.alecalbright.skyrimbuildanalyzer.model;

import java.util.List;

import com.alecalbright.skyrimbuildanalyzer.repository.ArmorRepository;
import com.alecalbright.skyrimbuildanalyzer.repository.WeaponRepository;

import lombok.Getter;

@Getter
public class EnemyDefinition {
    private final String name;
    private final int level;
    private final double health;
    private final double stamina;
    private final double magicka;
    private final EnemyCategory category;
    private final String weaponName;
    private final String armorName;
    private final String shieldName;
    private final List<Perk> perks;

    public EnemyDefinition(String name, int level, double health, double stamina,
                           double magicka, EnemyCategory category, String weaponName,
                           String armorName, String shieldName, List<Perk> perks) {
        this.name = name;
        this.level = level;
        this.health = health;
        this.stamina = stamina;
        this.magicka = magicka;
        this.category = category;
        this.weaponName = weaponName;
        this.armorName = armorName;
        this.shieldName = shieldName;
        this.perks = perks != null ? List.copyOf(perks) : List.of();
    }

    public EnemyDefinition(String name, int level, double health, double stamina,
                           double magicka, EnemyCategory category, String weaponName) {
        this(name, level, health, stamina, magicka, category, weaponName, null, null, null);
    }

    public Character toCharacter(WeaponRepository weaponRepository, ArmorRepository armorRepository) {
        Weapon weapon = weaponRepository.getWeapon(weaponName);
        if (weapon == null) {
            throw new IllegalStateException("Weapon not found for enemy " + name + ": " + weaponName);
        }

        Armor armor = (armorName != null) ? armorRepository.getArmor(armorName) : null;
        Shield shield = (shieldName != null) ? armorRepository.getShield(shieldName) : null;

        Character character = new Character(name, health, stamina, magicka, weapon, armor, shield);

        for (Perk perk : perks) {
            character.addPerk(perk);
        }

        return character;
    }

    @Override
    public String toString() {
        return String.format("%s (Lvl %d, HP %.0f, %s)", name, level, health, category.getDisplayName());
    }
}

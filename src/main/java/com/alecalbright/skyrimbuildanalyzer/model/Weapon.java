package com.alecalbright.skyrimbuildanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

// Represents a weapon in Skyrim with given specifications

@Data
@AllArgsConstructor
public class Weapon {
    private final String name;
    private final double baseDamage;
    private final double attackSpeed;
    private final WeaponType weaponType;

    public double getDPS(){
        return baseDamage * attackSpeed;
    }
}

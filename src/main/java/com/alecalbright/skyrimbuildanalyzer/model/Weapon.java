package com.alecalbright.skyrimbuildanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

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

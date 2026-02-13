package com.alecalbright.skyrimbuildanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

// @Data automatically generates the standard get() methods including equals, hashCode, and toString
// @AllArgsConstructor generates a class constructor with all args

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

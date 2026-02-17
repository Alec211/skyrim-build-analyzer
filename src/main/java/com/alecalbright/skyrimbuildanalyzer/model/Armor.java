package com.alecalbright.skyrimbuildanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Armor {
    private final String name;
    private final double baseArmorRating;
    private final ArmorType armorType;

    public ArmorWeight getWeight() {
        return armorType.getWeight();
    }

    public int getTier() {
        return armorType.getTier();
    }
}

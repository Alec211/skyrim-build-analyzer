package com.alecalbright.skyrimbuildanalyzer.model;

public enum ArmorType {
    // Light Armor
    HIDE(ArmorWeight.LIGHT, 1),
    LEATHER(ArmorWeight.LIGHT, 1),
    ELVEN(ArmorWeight.LIGHT, 2),
    SCALED(ArmorWeight.LIGHT, 2),
    GLASS(ArmorWeight.LIGHT, 3),
    DRAGONSCALE(ArmorWeight.LIGHT, 4),

    // Heavy Armor
    IRON(ArmorWeight.HEAVY, 1),
    STEEL(ArmorWeight.HEAVY, 1),
    DWARVEN(ArmorWeight.HEAVY, 2),
    ORCISH(ArmorWeight.HEAVY, 2),
    EBONY(ArmorWeight.HEAVY, 3),
    DAEDRIC(ArmorWeight.HEAVY, 4),
    DRAGONPLATE(ArmorWeight.HEAVY, 4),

    // No armor
    NONE(ArmorWeight.NONE, 0);

    private final ArmorWeight weight;
    private final int tier;

    ArmorType(ArmorWeight weight, int tier) {
        this.weight = weight;
        this.tier = tier;
    }

    public ArmorWeight getWeight() {
        return weight;
    }

    public int getTier() {
        return tier;
    }
}

package com.alecalbright.skyrimbuildanalyzer.model;

public enum Perk {

    // ONE-HANDED PERKS

    ARMSMAN_1("Armsman 1/5", 1.2, PerkCategory.DAMAGE_BOOST),
    ARMSMAN_2("Armsman 2/5", 1.4, PerkCategory.DAMAGE_BOOST),
    ARMSMAN_3("Armsman 3/5", 1.6, PerkCategory.DAMAGE_BOOST),
    ARMSMAN_4("Armsman 4/5", 1.8, PerkCategory.DAMAGE_BOOST),
    ARMSMAN_5("Armsman 5/5", 2.0, PerkCategory.DAMAGE_BOOST),

    DUAL_FLURRY("Dual Flurry", 1.2, PerkCategory.SPEED),
    DUAL_SAVAGERY("Dual Savagery", 1.5, PerkCategory.DAMAGE_BOOST),
    CRITICAL_CHARGE("Critical Charge", 1.0, PerkCategory.CRITICAL),

    // TWO-HANDED PERKS

    BARBARIAN_1("Barbarian 1/5", 1.2, PerkCategory.DAMAGE_BOOST),
    BARBARIAN_2("Barbarian 2/5", 1.4, PerkCategory.DAMAGE_BOOST),
    BARBARIAN_3("Barbarian 3/5", 1.6, PerkCategory.DAMAGE_BOOST),
    BARBARIAN_4("Barbarian 4/5", 1.8, PerkCategory.DAMAGE_BOOST),
    BARBARIAN_5("Barbarian 5/5", 2.0, PerkCategory.DAMAGE_BOOST),

    CHAMPIONS_STANCE("Champion's Stance", 1.25, PerkCategory.DAMAGE_BOOST),
    DEVASTATING_BLOW("Devastating Blow", 1.0, PerkCategory.UTILITY),

    // ARCHERY PERKS

    OVERDRAW_1("Overdraw 1/5", 1.2, PerkCategory.DAMAGE_BOOST),
    OVERDRAW_2("Overdraw 2/5", 1.4, PerkCategory.DAMAGE_BOOST),
    OVERDRAW_3("Overdraw 3/5", 1.6, PerkCategory.DAMAGE_BOOST),
    OVERDRAW_4("Overdraw 4/5", 1.8, PerkCategory.DAMAGE_BOOST),
    OVERDRAW_5("Overdraw 5/5", 2.0, PerkCategory.DAMAGE_BOOST),

    CRITICAL_SHOT("Critical Shot", 1.5, PerkCategory.CRITICAL),
    HUNTERS_DISCIPLINE("Hunter's Discipline", 1.25, PerkCategory.DAMAGE_BOOST),
    QUICK_SHOT("Quick Shot", 1.3, PerkCategory.SPEED),
    STEADY_HAND("Steady Hand", 1.0, PerkCategory.UTILITY),
    RANGER("Ranger", 1.0, PerkCategory.UTILITY),

    // SNEAK PERKS

    BACKSTAB("Backstab", 6.0, PerkCategory.SNEAK_ATTACK),
    ASSASSINS_BLADE("Assassin's Blade", 15.0, PerkCategory.SNEAK_ATTACK),
    DEADLY_AIM("Deadly Aim", 3.0, PerkCategory.SNEAK_ATTACK),
    SHADOW_WARRIOR("Shadow Warrior", 1.0, PerkCategory.UTILITY),

    // HEAVY ARMOR PERKS

    JUGGERNAUT_1("Juggernaut 1/5", 1.2, PerkCategory.DEFENSE),
    JUGGERNAUT_2("Juggernaut 2/5", 1.4, PerkCategory.DEFENSE),
    JUGGERNAUT_3("Juggernaut 3/5", 1.6, PerkCategory.DEFENSE),
    JUGGERNAUT_4("Juggernaut 4/5", 1.8, PerkCategory.DEFENSE),
    JUGGERNAUT_5("Juggernaut 5/5", 2.0, PerkCategory.DEFENSE),

    CONDITIONING("Conditioning", 1.0, PerkCategory.UTILITY),
    MATCHING_SET("Matching Set", 1.25, PerkCategory.DEFENSE),

    // LIGHT ARMOR PERKS

    AGILE_DEFENDER_1("Agile Defender 1/5", 1.2, PerkCategory.DEFENSE),
    AGILE_DEFENDER_2("Agile Defender 2/5", 1.4, PerkCategory.DEFENSE),
    AGILE_DEFENDER_3("Agile Defender 3/5", 1.6, PerkCategory.DEFENSE),
    AGILE_DEFENDER_4("Agile Defender 4/5", 1.8, PerkCategory.DEFENSE),
    AGILE_DEFENDER_5("Agile Defender 5/5", 2.0, PerkCategory.DEFENSE),

    WIND_WALKER("Wind Walker", 1.0, PerkCategory.UTILITY),

    // BLOCK PERKS

    SHIELD_WALL_1("Shield Wall 1/5", 1.2, PerkCategory.DEFENSE),
    SHIELD_WALL_2("Shield Wall 2/5", 1.4, PerkCategory.DEFENSE),
    SHIELD_WALL_3("Shield Wall 3/5", 1.6, PerkCategory.DEFENSE),
    SHIELD_WALL_4("Shield Wall 4/5", 1.8, PerkCategory.DEFENSE),
    SHIELD_WALL_5("Shield Wall 5/5", 2.0, PerkCategory.DEFENSE),

    SHIELD_CHARGE("Shield Charge", 1.0, PerkCategory.UTILITY);

    private final String displayName;
    private final double multiplier;
    private final PerkCategory category;

    Perk(String displayName, double multiplier, PerkCategory category){
        this.displayName = displayName;
        this.multiplier = multiplier;
        this.category = category;
    }

    public String getDisplayName(){
        return displayName;
    }

    public double getMultiplier(){
        return multiplier;
    }

    public PerkCategory getCategory(){
        return category;
    }

    public boolean boostsDamage(){
        return multiplier > 1.0;
    }

    public double getDamageIncreasePercent(){
        return (multiplier - 1.0) * 100.0;
    }

    @Override
    public String toString(){
        if(boostsDamage()){
            return displayName + " (+" + String.format("%.0f", getDamageIncreasePercent()) + "% damage)";
        }

        return displayName;
    }
}

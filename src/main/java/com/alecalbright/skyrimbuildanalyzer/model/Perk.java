package com.alecalbright.skyrimbuildanalyzer.model;

public enum Perk {

    // ONE-HANDED PERKS

    // One-handed weapons do 20% more damage per rank (5 ranks total)
    ARMSMAN_1("Armsman 1/5", 1.2),
    ARMSMAN_2("Armsman 2/5", 1.4),
    ARMSMAN_3("Armsman 3/5", 1.6),
    ARMSMAN_4("Armsman 4/5", 1.8),
    ARMSMAN_5("Armsman 5/5", 2.0),
    
    // Attacks with one-handed weapons are 20% faster
    DUAL_FLURRY("Dual Flurry", 1.2),
    
    // Dual-wielding power attacks do 50% more damage
    DUAL_SAVAGERY("Dual Savagery", 1.5),
    
    // Critical hit chance increased by 25%
    CRITICAL_CHARGE("Critical Charge", 1.0),
    
    //TWO-HANDED PERKS
    
    // Two-handed weapons do 20% more damage per rank (5 ranks total)
    BARBARIAN_1("Barbarian 1/5", 1.2),
    BARBARIAN_2("Barbarian 2/5", 1.4),
    BARBARIAN_3("Barbarian 3/5", 1.6),
    BARBARIAN_4("Barbarian 4/5", 1.8),
    BARBARIAN_5("Barbarian 5/5", 2.0),
    
    // Standing power attacks do 25% bonus damage with a chance to decapitate
    CHAMPIONS_STANCE("Champion's Stance", 1.25),
    
    // Power attacks with two-handed weapons have a chance to knock down
    DEVASTATING_BLOW("Devastating Blow", 1.0),
    
    // ARCHERY PERKS
    
    //Bows do 20% more damage per rank (5 ranks total)
    OVERDRAW_1("Overdraw 1/5", 1.2),
    OVERDRAW_2("Overdraw 2/5", 1.4),
    OVERDRAW_3("Overdraw 3/5", 1.6),
    OVERDRAW_4("Overdraw 4/5", 1.8),
    OVERDRAW_5("Overdraw 5/5", 2.0),
    
    // Bows do 50% more critical damage
    CRITICAL_SHOT("Critical Shot", 1.5),
    
    // Bows deal 25% more damage to animals
    HUNTERS_DISCIPLINE("Hunter's Discipline", 1.25),
    
    // Able to draw a bow 30% faster
    QUICK_SHOT("Quick Shot", 1.3),
    
    // Steady Hand - zooming in slows time
    STEADY_HAND("Steady Hand", 1.0),
    
    // Can move faster with a drawn bow
    RANGER("Ranger", 1.0),
    
    // SNEAK PERKS
    
    // Sneak attacks with one-handed weapons do 6x damage
    BACKSTAB("Backstab", 6.0),
    
    // Sneak attacks with daggers do 15x damage
    ASSASSINS_BLADE("Assassin's Blade", 15.0),
    
    // Sneak attacks with bows do 3x damage
     DEADLY_AIM("Deadly Aim", 3.0),
    
    // Moving in shadows reduces detection by 50%
    SHADOW_WARRIOR("Shadow Warrior", 1.0),
    
    // HEAVY ARMOR PERKS
    
    // Heavy armor rating increased by 20% per rank (5 ranks)
    JUGGERNAUT_1("Juggernaut 1/5", 1.2),
    JUGGERNAUT_2("Juggernaut 2/5", 1.4),
    JUGGERNAUT_3("Juggernaut 3/5", 1.6),
    JUGGERNAUT_4("Juggernaut 4/5", 1.8),
    JUGGERNAUT_5("Juggernaut 5/5", 2.0),
    
    // Heavy armor weighs nothing and doesn't slow you down
    CONDITIONING("Conditioning", 1.0),
    
    // Armor rating increased by 25% when wearing a matched set
    MATCHING_SET("Matching Set", 1.25),
    
    // LIGHT ARMOR PERKS
    
    // Light armor rating increased by 20% per rank (5 ranks)
    AGILE_DEFENDER_1("Agile Defender 1/5", 1.2),
    AGILE_DEFENDER_2("Agile Defender 2/5", 1.4),
    AGILE_DEFENDER_3("Agile Defender 3/5", 1.6),
    AGILE_DEFENDER_4("Agile Defender 4/5", 1.8),
    AGILE_DEFENDER_5("Agile Defender 5/5", 2.0),
    
    // 10% chance to avoid all damage from a melee attack
    WIND_WALKER("Wind Walker", 1.0),
    
    // BLOCK PERKS
    
    // Block 20% more damage per rank (5 ranks)
    SHIELD_WALL_1("Shield Wall 1/5", 1.2),
    SHIELD_WALL_2("Shield Wall 2/5", 1.4),
    SHIELD_WALL_3("Shield Wall 3/5", 1.6),
    SHIELD_WALL_4("Shield Wall 4/5", 1.8),
    SHIELD_WALL_5("Shield Wall 5/5", 2.0),
    
    // Sprinting with a shield raised knocks down enemies
    SHIELD_CHARGE("Shield Charge", 1.0);

    private final String displayName;
    private final double multiplier;

    Perk(String displayName, double multiplier){
        this.displayName = displayName;
        this.multiplier = multiplier;
    }

    public String getDisplayName(){
        return displayName;
    }

    public double getMultiplier(){
        return multiplier;
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

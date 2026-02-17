package com.alecalbright.skyrimbuildanalyzer.dto;

import java.util.Map;

public record DamageResponse(
    String archetype,
    String weaponName,
    double weaponBaseDamage,
    double weaponSpeed,
    double weaponDPS,
    double perkMultiplier,
    double theoreticalDamagePerHit,
    Map<String, Double> perkBreakdown,
    String balanceWarning
) {}

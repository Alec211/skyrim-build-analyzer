package com.alecalbright.skyrimbuildanalyzer.dto;

import java.util.List;
import java.util.Map;

public record PerkEfficiencyResponse(
    List<ArchetypeEfficiency> rankings,
    Map<String, String> balanceWarnings
) {

    public record ArchetypeEfficiency(
        String archetype,
        double damagePerPerk,
        int perkCount,
        double perkMultiplier,
        double theoreticalDamage
    ) {}
}

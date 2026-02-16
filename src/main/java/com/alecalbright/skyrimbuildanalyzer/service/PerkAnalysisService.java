package com.alecalbright.skyrimbuildanalyzer.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alecalbright.skyrimbuildanalyzer.archetype.CharacterArchetype;
import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.Perk;
import com.alecalbright.skyrimbuildanalyzer.repository.WeaponRepository;

@Service
public class PerkAnalysisService {

    private final WeaponRepository weaponRepository;

    public PerkAnalysisService(WeaponRepository weaponRepository){
        this.weaponRepository = weaponRepository;
    }

    public double calculateTheoreticalDamage(CharacterArchetype archetype){
        Character character = archetype.create(weaponRepository);
        double baseDamage = character.calculateDamage();
        double perkMultiplier = character.getPerkDamageMultiplier();
        return baseDamage * perkMultiplier;
    }

    /**
     * Returns a map of each perk to its individual multiplier contribution for a given archetype.
     */
    public Map<Perk, Double> getPerkBreakdown(CharacterArchetype archetype){
        Map<Perk, Double> breakdown = new HashMap<>();
        for (Perk perk : archetype.getPerks()) {
            if (perk.boostsDamage()) {
                breakdown.put(perk, perk.getMultiplier());
            }
        }
        return breakdown;
    }

    /**
     * Ranks all archetypes by their theoretical damage per perk invested.
     * Higher = more efficient perk usage.
     */
    public List<Map.Entry<CharacterArchetype, Double>> comparePerkEfficiency(){
        Map<CharacterArchetype, Double> efficiencyMap = new HashMap<>();

        for (CharacterArchetype archetype : CharacterArchetype.values()) {
            double theoreticalDmg = calculateTheoreticalDamage(archetype);
            int perkCount = archetype.getPerks().length;
            double efficiency = perkCount > 0 ? theoreticalDmg / perkCount : 0.0;
            efficiencyMap.put(archetype, efficiency);
        }

        List<Map.Entry<CharacterArchetype, Double>> sorted = new ArrayList<>(efficiencyMap.entrySet());
        sorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        return sorted;
    }

    /**
     * Flags archetypes where perk multiplier stacking produces unrealistic damage.
     * Anything over 10x total multiplier is likely unrealistic.
     */
    public Map<CharacterArchetype, String> identifyOverkillPerks(){
        Map<CharacterArchetype, String> warnings = new HashMap<>();

        for (CharacterArchetype archetype : CharacterArchetype.values()) {
            Character character = archetype.create(weaponRepository);
            double multiplier = character.getPerkDamageMultiplier();

            if (multiplier > 50.0) {
                warnings.put(archetype, String.format(
                    "%.1fx multiplier — sneak perks are stacking multiplicatively instead of replacing each other",
                    multiplier));
            } else if (multiplier > 10.0) {
                warnings.put(archetype, String.format(
                    "%.1fx multiplier — high but may be intentional for sneak builds",
                    multiplier));
            }
        }

        return warnings;
    }
}

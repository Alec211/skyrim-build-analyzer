package com.alecalbright.skyrimbuildanalyzer.model;

import com.alecalbright.skyrimbuildanalyzer.archetype.CharacterArchetype;
import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

public record MatchupResult(
    CharacterArchetype archetype1,
    CharacterArchetype archetype2,
    MultiSimulationResult simulationResult,
    double archetype1WinRate,
    double confidenceInterval
) {
    public MatchupResult {
        if (archetype1 == null || archetype2 == null) {
            throw new IllegalArgumentException("Archetypes cannot be null");
        }
        if (simulationResult == null) {
            throw new IllegalArgumentException("Simulation result cannot be null");
        }
    }

    public CharacterArchetype getWinningArchetype(){
        if (archetype1WinRate > 50.0) return archetype1;
        if (archetype1WinRate < 50.0) return archetype2;
        return null;
    }

    public boolean isDecisive(){
        return Math.abs(archetype1WinRate - 50.0) > 15.0;
    }

    @Override
    public String toString(){
        return String.format("%s vs %s: %.1f%% - %.1f%%",
            archetype1.getDisplayName(),
            archetype2.getDisplayName(),
            archetype1WinRate,
            100.0 - archetype1WinRate);
    }
}

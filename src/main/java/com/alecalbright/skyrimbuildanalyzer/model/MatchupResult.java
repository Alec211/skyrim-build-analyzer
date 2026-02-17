package com.alecalbright.skyrimbuildanalyzer.model;

import com.alecalbright.skyrimbuildanalyzer.archetype.CharacterArchetype;
import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

public record MatchupResult(
    CharacterArchetype archetype1,
    CharacterArchetype archetype2,
    MultiSimulationResult simulationResult,
    double archetype1WinRate,
    double confidenceIntervalWidth
) {
    public MatchupResult {
        if (archetype1 == null || archetype2 == null) {
            throw new IllegalArgumentException("Archetypes cannot be null");
        }
        if (simulationResult == null) {
            throw new IllegalArgumentException("Simulation result cannot be null");
        }
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

package com.alecalbright.skyrimbuildanalyzer.model;

import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

public record MatchupResult(
    String fighter1Name,
    String fighter2Name,
    MultiSimulationResult simulationResult,
    double fighter1WinRate,
    double confidenceIntervalWidth
) {

    @Override
    public String toString(){
        return String.format("%s vs %s: %.1f%% - %.1f%%",
            fighter1Name, fighter2Name,
            fighter1WinRate, 100.0 - fighter1WinRate);
    }
}

package com.alecalbright.skyrimbuildanalyzer.simulation;

import java.util.List;

public record MultiSimulationResult(
    String fighter1Name,
    String fighter2Name,
    int totalFights,
    int fighter1Wins,
    int fighter2Wins,
    int draws,
    List<FightResult> allFights
) {

    public MultiSimulationResult {
        allFights = List.copyOf(allFights);
    }

    public double fighter1WinRate(){
        return totalFights == 0 ? 0.0 : (fighter1Wins * 100.0) / totalFights;
    }

    public double fighter2WinRate(){
        return totalFights == 0 ? 0.0 : (fighter2Wins * 100.0) / totalFights;
    }

    public double drawRate(){
        return totalFights == 0 ? 0.0 : (draws * 100.0) / totalFights;
    }

    public String overallWinner(){
        if(fighter1Wins > fighter2Wins) return fighter1Name;
        if(fighter2Wins > fighter1Wins) return fighter2Name;
        return "Tie";
    }

    public int winMargin(){
        return Math.abs(fighter1Wins - fighter2Wins);
    }

    public double averageFightDuration(){
        if(allFights.isEmpty()) return 0.0;
        return allFights.stream().mapToInt(FightResult::totalTurns).average().orElse(0.0);
    }

    public int shortestFightDuration(){
        return allFights.stream().mapToInt(FightResult::totalTurns).min().orElse(0);
    }

    public int longestFightDuration(){
        return allFights.stream().mapToInt(FightResult::totalTurns).max().orElse(0);
    }

    @Override
    public String toString(){
        return String.format(
            "%s vs %s: %d fights | %s wins %.1f%% | %s wins %.1f%% | Draws: %.1f%%",
            fighter1Name, fighter2Name, totalFights,
            fighter1Name, fighter1WinRate(),
            fighter2Name, fighter2WinRate(),
            drawRate()
        );
    }
}

package com.alecalbright.skyrimbuildanalyzer.model;

public record ArchetypeRanking(
    String fighterName,
    int totalWins,
    int totalLosses,
    int totalDraws,
    double overallWinRate,
    String tier,
    String bestMatchup,
    String worstMatchup
) {

    public int totalFights(){
        return totalWins + totalLosses + totalDraws;
    }

    @Override
    public String toString(){
        return String.format("[%s] %s — %.1f%% win rate (%d-%d-%d)",
            tier, fighterName, overallWinRate,
            totalWins, totalLosses, totalDraws);
    }
}

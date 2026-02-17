package com.alecalbright.skyrimbuildanalyzer.dto;

import java.util.List;

import com.alecalbright.skyrimbuildanalyzer.model.ArchetypeRanking;

public record TournamentResponse(
    int fightsPerMatchup,
    List<ArchetypeRanking> rankings,
    List<MatchupSummary> matchups,
    double[][] matchupMatrix,
    List<String> archetypeNames
) {

    public record MatchupSummary(
        String archetype1,
        String archetype2,
        int archetype1Wins,
        int archetype2Wins,
        int draws,
        double archetype1WinRate,
        boolean statisticallySignificant
    ) {}
}

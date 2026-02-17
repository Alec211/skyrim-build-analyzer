package com.alecalbright.skyrimbuildanalyzer.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alecalbright.skyrimbuildanalyzer.archetype.CharacterArchetype;
import com.alecalbright.skyrimbuildanalyzer.model.ArchetypeRanking;
import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.MatchupResult;
import com.alecalbright.skyrimbuildanalyzer.repository.ArmorRepository;
import com.alecalbright.skyrimbuildanalyzer.repository.WeaponRepository;
import com.alecalbright.skyrimbuildanalyzer.simulation.CombatSimulator;
import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

@Service
public class ArchetypeMatchupService {

    private final CombatSimulator combatSimulator;
    private final WeaponRepository weaponRepository;
    private final ArmorRepository armorRepository;
    private final ConfidenceAnalysisService confidenceService;

    public ArchetypeMatchupService(CombatSimulator combatSimulator,
                                   WeaponRepository weaponRepository,
                                   ArmorRepository armorRepository,
                                   ConfidenceAnalysisService confidenceService){
        this.combatSimulator = combatSimulator;
        this.weaponRepository = weaponRepository;
        this.armorRepository = armorRepository;
        this.confidenceService = confidenceService;
    }

    public List<MatchupResult> runFullTournament(int fightsPerMatchup){
        CharacterArchetype[] archetypes = CharacterArchetype.values();
        List<MatchupResult> matchups = new ArrayList<>();

        for (int i = 0; i < archetypes.length; i++) {
            for (int j = i + 1; j < archetypes.length; j++) {
                Character c1 = archetypes[i].create(weaponRepository, armorRepository);
                Character c2 = archetypes[j].create(weaponRepository, armorRepository);

                MultiSimulationResult result = combatSimulator.simulateMultipleFights(c1, c2, fightsPerMatchup);

                double[] ci = confidenceService.calculateWinRateConfidenceInterval(
                    result.fighter1Wins(), result.totalFights());
                double ciWidth = ci[1] - ci[0];

                matchups.add(new MatchupResult(
                    archetypes[i], archetypes[j], result,
                    result.fighter1WinRate(), ciWidth
                ));
            }
        }

        return matchups;
    }

    public double[][] getMatchupMatrix(List<MatchupResult> matchups){
        CharacterArchetype[] archetypes = CharacterArchetype.values();
        int n = archetypes.length;
        double[][] matrix = new double[n][n];

        for (int i = 0; i < n; i++) {
            matrix[i][i] = 50.0;
        }

        for (MatchupResult matchup : matchups) {
            int i = matchup.archetype1().ordinal();
            int j = matchup.archetype2().ordinal();
            matrix[i][j] = matchup.archetype1WinRate();
            matrix[j][i] = 100.0 - matchup.archetype1WinRate();
        }

        return matrix;
    }

    public List<ArchetypeRanking> getArchetypeRankings(List<MatchupResult> matchups){
        CharacterArchetype[] archetypes = CharacterArchetype.values();

        Map<CharacterArchetype, int[]> stats = new HashMap<>();
        for (CharacterArchetype a : archetypes) {
            stats.put(a, new int[]{0, 0, 0});
        }

        for (MatchupResult matchup : matchups) {
            MultiSimulationResult sim = matchup.simulationResult();

            stats.get(matchup.archetype1())[0] += sim.fighter1Wins();
            stats.get(matchup.archetype1())[1] += sim.fighter2Wins();
            stats.get(matchup.archetype1())[2] += sim.draws();

            stats.get(matchup.archetype2())[0] += sim.fighter2Wins();
            stats.get(matchup.archetype2())[1] += sim.fighter1Wins();
            stats.get(matchup.archetype2())[2] += sim.draws();
        }

        double[][] matrix = getMatchupMatrix(matchups);

        Map<CharacterArchetype, String> bestMatchups = new HashMap<>();
        Map<CharacterArchetype, String> worstMatchups = new HashMap<>();

        for (int i = 0; i < archetypes.length; i++) {
            double bestRate = -1;
            double worstRate = 101;
            String best = "None";
            String worst = "None";

            for (int j = 0; j < archetypes.length; j++) {
                if (i == j) continue;
                if (matrix[i][j] > bestRate) {
                    bestRate = matrix[i][j];
                    best = archetypes[j].getDisplayName();
                }
                if (matrix[i][j] < worstRate) {
                    worstRate = matrix[i][j];
                    worst = archetypes[j].getDisplayName();
                }
            }
            bestMatchups.put(archetypes[i], best);
            worstMatchups.put(archetypes[i], worst);
        }

        List<ArchetypeRanking> rankings = new ArrayList<>();
        for (CharacterArchetype archetype : archetypes) {
            int[] s = stats.get(archetype);
            int totalFights = s[0] + s[1] + s[2];
            double winRate = totalFights > 0 ? (s[0] * 100.0) / totalFights : 0.0;
            String tier = assignTier(winRate);

            rankings.add(new ArchetypeRanking(
                archetype, s[0], s[1], s[2], winRate, tier,
                bestMatchups.get(archetype), worstMatchups.get(archetype)
            ));
        }

        rankings.sort(Comparator.comparingDouble(ArchetypeRanking::overallWinRate).reversed());
        return rankings;
    }

    public MatchupResult getSpecificMatchup(CharacterArchetype a1, CharacterArchetype a2, int fightsPerMatchup){
        Character c1 = a1.create(weaponRepository, armorRepository);
        Character c2 = a2.create(weaponRepository, armorRepository);

        MultiSimulationResult result = combatSimulator.simulateMultipleFights(c1, c2, fightsPerMatchup);

        double[] ci = confidenceService.calculateWinRateConfidenceInterval(
            result.fighter1Wins(), result.totalFights());

        return new MatchupResult(a1, a2, result, result.fighter1WinRate(), ci[1] - ci[0]);
    }

    private String assignTier(double winRate){
        if (winRate >= 70.0) return "S";
        if (winRate >= 55.0) return "A";
        if (winRate >= 45.0) return "B";
        if (winRate >= 30.0) return "C";
        return "D";
    }
}

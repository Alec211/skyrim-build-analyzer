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
import com.alecalbright.skyrimbuildanalyzer.model.EnemyDefinition;
import com.alecalbright.skyrimbuildanalyzer.model.MatchupResult;
import com.alecalbright.skyrimbuildanalyzer.repository.ArmorRepository;
import com.alecalbright.skyrimbuildanalyzer.repository.EnemyRepository;
import com.alecalbright.skyrimbuildanalyzer.repository.WeaponRepository;
import com.alecalbright.skyrimbuildanalyzer.simulation.CombatSimulator;
import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

@Service
public class ArchetypeMatchupService {

    private final CombatSimulator combatSimulator;
    private final WeaponRepository weaponRepository;
    private final ArmorRepository armorRepository;
    private final EnemyRepository enemyRepository;
    private final ConfidenceAnalysisService confidenceService;

    public ArchetypeMatchupService(CombatSimulator combatSimulator,
                                   WeaponRepository weaponRepository,
                                   ArmorRepository armorRepository,
                                   EnemyRepository enemyRepository,
                                   ConfidenceAnalysisService confidenceService){
        this.combatSimulator = combatSimulator;
        this.weaponRepository = weaponRepository;
        this.armorRepository = armorRepository;
        this.enemyRepository = enemyRepository;
        this.confidenceService = confidenceService;
    }

    public Character resolveFighter(String name) {
        // Try archetype first
        for (CharacterArchetype archetype : CharacterArchetype.values()) {
            if (archetype.name().equalsIgnoreCase(name)
                || archetype.getDisplayName().equalsIgnoreCase(name)) {
                return archetype.create(weaponRepository, armorRepository);
            }
        }

        // Try enemy
        EnemyDefinition enemy = enemyRepository.getEnemyByName(name);
        if (enemy != null) {
            return enemy.toCharacter(weaponRepository, armorRepository);
        }

        throw new IllegalArgumentException("Unknown fighter: " + name
            + ". Must be a valid archetype or enemy name.");
    }

    public List<String> getAllFighterNames(boolean includeEnemies) {
        List<String> names = new ArrayList<>();
        for (CharacterArchetype a : CharacterArchetype.values()) {
            names.add(a.getDisplayName());
        }
        if (includeEnemies) {
            names.addAll(enemyRepository.getEnemyNames());
        }
        return names;
    }

    public List<MatchupResult> runFullTournament(int fightsPerMatchup, boolean includeEnemies){
        List<String> fighterNames = getAllFighterNames(includeEnemies);
        List<MatchupResult> matchups = new ArrayList<>();

        for (int i = 0; i < fighterNames.size(); i++) {
            for (int j = i + 1; j < fighterNames.size(); j++) {
                Character c1 = resolveFighter(fighterNames.get(i));
                Character c2 = resolveFighter(fighterNames.get(j));

                MultiSimulationResult result = combatSimulator.simulateMultipleFights(c1, c2, fightsPerMatchup);

                double[] ci = confidenceService.calculateWinRateConfidenceInterval(
                    result.fighter1Wins(), result.totalFights());
                double ciWidth = ci[1] - ci[0];

                matchups.add(new MatchupResult(
                    fighterNames.get(i), fighterNames.get(j), result,
                    result.fighter1WinRate(), ciWidth
                ));
            }
        }

        return matchups;
    }

    public List<MatchupResult> runFullTournament(int fightsPerMatchup){
        return runFullTournament(fightsPerMatchup, false);
    }

    public double[][] getMatchupMatrix(List<MatchupResult> matchups, List<String> fighterNames){
        int n = fighterNames.size();
        double[][] matrix = new double[n][n];

        Map<String, Integer> nameToIndex = new HashMap<>();
        for (int i = 0; i < n; i++) {
            nameToIndex.put(fighterNames.get(i), i);
            matrix[i][i] = 50.0;
        }

        for (MatchupResult matchup : matchups) {
            Integer i = nameToIndex.get(matchup.fighter1Name());
            Integer j = nameToIndex.get(matchup.fighter2Name());
            if (i == null || j == null) continue;

            matrix[i][j] = matchup.fighter1WinRate();
            matrix[j][i] = 100.0 - matchup.fighter1WinRate();
        }

        return matrix;
    }

    public List<ArchetypeRanking> getArchetypeRankings(List<MatchupResult> matchups, List<String> fighterNames){
        Map<String, int[]> stats = new HashMap<>();
        for (String name : fighterNames) {
            stats.put(name, new int[]{0, 0, 0});
        }

        for (MatchupResult matchup : matchups) {
            MultiSimulationResult sim = matchup.simulationResult();

            int[] s1 = stats.get(matchup.fighter1Name());
            int[] s2 = stats.get(matchup.fighter2Name());
            if (s1 == null || s2 == null) continue;

            s1[0] += sim.fighter1Wins();
            s1[1] += sim.fighter2Wins();
            s1[2] += sim.draws();

            s2[0] += sim.fighter2Wins();
            s2[1] += sim.fighter1Wins();
            s2[2] += sim.draws();
        }

        double[][] matrix = getMatchupMatrix(matchups, fighterNames);

        Map<String, String> bestMatchups = new HashMap<>();
        Map<String, String> worstMatchups = new HashMap<>();

        for (int i = 0; i < fighterNames.size(); i++) {
            double bestRate = -1;
            double worstRate = 101;
            String best = "None";
            String worst = "None";

            for (int j = 0; j < fighterNames.size(); j++) {
                if (i == j) continue;
                if (matrix[i][j] > bestRate) {
                    bestRate = matrix[i][j];
                    best = fighterNames.get(j);
                }
                if (matrix[i][j] < worstRate) {
                    worstRate = matrix[i][j];
                    worst = fighterNames.get(j);
                }
            }
            bestMatchups.put(fighterNames.get(i), best);
            worstMatchups.put(fighterNames.get(i), worst);
        }

        List<ArchetypeRanking> rankings = new ArrayList<>();
        for (String name : fighterNames) {
            int[] s = stats.get(name);
            int totalFights = s[0] + s[1] + s[2];
            double winRate = totalFights > 0 ? (s[0] * 100.0) / totalFights : 0.0;
            String tier = assignTier(winRate);

            rankings.add(new ArchetypeRanking(
                name, s[0], s[1], s[2], winRate, tier,
                bestMatchups.get(name), worstMatchups.get(name)
            ));
        }

        rankings.sort(Comparator.comparingDouble(ArchetypeRanking::overallWinRate).reversed());
        return rankings;
    }

    public MatchupResult getSpecificMatchup(String fighter1, String fighter2, int fightsPerMatchup){
        Character c1 = resolveFighter(fighter1);
        Character c2 = resolveFighter(fighter2);

        MultiSimulationResult result = combatSimulator.simulateMultipleFights(c1, c2, fightsPerMatchup);

        double[] ci = confidenceService.calculateWinRateConfidenceInterval(
            result.fighter1Wins(), result.totalFights());

        return new MatchupResult(
            c1.getName(), c2.getName(), result,
            result.fighter1WinRate(), ci[1] - ci[0]
        );
    }

    private String assignTier(double winRate){
        if (winRate >= 70.0) return "S";
        if (winRate >= 55.0) return "A";
        if (winRate >= 45.0) return "B";
        if (winRate >= 30.0) return "C";
        return "D";
    }
}

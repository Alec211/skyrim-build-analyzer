package com.alecalbright.skyrimbuildanalyzer.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alecalbright.skyrimbuildanalyzer.archetype.CharacterArchetype;
import com.alecalbright.skyrimbuildanalyzer.model.ArchetypeRanking;
import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.MatchupResult;
import com.alecalbright.skyrimbuildanalyzer.model.Perk;
import com.alecalbright.skyrimbuildanalyzer.repository.WeaponRepository;
import com.alecalbright.skyrimbuildanalyzer.service.ArchetypeMatchupService;
import com.alecalbright.skyrimbuildanalyzer.service.ConfidenceAnalysisService;
import com.alecalbright.skyrimbuildanalyzer.service.DamageAnalysisService;
import com.alecalbright.skyrimbuildanalyzer.service.PerkAnalysisService;
import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final ArchetypeMatchupService matchupService;
    private final DamageAnalysisService damageService;
    private final PerkAnalysisService perkService;
    private final ConfidenceAnalysisService confidenceService;
    private final WeaponRepository weaponRepository;

    public AnalyticsController(ArchetypeMatchupService matchupService,
                               DamageAnalysisService damageService,
                               PerkAnalysisService perkService,
                               ConfidenceAnalysisService confidenceService,
                               WeaponRepository weaponRepository){
        this.matchupService = matchupService;
        this.damageService = damageService;
        this.perkService = perkService;
        this.confidenceService = confidenceService;
        this.weaponRepository = weaponRepository;
    }

    @GetMapping("/tournament")
    public String tournament(@RequestParam(defaultValue = "100") int fights){
        List<MatchupResult> matchups = matchupService.runFullTournament(fights);
        List<ArchetypeRanking> rankings = matchupService.getArchetypeRankings(fights);

        StringBuilder output = new StringBuilder();
        output.append("=== ARCHETYPE TOURNAMENT (" + fights + " fights per matchup) ===\n\n");

        // Rankings
        output.append("--- OVERALL RANKINGS ---\n\n");
        int rank = 1;
        for (ArchetypeRanking r : rankings) {
            output.append(String.format("#%d %s\n", rank++, r.toString()));
            output.append(String.format("    Best matchup: vs %s | Worst matchup: vs %s\n\n",
                r.bestMatchup(), r.worstMatchup()));
        }

        // Matchup matrix
        CharacterArchetype[] archetypes = CharacterArchetype.values();
        double[][] matrix = matchupService.getMatchupMatrix(fights);

        output.append("--- MATCHUP MATRIX (row vs column win %) ---\n\n");

        // Header row
        output.append(String.format("%-22s", ""));
        for (CharacterArchetype a : archetypes) {
            output.append(String.format("%-10s", abbreviate(a.getDisplayName())));
        }
        output.append("\n");

        for (int i = 0; i < archetypes.length; i++) {
            output.append(String.format("%-22s", archetypes[i].getDisplayName()));
            for (int j = 0; j < archetypes.length; j++) {
                if (i == j) {
                    output.append(String.format("%-10s", "---"));
                } else {
                    output.append(String.format("%-10s", String.format("%.0f%%", matrix[i][j])));
                }
            }
            output.append("\n");
        }

        // Individual matchup details
        output.append("\n--- ALL MATCHUPS ---\n\n");
        for (MatchupResult m : matchups) {
            MultiSimulationResult sim = m.simulationResult();
            output.append(String.format("%-25s vs %-25s | %3d - %3d",
                m.archetype1().getDisplayName(),
                m.archetype2().getDisplayName(),
                sim.fighter1Wins(), sim.fighter2Wins()));

            if (sim.draws() > 0) {
                output.append(String.format(" (%d draws)", sim.draws()));
            }

            boolean significant = confidenceService.isStatisticallySignificant(sim);
            output.append(significant ? " *" : "");
            output.append("\n");
        }

        output.append("\n* = statistically significant result (p < 0.05)\n");

        return output.toString().replace("\n", "<br>");
    }

    @GetMapping("/rankings")
    public String rankings(@RequestParam(defaultValue = "100") int fights){
        List<ArchetypeRanking> rankings = matchupService.getArchetypeRankings(fights);

        StringBuilder output = new StringBuilder();
        output.append("=== ARCHETYPE TIER LIST (" + fights + " fights per matchup) ===\n\n");

        String currentTier = "";
        for (ArchetypeRanking r : rankings) {
            if (!r.tier().equals(currentTier)) {
                currentTier = r.tier();
                output.append("--- ").append(currentTier).append(" TIER ---\n");
            }
            output.append(String.format("  %s — %.1f%% win rate (%d-%d-%d)\n",
                r.archetype().getDisplayName(), r.overallWinRate(),
                r.totalWins(), r.totalLosses(), r.totalDraws()));
            output.append(String.format("    Best vs: %s | Worst vs: %s\n",
                r.bestMatchup(), r.worstMatchup()));
        }

        output.append("\nTier criteria: S(70%+) A(55%+) B(45%+) C(30%+) D(<30%)\n");
        output.append("Recommended sample size for 5%% margin of error: ");
        output.append(confidenceService.recommendedSampleSize(5)).append(" fights\n");

        return output.toString().replace("\n", "<br>");
    }

    @GetMapping("/matchup")
    public String matchup(@RequestParam String a1, @RequestParam String a2,
                          @RequestParam(defaultValue = "500") int fights){
        CharacterArchetype arch1;
        CharacterArchetype arch2;

        try {
            arch1 = CharacterArchetype.valueOf(a1.toUpperCase());
            arch2 = CharacterArchetype.valueOf(a2.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Invalid archetype name. Valid options: STEALTH_ARCHER, TWO_HANDED_WARRIOR, " +
                   "DUAL_WIELDING_BERSERKER, ASSASSIN, PALADIN, BARBARIAN, RANGER, SPELLSWORD";
        }

        MatchupResult matchup = matchupService.getSpecificMatchup(arch1, arch2, fights);
        MultiSimulationResult sim = matchup.simulationResult();

        Character c1 = arch1.create(weaponRepository);
        Character c2 = arch2.create(weaponRepository);

        StringBuilder output = new StringBuilder();
        output.append("=== HEAD-TO-HEAD: " + arch1.getDisplayName() + " vs " + arch2.getDisplayName() + " ===\n\n");
        output.append("Fights simulated: ").append(fights).append("\n\n");

        // Fighter profiles
        output.append("--- " + arch1.getDisplayName() + " ---\n");
        output.append("  Weapon: ").append(c1.getWeapon().getName()).append("\n");
        output.append("  HP: ").append(c1.getMaxHealth()).append("\n");
        output.append("  Base Damage: ").append(String.format("%.1f", c1.calculateDamage())).append("\n");
        output.append("  Wins: ").append(sim.fighter1Wins()).append("\n");
        output.append("  Win Rate: ").append(String.format("%.1f%%", sim.fighter1WinRate())).append("\n");
        output.append("  Avg DPS: ").append(String.format("%.2f", damageService.calculateAverageDPS(sim, sim.fighter1Name()))).append("\n\n");

        output.append("--- " + arch2.getDisplayName() + " ---\n");
        output.append("  Weapon: ").append(c2.getWeapon().getName()).append("\n");
        output.append("  HP: ").append(c2.getMaxHealth()).append("\n");
        output.append("  Base Damage: ").append(String.format("%.1f", c2.calculateDamage())).append("\n");
        output.append("  Wins: ").append(sim.fighter2Wins()).append("\n");
        output.append("  Win Rate: ").append(String.format("%.1f%%", sim.fighter2WinRate())).append("\n");
        output.append("  Avg DPS: ").append(String.format("%.2f", damageService.calculateAverageDPS(sim, sim.fighter2Name()))).append("\n\n");

        // Stats
        output.append("--- FIGHT STATISTICS ---\n");
        output.append("  Draws: ").append(sim.draws()).append("\n");
        output.append("  Avg fight length: ").append(String.format("%.1f turns", sim.averageFightDuration())).append("\n");
        output.append("  Shortest fight: ").append(sim.shortestFightDuration()).append(" turns\n");
        output.append("  Longest fight: ").append(sim.longestFightDuration()).append(" turns\n");
        output.append("  Avg TTK: ").append(String.format("%.1f turns", damageService.calculateAverageTimeToKill(sim))).append("\n");

        double[] ci = confidenceService.calculateWinRateConfidenceInterval(sim.fighter1Wins(), sim.totalFights());
        output.append(String.format("\n  %s win rate 95%% CI: [%.1f%% - %.1f%%]\n",
            arch1.getDisplayName(), ci[0], ci[1]));
        output.append("  Statistically significant: ").append(
            confidenceService.isStatisticallySignificant(sim) ? "YES" : "NO").append("\n");

        return output.toString().replace("\n", "<br>");
    }

    @GetMapping("/damage")
    public String damage(@RequestParam(defaultValue = "STEALTH_ARCHER") String archetype){
        CharacterArchetype arch;
        try {
            arch = CharacterArchetype.valueOf(archetype.toUpperCase());
        } catch (IllegalArgumentException e) {
            return "Invalid archetype. Valid: STEALTH_ARCHER, TWO_HANDED_WARRIOR, " +
                   "DUAL_WIELDING_BERSERKER, ASSASSIN, PALADIN, BARBARIAN, RANGER, SPELLSWORD";
        }

        Character character = arch.create(weaponRepository);

        StringBuilder output = new StringBuilder();
        output.append("=== DAMAGE ANALYSIS: " + arch.getDisplayName() + " ===\n\n");

        output.append("Weapon: ").append(character.getWeapon().getName()).append("\n");
        output.append("Weapon Base Damage: ").append(String.format("%.1f", character.getWeapon().getBaseDamage())).append("\n");
        output.append("Weapon Speed: ").append(character.getWeapon().getAttackSpeed()).append("\n");
        output.append("Weapon DPS: ").append(String.format("%.1f", character.getWeapon().getDPS())).append("\n\n");

        output.append("Perk Multiplier: ").append(String.format("%.2fx", character.getPerkDamageMultiplier())).append("\n");
        output.append("Theoretical Damage Per Hit: ").append(String.format("%.1f", perkService.calculateTheoreticalDamage(arch))).append("\n\n");

        output.append("--- PERK BREAKDOWN ---\n");
        Map<Perk, Double> breakdown = perkService.getPerkBreakdown(arch);
        if (breakdown.isEmpty()) {
            output.append("  No damage-boosting perks\n");
        } else {
            for (Map.Entry<Perk, Double> entry : breakdown.entrySet()) {
                output.append(String.format("  %-30s %.1fx\n",
                    entry.getKey().getDisplayName(), entry.getValue()));
            }
        }

        Map<CharacterArchetype, String> warnings = perkService.identifyOverkillPerks();
        if (warnings.containsKey(arch)) {
            output.append("\nWARNING: ").append(warnings.get(arch)).append("\n");
        }

        return output.toString().replace("\n", "<br>");
    }

    @GetMapping("/perks")
    public String perks(){
        StringBuilder output = new StringBuilder();
        output.append("=== PERK EFFICIENCY ANALYSIS ===\n\n");

        output.append("--- DAMAGE PER PERK INVESTED (higher = more efficient) ---\n\n");
        List<Map.Entry<CharacterArchetype, Double>> efficiency = perkService.comparePerkEfficiency();
        int rank = 1;
        for (Map.Entry<CharacterArchetype, Double> entry : efficiency) {
            CharacterArchetype arch = entry.getKey();
            Character c = arch.create(weaponRepository);

            output.append(String.format("#%d %-25s | %.1f dmg/perk | %d perks | %.1fx multiplier | %.1f theoretical dmg\n",
                rank++,
                arch.getDisplayName(),
                entry.getValue(),
                arch.getPerks().length,
                c.getPerkDamageMultiplier(),
                perkService.calculateTheoreticalDamage(arch)));
        }

        // Warnings
        Map<CharacterArchetype, String> warnings = perkService.identifyOverkillPerks();
        if (!warnings.isEmpty()) {
            output.append("\n--- BALANCE WARNINGS ---\n\n");
            for (Map.Entry<CharacterArchetype, String> w : warnings.entrySet()) {
                output.append(w.getKey().getDisplayName()).append(": ").append(w.getValue()).append("\n");
            }
        }

        return output.toString().replace("\n", "<br>");
    }

    private String abbreviate(String name){
        if (name.length() <= 8) return name;

        String[] words = name.split("[\\s-]+");
        if (words.length == 1) return name.substring(0, 8);

        StringBuilder abbr = new StringBuilder();
        for (String word : words) {
            abbr.append(word.substring(0, Math.min(3, word.length())));
        }
        return abbr.toString();
    }
}

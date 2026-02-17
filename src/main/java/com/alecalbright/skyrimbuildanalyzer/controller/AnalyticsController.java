package com.alecalbright.skyrimbuildanalyzer.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alecalbright.skyrimbuildanalyzer.archetype.CharacterArchetype;
import com.alecalbright.skyrimbuildanalyzer.dto.DamageResponse;
import com.alecalbright.skyrimbuildanalyzer.dto.MatchupResponse;
import com.alecalbright.skyrimbuildanalyzer.dto.PerkEfficiencyResponse;
import com.alecalbright.skyrimbuildanalyzer.dto.TournamentResponse;
import com.alecalbright.skyrimbuildanalyzer.model.ArchetypeRanking;
import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.MatchupResult;
import com.alecalbright.skyrimbuildanalyzer.model.Perk;
import com.alecalbright.skyrimbuildanalyzer.repository.ArmorRepository;
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
    private final ArmorRepository armorRepository;

    public AnalyticsController(ArchetypeMatchupService matchupService,
                               DamageAnalysisService damageService,
                               PerkAnalysisService perkService,
                               ConfidenceAnalysisService confidenceService,
                               WeaponRepository weaponRepository,
                               ArmorRepository armorRepository){
        this.matchupService = matchupService;
        this.damageService = damageService;
        this.perkService = perkService;
        this.confidenceService = confidenceService;
        this.weaponRepository = weaponRepository;
        this.armorRepository = armorRepository;
    }

    @GetMapping("/tournament")
    public TournamentResponse tournament(@RequestParam(defaultValue = "100") int fights){
        List<MatchupResult> matchups = matchupService.runFullTournament(fights);
        List<ArchetypeRanking> rankings = matchupService.getArchetypeRankings(matchups);
        double[][] matrix = matchupService.getMatchupMatrix(matchups);

        List<String> archetypeNames = new ArrayList<>();
        for (CharacterArchetype a : CharacterArchetype.values()) {
            archetypeNames.add(a.getDisplayName());
        }

        List<TournamentResponse.MatchupSummary> matchupSummaries = new ArrayList<>();
        for (MatchupResult m : matchups) {
            MultiSimulationResult sim = m.simulationResult();
            matchupSummaries.add(new TournamentResponse.MatchupSummary(
                m.archetype1().getDisplayName(),
                m.archetype2().getDisplayName(),
                sim.fighter1Wins(),
                sim.fighter2Wins(),
                sim.draws(),
                m.archetype1WinRate(),
                confidenceService.isStatisticallySignificant(sim)
            ));
        }

        return new TournamentResponse(fights, rankings, matchupSummaries, matrix, archetypeNames);
    }

    @GetMapping("/rankings")
    public List<ArchetypeRanking> rankings(@RequestParam(defaultValue = "100") int fights){
        List<MatchupResult> matchups = matchupService.runFullTournament(fights);
        return matchupService.getArchetypeRankings(matchups);
    }

    @GetMapping("/matchup")
    public MatchupResponse matchup(@RequestParam String a1, @RequestParam String a2,
                                   @RequestParam(defaultValue = "500") int fights){
        CharacterArchetype arch1 = CharacterArchetype.valueOf(a1.toUpperCase());
        CharacterArchetype arch2 = CharacterArchetype.valueOf(a2.toUpperCase());

        MatchupResult matchup = matchupService.getSpecificMatchup(arch1, arch2, fights);
        MultiSimulationResult sim = matchup.simulationResult();

        Character c1 = arch1.create(weaponRepository, armorRepository);
        Character c2 = arch2.create(weaponRepository, armorRepository);

        MatchupResponse.FighterProfile fighter1 = new MatchupResponse.FighterProfile(
            arch1.getDisplayName(),
            c1.getWeapon().getName(),
            c1.getMaxHealth(),
            c1.calculateDamage(),
            sim.fighter1Wins(),
            sim.fighter1WinRate(),
            damageService.calculateAverageDPS(sim, sim.fighter1Name())
        );

        MatchupResponse.FighterProfile fighter2 = new MatchupResponse.FighterProfile(
            arch2.getDisplayName(),
            c2.getWeapon().getName(),
            c2.getMaxHealth(),
            c2.calculateDamage(),
            sim.fighter2Wins(),
            sim.fighter2WinRate(),
            damageService.calculateAverageDPS(sim, sim.fighter2Name())
        );

        double[] ci = confidenceService.calculateWinRateConfidenceInterval(sim.fighter1Wins(), sim.totalFights());

        MatchupResponse.FightStats stats = new MatchupResponse.FightStats(
            sim.draws(),
            sim.averageFightDuration(),
            sim.shortestFightDuration(),
            sim.longestFightDuration(),
            damageService.calculateAverageTimeToKill(sim),
            ci[0],
            ci[1],
            confidenceService.isStatisticallySignificant(sim)
        );

        return new MatchupResponse(fights, fighter1, fighter2, stats);
    }

    @GetMapping("/damage")
    public DamageResponse damage(@RequestParam(defaultValue = "STEALTH_ARCHER") String archetype){
        CharacterArchetype arch = CharacterArchetype.valueOf(archetype.toUpperCase());
        Character character = arch.create(weaponRepository, armorRepository);

        Map<String, Double> perkBreakdown = new LinkedHashMap<>();
        Map<Perk, Double> breakdown = perkService.getPerkBreakdown(arch);
        for (Map.Entry<Perk, Double> entry : breakdown.entrySet()) {
            perkBreakdown.put(entry.getKey().getDisplayName(), entry.getValue());
        }

        Map<CharacterArchetype, String> warnings = perkService.identifyOverkillPerks();
        String warning = warnings.getOrDefault(arch, null);

        return new DamageResponse(
            arch.getDisplayName(),
            character.getWeapon().getName(),
            character.getWeapon().getBaseDamage(),
            character.getWeapon().getAttackSpeed(),
            character.getWeapon().getDPS(),
            character.getPerkDamageMultiplier(),
            perkService.calculateTheoreticalDamage(arch),
            perkBreakdown,
            warning
        );
    }

    @GetMapping("/perks")
    public PerkEfficiencyResponse perks(){
        List<Map.Entry<CharacterArchetype, Double>> efficiency = perkService.comparePerkEfficiency();

        List<PerkEfficiencyResponse.ArchetypeEfficiency> rankings = new ArrayList<>();
        for (Map.Entry<CharacterArchetype, Double> entry : efficiency) {
            CharacterArchetype arch = entry.getKey();
            Character c = arch.create(weaponRepository, armorRepository);

            rankings.add(new PerkEfficiencyResponse.ArchetypeEfficiency(
                arch.getDisplayName(),
                entry.getValue(),
                arch.getPerks().length,
                c.getPerkDamageMultiplier(),
                perkService.calculateTheoreticalDamage(arch)
            ));
        }

        Map<String, String> balanceWarnings = new LinkedHashMap<>();
        Map<CharacterArchetype, String> warnings = perkService.identifyOverkillPerks();
        for (Map.Entry<CharacterArchetype, String> w : warnings.entrySet()) {
            balanceWarnings.put(w.getKey().getDisplayName(), w.getValue());
        }

        return new PerkEfficiencyResponse(rankings, balanceWarnings);
    }
}

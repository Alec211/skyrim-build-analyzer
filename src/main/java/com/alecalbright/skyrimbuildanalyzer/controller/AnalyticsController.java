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
import com.alecalbright.skyrimbuildanalyzer.dto.EncounterResponse;
import com.alecalbright.skyrimbuildanalyzer.dto.MatchupResponse;
import com.alecalbright.skyrimbuildanalyzer.dto.PerkEfficiencyResponse;
import com.alecalbright.skyrimbuildanalyzer.dto.TournamentResponse;
import com.alecalbright.skyrimbuildanalyzer.model.ArchetypeRanking;
import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.EnemyCategory;
import com.alecalbright.skyrimbuildanalyzer.model.EnemyDefinition;
import com.alecalbright.skyrimbuildanalyzer.model.MatchupResult;
import com.alecalbright.skyrimbuildanalyzer.model.Perk;
import com.alecalbright.skyrimbuildanalyzer.repository.ArmorRepository;
import com.alecalbright.skyrimbuildanalyzer.repository.EnemyRepository;
import com.alecalbright.skyrimbuildanalyzer.repository.WeaponRepository;
import com.alecalbright.skyrimbuildanalyzer.service.ArchetypeMatchupService;
import com.alecalbright.skyrimbuildanalyzer.service.ConfidenceAnalysisService;
import com.alecalbright.skyrimbuildanalyzer.service.DamageAnalysisService;
import com.alecalbright.skyrimbuildanalyzer.service.PerkAnalysisService;
import com.alecalbright.skyrimbuildanalyzer.simulation.EncounterResult;
import com.alecalbright.skyrimbuildanalyzer.simulation.EncounterSimulator;
import com.alecalbright.skyrimbuildanalyzer.simulation.FightResult;
import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final ArchetypeMatchupService matchupService;
    private final DamageAnalysisService damageService;
    private final PerkAnalysisService perkService;
    private final ConfidenceAnalysisService confidenceService;
    private final EncounterSimulator encounterSimulator;
    private final WeaponRepository weaponRepository;
    private final ArmorRepository armorRepository;
    private final EnemyRepository enemyRepository;

    public AnalyticsController(ArchetypeMatchupService matchupService,
                               DamageAnalysisService damageService,
                               PerkAnalysisService perkService,
                               ConfidenceAnalysisService confidenceService,
                               EncounterSimulator encounterSimulator,
                               WeaponRepository weaponRepository,
                               ArmorRepository armorRepository,
                               EnemyRepository enemyRepository){
        this.matchupService = matchupService;
        this.damageService = damageService;
        this.perkService = perkService;
        this.confidenceService = confidenceService;
        this.encounterSimulator = encounterSimulator;
        this.weaponRepository = weaponRepository;
        this.armorRepository = armorRepository;
        this.enemyRepository = enemyRepository;
    }

    @GetMapping("/tournament")
    public TournamentResponse tournament(@RequestParam(defaultValue = "100") int fights,
                                         @RequestParam(defaultValue = "false") boolean includeEnemies){
        List<String> fighterNames = matchupService.getAllFighterNames(includeEnemies);
        List<MatchupResult> matchups = matchupService.runFullTournament(fights, includeEnemies);
        List<ArchetypeRanking> rankings = matchupService.getArchetypeRankings(matchups, fighterNames);
        double[][] matrix = matchupService.getMatchupMatrix(matchups, fighterNames);

        List<TournamentResponse.MatchupSummary> matchupSummaries = new ArrayList<>();
        for (MatchupResult m : matchups) {
            MultiSimulationResult sim = m.simulationResult();
            matchupSummaries.add(new TournamentResponse.MatchupSummary(
                m.fighter1Name(),
                m.fighter2Name(),
                sim.fighter1Wins(),
                sim.fighter2Wins(),
                sim.draws(),
                m.fighter1WinRate(),
                confidenceService.isStatisticallySignificant(sim)
            ));
        }

        return new TournamentResponse(fights, rankings, matchupSummaries, matrix, fighterNames);
    }

    @GetMapping("/rankings")
    public List<ArchetypeRanking> rankings(@RequestParam(defaultValue = "100") int fights,
                                           @RequestParam(defaultValue = "false") boolean includeEnemies){
        List<String> fighterNames = matchupService.getAllFighterNames(includeEnemies);
        List<MatchupResult> matchups = matchupService.runFullTournament(fights, includeEnemies);
        return matchupService.getArchetypeRankings(matchups, fighterNames);
    }

    @GetMapping("/matchup")
    public MatchupResponse matchup(@RequestParam String a1, @RequestParam String a2,
                                   @RequestParam(defaultValue = "500") int fights){
        MatchupResult matchup = matchupService.getSpecificMatchup(a1, a2, fights);
        MultiSimulationResult sim = matchup.simulationResult();

        Character c1 = matchupService.resolveFighter(a1);
        Character c2 = matchupService.resolveFighter(a2);

        MatchupResponse.FighterProfile fighter1 = new MatchupResponse.FighterProfile(
            c1.getName(),
            c1.getWeapon().getName(),
            c1.getMaxHealth(),
            c1.calculateDamage(),
            sim.fighter1Wins(),
            sim.fighter1WinRate(),
            damageService.calculateAverageDPS(sim, sim.fighter1Name())
        );

        MatchupResponse.FighterProfile fighter2 = new MatchupResponse.FighterProfile(
            c2.getName(),
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

    @GetMapping("/enemies")
    public Map<String, Object> enemies(@RequestParam(required = false) String category){
        List<EnemyDefinition> enemies;
        if (category != null && !category.isBlank()) {
            EnemyCategory cat = EnemyCategory.valueOf(category.toUpperCase());
            enemies = enemyRepository.getEnemiesByCategory(cat);
        } else {
            enemies = enemyRepository.getAllEnemies();
        }

        List<Map<String, Object>> enemyList = new ArrayList<>();
        for (EnemyDefinition enemy : enemies) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", enemy.getName());
            entry.put("level", enemy.getLevel());
            entry.put("health", enemy.getHealth());
            entry.put("category", enemy.getCategory().getDisplayName());
            entry.put("weapon", enemy.getWeaponName());
            enemyList.add(entry);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("count", enemyList.size());
        response.put("enemies", enemyList);
        return response;
    }

    @GetMapping("/fighters")
    public Map<String, Object> fighters(@RequestParam(defaultValue = "false") boolean includeEnemies){
        List<String> archetypes = new ArrayList<>();
        for (CharacterArchetype a : CharacterArchetype.values()) {
            archetypes.add(a.getDisplayName());
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("archetypes", archetypes);

        if (includeEnemies) {
            response.put("enemies", enemyRepository.getEnemyNames());
        }

        return response;
    }

    @GetMapping("/encounter")
    public EncounterResponse encounter(@RequestParam String archetype,
                                       @RequestParam String enemies,
                                       @RequestParam(defaultValue = "100") int fights){
        CharacterArchetype arch = CharacterArchetype.valueOf(archetype.toUpperCase());

        String[] enemyNames = enemies.split(",");
        List<Character> enemyCharacters = new ArrayList<>();
        for (String name : enemyNames) {
            EnemyDefinition enemy = enemyRepository.getEnemyByName(name.trim());
            if (enemy == null) {
                throw new IllegalArgumentException("Unknown enemy: " + name.trim());
            }
            enemyCharacters.add(enemy.toCharacter(weaponRepository, armorRepository));
        }

        List<EncounterResult> results = new ArrayList<>();
        int survivals = 0;
        int totalDefeated = 0;
        double totalDmgDealt = 0;
        double totalDmgReceived = 0;

        for (int i = 0; i < fights; i++) {
            Character player = arch.create(weaponRepository, armorRepository);

            List<Character> freshEnemies = new ArrayList<>();
            for (String name : enemyNames) {
                EnemyDefinition def = enemyRepository.getEnemyByName(name.trim());
                freshEnemies.add(def.toCharacter(weaponRepository, armorRepository));
            }

            EncounterResult result = encounterSimulator.simulate(player, freshEnemies);
            results.add(result);

            if (result.playerSurvived()) survivals++;
            totalDefeated += result.enemiesDefeated();
            totalDmgDealt += result.totalDamageDealt();
            totalDmgReceived += result.totalDamageReceived();
        }

        double survivalRate = (survivals * 100.0) / fights;
        double avgDefeated = (double) totalDefeated / fights;
        double avgDmgDealt = totalDmgDealt / fights;
        double avgDmgReceived = totalDmgReceived / fights;

        List<EncounterResponse.EnemyFightSummary> perEnemy = new ArrayList<>();
        for (int e = 0; e < enemyNames.length; e++) {
            String eName = enemyNames[e].trim();
            int wins = 0;
            double dmgDealt = 0;
            double dmgReceived = 0;
            int fightCount = 0;

            for (EncounterResult r : results) {
                if (e < r.individualFights().size()) {
                    FightResult fight = r.individualFights().get(e);
                    fightCount++;
                    if (fight.didFighter1Win()) wins++;
                    dmgDealt += fight.getTotalDamageByFighter(fight.fighter1Name());
                    dmgReceived += fight.getTotalDamageByFighter(fight.fighter2Name());
                }
            }

            double winRate = fightCount > 0 ? (wins * 100.0) / fightCount : 0;
            perEnemy.add(new EncounterResponse.EnemyFightSummary(
                eName,
                winRate,
                fightCount > 0 ? dmgDealt / fightCount : 0,
                fightCount > 0 ? dmgReceived / fightCount : 0
            ));
        }

        return new EncounterResponse(
            arch.getDisplayName(), fights, survivalRate,
            avgDefeated, avgDmgDealt, avgDmgReceived, perEnemy
        );
    }
}

package com.alecalbright.skyrimbuildanalyzer.simulation;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alecalbright.skyrimbuildanalyzer.model.Character;

@Service
public class EncounterSimulator {

    private final CombatSimulator combatSimulator;

    public EncounterSimulator(CombatSimulator combatSimulator) {
        this.combatSimulator = combatSimulator;
    }

    public EncounterResult simulate(Character player, List<Character> enemies) {
        player.reset();

        List<FightResult> fights = new ArrayList<>();
        int enemiesDefeated = 0;
        double totalDamageDealt = 0;
        double totalDamageReceived = 0;
        String killedBy = null;

        for (Character enemy : enemies) {
            enemy.reset();

            FightResult result = combatSimulator.simulateFight(player, enemy);
            fights.add(result);

            totalDamageDealt += result.getTotalDamageByFighter(player.getName());
            totalDamageReceived += result.getTotalDamageByFighter(enemy.getName());

            if (result.didFighter1Win()) {
                enemiesDefeated++;
            } else {
                killedBy = enemy.getName();
                break;
            }
        }

        boolean survived = player.isAlive();
        double remainingHP = Math.max(player.getHealth(), 0);

        return new EncounterResult(
            player.getName(),
            enemiesDefeated,
            enemies.size(),
            survived,
            remainingHP,
            totalDamageDealt,
            totalDamageReceived,
            fights,
            killedBy
        );
    }

    public List<EncounterResult> simulateMultiple(Character player, List<Character> enemies, int runs) {
        List<EncounterResult> results = new ArrayList<>();
        for (int i = 0; i < runs; i++) {
            results.add(simulate(player, enemies));
        }
        return results;
    }
}

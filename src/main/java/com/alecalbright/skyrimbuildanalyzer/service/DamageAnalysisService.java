package com.alecalbright.skyrimbuildanalyzer.service;

import org.springframework.stereotype.Service;

import com.alecalbright.skyrimbuildanalyzer.simulation.FightResult;
import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

@Service
public class DamageAnalysisService {

    private double calculateDPS(FightResult fight, String fighterName){
        if (fight.totalTurns() == 0) return 0.0;

        double totalDamage = fight.getTotalDamageByFighter(fighterName);
        return totalDamage / fight.totalTurns();
    }

    private int calculateTimeToKill(FightResult fight){
        if (fight.wasDraw()) return -1;
        return fight.totalTurns();
    }

    public double calculateAverageDPS(MultiSimulationResult results, String fighterName){
        double totalDPS = 0;
        int validFights = 0;

        for (FightResult fight : results.allFights()) {
            if (fight.totalTurns() > 0) {
                totalDPS += calculateDPS(fight, fighterName);
                validFights++;
            }
        }

        return validFights > 0 ? totalDPS / validFights : 0.0;
    }

    public double calculateAverageTimeToKill(MultiSimulationResult results){
        int totalTTK = 0;
        int decisiveFights = 0;

        for (FightResult fight : results.allFights()) {
            int ttk = calculateTimeToKill(fight);
            if (ttk > 0) {
                totalTTK += ttk;
                decisiveFights++;
            }
        }

        return decisiveFights > 0 ? (double) totalTTK / decisiveFights : 0.0;
    }
}

package com.alecalbright.skyrimbuildanalyzer.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.alecalbright.skyrimbuildanalyzer.simulation.FightResult;
import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

@Service
public class DamageAnalysisService {

    public double calculateDPS(FightResult fight, String fighterName){
        if (fight.totalTurns() == 0) return 0.0;

        double totalDamage = fight.getTotalDamageByFighter(fighterName);
        return totalDamage / fight.totalTurns();
    }

    public int calculateTimeToKill(FightResult fight){
        if (fight.wasDraw()) return -1;
        return fight.totalTurns();
    }

    public Map<String, double[]> getDamageBreakdown(MultiSimulationResult results){
        Map<String, double[]> breakdown = new HashMap<>();

        String f1 = results.fighter1Name();
        String f2 = results.fighter2Name();

        double f1TotalDmg = 0, f1MinDmg = Double.MAX_VALUE, f1MaxDmg = 0;
        double f2TotalDmg = 0, f2MinDmg = Double.MAX_VALUE, f2MaxDmg = 0;

        for (FightResult fight : results.allFights()) {
            double dmg1 = fight.getTotalDamageByFighter(f1);
            f1TotalDmg += dmg1;
            f1MinDmg = Math.min(f1MinDmg, dmg1);
            f1MaxDmg = Math.max(f1MaxDmg, dmg1);

            double dmg2 = fight.getTotalDamageByFighter(f2);
            f2TotalDmg += dmg2;
            f2MinDmg = Math.min(f2MinDmg, dmg2);
            f2MaxDmg = Math.max(f2MaxDmg, dmg2);
        }

        int numFights = results.totalFights();
        // [avg, min, max]
        breakdown.put(f1, new double[]{f1TotalDmg / numFights, f1MinDmg, f1MaxDmg});
        breakdown.put(f2, new double[]{f2TotalDmg / numFights, f2MinDmg, f2MaxDmg});

        return breakdown;
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

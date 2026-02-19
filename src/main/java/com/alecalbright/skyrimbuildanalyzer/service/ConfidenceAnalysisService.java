package com.alecalbright.skyrimbuildanalyzer.service;

import org.springframework.stereotype.Service;

import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

@Service
public class ConfidenceAnalysisService {

    private static final double Z_95 = 1.96; // 95% confidence

    // Wilson score interval — better than normal approximation at small sample sizes
    public double[] calculateWinRateConfidenceInterval(int wins, int totalFights){
        if (totalFights == 0) return new double[]{0.0, 0.0};

        double p = (double) wins / totalFights;
        double z2 = Z_95 * Z_95;
        double n = totalFights;

        double denominator = 1 + z2 / n;
        double center = (p + z2 / (2 * n)) / denominator;
        double margin = (Z_95 * Math.sqrt((p * (1 - p) + z2 / (4 * n)) / n)) / denominator;

        double lower = Math.max(0.0, center - margin) * 100.0;
        double upper = Math.min(1.0, center + margin) * 100.0;

        return new double[]{lower, upper};
    }

    // Two-tailed z-test: is the win rate significantly different from 50%?
    public boolean isStatisticallySignificant(MultiSimulationResult results){
        int n = results.totalFights();
        if (n < 10) return false;

        double observed = (double) results.fighter1Wins() / n;
        double standardError = Math.sqrt(0.25 / n); // 0.5 * 0.5 = 0.25
        double zScore = Math.abs(observed - 0.5) / standardError;

        return zScore > Z_95;
    }

}

package com.alecalbright.skyrimbuildanalyzer.service;

import org.springframework.stereotype.Service;

import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

@Service
public class ConfidenceAnalysisService {

    // Z-score for 95% confidence level
    private static final double Z_95 = 1.96;

    /**
     * Wilson score interval for win rate confidence.
     * More accurate than normal approximation for small samples or extreme proportions.
     */
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

    /**
     * Tests if the win rate is significantly different from 50% (fair matchup).
     * Uses a two-tailed binomial proportion test.
     */
    public boolean isStatisticallySignificant(MultiSimulationResult results){
        int n = results.totalFights();
        if (n < 10) return false;

        double observed = (double) results.fighter1Wins() / n;
        double expected = 0.5;

        // Z-test for proportion
        double standardError = Math.sqrt(expected * (1 - expected) / n);
        double zScore = Math.abs(observed - expected) / standardError;

        // Significant at 95% confidence if |z| > 1.96
        return zScore > Z_95;
    }

    /**
     * Calculates the minimum number of fights needed to detect a given win rate
     * difference from 50% with 95% confidence and 80% power.
     */
    public int recommendedSampleSize(double desiredMarginOfError){
        if (desiredMarginOfError <= 0 || desiredMarginOfError >= 50) {
            throw new IllegalArgumentException("Margin of error must be between 0 and 50 percent");
        }

        double marginDecimal = desiredMarginOfError / 100.0;
        // n = (z^2 * p * (1-p)) / E^2, using p=0.5 for maximum sample size
        double n = (Z_95 * Z_95 * 0.25) / (marginDecimal * marginDecimal);

        return (int) Math.ceil(n);
    }
}

package com.alecalbright.skyrimbuildanalyzer.simulation;

import java.util.List;

public record MultiSimulationResult(
    String fighter1Name,
    String fighter2Name,
    int totalFights,
    int fighter1Wins,
    int fighter2Wins,
    int draws,
    List<FightResult> allFights
) {
    
    public MultiSimulationResult {
        if(fighter1Name == null || fighter1Name.isBlank()){
            throw new IllegalArgumentException("Fighter 1 name cannot be null or blank");
        }
        if(fighter2Name == null || fighter2Name.isBlank()){
            throw new IllegalArgumentException("Fighter 2 name cannot be null or blank");
        }
        
        if(totalFights < 0){
            throw new IllegalArgumentException("Total fights cannot be negative");
        }
        if(fighter1Wins < 0){
            throw new IllegalArgumentException("Fighter 1 wins cannot be negative");
        }
        if(fighter2Wins < 0){
            throw new IllegalArgumentException("Fighter 2 wins cannot be negative");
        }
        if(draws < 0){
            throw new IllegalArgumentException("Draws cannot be negative");
        }
        
        if((fighter1Wins + fighter2Wins + draws) != totalFights){
            throw new IllegalArgumentException(
                String.format(
                    "Data inconsistency: fighter1Wins(%d) + fighter2Wins(%d) + draws(%d) = %d, but totalFights = %d",
                    fighter1Wins, fighter2Wins, draws,
                    (fighter1Wins + fighter2Wins + draws), totalFights
                )
            );
        }
        
        if(allFights == null){
            throw new IllegalArgumentException("All fights list cannot be null");
        }
        if(allFights.size() != totalFights){
            throw new IllegalArgumentException(
                String.format(
                    "Fights list size (%d) doesn't match totalFights (%d)",
                    allFights.size(), totalFights
                )
            );
        }
        
        allFights = List.copyOf(allFights);
    }
    
    public double fighter1WinRate(){
        if(totalFights == 0){
            return 0.0;
        }
        return (fighter1Wins * 100.0) / totalFights;
    }
    
    public double fighter2WinRate(){
        if(totalFights == 0){
            return 0.0;
        }
        return (fighter2Wins * 100.0) / totalFights;
    }
    
    public double drawRate(){
        if(totalFights == 0){
            return 0.0;
        }
        return (draws * 100.0) / totalFights;
    }
    
    public String overallWinner(){
        if(fighter1Wins > fighter2Wins){
            return fighter1Name;
        } 
        else if(fighter2Wins > fighter1Wins){
            return fighter2Name;
        } 
        else{
            return "Tie";
        }
    }
    
    public int winMargin(){
        return Math.abs(fighter1Wins - fighter2Wins);
    }
    
    public boolean isSignificant(){
        return winMargin() > (totalFights * 0.1);
    }

    public boolean isDataConsistent(){
        return (fighter1Wins + fighter2Wins + draws) == totalFights;
    }
    
    public double averageFightDuration(){
        if (allFights.isEmpty()) {
            return 0.0;
        }
        
        int totalTurns = allFights.stream()
            .mapToInt(FightResult::totalTurns)
            .sum();
        
        return (double) totalTurns / allFights.size();
    }

    public int shortestFightDuration(){
        return allFights.stream()
            .mapToInt(FightResult::totalTurns)
            .min()
            .orElse(0);
    }
    
    public int longestFightDuration(){
        return allFights.stream()
            .mapToInt(FightResult::totalTurns)
            .max()
            .orElse(0);
    }
    
    @Override
    public String toString(){
        return String.format(
            "%s vs %s: %d fights | %s wins %.1f%% | %s wins %.1f%% | Draws: %.1f%%",
            fighter1Name,
            fighter2Name,
            totalFights,
            fighter1Name,
            fighter1WinRate(),
            fighter2Name,
            fighter2WinRate(),
            drawRate()
        );
    }
}

package com.alecalbright.skyrimbuildanalyzer.simulation;

import java.util.List;

public class MultiSimulationResult {
    private final String fighter1Name;
    private final String fighter2Name;
    private final int totalFights;
    private final int fighter1Wins;
    private final int fighter2Wins;
    private final int draws;
    private final List<FightResult> allFights;

    public MultiSimulationResult(String fighter1Name, String fighter2Name, int totalFights, int fighter1Wins, int fighter2Wins, int draws, List<FightResult> allFights){
        this.fighter1Name = fighter1Name;
        this.fighter2Name = fighter2Name;
        this.totalFights = totalFights;
        this.fighter1Wins = fighter1Wins;
        this.fighter2Wins = fighter2Wins;
        this.draws = draws;
        this.allFights = List.copyOf(allFights);
    }

    // GETTERS

    public String getFighter1Name(){
        return fighter1Name;
    }
    
    public String getFighter2Name(){
        return fighter2Name;
    }
    
    public int getTotalFights(){
        return totalFights;
    }
    
    public int getFighter1Wins(){
        return fighter1Wins;
    }
    
    public int getFighter2Wins(){
        return fighter2Wins;
    }

    public int getDraws(){
        return draws;
    }
    
    public List<FightResult> getAllFights(){
        return allFights;
    }

    public double getFighter1WinRate(){
        if(totalFights == 0){
            return 0.0;
        }

        return (fighter1Wins * 100.0) / totalFights;
    }

    public double getFighter2WinRate(){
        if(totalFights == 0){
            return 0.0;
        }

        return (fighter2Wins * 100.0) / totalFights;
    }

    public double getDrawRate(){
        if(totalFights == 0){
            return 0.0;
        }
        
        return (draws * 100.0) / totalFights;
    }

    public String getOverallWinner(){
        if(fighter1Wins > fighter2Wins){
            return fighter1Name;
        }
        else if(fighter2Wins > fighter1Wins){
            return fighter2Name;
        } 
        else{
            return "Draw";
        }
    }

    public int getWinMargin(){
        return Math.abs(fighter1Wins - fighter2Wins);
    }

    public boolean isSignificant(){
        return getWinMargin() > (totalFights * 0.1);
    }

    public boolean isDataConsistent(){
        return (fighter1Wins + fighter2Wins + draws) == totalFights;
    }

    @Override
    public String toString(){
        return String.format("%s vs %s: %d fights | %s wins %.1f%% | %s wins %.1f%%",
            fighter1Name, fighter2Name, totalFights,
            fighter1Name, getFighter1WinRate(),
            fighter2Name, getFighter2WinRate(),
            getDrawRate());
    }
}

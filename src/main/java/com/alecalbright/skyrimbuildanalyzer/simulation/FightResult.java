package com.alecalbright.skyrimbuildanalyzer.simulation;

import java.util.List;

import com.alecalbright.skyrimbuildanalyzer.model.CombatEvent;

public class FightResult {
    private final String fighter1Name;
    private final String fighter2Name;
    private final String winnerName;
    private final int totalTurns;
    private final List<CombatEvent> combatEvents;

    public FightResult(String fighter1Name, String fighter2Name, String winnerName, int totalTurns, List<CombatEvent> combatEvents){
        this.fighter1Name = fighter1Name;
        this.fighter2Name = fighter2Name;
        this.winnerName = winnerName;
        this.totalTurns = totalTurns;
        this.combatEvents = List.copyOf(combatEvents);
    }

    // GETTERS

    public String getFighter1Name(){
        return fighter1Name;
    }
    
    public String getFighter2Name(){
        return fighter2Name;
    }
    
    public String getWinnerName(){
        return winnerName;
    }
    
    public int getTotalTurns(){
        return totalTurns;
    }
    
    public List<CombatEvent> getCombatEvents(){
        return combatEvents;
    }

    public boolean didFighter1Win(){
        return winnerName.equals(fighter1Name);
    }
    
    public boolean didFighter2Win(){
        return winnerName.equals(fighter2Name);
    }

    public boolean wasDraw(){
        return !didFighter1Win() && !didFighter2Win();
    }
    
    public double getTotalDamageByFighter(String fighterName){
        return combatEvents.stream().filter(event -> event.attackerName().equals(fighterName)).mapToDouble(CombatEvent::damageDealt).sum();
    }
    
    public double getAverageDamageByFighter(String fighterName){
        long attackCount = combatEvents.stream().filter(event -> event.attackerName().equals(fighterName)).count();
        
        if (attackCount == 0) return 0.0;
        
        double totalDamage = getTotalDamageByFighter(fighterName);
        return totalDamage / attackCount;
    }

    @Override
    public String toString() {
        return String.format("Fight: %s vs %s | Winner: %s | Turns: %d | Events: %d",
            fighter1Name, fighter2Name, winnerName, totalTurns, combatEvents.size());
    }
}

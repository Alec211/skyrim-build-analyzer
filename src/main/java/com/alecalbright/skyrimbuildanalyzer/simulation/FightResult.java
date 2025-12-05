package com.alecalbright.skyrimbuildanalyzer.simulation;

import java.util.List;

import com.alecalbright.skyrimbuildanalyzer.model.CombatEvent;

public record FightResult(
    String fighter1Name,
    String fighter2Name,
    String winnerName,
    int totalTurns,
    List<CombatEvent> combatEvents
) {

    public FightResult {
        if(fighter1Name == null || fighter1Name.isBlank()){
            throw new IllegalArgumentException("Fighter 1 name cannot be null or blank");
        }
        if(fighter2Name == null || fighter2Name.isBlank()){
            throw new IllegalArgumentException("Fighter 2 name cannot be null or blank");
        }
        if(winnerName == null || winnerName.isBlank()){
            throw new IllegalArgumentException("Winner name cannot be null or blank");
        }
        if(totalTurns < 0){
            throw new IllegalArgumentException("Total turns cannot be negative");
        }
        if(combatEvents == null){
            throw new IllegalArgumentException("Combat events list cannot be null");
        }
        
        combatEvents = List.copyOf(combatEvents);
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
        if (fighterName == null || fighterName.isBlank()) {
            throw new IllegalArgumentException("Fighter name cannot be null or blank");
        }
        
        return combatEvents.stream()
            .filter(event -> event.attackerName().equals(fighterName))
            .mapToDouble(CombatEvent::damageDealt)
            .sum();
    }
    
    public double getAverageDamageByFighter(String fighterName){
        if (fighterName == null || fighterName.isBlank()) {
            throw new IllegalArgumentException("Fighter name cannot be null or blank");
        }
        
        long attackCount = combatEvents.stream()
            .filter(event -> event.attackerName().equals(fighterName))
            .count();
        
        if (attackCount == 0) {
            return 0.0;
        }
        
        double totalDamage = getTotalDamageByFighter(fighterName);
        return totalDamage / attackCount;
    }
    
    public long getAttackCountByFighter(String fighterName){
        if(fighterName == null || fighterName.isBlank()){
            throw new IllegalArgumentException("Fighter name cannot be null or blank");
        }
        
        return combatEvents.stream()
            .filter(event -> event.attackerName().equals(fighterName))
            .count();
    }
    
    @Override
    public String toString(){
        return String.format(
            "Fight: %s vs %s | Winner: %s | Turns: %d | Events: %d",
            fighter1Name,
            fighter2Name,
            winnerName,
            totalTurns,
            combatEvents.size()
        );
    }
}
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
        return combatEvents.stream()
            .filter(event -> event.attackerName().equals(fighterName))
            .mapToDouble(CombatEvent::damageDealt)
            .sum();
    }

    public double getAverageDamageByFighter(String fighterName){
        long attackCount = combatEvents.stream()
            .filter(event -> event.attackerName().equals(fighterName))
            .count();

        if(attackCount == 0) return 0.0;

        return getTotalDamageByFighter(fighterName) / attackCount;
    }

    public long getAttackCountByFighter(String fighterName){
        return combatEvents.stream()
            .filter(event -> event.attackerName().equals(fighterName))
            .count();
    }

    @Override
    public String toString(){
        return String.format(
            "Fight: %s vs %s | Winner: %s | Turns: %d | Events: %d",
            fighter1Name, fighter2Name, winnerName, totalTurns, combatEvents.size()
        );
    }
}

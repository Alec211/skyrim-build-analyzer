package com.alecalbright.skyrimbuildanalyzer.simulation;

import java.util.ArrayList;
import java.util.List;

import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.CombatEvent;

public class CombatSimulator {
    private static final int MAX_TURNS = 1000;

    public FightResult simulateFight(Character character1, Character character2){
        character1.reset();
        character2.reset();

        List<CombatEvent> combatEvents = new ArrayList<>();
        int turnCounter = 0;

        while(character1.isAlive() && character2.isAlive() && turnCounter < MAX_TURNS){
            turnCounter++;

            // Character 1 as attacker
            double damage1 = character1.calculateDamage();
            character2.takeDamage(damage1);

            CombatEvent event1 = CombatEvent.now(character1.getName(), character2.getName(), damage1, character1.getWeapon().getName(), false, false);
            combatEvents.add(event1);

            if(!character2.isAlive()){
                break;
            }

            double damage2 = character2.calculateDamage();
            character1.takeDamage(damage2);

            CombatEvent event2 = CombatEvent.now(character2.getName(), character1.getName(), damage2, character2.getWeapon().getName(), false, false);
            combatEvents.add(event2);

            if(!character1.isAlive()){
                break;
            }
        }

        String winner;

        if(turnCounter >= MAX_TURNS){
            winner = "Draw (Timeout, Max Turns Exceeded)";
        }
        else if(character1.isAlive()){
            winner = character1.getName();
        }
        else if(character2.isAlive()){
            winner = character2.getName();
        }
        else{
            winner = "Draw (Both Characters Died)";
        }

        return new FightResult(character1.getName(), character2.getName(), winner, turnCounter, combatEvents);
    }

    public MultiSimulationResult simulateMultipleFights(Character character1, Character character2, int numFights){
        if (numFights < 1){
        throw new IllegalArgumentException("Number of fights must be at least 1");
        }
        if (numFights > 10000){
            throw new IllegalArgumentException("Number of fights cannot exceed 10,000 (performance limit)");
        }

        // Storing all fights in an ArrayList 
        List<FightResult> allFights = new ArrayList<>();
        
        int fighter1Wins = 0;
        int fighter2Wins = 0;
        int matchDraws = 0;

        for (int i = 0; i < numFights; i++) {
            FightResult result = simulateFight(character1, character2);
            allFights.add(result);
            
            if (result.didFighter1Win()){
                fighter1Wins++;
            }
            else if (result.didFighter2Win()){
                fighter2Wins++;
            }
            else{
                matchDraws++;
            }
        }
        
        return new MultiSimulationResult(
            character1.getName(),
            character2.getName(),
            numFights,
            fighter1Wins,
            fighter2Wins,
            matchDraws,
            allFights
        );
        }
}

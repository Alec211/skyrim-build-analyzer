package com.alecalbright.skyrimbuildanalyzer.simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.CombatEvent;

@Service
public class CombatSimulator {
    private static final int MAX_TURNS = 1000;
    private static final double DAMAGE_VARIANCE = 0.10;

    private final Random random = new Random();

    public FightResult simulateFight(Character character1, Character character2){
        character1.reset();
        character2.reset();

        List<CombatEvent> combatEvents = new ArrayList<>();
        int turnCounter = 0;

        while(character1.isAlive() && character2.isAlive() && turnCounter < MAX_TURNS){
            turnCounter++;

            // Character 1 attacks Character 2
            boolean sneak1 = isSneak(character1, turnCounter);
            boolean crit1 = isCriticalHit(character1);
            double rawDamage1 = character1.calculateDamage(sneak1, crit1) * applyVariance();
            double reducedDamage1 = character2.applyDamageReduction(rawDamage1);
            double blocked1 = rawDamage1 - reducedDamage1;

            character2.takeDamage(reducedDamage1);
            combatEvents.add(CombatEvent.now(
                character1.getName(), character2.getName(), reducedDamage1, blocked1,
                character1.getWeapon().getName(), crit1, sneak1
            ));

            if(!character2.isAlive()) break;

            // Character 2 attacks Character 1
            boolean sneak2 = isSneak(character2, turnCounter);
            boolean crit2 = isCriticalHit(character2);
            double rawDamage2 = character2.calculateDamage(sneak2, crit2) * applyVariance();
            double reducedDamage2 = character1.applyDamageReduction(rawDamage2);
            double blocked2 = rawDamage2 - reducedDamage2;

            character1.takeDamage(reducedDamage2);
            combatEvents.add(CombatEvent.now(
                character2.getName(), character1.getName(), reducedDamage2, blocked2,
                character2.getWeapon().getName(), crit2, sneak2
            ));

            if(!character1.isAlive()) break;
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

    private boolean isSneak(Character attacker, int turn){
        return turn == 1 && attacker.canSneakAttack();
    }

    private boolean isCriticalHit(Character attacker){
        return random.nextDouble() < attacker.getCriticalChance();
    }

    private double applyVariance(){
        return 1.0 - DAMAGE_VARIANCE + (random.nextDouble() * 2 * DAMAGE_VARIANCE);
    }

    public MultiSimulationResult simulateMultipleFights(Character character1, Character character2, int numFights){
        if (numFights < 1){
            throw new IllegalArgumentException("Number of fights must be at least 1");
        }
        if (numFights > 10000){
            throw new IllegalArgumentException("Number of fights cannot exceed 10,000 (performance limit)");
        }

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

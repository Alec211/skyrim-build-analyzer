package com.alecalbright.skyrimbuildanalyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.CombatEvent;
import com.alecalbright.skyrimbuildanalyzer.model.Weapon;
import com.alecalbright.skyrimbuildanalyzer.model.WeaponType;
import com.alecalbright.skyrimbuildanalyzer.simulation.CombatSimulator;
import com.alecalbright.skyrimbuildanalyzer.simulation.FightResult;
import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

@RestController
public class CombatController {

    private final CombatSimulator combatSimulator;

    public CombatController(CombatSimulator combatSimulator){
        this.combatSimulator = combatSimulator;
    }

    @GetMapping("/test-combat-event")
    public String testCombatEvent(){
        StringBuilder result = new StringBuilder();

        CombatEvent normalAttack = CombatEvent.now(
            "Stealth Archer", "Two-Handed Warrior", 45.5, "Ebony Bow", false, false
        );
        CombatEvent criticalHit = CombatEvent.now(
            "Two-Handed Warrior", "Stealth Archer", 89.0, "Dragonbone Greatsword", true, false
        );
        CombatEvent sneakAttack = CombatEvent.now(
            "Stealth Archer", "Two-Handed Warrior", 285.0, "Ebony Bow", false, true
        );
        CombatEvent criticalSneakAttack = CombatEvent.now(
            "Assassin", "Unsuspecting Guard", 1650.0, "Ebony Dagger", true, true
        );

        result.append("=== COMBAT LOG ===\n\n");

        result.append("Event 1: ").append(normalAttack).append("\n");
        result.append("  Attacker: ").append(normalAttack.attackerName()).append("\n");
        result.append("  Damage: ").append(normalAttack.damageDealt()).append("\n");
        result.append("  Special? ").append(normalAttack.isSpecialAttack()).append("\n\n");

        result.append("Event 2: ").append(criticalHit).append("\n");
        result.append("  Special? ").append(criticalHit.isSpecialAttack()).append("\n\n");

        result.append("Event 3: ").append(sneakAttack).append("\n");
        result.append("  Special? ").append(sneakAttack.isSpecialAttack()).append("\n\n");

        result.append("Event 4: ").append(criticalSneakAttack).append("\n");
        result.append("  Special? ").append(criticalSneakAttack.isSpecialAttack()).append("\n");

        return result.toString().replace("\n", "<br>");
    }

    @GetMapping("/test-combat")
    public String testCombat(){
        Weapon bow = new Weapon("Ebony Bow", 19.0, 1.0, WeaponType.BOW);
        Weapon greatsword = new Weapon("Dragonbone Greatsword", 25.0, 0.7, WeaponType.TWO_HANDED_GREATSWORD);

        Character stealthArcher = new Character("Stealth Archer", 200.0, 250.0, 100.0, bow);
        Character warrior = new Character("Two-Handed Warrior", 400.0, 250.0, 100.0, greatsword);

        FightResult fightResult = combatSimulator.simulateFight(stealthArcher, warrior);

        StringBuilder result = new StringBuilder();
        result.append("=== SINGLE COMBAT RESULT ===\n\n");

        result.append("Fighter 1: ").append(fightResult.fighter1Name()).append("\n");
        result.append("  Weapon: ").append(bow.getName()).append(" (").append(bow.getBaseDamage()).append(" damage)\n");
        result.append("  Starting Health: 200\n");
        result.append("  Remaining Health: ").append(stealthArcher.getHealth()).append("\n");
        result.append("  Total Damage Dealt: ").append(String.format("%.1f", fightResult.getTotalDamageByFighter(fightResult.fighter1Name()))).append("\n");
        result.append("  Average Damage per Hit: ").append(String.format("%.1f", fightResult.getAverageDamageByFighter(fightResult.fighter1Name()))).append("\n\n");

        result.append("Fighter 2: ").append(fightResult.fighter2Name()).append("\n");
        result.append("  Weapon: ").append(greatsword.getName()).append(" (").append(greatsword.getBaseDamage()).append(" damage)\n");
        result.append("  Starting Health: 400\n");
        result.append("  Remaining Health: ").append(warrior.getHealth()).append("\n");
        result.append("  Total Damage Dealt: ").append(String.format("%.1f", fightResult.getTotalDamageByFighter(fightResult.fighter2Name()))).append("\n");
        result.append("  Average Damage per Hit: ").append(String.format("%.1f", fightResult.getAverageDamageByFighter(fightResult.fighter2Name()))).append("\n\n");

        result.append("WINNER: ").append(fightResult.winnerName()).append("!\n");
        result.append("Fight Duration: ").append(fightResult.totalTurns()).append(" turns\n");
        result.append("Total Combat Events: ").append(fightResult.combatEvents().size()).append("\n\n");

        result.append("=== COMBAT LOG (First 5 Events) ===\n\n");
        int eventsToShow = Math.min(5, fightResult.combatEvents().size());
        for (int i = 0; i < eventsToShow; i++) {
            result.append("Event ").append(i + 1).append(": ").append(fightResult.combatEvents().get(i)).append("\n");
        }

        return result.toString().replace("\n", "<br>");
    }

    @GetMapping("/test-multi-combat")
    public String testMultiCombat(){
        Weapon bow = new Weapon("Ebony Bow", 19.0, 1.0, WeaponType.BOW);
        Weapon greatsword = new Weapon("Dragonbone Greatsword", 25.0, 0.7, WeaponType.TWO_HANDED_GREATSWORD);

        Character stealthArcher = new Character("Stealth Archer", 200.0, 250.0, 100.0, bow);
        Character warrior = new Character("Two-Handed Warrior", 400.0, 250.0, 100.0, greatsword);

        MultiSimulationResult results = combatSimulator.simulateMultipleFights(stealthArcher, warrior, 100);

        StringBuilder output = new StringBuilder();
        output.append("=== COMBAT SIMULATION: 100 FIGHTS ===\n\n");

        output.append("Fighter 1: ").append(results.fighter1Name()).append("\n");
        output.append("  Wins: ").append(results.fighter1Wins()).append("\n");
        output.append("  Win Rate: ").append(String.format("%.1f", results.fighter1WinRate())).append("%\n\n");

        output.append("Fighter 2: ").append(results.fighter2Name()).append("\n");
        output.append("  Wins: ").append(results.fighter2Wins()).append("\n");
        output.append("  Win Rate: ").append(String.format("%.1f", results.fighter2WinRate())).append("%\n\n");

        output.append("Draws: ").append(results.draws()).append("\n");
        output.append("Draw Rate: ").append(String.format("%.1f", results.drawRate())).append("%\n\n");

        output.append("Overall Winner: ").append(results.overallWinner()).append("\n");
        output.append("Win Margin: ").append(results.winMargin()).append(" fights\n");
        output.append("Statistically Significant? ").append(results.isSignificant() ? "YES" : "NO").append("\n\n");

        output.append("Average Fight Duration: ").append(String.format("%.1f", results.averageFightDuration())).append(" turns\n");
        output.append("Shortest Fight: ").append(results.shortestFightDuration()).append(" turns\n");
        output.append("Longest Fight: ").append(results.longestFightDuration()).append(" turns\n\n");

        output.append("=== SAMPLE FIGHTS ===\n\n");
        for (int i = 0; i < Math.min(5, results.allFights().size()); i++) {
            FightResult fight = results.allFights().get(i);
            output.append("Fight ").append(i + 1).append(": ").append(fight.toString()).append("\n");
        }

        return output.toString().replace("\n", "<br>");
    }

    @GetMapping("/test-draw-scenario")
    public String testDrawScenario(){
        Weapon noDamageWeapon1 = new Weapon("Wooden Stick", 0.0, 1.0, WeaponType.ONE_HANDED_SWORD);
        Weapon noDamageWeapon2 = new Weapon("Broken Dagger", 0.0, 1.0, WeaponType.ONE_HANDED_DAGGER);

        Character fighter1 = new Character("Pacifist", 100.0, 100.0, 100.0, noDamageWeapon1);
        Character fighter2 = new Character("Also Pacifist", 100.0, 100.0, 100.0, noDamageWeapon2);

        MultiSimulationResult results = combatSimulator.simulateMultipleFights(fighter1, fighter2, 10);

        StringBuilder output = new StringBuilder();
        output.append("=== DRAW SCENARIO TEST ===\n\n");
        output.append("Both fighters have 0 damage weapons\n");
        output.append("Expected: All fights should be draws (hit MAX_TURNS)\n\n");

        output.append("Results:\n");
        output.append("  Fighter 1 Wins: ").append(results.fighter1Wins()).append("\n");
        output.append("  Fighter 2 Wins: ").append(results.fighter2Wins()).append("\n");
        output.append("  Draws: ").append(results.draws()).append("\n\n");

        output.append("  Fighter 1: ").append(String.format("%.1f", results.fighter1WinRate())).append("%\n");
        output.append("  Fighter 2: ").append(String.format("%.1f", results.fighter2WinRate())).append("%\n");
        output.append("  Draws: ").append(String.format("%.1f", results.drawRate())).append("%\n\n");

        if (!results.allFights().isEmpty()) {
            FightResult firstFight = results.allFights().get(0);
            output.append("  Winner: ").append(firstFight.winnerName()).append("\n");
            output.append("  Total Turns: ").append(firstFight.totalTurns()).append("\n");
            output.append("  Was Draw? ").append(firstFight.wasDraw() ? "YES" : "NO").append("\n");
        }

        return output.toString().replace("\n", "<br>");
    }
}

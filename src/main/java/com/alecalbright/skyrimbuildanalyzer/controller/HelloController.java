package com.alecalbright.skyrimbuildanalyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.CombatEvent;
import com.alecalbright.skyrimbuildanalyzer.model.Perk;
import com.alecalbright.skyrimbuildanalyzer.model.Weapon;
import com.alecalbright.skyrimbuildanalyzer.model.WeaponType;
import com.alecalbright.skyrimbuildanalyzer.simulation.CombatSimulator;
import com.alecalbright.skyrimbuildanalyzer.simulation.FightResult;
import com.alecalbright.skyrimbuildanalyzer.simulation.MultiSimulationResult;

@RestController
public class HelloController {

    @GetMapping("/")
    public String home(){
        return "Welcome to Skyrim Build Analyzer API! Try /hello endpoint.";
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello from Skyrim Build Analyzer!";
    }

    @GetMapping("/status")
    public String status(){
        return "Skyrim Build Analyzer is running! Version 1.0 - Ready for combat simulations.";
    }

    @GetMapping("/test-weapon")
    public String testWeapon(){
    Weapon ironSword = new Weapon(
        "Iron Sword", 
        7.0, 
        1.0, 
        WeaponType.ONE_HANDED_SWORD
    );
    
    return "Created weapon: " + ironSword.toString() + 
           " | DPS: " + ironSword.getDPS();
    }

    @GetMapping("/test-character")
    public String testCharacter(){
        Weapon ebonyBow = new Weapon("Ebony Bow", 19.0, 1.0, WeaponType.BOW);
        Character stealthArcher = new Character("Stealth Archer", 200.0, 250.0, 100.0, ebonyBow);

        stealthArcher.setArcherySkill(100);
        stealthArcher.setSneakSkill(100);

        String result = "Created: " + stealthArcher.toString();
        result += "\nIs alive? " + stealthArcher.isAlive();
        result += "\nDamage output: " + stealthArcher.calculateDamage();
        
        stealthArcher.takeDamage(50);
        result += "\nAfter taking 50 damage: " + stealthArcher.toString();
        result += "\nIs alive? " + stealthArcher.isAlive();
        
        stealthArcher.takeDamage(200);
        result += "\nAfter taking 200 damage: " + stealthArcher.toString();
        result += "\nIs alive? " + stealthArcher.isAlive();
        
        stealthArcher.reset();
        result += "\nAfter reset: " + stealthArcher.toString();
        
        return result.replace("\n", "<br>");
    }

    @GetMapping("/test-perks")
    public String testPerks() {
    StringBuilder result = new StringBuilder();
    
    result.append("=== DAMAGE-BOOSTING PERKS ===\n\n");
    
    for (Perk perk : Perk.values()) {
        if (perk.boostsDamage()) {
            result.append(perk.toString()).append("\n");
        }
    }
    
    result.append("\n=== SPECIFIC PERK EXAMPLES ===\n\n");
    
    Perk armsman = Perk.ARMSMAN_5;
    result.append("Perk: ").append(armsman.getDisplayName()).append("\n");
    result.append("Multiplier: ").append(armsman.getMultiplier()).append("x\n");
    result.append("Damage increase: ").append(armsman.getDamageIncreasePercent()).append("%\n");
    result.append("Boosts damage? ").append(armsman.boostsDamage()).append("\n\n");
    
    double baseDamage = 10.0;
    double withPerk = baseDamage * armsman.getMultiplier();
    result.append("Base damage: ").append(baseDamage).append("\n");
    result.append("With Armsman 5/5: ").append(withPerk).append("\n\n");
    
    Perk assassinsBlade = Perk.ASSASSINS_BLADE;
    double sneakDamage = baseDamage * assassinsBlade.getMultiplier();
    result.append("With Assassin's Blade: ").append(sneakDamage).append(" (15x!)\n");
    
    return result.toString().replace("\n", "<br>");
    }

    @GetMapping("/test-character-perks")
    public String testCharacterPerks() {
    // Create new weapon
    Weapon dagger = new Weapon(
        "Ebony Dagger",
        11.0,
        1.3,
        WeaponType.ONE_HANDED_DAGGER
    );
    
    // Create new character
    Character assassin = new Character(
        "Assassin",
        200.0,
        250.0,
        100.0,
        dagger
    );
    
    // Set skills
    assassin.setOneHandedSkill(100);
    assassin.setSneakSkill(100);
    
    // Add perks
    assassin.addPerk(Perk.ARMSMAN_5);
    assassin.addPerk(Perk.ASSASSINS_BLADE);
    assassin.addPerk(Perk.BACKSTAB);
    
    // Test perk methods
    StringBuilder result = new StringBuilder();
    result.append("Character: ").append(assassin.getName()).append("\n");
    result.append("Weapon: ").append(assassin.getWeapon().getName()).append("\n");
    result.append("Base damage: ").append(assassin.getWeapon().getBaseDamage()).append("\n\n");
    
    result.append("Perks:\n");
    for (Perk perk : assassin.getPerks()) {
        result.append("  - ").append(perk.toString()).append("\n");
    }
    
    result.append("\nPerk damage multiplier: ").append(assassin.getPerkDamageMultiplier()).append("x\n");
    result.append("Has Armsman 5/5? ").append(assassin.hasPerk(Perk.ARMSMAN_5)).append("\n");
    result.append("Has Overdraw 5/5? ").append(assassin.hasPerk(Perk.OVERDRAW_5)).append("\n");
    
    double baseDamage = assassin.calculateDamage();
    double withPerks = baseDamage * assassin.getPerkDamageMultiplier();
    result.append("\nDamage without perks: ").append(baseDamage).append("\n");
    result.append("Damage with perks: ").append(withPerks).append("\n");
    
    return result.toString().replace("\n", "<br>");
    }

    @GetMapping("/test-combat-event")
    public String testCombatEvent() {
    StringBuilder result = new StringBuilder();
    
    // Create some combat events
    CombatEvent normalAttack = CombatEvent.now(
        "Stealth Archer",
        "Two-Handed Warrior",
        45.5,
        "Ebony Bow",
        false,
        false
    );
    
    CombatEvent criticalHit = CombatEvent.now(
        "Two-Handed Warrior",
        "Stealth Archer",
        89.0,
        "Dragonbone Greatsword",
        true,
        false
    );
    
    CombatEvent sneakAttack = CombatEvent.now(
        "Stealth Archer",
        "Two-Handed Warrior",
        285.0,
        "Ebony Bow",
        false,
        true
    );
    
    CombatEvent criticalSneakAttack = CombatEvent.now(
        "Assassin",
        "Unsuspecting Guard",
        1650.0,
        "Ebony Dagger",
        true,
        true
    );
    
    // Display events
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
    public String testCombat() {
        // Create two weapons
        Weapon bow = new Weapon(
            "Ebony Bow",
            19.0,
            1.0,
            WeaponType.BOW
        );
        
        Weapon greatsword = new Weapon(
            "Dragonbone Greatsword",
            25.0,
            0.7,
            WeaponType.TWO_HANDED_GREATSWORD
        );
        
        // Create two characters
        Character stealthArcher = new Character(
            "Stealth Archer",
            200.0,  // health
            250.0,  // stamina
            100.0,  // magicka
            bow
        );
        
        Character warrior = new Character(
            "Two-Handed Warrior",
            400.0,  // Much more health!
            250.0,
            100.0,
            greatsword
        );

        // Create simulator
        CombatSimulator simulator = new CombatSimulator();
        
        // Simulate fight - now returns FightResult!
        FightResult fightResult = simulator.simulateFight(stealthArcher, warrior);
        
        // Build result string
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
        
        // Show first few combat events
        result.append("=== COMBAT LOG (First 5 Events) ===\n\n");
        int eventsToShow = Math.min(5, fightResult.combatEvents().size());
        for (int i = 0; i < eventsToShow; i++) {
            result.append("Event ").append(i + 1).append(": ").append(fightResult.combatEvents().get(i)).append("\n");
        }
        
        return result.toString().replace("\n", "<br>");
    }

    @GetMapping("/test-multi-combat")
    public String testMultiCombat() {
    // Create weapons
    Weapon bow = new Weapon("Ebony Bow", 19.0, 1.0, WeaponType.BOW);
    Weapon greatsword = new Weapon("Dragonbone Greatsword", 25.0, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
    
    // Create characters
    Character stealthArcher = new Character("Stealth Archer", 200.0, 250.0, 100.0, bow);
    Character warrior = new Character("Two-Handed Warrior", 400.0, 250.0, 100.0, greatsword);
    
    // Create simulator
    CombatSimulator simulator = new CombatSimulator();
    
    // Simulate 100 fights
    MultiSimulationResult results = simulator.simulateMultipleFights(stealthArcher, warrior, 100);
    
    // Build detailed result string
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
    output.append("Statistically Significant? ").append(results.isSignificant() ? "YES" : "NO").append("\n");
    output.append("Data Consistent? ").append(results.isDataConsistent() ? "YES" : "NO").append("\n\n");

    // Add new stats!
    output.append("Average Fight Duration: ").append(String.format("%.1f", results.averageFightDuration())).append(" turns\n");
    output.append("Shortest Fight: ").append(results.shortestFightDuration()).append(" turns\n");
    output.append("Longest Fight: ").append(results.longestFightDuration()).append(" turns\n\n");
    
    // ← ADD DATA CONSISTENCY CHECK
    output.append("Data Consistent? ").append(results.isDataConsistent() ? "YES" : "NO").append("\n\n");
    
    // Show sample of individual fights
    output.append("=== SAMPLE FIGHTS ===\n\n");
    for (int i = 0; i < Math.min(5, results.allFights().size()); i++) {
        FightResult fight = results.allFights().get(i);
        output.append("Fight ").append(i + 1).append(": ").append(fight.toString()).append("\n");
    }
    
    return output.toString().replace("\n", "<br>");
    }

    @GetMapping("/test-draw-scenario")
    public String testDrawScenario() {
    // Create two weapons with ZERO damage - forces timeout draw
    Weapon noDamageWeapon1 = new Weapon("Wooden Stick", 0.0, 1.0, WeaponType.ONE_HANDED_SWORD);
    Weapon noDamageWeapon2 = new Weapon("Broken Dagger", 0.0, 1.0, WeaponType.ONE_HANDED_DAGGER);
    
    // Create characters
    Character fighter1 = new Character("Pacifist", 100.0, 100.0, 100.0, noDamageWeapon1);
    Character fighter2 = new Character("Also Pacifist", 100.0, 100.0, 100.0, noDamageWeapon2);
    
    // Create simulator
    CombatSimulator simulator = new CombatSimulator();
    
    // Simulate 10 fights (all should be draws!)
    MultiSimulationResult results = simulator.simulateMultipleFights(fighter1, fighter2, 10);
    
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

    output.append("Data Consistent? ").append(results.isDataConsistent() ? "YES ✓" : "NO ✗").append("\n\n");

    if (!results.allFights().isEmpty()) {
        FightResult firstFight = results.allFights().get(0);
        output.append("  Winner: ").append(firstFight.winnerName()).append("\n");
        output.append("  Total Turns: ").append(firstFight.totalTurns()).append("\n");
        output.append("  Was Draw? ").append(firstFight.wasDraw() ? "YES" : "NO").append("\n");
    }
    
    return output.toString().replace("\n", "<br>");
    }
}

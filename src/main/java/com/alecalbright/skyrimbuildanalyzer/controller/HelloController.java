package com.alecalbright.skyrimbuildanalyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.Perk;
import com.alecalbright.skyrimbuildanalyzer.model.Weapon;
import com.alecalbright.skyrimbuildanalyzer.model.WeaponType;

@RestController
public class HelloController {
    
    @GetMapping("/hello")
    public String hello(){
        return "Hello from Skyrim Build Analyzer!";
    }
    
    @GetMapping("/")
    public String home(){
        return "Welcome to Skyrim Build Analyzer API! Try /hello endpoint.";
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
}

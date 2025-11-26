package com.alecalbright.skyrimbuildanalyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alecalbright.skyrimbuildanalyzer.model.Character;
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
}

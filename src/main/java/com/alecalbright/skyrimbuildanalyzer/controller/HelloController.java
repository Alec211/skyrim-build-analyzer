package com.alecalbright.skyrimbuildanalyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alecalbright.skyrimbuildanalyzer.model.Weapon;
import com.alecalbright.skyrimbuildanalyzer.model.WeaponType;

@RestController
public class HelloController {
    
    @GetMapping("/hello")
    public String hello() {
        return "Hello from Skyrim Build Analyzer!";
    }
    
    @GetMapping("/")
    public String home() {
        return "Welcome to Skyrim Build Analyzer API! Try /hello endpoint.";
    }

    @GetMapping("/status")
    public String status() {
        return "Skyrim Build Analyzer is running! Version 1.0 - Ready for combat simulations.";
    }

    @GetMapping("/test-weapon")
    public String testWeapon() {
    Weapon ironSword = new Weapon(
        "Iron Sword", 
        7.0, 
        1.0, 
        WeaponType.ONE_HANDED_SWORD
    );
    
    return "Created weapon: " + ironSword.toString() + 
           " | DPS: " + ironSword.getDPS();
}
}

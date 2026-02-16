package com.alecalbright.skyrimbuildanalyzer.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alecalbright.skyrimbuildanalyzer.model.Weapon;
import com.alecalbright.skyrimbuildanalyzer.model.WeaponType;
import com.alecalbright.skyrimbuildanalyzer.repository.WeaponRepository;

@RestController
public class WeaponController {

    private final WeaponRepository weaponRepository;

    public WeaponController(WeaponRepository weaponRepository){
        this.weaponRepository = weaponRepository;
    }

    @GetMapping("/test-weapon")
    public String testWeapon(){
        Weapon ironSword = new Weapon("Iron Sword", 7.0, 1.0, WeaponType.ONE_HANDED_SWORD);

        return "Created weapon: " + ironSword.toString() + " | DPS: " + ironSword.getDPS();
    }

    @GetMapping("/test-repository")
    public String testRepository(){
        StringBuilder result = new StringBuilder();
        result.append("=== WEAPON REPOSITORY TEST ===\n\n");

        result.append("Total weapons: ").append(weaponRepository.getWeaponCount()).append("\n\n");

        Weapon ebonyBow = weaponRepository.getWeapon("Ebony Bow");
        if (ebonyBow != null) {
            result.append("Found: ").append(ebonyBow).append("\n");
            result.append("  DPS: ").append(ebonyBow.getDPS()).append("\n\n");
        }

        Weapon ebonyBow2 = weaponRepository.getWeapon("ebony bow");
        result.append("Case insensitive works? ").append(ebonyBow2 != null ? "YES" : "NO").append("\n\n");

        List<Weapon> bows = weaponRepository.getWeaponsByType(WeaponType.BOW);
        result.append("Bows available: ").append(bows.size()).append("\n");
        for (Weapon bow : bows) {
            result.append("  - ").append(bow.getName())
                  .append(" (").append(bow.getBaseDamage()).append(" dmg, ")
                  .append(bow.getDPS()).append(" DPS)\n");
        }
        result.append("\n");

        result.append("=== ALL WEAPONS ===\n");
        List<Weapon> allWeapons = weaponRepository.getAllWeapons();
        allWeapons.stream()
            .sorted((w1, w2) -> Double.compare(w2.getDPS(), w1.getDPS()))
            .forEach(weapon -> {
                result.append(String.format("%-25s | %4.0f dmg | %.1f speed | %.1f DPS | %s\n",
                    weapon.getName(),
                    weapon.getBaseDamage(),
                    weapon.getAttackSpeed(),
                    weapon.getDPS(),
                    weapon.getWeaponType()));
            });

        return result.toString().replace("\n", "<br>");
    }
}

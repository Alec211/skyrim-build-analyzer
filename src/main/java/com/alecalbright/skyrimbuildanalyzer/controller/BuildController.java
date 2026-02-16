package com.alecalbright.skyrimbuildanalyzer.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alecalbright.skyrimbuildanalyzer.archetype.CharacterArchetype;
import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.Perk;
import com.alecalbright.skyrimbuildanalyzer.model.Weapon;
import com.alecalbright.skyrimbuildanalyzer.model.WeaponType;
import com.alecalbright.skyrimbuildanalyzer.repository.WeaponRepository;

@RestController
public class BuildController {

    private final WeaponRepository weaponRepository;

    public BuildController(WeaponRepository weaponRepository){
        this.weaponRepository = weaponRepository;
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
    public String testPerks(){
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
    public String testCharacterPerks(){
        Weapon dagger = new Weapon("Ebony Dagger", 11.0, 1.3, WeaponType.ONE_HANDED_DAGGER);

        Character assassin = new Character("Assassin", 200.0, 250.0, 100.0, dagger);
        assassin.setOneHandedSkill(100);
        assassin.setSneakSkill(100);

        assassin.addPerk(Perk.ARMSMAN_5);
        assassin.addPerk(Perk.ASSASSINS_BLADE);
        assassin.addPerk(Perk.BACKSTAB);

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

    @GetMapping("/test-archetypes")
    public String testArchetypes(){
        StringBuilder result = new StringBuilder();
        result.append("=== CHARACTER ARCHETYPES TEST ===\n\n");

        for(CharacterArchetype archetype : CharacterArchetype.values()){
            try{
                Character character = archetype.create(weaponRepository);

                result.append("Archetype: ").append(archetype.getDisplayName()).append("\n");
                result.append("  Health: ").append(character.getMaxHealth()).append("\n");
                result.append("  Stamina: ").append(character.getMaxStamina()).append("\n");
                result.append("  Magicka: ").append(character.getMaxMagicka()).append("\n");
                result.append("  Weapon: ").append(character.getWeapon().getName()).append(" (").append(String.format("%.1f", character.getWeapon().getDPS())).append(" DPS)\n");
                result.append("  Skills: Archery=").append(character.getArcherySkill()).append(", Sneak=").append(character.getSneakSkill()).append(", 1H=").append(character.getOneHandedSkill()).append(", 2H=").append(character.getTwoHandedSkill()).append("\n");
                result.append("  Perks: ").append(character.getPerks().size()).append(" equipped\n");
                result.append("  Base Damage: ").append(String.format("%.1f", character.calculateDamage())).append("\n");
                result.append("  Perk Multiplier: ").append(String.format("%.2fx", character.getPerkDamageMultiplier())).append("\n\n");
            }
            catch(Exception e){
                result.append("  ERROR: ").append(e.getMessage()).append("\n\n");
            }
        }

        return result.toString().replace("\n", "<br>");
    }
}

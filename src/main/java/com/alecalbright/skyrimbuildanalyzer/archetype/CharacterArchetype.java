package com.alecalbright.skyrimbuildanalyzer.archetype;

import java.util.Comparator;
import java.util.List;

import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.Perk;
import com.alecalbright.skyrimbuildanalyzer.model.Weapon;
import com.alecalbright.skyrimbuildanalyzer.model.WeaponType;
import com.alecalbright.skyrimbuildanalyzer.repository.WeaponRepository;



public enum CharacterArchetype {
     STEALTH_ARCHER(
        "Stealth Archer",
        200.0,  // health
        250.0,  // stamina
        100.0,  // magicka
        100,    // archerySkill
        100,    // sneakSkill
        50,     // oneHandedSkill
        50,     // twoHandedSkill
        new Perk[]{
            Perk.OVERDRAW_5,
            Perk.CRITICAL_SHOT,
            Perk.DEADLY_AIM,
            Perk.QUICK_SHOT,
            Perk.RANGER
        },
        WeaponType.BOW
    ),
    TWO_HANDED_WARRIOR(
        "Two-Handed Warrior",
        400.0,  // health - tankier!
        300.0,  // stamina
        100.0,  // magicka
        50,     // archerySkill
        50,     // sneakSkill
        50,     // oneHandedSkill
        100,    // twoHandedSkill
        new Perk[]{
            Perk.BARBARIAN_5,
            Perk.CHAMPIONS_STANCE,
            Perk.DEVASTATING_BLOW
        },
        WeaponType.TWO_HANDED_GREATSWORD
    ),
    DUAL_WIELDING_BERSERKER(
        "Dual-Wielding Berserker",
        300.0,  // health
        300.0,  // stamina
        100.0,  // magicka
        50,     // archerySkill
        75,     // sneakSkill
        100,    // oneHandedSkill
        50,     // twoHandedSkill
        new Perk[]{
            Perk.ARMSMAN_5,
            Perk.DUAL_FLURRY,
            Perk.DUAL_SAVAGERY,
            Perk.CRITICAL_CHARGE
        },
        WeaponType.ONE_HANDED_SWORD
    ),
    ASSASSIN(
        "Assassin",
        250.0,  // health
        250.0,  // stamina
        150.0,  // magicka - uses illusion spells
        50,     // archerySkill
        100,    // sneakSkill
        100,    // oneHandedSkill
        50,     // twoHandedSkill
        new Perk[]{
            Perk.ARMSMAN_5,
            Perk.BACKSTAB,
            Perk.ASSASSINS_BLADE,
            Perk.SHADOW_WARRIOR
        },
        WeaponType.ONE_HANDED_DAGGER
    ),
    PALADIN(
        "Paladin",
        350.0,  // health - tanky
        250.0,  // stamina
        200.0,  // magicka - uses restoration
        50,     // archerySkill
        50,     // sneakSkill
        100,    // oneHandedSkill
        50,     // twoHandedSkill
        new Perk[]{
            Perk.ARMSMAN_5,
            Perk.CRITICAL_CHARGE,
            Perk.SHIELD_WALL_5
        },
        WeaponType.ONE_HANDED_SWORD
    ),
    BARBARIAN(
        "Barbarian",
        500.0,  // health - maximum tank!
        350.0,  // stamina
        100.0,  // magicka
        50,     // archerySkill
        50,     // sneakSkill
        50,     // oneHandedSkill
        100,    // twoHandedSkill
        new Perk[]{
            Perk.BARBARIAN_5,
            Perk.CHAMPIONS_STANCE,
            Perk.DEVASTATING_BLOW
        },
        WeaponType.TWO_HANDED_WARHAMMER
    ),
    RANGER(
        "Ranger",
        300.0,  // health
        300.0,  // stamina
        100.0,  // magicka
        100,    // archerySkill
        75,     // sneakSkill
        75,     // oneHandedSkill
        50,     // twoHandedSkill
        new Perk[]{
            Perk.OVERDRAW_5,
            Perk.CRITICAL_SHOT,
            Perk.DEADLY_AIM,
            Perk.ARMSMAN_3
        },
        WeaponType.BOW
    ),
    SPELLSWORD(
        "Spellsword",
        300.0,  // health
        200.0,  // stamina
        300.0,  // magicka - uses destruction spells
        50,     // archerySkill
        50,     // sneakSkill
        100,    // oneHandedSkill
        50,     // twoHandedSkill
        new Perk[]{
            Perk.ARMSMAN_5,
            Perk.CRITICAL_CHARGE
        },
        WeaponType.ONE_HANDED_SWORD
    );

    private final String displayName;
    private final double baseHealth;
    private final double baseStamina;
    private final double baseMagicka;
    private final int archerySkill;
    private final int sneakSkill;
    private final int oneHandedSkill;
    private final int twoHandedSkill;
    private final Perk[] perks;
    private final WeaponType preferredWeaponType;

    CharacterArchetype(String displayName, double baseHealth, double baseStamina,
                       double baseMagicka, int archerySkill, int sneakSkill,
                       int oneHandedSkill, int twoHandedSkill,
                       Perk[] perks, WeaponType preferredWeaponType){
        this.displayName = displayName;
        this.baseHealth = baseHealth;
        this.baseStamina = baseStamina;
        this.baseMagicka = baseMagicka;
        this.archerySkill = archerySkill;
        this.sneakSkill = sneakSkill;
        this.oneHandedSkill = oneHandedSkill;
        this.twoHandedSkill = twoHandedSkill;
        this.perks = perks;
        this.preferredWeaponType = preferredWeaponType;
    }

    public Character create(WeaponRepository weaponRepository){
        Weapon weapon = selectBestWeapon(weaponRepository);
        
        return create(weapon);
    }

    public Character create(Weapon weapon){
        if (weapon.getWeaponType() != preferredWeaponType){
            System.out.println("Warning: " + displayName + " prefers " + 
                             preferredWeaponType + " but equipped with " + 
                             weapon.getWeaponType());
        }
        
        Character character = new Character(
            displayName,
            baseHealth,
            baseStamina,
            baseMagicka,
            weapon
        );
        
        character.setArcherySkill(archerySkill);
        character.setSneakSkill(sneakSkill);
        character.setOneHandedSkill(oneHandedSkill);
        character.setTwoHandedSkill(twoHandedSkill);
        
        for(Perk perk : perks){
            character.addPerk(perk);
        }
        
        return character;
    }

    public Character createWithWeapon(WeaponRepository weaponRepository, String weaponName){
        Weapon weapon = weaponRepository.getWeapon(weaponName);
        
        if(weapon == null){
            throw new IllegalArgumentException("Weapon not found: " + weaponName);
        }
        
        return create(weapon);
    }

    private Weapon selectBestWeapon(WeaponRepository weaponRepository){
        List<Weapon> availableWeapons = weaponRepository.getWeaponsByType(preferredWeaponType);
        
        if(availableWeapons.isEmpty()){
            throw new IllegalStateException(
                "No weapons of type " + preferredWeaponType + " available in repository"
            );
        }
        
        return availableWeapons.stream().max(Comparator.comparingDouble(Weapon::getDPS)).orElseThrow();
    }

    public String getDisplayName(){
        return displayName;
    }
    
    public double getBaseHealth(){
        return baseHealth;
    }
    
    public double getBaseStamina(){
        return baseStamina;
    }
    
    public double getBaseMagicka(){
        return baseMagicka;
    }
    
    public WeaponType getPreferredWeaponType(){
        return preferredWeaponType;
    }
    
    public Perk[] getPerks(){
        return perks.clone();
    }
    
    public int getArcherySkill(){
        return archerySkill;
    }
    
    public int getSneakSkill(){
        return sneakSkill;
    }
    
    public int getOneHandedSkill(){
        return oneHandedSkill;
    }
    
    public int getTwoHandedSkill(){
        return twoHandedSkill;
    }
    
    @Override
    public String toString(){
        return displayName;
    }
}

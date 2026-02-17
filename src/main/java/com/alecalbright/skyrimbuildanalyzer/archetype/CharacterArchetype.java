package com.alecalbright.skyrimbuildanalyzer.archetype;

import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonValue;

import com.alecalbright.skyrimbuildanalyzer.model.Armor;
import com.alecalbright.skyrimbuildanalyzer.model.ArmorWeight;
import com.alecalbright.skyrimbuildanalyzer.model.Character;
import com.alecalbright.skyrimbuildanalyzer.model.Perk;
import com.alecalbright.skyrimbuildanalyzer.model.Shield;
import com.alecalbright.skyrimbuildanalyzer.model.Weapon;
import com.alecalbright.skyrimbuildanalyzer.model.WeaponType;
import com.alecalbright.skyrimbuildanalyzer.repository.ArmorRepository;
import com.alecalbright.skyrimbuildanalyzer.repository.WeaponRepository;

public enum CharacterArchetype {
    STEALTH_ARCHER(
        "Stealth Archer",
        200.0, 250.0, 100.0,
        100, 100, 50, 50,
        new Perk[]{
            Perk.OVERDRAW_5,
            Perk.CRITICAL_SHOT,
            Perk.DEADLY_AIM,
            Perk.QUICK_SHOT,
            Perk.RANGER,
            Perk.AGILE_DEFENDER_3
        },
        WeaponType.BOW,
        ArmorWeight.LIGHT, 2, false
    ),
    TWO_HANDED_WARRIOR(
        "Two-Handed Warrior",
        400.0, 300.0, 100.0,
        50, 50, 50, 100,
        new Perk[]{
            Perk.BARBARIAN_5,
            Perk.CHAMPIONS_STANCE,
            Perk.DEVASTATING_BLOW,
            Perk.JUGGERNAUT_5
        },
        WeaponType.TWO_HANDED_GREATSWORD,
        ArmorWeight.HEAVY, 3, false
    ),
    DUAL_WIELDING_BERSERKER(
        "Dual-Wielding Berserker",
        300.0, 300.0, 100.0,
        50, 75, 100, 50,
        new Perk[]{
            Perk.ARMSMAN_5,
            Perk.DUAL_FLURRY,
            Perk.DUAL_SAVAGERY,
            Perk.CRITICAL_CHARGE,
            Perk.AGILE_DEFENDER_3
        },
        WeaponType.ONE_HANDED_SWORD,
        ArmorWeight.LIGHT, 2, false
    ),
    ASSASSIN(
        "Assassin",
        250.0, 250.0, 150.0,
        50, 100, 100, 50,
        new Perk[]{
            Perk.ARMSMAN_5,
            Perk.BACKSTAB,
            Perk.ASSASSINS_BLADE,
            Perk.SHADOW_WARRIOR
        },
        WeaponType.ONE_HANDED_DAGGER,
        ArmorWeight.LIGHT, 1, false
    ),
    PALADIN(
        "Paladin",
        350.0, 250.0, 200.0,
        50, 50, 100, 50,
        new Perk[]{
            Perk.ARMSMAN_5,
            Perk.CRITICAL_CHARGE,
            Perk.SHIELD_WALL_5,
            Perk.JUGGERNAUT_5,
            Perk.MATCHING_SET
        },
        WeaponType.ONE_HANDED_SWORD,
        ArmorWeight.HEAVY, 3, true
    ),
    BARBARIAN(
        "Barbarian",
        500.0, 350.0, 100.0,
        50, 50, 50, 100,
        new Perk[]{
            Perk.BARBARIAN_5,
            Perk.CHAMPIONS_STANCE,
            Perk.DEVASTATING_BLOW,
            Perk.JUGGERNAUT_5,
            Perk.MATCHING_SET
        },
        WeaponType.TWO_HANDED_WARHAMMER,
        ArmorWeight.HEAVY, 4, false
    ),
    RANGER(
        "Ranger",
        300.0, 300.0, 100.0,
        100, 75, 75, 50,
        new Perk[]{
            Perk.OVERDRAW_5,
            Perk.CRITICAL_SHOT,
            Perk.DEADLY_AIM,
            Perk.ARMSMAN_3,
            Perk.AGILE_DEFENDER_3
        },
        WeaponType.BOW,
        ArmorWeight.LIGHT, 2, false
    ),
    SPELLSWORD(
        "Spellsword",
        300.0, 200.0, 300.0,
        50, 50, 100, 50,
        new Perk[]{
            Perk.ARMSMAN_5,
            Perk.CRITICAL_CHARGE,
            Perk.AGILE_DEFENDER_3
        },
        WeaponType.ONE_HANDED_SWORD,
        ArmorWeight.LIGHT, 2, false
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
    private final ArmorWeight preferredArmorWeight;
    private final int armorTier;
    private final boolean usesShield;

    CharacterArchetype(String displayName, double baseHealth, double baseStamina,
                       double baseMagicka, int archerySkill, int sneakSkill,
                       int oneHandedSkill, int twoHandedSkill,
                       Perk[] perks, WeaponType preferredWeaponType,
                       ArmorWeight preferredArmorWeight, int armorTier, boolean usesShield){
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
        this.preferredArmorWeight = preferredArmorWeight;
        this.armorTier = armorTier;
        this.usesShield = usesShield;
    }

    public Character create(WeaponRepository weaponRepository, ArmorRepository armorRepository){
        Weapon weapon = selectBestWeapon(weaponRepository);
        Armor armor = armorRepository.getBestArmorByWeightAndTier(preferredArmorWeight, armorTier);
        Shield shield = usesShield
            ? armorRepository.getBestShieldByWeight(preferredArmorWeight)
            : null;

        return create(weapon, armor, shield);
    }

    public Character create(WeaponRepository weaponRepository){
        Weapon weapon = selectBestWeapon(weaponRepository);
        return create(weapon, null, null);
    }

    public Character create(Weapon weapon, Armor armor, Shield shield){
        Character character = new Character(
            displayName,
            baseHealth,
            baseStamina,
            baseMagicka,
            weapon,
            armor,
            shield
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

        return create(weapon, null, null);
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

    @JsonValue
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

    public ArmorWeight getPreferredArmorWeight(){
        return preferredArmorWeight;
    }

    public int getArmorTier(){
        return armorTier;
    }

    public boolean usesShield(){
        return usesShield;
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

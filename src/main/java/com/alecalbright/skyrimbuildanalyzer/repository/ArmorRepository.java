package com.alecalbright.skyrimbuildanalyzer.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.alecalbright.skyrimbuildanalyzer.model.Armor;
import com.alecalbright.skyrimbuildanalyzer.model.ArmorType;
import com.alecalbright.skyrimbuildanalyzer.model.ArmorWeight;
import com.alecalbright.skyrimbuildanalyzer.model.Shield;

@Repository
public class ArmorRepository {

    private final Map<String, Armor> armorCache = new HashMap<>();
    private final Map<String, Shield> shieldCache = new HashMap<>();
    private boolean loaded = false;

    public Armor getArmor(String name) {
        ensureLoaded();
        return armorCache.get(name.toLowerCase());
    }

    public List<Armor> getAllArmor() {
        ensureLoaded();
        return new ArrayList<>(armorCache.values());
    }

    public List<Armor> getArmorByWeight(ArmorWeight weight) {
        ensureLoaded();
        return armorCache.values().stream()
            .filter(a -> a.getWeight() == weight)
            .toList();
    }

    public List<Armor> getArmorByType(ArmorType type) {
        ensureLoaded();
        return armorCache.values().stream()
            .filter(a -> a.getArmorType() == type)
            .toList();
    }

    public Armor getBestArmorByWeightAndTier(ArmorWeight weight, int tier) {
        ensureLoaded();
        return armorCache.values().stream()
            .filter(a -> a.getWeight() == weight && a.getTier() == tier)
            .max(Comparator.comparingDouble(Armor::getBaseArmorRating))
            .orElse(null);
    }

    public Shield getShield(String name) {
        ensureLoaded();
        return shieldCache.get(name.toLowerCase());
    }

    public List<Shield> getAllShields() {
        ensureLoaded();
        return new ArrayList<>(shieldCache.values());
    }

    public List<Shield> getShieldsByWeight(ArmorWeight weight) {
        ensureLoaded();
        return shieldCache.values().stream()
            .filter(s -> s.getWeight() == weight)
            .toList();
    }

    public Shield getBestShieldByWeight(ArmorWeight weight) {
        ensureLoaded();
        return shieldCache.values().stream()
            .filter(s -> s.getWeight() == weight)
            .max(Comparator.comparingDouble(Shield::getBaseArmorRating))
            .orElse(null);
    }

    private synchronized void ensureLoaded() {
        if (!loaded) {
            loadArmor();
            loadShields();
            loaded = true;
        }
    }

    private void loadArmor() {
        // Light Armor - Tier 1
        addArmor("Hide Armor", 40, ArmorType.HIDE);
        addArmor("Leather Armor", 52, ArmorType.LEATHER);

        // Light Armor - Tier 2
        addArmor("Elven Armor", 82, ArmorType.ELVEN);
        addArmor("Scaled Armor", 85, ArmorType.SCALED);

        // Light Armor - Tier 3
        addArmor("Glass Armor", 103, ArmorType.GLASS);

        // Light Armor - Tier 4
        addArmor("Dragonscale Armor", 111, ArmorType.DRAGONSCALE);

        // Heavy Armor - Tier 1
        addArmor("Iron Armor", 60, ArmorType.IRON);
        addArmor("Steel Armor", 72, ArmorType.STEEL);

        // Heavy Armor - Tier 2
        addArmor("Dwarven Armor", 98, ArmorType.DWARVEN);
        addArmor("Orcish Armor", 100, ArmorType.ORCISH);

        // Heavy Armor - Tier 3
        addArmor("Ebony Armor", 128, ArmorType.EBONY);

        // Heavy Armor - Tier 4
        addArmor("Daedric Armor", 144, ArmorType.DAEDRIC);
        addArmor("Dragonplate Armor", 136, ArmorType.DRAGONPLATE);
    }

    private void loadShields() {
        // Light Shields
        addShield("Elven Shield", 21, ArmorWeight.LIGHT);
        addShield("Glass Shield", 27, ArmorWeight.LIGHT);
        addShield("Dragonscale Shield", 29, ArmorWeight.LIGHT);

        // Heavy Shields
        addShield("Iron Shield", 20, ArmorWeight.HEAVY);
        addShield("Steel Shield", 24, ArmorWeight.HEAVY);
        addShield("Dwarven Shield", 26, ArmorWeight.HEAVY);
        addShield("Ebony Shield", 32, ArmorWeight.HEAVY);
        addShield("Daedric Shield", 36, ArmorWeight.HEAVY);
        addShield("Dragonplate Shield", 34, ArmorWeight.HEAVY);
    }

    private void addArmor(String name, double rating, ArmorType type) {
        armorCache.put(name.toLowerCase(), new Armor(name, rating, type));
    }

    private void addShield(String name, double rating, ArmorWeight weight) {
        shieldCache.put(name.toLowerCase(), new Shield(name, rating, weight));
    }
}

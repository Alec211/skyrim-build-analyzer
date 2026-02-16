package com.alecalbright.skyrimbuildanalyzer.repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Repository;

import com.alecalbright.skyrimbuildanalyzer.model.Weapon;
import com.alecalbright.skyrimbuildanalyzer.model.WeaponType;

@Repository
public class WeaponRepository {
    private static final String UESP_WEAPONS_URL = "https://en.uesp.net/wiki/Skyrim:Weapons";
    private static final int TIMEOUT_MS = 10000;
    private final Map<String, Weapon> weaponCache = new HashMap<>();
    private boolean weaponsLoaded = false;

    public WeaponRepository(){}

    public Weapon getWeapon(String name){
        ensureWeaponsLoaded();
        
        if(name == null || name.isBlank()){
            return null;
        }
        
        return weaponCache.get(name.toLowerCase().trim());
    }
    
    public List<Weapon> getAllWeapons(){
        ensureWeaponsLoaded();
        return List.copyOf(weaponCache.values());
    }
    
    public List<Weapon> getWeaponsByType(WeaponType weaponType){
        ensureWeaponsLoaded();
        
        return weaponCache.values().stream().filter(weapon -> weapon.getWeaponType() == weaponType).collect(Collectors.toList());
    }
    
    public boolean hasWeapon(String name){
        return getWeapon(name) != null;
    }

    public int getWeaponCount(){
        ensureWeaponsLoaded();
        return weaponCache.size();
    }

    public void reloadWeapons(){
        weaponCache.clear();
        weaponsLoaded = false;
        ensureWeaponsLoaded();
    }
    
    private synchronized void ensureWeaponsLoaded(){
        if(!weaponsLoaded){
            loadWeapons();
            weaponsLoaded = true;
        }
    }

    private void loadWeapons(){
        try{
            System.out.println("Loading weapons from UESP Wiki...");
            long startTime = System.currentTimeMillis();
            
            boolean scrapingSucceeded = false;
            
            try{
                Document doc = Jsoup.connect(UESP_WEAPONS_URL).timeout(TIMEOUT_MS).userAgent("Mozilla/5.0 (Educational Project)").get();
                
                System.out.println("Successfully connected to UESP");
                
                Elements tables = doc.select("table.wikitable");
                System.out.println("Found " + tables.size() + " tables on UESP weapons page");
                
                int tableNum = 0;
                for(Element table : tables){
                    tableNum++;
                    System.out.println("Parsing table #" + tableNum + "...");
                    int beforeCount = weaponCache.size();
                    parseWeaponTable(table);
                    int afterCount = weaponCache.size();
                    System.out.println("  Added " + (afterCount - beforeCount) + " weapons from this table");
                }
                
                System.out.println("Weapons from scraping: " + weaponCache.size());
                
                if(weaponCache.size() > 20){
                    scrapingSucceeded = true;
                }
                
            } 
            catch(IOException e){
                System.err.println("Failed to connect to UESP: " + e.getMessage());
            }
            
            // If scraping failed or got too few weapons, use fallback
            if(!scrapingSucceeded){
                System.out.println("Scraping didn't get enough weapons, using comprehensive database...");
                weaponCache.clear();
                fallbackSampleWeapons();
            }
            
            // Add legendary weapons whether it fails or not
            addLegendaryWeapons();
            
            long endTime = System.currentTimeMillis();
            System.out.println("Loaded " + weaponCache.size() + " total weapons in " + (endTime - startTime) + "ms");
            
        }
        catch(Exception e){
            throw new RuntimeException("Failed to load weapons: " + e.getMessage(), e);
        }
    }

    private void parseWeaponTable(Element table){
        Elements rows = table.select("tr");
        System.out.println("  Table has " + rows.size() + " rows");
        
        if(rows.isEmpty()){
            System.out.println("  Table is empty, skipping");
            return;
        }
        
        // Header row
        Element headerRow = rows.first();
        
        // DEBUG: Print what headers actually exist
        printTableHeaders(table);
        
        // Find Name column (contains "Name" and "ID")
        int nameCol = findNameColumn(headerRow);
        
        if(nameCol == -1){
            System.out.println("  Name column not found, skipping table");
            return;
        }
        
        System.out.println("  Name column found at index: " + nameCol);
        
        // Based on UESP structure:
        // - Name is at nameCol
        // - First number after Name is usually Damage
        // - We'll detect column types by analyzing first data row
        
        int damageCol = detectDamageColumn(rows, nameCol);
        int speedCol = detectSpeedColumn(headerRow);
        
        System.out.println("  Column indices: Name=" + nameCol + ", Damage=" + damageCol + ", Speed=" + speedCol);
        
        if (damageCol == -1) {
            System.out.println("  Damage column not found, skipping table");
            return;
        }
        
        // Parse each data row
        int parsedCount = 0;
        int skippedCount = 0;
        
        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cells = row.select("td");
            
            if (cells.size() <= nameCol || cells.size() <= damageCol) {
                skippedCount++;
                continue;
            }
            
            try {
                // Extract weapon data
                String name = extractName(cells.get(nameCol));
                double damage = extractNumber(cells.get(damageCol));
                double speed = (speedCol != -1 && cells.size() > speedCol) 
                    ? extractNumber(cells.get(speedCol)) 
                    : 1.0;
                
                WeaponType type = inferWeaponType(name, table);
                
                // Validate data
                if (name != null && !name.isBlank() && damage > 0) {
                    addWeapon(name, damage, speed, type);
                    parsedCount++;
                } else {
                    skippedCount++;
                }
                
            } catch (Exception e) {
                System.err.println("  Failed to parse row " + i + ": " + e.getMessage());
                skippedCount++;
            }
        }
        
        System.out.println("  Parsed " + parsedCount + " weapons, skipped " + skippedCount + " rows");
    }

    private String extractName(Element cell){
        // Try to get link text first (often the weapon name is in a link)
        Element link = cell.selectFirst("a");
        if (link != null) {
            return link.text().trim();
        }
        
        return cell.text().trim();
    }

    private double extractNumber(Element cell){
        String text = cell.text().trim();

        // Remove any non-numeric characters except decimal point and minus
        text = text.replaceAll("[^0-9.-]", "");
        
        if(text.isEmpty()){
            return 1.0;
        }
        
        try{
            return Double.parseDouble(text);
        }
        catch(NumberFormatException e){
            return 1.0;
        }
    }

    private WeaponType inferWeaponType(String name, Element table){
        String nameLower = name.toLowerCase();
        Element prevHeading = table.previousElementSibling();
        String context = "";
        
        while(prevHeading != null){
            if(prevHeading.tagName().matches("h[2-4]")){
                context = prevHeading.text().toLowerCase();
                break;
            }
            prevHeading = prevHeading.previousElementSibling();
        }
        
        // Bows
        if(nameLower.contains("bow") || context.contains("bow") || context.contains("archery")){
            if(nameLower.contains("cross")){
                return WeaponType.CROSSBOW;
            }
            return WeaponType.BOW;
        }
        
        // Two-handed weapons
        if(nameLower.contains("greatsword") || nameLower.contains("great sword")){
            return WeaponType.TWO_HANDED_GREATSWORD;
        }
        if(nameLower.contains("battleaxe") || nameLower.contains("battle axe") || nameLower.contains("battle-axe")){
            return WeaponType.TWO_HANDED_BATTLEAXE;
        }
        if(nameLower.contains("warhammer") || nameLower.contains("war hammer")){
            return WeaponType.TWO_HANDED_WARHAMMER;
        }
        
        // Check context for two-handed
        if(context.contains("two-handed") || context.contains("two handed")){
            if(context.contains("axe")){
                return WeaponType.TWO_HANDED_BATTLEAXE;
            }
            if(context.contains("hammer") || context.contains("mace")){
                return WeaponType.TWO_HANDED_WARHAMMER;
            }
            return WeaponType.TWO_HANDED_GREATSWORD;
        }
        
        if(nameLower.contains("dagger")){
            return WeaponType.ONE_HANDED_DAGGER;
        }
        if(nameLower.contains("mace")){
            return WeaponType.ONE_HANDED_MACE;
        }
        if(nameLower.contains("axe")){
            return WeaponType.ONE_HANDED_AXE;
        }
        if(nameLower.contains("sword")){
            return WeaponType.ONE_HANDED_SWORD;
        }
        
        // Default to one-handed sword
        return WeaponType.ONE_HANDED_SWORD;
    }

    private int findNameColumn(Element headerRow){
        Elements headers = headerRow.select("th");
        
        for(int i = 0; i < headers.size(); i++){
            String headerText = headers.get(i).text().trim().toLowerCase();
            
            if(headerText.contains("name")){
                return i;
            }
        }
        
        return -1;
    }

    private int detectDamageColumn(Elements rows, int nameCol){
        if (rows.size() < 2) {
            return -1;
        }
        
        // Look at the first data row (skip header row 0)
        Element firstDataRow = rows.get(1);
        Elements cells = firstDataRow.select("td");
        
        // Start searching after the name column
        for(int col = nameCol + 1; col < cells.size(); col++){
            String cellText = cells.get(col).text().trim();
            
            // Check if this cell contains a number
            if(cellText.matches("\\d+(\\.\\d+)?")){
                double value = Double.parseDouble(cellText);
                
                // Damage values in Skyrim are typically 1-30
                if(value >= 1 && value <= 100){
                    System.out.println("  Detected damage column at index " + col + " (first value: " + value + ")");
                    return col;
                }
            }
        }
        
        // Return -1 if damage column not found
        return -1;
    }

    private int detectSpeedColumn(Element headerRow){
        Elements headers = headerRow.select("th");
        
        for(int i = 0; i < headers.size(); i++){
            String headerText = headers.get(i).text().trim().toLowerCase();
            
            if(headerText.contains("speed") || headerText.equals("spd") || headerText.equals("spd.")){
                return i;
            }
        }
        
        // Return -1 if speed column not found
        return -1;
    }


    private void printTableHeaders(Element table){
        Element headerRow = table.select("tr").first();
        if(headerRow == null) return;
        
        Elements headers = headerRow.select("th");
        System.out.println("  Table headers: ");

        for(int i = 0; i < headers.size(); i++){
            System.out.println("    [" + i + "] = '" + headers.get(i).text().trim() + "'");
        }
    }

    private void addLegendaryWeapons() {
        // Unique legendary weapons from Skyrim
        addWeaponIfNotExists("Dawnbreaker", 12, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeaponIfNotExists("Chillrend", 15, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeaponIfNotExists("Nightingale Blade", 14, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeaponIfNotExists("Mehrunes' Razor", 11, 1.3, WeaponType.ONE_HANDED_DAGGER);
        addWeaponIfNotExists("Blade of Woe", 12, 1.3, WeaponType.ONE_HANDED_DAGGER);
        addWeaponIfNotExists("Wuuthrad", 25, 0.75, WeaponType.TWO_HANDED_BATTLEAXE);
        addWeaponIfNotExists("Volendrung", 25, 0.6, WeaponType.TWO_HANDED_WARHAMMER);
        addWeaponIfNotExists("Auriel's Bow", 13, 1.0, WeaponType.BOW);
        addWeaponIfNotExists("Zephyr", 12, 1.5, WeaponType.BOW); // Fastest bow!
    }

    private void addWeaponIfNotExists(String name, double damage, double speed, WeaponType type){
        String key = name.toLowerCase();
        if(!weaponCache.containsKey(key)){
            addWeapon(name, damage, speed, type);
        }
    }

    private void fallbackSampleWeapons(){
        System.out.println("Loading fallback sample weapons...");
        
        // One-handed weapons
        addWeapon("Iron Sword", 7, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeapon("Iron Dagger", 4, 1.3, WeaponType.ONE_HANDED_DAGGER);
        addWeapon("Steel Sword", 8, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeapon("Elven Sword", 13, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeapon("Glass Sword", 16, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeapon("Ebony Sword", 17, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeapon("Daedric Sword", 18, 1.0, WeaponType.ONE_HANDED_SWORD);
        
        // Two-handed weapons
        addWeapon("Iron Greatsword", 16, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Steel Greatsword", 17, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Elven Greatsword", 19, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Glass Greatsword", 22, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Ebony Greatsword", 23, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Daedric Greatsword", 24, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Dragonbone Greatsword", 25, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        
        // Bows
        addWeapon("Long Bow", 6, 0.75, WeaponType.BOW);
        addWeapon("Hunting Bow", 7, 1.0, WeaponType.BOW);
        addWeapon("Imperial Bow", 8, 1.0, WeaponType.BOW);
        addWeapon("Orcish Bow", 10, 1.0, WeaponType.BOW);
        addWeapon("Dwarven Bow", 12, 1.0, WeaponType.BOW);
        addWeapon("Elven Bow", 13, 1.0, WeaponType.BOW);
        addWeapon("Glass Bow", 15, 1.0, WeaponType.BOW);
        addWeapon("Ebony Bow", 19, 1.0, WeaponType.BOW);
        addWeapon("Daedric Bow", 19, 1.0, WeaponType.BOW);
        addWeapon("Dragonbone Bow", 20, 1.0, WeaponType.BOW);
    }

    private void addWeapon(String name, double damage, double speed, WeaponType type){
        Weapon weapon = new Weapon(name, damage, speed, type);
        weaponCache.put(name.toLowerCase(), weapon);
    }

}
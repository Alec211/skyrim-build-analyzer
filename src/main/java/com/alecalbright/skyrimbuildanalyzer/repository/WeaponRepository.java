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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.alecalbright.skyrimbuildanalyzer.model.Weapon;
import com.alecalbright.skyrimbuildanalyzer.model.WeaponType;

@Repository
public class WeaponRepository {
    private static final Logger log = LoggerFactory.getLogger(WeaponRepository.class);
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

        return weaponCache.values().stream()
            .filter(weapon -> weapon.getWeaponType() == weaponType)
            .collect(Collectors.toList());
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
            log.info("Loading weapons from UESP Wiki...");
            long startTime = System.currentTimeMillis();

            boolean scrapingSucceeded = false;

            try{
                Document doc = Jsoup.connect(UESP_WEAPONS_URL).timeout(TIMEOUT_MS).userAgent("Mozilla/5.0 (Educational Project)").get();

                log.info("Successfully connected to UESP");

                Elements tables = doc.select("table.wikitable");
                log.info("Found {} tables on UESP weapons page", tables.size());

                int tableNum = 0;
                for(Element table : tables){
                    tableNum++;
                    int beforeCount = weaponCache.size();
                    parseWeaponTable(table, tableNum);
                    int afterCount = weaponCache.size();
                    log.debug("Table #{}: added {} weapons", tableNum, afterCount - beforeCount);
                }

                log.info("Weapons from scraping: {}", weaponCache.size());

                if(weaponCache.size() > 20){
                    scrapingSucceeded = true;
                }

            }
            catch(IOException e){
                log.warn("Failed to connect to UESP: {}", e.getMessage());
            }

            if(!scrapingSucceeded){
                log.info("Scraping didn't get enough weapons, using fallback database...");
                weaponCache.clear();
                fallbackSampleWeapons();
            }

            addLegendaryWeapons();

            long endTime = System.currentTimeMillis();
            log.info("Loaded {} total weapons in {}ms", weaponCache.size(), endTime - startTime);

        }
        catch(Exception e){
            throw new RuntimeException("Failed to load weapons: " + e.getMessage(), e);
        }
    }

    private void parseWeaponTable(Element table, int tableNum){
        Elements rows = table.select("tr");

        if(rows.isEmpty()){
            return;
        }

        Element headerRow = rows.first();

        int nameCol = findNameColumn(headerRow);

        if(nameCol == -1){
            log.debug("Table #{}: name column not found, skipping", tableNum);
            return;
        }

        int damageCol = detectDamageColumn(rows, nameCol);
        int speedCol = detectSpeedColumn(headerRow);

        if (damageCol == -1) {
            log.debug("Table #{}: damage column not found, skipping", tableNum);
            return;
        }

        int parsedCount = 0;

        for (int i = 1; i < rows.size(); i++) {
            Element row = rows.get(i);
            Elements cells = row.select("td");

            if (cells.size() <= nameCol || cells.size() <= damageCol) {
                continue;
            }

            try {
                String name = extractName(cells.get(nameCol));
                double damage = extractNumber(cells.get(damageCol));
                double speed = (speedCol != -1 && cells.size() > speedCol)
                    ? extractNumber(cells.get(speedCol))
                    : 1.0;

                WeaponType type = inferWeaponType(name, table);

                if (name != null && !name.isBlank() && damage > 0) {
                    addWeapon(name, damage, speed, type);
                    parsedCount++;
                }

            } catch (Exception e) {
                log.debug("Table #{}, row {}: parse failed - {}", tableNum, i, e.getMessage());
            }
        }

        log.debug("Table #{}: parsed {} weapons", tableNum, parsedCount);
    }

    private String extractName(Element cell){
        Element link = cell.selectFirst("a");
        if (link != null) {
            return link.text().trim();
        }

        return cell.text().trim();
    }

    private double extractNumber(Element cell){
        String text = cell.text().trim();
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

        if(nameLower.contains("bow") || context.contains("bow") || context.contains("archery")){
            if(nameLower.contains("cross")){
                return WeaponType.CROSSBOW;
            }
            return WeaponType.BOW;
        }

        if(nameLower.contains("greatsword") || nameLower.contains("great sword")){
            return WeaponType.TWO_HANDED_GREATSWORD;
        }
        if(nameLower.contains("battleaxe") || nameLower.contains("battle axe") || nameLower.contains("battle-axe")){
            return WeaponType.TWO_HANDED_BATTLEAXE;
        }
        if(nameLower.contains("warhammer") || nameLower.contains("war hammer")){
            return WeaponType.TWO_HANDED_WARHAMMER;
        }

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

        Element firstDataRow = rows.get(1);
        Elements cells = firstDataRow.select("td");

        for(int col = nameCol + 1; col < cells.size(); col++){
            String cellText = cells.get(col).text().trim();

            if(cellText.matches("\\d+(\\.\\d+)?")){
                double value = Double.parseDouble(cellText);

                if(value >= 1 && value <= 100){
                    return col;
                }
            }
        }

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

        return -1;
    }

    private void addLegendaryWeapons() {
        addWeaponIfNotExists("Dawnbreaker", 12, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeaponIfNotExists("Chillrend", 15, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeaponIfNotExists("Nightingale Blade", 14, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeaponIfNotExists("Mehrunes' Razor", 11, 1.3, WeaponType.ONE_HANDED_DAGGER);
        addWeaponIfNotExists("Blade of Woe", 12, 1.3, WeaponType.ONE_HANDED_DAGGER);
        addWeaponIfNotExists("Wuuthrad", 25, 0.75, WeaponType.TWO_HANDED_BATTLEAXE);
        addWeaponIfNotExists("Volendrung", 25, 0.6, WeaponType.TWO_HANDED_WARHAMMER);
        addWeaponIfNotExists("Auriel's Bow", 13, 1.0, WeaponType.BOW);
        addWeaponIfNotExists("Zephyr", 12, 1.5, WeaponType.BOW);
    }

    private void addWeaponIfNotExists(String name, double damage, double speed, WeaponType type){
        String key = name.toLowerCase();
        if(!weaponCache.containsKey(key)){
            addWeapon(name, damage, speed, type);
        }
    }

    private void fallbackSampleWeapons(){
        log.info("Loading fallback sample weapons...");

        addWeapon("Iron Sword", 7, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeapon("Iron Dagger", 4, 1.3, WeaponType.ONE_HANDED_DAGGER);
        addWeapon("Steel Sword", 8, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeapon("Elven Sword", 13, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeapon("Glass Sword", 16, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeapon("Ebony Sword", 17, 1.0, WeaponType.ONE_HANDED_SWORD);
        addWeapon("Daedric Sword", 18, 1.0, WeaponType.ONE_HANDED_SWORD);

        addWeapon("Iron Greatsword", 16, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Steel Greatsword", 17, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Elven Greatsword", 19, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Glass Greatsword", 22, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Ebony Greatsword", 23, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Daedric Greatsword", 24, 0.7, WeaponType.TWO_HANDED_GREATSWORD);
        addWeapon("Dragonbone Greatsword", 25, 0.7, WeaponType.TWO_HANDED_GREATSWORD);

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

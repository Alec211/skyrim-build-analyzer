package com.alecalbright.skyrimbuildanalyzer.repository;

import java.io.IOException;
import java.util.ArrayList;
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

import com.alecalbright.skyrimbuildanalyzer.model.EnemyCategory;
import com.alecalbright.skyrimbuildanalyzer.model.EnemyDefinition;
import com.alecalbright.skyrimbuildanalyzer.model.Perk;

@Repository
public class EnemyRepository {
    private static final Logger log = LoggerFactory.getLogger(EnemyRepository.class);
    private static final int TIMEOUT_MS = 10000;
    private final Map<String, EnemyDefinition> enemyCache = new HashMap<>();
    private boolean loaded = false;

    public EnemyDefinition getEnemyByName(String name) {
        ensureLoaded();
        if (name == null || name.isBlank()) return null;
        return enemyCache.get(name.toLowerCase().trim());
    }

    public List<EnemyDefinition> getAllEnemies() {
        ensureLoaded();
        return List.copyOf(enemyCache.values());
    }

    public List<EnemyDefinition> getEnemiesByCategory(EnemyCategory category) {
        ensureLoaded();
        return enemyCache.values().stream()
            .filter(e -> e.getCategory() == category)
            .collect(Collectors.toList());
    }

    public List<String> getEnemyNames() {
        ensureLoaded();
        return enemyCache.values().stream()
            .map(EnemyDefinition::getName)
            .sorted()
            .collect(Collectors.toList());
    }

    public boolean hasEnemy(String name) {
        return getEnemyByName(name) != null;
    }

    private synchronized void ensureLoaded() {
        if (!loaded) {
            loadEnemies();
            loaded = true;
        }
    }

    private void loadEnemies() {
        log.info("Loading enemy data...");
        long startTime = System.currentTimeMillis();

        boolean scrapedEnough = false;

        try {
            int scraped = 0;
            scraped += scrapeBanditPage();
            scraped += scrapeDraugrPage();
            scraped += scrapeDragonPage();

            if (scraped >= 10) {
                scrapedEnough = true;
                log.info("Scraped {} enemies from UESP", scraped);
            }
        } catch (Exception e) {
            log.warn("Enemy scraping failed: {}", e.getMessage());
        }

        if (!scrapedEnough) {
            log.info("Scraping didn't get enough enemies, using fallback data...");
            enemyCache.clear();
        }

        loadFallbackEnemies();

        long elapsed = System.currentTimeMillis() - startTime;
        log.info("Loaded {} total enemies in {}ms", enemyCache.size(), elapsed);
    }

    private int scrapeBanditPage() {
        try {
            Document doc = Jsoup.connect("https://en.uesp.net/wiki/Skyrim:Bandit")
                .timeout(TIMEOUT_MS).userAgent("Mozilla/5.0 (Educational Project)").get();

            Elements tables = doc.select("table.wikitable");
            int count = 0;

            for (Element table : tables) {
                Elements rows = table.select("tr");
                if (rows.size() < 2) continue;

                Element header = rows.first();
                int nameCol = findColumn(header, "name");
                int levelCol = findColumn(header, "level", "lvl");
                int healthCol = findColumn(header, "health");

                if (nameCol == -1 || healthCol == -1) continue;

                for (int i = 1; i < rows.size(); i++) {
                    Elements cells = rows.get(i).select("td");
                    if (cells.size() <= Math.max(nameCol, healthCol)) continue;

                    try {
                        String name = cells.get(nameCol).text().trim();
                        double health = parseNumber(cells.get(healthCol).text());
                        int level = (levelCol != -1 && cells.size() > levelCol)
                            ? (int) parseNumber(cells.get(levelCol).text()) : 1;

                        if (name.isBlank() || health <= 0) continue;

                        String weapon = pickBanditWeapon(name, level);
                        String armor = pickBanditArmor(level);
                        List<Perk> perks = pickBanditPerks(level);

                        addEnemyIfNotExists(new EnemyDefinition(
                            name, level, health, 100, 0,
                            EnemyCategory.BANDIT, weapon, armor, null, perks
                        ));
                        count++;
                    } catch (Exception e) {
                        log.debug("Failed to parse bandit row: {}", e.getMessage());
                    }
                }
            }

            log.info("Scraped {} bandits from UESP", count);
            return count;
        } catch (IOException e) {
            log.warn("Failed to scrape bandit page: {}", e.getMessage());
            return 0;
        }
    }

    private int scrapeDraugrPage() {
        try {
            Document doc = Jsoup.connect("https://en.uesp.net/wiki/Skyrim:Draugr")
                .timeout(TIMEOUT_MS).userAgent("Mozilla/5.0 (Educational Project)").get();

            Elements tables = doc.select("table.wikitable");
            int count = 0;

            for (Element table : tables) {
                Elements rows = table.select("tr");
                if (rows.size() < 2) continue;

                Element header = rows.first();
                int nameCol = findColumn(header, "name", "type");
                int levelCol = findColumn(header, "level", "lvl");
                int healthCol = findColumn(header, "health");

                if (healthCol == -1) continue;
                if (nameCol == -1) nameCol = 0;

                for (int i = 1; i < rows.size(); i++) {
                    Elements cells = rows.get(i).select("td");
                    if (cells.size() <= healthCol) continue;

                    try {
                        String name = cells.get(nameCol).text().trim();
                        double health = parseNumber(cells.get(healthCol).text());
                        int level = (levelCol != -1 && cells.size() > levelCol)
                            ? (int) parseNumber(cells.get(levelCol).text()) : 1;

                        if (name.isBlank() || health <= 0) continue;
                        if (!name.toLowerCase().contains("draugr")) continue;

                        String weapon = pickDraugrWeapon(level);

                        addEnemyIfNotExists(new EnemyDefinition(
                            name, level, health, 100, 0,
                            EnemyCategory.DRAUGR, weapon
                        ));
                        count++;
                    } catch (Exception e) {
                        log.debug("Failed to parse draugr row: {}", e.getMessage());
                    }
                }
            }

            log.info("Scraped {} draugr from UESP", count);
            return count;
        } catch (IOException e) {
            log.warn("Failed to scrape draugr page: {}", e.getMessage());
            return 0;
        }
    }

    private int scrapeDragonPage() {
        try {
            Document doc = Jsoup.connect("https://en.uesp.net/wiki/Skyrim:Dragon")
                .timeout(TIMEOUT_MS).userAgent("Mozilla/5.0 (Educational Project)").get();

            Elements tables = doc.select("table.wikitable");
            int count = 0;

            for (Element table : tables) {
                Elements rows = table.select("tr");
                if (rows.size() < 2) continue;

                Element header = rows.first();
                int nameCol = findColumn(header, "name", "type", "dragon");
                int levelCol = findColumn(header, "level", "lvl");
                int healthCol = findColumn(header, "health");

                if (healthCol == -1) continue;
                if (nameCol == -1) nameCol = 0;

                for (int i = 1; i < rows.size(); i++) {
                    Elements cells = rows.get(i).select("td");
                    if (cells.size() <= healthCol) continue;

                    try {
                        String name = cells.get(nameCol).text().trim();
                        double health = parseNumber(cells.get(healthCol).text());
                        int level = (levelCol != -1 && cells.size() > levelCol)
                            ? (int) parseNumber(cells.get(levelCol).text()) : 10;

                        if (name.isBlank() || health <= 0) continue;

                        String weapon = pickDragonWeapon(name);

                        addEnemyIfNotExists(new EnemyDefinition(
                            name, level, health, 200, 150,
                            EnemyCategory.DRAGON, weapon
                        ));
                        count++;
                    } catch (Exception e) {
                        log.debug("Failed to parse dragon row: {}", e.getMessage());
                    }
                }
            }

            log.info("Scraped {} dragons from UESP", count);
            return count;
        } catch (IOException e) {
            log.warn("Failed to scrape dragon page: {}", e.getMessage());
            return 0;
        }
    }

    private void loadFallbackEnemies() {
        // === BANDITS ===
        addEnemyIfNotExists(new EnemyDefinition("Bandit", 1, 35, 50, 0,
            EnemyCategory.BANDIT, "Iron Sword", "Hide Armor", null, null));
        addEnemyIfNotExists(new EnemyDefinition("Bandit Outlaw", 5, 108, 75, 0,
            EnemyCategory.BANDIT, "Iron War Axe", "Leather Armor", null, null));
        addEnemyIfNotExists(new EnemyDefinition("Bandit Thug", 9, 183, 100, 0,
            EnemyCategory.BANDIT, "Steel Sword", "Leather Armor", null, null));
        addEnemyIfNotExists(new EnemyDefinition("Bandit Highwayman", 14, 283, 100, 0,
            EnemyCategory.BANDIT, "Steel War Axe", "Steel Armor", null,
            List.of(Perk.ARMSMAN_1)));
        addEnemyIfNotExists(new EnemyDefinition("Bandit Chief", 19, 398, 150, 0,
            EnemyCategory.BANDIT, "Orcish Sword", "Steel Armor", "Steel Shield",
            List.of(Perk.ARMSMAN_3)));
        addEnemyIfNotExists(new EnemyDefinition("Bandit Marauder", 25, 489, 200, 0,
            EnemyCategory.BANDIT, "Orcish War Axe", "Dwarven Armor", null,
            List.of(Perk.ARMSMAN_3, Perk.JUGGERNAUT_2)));

        // === DRAUGR ===
        addEnemyIfNotExists(new EnemyDefinition("Draugr", 1, 50, 50, 0,
            EnemyCategory.DRAUGR, "Ancient Nord Sword"));
        addEnemyIfNotExists(new EnemyDefinition("Restless Draugr", 4, 100, 75, 0,
            EnemyCategory.DRAUGR, "Ancient Nord War Axe"));
        addEnemyIfNotExists(new EnemyDefinition("Draugr Wight", 6, 145, 100, 25,
            EnemyCategory.DRAUGR, "Ancient Nord Sword"));
        addEnemyIfNotExists(new EnemyDefinition("Draugr Scourge", 13, 250, 125, 50,
            EnemyCategory.DRAUGR, "Honed Ancient Nord Sword"));
        addEnemyIfNotExists(new EnemyDefinition("Draugr Deathlord", 21, 400, 200, 100,
            EnemyCategory.DRAUGR, "Honed Ancient Nord Greatsword"));
        addEnemyIfNotExists(new EnemyDefinition("Draugr Death Overlord", 30, 600, 250, 150,
            EnemyCategory.DRAUGR, "Honed Ancient Nord Greatsword"));

        // === DRAGONS ===
        addEnemyIfNotExists(new EnemyDefinition("Dragon", 10, 1497, 200, 150,
            EnemyCategory.DRAGON, "Dragon Bite"));
        addEnemyIfNotExists(new EnemyDefinition("Blood Dragon", 18, 2136, 250, 200,
            EnemyCategory.DRAGON, "Dragon Bite"));
        addEnemyIfNotExists(new EnemyDefinition("Frost Dragon", 27, 2855, 300, 250,
            EnemyCategory.DRAGON, "Elder Dragon Bite"));
        addEnemyIfNotExists(new EnemyDefinition("Elder Dragon", 36, 3071, 350, 300,
            EnemyCategory.DRAGON, "Elder Dragon Bite"));
        addEnemyIfNotExists(new EnemyDefinition("Ancient Dragon", 45, 3071, 400, 350,
            EnemyCategory.DRAGON, "Ancient Dragon Bite"));
        addEnemyIfNotExists(new EnemyDefinition("Legendary Dragon", 59, 4163, 500, 400,
            EnemyCategory.DRAGON, "Legendary Dragon Bite"));

        // === FALMER ===
        addEnemyIfNotExists(new EnemyDefinition("Falmer", 1, 77, 75, 25,
            EnemyCategory.FALMER, "Falmer Sword"));
        addEnemyIfNotExists(new EnemyDefinition("Falmer Skulker", 8, 152, 100, 50,
            EnemyCategory.FALMER, "Falmer War Axe"));
        addEnemyIfNotExists(new EnemyDefinition("Falmer Gloomlurker", 14, 252, 125, 75,
            EnemyCategory.FALMER, "Honed Falmer Sword"));
        addEnemyIfNotExists(new EnemyDefinition("Falmer Shadowmaster", 22, 402, 175, 100,
            EnemyCategory.FALMER, "Honed Falmer Sword"));

        // === FORSWORN ===
        addEnemyIfNotExists(new EnemyDefinition("Forsworn", 1, 75, 75, 0,
            EnemyCategory.FORSWORN, "Forsworn Sword"));
        addEnemyIfNotExists(new EnemyDefinition("Forsworn Pillager", 9, 177, 100, 0,
            EnemyCategory.FORSWORN, "Forsworn Axe"));
        addEnemyIfNotExists(new EnemyDefinition("Forsworn Ravager", 19, 350, 150, 0,
            EnemyCategory.FORSWORN, "Forsworn Axe", null, null,
            List.of(Perk.ARMSMAN_2)));
        addEnemyIfNotExists(new EnemyDefinition("Forsworn Briarheart", 24, 461, 200, 100,
            EnemyCategory.FORSWORN, "Forsworn Sword", null, null,
            List.of(Perk.ARMSMAN_3)));

        // === ANIMALS ===
        addEnemyIfNotExists(new EnemyDefinition("Wolf", 2, 36, 50, 0,
            EnemyCategory.ANIMAL, "Wolf Bite"));
        addEnemyIfNotExists(new EnemyDefinition("Ice Wolf", 8, 105, 75, 0,
            EnemyCategory.ANIMAL, "Wolf Bite"));
        addEnemyIfNotExists(new EnemyDefinition("Bear", 6, 175, 100, 0,
            EnemyCategory.ANIMAL, "Bear Claws"));
        addEnemyIfNotExists(new EnemyDefinition("Cave Bear", 12, 275, 125, 0,
            EnemyCategory.ANIMAL, "Bear Claws"));
        addEnemyIfNotExists(new EnemyDefinition("Sabre Cat", 10, 142, 100, 0,
            EnemyCategory.ANIMAL, "Sabre Cat Claws"));
        addEnemyIfNotExists(new EnemyDefinition("Snowy Sabre Cat", 15, 253, 125, 0,
            EnemyCategory.ANIMAL, "Sabre Cat Claws"));
        addEnemyIfNotExists(new EnemyDefinition("Troll", 14, 270, 150, 0,
            EnemyCategory.ANIMAL, "Troll Claws"));
        addEnemyIfNotExists(new EnemyDefinition("Frost Troll", 22, 470, 200, 0,
            EnemyCategory.ANIMAL, "Troll Claws"));
        addEnemyIfNotExists(new EnemyDefinition("Giant", 32, 591, 300, 0,
            EnemyCategory.ANIMAL, "Giant Club"));

        // === DWEMER AUTOMATONS ===
        addEnemyIfNotExists(new EnemyDefinition("Dwarven Spider", 1, 44, 50, 0,
            EnemyCategory.DWEMER_AUTOMATON, "Dwarven Spider Attack"));
        addEnemyIfNotExists(new EnemyDefinition("Dwarven Sphere", 12, 227, 100, 0,
            EnemyCategory.DWEMER_AUTOMATON, "Dwarven Sphere Attack"));
        addEnemyIfNotExists(new EnemyDefinition("Dwarven Centurion", 24, 665, 200, 0,
            EnemyCategory.DWEMER_AUTOMATON, "Dwarven Centurion Hammer"));

        // === DAEDRA ===
        addEnemyIfNotExists(new EnemyDefinition("Flame Atronach", 1, 150, 100, 200,
            EnemyCategory.DAEDRA, "Flame Atronach Attack"));
        addEnemyIfNotExists(new EnemyDefinition("Frost Atronach", 14, 398, 150, 100,
            EnemyCategory.DAEDRA, "Frost Atronach Slam"));
        addEnemyIfNotExists(new EnemyDefinition("Storm Atronach", 30, 541, 200, 150,
            EnemyCategory.DAEDRA, "Storm Atronach Strike"));
        addEnemyIfNotExists(new EnemyDefinition("Dremora", 19, 345, 200, 100,
            EnemyCategory.DAEDRA, "Dremora Greatsword", "Daedric Armor", null,
            List.of(Perk.BARBARIAN_3)));
    }

    private void addEnemyIfNotExists(EnemyDefinition enemy) {
        String key = enemy.getName().toLowerCase();
        if (!enemyCache.containsKey(key)) {
            enemyCache.put(key, enemy);
        }
    }

    private int findColumn(Element headerRow, String... keywords) {
        Elements headers = headerRow.select("th");
        for (int i = 0; i < headers.size(); i++) {
            String text = headers.get(i).text().trim().toLowerCase();
            for (String keyword : keywords) {
                if (text.contains(keyword.toLowerCase())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private double parseNumber(String text) {
        if (text == null || text.isBlank()) return 0;
        String cleaned = text.replaceAll("[^0-9.]", "");
        if (cleaned.isEmpty()) return 0;
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private String pickBanditWeapon(String name, int level) {
        if (level >= 20) return "Orcish Sword";
        if (level >= 10) return "Steel Sword";
        return "Iron Sword";
    }

    private String pickBanditArmor(int level) {
        if (level >= 15) return "Steel Armor";
        if (level >= 5) return "Leather Armor";
        return "Hide Armor";
    }

    private List<Perk> pickBanditPerks(int level) {
        List<Perk> perks = new ArrayList<>();
        if (level >= 25) {
            perks.add(Perk.ARMSMAN_3);
            perks.add(Perk.JUGGERNAUT_2);
        } else if (level >= 15) {
            perks.add(Perk.ARMSMAN_2);
        } else if (level >= 10) {
            perks.add(Perk.ARMSMAN_1);
        }
        return perks;
    }

    private String pickDraugrWeapon(int level) {
        if (level >= 15) return "Honed Ancient Nord Greatsword";
        if (level >= 6) return "Honed Ancient Nord Sword";
        return "Ancient Nord Sword";
    }

    private String pickDragonWeapon(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("legendary")) return "Legendary Dragon Bite";
        if (lower.contains("ancient")) return "Ancient Dragon Bite";
        if (lower.contains("elder") || lower.contains("frost")) return "Elder Dragon Bite";
        return "Dragon Bite";
    }
}

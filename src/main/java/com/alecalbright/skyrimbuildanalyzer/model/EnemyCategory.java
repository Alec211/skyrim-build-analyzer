package com.alecalbright.skyrimbuildanalyzer.model;

public enum EnemyCategory {
    BANDIT("Bandit"),
    DRAUGR("Draugr"),
    DRAGON("Dragon"),
    FALMER("Falmer"),
    FORSWORN("Forsworn"),
    ANIMAL("Animal"),
    DWEMER_AUTOMATON("Dwemer Automaton"),
    DAEDRA("Daedra");

    private final String displayName;

    EnemyCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

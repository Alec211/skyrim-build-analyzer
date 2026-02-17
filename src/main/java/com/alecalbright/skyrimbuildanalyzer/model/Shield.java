package com.alecalbright.skyrimbuildanalyzer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Shield {
    private final String name;
    private final double baseArmorRating;
    private final ArmorWeight weight;
}

package com.alecalbright.skyrimbuildanalyzer.model;

import java.time.LocalDateTime;

public record CombatEvent(
    String attackerName,
    String defenderName,
    double damageDealt,
    String weaponUsed,
    boolean wasCritical,
    boolean wasSneakAttack,
    LocalDateTime timeStamp
){
    public CombatEvent{
        if(attackerName == null || attackerName.isBlank()){
            throw new IllegalArgumentException("Attacker name can't be null or blank");
        }
        if(defenderName == null || defenderName.isBlank()){
            throw new IllegalArgumentException("Defender name can't be null or blank");
        }
        if(damageDealt < 0){
            throw new IllegalArgumentException("Damage can't be negative");
        }
        if(timeStamp == null){
            throw new IllegalArgumentException("Timestamp can't be null");
        }
    }

    public static CombatEvent now(String attackerName, String defenderName, double damageDealt, String weaponUsed, boolean wasCritical, boolean wasSneakAttack){
        return new CombatEvent(attackerName, defenderName, damageDealt, weaponUsed, wasCritical, wasSneakAttack, LocalDateTime.now());
    }

    public boolean isSpecialAttack(){
        return wasCritical || wasSneakAttack;
    }

    public String getDescription(){
        StringBuilder desc = new StringBuilder();
        desc.append(attackerName).append(" attacks ").append(defenderName);
        desc.append(" with ").append(weaponUsed);
        desc.append(" for ").append(String.format("%.1f", damageDealt)).append(" damage");

        if(wasCritical && wasSneakAttack){
            desc.append(" (CRITICAL SNEAK ATTACK!)");
        }
        else if(wasCritical){
            desc.append(" (CRITICAL HIT!)");
        }
        else if(wasSneakAttack){
            desc.append(" (Sneak Attack)");
        }

        return desc.toString();
    }

    @Override
    public String toString(){
        return getDescription();
    }
}

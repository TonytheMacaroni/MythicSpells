package com.tonythemacaroni.mythicspells.conditions;

import java.util.Set;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAPIHelper;

import com.nisovin.magicspells.castmodifiers.Condition;

public class FactionCondition extends Condition {

    private Set<String> factions;

    @Override
    public boolean initialize(String var) {
        if (var == null || var.isEmpty()) return false;

        factions = new HashSet<>();

        String[] split = var.split(",");
        for (String type : split) {
            type = type.trim();
            if (!type.isEmpty()) factions.add(type);
        }

        return !factions.isEmpty();
    }

    @Override
    public boolean check(LivingEntity caster) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.isMythicMob(caster)) return false;

        ActiveMob mob = helper.getMythicMobInstance(caster);
        return factions.contains(mob.getFaction());
    }

    @Override
    public boolean check(LivingEntity caster, LivingEntity target) {
        return check(target);
    }

    @Override
    public boolean check(LivingEntity caster, Location location) {
        return false;
    }

}

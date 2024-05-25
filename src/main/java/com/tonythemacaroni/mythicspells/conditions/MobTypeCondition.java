package com.tonythemacaroni.mythicspells.conditions;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAPIHelper;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.castmodifiers.Condition;

@Name("mobtype")
public class MobTypeCondition extends Condition {

    private Set<String> mythicTypes;

    @Override
    public boolean initialize(@NotNull String var) {
        if (var.isEmpty()) return false;

        mythicTypes = new HashSet<>();

        String[] split = var.split(",");
        for (String type : split) {
            type = type.trim();
            if (!type.isEmpty()) mythicTypes.add(type);
        }

        return !mythicTypes.isEmpty();
    }

    @Override
    public boolean check(LivingEntity caster) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.isMythicMob(caster)) return false;

        ActiveMob mob = helper.getMythicMobInstance(caster);
        return mythicTypes.contains(mob.getMobType());
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

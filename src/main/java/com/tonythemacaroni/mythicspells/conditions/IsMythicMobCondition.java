package com.tonythemacaroni.mythicspells.conditions;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.bukkit.MythicBukkit;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.castmodifiers.Condition;

@Name("ismythicmob")
public class IsMythicMobCondition extends Condition {

    @Override
    public boolean initialize(@NotNull String var) {
        return true;
    }

    @Override
    public boolean check(LivingEntity caster) {
        return MythicBukkit.inst().getAPIHelper().isMythicMob(caster);
    }

    @Override
    public boolean check(LivingEntity caster, LivingEntity target) {
        return MythicBukkit.inst().getAPIHelper().isMythicMob(target);
    }

    @Override
    public boolean check(LivingEntity caster, Location location) {
        return false;
    }

}

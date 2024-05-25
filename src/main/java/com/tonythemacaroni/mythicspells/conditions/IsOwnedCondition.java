package com.tonythemacaroni.mythicspells.conditions;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.BukkitAPIHelper;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.castmodifiers.Condition;

@Name("isowned")
public class IsOwnedCondition extends Condition {

    @Override
    public boolean initialize(@NotNull String var) {
        return true;
    }

    @Override
    public boolean check(LivingEntity caster) {
        return false;
    }

    @Override
    public boolean check(LivingEntity caster, LivingEntity target) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.isMythicMob(target)) return false;

        ActiveMob mob = helper.getMythicMobInstance(target);
        UUID owner = mob.getOwner().orElse(null);

        return caster.getUniqueId().equals(owner);
    }

    @Override
    public boolean check(LivingEntity caster, Location location) {
        return false;
    }

}

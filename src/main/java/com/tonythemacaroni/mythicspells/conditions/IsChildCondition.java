package com.tonythemacaroni.mythicspells.conditions;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.api.adapters.AbstractEntity;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.castmodifiers.Condition;

@Name("ischild")
public class IsChildCondition extends Condition {

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
        if (!helper.isMythicMob(caster)) return false;

        UUID targetUUID = target.getUniqueId();

        ActiveMob mob = helper.getMythicMobInstance(caster);
        for (AbstractEntity child : mob.getChildren())
            if (targetUUID.equals(child.getBukkitEntity().getUniqueId()))
                return true;

        return false;
    }

    @Override
    public boolean check(LivingEntity caster, Location location) {
        return false;
    }

}

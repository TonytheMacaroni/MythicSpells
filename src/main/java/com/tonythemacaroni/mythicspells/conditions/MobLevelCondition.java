package com.tonythemacaroni.mythicspells.conditions;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.BukkitAPIHelper;

import com.nisovin.magicspells.handlers.DebugHandler;
import com.nisovin.magicspells.castmodifiers.conditions.util.OperatorCondition;

public class MobLevelCondition extends OperatorCondition {

    private double level;

    @Override
    public boolean initialize(String var) {
        if (var.length() < 2 || !super.initialize(var)) return false;

        try {
            level = Double.parseDouble(var.substring(1));
            return true;
        } catch (NumberFormatException e) {
            DebugHandler.debugNumberFormat(e);
            return false;
        }
    }

    @Override
    public boolean check(LivingEntity caster) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.isMythicMob(caster)) return false;

        ActiveMob mob = helper.getMythicMobInstance(caster);
        double mobLevel = mob.getLevel();

        if (equals) return mobLevel == level;
        else if (moreThan) return mobLevel > level;
        else if (lessThan) return mobLevel < level;
        return false;
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

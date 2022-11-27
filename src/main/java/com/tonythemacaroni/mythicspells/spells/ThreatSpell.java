package com.tonythemacaroni.mythicspells.spells;

import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAPIHelper;

import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.util.ValidTargetChecker;
import com.nisovin.magicspells.spells.TargetedEntitySpell;

public class ThreatSpell extends TargetedSpell implements TargetedEntitySpell {

    private static final ValidTargetChecker THREAT_TABLE_CHECKER = entity -> {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.isMythicMob(entity)) return false;

        ActiveMob mob = helper.getMythicMobInstance(entity);
        return mob.hasThreatTable();
    };

    private final ConfigData<Double> threat;

    private final ConfigData<Boolean> powerAffectsThreat;

    public ThreatSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        threat = getConfigDataDouble("threat", 0);

        powerAffectsThreat = getConfigDataBoolean("power-affects-threat", true);
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            TargetInfo<LivingEntity> targetInfo = getTargetedEntity(caster, power, THREAT_TABLE_CHECKER, args);
            if (targetInfo.noTarget()) return noTarget(caster, args, targetInfo);

            LivingEntity target = targetInfo.target();
            power = targetInfo.power();

            if (!threat(caster, target, power, args)) return noTarget(caster, args);

            sendMessages(caster, target, args);
            return PostCastAction.NO_MESSAGES;
        }

        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power, String[] args) {
        if (!validTargetList.canTarget(caster, target) || !THREAT_TABLE_CHECKER.isValidTarget(target)) return false;
        return threat(caster, target, power, args);
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
        if (!validTargetList.canTarget(caster, target) || !THREAT_TABLE_CHECKER.isValidTarget(target)) return false;
        return threat(caster, target, power, null);
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float power, String[] args) {
        if (!validTargetList.canTarget(target) || !THREAT_TABLE_CHECKER.isValidTarget(target)) return false;
        return threat(null, target, power, args);
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float power) {
        if (!validTargetList.canTarget(target) || !THREAT_TABLE_CHECKER.isValidTarget(target)) return false;
        return threat(null, target, power, null);
    }

    private boolean threat(LivingEntity caster, LivingEntity target, float power, String[] args) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();

        double threat = this.threat.get(caster, target, power, args);
        if (powerAffectsThreat.get(caster, target, power, args)) threat *= power;

        boolean success;
        if (threat >= 0) success = helper.addThreat(caster, target, threat);
        else success = helper.reduceThreat(caster, target, -threat);
        if (!success) return false;

        playSpellEffects(caster, target, power, args);

        return true;
    }

}

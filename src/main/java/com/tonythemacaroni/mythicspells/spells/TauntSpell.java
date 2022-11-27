package com.tonythemacaroni.mythicspells.spells;

import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAPIHelper;

import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.ValidTargetChecker;
import com.nisovin.magicspells.spells.TargetedEntitySpell;

public class TauntSpell extends TargetedSpell implements TargetedEntitySpell {

    private static final ValidTargetChecker THREAT_TABLE_CHECKER = entity -> {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.isMythicMob(entity)) return false;

        ActiveMob mob = helper.getMythicMobInstance(entity);
        return mob.hasThreatTable();
    };

    public TauntSpell(MagicConfig config, String spellName) {
        super(config, spellName);
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            TargetInfo<LivingEntity> targetInfo = getTargetedEntity(caster, power, THREAT_TABLE_CHECKER, args);
            if (targetInfo.noTarget()) return noTarget(caster, args, targetInfo);

            LivingEntity target = targetInfo.target();
            power = targetInfo.power();

            if (!taunt(caster, target, power, args)) return noTarget(caster, args);

            sendMessages(caster, target, args);
            return PostCastAction.NO_MESSAGES;
        }

        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power, String[] args) {
        return taunt(caster, target, power, args);
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
        return taunt(caster, target, power, null);
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float power, String[] args) {
        return taunt(null, target, power, args);
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float power) {
        return taunt(null, target, power, null);
    }

    private boolean taunt(LivingEntity caster, LivingEntity target, float power, String[] args) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.taunt(caster, target)) return false;

        playSpellEffects(caster, target, power, args);
        return true;
    }

}

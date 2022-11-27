package com.tonythemacaroni.mythicspells.spells;

import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.BukkitAPIHelper;

import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.util.ValidTargetChecker;
import com.nisovin.magicspells.spells.TargetedEntitySpell;

public class SignalSpell extends TargetedSpell implements TargetedEntitySpell {

    private static final ValidTargetChecker MYTHIC_MOB_CHECKER = entity -> {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        return helper.isMythicMob(entity);
    };

    private final ConfigData<String> signal;

    public SignalSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        signal = getConfigDataString("signal", "");
    }

    @Override
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            TargetInfo<LivingEntity> targetInfo = getTargetedEntity(caster, power, MYTHIC_MOB_CHECKER, args);
            if (targetInfo.noTarget()) return noTarget(caster, args, targetInfo);

            LivingEntity target = targetInfo.target();
            power = targetInfo.power();

            if (!signal(caster, target, power, args)) return noTarget(caster, args);

            sendMessages(caster, target, args);
            return PostCastAction.NO_MESSAGES;
        }

        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power, String[] args) {
        return signal(caster, target, power, args);
    }

    @Override
    public boolean castAtEntity(LivingEntity caster, LivingEntity target, float power) {
        return signal(caster, target, power, null);
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float power, String[] args) {
        return signal(null, target, power, args);
    }

    @Override
    public boolean castAtEntity(LivingEntity target, float power) {
        return signal(null, target, power, null);
    }

    private boolean signal(LivingEntity caster, LivingEntity target, float power, String[] args) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.isMythicMob(target)) return false;

        ActiveMob mob = helper.getMythicMobInstance(target);
        mob.signalMob(BukkitAdapter.adapt(caster), signal.get(caster, target, power, args));

        playSpellEffects(caster, target, power, args);
        return true;
    }

}

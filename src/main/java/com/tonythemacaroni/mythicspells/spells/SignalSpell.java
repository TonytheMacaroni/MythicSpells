package com.tonythemacaroni.mythicspells.spells;

import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.BukkitAPIHelper;

import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.CastResult;
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

    private final String strCantSignal;

    public SignalSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        signal = getConfigDataString("signal", "");

        strCantSignal = getConfigString("str-cant-signal", "");
    }

    @Override
    public CastResult cast(SpellData data) {
        TargetInfo<LivingEntity> info = getTargetedEntity(data, MYTHIC_MOB_CHECKER);
        if (info.noTarget()) return noTarget(info);

        return castAtEntity(info.spellData());
    }

    @Override
    public CastResult castAtEntity(SpellData data) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.isMythicMob(data.target())) return noTarget(strCantSignal, data);

        ActiveMob mob = helper.getMythicMobInstance(data.target());
        mob.signalMob(BukkitAdapter.adapt(data.caster()), signal.get(data));

        playSpellEffects(data);
        return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
    }

}

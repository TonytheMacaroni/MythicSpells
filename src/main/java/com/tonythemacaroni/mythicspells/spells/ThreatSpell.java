package com.tonythemacaroni.mythicspells.spells;

import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAPIHelper;

import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.CastResult;
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

    private final String strCantThreaten;

    public ThreatSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        threat = getConfigDataDouble("threat", 0);

        powerAffectsThreat = getConfigDataBoolean("power-affects-threat", true);

        strCantThreaten = getConfigString("str-cant-threaten", "");
    }

    @Override
    public CastResult cast(SpellData data) {
        TargetInfo<LivingEntity> info = getTargetedEntity(data, THREAT_TABLE_CHECKER);
        if (info.noTarget()) return noTarget(info);

        return castAtEntity(info.spellData());
    }

    @Override
    public CastResult castAtEntity(SpellData data) {
        if (!data.hasCaster()) return new CastResult(PostCastAction.ALREADY_HANDLED, data);

        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();

        double threat = this.threat.get(data);
        if (powerAffectsThreat.get(data)) threat *= data.power();

        boolean success;
        if (threat >= 0) success = helper.addThreat(data.target(), data.caster(), threat);
        else success = helper.reduceThreat(data.target(), data.caster(), -threat);

        if (!success) return noTarget(strCantThreaten, data);

        playSpellEffects(data);
        return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
    }

    @Override
    public ValidTargetChecker getValidTargetChecker() {
        return THREAT_TABLE_CHECKER;
    }

}

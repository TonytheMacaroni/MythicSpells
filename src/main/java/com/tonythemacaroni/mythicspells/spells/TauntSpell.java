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
import com.nisovin.magicspells.util.ValidTargetChecker;
import com.nisovin.magicspells.spells.TargetedEntitySpell;

public class TauntSpell extends TargetedSpell implements TargetedEntitySpell {

    private static final ValidTargetChecker THREAT_TABLE_CHECKER = entity -> {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.isMythicMob(entity)) return false;

        ActiveMob mob = helper.getMythicMobInstance(entity);
        return mob.hasThreatTable();
    };

    private final String strCantTaunt;

    public TauntSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        strCantTaunt = getConfigString("str-cant-taunt", "");
    }

    @Override
    public CastResult cast(SpellData data) {
        TargetInfo<LivingEntity> info = getTargetedEntity(data, THREAT_TABLE_CHECKER);
        if (info.noTarget()) return noTarget(info);

        return castAtEntity(info.spellData());
    }

    @Override
    public CastResult castAtEntity(SpellData data) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (!helper.taunt(data.target(), data.caster())) return noTarget(strCantTaunt, data);

        playSpellEffects(data);
        return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
    }

}

package com.tonythemacaroni.mythicspells.spells;

import java.util.Collection;
import java.util.Collections;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.BukkitAPIHelper;

import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.CastResult;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spells.TargetedEntitySpell;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;
import com.nisovin.magicspells.spells.TargetedEntityFromLocationSpell;

public class CastSkillSpell extends TargetedSpell implements TargetedEntitySpell, TargetedLocationSpell, TargetedEntityFromLocationSpell {

    private final ConfigData<Boolean> requireEntityTarget;
    private final ConfigData<Boolean> pointBlank;
    private final ConfigData<Boolean> passPower;
    private final ConfigData<Boolean> targeted;

    private final ConfigData<Double> yOffset;

    private final ConfigData<String> skill;

    public CastSkillSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        targeted = getConfigDataBoolean("targeted", true);
        passPower = getConfigDataBoolean("pass-power", true);
        pointBlank = getConfigDataBoolean("point-blank", false);
        requireEntityTarget = getConfigDataBoolean("require-entity-target", false);

        yOffset = getConfigDataDouble("y-offset", 0);

        skill = getConfigDataString("skill", null);
    }

    @Override
    public CastResult cast(SpellData data) {
        if (targeted.get(data)) {
            if (requireEntityTarget.get(data)) {
                TargetInfo<LivingEntity> info = getTargetedEntity(data);
                if (info.noTarget()) return noTarget(info);
                data = info.spellData();
            } else {
                if (pointBlank.get(data)) {
                    SpellTargetLocationEvent targetEvent = new SpellTargetLocationEvent(this, data, data.caster().getLocation());
                    if (!targetEvent.callEvent()) return noTarget(targetEvent);
                    data = targetEvent.getSpellData();
                } else {
                    TargetInfo<Location> info = getTargetedLocation(data);
                    if (info.noTarget()) return noTarget(info);
                    data = info.spellData();
                }

                data = data.location(data.location().add(0, yOffset.get(data), 0));
            }
        }

        return castSkill(data);
    }

    @Override
    public CastResult castAtLocation(SpellData data) {
        return castSkill(data.location(data.location().add(0, yOffset.get(data), 0)));
    }

    @Override
    public CastResult castAtEntity(SpellData data) {
        return castSkill(data);
    }

    @Override
    public CastResult castAtEntityFromLocation(SpellData data) {
        return castSkill(data.location(data.location().add(0, yOffset.get(data), 0)));
    }

    private CastResult castSkill(SpellData data) {
        String skill = this.skill.get(data);
        if (skill == null) return noTarget(data);

        Entity caster = data.caster();
        Location origin = data.hasLocation() && data.hasTarget() ? data.location() : null;
        Collection<Entity> eTargets = data.hasTarget() ? Collections.singleton(data.target()) : null;
        Collection<Location> lTargets = data.hasLocation() && !data.hasTarget() ? Collections.singleton(data.location()) : null;
        float power = passPower.get(data) ? data.power() : 1f;

        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        if (helper.castSkill(caster, skill, origin, eTargets, lTargets, power)) {
            playSpellEffects(data);
            return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
        }

        return noTarget(data);
    }

}

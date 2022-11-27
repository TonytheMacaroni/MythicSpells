package com.tonythemacaroni.mythicspells.mechanics;

import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.Subspell;

import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.core.skills.mechanics.CustomMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;

@MythicMechanic(name = "spell", description = "Cast a MagicSpell spell.")
public class SpellMechanic extends SkillMechanic implements INoTargetSkill, ITargetedEntitySkill, ITargetedLocationSkill {

    private final boolean passTargeting;
    private final boolean requireTarget;
    private final Subspell spell;
    private boolean invalid;

    public SpellMechanic(CustomMechanic parent) {
        super(parent.getManager(), parent.getFile(), parent.getConfig().getLine(), parent.getConfig(), parent.getTimerInterval());

        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY;

        spell = new Subspell(config.getPlaceholderString(new String[]{"spell", "s"}, "").get());
        invalid = !spell.process();

        passTargeting = config.getBoolean(new String[]{"passtargeting", "pt"}, true);
        requireTarget = config.getBoolean(new String[]{"requiretarget", "rt"}, parent.getTargeter().isPresent());
    }

    public void reprocess() {
        invalid = !spell.process();
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        if (invalid) return SkillResult.INVALID_CONFIG;
        if (requireTarget) return SkillResult.CONDITION_FAILED;

        if (!(BukkitAdapter.adapt(data.getCaster().getEntity()) instanceof LivingEntity livingCaster))
            return SkillResult.INVALID_TARGET;

        spell.cast(livingCaster, data.getPower());

        return SkillResult.SUCCESS;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (invalid) return SkillResult.INVALID_CONFIG;

        if (!(BukkitAdapter.adapt(data.getCaster().getEntity()) instanceof LivingEntity livingCaster))
            return SkillResult.INVALID_TARGET;

        LivingEntity livingTarget = BukkitAdapter.adapt(target) instanceof LivingEntity le ? le : null;

        if (spell.isTargetedEntityFromLocationSpell()) {
            if (livingTarget == null) return SkillResult.INVALID_TARGET;
            spell.castAtEntityFromLocation(livingCaster, BukkitAdapter.adapt(data.getOrigin()), livingTarget, data.getPower(), passTargeting);
        } else if (spell.isTargetedEntitySpell()) {
            if (livingTarget == null) return SkillResult.INVALID_TARGET;
            spell.castAtEntity(livingCaster, livingTarget, data.getPower(), passTargeting);
        } else if (spell.isTargetedLocationSpell()) {
            if (livingTarget == null) return SkillResult.INVALID_TARGET;
            spell.castAtLocation(livingCaster, livingTarget.getLocation(), data.getPower());
        } else spell.cast(livingCaster, data.getPower());

        return SkillResult.SUCCESS;
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        if (invalid) return SkillResult.INVALID_CONFIG;

        if (!(BukkitAdapter.adapt(data.getCaster().getEntity()) instanceof LivingEntity livingCaster))
            return SkillResult.INVALID_TARGET;

        if (spell.isTargetedLocationSpell())
            spell.castAtLocation(livingCaster, BukkitAdapter.adapt(target), data.getPower());
        else spell.cast(livingCaster, data.getPower());

        return SkillResult.SUCCESS;
    }

}

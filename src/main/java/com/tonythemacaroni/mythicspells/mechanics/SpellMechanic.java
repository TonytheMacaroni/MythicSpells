package com.tonythemacaroni.mythicspells.mechanics;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.util.SpellData;

import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.INoTargetSkill;
import io.lumine.mythic.core.skills.SkillMechanic;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.bukkit.utils.serialize.Optl;
import io.lumine.mythic.api.skills.ThreadSafetyLevel;
import io.lumine.mythic.api.adapters.AbstractLocation;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.ITargetedLocationSkill;
import io.lumine.mythic.core.skills.mechanics.CustomMechanic;
import io.lumine.mythic.core.utils.annotations.MythicMechanic;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;

@MythicMechanic(name = "spell", description = "Cast a MagicSpell spell.")
public class SpellMechanic extends SkillMechanic implements INoTargetSkill, ITargetedEntitySkill, ITargetedLocationSkill {

    private final PlaceholderString spellPlaceholder;
    private final boolean passTargeting;
    private final boolean requireTarget;
    private final boolean castAsParent;
    private final boolean castAsOwner;
    private final boolean passPower;
    private final Subspell spell;
    private boolean invalid;

    public SpellMechanic(CustomMechanic parent) {
        super(parent.getManager(), parent.getFile(), parent.getConfig().getLine(), parent.getConfig(), parent.getTimerInterval());

        threadSafetyLevel = ThreadSafetyLevel.SYNC_ONLY;

        PlaceholderString placeholder = config.getPlaceholderString(new String[]{"spell", "s"}, "");
        if (placeholder.isStatic()) {
            spell = new Subspell(placeholder.get());
            spellPlaceholder = null;
            invalid = !spell.process();
        } else {
            spell = null;
            spellPlaceholder = placeholder;
            invalid = false;
        }

        passPower = config.getBoolean(new String[]{"passpower", "pp"}, true);
        castAsOwner = config.getBoolean(new String[]{"castasowner", "cao"}, false);
        castAsParent = config.getBoolean(new String[]{"castasparent", "cap"}, false);
        passTargeting = config.getBoolean(new String[]{"passtargeting", "pt"}, false);
        requireTarget = config.getBoolean(new String[]{"requiretarget", "rt"}, parent.getTargeter().isPresent());
    }

    public void reprocess() {
        if (spell != null) invalid = !spell.process();
    }

    @Override
    public SkillResult cast(SkillMetadata data) {
        if (invalid) return SkillResult.INVALID_CONFIG;
        if (requireTarget) return SkillResult.CONDITION_FAILED;

        Subspell spell = this.spell;
        if (spellPlaceholder != null) {
            spell = new Subspell(spellPlaceholder.get(data));
            if (!spell.process()) return SkillResult.INVALID_CONFIG;
        }

        Entity caster = getCaster(data);
        if (caster == null) return SkillResult.CONDITION_FAILED;
        if (!(caster instanceof LivingEntity livingCaster)) return SkillResult.INVALID_TARGET;

        SpellData spellData = new SpellData(livingCaster, passPower ? data.getPower() : 1f, null);
        spell.subcast(spellData, passTargeting);

        return SkillResult.SUCCESS;
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (invalid) return SkillResult.INVALID_CONFIG;

        Subspell spell = this.spell;
        if (spellPlaceholder != null) {
            spell = new Subspell(spellPlaceholder.get(data, target));
            if (!spell.process()) return SkillResult.INVALID_CONFIG;
        }

        Entity caster = getCaster(data);
        if (caster == null) return SkillResult.CONDITION_FAILED;

        if (!(BukkitAdapter.adapt(target) instanceof LivingEntity livingTarget)) return SkillResult.INVALID_TARGET;

        LivingEntity livingCaster = caster instanceof LivingEntity le ? le : null;

        SpellData spellData = new SpellData(livingCaster, livingTarget, BukkitAdapter.adapt(data.getOrigin()), passPower ? data.getPower() : 1f, null);
        spell.subcast(spellData, passTargeting);

        return SkillResult.SUCCESS;
    }

    @Override
    public SkillResult castAtLocation(SkillMetadata data, AbstractLocation target) {
        if (invalid) return SkillResult.INVALID_CONFIG;

        Subspell spell = this.spell;
        if (spellPlaceholder != null) {
            spell = new Subspell(spellPlaceholder.get(data));
            if (!spell.process()) return SkillResult.INVALID_CONFIG;
        }

        Entity caster = getCaster(data);
        if (caster == null) return SkillResult.CONDITION_FAILED;

        LivingEntity livingCaster = caster instanceof LivingEntity le ? le : null;

        SpellData spellData = new SpellData(livingCaster, BukkitAdapter.adapt(target), passPower ? data.getPower() : 1f, null);
        spell.subcast(spellData, passTargeting);

        return SkillResult.SUCCESS;
    }

    private Entity getCaster(SkillMetadata data) {
        if (castAsOwner) {
            if (!(data.getCaster() instanceof ActiveMob mob)) return null;

            Optl<UUID> owner = mob.getOwnerUUID();
            if (!owner.isPresent()) return null;

            return Bukkit.getEntity(owner.get());
        }

        if (castAsParent) {
            if (!(data.getCaster() instanceof ActiveMob mob)) return null;

            Optl<AbstractEntity> parent = mob.getParent();
            if (!parent.isPresent()) return null;

            return BukkitAdapter.adapt(parent.get());
        }

        return BukkitAdapter.adapt(data.getCaster().getEntity());
    }

}

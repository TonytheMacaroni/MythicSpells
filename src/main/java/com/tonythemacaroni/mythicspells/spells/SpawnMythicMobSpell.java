package com.tonythemacaroni.mythicspells.spells;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.LivingEntity;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.api.mobs.entities.SpawnReason;

import com.nisovin.magicspells.Subspell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.CastResult;
import com.nisovin.magicspells.util.TargetInfo;
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;
import com.nisovin.magicspells.events.SpellTargetLocationEvent;

public class SpawnMythicMobSpell extends TargetedSpell implements TargetedLocationSpell {

    private final ConfigData<String> type;

    private final ConfigData<Double> yOffset;
    private final ConfigData<Double> level;

    private final ConfigData<Boolean> powerAffectsLevel;
    private final ConfigData<Boolean> pointBlank;
    private final ConfigData<Boolean> spawnInAir;
    private final ConfigData<Boolean> owned;
    private final ConfigData<Boolean> tamed;

    private final String strCantSpawn;

    private String spellOnSpawnName;
    private Subspell spellOnSpawn;

    public SpawnMythicMobSpell(MagicConfig config, String spellName) {
        super(config, spellName);

        type = getConfigDataString("type", null);

        yOffset = getConfigDataDouble("y-offset", 1);
        level = getConfigDataDouble("level", 1);

        powerAffectsLevel = getConfigDataBoolean("power-affects-level", true);
        pointBlank = getConfigDataBoolean("point-blank", false);
        spawnInAir = getConfigDataBoolean("spawn-in-air", false);
        owned = getConfigDataBoolean("owned", false);
        tamed = getConfigDataBoolean("tamed", false);

        spellOnSpawnName = getConfigString("spell-on-spawn", null);
        strCantSpawn = getConfigString("str-cant-spawn", "");
    }

    @Override
    public void initialize() {
        super.initialize();

        if (spellOnSpawnName != null) {
            spellOnSpawn = new Subspell(spellOnSpawnName);

            if (!spellOnSpawn.process()) {
                MagicSpells.error("SpawnMythicMobSpell '" + internalName + "' has an invalid spell-on-spawn '" + spellOnSpawnName + "' defined!");
                spellOnSpawn = null;
            }

            spellOnSpawnName = null;
        }
    }

    @Override
    public CastResult cast(SpellData data) {
        if (pointBlank.get(data)) {
            SpellTargetLocationEvent targetEvent = new SpellTargetLocationEvent(this, data, data.caster().getLocation());
            if (!targetEvent.callEvent()) return noTarget(targetEvent);
            data = targetEvent.getSpellData();
        } else {
            TargetInfo<Location> info = getTargetedBlockLocation(data, spawnInAir.get(data));
            if (info.noTarget()) return noTarget(data);
            data = info.spellData();
        }

        return castAtLocation(data);
    }

    @Override
    public CastResult castAtLocation(SpellData data) {
        String mobType = type.get(data);
        if (mobType == null) return noTarget(strCantSpawn, data);

        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        MythicMob type = helper.getMythicMob(mobType);
        if (type == null) return noTarget(strCantSpawn, data);

        Location location = data.location();

        location.add(0, yOffset.get(data), 0);
        data = data.location(location);

        double level = this.level.get(data);
        if (powerAffectsLevel.get(data)) level *= data.power();

        ActiveMob am = type.spawn(BukkitAdapter.adapt(location), level, SpawnReason.OTHER);
        Entity mob = am.getEntity().getBukkitEntity();

        if (tamed.get(data) && mob instanceof Tameable tameable && data.caster() instanceof AnimalTamer tamer)
            tameable.setOwner(tamer);

        if (data.hasCaster() && owned.get(data))
            am.setOwnerUUID(data.caster().getUniqueId());

        LivingEntity livingMob = mob instanceof LivingEntity le ? le : null;
        data = data.target(livingMob);

        if (spellOnSpawn != null) spellOnSpawn.subcast(data.noLocation());

        if (data.hasCaster()) playSpellEffects(data.caster(), mob, data);
        else playSpellEffects(EffectPosition.TARGET, mob, data);

        return new CastResult(PostCastAction.HANDLE_NORMALLY, data);
    }

}

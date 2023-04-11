package com.tonythemacaroni.mythicspells.spells;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
import com.nisovin.magicspells.util.MagicConfig;
import com.nisovin.magicspells.spells.TargetedSpell;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.spelleffects.EffectPosition;
import com.nisovin.magicspells.spells.TargetedLocationSpell;

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
    public PostCastAction castSpell(LivingEntity caster, SpellCastState state, float power, String[] args) {
        if (state == SpellCastState.NORMAL) {
            boolean spawnInAir = this.spawnInAir.get(caster, null, power, args);

            Location target;
            if (pointBlank.get(caster, null, power, args)) {
                target = caster.getLocation();
                if (!spawnInAir && target.getBlock().getType().isAir()) return noTarget(caster, args);
            } else {
                Block block = getTargetedBlock(caster, power, args);
                if (block == null || (!spawnInAir && block.getType().isAir())) return noTarget(caster, args);

                target = block.getLocation();
            }

            if (!spawnMob(caster, target, power, args)) return noTarget(caster, strCantSpawn, args);
        }

        return PostCastAction.HANDLE_NORMALLY;
    }

    @Override
    public boolean castAtLocation(LivingEntity caster, Location target, float power, String[] args) {
        return spawnMob(caster, target, power, args);
    }

    @Override
    public boolean castAtLocation(LivingEntity caster, Location target, float power) {
        return spawnMob(caster, target, power, null);
    }

    @Override
    public boolean castAtLocation(Location target, float power, String[] args) {
        return spawnMob(null, target, power, args);
    }

    @Override
    public boolean castAtLocation(Location target, float power) {
        return spawnMob(null, target, power, null);
    }

    private boolean spawnMob(LivingEntity caster, Location target, float power, String[] args) {
        String mobType = type.get(caster, null, power, args);
        if (mobType == null) return false;

        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();
        MythicMob type = helper.getMythicMob(mobType);
        if (type == null) return false;

        target.add(0, yOffset.get(caster, null, power, args), 0);

        double level = this.level.get(caster, null, power, args);
        if (powerAffectsLevel.get(caster, null, power, args)) level *= power;

        ActiveMob am = type.spawn(BukkitAdapter.adapt(target), level, SpawnReason.OTHER);
        Entity mob = am.getEntity().getBukkitEntity();

        if (tamed.get(caster, null, power, args) && mob instanceof Tameable tameable && caster instanceof AnimalTamer tamer)
            tameable.setOwner(tamer);

        if (caster != null && owned.get(caster, null, power, args))
            am.setOwner(caster.getUniqueId());

        LivingEntity livingMob = mob instanceof LivingEntity le ? le : null;

        if (spellOnSpawn != null) {
            if (livingMob != null && spellOnSpawn.isTargetedEntitySpell())
                spellOnSpawn.castAtEntity(caster, livingMob, power);
            else if (spellOnSpawn.isTargetedLocationSpell())
                spellOnSpawn.castAtLocation(caster, mob.getLocation(), power);
            else
                spellOnSpawn.cast(caster, power);
        }

        SpellData data = new SpellData(caster, livingMob, power, args);
        if (caster != null) playSpellEffects(caster, mob, data);
        else playSpellEffects(EffectPosition.TARGET, mob, data);

        return true;
    }

}

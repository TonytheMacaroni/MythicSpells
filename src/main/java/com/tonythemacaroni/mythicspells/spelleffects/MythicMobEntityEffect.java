package com.tonythemacaroni.mythicspells.spelleffects;

import java.util.Set;
import java.util.Collections;
import java.util.WeakHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.configuration.ConfigurationSection;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.BukkitAPIHelper;
import io.lumine.mythic.api.mobs.entities.SpawnReason;

import com.nisovin.magicspells.util.Name;
import com.nisovin.magicspells.util.SpellData;
import com.nisovin.magicspells.util.config.ConfigData;
import com.nisovin.magicspells.util.config.ConfigDataUtil;
import com.nisovin.magicspells.spelleffects.effecttypes.EntityEffect;

import com.tonythemacaroni.mythicspells.MythicSpells;

@Name("mythicmobentity")
public class MythicMobEntityEffect extends EntityEffect {

    public static final Set<Entity> entities = Collections.newSetFromMap(new WeakHashMap<>());

    private ConfigData<String> type;
    private ConfigData<Double> level;
    private ConfigData<Boolean> gravity;
    private ConfigData<Integer> duration;

    @Override
    protected void loadFromConfig(ConfigurationSection config) {
        type = ConfigDataUtil.getString(config, "type", null);
        level = ConfigDataUtil.getDouble(config, "level", 1);
        gravity = ConfigDataUtil.getBoolean(config, "gravity", false);
        duration = ConfigDataUtil.getInteger(config, "duration", 0);
    }

    @Override
    protected Entity playEntityEffectLocation(Location location, SpellData data) {
        BukkitAPIHelper helper = MythicBukkit.inst().getAPIHelper();

        MythicMob type = helper.getMythicMob(this.type.get(data));
        if (type == null) return null;

        ActiveMob am = type.spawn(BukkitAdapter.adapt(location), level.get(data), SpawnReason.OTHER, entity -> {
            entity.setPersistent(false);
            entity.setGravity(gravity.get(data));
            entity.addScoreboardTag(EntityEffect.ENTITY_TAG);
        });
        if (am == null) return null;

        return am.getEntity().getBukkitEntity();
    }

    @Override
    public Runnable playEffectLocation(Location location, SpellData data) {
        Entity entity = playEntityEffectLocation(location, data);
        if (entity == null) return null;

        entities.add(entity);

        int duration = this.duration.get(data);
        if (duration > 0) {
            entity.getScheduler().runDelayed(
                MythicSpells.getInstance(),
                task -> {
                    entities.remove(entity);
                    entity.remove();
                },
                () -> entities.remove(entity),
                duration
            );
        }

        return null;
    }

    @Override
    public void turnOff() {
        entities.forEach(Entity::remove);
        entities.clear();
    }

}

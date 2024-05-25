package com.tonythemacaroni.mythicspells;

import java.util.Set;
import java.util.Collections;
import java.util.WeakHashMap;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;

import com.nisovin.magicspells.events.ConditionsLoadingEvent;
import com.nisovin.magicspells.events.MagicSpellsLoadedEvent;
import com.nisovin.magicspells.util.managers.ConditionManager;

import com.tonythemacaroni.mythicspells.conditions.*;
import com.tonythemacaroni.mythicspells.mechanics.SpellMechanic;

public class MythicSpellsListener implements Listener {

    private final Set<SpellMechanic> registeredMechanics = Collections.newSetFromMap(new WeakHashMap<>());

    @EventHandler
    public void onMechanicLoad(MythicMechanicLoadEvent event) {
        String mechanicName = event.getMechanicName();
        if (!"spell".equalsIgnoreCase(mechanicName)) return;

        SpellMechanic mechanic = new SpellMechanic(event.getContainer());

        event.register(mechanic);
        registeredMechanics.add(mechanic);
    }

    @EventHandler
    public void onMagicSpellsLoaded(MagicSpellsLoadedEvent event) {
        registeredMechanics.forEach(SpellMechanic::reprocess);
    }

    @EventHandler
    public void onMagicSpellsLoading(ConditionsLoadingEvent event) {
        ConditionManager manager = event.getConditionManager();

        manager.addCondition(FactionCondition.class);
        manager.addCondition(IsChildCondition.class);
        manager.addCondition(IsMythicMobCondition.class);
        manager.addCondition(IsOwnedCondition.class);
        manager.addCondition(IsOwnerCondition.class);
        manager.addCondition(IsParentCondition.class);
        manager.addCondition(MobLevelCondition.class);
        manager.addCondition(MobTypeCondition.class);
    }

}

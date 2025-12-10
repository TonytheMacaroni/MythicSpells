package com.tonythemacaroni.mythicspells;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class MythicSpells extends JavaPlugin {

    private static MythicSpells instance;

    @Override
    public void onEnable() {
        instance = this;

        getServer().getPluginManager().registerEvents(new MythicSpellsListener(), this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }

    public static MythicSpells getInstance() {
        return instance;
    }

}

package com.kylenanakdewa.core;

/**
 * A modular component in CoRE. This allows for sub-plugins to run under the
 * main CoRE plugin.
 * <p>
 * Each loaded module will receive onEnable and onDisable events, which will be
 * triggered by the main CoRE plugin.
 *
 * @author Kyle Nanakdewa
 */
public abstract class CoreModule {

    /** The CoRE instance that owns this module. */
    private final CorePlugin corePlugin;

    public CoreModule(CorePlugin plugin) {
        corePlugin = plugin;
    }

    /**
     * Gets the CoRE plugin instance that owns this module.
     */
    protected final CorePlugin getPlugin() {
        return corePlugin;
    }

    /**
     * Called when this module is enabled. This will typically happen when the main
     * CoRE plugin is enabled or reloaded.
     */
    public void onEnable() {
    }

    /**
     * Called when this module is disabled. This will typically happen when the main
     * CoRE plugin is disabled or reloaded.
     */
    public void onDisable() {
    }

}
package me.ablax.decode.managers;

import me.ablax.decode.annotation.RegisterListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

class ListenersManager {

    private final Map<String, Object> components;
    private final ComponentsManager componentsManager;

    ListenersManager(Map<String, Object> components, ComponentsManager componentsManager) {
        this.components = components;
        this.componentsManager = componentsManager;
    }

    void registerAllListeners(JavaPlugin javaPlugin, List<? extends Class<?>> classesList) {
        for (Class<?> aClass : classesList) {
            if (aClass.isAnnotationPresent(RegisterListener.class)) {
                registerListener(javaPlugin, aClass);
            }
        }
    }

    private void registerListener(JavaPlugin claz, Class<?> aClass) {
        if (components.containsKey(aClass.getCanonicalName())) {
            Bukkit.getPluginManager().registerEvents((Listener) components.get(aClass.getCanonicalName()), claz);
        } else {
            componentsManager.registerComponent(aClass);
            if (components.containsKey(aClass.getCanonicalName())) {
                Bukkit.getPluginManager().registerEvents((Listener) components.get(aClass.getCanonicalName()), claz);
            }
        }
    }

}

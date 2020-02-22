package me.ablax.decode.managers;

import me.ablax.decode.annotation.RegisterCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

class CommandsManager {

    private final Map<String, Object> components;
    private final ComponentsManager componentsManager;

    CommandsManager(Map<String, Object> components, ComponentsManager componentsManager) {
        this.components = components;
        this.componentsManager = componentsManager;
    }

    void registerAllCommands(List<? extends Class<?>> classesList) {
        try {
            for (Class<?> aClass : classesList) {
                if (aClass.isAnnotationPresent(RegisterCommand.class)) {
                    registerCommand(JavaPlugin.getProvidingPlugin(aClass), aClass);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerCommand(JavaPlugin plugin, Class<?> aClass) {
        if (components.containsKey(aClass.getCanonicalName())) {
            final RegisterCommand annotation = aClass.getAnnotation(RegisterCommand.class);
            plugin.getCommand(annotation.commandName()).setExecutor((CommandExecutor) components.get(aClass.getCanonicalName()));
        } else {
            componentsManager.registerComponent(aClass);
            if (components.containsKey(aClass.getCanonicalName())) {
                final RegisterCommand annotation = aClass.getAnnotation(RegisterCommand.class);
                plugin.getCommand(annotation.commandName()).setExecutor((CommandExecutor) components.get(aClass.getCanonicalName()));
            }
        }
    }
}

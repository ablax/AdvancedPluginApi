package me.ablax.decode.managers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class ManagersController {

    private static Object isInstantiated;

    private final ComponentsManager componentsManager;
    private final InjectorsManager injectorsManager;
    private final ConfigValuesManager configValuesManager;
    private final ListenersManager listenersManager;
    private final CommandsManager commandsManager;

    public ManagersController(Map<String, Object> components) {
        if (isInstantiated != null) {
            Bukkit.getServer().shutdown();
            throw new SecurityException("This move is illegal!");
        }
        isInstantiated = new Object();
        this.configValuesManager = new ConfigValuesManager(components);
        this.componentsManager = new ComponentsManager(components, configValuesManager);
        this.injectorsManager = new InjectorsManager(components, componentsManager);
        this.listenersManager = new ListenersManager(components, componentsManager);
        this.commandsManager = new CommandsManager(components, componentsManager);
    }

    public void registerComponent(Class<?> componentToRegister) {
        this.componentsManager.registerComponent(componentToRegister);
    }

    public void registerAllCommands(List<? extends Class<?>> classesList) {
        this.commandsManager.registerAllCommands(classesList);
    }

    public void registerAllListeners(List<? extends Class<?>> classesList) {
        listenersManager.registerAllListeners(classesList);
    }

    public void registerAllComponents(List<? extends Class<?>> classesList) {
        this.componentsManager.registerAllComponents(classesList);
    }

    public void populateInjectors(JavaPlugin javaPlugin) {
        this.injectorsManager.populateInjectors(javaPlugin);
    }

    public void populateInjectors(List<? extends Class<?>> classesList) {
        for (Class<?> clazz : classesList) {
            if (componentsManager.containsClass(clazz)) {
                this.injectorsManager.populateInjectors(clazz);
            }
        }
    }

    public void populateValues(JavaPlugin javaPlugin) {
        this.configValuesManager.populateValues(javaPlugin);
    }

    public void populateValues(List<? extends Class<?>> classesList) {
        for (Class<?> clazz : classesList) {
            if (componentsManager.containsClass(clazz)) {
                this.configValuesManager.populateValues(clazz);
            }
        }
    }
}

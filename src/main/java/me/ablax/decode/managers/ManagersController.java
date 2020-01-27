package me.ablax.decode.managers;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public class ManagersController {

    private static Object instance = new Object();

    private final ComponentsManager componentsManager;
    private final InjectorsManager injectorsManager;
    private final ListenersManager listenersManager;
    private final CommandsManager commandsManager;

    public ManagersController(Map<String, Object> components) {
        if (instance == null) {
            Bukkit.getServer().shutdown();
            throw new SecurityException("This move is illegal!");
        }
        instance = null;
        this.componentsManager = new ComponentsManager(components);
        this.injectorsManager = new InjectorsManager(components, componentsManager);
        this.listenersManager = new ListenersManager(components, componentsManager);
        this.commandsManager = new CommandsManager(components, componentsManager);
    }

    public void registerComponent(Class<?> componentToRegister) {
        this.componentsManager.registerComponent(componentToRegister);
    }

    public void registerAllCommands(JavaPlugin javaPlugin, List<? extends Class<?>> classesList) {
        this.commandsManager.registerAllCommands(javaPlugin, classesList);
    }

    public void registerAllListeners(JavaPlugin javaPlugin, List<? extends Class<?>> classesList) {
        listenersManager.registerAllListeners(javaPlugin, classesList);
    }

    public void registerAllComponents(List<? extends Class<?>> classesList) {
        this.componentsManager.registerAllComponents(classesList);
    }

    public void populateInjectors(JavaPlugin javaPlugin) {
        this.injectorsManager.populateInjectors(javaPlugin);
    }
}

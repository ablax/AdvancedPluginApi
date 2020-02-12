package me.ablax.decode;

import com.google.common.reflect.ClassPath;
import me.ablax.decode.annotation.Component;
import me.ablax.decode.managers.ManagersController;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class ApiManagerImpl {

    private final Map<String, Object> components;
    private final ManagersController managersController;

    ApiManagerImpl() {
        components = new HashMap<>();
        managersController = new ManagersController(components);
    }

    private List<? extends Class<?>> getClassesList(Class<? extends JavaPlugin> javaPluginClass) {
        List<? extends Class<?>> classesList = new ArrayList<>();
        final ClassPath path;
        try {
            path = ClassPath.from(javaPluginClass.getClassLoader());
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.SEVERE, "I could't find any classes belonging to plugin: " + javaPluginClass.getSimpleName(), e);
            return classesList;
        }
        classesList = path.getTopLevelClassesRecursive(javaPluginClass.getPackage().getName())
                .stream().map(classInfo -> {
                    try {
                        return Class.forName(classInfo.getName(), true, javaPluginClass.getClassLoader());
                    } catch (ClassNotFoundException e) {
                        Bukkit.getLogger().log(Level.SEVERE, "I could't find declared class " + classInfo.getName() + " belonging to plugin: " + javaPluginClass.getSimpleName(), e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return classesList;
    }

    void register(JavaPlugin javaPlugin) {
        final Class<? extends JavaPlugin> javaPluginClass = javaPlugin.getClass();

        components.put(javaPluginClass.getCanonicalName(), javaPlugin);
        managersController.populateInjectors(javaPlugin);
        managersController.populateValues(javaPlugin);

        List<? extends Class<?>> classesList = getClassesList(javaPluginClass);

        if (!classesList.isEmpty()) {
            managersController.registerAllComponents(classesList);
            managersController.registerAllListeners(javaPlugin, classesList);
            managersController.registerAllCommands(javaPlugin, classesList);
            managersController.populateInjectors(classesList);
            managersController.populateValues(javaPlugin, classesList);
        } else {
            Bukkit.getLogger().severe("I could't find any classes belonging to plugin: " + javaPlugin.getName());
        }
    }

    Object getComponent(Class<?> getObject) {
        if (!components.containsKey(getObject.getCanonicalName())
                && getObject.isAnnotationPresent(Component.class)) {
            managersController.registerComponent(getObject);
        }

        if (components.containsKey(getObject.getCanonicalName())) {
            return components.get(getObject.getCanonicalName());
        }

        return null;
    }
}

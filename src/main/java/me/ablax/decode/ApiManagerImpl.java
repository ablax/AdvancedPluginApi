package me.ablax.decode;

import com.google.common.reflect.ClassPath;
import me.ablax.decode.annotation.AutoInject;
import me.ablax.decode.annotation.Component;
import me.ablax.decode.annotation.RegisterCommand;
import me.ablax.decode.annotation.RegisterListener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApiManagerImpl {

    private static final ApiManagerImpl instance = new ApiManagerImpl();
    private final Map<String, Object> components;

    private ApiManagerImpl() {
        components = new HashMap<>();
    }

    public static ApiManagerImpl getInstance() {
        return instance;
    }

    private void registerAllComponents(List<? extends Class<?>> allClasses) {
        try {
            for (Class<?> aClass : allClasses) {
                if (aClass.isAnnotationPresent(Component.class)) {
                    registerComponent(aClass);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerComponent(Class<?> aClass) {
        if (!components.containsKey(aClass.getCanonicalName())) {
            loadClass(aClass);
        }
    }

    private void loadClass(Class<?> aClass) {
        try {
            Constructor<?> constructor = getConstructor(aClass);
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            Object[] vars = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                if (components.containsKey(parameterType.getCanonicalName())) {
                    vars[i] = components.get(parameterType.getCanonicalName());
                } else if (parameterType.isAnnotationPresent(Component.class)) {
                    registerComponent(parameterType);
                    vars[i] = components.get(parameterType.getCanonicalName());
                }
            }
            boolean aPrivate = Modifier.isPrivate(constructor.getModifiers());
            if (aPrivate) {
                constructor.setAccessible(true);
            }
            components.put(aClass.getCanonicalName(), constructor.newInstance(vars));
            if (aPrivate) {
                constructor.setAccessible(false);
            }
        } catch (Exception ex) {
        }
    }

    private Constructor<?> getConstructor(Class<?> aClass) {
        Constructor<?>[] constructors = aClass.getDeclaredConstructors();
        if (constructors.length == 0) {
            constructors = aClass.getConstructors();
        }
        return constructors[0];
    }

    private void populateInjectors(Object klass) {
        for (Field field : klass.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoInject.class)) {
                Class<?> type = field.getType();
                try {
                    if (components.containsKey(type.getCanonicalName())) {
                        populateField(klass, field, type);
                    } else if (field.getType().isAnnotationPresent(Component.class)) {
                        registerComponent(field.getType());
                        if (components.containsKey(type.getCanonicalName())) {
                            populateField(klass, field, type);
                        }
                    }
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void populateField(Object klass, Field field, Class<?> type) throws NoSuchFieldException, IllegalAccessException {
        field.setAccessible(true);
        if (Modifier.isFinal(field.getModifiers())) {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        }
        field.set(klass, components.get(type.getCanonicalName()));
    }

    private void registerAllListeners(JavaPlugin javaPlugin, List<? extends Class<?>> classesList) {
        try {
            for (Class<?> aClass : classesList) {
                if (aClass.isAnnotationPresent(RegisterListener.class)) {
                    registerListener(javaPlugin, aClass);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerAllCommands(JavaPlugin javaPlugin, List<? extends Class<?>> classesList) {
        try {
            for (Class<?> aClass : classesList) {
                if (aClass.isAnnotationPresent(RegisterCommand.class)) {
                    registerCommand(javaPlugin, aClass);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerListener(JavaPlugin claz, Class<?> aClass) {
        if (components.containsKey(aClass.getCanonicalName())) {
            Bukkit.getPluginManager().registerEvents((Listener) components.get(aClass.getCanonicalName()), claz);
        } else {
            registerComponent(aClass);
            if (components.containsKey(aClass.getCanonicalName())) {
                Bukkit.getPluginManager().registerEvents((Listener) components.get(aClass.getCanonicalName()), claz);
            }
        }
    }

    private void registerCommand(JavaPlugin plugin, Class<?> aClass) {
        if (components.containsKey(aClass.getCanonicalName())) {
            final RegisterCommand annotation = aClass.getAnnotation(RegisterCommand.class);
            plugin.getCommand(annotation.commandName()).setExecutor((CommandExecutor) components.get(aClass.getCanonicalName()));
        } else {
            registerComponent(aClass);
            if (components.containsKey(aClass.getCanonicalName())) {
                final RegisterCommand annotation = aClass.getAnnotation(RegisterCommand.class);
                plugin.getCommand(annotation.commandName()).setExecutor((CommandExecutor) components.get(aClass.getCanonicalName()));
            }
        }
    }

    private List<? extends Class<?>> getClassesList(Class<? extends JavaPlugin> javaPluginClass) {
        List<? extends Class<?>> classesList = null;
        try {
            final ClassPath path = ClassPath.from(javaPluginClass.getClassLoader());
            classesList = path.getTopLevelClassesRecursive(javaPluginClass.getPackage().getName()).stream().map(classInfo -> {
                try {
                    return Class.forName(classInfo.getName(), true, javaPluginClass.getClassLoader());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return null;
            }).collect(Collectors.toList());

        } catch (IOException e) {
            e.printStackTrace();
        }
        return classesList;
    }

    void register(JavaPlugin javaPlugin) {
        final Class<? extends JavaPlugin> javaPluginClass = javaPlugin.getClass();
        components.put(javaPluginClass.getCanonicalName(), javaPlugin);
        populateInjectors(javaPlugin);
        List<? extends Class<?>> classesList = getClassesList(javaPluginClass);

        if (classesList != null) {
            registerAllComponents(classesList);
            registerAllListeners(javaPlugin, classesList);
            registerAllCommands(javaPlugin, classesList);
        }
    }

    Object getComponent(Class<?> getObject) {
        if (!components.containsKey(getObject.getCanonicalName())
                && getObject.isAnnotationPresent(Component.class)) {
            registerComponent(getObject);
        }

        if (components.containsKey(getObject.getCanonicalName())) {
            return components.get(getObject.getCanonicalName());
        }

        return null;
    }
}

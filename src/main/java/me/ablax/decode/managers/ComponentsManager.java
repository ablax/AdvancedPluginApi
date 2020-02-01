package me.ablax.decode.managers;

import me.ablax.decode.annotation.Component;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

class ComponentsManager {

    private final Map<String, Object> components;

    ComponentsManager(Map<String, Object> components) {
        this.components = components;
    }

    void registerAllComponents(List<? extends Class<?>> allClasses) {
        for (Class<?> aClass : allClasses) {
            if (aClass.isAnnotationPresent(Component.class)) {
                registerComponent(aClass);
            }
        }
    }

    void registerComponent(Class<?> aClass) {
        if (!components.containsKey(aClass.getCanonicalName())) {
            loadClass(aClass);
        }
    }

    private void loadClass(Class<?> aClass) {
        final List<Constructor<?>> constructors = getConstructors(aClass);
        for (Constructor<?> constructor : constructors) {
            try {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                Object[] vars = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    Class<?> parameterType = parameterTypes[i];
                    if (components.containsKey(parameterType.getCanonicalName())) {
                        vars[i] = components.get(parameterType.getCanonicalName());
                    } else if (parameterType.isAnnotationPresent(Component.class)) {
                        registerComponent(parameterType);
                        vars[i] = components.get(parameterType.getCanonicalName());
                    } else {
                        throw new InstantiationException("I don't know what " + parameterType.getSimpleName() + " is to pass it on " + aClass.getSimpleName());
                    }
                }
                boolean isPrivate = Modifier.isPrivate(constructor.getModifiers());
                if (isPrivate) {
                    constructor.setAccessible(true);
                }
                components.put(aClass.getCanonicalName(), constructor.newInstance(vars));
                if (isPrivate) {
                    constructor.setAccessible(false);
                }
                return;
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException ex) {
                Bukkit.getLogger().log(Level.WARNING, "Error occurred while instantiating " + aClass.getSimpleName() + "!", ex);
            }
        }
        Bukkit.getLogger().severe("After trying all we can, we weren't able to instantiate " + aClass.getSimpleName() + " are you sure you have a nice friendly constructor?");
    }

    private List<Constructor<?>> getConstructors(Class<?> aClass) {
        Constructor<?>[] privateConstructors = aClass.getDeclaredConstructors();
        final Constructor<?>[] publicConstructors = aClass.getConstructors();

        List<Constructor<?>> constructorsList = new ArrayList<>();
        constructorsList.addAll(Arrays.asList(privateConstructors));
        constructorsList.addAll(Arrays.asList(publicConstructors));

        return constructorsList;
    }

}

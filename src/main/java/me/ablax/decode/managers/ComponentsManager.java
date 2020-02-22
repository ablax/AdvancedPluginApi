package me.ablax.decode.managers;

import me.ablax.decode.annotation.Component;
import me.ablax.decode.annotation.ConfigValue;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

class ComponentsManager {

    private final Map<String, Object> components;
    private final ConfigValuesManager configValuesManager;

    ComponentsManager(final Map<String, Object> components, final ConfigValuesManager configValuesManager) {
        this.components = components;
        this.configValuesManager = configValuesManager;
    }

    boolean containsClass(Class<?> clazz) {
        return components.containsKey(clazz.getCanonicalName());
    }

    void registerAllComponents(List<? extends Class<?>> allClasses) {
        for (Class<?> aClass : allClasses) {
            if (aClass.isInterface()) {
                boolean registered = false;
                for (Class<?> tempImp : allClasses) {
                    if (!registered && !tempImp.isInterface()) {
                        if (aClass.isAssignableFrom(tempImp) && tempImp.isAnnotationPresent(Component.class)) {
                            registerComponent(tempImp);
                            components.put(aClass.getCanonicalName(), components.get(tempImp.getCanonicalName()));
                            registered = true;
                        }
                    }
                }
            }
        }
        for (Class<?> aClass : allClasses) {
            if (!aClass.isInterface()) {
                if (aClass.isAnnotationPresent(Component.class)) {
                    registerComponent(aClass);
                }
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
                Parameter[] parameters = constructor.getParameters();
                Object[] vars = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    Class<?> parameterType = parameters[i].getType();
                    final Object resolved = resolveParameter(aClass, parameters[i]);
                    if (resolved == null) {
                        throw new InstantiationException("I don't know what " + parameterType.getSimpleName() + " is to pass it on " + aClass.getSimpleName());
                    }
                    vars[i] = resolved;
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

    private Object resolveParameter(Class<?> parent, Parameter parameter) {
        final Class<?> parameterType = parameter.getType();
        if (components.containsKey(parameterType.getCanonicalName())) {
            return components.get(parameterType.getCanonicalName());
        } else {
            if (parameterType.isAnnotationPresent(Component.class)) {
                registerComponent(parameterType);
                return components.get(parameterType.getCanonicalName());
            } else if (parameter.isAnnotationPresent(ConfigValue.class)) {
                return configValuesManager.getValue(parent, parameter);
            }
            return null;
        }
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

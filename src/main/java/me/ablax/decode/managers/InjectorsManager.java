package me.ablax.decode.managers;

import me.ablax.decode.annotation.AutoInject;
import me.ablax.decode.annotation.Component;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.logging.Level;

class InjectorsManager {

    private final Map<String, Object> components;
    private final ComponentsManager componentsManager;
    private sun.misc.Unsafe unsafe;
    private boolean useUnsafe = true;

    InjectorsManager(Map<String, Object> components, ComponentsManager componentsManager) {
        try {
            Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            final Object theUnsafe = field.get(null);
            unsafe = (sun.misc.Unsafe) theUnsafe;
        } catch (Exception e) {
            unsafe = null;
            useUnsafe = false;
        }

        this.components = components;
        this.componentsManager = componentsManager;
    }

    void populateInjectors(Object klass) {
        for (Field field : klass.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(AutoInject.class)) {
                Class<?> type = field.getType();
                if (components.containsKey(type.getCanonicalName())) {
                    populateField(klass, field, type);
                } else if (field.getType().isAnnotationPresent(Component.class)) {
                    componentsManager.registerComponent(field.getType());
                    if (components.containsKey(type.getCanonicalName())) {
                        populateField(klass, field, type);
                    }
                }
            }
        }
    }

    private void populateField(Object klass, Field field, Class<?> type) {
        final boolean accessible = field.isAccessible();
        final Object injectInstance = components.get(type.getCanonicalName());
        if (!useUnsafe) {
            if (!accessible) {
                field.setAccessible(true);
            }
            try {
                if (Modifier.isFinal(field.getModifiers())) {
                    Field modifiersField = Field.class.getDeclaredField("modifiers");
                    modifiersField.setAccessible(true);
                    modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                }
            } catch (IllegalAccessException ex) {
                Bukkit.getLogger().log(Level.SEVERE,
                        "AdvancedPluginAPI can't work like that. It's not allowed to use Unsafe, neither access private/final/both fields.", ex);
            } catch (NoSuchFieldException ex) {
                Bukkit.getLogger().log(Level.SEVERE,
                        "AdvancedPluginAPI can't work like that. It's not allowed to use Unsafe, neither the claimed filed was there. Please report this to owner: ", ex);
            }
            try {
                field.set(klass, injectInstance);
            } catch (IllegalAccessException ex) {
                Bukkit.getLogger().log(Level.SEVERE,
                        "AdvancedPluginAPI can't work like that. It's not allowed to use Unsafe, neither access private/final/both fields.", ex);
            }
            if (!accessible) {
                field.setAccessible(false);
            }
            return;
        }

        if (!accessible) {
            field.setAccessible(true);
        }
        unsafe.putObject(klass, unsafe.objectFieldOffset(field), injectInstance);
        if (!accessible) {
            field.setAccessible(false);
        }
    }

}

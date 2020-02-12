package me.ablax.decode.managers;

import me.ablax.decode.annotation.Value;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.logging.Level;

class ValuesManager {

    private final Map<String, Object> components;
    private sun.misc.Unsafe unsafe;
    private boolean useUnsafe = true;

    ValuesManager(Map<String, Object> components) {
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
    }

    void populateValues(Object klass) {
        for (Field field : klass.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                Class<?> type = field.getType();
                final Value annotation = type.getAnnotation(Value.class);
                if (klass instanceof Plugin) {
                    final Object object = ((Plugin) klass).getConfig().get(annotation.name());
                    populateField(klass, field, object);
                }
            }
        }
    }

    public void populateValues(JavaPlugin javaPlugin, Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Value.class)) {
                Class<?> type = field.getType();
                final Value annotation = type.getAnnotation(Value.class);
                final Object object = javaPlugin.getConfig().get(annotation.name());
                populateField(components.get(clazz.getCanonicalName()), field, object);
            }
        }
    }

    private void populateField(Object klass, Field field, Object injectInstance) {
        final boolean accessible = field.isAccessible();
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

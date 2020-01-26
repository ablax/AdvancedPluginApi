package me.ablax.decode;

import com.google.common.reflect.ClassPath;
import me.ablax.decode.annotation.AutoInject;
import me.ablax.decode.annotation.Component;
import me.ablax.decode.annotation.RegisterListener;
import org.atteo.classindex.ClassIndex;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
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

    private void registerAllComponents(List<? extends Class<?>> claz) {
        try {
            for (Class<?> aClass : ClassIndex.getAnnotated(Component.class)) {
                registerComponent(aClass);
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

    private void registerAllListeners(Object claz, List<? extends Class<?>> classesList) {
        try {
            for (Class<?> aClass : ClassIndex.getAnnotated(RegisterListener.class)) {
                registerListener(claz, aClass);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerListener(Object claz, Class<?> aClass) {
        if (components.containsKey(aClass.getCanonicalName())) {
            Bukkit.getPluginManager().registerEvents((Listener) components.get(aClass.getCanonicalName()), (Plugin) claz);
        } else if (aClass.isAnnotationPresent(RegisterListener.class)) {
            registerComponent(aClass);
            if (components.containsKey(aClass.getCanonicalName())) {
                Bukkit.getPluginManager().registerEvents((Listener) components.get(aClass.getCanonicalName()), (Plugin) claz);
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

    public void register(JavaPlugin javaPlugin) {
        final Class<? extends JavaPlugin> javaPluginClass = javaPlugin.getClass();
        components.put(javaPluginClass.getCanonicalName(), javaPlugin);
        populateInjectors(javaPlugin);
        List<? extends Class<?>> classesList = getClassesList(javaPluginClass);

        if (classesList != null) {
            registerAllComponents(classesList);
            registerAllListeners(javaPlugin, classesList);
        }

        components.values().forEach(o -> {
            if (o.getClass().isAnnotationPresent(RegisterListener.class)) {
                registerListener(javaPlugin, o.getClass());
            }
        });
    }

    public Object getComponent(Class<?> getObject) {
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

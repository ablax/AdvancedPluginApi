package me.ablax.decode;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedPlugin extends JavaPlugin {

    private ApiManagerImpl instance = null;

    @Override
    public void onEnable() {
        if (instance != null) {
            Bukkit.getServer().shutdown();
            throw new SecurityException("This move is illegal!");
        }
        instance = new ApiManagerImpl();
    }

    public void registerPlugin(JavaPlugin javaPlugin) {
        instance.register(javaPlugin);
    }

    public Object getComponent(Class<?> getObject) {
        return instance.getComponent(getObject);
    }

}

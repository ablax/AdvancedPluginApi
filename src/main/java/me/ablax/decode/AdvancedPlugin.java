package me.ablax.decode;

import org.bukkit.plugin.java.JavaPlugin;

public class AdvancedPlugin extends JavaPlugin {

    private ApiManagerImpl apiManager;

    @Override
    public void onEnable() {
        apiManager = ApiManagerImpl.getInstance();
    }

    public void registerPlugin(JavaPlugin javaPlugin) {
        apiManager.register(javaPlugin);
    }

    public Object getComponent(Class<?> getObject) {
        return apiManager.getComponent(getObject);
    }


}

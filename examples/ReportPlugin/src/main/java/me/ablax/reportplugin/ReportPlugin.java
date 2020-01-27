package me.ablax.reportplugin;

import me.ablax.decode.AdvancedPlugin;
import me.ablax.decode.annotation.AutoInject;
import me.ablax.reportplugin.discordbot.DiscordBot;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import javax.security.auth.login.LoginException;
import java.util.logging.Level;

public class ReportPlugin extends JavaPlugin {

    @AutoInject
    private DiscordBot discordBot;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        final Plugin advancedPluginApi = Bukkit.getPluginManager().getPlugin("AdvancedPluginApi");
        if (advancedPluginApi != null) {
            final AdvancedPlugin pluginApi = (AdvancedPlugin) advancedPluginApi;
            pluginApi.registerPlugin(this);
        }

        try {
            discordBot.init();
        } catch (LoginException e) {
            getLogger().log(Level.SEVERE, "An error occurred", e);
            setEnabled(false);
        }
    }
}

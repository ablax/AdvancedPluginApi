package me.ablax.reportplugin.configuration;

import me.ablax.decode.annotation.Component;
import me.ablax.reportplugin.ReportPlugin;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

@Component
public class ConfigurationManager {

    private String token;
    private String notifyRole;
    private String textChannel;
    private String guildName;
    private String playerNotFound;
    private String help;
    private String successfulReport;
    private String onlyReportOnce;

    private ConfigurationManager(ReportPlugin plugin) {
        FileConfiguration config = plugin.getConfig();

        token = config.getString("token");
        notifyRole = config.getString("notifyRole");
        guildName = config.getString("guildName");
        textChannel = config.getString("textChannel");
        playerNotFound = colorize(config.getString("playerNotFound"));
        help = colorize(config.getString("help"));
        successfulReport = colorize(config.getString("successfulReport"));
        onlyReportOnce = colorize(config.getString("onlyReportOnce"));
    }

    public String getGuildName() {
        return guildName;
    }

    public String getHelp() {
        return help;
    }

    public String getTextChannel() {
        return textChannel;
    }

    public String getToken() {
        return token;
    }

    public String getNotifyRole() {
        return notifyRole;
    }

    public String getPlayerNotFound() {
        return playerNotFound;
    }

    public String getSuccessfulReport(String name, String reason) {
        return successfulReport
                .replaceAll("%name%", name)
                .replaceAll("%reason%", reason);
    }

    private String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public String getOnlyOnceReport() {
        return onlyReportOnce;
    }
}

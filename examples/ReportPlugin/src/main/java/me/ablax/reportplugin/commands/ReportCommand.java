package me.ablax.reportplugin.commands;

import me.ablax.decode.annotation.Component;
import me.ablax.decode.annotation.RegisterCommand;
import me.ablax.reportplugin.configuration.ConfigurationManager;
import me.ablax.reportplugin.discordbot.DiscordBot;
import me.ablax.reportplugin.util.Constants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RegisterCommand(commandName = "report")
public class ReportCommand implements CommandExecutor {

    private final Map<UUID, UUID> avoidRepetition;
    private final ConfigurationManager configurationManager;
    private final DiscordBot discordBot;

    public ReportCommand(ConfigurationManager configurationManager, DiscordBot discordBot) {
        this.configurationManager = configurationManager;
        this.discordBot = discordBot;
        avoidRepetition = new ConcurrentHashMap<>();
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000 * 60 * 5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                avoidRepetition.clear();
            }
        });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage(ChatColor.RED + "Sorry, but this command is for players only!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(configurationManager.getHelp());
            return true;
        }

        final List<Player> players = Bukkit.matchPlayer(args[0]);

        if (players.isEmpty()) {
            player.sendMessage(configurationManager.getPlayerNotFound());
            return true;
        }

        Player target = players.get(0);

        if (avoidRepetition.containsKey(player.getUniqueId())) {
            if (avoidRepetition.get(player.getUniqueId()).equals(target.getUniqueId())) {
                player.sendMessage(configurationManager.getOnlyOnceReport());
                return true;
            }
        }

        StringBuilder builder = new StringBuilder(32);
        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                builder.append(args[i]).append(" ");
            }
        }

        String reason;

        if (builder.length() == 0) {
            reason = Constants.NO_REASON_SPECIFIED;
        } else {
            reason = builder.toString();
        }

        avoidRepetition.put(player.getUniqueId(), target.getUniqueId());

        discordBot.sendReport(sender.getName(), target.getName(), reason);
        sender.sendMessage(configurationManager.getSuccessfulReport(target.getName(), reason));
        return true;
    }


}

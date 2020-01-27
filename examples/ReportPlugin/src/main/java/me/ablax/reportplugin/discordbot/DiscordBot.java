package me.ablax.reportplugin.discordbot;

import me.ablax.decode.annotation.Component;
import me.ablax.reportplugin.configuration.ConfigurationManager;
import me.ablax.reportplugin.util.EditDistanceRecursive;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.List;

@Component
public class DiscordBot {

    private final EmbedBuilder builder = new EmbedBuilder();
    private JDA jda;
    private Guild guild;
    private TextChannel textChannel;
    private Role role;
    private final ConfigurationManager configurationManager;

    public DiscordBot(ConfigurationManager configurationManager) {
        this.configurationManager = configurationManager;
    }

    private void getNotifyRole(String notifyRoleName) {
        final List<Role> rolesByName = guild.getRolesByName(notifyRoleName, true);

        if (rolesByName.isEmpty()) {
            int max = 500;
            for (Role role : guild.getRoles()) {
                final int similarity = EditDistanceRecursive.calculate(role.getName(), notifyRoleName);
                if (similarity < max) {
                    max = similarity;
                    this.role = role;
                }
            }
        } else {
            this.role = rolesByName.get(0);
        }
    }

    private void getTextChannel(String textChannelName) {
        final List<TextChannel> textChannelsByName = guild.getTextChannelsByName(textChannelName, true);

        if (textChannelsByName.isEmpty()) {
            int max = 500;
            for (TextChannel textChannel1 : guild.getTextChannels()) {
                final int similarity = EditDistanceRecursive.calculate(textChannel1.getName(), textChannelName);
                if (similarity < max) {
                    max = similarity;
                    this.textChannel = textChannel1;
                }
            }
        } else {
            this.textChannel = textChannelsByName.get(0);
        }
    }

    private void getGuild(String guildName) {
        final List<Guild> guildsByName = jda.getGuildsByName(guildName, true);

        if (guildsByName.isEmpty()) {
            int max = 500;
            for (Guild jdaGuild : jda.getGuilds()) {
                final int similarity = EditDistanceRecursive.calculate(jdaGuild.getName(), guildName);
                if (similarity < max) {
                    max = similarity;
                    this.guild = jdaGuild;
                }
            }
        } else {
            this.guild = guildsByName.get(0);
        }
    }

    public void sendReport(String reporter, String player, String reason) {
        textChannel.sendMessage(role.getAsMention()).queue(message -> message.delete().queue());

        final EmbedBuilder embedBuilder = new EmbedBuilder(builder);

        embedBuilder.addField("Reporter", reporter, true);
        embedBuilder.addField("Reported player", player, true);
        embedBuilder.addField("Reason", reason, true);
        textChannel.sendMessage(embedBuilder.build()).queue();
    }

    public void init() throws LoginException {
        String token = configurationManager.getToken();
        jda = new JDABuilder(AccountType.BOT).setToken(token).build();

        jda.setRequestTimeoutRetry(true);
        jda.setAutoReconnect(true);

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final String guildName = configurationManager.getGuildName();
        getGuild(guildName);
        if (guild == null) {
            throw new LoginException("Guild " + guildName + " not found!");
        }

        final String textChannelName = configurationManager.getTextChannel();
        getTextChannel(textChannelName);
        if (textChannel == null) {
            throw new LoginException("Text channel " + textChannelName + " not found!");
        }


        final String notifyRoleName = configurationManager.getNotifyRole();
        getNotifyRole(notifyRoleName);
        if (role == null) {
            throw new LoginException("Role " + notifyRoleName + " not found!");
        }

        builder.setColor(Color.WHITE);
        builder.setTitle("New report");
    }
}

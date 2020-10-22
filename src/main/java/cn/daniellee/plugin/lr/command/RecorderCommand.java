package cn.daniellee.plugin.lr.command;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.model.PlayerData;
import cn.daniellee.plugin.lr.runnable.LiveRunnable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RecorderCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length > 0) {
            if (commandSender.isOp()) {
                if ("reload".equalsIgnoreCase(strings[0])) {
                    LiveRecorder.getInstance().reloadConfig();
                    LiveRecorder.getInstance().loadConfig();
                    commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.reload-success", "&eConfiguration reload completed.")).replace("&", "§"));
                    return true;
                } else if ("target".equalsIgnoreCase(strings[0]) && strings.length > 1) {
                    Player target = Bukkit.getPlayer(strings[1]);
                    if (target != null) {
                        if (LiveCore.recorder != null) {
                            LiveCore.recorder.teleport(LiveCore.getLiveLocation(target.getLocation()));
                            LiveCore.recordingPlayer = strings[1];
                            commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.target-set", "&eThe target player has been set as the recording object.")).replace("&", "§"));
                        } else commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.no-recorder", "&eThe recorder is not online and cannot set the target.")).replace("&", "§"));
                    } else commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.invalid-player", "&eTarget player does not exist.")).replace("&", "§"));
                    return true;
                } else if ("time".equalsIgnoreCase(strings[0]) && strings.length > 1) {
                    LiveRecorder.getInstance().getConfig().set("setting.record-seconds", Integer.valueOf(strings[1]));
                    commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.time-set", "&eThe time of each player's live recording is set successfully.")).replace("&", "§"));
                    return true;
                } else if ("recorder".equalsIgnoreCase(strings[0]) && strings.length > 1) {
                    Player recorder = Bukkit.getPlayer(strings[1]);
                    if (recorder != null) {
                        LiveCore.recorder = recorder;
                        LiveRecorder.getInstance().getConfig().set("setting.recorder-name", recorder.getName());
                        commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.recorder-set", "&eThe target player has been set as the live recorder.")).replace("&", "§"));
                    } else commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.invalid-player", "&eTarget player does not exist.")).replace("&", "§"));
                    return true;
                } else if ("reset".equalsIgnoreCase(strings[0])) {
                    LiveRunnable.resetRecordedSeconds();
                    commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.second-reset", "&eReset successfully, will automatically switch live target.")).replace("&", "§"));
                    return true;
                }
            }
            if ("toggle".equals(strings[0]) && commandSender.hasPermission("recorder.toggle")) {
                PlayerData playerData = LiveRecorder.getInstance().playerData.get(commandSender.getName());
                if (playerData == null) {
                    playerData = new PlayerData(commandSender.getName());
                }
                playerData.setDenied(!playerData.isDenied());
                LiveRecorder.getInstance().playerData.put(commandSender.getName(), playerData);
                LiveRecorder.getInstance().playerDataYaml.set(commandSender.getName() + ".denied", playerData.isDenied());
                commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.toggle-set", "&eSet up successfully, you {status} &ebroadcast.").replace("{status}", playerData.isDenied() ? LiveRecorder.getInstance().getConfig().getString("message.toggle-status.denied", "&cwill not be") : LiveRecorder.getInstance().getConfig().getString("message.toggle-status.accept", "&awill be"))).replace("&", "§"));
            } else sendHelp(commandSender);
        } else sendHelp(commandSender);
        return true;
    }


    private void sendHelp(CommandSender commandSender) {
        commandSender.sendMessage(("&m&a----&m&6----------&3 " + LiveRecorder.getInstance().getConfig().getString("prompt-prefix", "LiveRecorder") + " &m&6----------&m&a----").replace("&", "§"));

        String targetText = LiveRecorder.getInstance().getConfig().getString("help.target", "Set the target player as the broadcast object.").replace("&", "§");
        TextComponent targetHelp = new TextComponent("/lr target  " + targetText);
        targetHelp.setColor(ChatColor.GRAY);
        targetHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lr target "));
        targetHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(targetText).color(ChatColor.BLUE).create()));
        commandSender.spigot().sendMessage(targetHelp);

        String timeText = LiveRecorder.getInstance().getConfig().getString("help.time", "Set the time each player is broadcast(unit: second).").replace("&", "§");
        TextComponent timeHelp = new TextComponent("/lr time  " + timeText);
        timeHelp.setColor(ChatColor.GRAY);
        timeHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lr time "));
        timeHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(timeText).color(ChatColor.BLUE).create()));
        commandSender.spigot().sendMessage(timeHelp);

        String recorderText = LiveRecorder.getInstance().getConfig().getString("help.recorder", "Set the target player as the live recorder.").replace("&", "§");
        TextComponent recorderHelp = new TextComponent("/lr recorder  " + recorderText);
        recorderHelp.setColor(ChatColor.GRAY);
        recorderHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lr recorder "));
        recorderHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(recorderText).color(ChatColor.BLUE).create()));
        commandSender.spigot().sendMessage(recorderHelp);

        String resetText = LiveRecorder.getInstance().getConfig().getString("help.reset", "Resetting the live broadcast time will automatically switch the live broadcast target.").replace("&", "§");
        TextComponent resetHelp = new TextComponent("/lr reset  " + resetText);
        resetHelp.setColor(ChatColor.GRAY);
        resetHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lr reset"));
        resetHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(resetText).color(ChatColor.BLUE).create()));
        commandSender.spigot().sendMessage(resetHelp);

        String toggleText = LiveRecorder.getInstance().getConfig().getString("help.toggle", "&eSet whether you will be broadcast live (for players).").replace("&", "§");
        TextComponent toggleHelp = new TextComponent("/lr toggle  " + toggleText);
        toggleHelp.setColor(ChatColor.GRAY);
        toggleHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lr toggle"));
        toggleHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(resetText).color(ChatColor.BLUE).create()));
        commandSender.spigot().sendMessage(toggleHelp);

        String reloadText = LiveRecorder.getInstance().getConfig().getString("help.reload", "Reload configuration.").replace("&", "§");
        TextComponent reloadHelp = new TextComponent("/lr reload  " + reloadText);
        reloadHelp.setColor(ChatColor.GRAY);
        reloadHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/lr reload"));
        reloadHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(reloadText).color(ChatColor.BLUE).create()));
        commandSender.spigot().sendMessage(reloadHelp);
    }
}

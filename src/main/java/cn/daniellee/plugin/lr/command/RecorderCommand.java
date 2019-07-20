package cn.daniellee.plugin.lr.command;

import cn.daniellee.plugin.lr.LiveRecorder;
import cn.daniellee.plugin.lr.core.LiveCore;
import cn.daniellee.plugin.lr.runnable.LiveRunnable;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.math.BigDecimal;

public class RecorderCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length > 0 && commandSender.isOp()) {
            if ("reload".equalsIgnoreCase(strings[0])) {
                LiveRecorder.getInstance().reloadConfig();
                LiveRecorder.getInstance().loadConfig();
                commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.reload-success", "&eConfiguration reload completed.")).replace("&", "§"));
                return true;
            } else if ("target".equalsIgnoreCase(strings[0]) && strings.length > 1) {
                Player target = Bukkit.getPlayer(strings[1]);
                if (target != null) {
	                if (LiveCore.recordingPlayer != null) {
		                Player current = Bukkit.getPlayer(LiveCore.recordingPlayer);
		                if (current != null) {
			                current.setGlowing(false);
		                }
	                }
	                target.setGlowing(true);
                    LiveCore.recorder.teleport(LiveCore.getLiveLovation(target.getLocation()));
                    LiveCore.recordingPlayer = strings[1];
                    commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.target-set", "&eThe target player has been set as the recording object.")).replace("&", "§"));
                } else commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.invalid-player", "&eTarget player does not exist.")).replace("&", "§"));
            } else if ("time".equalsIgnoreCase(strings[0]) && strings.length > 1) {
                LiveRecorder.getInstance().getConfig().set("setting.record-seconds", Integer.valueOf(strings[1]));
                commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.time-set", "&eThe time of each player's live recording is set successfully.")).replace("&", "§"));
            } else if ("recorder".equalsIgnoreCase(strings[0]) && strings.length > 1) {
                Player recorder = Bukkit.getPlayer(strings[1]);
                if (recorder != null) {
                    LiveCore.recorder = recorder;
                    LiveRecorder.getInstance().getConfig().set("setting.recorder-name", recorder.getName());
                    commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.recorder-set", "&eThe target player has been set as the live recorder.")).replace("&", "§"));
                } else commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.invalid-player", "&eTarget player does not exist.")).replace("&", "§"));
            } else if ("reset".equalsIgnoreCase(strings[1])) {
                LiveRunnable.resetRecordedSeconds();
                commandSender.sendMessage((LiveRecorder.getInstance().getPrefix() + LiveRecorder.getInstance().getConfig().getString("message.second-reset", "&eReset successfully, will automatically switch live target.")).replace("&", "§"));
            }
        } else sendHelp(commandSender);
        return true;
    }


    private void sendHelp(CommandSender commandSender) {
        commandSender.sendMessage(("&m&a----&m&6----------&3 " + LiveRecorder.getInstance().getConfig().getString("prompt-prefix", "LiveRecorder") + " &m&6----------&m&a----").replace("&", "§"));

//        String tpaText = OptimizedTeleport.getInstance().getConfig().getString("tpa-help", "&eRequest to be sent to a player (/tpa)").replace("&", "§");
//        TextComponent tpaHelp = new TextComponent("/otpa <player>  " + tpaText);
//        tpaHelp.setColor(ChatColor.GRAY);
//        tpaHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/otpa "));
//        tpaHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tpaText).color(ChatColor.BLUE).create()));
//        player.spigot().sendMessage(tpaHelp);
//
//        String tpacceptText = OptimizedTeleport.getInstance().getConfig().getString("tpaccept-help", "&eAccept the teleport request (/tpaccept)").replace("&", "§");
//        TextComponent tpacceptHelp = new TextComponent("/otpaccept  " + tpacceptText);
//        tpacceptHelp.setColor(ChatColor.GRAY);
//        tpacceptHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/otpaccept"));
//        tpacceptHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tpacceptText).color(ChatColor.BLUE).create()));
//        player.spigot().sendMessage(tpacceptHelp);
//
//        String tpdenyText = OptimizedTeleport.getInstance().getConfig().getString("tpdeny-help", "&eRefuse to teleport request (/tpdeny)").replace("&", "§");
//        TextComponent tpdenyHelp = new TextComponent("/otpdeny  " + tpdenyText);
//        tpdenyHelp.setColor(ChatColor.GRAY);
//        tpdenyHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/otpdeny"));
//        tpdenyHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tpdenyText).color(ChatColor.BLUE).create()));
//        player.spigot().sendMessage(tpdenyHelp);
//
//        String tpahereText = OptimizedTeleport.getInstance().getConfig().getString("tpahere-help", "&eRequest to teleport a player to your location (/tpahere)").replace("&", "§");
//        TextComponent tpahereHelp = new TextComponent("/otpahere <player>  " + tpahereText);
//        tpahereHelp.setColor(ChatColor.GRAY);
//        tpahereHelp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/otpahere "));
//        tpahereHelp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tpahereText).color(ChatColor.BLUE).create()));
//        player.spigot().sendMessage(tpahereHelp);
    }
}

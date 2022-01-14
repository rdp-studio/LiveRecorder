package cn.daniellee.plugin.lr.command;

import cn.daniellee.plugin.lr.core.LiveCore;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RecorderCompleter implements TabCompleter {

	private String[] subCommands = {"switch", "target", "time", "recorder", "reset", "toggle", "reload"};
	private String[] times = {"15", "30", "60"};

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		if (strings.length == 0) return Arrays.asList(subCommands);
		else if (strings.length == 1) return Arrays.stream(subCommands).filter(string -> string.startsWith(strings[0])).collect(Collectors.toList());
		else if (strings.length == 2) {
			if ("target".equals(strings[0]) || "recorder".equals(strings[0])) return Collections.list(LiveCore.activePlayers.keys());
			else if ("time".equals(strings[0])) return Arrays.asList(times);
		}
		return new ArrayList<>();
	}

}

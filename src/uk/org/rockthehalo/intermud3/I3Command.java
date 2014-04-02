package uk.org.rockthehalo.intermud3;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.org.rockthehalo.intermud3.services.I3Channel;
import uk.org.rockthehalo.intermud3.services.Services;

public class I3Command implements CommandExecutor {
	/**
	 * Constructor.
	 */
	I3Command() {
	}

	/**
	 * Process player commands.
	 * 
	 * @param sender
	 *            Source of the command
	 * @param command
	 *            Command which was executed
	 * @param label
	 *            Alias of the command which was used
	 * @param args
	 *            Passed command arguments
	 * @return true on success
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (args.length < 1)
			return !checkPerm(sender, "help") || usage(sender, command);

		String subcmd = StringUtils.lowerCase(args[0]);

		if (args.length > 1) {
			String tmp;

			tmp = StringUtils.join(args, " ");
			tmp = tmp.substring(tmp.indexOf(" ") + 1);
			args = tmp.split(" ");
		}

		return processI3SubCommand(sender, command, subcmd, args);
	}

	/**
	 * @param sender
	 * @param command
	 * @param subcmd
	 * @param args
	 * @return
	 */
	private boolean processI3SubCommand(CommandSender sender, Command command,
			String subcmd, String[] args) {
		Player player;
		String input = null;
		String chan = null;
		String msg = null;

		input = StringUtils.join(args, " ");

		if (subcmd.equals("connect")) {
			if (!checkPerm(sender, "connect")) {
				return false;
			}

			Services.create();
			Intermud3.network.connect();

			return true;
		} else if (subcmd.equals("disconnect")) {
			if (!checkPerm(sender, "disconnect")) {
				return false;
			}

			Intermud3.network.shutdown(0);
			Services.remove();

			return true;
		} else if (subcmd.equals("emote")) {
			if (!(Player.class.isInstance(sender))) {
				sender.sendMessage("Can only send emotes as player.");

				return true;
			}

			if (!checkPerm(sender, "emote"))
				return false;

			if (args.length < 2)
				return usage(sender, command, subcmd);

			player = (Player) sender;
			chan = args[0];
			msg = input.substring(input.indexOf(" ") + 1);

			I3Channel service = (I3Channel) Services.getService("channel");

			if (service != null)
				service.sendEmote(chan, player, msg);

			return true;
		} else if (subcmd.equals("msg")) {
			if (!(Player.class.isInstance(sender))) {
				sender.sendMessage("Can only send messages as player.");

				return true;
			}

			if (!checkPerm(sender, "msg"))
				return false;

			if (args.length < 2)
				return usage(sender, command, subcmd);

			player = (Player) sender;
			chan = args[0];
			msg = input.substring(input.indexOf(" ") + 1);

			I3Channel service = (I3Channel) Services.getService("channel");

			if (service != null)
				service.sendMessage(chan, player, msg);

			return true;
		} else if (subcmd.equals("debug")) {
			if (Player.class.isInstance(sender)) {
				sender.sendMessage("Can only get debug info from the console.");

				return true;
			}

			Intermud3.callout.debugInfo();
			Intermud3.heartbeat.debugInfo();
			Services.debugInfo();

			return true;
		} else {
			sender.sendMessage(ChatColor.RED + "Unknown I3 command. (" + subcmd
					+ ")");

			return false;
		}
	}

	/**
	 * @param sender
	 * @param subnode
	 * @return
	 */
	private boolean checkPerm(CommandSender sender, String subnode) {
		boolean ok = sender.hasPermission("intermud3." + subnode);

		if (!ok) {
			sender.sendMessage(ChatColor.RED
					+ "You do not have permissions to do that.");
		}

		return ok;
	}

	/**
	 * @param sender
	 * @param command
	 * @return
	 */
	private boolean usage(CommandSender sender, Command command) {
		sender.sendMessage(ChatColor.RED + "[====" + ChatColor.GREEN
				+ " /intermud3 " + ChatColor.RED + "====]");

		for (String line : command.getUsage().split("\\n"))
			sender.sendMessage(formatLine(line));

		return true;
	}

	/**
	 * @param sender
	 * @param command
	 * @param subcmd
	 * @return
	 */
	private boolean usage(CommandSender sender, Command command, String subcmd) {
		sender.sendMessage(ChatColor.RED + "[====" + ChatColor.GREEN
				+ " /intermud3 " + subcmd + " " + ChatColor.RED + "====]");

		for (String line : command.getUsage().split("\\n"))
			if (line.startsWith("/<command> " + subcmd))
				sender.sendMessage(formatLine(line));

		return true;
	}

	/**
	 * @param line
	 * @return
	 */
	private String formatLine(String line) {
		int i = line.indexOf(" - ");
		String usage = line.substring(0, i);
		String desc = line.substring(i + 3);

		usage = usage.replace("<command>", "intermud3");
		usage = usage.replaceAll("\\[[^]:]+\\]", ChatColor.AQUA + "$0"
				+ ChatColor.GREEN);
		usage = usage.replaceAll("\\[[^]]+:\\]", ChatColor.AQUA + "$0"
				+ ChatColor.LIGHT_PURPLE);
		usage = usage.replaceAll("<[^>]+>", ChatColor.LIGHT_PURPLE + "$0"
				+ ChatColor.GREEN);

		return ChatColor.GREEN + usage + " - " + ChatColor.WHITE + desc;
	}
}

package uk.org.rockthehalo.intermud3;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class I3Command implements CommandExecutor {
	private Intermud3 I3instance = null;

	/**
	 * Constructor.
	 */
	I3Command(Intermud3 instance) {
		this.I3instance = instance;
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
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		String cmd = StringUtils.lowerCase(command.getName());
		String subcmd = null;

		if (cmd.substring(0, 2).equals("i3") && cmd.length() > 2) {
			subcmd = StringUtils.lowerCase(cmd.substring(2));

			return processI3SubCommand(sender, command, subcmd, args);
		} else {
			String tmp;

			if (args.length < 1) {
				return !checkPerm(sender, "help") || usage(sender, command);
			}

			subcmd = StringUtils.lowerCase(args[0]);

			if (args.length > 1) {
				tmp = StringUtils.join(args, " ");
				tmp = tmp.substring(tmp.indexOf(" ") + 1);
				args = tmp.split(" ");
			}

			return processI3SubCommand(sender, command, subcmd, args);
		}
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
		String input = null;
		String packetType = null;
		String chan = null;
		String msg = null;

		input = StringUtils.join(args, " ");

		if (subcmd.equals("reload")) {
			if (!checkPerm(sender, "reload")) {
				return false;
			}

			this.I3instance.reloadPlugin();

			return true;
		} else if (subcmd.equals("emote")) {
			if (!checkPerm(sender, "emote")) {
				return false;
			}

			if (args.length < 2) {
				return usage(sender, command, subcmd);
			}

			packetType = "channel-e";
			chan = args[0];
			msg = input.substring(input.indexOf(" ") + 1);

			this.I3instance.sendPacket(sender.getName(), packetType, chan, msg);

			return true;
		} else if (subcmd.equals("msg")) {
			if (!checkPerm(sender, "msg")) {
				return false;
			}

			if (args.length < 2) {
				return usage(sender, command, subcmd);
			}

			packetType = "channel-m";
			chan = args[0];
			msg = input.substring(input.indexOf(" ") + 1);

			this.I3instance.sendPacket(sender.getName(), packetType, chan, msg);

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
		for (String line : command.getUsage().split("\\n")) {
			sender.sendMessage(formatLine(line));
		}

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
				+ " /permissons " + subcmd + " " + ChatColor.RED + "====]");

		for (String line : command.getUsage().split("\\n")) {
			if (line.startsWith("/<command> " + subcmd)) {
				sender.sendMessage(formatLine(line));
			}
		}

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

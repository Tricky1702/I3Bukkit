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
	public I3Command() {
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
		String[] subcmdargs = {};

		if (args.length > 1) {
			String tmp;

			tmp = StringUtils.join(args, " ", 1, args.length);
			subcmdargs = tmp.split(" ");
		}

		return processI3SubCommand(sender, command, subcmd, subcmdargs);
	}

	/**
	 * @param sender
	 *            Source of the command
	 * @param command
	 *            Command which was executed
	 * @param subcmd
	 *            Intermud3 command which was executed
	 * @param args
	 *            Passed Intermud3 command arguments
	 * @return true on success
	 */
	private boolean processI3SubCommand(CommandSender sender, Command command,
			String subcmd, String[] args) {
		String input = null;

		Log.debug("args: " + StringUtils.join(args, ","));

		if (subcmd.equals("connect")) {
			if (!checkPerm(sender, "connect")) {
				return false;
			}

			Services.createServices();
			Intermud3.network.connect();

			return true;
		} else if (subcmd.equals("disconnect")) {
			if (!checkPerm(sender, "disconnect")) {
				return false;
			}

			Intermud3.network.shutdown(0);
			Services.removeServices();

			return true;
		} else if (subcmd.equals("emote")) {
			if (!(Utils.isPlayer(sender))) {
				sender.sendMessage("Can only send emotes as player.");

				return true;
			}

			if (!checkPerm(sender, "emote"))
				return false;

			if (args.length < 2)
				return usage(sender, command, subcmd);

			I3Channel service = (I3Channel) Services.getService("channel");

			if (service == null)
				sender.sendMessage(ChatColor.RED
						+ "I3 channel service not available.");
			else {
				String plrName = ((Player) sender).getDisplayName();
				String chan = args[0];

				if (service.getAliases().containsKey(chan)
						&& !service.getAliases().containsValue(chan))
					chan = service.getAliases().get(chan);

				if (!service.getListening().contains(chan)) {
					sender.sendMessage("Not listening to I3 channel "
							+ ChatColor.GREEN + input);

					return true;
				}

				input = StringUtils.join(args, " ", 1, args.length);
				service.sendEmote(chan, plrName, input);
			}

			return true;
		} else if (subcmd.equals("msg")) {
			if (!(Utils.isPlayer(sender))) {
				sender.sendMessage("Can only send messages as player.");

				return true;
			}

			if (!checkPerm(sender, "msg"))
				return false;

			if (args.length < 2)
				return usage(sender, command, subcmd);

			I3Channel service = (I3Channel) Services.getService("channel");

			if (service == null)
				sender.sendMessage(ChatColor.RED
						+ "I3 channel service not available.");
			else {
				String plrName = ((Player) sender).getDisplayName();
				String chan = args[0];

				if (service.getAliases().containsKey(chan)
						&& !service.getAliases().containsValue(chan))
					chan = service.getAliases().get(chan);

				if (!service.getListening().contains(chan)) {
					sender.sendMessage("Not listening to I3 channel "
							+ ChatColor.GREEN + input);

					return true;
				}

				input = StringUtils.join(args, " ", 1, args.length);
				service.sendMessage(chan, plrName, input);
			}

			return true;
		} else if (subcmd.equals("channels")) {
			if (!checkPerm(sender, "channels"))
				return false;

			I3Channel service = (I3Channel) Services.getService("channel");

			if (service == null)
				sender.sendMessage(ChatColor.RED
						+ "I3 channel service not available.");
			else {
				if (args.length > 0)
					input = args[0].toLowerCase();

				if (input == null)
					service.showChannelsListening(sender);
				else {
					if (input.equals("available"))
						service.showChannelsAvailable(sender);
					else if (input.equals("aliases"))
						service.showChannelAliases(sender);
					else if (input.equals("tunein")) {
						if (!checkPerm(sender, "tune"))
							return false;

						if (args.length < 2)
							return usage(sender, command, subcmd + " tunein");

						input = args[1];

						if (!service.getChanList().containsKey(input)) {
							sender.sendMessage("I3 channel " + ChatColor.GREEN
									+ input + ChatColor.RESET
									+ " not available.");

							return true;
						}

						service.tuneIn(input);
						sender.sendMessage("Tuned into I3 channel "
								+ ChatColor.GREEN + input);
					} else if (input.equals("tuneout")) {
						if (!checkPerm(sender, "tune"))
							return false;

						if (args.length < 2)
							return usage(sender, command, subcmd + " tuneout");

						input = args[1];

						if (!service.getListening().contains(input)) {
							sender.sendMessage("Not listening to I3 channel "
									+ ChatColor.GREEN + input);

							return true;
						}

						service.tuneOut(input);
						sender.sendMessage("Tuned out of I3 channel "
								+ ChatColor.GREEN + input);
					} else if (input.equals("alias")) {
						if (!checkPerm(sender, "alias"))
							return false;

						if (args.length < 3)
							return usage(sender, command, subcmd + " alias");

						String alias = args[1];
						String chan = args[2];

						service.setAlias(alias, chan);
						sender.sendMessage("Alias set: " + ChatColor.GREEN
								+ alias + ChatColor.RESET + " -> "
								+ ChatColor.GREEN + chan);
					} else if (input.equals("unalias")) {
						if (!checkPerm(sender, "alias"))
							return false;

						if (args.length < 2)
							return usage(sender, command, subcmd + " unalias");

						String alias = args[1];

						if (!service.getAliases().containsKey(alias)) {
							sender.sendMessage("Alias " + ChatColor.GREEN
									+ alias + ChatColor.RESET
									+ " does not exist.");

							return true;
						}

						service.setAlias(alias, null);
						sender.sendMessage("Alias unset: " + ChatColor.GREEN
								+ alias);
					}
				}
			}

			return true;
		} else if (subcmd.equals("debug")) {
			if (Utils.isPlayer(sender)) {
				sender.sendMessage(ChatColor.RED
						+ "Can only get debug info from the console."
						+ ChatColor.RESET);

				return true;
			}

			Intermud3.callout.debugInfo();
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

package uk.org.rockthehalo.intermud3;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import uk.org.rockthehalo.intermud3.services.I3Channel;
import uk.org.rockthehalo.intermud3.services.I3Mudlist;
import uk.org.rockthehalo.intermud3.services.I3UCache;
import uk.org.rockthehalo.intermud3.services.ServiceManager;
import uk.org.rockthehalo.intermud3.services.ServiceType;

public class I3Command implements CommandExecutor {
	private String lastChannel = null;

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
	public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
		if (!checkPerm(sender, "use"))
			return false;

		boolean playerCommand = false;
		boolean adminCommand = false;

		if (label.equalsIgnoreCase("intermud3") || label.equalsIgnoreCase("i3")) {
			if (args.length < 1)
				return !checkPerm(sender, "help") || usage("intermud3", sender, command);

			playerCommand = true;
		} else if (label.equalsIgnoreCase("i3admin") || label.equalsIgnoreCase("i3a")) {
			if (args.length < 1)
				return !checkPerm(sender, "help") || usage("i3admin", sender, command);

			adminCommand = true;
		}

		final String subcmd = StringUtils.lowerCase(args[0]);
		String[] subcmdargs = {};

		if (args.length > 1) {
			final String tmp = StringUtils.join(args, " ", 1, args.length);

			subcmdargs = tmp.split(" ");
		}

		if (playerCommand)
			return processI3SubCommand(sender, command, "intermud3", subcmd, subcmdargs);
		else if (adminCommand)
			return processI3AdminSubCommand(sender, command, "i3admin", subcmd, subcmdargs);
		else
			return false;
	}

	/**
	 * @param sender
	 *            Source of the command
	 * @param command
	 *            Command which was executed
	 * @param label
	 *            Alias of the command which was used
	 * @param subcmd
	 *            Intermud3 command which was executed
	 * @param args
	 *            Passed Intermud3 command arguments
	 * @return true on success
	 */
	private boolean processI3SubCommand(final CommandSender sender, final Command command, final String label, final String subcmd,
			final String[] args) {
		final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();
		final I3UCache i3UCache = ServiceType.I3UCACHE.getService();

		Log.debug("args: " + StringUtils.join(args, ","));

		if (i3Channel == null || i3UCache == null) {
			sender.sendMessage(ChatColor.RED + "I3 services not available.");

			return true;
		}

		if (subcmd.equals("emote")) {
			if (!(Utils.isPlayer(sender))) {
				sender.sendMessage("Can only send emotes as player.");

				return true;
			}

			if (!checkPerm(sender, "channel"))
				return false;

			if (args.length < 2)
				return usage(label, sender, command, subcmd);

			final String plrName = Utils.stripColor(((Player) sender).getName());
			String chan = args[0];

			if (chan.equals(".") && this.lastChannel != null)
				chan = this.lastChannel;

			if (i3UCache.getAliases(plrName).containsKey(chan) && !i3UCache.getAliases(plrName).containsValue(chan))
				chan = i3UCache.getAliases(plrName).get(chan);

			final Map<String, Object> user = i3UCache.getLocalUser(plrName);
			@SuppressWarnings("unchecked")
			final List<String> tunein = (List<String>) user.get("TUNEIN");

			if (!tunein.contains(chan)) {
				sender.sendMessage("Not listening to I3 channel " + ChatColor.GREEN + chan);

				return true;
			}

			this.lastChannel = chan;

			final String input = StringUtils.join(args, " ", 1, args.length);

			i3Channel.sendEmote(chan, plrName, input);
		} else if (subcmd.equals("msg")) {
			if (!(Utils.isPlayer(sender))) {
				sender.sendMessage("Can only send messages as player.");

				return true;
			}

			if (!checkPerm(sender, "channel"))
				return false;

			if (args.length < 2)
				return usage(label, sender, command, subcmd);

			final String plrName = Utils.stripColor(((Player) sender).getName());
			String chan = args[0];

			if (chan.equals(".") && this.lastChannel != null)
				chan = this.lastChannel;

			if (i3UCache.getAliases(plrName).containsKey(chan) && !i3UCache.getAliases(plrName).containsValue(chan))
				chan = i3UCache.getAliases(plrName).get(chan);

			final Map<String, Object> user = i3UCache.getLocalUser(plrName);
			@SuppressWarnings("unchecked")
			final List<String> tunein = (List<String>) user.get("TUNEIN");

			if (!tunein.contains(chan)) {
				sender.sendMessage("Not listening to I3 channel " + ChatColor.GREEN + chan);

				return true;
			}

			this.lastChannel = chan;

			final String input = StringUtils.join(args, " ", 1, args.length);

			i3Channel.sendMessage(chan, plrName, input);
		} else if (subcmd.startsWith("tune")) {
			if (subcmd.equals("tunein")) {
				if (args.length < 1)
					return usage(label, sender, command, "tune");

				final String input = args[0];

				if (!i3Channel.getChanList().containsKey(input)) {
					sender.sendMessage("I3 channel " + ChatColor.GREEN + input + ChatColor.RESET + " not available.");

					return true;
				}

				i3UCache.tuneIn(((Player) sender).getName(), input);
				sender.sendMessage("Tuned into I3 channel " + ChatColor.GREEN + input);
			} else if (subcmd.equals("tuneout")) {
				if (args.length < 1)
					return usage(label, sender, command, "tune");

				final String input = args[0];

				if (!i3Channel.getListening().contains(input)) {
					sender.sendMessage("Not listening to I3 channel " + ChatColor.GREEN + input);

					return true;
				}

				i3UCache.tuneOut(((Player) sender).getName(), input);
				sender.sendMessage("Tuned out of I3 channel " + ChatColor.GREEN + input);
			} else
				return usage(label, sender, command, "tune");
		} else if (subcmd.equals("alias")) {
			if (args.length < 2)
				return usage(label, sender, command, subcmd);

			final String alias = args[0];
			final String chan = args[1];

			if (i3Channel.getChanList().containsKey(alias)) {
				sender.sendMessage(ChatColor.RED + "Can not set alias name to an existing I3 channel.");

				return true;
			}

			i3UCache.setAlias(((Player) sender).getName(), alias, chan);
			sender.sendMessage("Alias set: " + ChatColor.GREEN + alias + ChatColor.RESET + " -> " + ChatColor.GREEN + chan);
		} else if (subcmd.equals("unalias")) {
			if (args.length < 1)
				return usage(label, sender, command, subcmd);

			final String alias = args[0];
			final Map<String, String> aliases = i3UCache.getAliases(Utils.stripColor(((Player) sender).getName()));

			if (aliases == null || !aliases.containsKey(alias)) {
				sender.sendMessage("Alias " + ChatColor.GREEN + alias + ChatColor.RESET + " does not exist.");

				return true;
			}

			i3UCache.setAlias(((Player) sender).getName(), alias, null);
			sender.sendMessage("Alias unset: " + ChatColor.GREEN + alias);
		} else if (subcmd.equals("list")) {
			i3Channel.showChannelsListening(sender);
		} else if (subcmd.equals("available")) {
			i3Channel.showChannelsAvailable(sender);
		} else if (subcmd.equals("aliases")) {
			i3Channel.showChannelAliases(sender);
		} else {
			sender.sendMessage(ChatColor.RED + "Unknown I3 command. (" + subcmd + ")");

			return false;
		}

		return true;
	}

	/**
	 * @param sender
	 *            Source of the command
	 * @param command
	 *            Command which was executed
	 * @param label
	 *            Alias of the command which was used
	 * @param subcmd
	 *            Intermud3 admin command which was executed
	 * @param args
	 *            Passed Intermud3 admin command arguments
	 * @return true on success
	 */
	private boolean processI3AdminSubCommand(final CommandSender sender, final Command command, final String label,
			final String subcmd, final String[] args) {
		String input = null;

		Log.debug("args: " + StringUtils.join(args, ","));

		if (subcmd.equals("connect")) {
			if (!checkPerm(sender, "admin.connect"))
				return false;

			if (!Intermud3.network.isConnected()) {
				ServiceManager.createServices();
				Intermud3.network.connect();
			}
		} else if (subcmd.equals("disconnect")) {
			if (!checkPerm(sender, "admin.disconnect"))
				return false;

			if (Intermud3.network.isConnected()) {
				Intermud3.network.shutdown(0);
				ServiceManager.removeServices();
			}
		} else if (subcmd.equals("reload")) {
			if (!checkPerm(sender, "admin.reload"))
				return false;

			final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();
			final I3Mudlist i3Mudlist = ServiceType.I3MUDLIST.getService();
			final I3UCache i3UCache = ServiceType.I3UCACHE.getService();

			if (i3Channel != null)
				i3Channel.reloadConfig(true);

			if (i3Mudlist != null)
				i3Mudlist.reloadConfig(true);

			if (i3UCache != null)
				i3UCache.reloadConfig(true);

			Intermud3.network.reloadConfig(true);

			sender.sendMessage("Config files reload.");
		} else if (subcmd.equals("save")) {
			if (!checkPerm(sender, "admin.save"))
				return false;

			final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();
			final I3Mudlist i3Mudlist = ServiceType.I3MUDLIST.getService();
			final I3UCache i3UCache = ServiceType.I3UCACHE.getService();

			if (i3Channel != null)
				i3Channel.saveConfig(true);

			if (i3Mudlist != null)
				i3Mudlist.saveConfig(true);

			if (i3UCache != null)
				i3UCache.saveConfig(true);

			Intermud3.network.saveConfig(true);

			sender.sendMessage("Config files saved.");
		} else if (subcmd.equals("channels")) {
			if (!checkPerm(sender, "admin.channels"))
				return false;

			final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

			if (i3Channel == null)
				sender.sendMessage(ChatColor.RED + "I3 channel service not available.");
			else {
				if (args.length > 0)
					input = args[0].toLowerCase();

				if (input == null)
					i3Channel.showChannelsListening(sender);
				else {
					if (input.equals("aliases"))
						i3Channel.showChannelAliases(sender);
					else if (input.equals("tunein")) {
						if (!checkPerm(sender, "admin.channels.tune"))
							return false;

						if (args.length < 2)
							return usage(label, sender, command, subcmd + " tune");

						input = args[1];

						if (!i3Channel.getChanList().containsKey(input)) {
							sender.sendMessage("I3 channel " + ChatColor.GREEN + input + ChatColor.RESET + " not available.");

							return true;
						}

						i3Channel.tuneIn(input);
						sender.sendMessage("Tuned into I3 channel " + ChatColor.GREEN + input);
					} else if (input.equals("tuneout")) {
						if (!checkPerm(sender, "admin.channels.tune"))
							return false;

						if (args.length < 2)
							return usage(label, sender, command, subcmd + " tune");

						input = args[1];

						if (!i3Channel.getListening().contains(input)) {
							sender.sendMessage("Not listening to I3 channel " + ChatColor.GREEN + input);

							return true;
						}

						i3Channel.tuneOut(input);
						sender.sendMessage("Tuned out of I3 channel " + ChatColor.GREEN + input);
					} else if (input.equals("alias")) {
						if (!checkPerm(sender, "admin.channels.alias"))
							return false;

						if (args.length < 3)
							return usage(label, sender, command, subcmd + " alias");

						final String alias = args[1];
						final String chan = args[2];

						if (i3Channel.getChanList().containsKey(alias)) {
							sender.sendMessage(ChatColor.RED + "Can not set alias name to an existing I3 channel.");

							return true;
						}

						i3Channel.setAlias(alias, chan);
						sender.sendMessage("Alias set: " + ChatColor.GREEN + alias + ChatColor.RESET + " -> " + ChatColor.GREEN
								+ chan);
					} else if (input.equals("unalias")) {
						if (!checkPerm(sender, "admin.channels.alias"))
							return false;

						if (args.length < 2)
							return usage(label, sender, command, subcmd + " unalias");

						final String alias = args[1];

						if (!i3Channel.getAliases().containsKey(alias)) {
							sender.sendMessage("Alias " + ChatColor.GREEN + alias + ChatColor.RESET + " does not exist.");

							return true;
						}

						i3Channel.setAlias(alias, null);
						sender.sendMessage("Alias unset: " + ChatColor.GREEN + alias);
					}
				}
			}
		} else if (subcmd.equals("debug")) {
			if (Utils.isPlayer(sender)) {
				sender.sendMessage(ChatColor.RED + "Can only get debug info from the console." + ChatColor.RESET);

				return true;
			}

			if (args.length > 0
					&& (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("switch"))) {
				if (!checkPerm(sender, "admin.debug"))
					return false;

				final boolean oldDebug = Log.getDebugFlag();
				boolean debug;

				if (args[0].equalsIgnoreCase("on"))
					debug = true;
				else if (args[0].equalsIgnoreCase("off"))
					debug = false;
				else
					debug = !oldDebug;

				if (!debug)
					Log.info("Switching debug messages off.");
				else
					Log.info("Switching debug messages on.");

				Log.setDebugFlag(debug);
			} else {
				Intermud3.callout.debugInfo();
				ServiceManager.debugInfo();
			}
		} else {
			sender.sendMessage(ChatColor.RED + "Unknown I3 admin command. (" + subcmd + ")");

			return false;
		}

		return true;
	}

	/**
	 * @param sender
	 * @param subnode
	 * @return
	 */
	private boolean checkPerm(final CommandSender sender, final String subnode) {
		final boolean ok = sender.hasPermission("intermud3." + subnode);

		if (!ok)
			sender.sendMessage(ChatColor.RED + "You do not have permission to do that");

		return ok;
	}

	/**
	 * @param cmd
	 * @param sender
	 * @param command
	 * @return
	 */
	private boolean usage(final String cmd, final CommandSender sender, final Command command) {
		sender.sendMessage(ChatColor.RED + "[====" + ChatColor.GREEN + " /" + cmd + " " + ChatColor.RED + "====]");

		for (final String line : command.getUsage().split("\\n"))
			sender.sendMessage(formatLine(cmd, line));

		return true;
	}

	/**
	 * @param cmd
	 * @param sender
	 * @param command
	 * @param subcmd
	 * @return
	 */
	private boolean usage(final String cmd, final CommandSender sender, final Command command, final String subcmd) {
		sender.sendMessage(ChatColor.RED + "[====" + ChatColor.GREEN + " /" + cmd + " " + subcmd + " " + ChatColor.RED + "====]");

		for (final String line : command.getUsage().split("\\n"))
			if (line.startsWith("/<command> " + subcmd))
				sender.sendMessage(formatLine(cmd, line));

		return true;
	}

	/**
	 * @param cmd
	 * @param line
	 * @return
	 */
	private String formatLine(final String cmd, final String line) {
		int i = line.indexOf(" - ");
		String usage = line.substring(0, i);
		final String desc = line.substring(i + 3);

		usage = usage.replace("<command>", cmd);
		usage = usage.replaceAll("\\[[^]:]+\\]", ChatColor.AQUA + "$0" + ChatColor.GREEN);
		usage = usage.replaceAll("\\[[^]]+:\\]", ChatColor.AQUA + "$0" + ChatColor.LIGHT_PURPLE);
		usage = usage.replaceAll("<[^>]+>", ChatColor.LIGHT_PURPLE + "$0" + ChatColor.GREEN);

		return ChatColor.GREEN + usage + " - " + ChatColor.WHITE + desc;
	}
}

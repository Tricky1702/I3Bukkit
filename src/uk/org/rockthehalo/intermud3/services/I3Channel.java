package uk.org.rockthehalo.intermud3.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import uk.org.rockthehalo.intermud3.Config;
import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.PacketTypes.PacketType;
import uk.org.rockthehalo.intermud3.Payload;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public class I3Channel extends ServiceTemplate {
	private static final Payload chanEmotePayload = new Payload(Arrays.asList(
			"CHAN_CHANNAME", "CHAN_VISNAME", "CHAN_EMOTE"));
	private static final Payload chanlistPayload = new Payload(Arrays.asList(
			"CHAN_ID", "CHAN_LIST"));
	private static final Payload chanMessagePayload = new Payload(
			Arrays.asList("CHAN_CHANNAME", "CHAN_VISNAME", "CHAN_MESSAGE"));
	private static final Payload chanTargetPayload = new Payload(Arrays.asList(
			"CHAN_CHANNAME", "CHAN_T_MUD", "CHAN_T_USER", "CHAN_O_MSG",
			"CHAN_T_MSG", "CHAN_O_VISNAME", "CHAN_T_VISNAME"));
	private static final Payload chanUserReplyPayload = new Payload(
			Arrays.asList("CHAN_USERNAME", "CHAN_VISNAME", "CHAN_GENDER"));
	private static final Payload chanUserReqPayload = new Payload(
			Arrays.asList("CHAN_USERNAME"));

	private static final int hBeatDelay = 60;

	private Map<String, String> aliasToChannel = new ConcurrentHashMap<String, String>();
	private LPCMapping chanList = new LPCMapping();
	private Map<String, String> channelToAlias = new ConcurrentHashMap<String, String>();
	private Config config = null;
	private List<String> tuneinChannels = new ArrayList<String>();
	private LPCArray listening = new LPCArray();

	public I3Channel() {
	}

	/**
	 * @param packet
	 */
	private void chanEmoteHandler(Packet packet) {
		LPCString oMudName = packet.getLPCString(Payload.O_MUD);

		if (oMudName == null)
			return;

		LPCString channel = packet.getLPCString(chanEmotePayload
				.get("CHAN_CHANNAME"));
		LPCString emote = packet.getLPCString(chanEmotePayload
				.get("CHAN_EMOTE"));

		if (channel == null || emote == null)
			return;

		I3UCache ucache = ServiceType.I3UCACHE.getService();
		LPCString visName = packet.getLPCString(chanEmotePayload
				.get("CHAN_VISNAME"));

		if (ucache != null) {
			LPCString userName = packet.getLPCString(Payload.O_USER);

			ucache.checkUser(oMudName, userName, visName, true);

			String tmp = ucache.getVisname(oMudName, userName);

			if (tmp != null)
				visName = new LPCString(tmp);
		}

		if (visName == null)
			visName = packet.getLPCString(Payload.O_USER);

		if (!oMudName.toString().equals(Utils.getServerName()))
			visName = new LPCString(visName + "@" + oMudName);

		Player[] players = Intermud3.instance.getServer().getOnlinePlayers();
		String chan = channel.toString();
		String msg = emote.toString().replace("$N",
				"%^DARKYELLOW%^" + visName.toString() + "%^RESET%^");

		msg = Utils.toChatColor(msg);

		for (Player player : players) {
			String name = Utils.stripColor(player.getName());
			UUID uuid = player.getUniqueId();
			List<String> tunein = ucache.getTunein(name);

			if (tunein == null)
				tunein = ucache.getTunein(uuid);

			if (tunein == null)
				continue;

			Map<String, String> revAliases = ucache.getReverseAliases(name);

			if (revAliases == null)
				revAliases = ucache.getReverseAliases(uuid);

			if (revAliases != null && revAliases.containsKey(chan))
				chan = revAliases.get(chan);

			if (player.hasPermission("intermud3.use")
					&& player.hasPermission("intermud3.channel")
					&& tunein.contains(channel.toString()))
				player.sendMessage("[I3/" + chan + "] " + msg);
		}
	}

	/**
	 * @param packet
	 */
	public void chanListReply(Packet packet) {
		if (packet.size() != chanlistPayload.size()) {
			Log.error("We don't like chanlist packet size. Should be "
					+ chanlistPayload.size() + " but is " + packet.size());
			Log.error(packet.toMudMode());

			return;
		}

		int oMud = Payload.O_MUD;
		String oMudName = packet.getLPCString(oMud).toString();

		if (!oMudName.equals(Intermud3.network.getRouterName().toString())) {
			Log.error("Illegal access. Not from the router.");
			Log.error(packet.toMudMode());

			return;
		}

		LPCMapping list = packet
				.getLPCMapping(chanlistPayload.get("CHAN_LIST"));

		if (list != null) {
			LPCArray listeningCopy = new LPCArray(this.listening);

			for (Object channel : listeningCopy)
				if (!list.containsKey(channel)) {
					this.listening.remove(channel);
					this.tuneinChannels.remove(channel.toString());
				}
		}

		LPCInt chanlistID = packet.getLPCInt(chanlistPayload.get("CHAN_ID"));

		if (chanlistID.toInt() == Intermud3.network.getChanlistID().toInt())
			return;

		Intermud3.network.setChanlistID(chanlistID);

		if (list != null) {
			String msg = new String();

			for (Object obj : list.keySet()) {
				LPCString channel = new LPCString(obj.toString());
				LPCArray hostInfo = list.getLPCArray(channel);
				LPCArray chanInfo = this.chanList.getLPCArray(channel);

				if (channel.isEmpty()) {
					msg = "Empty channel name. Ignoring.";
				} else if (hostInfo == null && chanInfo != null) {
					msg = "Deleting channel '" + channel
							+ "' from the chanlist.";
					this.chanList.remove(channel);

					if (this.listening.contains(channel))
						sendChannelListen(channel, false);

					if (this.tuneinChannels.contains(channel.toString()))
						this.tuneinChannels.remove(channel.toString());
				} else if (hostInfo != null) {
					if (!this.chanList.isEmpty()) {
						if (chanInfo != null
								&& !chanInfo.toString().equals(
										hostInfo.toString())) {
							msg = "Updating data for channel '" + channel
									+ "' in the chanlist.";
						} else if (chanInfo == null) {
							msg = "Adding channel '" + channel
									+ "' to the chanlist.";
						}
					} else {
						msg = "Creating chanlist. Adding channel '" + channel
								+ "' to the chanlist.";
					}

					this.chanList.set(channel, hostInfo);

					if (!this.listening.contains(channel))
						sendChannelListen(channel, true);
				}

				if (!msg.isEmpty())
					Log.debug(msg);
			}
		}

		Intermud3.instance.saveConfig();
		saveConfig();
	}

	/**
	 * @param packet
	 */
	private void chanMessageHandler(Packet packet) {
		LPCString oMudName = packet.getLPCString(Payload.O_MUD);

		if (oMudName == null)
			return;

		LPCString channel = packet.getLPCString(chanMessagePayload
				.get("CHAN_CHANNAME"));
		LPCString message = packet.getLPCString(chanMessagePayload
				.get("CHAN_MESSAGE"));

		if (channel == null || message == null)
			return;

		I3UCache ucache = ServiceType.I3UCACHE.getService();
		LPCString visName = packet.getLPCString(chanMessagePayload
				.get("CHAN_VISNAME"));

		if (ucache != null) {
			LPCString userName = packet.getLPCString(Payload.O_USER);

			ucache.checkUser(oMudName, userName, visName, true);

			String tmp = ucache.getVisname(oMudName, userName);

			if (tmp != null)
				visName = new LPCString(tmp);
		}

		if (visName == null)
			visName = packet.getLPCString(Payload.O_USER);

		if (!oMudName.toString().equals(Utils.getServerName()))
			visName = new LPCString(visName + "@" + oMudName);

		Player[] players = Intermud3.instance.getServer().getOnlinePlayers();
		String chan = channel.toString();
		String msg = message.toString();

		msg = Utils.toChatColor(msg);

		for (Player player : players) {
			String name = Utils.stripColor(player.getName());
			UUID uuid = player.getUniqueId();
			List<String> tunein = ucache.getTunein(name);

			if (tunein == null)
				tunein = ucache.getTunein(uuid);

			if (tunein == null)
				continue;

			Map<String, String> revAliases = ucache.getReverseAliases(name);

			if (revAliases == null)
				revAliases = ucache.getReverseAliases(uuid);

			if (revAliases != null && revAliases.containsKey(chan))
				chan = revAliases.get(chan);

			if (player.hasPermission("intermud3.use")
					&& player.hasPermission("intermud3.channel")
					&& tunein.contains(channel.toString()))
				player.sendMessage("[I3/" + chan + "] " + ChatColor.GOLD
						+ visName + ChatColor.RESET + ": " + msg);
		}
	}

	/**
	 * @param packet
	 */
	private void chanTargetHandler(Packet packet) {
		LPCString oMudName = packet.getLPCString(Payload.O_MUD);

		if (oMudName == null)
			return;

		LPCString channel = packet.getLPCString(chanTargetPayload
				.get("CHAN_CHANNAME"));

		if (channel == null)
			return;

		LPCString oMessage = packet.getLPCString(chanTargetPayload
				.get("CHAN_O_MSG"));

		if (oMessage == null)
			return;

		LPCString tVisName = packet.getLPCString(chanTargetPayload
				.get("CHAN_T_VISNAME"));
		LPCString tMudName = packet.getLPCString(chanTargetPayload
				.get("CHAN_T_MUD"));

		if (!tMudName.toString().equals(Utils.getServerName()))
			tVisName = new LPCString(tVisName + "@" + tMudName);

		I3UCache ucache = ServiceType.I3UCACHE.getService();
		LPCString oVisName = packet.getLPCString(chanTargetPayload
				.get("CHAN_O_VISNAME"));

		if (ucache != null) {
			LPCString userName = packet.getLPCString(Payload.O_USER);

			ucache.checkUser(oMudName, userName, oVisName, true);

			String tmp = ucache.getVisname(oMudName, userName);

			if (tmp != null)
				oVisName = new LPCString(tmp);
		}

		if (oVisName == null)
			oVisName = packet.getLPCString(Payload.O_USER);

		if (!oMudName.toString().equals(Utils.getServerName()))
			oVisName = new LPCString(oVisName + "@" + oMudName);

		LPCString tMessage = packet.getLPCString(chanTargetPayload
				.get("CHAN_T_MSG"));

		String tMsg = tMessage.toString();

		tMsg = tMsg.replace("$N", "%^DARKYELLOW%^" + oVisName.toString()
				+ "%^RESET%^");
		tMsg = tMsg.replace("$O", "%^YELLOW%^" + tVisName.toString()
				+ "%^RESET%^");
		tMsg = Utils.toChatColor(tMsg);

		String oMsg = oMessage.toString();

		oMsg = oMsg.replace("$N", "%^DARKYELLOW%^" + oVisName.toString()
				+ "%^RESET%^");
		oMsg = oMsg.replace("$O", "%^YELLOW%^" + tVisName.toString()
				+ "%^RESET%^");
		oMsg = Utils.toChatColor(oMsg);

		Player[] players = Intermud3.instance.getServer().getOnlinePlayers();
		String chan = channel.toString();

		for (Player player : players) {
			String name = Utils.stripColor(player.getName());
			UUID uuid = player.getUniqueId();
			List<String> tunein = ucache.getTunein(name);

			if (tunein == null)
				tunein = ucache.getTunein(uuid);

			if (tunein == null)
				continue;

			Map<String, String> revAliases = ucache.getReverseAliases(name);

			if (revAliases == null)
				revAliases = ucache.getReverseAliases(uuid);

			if (revAliases != null && revAliases.containsKey(chan))
				chan = revAliases.get(chan);

			if (player.hasPermission("intermud3.use")
					&& player.hasPermission("intermud3.channel")
					&& tunein.contains(channel.toString())) {
				String listener = name.toLowerCase();

				if (listener.equals(oVisName.toString().toLowerCase()))
					player.sendMessage("[I3/" + chan + "] " + oMsg);
				else if (!tVisName.toString().contains("@")
						&& tVisName.toString().toLowerCase().equals(listener))
					player.sendMessage("[I3/" + chan + "] " + tMsg);
				else
					player.sendMessage("[I3/" + chan + "] " + oMsg);
			}
		}
	}

	/**
	 * @param packet
	 */
	private void chanUserReply(Packet packet) {
		I3UCache ucache = ServiceType.I3UCACHE.getService();

		if (ucache != null) {
			LPCString oMud = packet.getLPCString(Payload.O_MUD);
			LPCString uName = packet.getLPCString(chanUserReplyPayload
					.get("CHAN_USERNAME"));
			LPCString vName = packet.getLPCString(chanUserReplyPayload
					.get("CHAN_VISNAME"));
			LPCInt gender = packet.getLPCInt(chanUserReplyPayload
					.get("CHAN_GENDER"));

			ucache.addUserCache(oMud, uName, vName, gender);
		}
	}

	/**
	 * @param packet
	 */
	private void chanUserReq(Packet packet) {
		LPCString oMud = packet.getLPCString(Payload.O_MUD);
		LPCString uName = packet.getLPCString(chanUserReqPayload
				.get("CHAN_USERNAME"));
		OfflinePlayer player = Intermud3.instance.getServer().getOfflinePlayer(
				uName.toString());

		if (player == null) {
			I3Error error = ServiceType.I3ERROR.getService();

			if (error != null) {
				Packet errPacket = new Packet();

				errPacket.add(new LPCInt(0));
				errPacket.add(oMud);
				errPacket.add(packet.getLPCString(Payload.O_USER));
				errPacket.add(new LPCString("unk-user"));
				errPacket.add(new LPCString(uName + "@"
						+ packet.getLPCString(Payload.T_MUD)
						+ " was not found!"));
				errPacket.add(packet);
				error.send(errPacket);
			}
		}

		Packet payload = new Packet();

		payload.add(uName);
		payload.add(Utils.stripColor(player.getName()));
		payload.add(new LPCInt(2));

		Intermud3.network.sendToMud(PacketType.CHAN_USER_REPLY, null,
				oMud.toString(), payload);
	}

	/**
	 * @param packet
	 */
	private void chanWhoReply(Packet packet) {
	}

	/**
	 * @param packet
	 */
	private void chanWhoReq(Packet packet) {
	}

	public void create() {
		this.config = new Config(Intermud3.instance, "chanlist.yml");

		if (!this.config.getFile().exists()) {
			FileConfiguration root = this.config.getConfig();
			ConfigurationSection def = root.createSection("default");
			List<String> defTuneIn = new ArrayList<String>(Arrays.asList(
					"dchat", "dead_souls", "dead_test4", "imud_code",
					"minecraft"));

			def.addDefault("tunein", defTuneIn);

			ConfigurationSection aliases = def.createSection("aliases");

			aliases.addDefault("dc", "dchat");
			aliases.addDefault("ds", "dead_souls");
			aliases.addDefault("dt", "dead_test4");
			aliases.addDefault("ic", "imud_code");
			aliases.addDefault("mc", "minecraft");

			root.addDefault("chanList", "([])");
			this.config.saveConfig();
		}

		reloadConfig(false);

		ServiceType.I3CHANNEL.setVisibleOnRouter(true);
		Intermud3.callout.addHeartBeat(this, hBeatDelay);
	}

	public void debugInfo() {
		Log.debug("Channel: listening: "
				+ StringUtils.join(this.listening.iterator(), ", "));
		Log.debug("Channel: chanList:  "
				+ StringUtils.join(this.chanList.keySet().iterator(), ", "));
	}

	public Map<String, String> getAliases() {
		return this.aliasToChannel;
	}

	public LPCMapping getChanList() {
		return this.chanList.clone();
	}

	public LPCArray getListening() {
		return this.listening.clone();
	}

	public List<String> getTunein() {
		return this.tuneinChannels;
	}

	public void heartBeat() {
		if (this.listening.isEmpty())
			requestChanList();
	}

	/**
	 * Reload the chanlist config file and setup the local variables.
	 */
	public void reloadConfig() {
		reloadConfig(true);
	}

	/**
	 * Reload the chanlist config file and setup the local variables.
	 */
	public void reloadConfig(boolean flag) {
		this.config.reloadConfig();

		FileConfiguration root = this.config.getConfig();

		if (root.contains("chanList")) {
			try {
				this.chanList.setLPCData(Utils.toObject(root
						.getString("chanList")));
			} catch (I3Exception e) {
				e.printStackTrace();
			}
		}

		this.tuneinChannels.clear();
		this.aliasToChannel.clear();
		this.channelToAlias.clear();

		if (!root.contains("default")) {
			List<String> configAliases = new ArrayList<String>();

			this.tuneinChannels = new ArrayList<String>(
					root.getStringList("tunein"));
			configAliases = new ArrayList<String>(root.getStringList("aliases"));

			for (String s : configAliases) {
				String[] parts = StringUtils.split(s, ":");
				String alias = parts[0].trim();
				String channel = parts[1].trim();

				this.aliasToChannel.put(alias, channel);
				this.channelToAlias.put(channel, alias);
			}

			root.set("tunein", null);
			root.set("aliases", null);
		} else {
			ConfigurationSection def = root.getConfigurationSection("default");

			this.tuneinChannels = new ArrayList<String>(
					def.getStringList("tunein"));

			ConfigurationSection aliases = def
					.getConfigurationSection("aliases");

			for (String alias : aliases.getKeys(false)) {
				String channel = aliases.getString(alias);

				this.aliasToChannel.put(alias, channel);
				this.channelToAlias.put(channel, alias);
			}
		}

		if (flag)
			Log.info(this.config.getFile().getName() + " loaded.");
	}

	public void remove() {
		Intermud3.callout.removeHeartBeat(this);
		saveConfig();
		this.config.remove();
		chanEmotePayload.remove();
		chanlistPayload.remove();
		chanMessagePayload.remove();
		chanTargetPayload.remove();
		chanUserReplyPayload.remove();
		chanUserReqPayload.remove();

		LPCArray listeningCopy = this.listening.clone();

		for (Object obj : listeningCopy)
			sendChannelListen((LPCString) obj, false);

		// Clear out all lists.
		this.aliasToChannel.clear();
		this.chanList.clear();
		this.channelToAlias.clear();
		this.listening.clear();
		this.tuneinChannels.clear();

		// Remove references.
		this.aliasToChannel = null;
		this.chanList = null;
		this.channelToAlias = null;
		this.config = null;
		this.listening = null;
		this.tuneinChannels = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.org.rockthehalo.intermud3.services.ServiceTemplate#replyHandler(uk.org
	 * .rockthehalo.intermud3.LPC.Packet)
	 */
	@Override
	public void replyHandler(Packet packet) {
		String namedType = packet.getLPCString(Payload.TYPE).toString();
		PacketType type = PacketType.getNamedType(namedType);

		switch (type) {
		case CHAN_EMOTE:
			chanEmoteHandler(packet);

			break;
		case CHAN_FILTER_REPLY:
			Log.warn("Filter reply not recognized.");
			Log.warn("Packet: " + packet.toMudMode());

			break;
		case CHAN_MESSAGE:
			chanMessageHandler(packet);

			break;
		case CHAN_TARGET:
			chanTargetHandler(packet);

			break;
		case CHAN_USER_REPLY:
			chanUserReply(packet);

			break;
		case CHAN_WHO_REPLY:
			chanWhoReply(packet);

			break;
		case CHANLIST_REPLY:
			chanListReply(packet);

			break;
		default:
			Log.error("Unhandled channel reply handler: " + namedType);
			Log.error("Packet: " + packet.toMudMode());

			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.org.rockthehalo.intermud3.services.ServiceTemplate#reqHandler(uk.org
	 * .rockthehalo.intermud3.LPC.Packet)
	 */
	@Override
	public void reqHandler(Packet packet) {
		String namedType = packet.getLPCString(Payload.TYPE).toString();
		PacketType type = PacketType.getNamedType(namedType);

		switch (type) {
		case CHAN_FILTER_REQ:
			Log.warn("Filter request not recognized.");
			Log.warn("Packet: " + packet.toMudMode());

			break;
		case CHAN_USER_REQ:
			chanUserReq(packet);

			break;
		case CHAN_WHO_REQ:
			chanWhoReq(packet);

			break;
		default:
			Log.error("Unhandled channel request handler: " + namedType);
			Log.error("Packet: " + packet.toMudMode());

			break;
		}
	}

	public void requestChanList() {
		if (Intermud3.network.isRouterConnected()) {
			Intermud3.network.setChanlistID(0);
			Intermud3.network.sendToRouter("chanlist-req", null, null);
		}
	}

	public void saveConfig() {
		FileConfiguration root = this.config.getConfig();

		if (root.contains("tunein"))
			root.set("tunein", null);

		if (root.contains("aliases"))
			root.set("aliases", null);

		ConfigurationSection def = root.createSection("default");

		def.set("tunein", this.tuneinChannels);

		ConfigurationSection aliases = def.createSection("aliases");

		for (String alias : this.aliasToChannel.keySet())
			aliases.set(alias, this.aliasToChannel.get(alias));

		root.set("chanList", Utils.toMudMode(this.chanList));

		this.config.saveConfig();
	}

	public void sendChannelListen(LPCString channel, boolean flag) {
		if (flag) {
			if (!this.chanList.containsKey(channel))
				return;
		} else {
			if (!this.listening.contains(channel))
				return;
		}

		tuneChannel(channel, flag);

		if (!flag)
			this.listening.remove(channel);
		else
			this.listening.add(channel);
	}

	public void sendChannelListen(String channel, boolean flag) {
		sendChannelListen(new LPCString(channel), flag);
	}

	/**
	 * @param mudname
	 * @param username
	 */
	public void sendChanUserReq(String mudname, String username) {
		Packet payload = new Packet();

		payload.add(new LPCString(username));
		Intermud3.network.sendToMud(PacketType.CHAN_USER_REQ, null, mudname,
				payload);
	}

	public void sendEmote(String chan, String plrName, String msg) {
		Packet payload = new Packet();

		if (this.aliasToChannel.containsKey(chan)
				&& !this.aliasToChannel.containsValue(chan))
			chan = this.aliasToChannel.get(chan);

		plrName = Utils.stripColor(plrName);
		msg = Utils.toPinkfish(msg);
		payload.add(new LPCString(chan));
		payload.add(new LPCString(plrName));
		payload.add(new LPCString("$N " + msg));
		Intermud3.network.sendToAll(PacketType.CHAN_EMOTE, plrName, payload);
	}

	public void sendMessage(String chan, String plrName, String msg) {
		Packet payload = new Packet();

		if (this.aliasToChannel.containsKey(chan)
				&& !this.aliasToChannel.containsValue(chan))
			chan = this.aliasToChannel.get(chan);

		plrName = Utils.stripColor(plrName);
		msg = Utils.toPinkfish(msg);
		payload.add(new LPCString(chan));
		payload.add(new LPCString(plrName));
		payload.add(new LPCString(msg));
		Intermud3.network.sendToAll(PacketType.CHAN_MESSAGE, plrName, payload);
	}

	public void setAlias(String alias, String channel) {
		if (channel == null) {
			channel = this.aliasToChannel.get(alias);
			this.aliasToChannel.remove(alias);
			this.channelToAlias.remove(channel);
		} else {
			this.aliasToChannel.put(alias, channel);
			this.channelToAlias.put(channel, alias);
		}

		saveConfig();
	}

	public void showChannelsListening(CommandSender sender) {
		List<String> list = new ArrayList<String>();

		if (!Utils.isPlayer(sender)) {
			for (Object obj : this.tuneinChannels)
				list.add(ChatColor.GREEN + obj.toString());
		} else {
			I3UCache i3UCache = ServiceType.I3UCACHE.getService();

			if (i3UCache != null) {
				String name = Utils.stripColor(((Player) sender).getName());
				List<Object> user = i3UCache.getLocalUser(name);

				if (user != null) {
					@SuppressWarnings("unchecked")
					List<String> tunein = (List<String>) user
							.get(I3UCache.TUNEIN);

					for (String channel : tunein)
						list.add(ChatColor.GREEN + channel);
				}
			}
		}

		Collections.sort(list);

		String listeningChannels = StringUtils.join(list, ChatColor.RESET
				+ ", ");

		sender.sendMessage("Listening (" + list.size() + "): "
				+ listeningChannels);
	}

	@SuppressWarnings("unchecked")
	public void showChannelsAvailable(CommandSender sender) {
		I3UCache i3UCache = null;
		List<Object> user = null;
		List<String> tunein = null;
		boolean isPlayer = Utils.isPlayer(sender);

		if (isPlayer) {
			i3UCache = ServiceType.I3UCACHE.getService();

			if (i3UCache != null) {
				String name = Utils.stripColor(((Player) sender).getName());
				user = i3UCache.getLocalUser(name);

				if (user != null)
					tunein = (List<String>) user.get(I3UCache.TUNEIN);
			}
		}

		List<String> list = new ArrayList<String>();

		for (Object obj : this.chanList.keySet()) {
			String key = obj.toString();

			if (!isPlayer) {
				if (!this.listening.contains(key))
					list.add(ChatColor.GREEN + key);
			} else {
				if (tunein != null) {
					if (!tunein.contains(key))
						list.add(ChatColor.GREEN + key);
				}
			}
		}

		Collections.sort(list);

		String availableChannels = StringUtils.join(list, ChatColor.RESET
				+ ", ");

		sender.sendMessage("Available (" + list.size() + "): "
				+ availableChannels);
	}

	public void showChannelAliases(CommandSender sender) {
		List<String> list = new ArrayList<String>();

		if (!Utils.isPlayer(sender)) {
			for (String key : this.aliasToChannel.keySet()) {
				String val = this.aliasToChannel.get(key);

				list.add(ChatColor.GREEN + key + ChatColor.RESET + ": "
						+ ChatColor.GREEN + val);
			}
		} else {
			I3UCache i3UCache = ServiceType.I3UCACHE.getService();

			if (i3UCache != null) {
				String name = Utils.stripColor(((Player) sender).getName());
				List<Object> user = i3UCache.getLocalUser(name);

				if (user != null) {
					@SuppressWarnings("unchecked")
					Map<String, String> aliases = (Map<String, String>) user
							.get(I3UCache.ALIASES);

					for (String key : aliases.keySet()) {
						String val = aliases.get(key);

						list.add(ChatColor.GREEN + key + ChatColor.RESET + ": "
								+ ChatColor.GREEN + val);
					}
				}
			}
		}

		Collections.sort(list);
		sender.sendMessage("Aliases (" + list.size() + "):");

		for (String line : list)
			sender.sendMessage("  " + line);
	}

	private void tuneChannel(LPCString channel, boolean flag) {
		if (flag) {
			if (!this.chanList.containsKey(channel))
				return;
		} else {
			if (!this.listening.contains(channel))
				return;
		}

		Packet packet = new Packet();

		packet.add(channel);
		packet.add(flag ? new LPCInt(1) : new LPCInt(0));
		Intermud3.network.sendToRouter("channel-listen", null, packet);
	}

	public void tuneIn(String channel) {
		if (!this.tuneinChannels.contains(channel))
			this.tuneinChannels.add(channel);

		saveConfig();
	}

	public void tuneOut(String channel) {
		if (this.tuneinChannels.contains(channel))
			this.tuneinChannels.remove(channel);

		saveConfig();
	}
}

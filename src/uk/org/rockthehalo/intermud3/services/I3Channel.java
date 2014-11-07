package uk.org.rockthehalo.intermud3.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

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
	private static final int hBeatDelay = 60;

	public static final Payload chanEmotePayload = new Payload(Arrays.asList(
			"CHAN_CHANNAME", "CHAN_VISNAME", "CHAN_EMOTE"));
	public static final Payload chanlistPayload = new Payload(Arrays.asList(
			"CHAN_ID", "CHAN_LIST"));
	public static final Payload chanListenPayload = new Payload(Arrays.asList(
			"CHAN_CHANNAME", "CHAN_STATE"));
	public static final Payload chanMessagePayload = new Payload(Arrays.asList(
			"CHAN_CHANNAME", "CHAN_VISNAME", "CHAN_MESSAGE"));
	public static final Payload chanTargetPayload = new Payload(Arrays.asList(
			"CHAN_CHANNAME", "CHAN_T_MUD", "CHAN_T_USER", "CHAN_O_MSG",
			"CHAN_T_MSG", "CHAN_O_VISNAME", "CHAN_T_VISNAME"));
	public static final Payload chanUserReplyPayload = new Payload(
			Arrays.asList("CHAN_USERNAME", "CHAN_VISNAME", "CHAN_GENDER"));
	public static final Payload chanUserReqPayload = new Payload(
			Arrays.asList("CHAN_USERNAME"));

	private final Map<String, String> aliasToChannel = Collections
			.synchronizedMap(new HashMap<String, String>());
	private final LPCMapping chanList = new LPCMapping();
	private final Map<String, String> channelToAlias = Collections
			.synchronizedMap(new HashMap<String, String>());
	private final LPCArray listening = new LPCArray();

	private LPCInt chanlistID = new LPCInt();
	private Config config = null;
	private List<String> tuneinChannels = new ArrayList<String>();

	public I3Channel() {
	}

	/**
	 * @param packet
	 */
	private void chanEmoteHandler(final Packet packet) {
		final LPCString oMudName = packet.getLPCString(Payload.O_MUD);

		if (oMudName == null)
			return;

		final LPCString channel = packet.getLPCString(chanEmotePayload
				.get("CHAN_CHANNAME"));
		final LPCString emote = packet.getLPCString(chanEmotePayload
				.get("CHAN_EMOTE"));

		if (channel == null || emote == null)
			return;

		final I3UCache ucache = ServiceType.I3UCACHE.getService();
		LPCString visName = packet.getLPCString(chanEmotePayload
				.get("CHAN_VISNAME"));

		if (ucache != null) {
			LPCString userName = packet.getLPCString(Payload.O_USER);

			ucache.checkUser(oMudName, userName, visName, true);

			final String tmp = ucache.getVisname(oMudName, userName);

			if (tmp != null)
				visName = new LPCString(tmp);
		}

		if (visName == null)
			visName = packet.getLPCString(Payload.O_USER);

		if (!oMudName.toString().equals(Utils.getServerName()))
			visName = new LPCString(visName + "@" + oMudName);

		final Player[] players = Intermud3.instance.getServer()
				.getOnlinePlayers();
		String chan = channel.toString();
		String msg = emote.toString().replace("$N",
				"%^DARKYELLOW%^" + visName.toString() + "%^RESET%^");

		msg = Utils.toChatColor(msg);

		for (final Player player : players) {
			final String name = Utils.stripColor(player.getName());
			final UUID uuid = player.getUniqueId();
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
	public void chanListReply(final Packet packet) {
		if (packet.size() != chanlistPayload.size()) {
			Log.error("We don't like chanlist packet size. Should be "
					+ chanlistPayload.size() + " but is " + packet.size());
			Log.error(packet.toMudMode());

			return;
		}

		final String oMudName = packet.getLPCString(Payload.O_MUD).toString();

		if (!oMudName.equals(Intermud3.network.getRouterName().toString())) {
			Log.error("Illegal access. Not from the router.");
			Log.error(packet.toMudMode());

			return;
		}

		final LPCInt chanlistID = packet.getLPCInt(chanlistPayload
				.get("CHAN_ID"));

		if (chanlistID.equals(this.chanlistID))
			return;

		setChanlistID(chanlistID);

		final LPCMapping list = packet.getLPCMapping(chanlistPayload
				.get("CHAN_LIST"));

		if (list != null) {
			final LPCArray listeningCopy = new LPCArray(this.listening);

			for (final Object channel : listeningCopy)
				if (!list.containsKey(channel)) {
					this.listening.remove(channel);
					this.tuneinChannels.remove(channel.toString());
				}
		}

		if (list != null) {
			for (final Entry<Object, Object> channel : list.entrySet()) {
				final LPCString channame = (LPCString) channel.getKey();
				final Object value = channel.getValue();
				LPCArray hostInfo = null;
				String msg = "";

				if (Utils.isLPCArray(value))
					hostInfo = (LPCArray) value;

				final LPCArray chanInfo = this.chanList.getLPCArray(channame);

				if (channame.isEmpty()) {
					msg = "Empty channel name. Ignoring.";
				} else if (hostInfo == null && chanInfo != null) {
					msg = "Deleting channel '" + channame
							+ "' from the chanlist.";
					this.chanList.remove(channame);

					if (this.listening.contains(channame))
						sendChannelListen(channame, false);

					if (this.tuneinChannels.contains(channame.toString()))
						this.tuneinChannels.remove(channame.toString());
				} else if (hostInfo != null) {
					if (!this.chanList.isEmpty()) {
						if (chanInfo != null && !chanInfo.equals(hostInfo)) {
							msg = "Updating data for channel '" + channame
									+ "' in the chanlist.";
						} else if (chanInfo == null) {
							msg = "Adding channel '" + channame
									+ "' to the chanlist.";
						}
					} else {
						msg = "Creating chanlist. Adding channel '" + channame
								+ "' to the chanlist.";
					}

					this.chanList.put(channame, hostInfo);

					if (!this.listening.contains(channame))
						sendChannelListen(channame, true);
				}

				if (!msg.isEmpty())
					Log.debug(msg);
			}
		}

		saveConfig();
	}

	/**
	 * @param packet
	 */
	private void chanMessageHandler(final Packet packet) {
		final LPCString oMudName = packet.getLPCString(Payload.O_MUD);

		if (oMudName == null)
			return;

		final LPCString channel = packet.getLPCString(chanMessagePayload
				.get("CHAN_CHANNAME"));
		final LPCString message = packet.getLPCString(chanMessagePayload
				.get("CHAN_MESSAGE"));

		if (channel == null || message == null)
			return;

		final I3UCache ucache = ServiceType.I3UCACHE.getService();
		LPCString visName = packet.getLPCString(chanMessagePayload
				.get("CHAN_VISNAME"));

		if (ucache != null) {
			final LPCString userName = packet.getLPCString(Payload.O_USER);

			ucache.checkUser(oMudName, userName, visName, true);

			final String tmp = ucache.getVisname(oMudName, userName);

			if (tmp != null)
				visName = new LPCString(tmp);
		}

		if (visName == null)
			visName = packet.getLPCString(Payload.O_USER);

		if (!oMudName.toString().equals(Utils.getServerName()))
			visName = new LPCString(visName + "@" + oMudName);

		final Player[] players = Intermud3.instance.getServer()
				.getOnlinePlayers();
		String chan = channel.toString();
		String msg = message.toString();

		msg = Utils.toChatColor(msg);

		for (final Player player : players) {
			final String name = Utils.stripColor(player.getName());
			final UUID uuid = player.getUniqueId();
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
	private void chanTargetHandler(final Packet packet) {
		final LPCString oMudName = packet.getLPCString(Payload.O_MUD);

		if (oMudName == null)
			return;

		final LPCString channel = packet.getLPCString(chanTargetPayload
				.get("CHAN_CHANNAME"));

		if (channel == null)
			return;

		final LPCString oMessage = packet.getLPCString(chanTargetPayload
				.get("CHAN_O_MSG"));

		if (oMessage == null)
			return;

		LPCString tVisName = packet.getLPCString(chanTargetPayload
				.get("CHAN_T_VISNAME"));
		final LPCString tMudName = packet.getLPCString(chanTargetPayload
				.get("CHAN_T_MUD"));

		if (!tMudName.equals(Utils.getServerName()))
			tVisName = new LPCString(tVisName + "@" + tMudName);

		final I3UCache ucache = ServiceType.I3UCACHE.getService();
		LPCString oVisName = packet.getLPCString(chanTargetPayload
				.get("CHAN_O_VISNAME"));

		if (ucache != null) {
			final LPCString userName = packet.getLPCString(Payload.O_USER);

			ucache.checkUser(oMudName, userName, oVisName, true);

			final String tmp = ucache.getVisname(oMudName, userName);

			if (tmp != null)
				oVisName = new LPCString(tmp);
		}

		if (oVisName == null)
			oVisName = packet.getLPCString(Payload.O_USER);

		if (!oMudName.toString().equals(Utils.getServerName()))
			oVisName = new LPCString(oVisName + "@" + oMudName);

		final LPCString tMessage = packet.getLPCString(chanTargetPayload
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

		final Player[] players = Intermud3.instance.getServer()
				.getOnlinePlayers();
		String chan = channel.toString();

		for (final Player player : players) {
			final String name = Utils.stripColor(player.getName());
			final UUID uuid = player.getUniqueId();
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
				final String listener = name.toLowerCase();

				if (listener.equals(oVisName.toLowerCase()))
					player.sendMessage("[I3/" + chan + "] " + oMsg);
				else if (!tVisName.contains("@")
						&& tVisName.toLowerCase().equals(listener))
					player.sendMessage("[I3/" + chan + "] " + tMsg);
				else
					player.sendMessage("[I3/" + chan + "] " + oMsg);
			}
		}
	}

	/**
	 * @param packet
	 */
	private void chanUserReply(final Packet packet) {
		final I3UCache ucache = ServiceType.I3UCACHE.getService();

		if (ucache != null) {
			final LPCString oMud = packet.getLPCString(Payload.O_MUD);
			final LPCString uName = packet.getLPCString(chanUserReplyPayload
					.get("CHAN_USERNAME"));
			final LPCString vName = packet.getLPCString(chanUserReplyPayload
					.get("CHAN_VISNAME"));
			final LPCInt gender = packet.getLPCInt(chanUserReplyPayload
					.get("CHAN_GENDER"));

			ucache.addUserCache(oMud, uName, vName, gender);
		}
	}

	/**
	 * @param packet
	 */
	private void chanUserReq(final Packet packet) {
		final LPCString oMud = packet.getLPCString(Payload.O_MUD);
		final LPCString uName = packet.getLPCString(chanUserReqPayload
				.get("CHAN_USERNAME"));
		final OfflinePlayer player = Intermud3.instance.getServer()
				.getOfflinePlayer(uName.toString());
		final Packet payload = new Packet();

		if (player == null) {
			final I3Error error = ServiceType.I3ERROR.getService();

			if (error != null) {
				payload.add(new LPCInt(0));
				payload.add(oMud);
				payload.add(packet.getLPCString(Payload.O_USER));
				payload.add(new LPCString("unk-user"));
				payload.add(new LPCString(uName + "@"
						+ packet.getLPCString(Payload.T_MUD)
						+ " was not found!"));
				payload.add(packet);
				error.send(payload);
			}

			return;
		}

		payload.add(uName);
		payload.add(Utils.stripColor(player.getName()));
		payload.add(new LPCInt(2));

		Intermud3.network.sendToMud(PacketType.CHAN_USER_REPLY, null,
				oMud.toString(), payload);
	}

	/**
	 * @param packet
	 */
	private void chanWhoReply(final Packet packet) {
	}

	/**
	 * @param packet
	 */
	private void chanWhoReq(final Packet packet) {
	}

	public void create() {
		this.config = new Config(Intermud3.instance, "chanlist.yml");

		if (!this.config.getFile().exists()) {
			final FileConfiguration root = this.config.getConfig();
			final ConfigurationSection def = root.createSection("default");
			final List<String> defTuneIn = new ArrayList<String>(
					Arrays.asList("minecraft"));

			def.addDefault("tunein", defTuneIn);

			final ConfigurationSection aliases = def.createSection("aliases");

			aliases.addDefault("mc", "minecraft");

			root.addDefault("chanlistID", 0);
			root.addDefault("chanList", "([])");

			this.config.saveConfig();
		}

		reloadConfig();

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

	/**
	 * @return the chanlistID
	 */
	public LPCInt getChanlistID() {
		return this.chanlistID;
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
		reloadConfig(false);
	}

	/**
	 * Reload the chanlist config file and setup the local variables.
	 */
	public void reloadConfig(final boolean flag) {
		this.config.reloadConfig();

		final FileConfiguration root = this.config.getConfig();

		this.chanlistID = new LPCInt(root.getLong("chanlistID", 0));

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
			this.tuneinChannels = new ArrayList<String>(
					root.getStringList("tunein"));

			final List<String> configAliases = new ArrayList<String>(
					root.getStringList("aliases"));

			for (final String s : configAliases) {
				final String[] parts = StringUtils.split(s, ":");
				final String alias = parts[0].trim();
				final String channel = parts[1].trim();

				this.aliasToChannel.put(alias, channel);
				this.channelToAlias.put(channel, alias);
			}

			root.set("tunein", null);
			root.set("aliases", null);
		} else {
			final ConfigurationSection def = root
					.getConfigurationSection("default");

			this.tuneinChannels = new ArrayList<String>(
					def.getStringList("tunein"));

			final ConfigurationSection aliases = def
					.getConfigurationSection("aliases");

			for (final String alias : aliases.getKeys(false)) {
				final String channel = aliases.getString(alias);

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

		final LPCArray listeningCopy = this.listening.clone();

		for (final Object obj : listeningCopy)
			sendChannelListen((LPCString) obj, false);

		// Clear out all lists.
		this.aliasToChannel.clear();
		this.chanList.clear();
		this.channelToAlias.clear();
		this.listening.clear();
		this.tuneinChannels.clear();

		// Remove references.
		this.chanlistID = null;
		this.config = null;
		this.tuneinChannels = null;
	}

	public void removeAvailableChannel(final LPCString channel) {
		if (this.chanList.containsKey(channel))
			this.chanList.remove(channel);
	}

	public void removeAvailableChannel(final String channel) {
		removeAvailableChannel(new LPCString(channel));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.org.rockthehalo.intermud3.services.ServiceTemplate#replyHandler(uk.org
	 * .rockthehalo.intermud3.LPC.Packet)
	 */
	@Override
	public void replyHandler(final Packet packet) {
		final String namedType = packet.getLPCString(Payload.TYPE).toString();
		final PacketType type = PacketType.getNamedType(namedType);

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
	public void reqHandler(final Packet packet) {
		final String namedType = packet.getLPCString(Payload.TYPE).toString();
		final PacketType type = PacketType.getNamedType(namedType);

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
			setChanlistID(0);
			Intermud3.network.sendToRouter("chanlist-req", null, null);
		}
	}

	public void saveConfig() {
		saveConfig(false);
	}

	public void saveConfig(final boolean flag) {
		// Clear the configuration.
		this.config.clearConfig();

		final FileConfiguration root = this.config.getConfig();
		final ConfigurationSection def = root.createSection("default");

		def.set("tunein", this.tuneinChannels);

		final ConfigurationSection aliases = def.createSection("aliases");

		for (final Entry<String, String> alias : this.aliasToChannel.entrySet())
			aliases.set(alias.getKey(), alias.getValue());

		root.set("chanlistID", this.chanlistID.toNum());
		root.set("chanList", Utils.toMudMode(this.chanList));

		this.config.saveConfig();

		if (flag)
			Log.info(this.config.getFile().getName() + " saved.");
	}

	public void sendChannelListen(final LPCString channel, final boolean flag) {
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

	public void sendChannelListen(final String channel, final boolean flag) {
		sendChannelListen(new LPCString(channel), flag);
	}

	/**
	 * @param mudname
	 * @param username
	 */
	public void sendChanUserReq(final String mudname, final String username) {
		final Packet payload = new Packet();

		payload.add(new LPCString(username));
		Intermud3.network.sendToMud(PacketType.CHAN_USER_REQ, null, mudname,
				payload);
	}

	public void sendEmote(String chan, String plrName, String msg) {
		final Packet payload = new Packet();

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
		final Packet payload = new Packet();

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

	public void setAlias(final String alias, String channel) {
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

	/**
	 * @param chanlistID
	 *            the chanlistID to set
	 */
	public void setChanlistID(final long chanlistID) {
		this.chanlistID = new LPCInt(chanlistID);
		this.config.getConfig().set("chanlistID", chanlistID);
	}

	/**
	 * @param chanlistID
	 *            the chanlistID to set
	 */
	public void setChanlistID(final LPCInt chanlistID) {
		this.chanlistID = new LPCInt(chanlistID);
		this.config.getConfig().set("chanlistID", chanlistID.toNum());
	}

	public void showChannelsListening(final CommandSender sender) {
		final List<String> list = new ArrayList<String>();

		if (!Utils.isPlayer(sender)) {
			for (final Object obj : this.listening)
				list.add(ChatColor.GREEN + obj.toString());
		} else {
			final I3UCache i3UCache = ServiceType.I3UCACHE.getService();

			if (i3UCache != null) {
				final String name = Utils.stripColor(((Player) sender)
						.getName());
				final List<Object> user = i3UCache.getLocalUser(name);

				if (user != null) {
					@SuppressWarnings("unchecked")
					final List<String> tunein = (List<String>) user
							.get(I3UCache.TUNEIN);

					for (final String channel : tunein)
						list.add(ChatColor.GREEN + channel);
				}
			}
		}

		Collections.sort(list);

		final String listeningChannels = StringUtils.join(list, ChatColor.RESET
				+ ", ");

		sender.sendMessage("Listening (" + list.size() + "): "
				+ listeningChannels);
	}

	@SuppressWarnings("unchecked")
	public void showChannelsAvailable(final CommandSender sender) {
		List<String> tunein = null;
		final boolean isPlayer = Utils.isPlayer(sender);

		if (isPlayer) {
			final I3UCache i3UCache = ServiceType.I3UCACHE.getService();

			if (i3UCache != null) {
				final String name = Utils.stripColor(((Player) sender)
						.getName());
				final List<Object> user = i3UCache.getLocalUser(name);

				if (user != null)
					tunein = (List<String>) user.get(I3UCache.TUNEIN);
			}
		}

		final List<String> list = new ArrayList<String>();

		for (final Object key : this.chanList.keySet()) {
			final LPCString channel = (LPCString) key;

			if (!isPlayer) {
				if (!this.listening.contains(channel))
					list.add(ChatColor.GREEN + channel.toString());
			} else {
				if (tunein != null) {
					if (!tunein.contains(channel.toString()))
						list.add(ChatColor.GREEN + channel.toString());
				}
			}
		}

		Collections.sort(list);

		final String availableChannels = StringUtils.join(list, ChatColor.RESET
				+ ", ");

		sender.sendMessage("Available (" + list.size() + "): "
				+ availableChannels);
	}

	public void showChannelAliases(final CommandSender sender) {
		final List<String> list = new ArrayList<String>();

		if (!Utils.isPlayer(sender)) {
			for (final Entry<String, String> a2c : this.aliasToChannel
					.entrySet()) {
				final String key = a2c.getKey();
				final String val = a2c.getValue();

				list.add(ChatColor.GREEN + key + ChatColor.RESET + ": "
						+ ChatColor.GREEN + val);
			}
		} else {
			final I3UCache i3UCache = ServiceType.I3UCACHE.getService();

			if (i3UCache != null) {
				final String name = Utils.stripColor(((Player) sender)
						.getName());
				final List<Object> user = i3UCache.getLocalUser(name);

				if (user != null) {
					@SuppressWarnings("unchecked")
					final Map<String, String> aliases = (Map<String, String>) user
							.get(I3UCache.ALIASES);

					for (final Entry<String, String> alias : aliases.entrySet()) {
						final String key = alias.getKey();
						final String val = alias.getValue();

						list.add(ChatColor.GREEN + key + ChatColor.RESET + ": "
								+ ChatColor.GREEN + val);
					}
				}
			}
		}

		Collections.sort(list);
		sender.sendMessage("Aliases (" + list.size() + "):");

		for (final String line : list)
			sender.sendMessage("  " + line);
	}

	private void tuneChannel(final LPCString channel, final boolean flag) {
		if (flag) {
			if (!this.chanList.containsKey(channel))
				return;
		} else {
			if (!this.listening.contains(channel))
				return;
		}

		final Packet packet = new Packet();

		packet.add(channel);
		packet.add(flag ? new LPCInt(1) : new LPCInt(0));
		Intermud3.network.sendToRouter("channel-listen", null, packet);
	}

	public void tuneIn(final String channel) {
		if (!this.tuneinChannels.contains(channel))
			this.tuneinChannels.add(channel);

		saveConfig();
	}

	public void tuneOut(final String channel) {
		if (this.tuneinChannels.contains(channel))
			this.tuneinChannels.remove(channel);

		saveConfig();
	}
}

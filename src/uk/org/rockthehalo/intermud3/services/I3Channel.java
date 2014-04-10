package uk.org.rockthehalo.intermud3.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.PacketTypes.BasePayload;
import uk.org.rockthehalo.intermud3.LPC.PacketTypes.PacketType;

public class I3Channel extends ServiceTemplate {
	public enum ChanEmotePayload {
		CHANNAME(6), VISNAME(7), EMOTE(8);

		private int index;

		private ChanEmotePayload(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public static int size() {
			return BasePayload.size() + ChanEmotePayload.values().length;
		}
	}

	public enum ChanlistPayload {
		ID(6), LIST(7);

		private int index;

		private ChanlistPayload(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public static int size() {
			return BasePayload.size() + ChanlistPayload.values().length;
		}
	}

	public enum ChanMessagePayload {
		CHANNAME(6), VISNAME(7), MESSAGE(8);

		private int index;

		private ChanMessagePayload(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public static int size() {
			return BasePayload.size() + ChanMessagePayload.values().length;
		}
	}

	public enum ChanTargetPayload {
		CHANNAME(6), T_MUD(7), T_USER(8), O_MSG(9), T_MSG(10), O_VISNAME(11), T_VISNAME(
				12);

		private int index;

		private ChanTargetPayload(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public static int size() {
			return BasePayload.size() + ChanTargetPayload.values().length;
		}
	}

	public enum ChanUserReplyPayload {
		USERNAME(6), VISNAME(7), GENDER(8);

		private int index;

		private ChanUserReplyPayload(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public static int size() {
			return BasePayload.size() + ChanUserReplyPayload.values().length;
		}
	}

	public enum ChanUserReqPayload {
		USERNAME(6);

		private int index;

		private ChanUserReqPayload(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public static int size() {
			return BasePayload.size() + ChanUserReqPayload.values().length;
		}
	}

	private final int hBeatDelay = 60;

	private List<String> configTunein = new ArrayList<String>();
	private List<String> configAliases = new ArrayList<String>();
	private Vector<String> tuneinChannels = new Vector<String>();
	private Map<String, String> aliasToChannel = new ConcurrentHashMap<String, String>();
	private Map<String, String> channelToAlias = new ConcurrentHashMap<String, String>();
	private FileConfiguration chanlistConfig = null;
	private File chanlistConfigFile = null;
	private LPCMapping chanList = new LPCMapping();
	private LPCArray listening = new LPCArray();

	public I3Channel() {
	}

	/**
	 * @param packet
	 */
	private void chanEmoteHandler(Packet packet) {
		LPCString oMudName = packet.getLPCString(BasePayload.O_MUD.getIndex());

		if (oMudName == null)
			return;

		LPCString channel = packet.getLPCString(ChanEmotePayload.CHANNAME
				.getIndex());
		LPCString emote = packet
				.getLPCString(ChanEmotePayload.EMOTE.getIndex());

		if (channel == null || emote == null)
			return;

		LPCString visName = packet.getLPCString(ChanEmotePayload.VISNAME
				.getIndex());

		if (visName == null)
			visName = packet.getLPCString(BasePayload.O_USER.getIndex());

		if (!oMudName.toString().equals(
				Intermud3.instance.getServer().getServerName()))
			visName = new LPCString(visName + "@" + oMudName);

		Player[] players = Intermud3.instance.getServer().getOnlinePlayers();
		String chan = channel.toString();
		String msg = emote.toString().replace("$N", visName.toString());

		if (this.channelToAlias.containsKey(chan))
			chan = this.channelToAlias.get(chan);

		msg = Utils.toChatColor(msg);

		for (Player player : players)
			if (player.hasPermission("intermud3.emote"))
				player.sendMessage("[I3/" + channel + "] " + msg);
	}

	/**
	 * @param packet
	 */
	public void chanListReply(Packet packet) {
		if (packet.size() != ChanlistPayload.size()) {
			Log.error("We don't like chanlist packet size. Should be "
					+ ChanlistPayload.size() + " but is " + packet.size());
			Log.error(packet.toMudMode());

			return;
		}

		int oMud = BasePayload.O_MUD.getIndex();
		String oMudName = packet.getLPCString(oMud).toString();

		if (!oMudName.equals(Intermud3.network.getRouterName().toString())) {
			Log.error("Illegal access. Not from the router.");
			Log.error(packet.toMudMode());

			return;
		}

		LPCMapping list = packet.getLPCMapping(ChanlistPayload.LIST.getIndex());

		if (list != null) {
			LPCArray listeningCopy = new LPCArray(this.listening);

			for (Object channel : listeningCopy)
				if (!list.containsKey(channel)) {
					this.listening.remove(channel);
					this.tuneinChannels.remove(channel.toString());
				}
		}

		LPCInt chanlistID = packet.getLPCInt(ChanlistPayload.ID.getIndex());

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
					if (this.chanList.size() != 0) {
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

					if (!this.listening.contains(channel)
							&& this.tuneinChannels.contains(channel.toString()))
						sendChannelListen(channel, true);
				}

				if (!msg.isEmpty())
					Log.debug(msg);
			}
		}

		Intermud3.instance.saveConfig();
		saveChanlistConfig();
	}

	/**
	 * @param packet
	 */
	private void chanMessageHandler(Packet packet) {
		LPCString oMudName = packet.getLPCString(BasePayload.O_MUD.getIndex());

		if (oMudName == null)
			return;

		LPCString channel = packet.getLPCString(ChanMessagePayload.CHANNAME
				.getIndex());
		LPCString message = packet.getLPCString(ChanMessagePayload.MESSAGE
				.getIndex());

		if (channel == null || message == null)
			return;

		LPCString visName = packet.getLPCString(ChanMessagePayload.VISNAME
				.getIndex());

		if (visName == null)
			visName = packet.getLPCString(BasePayload.O_USER.getIndex());

		if (!oMudName.toString().equals(
				Intermud3.instance.getServer().getServerName()))
			visName = new LPCString(visName + "@" + oMudName);

		Player[] players = Intermud3.instance.getServer().getOnlinePlayers();
		String chan = channel.toString();
		String msg = message.toString();

		if (this.channelToAlias.containsKey(chan))
			chan = this.channelToAlias.get(chan);

		msg = Utils.toChatColor(msg);

		for (Player player : players)
			if (player.hasPermission("intermud3.msg"))
				player.sendMessage("[I3/" + chan + "] " + visName + ": " + msg);
	}

	/**
	 * @param packet
	 */
	private void chanTargetHandler(Packet packet) {
		LPCString oMudName = packet.getLPCString(BasePayload.O_MUD.getIndex());

		if (oMudName == null)
			return;

		LPCString channel = packet.getLPCString(ChanTargetPayload.CHANNAME
				.getIndex());

		if (channel == null)
			return;

		LPCString target = packet.getLPCString(ChanTargetPayload.T_VISNAME
				.getIndex());
		LPCString tMudName = packet.getLPCString(ChanTargetPayload.T_MUD
				.getIndex());

		if (!tMudName.toString().equals(
				Intermud3.instance.getServer().getServerName()))
			target = new LPCString(target + "@" + tMudName);

		LPCString oMessage = packet.getLPCString(ChanTargetPayload.O_MSG
				.getIndex());

		if (oMessage == null)
			return;

		LPCString oName = packet.getLPCString(ChanTargetPayload.O_VISNAME
				.getIndex());

		if (oName == null)
			oName = packet.getLPCString(BasePayload.O_USER.getIndex());

		if (!oMudName.toString().equals(
				Intermud3.instance.getServer().getServerName()))
			oName = new LPCString(oName + "@" + oMudName);

		String chan = channel.toString();

		if (this.channelToAlias.containsKey(chan))
			chan = this.channelToAlias.get(chan);

		LPCString tMessage = packet.getLPCString(ChanTargetPayload.T_MSG
				.getIndex());

		String tMsg = Utils.toChatColor(tMessage.toString());

		tMsg = "[I3/" + chan + "] " + tMsg;
		tMsg = tMsg.replace("$N", oName.toString());
		tMsg = tMsg.replace("$O", target.toString());

		String oMsg = Utils.toChatColor(oMessage.toString());

		oMsg = "[I3/" + chan + "] " + oMsg;
		oMsg = oMsg.replace("$N", oName.toString());
		oMsg = oMsg.replace("$O", target.toString());

		Player[] players = Intermud3.instance.getServer().getOnlinePlayers();

		for (Player player : players)
			if (player.hasPermission("intermud3.msg")) {
				String listener = ChatColor.stripColor(player.getName())
						.toLowerCase();

				if (listener.equals(oName.toString().toLowerCase()))
					player.sendMessage(oMsg);
				else if (!target.toString().contains("@")
						&& target.toString().toLowerCase().equals(listener))
					player.sendMessage(tMsg);
				else
					player.sendMessage(oMsg);
			}
	}

	/**
	 * @param packet
	 */
	private void chanUserReply(Packet packet) {
	}

	/**
	 * @param packet
	 */
	private void chanUserReq(Packet packet) {
		LPCString oMud = packet.getLPCString(BasePayload.O_MUD.getIndex());
		LPCString uName = packet.getLPCString(ChanUserReqPayload.USERNAME
				.getIndex());
		OfflinePlayer player = Intermud3.instance.getServer().getOfflinePlayer(
				uName.toString());

		if (player == null) {
			I3Error error = ServiceType.I3ERROR.getService();

			if (error != null) {
				Packet errPacket = new Packet();

				errPacket.add(new LPCInt(0));
				errPacket.add(oMud);
				errPacket
						.add(packet.getLPCString(BasePayload.O_USER.getIndex()));
				errPacket.add(new LPCString("unk-user"));
				errPacket.add(new LPCString(uName + "@"
						+ packet.getLPCString(BasePayload.T_MUD.getIndex())
						+ " was not found!"));
				errPacket.add(packet);
			}
		}

		Packet payload = new Packet();

		payload.add(uName);
		payload.add(ChatColor.stripColor(player.getName()));
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
		saveDefaultConfig();

		try {
			this.configTunein = new ArrayList<String>(getChanlistConfig()
					.getStringList("tunein"));
			this.configAliases = new ArrayList<String>(getChanlistConfig()
					.getStringList("aliases"));
			this.chanList.setLPCData(Utils.toObject(getChanlistConfig()
					.getString("chanList")));
		} catch (I3Exception e) {
			e.printStackTrace();
		}

		this.tuneinChannels.clear();

		for (String s : this.configTunein)
			this.tuneinChannels.add(s);

		this.aliasToChannel.clear();
		this.channelToAlias.clear();

		for (String s : this.configAliases) {
			String[] parts = StringUtils.split(s, ":");
			String alias = parts[0].trim();
			String channel = parts[1].trim();

			this.aliasToChannel.put(alias, channel);
			this.channelToAlias.put(channel, alias);
		}

		ServiceType.I3CHANNEL.setVisibleOnRouter(true);
		Intermud3.callout.addHeartBeat(this, this.hBeatDelay);
	}

	public void debugInfo() {
		Log.debug("Channel: listening: "
				+ StringUtils.join(this.listening.iterator(), ", "));
		Log.debug("Channel: chanList:  "
				+ StringUtils.join(this.chanList.keySet().iterator(), ", "));
	}

	public Map<String, String> getAliases() {
		Map<String, String> aliases = new ConcurrentHashMap<String, String>();

		for (Object o : this.aliasToChannel.keySet())
			aliases.put(o.toString(), this.aliasToChannel.get(o));

		return aliases;
	}

	public LPCMapping getChanList() {
		return this.chanList.clone();
	}

	public FileConfiguration getChanlistConfig() {
		if (this.chanlistConfig == null)
			reloadChanlistConfig();

		return this.chanlistConfig;
	}

	public LPCArray getListening() {
		return this.listening.clone();
	}

	public void heartBeat() {
		if (this.listening.size() == 0)
			requestChanList();
	}

	public void reloadChanlistConfig() {
		if (this.chanlistConfigFile == null)
			this.chanlistConfigFile = new File(
					Intermud3.instance.getDataFolder(), "chanlist.yml");

		this.chanlistConfig = YamlConfiguration
				.loadConfiguration(this.chanlistConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = Intermud3.instance
				.getResource("chanlist.yml");

		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			this.chanlistConfig.setDefaults(defConfig);
		}
	}

	public void remove() {
		Intermud3.callout.removeHeartBeat(this);
		saveChanlistConfig();

		LPCArray listeningCopy = this.listening.clone();

		for (Object obj : listeningCopy)
			sendChannelListen((LPCString) obj, false);

		this.listening.clear();
		this.chanList.clear();
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
		String namedType = packet.getLPCString(BasePayload.TYPE.getIndex())
				.toString();
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
		String namedType = packet.getLPCString(BasePayload.TYPE.getIndex())
				.toString();
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

	public void saveChanlistConfig() {
		if (this.chanlistConfig == null || this.chanlistConfigFile == null)
			return;

		getChanlistConfig().set("tunein", this.configTunein);
		getChanlistConfig().set("aliases", this.configAliases);
		getChanlistConfig().set("chanList", Utils.toMudMode(this.chanList));

		try {
			getChanlistConfig().save(this.chanlistConfigFile);
		} catch (IOException ioE) {
			Log.error("Could not save config to " + this.chanlistConfigFile,
					ioE);
		}
	}

	public void saveDefaultConfig() {
		if (this.chanlistConfigFile == null)
			this.chanlistConfigFile = new File(
					Intermud3.instance.getDataFolder(), "chanlist.yml");

		if (!this.chanlistConfigFile.exists())
			Intermud3.instance.saveResource("chanlist.yml", false);
	}

	private void sendChannelListen(LPCString channel, boolean flag) {
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

	public void sendEmote(String chan, String plrName, String msg) {
		Packet payload = new Packet();

		if (this.aliasToChannel.containsKey(chan)
				&& !this.aliasToChannel.containsValue(chan))
			chan = this.aliasToChannel.get(chan);

		plrName = ChatColor.stripColor(plrName);
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

		plrName = ChatColor.stripColor(plrName);
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

		this.configAliases.clear();

		for (String s : this.aliasToChannel.keySet())
			this.configAliases.add(s + ": " + this.aliasToChannel.get(s));

		saveChanlistConfig();
	}

	public void showChannelsListening(CommandSender sender) {
		String listeningChannels = null;
		List<String> list = new ArrayList<String>();

		for (Object obj : this.listening)
			list.add(ChatColor.GREEN + obj.toString());

		Collections.sort(list);
		listeningChannels = StringUtils.join(list, ChatColor.RESET + ", ");
		sender.sendMessage("Listening (" + list.size() + "): "
				+ listeningChannels);
	}

	public void showChannelsAvailable(CommandSender sender) {
		String availableChannels = null;
		List<String> list = new ArrayList<String>();

		for (Object obj : this.chanList.keySet()) {
			String key = obj.toString();

			if (!this.listening.contains(key))
				list.add(ChatColor.GREEN + key);
		}

		Collections.sort(list);
		availableChannels = StringUtils.join(list, ChatColor.RESET + ", ");
		sender.sendMessage("Available (" + list.size() + "): "
				+ availableChannels);
	}

	public void showChannelAliases(CommandSender sender) {
		List<String> list = new ArrayList<String>();

		for (String key : this.aliasToChannel.keySet()) {
			String val = this.aliasToChannel.get(key);

			list.add(ChatColor.GREEN + key + ChatColor.RESET + ": "
					+ ChatColor.GREEN + val);
		}

		Collections.sort(list);
		sender.sendMessage("Aliases (" + list.size() + "):");

		for (String line : list)
			sender.sendMessage("- " + line);
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
		if (!this.configTunein.contains(channel))
			this.configTunein.add(channel);

		sendChannelListen(channel, true);
		saveChanlistConfig();
	}

	public void tuneOut(String channel) {
		if (this.configTunein.contains(channel))
			this.configTunein.remove(channel);

		sendChannelListen(channel, false);
		saveChanlistConfig();
	}

	/**
	 * Reload the chanlist config file and setup the local variables.
	 */
	public void updateConfig() {
		reloadChanlistConfig();

		try {
			this.configTunein = new ArrayList<String>(getChanlistConfig()
					.getStringList("tunein"));
			this.configAliases = new ArrayList<String>(getChanlistConfig()
					.getStringList("aliases"));
			this.chanList.setLPCData(Utils.toObject(getChanlistConfig()
					.getString("chanList")));
		} catch (I3Exception e) {
			e.printStackTrace();
		}

		LPCArray listeningCopy = new LPCArray(this.listening);

		for (Object obj : listeningCopy) {
			String channel = obj.toString();

			if (!this.configTunein.contains(channel))
				sendChannelListen(channel, false);
		}

		this.tuneinChannels.clear();

		for (String channel : this.configTunein) {
			this.tuneinChannels.add(channel);

			if (!this.listening.contains(channel))
				sendChannelListen(channel, true);
		}

		this.aliasToChannel.clear();
		this.channelToAlias.clear();

		for (String s : this.configAliases) {
			String[] parts = StringUtils.split(s, ":");
			String alias = parts[0].trim();
			String channel = parts[1].trim();

			this.aliasToChannel.put(alias, channel);
			this.channelToAlias.put(channel, alias);
		}
	}
}

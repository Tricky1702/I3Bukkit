package uk.org.rockthehalo.intermud3.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Network;
import uk.org.rockthehalo.intermud3.LPC.CallOut;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.LPC.LPCVar;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketBase;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketTypes;

public class I3Channel extends ServiceTemplate {
	private final Intermud3 i3;
	private final CallOut callout;
	private final Network network;
	private final int hBeatDelay = 60;
	private final Vector<String> defChannels;

	private Map<String, String> aliasToChannel;
	private Map<String, String> channelToAlias;
	private FileConfiguration chanlistConfig = null;
	private File chanlistConfigFile = null;
	private LPCMapping chanList;
	private LPCArray listening;

	public I3Channel() {
		this.i3 = Intermud3.instance;
		this.callout = CallOut.instance;
		this.network = Network.instance;

		super.setServiceName("channel");
		Services.addService(this);

		this.defChannels = new Vector<String>();
		this.defChannels.add("dchat");
		this.defChannels.add("dead_souls");
		this.defChannels.add("dead_test4");
		this.defChannels.add("imud_code");
		this.defChannels.add("minecraft");

		this.aliasToChannel = new Hashtable<String, String>();
		this.aliasToChannel.put("dc", "dchat");
		this.aliasToChannel.put("ds", "dead_souls");
		this.aliasToChannel.put("dt", "dead_test4");
		this.aliasToChannel.put("ic", "imud_code");
		this.aliasToChannel.put("mc", "minecraft");

		this.channelToAlias = new Hashtable<String, String>();
		this.channelToAlias.put("dchat", "dc");
		this.channelToAlias.put("dead_souls", "ds");
		this.channelToAlias.put("dead_test4", "dt");
		this.channelToAlias.put("imud_code", "ic");
		this.channelToAlias.put("minecraft", "mc");

		this.chanList = new LPCMapping();
		this.listening = new LPCArray();
	}

	/**
	 * @param packet
	 */
	private void chanEmoteHandler(Packet packet) {
		LPCString oMudName = packet.getLPCString(PacketBase.O_MUD.getIndex());

		if (oMudName == null)
			return;

		LPCString channel = packet.getLPCString(6);
		LPCString message = packet.getLPCString(8);

		if (channel == null || message == null)
			return;

		LPCString visName = packet.getLPCString(7);

		if (visName == null)
			visName = packet.getLPCString(PacketBase.O_USER.getIndex());

		String name;

		if (oMudName.toString().equals(this.i3.getServer().getServerName()))
			name = visName + "@" + oMudName;
		else
			name = visName.toString();

		String msg = message.toString().replace("$N", name);
		Player[] players = this.i3.getServer().getOnlinePlayers();

		for (Player player : players)
			if (player.hasPermission("intermud3.emote"))
				player.sendMessage("[I3/" + channel + "] " + msg);
	}

	/**
	 * @param packet
	 */
	public void chanListReply(Packet packet) {
		if (packet.size() != 8) {
			this.i3.logError("We don't like chanlist packet size. Should be 8 but is "
					+ packet.size());
			this.i3.logError(packet.toMudMode());

			return;
		}

		int oMud = PacketBase.O_MUD.getIndex();
		String oMudName = packet.getLPCString(oMud).toString();

		if (!oMudName.equals(this.network.getRouterName().toString())) {
			this.i3.logError("Illegal access. Not from the router.");
			this.i3.logError(packet.toMudMode());

			return;
		}

		LPCMapping info = packet.getLPCMapping(7);

		if (info != null) {
			LPCArray listeningCopy = new LPCArray(this.listening);

			for (Object channel : listeningCopy)
				if (!info.containsKey(channel))
					this.listening.remove(channel);
		}

		LPCInt chanlistID = packet.getLPCInt(6);

		if (chanlistID.toInt() == this.network.getChanlistID().toInt())
			return;

		this.network.setChanlistID(chanlistID);
		this.i3.saveConfig();

		if (info != null) {
			for (Object channel : info.keySet()) {
				LPCArray hostInfo = info.getLPCArray(channel);
				LPCArray chanInfo = this.chanList.getLPCArray(channel);

				if (((LPCString) channel).isEmpty()) {
					this.i3.debug("Empty channel name. Ignoring.");

					continue;
				} else if (hostInfo == null && chanInfo != null) {
					this.i3.debug("Deleting channel '" + channel
							+ "' from the chanlist.");
					this.chanList.remove(channel);

					if (this.listening.contains(channel)
							&& this.defChannels.contains(channel.toString()))
						sendChannelListen((LPCString) channel, false);
				} else if (hostInfo != null) {
					if (this.chanList.size() != 0) {
						if (chanInfo != null
								&& chanInfo.toString() != hostInfo.toString()) {
							this.i3.debug("Updating data for channel '"
									+ channel + "' in the chanlist.");
						} else if (chanInfo == null) {
							this.i3.debug("Adding channel '" + channel
									+ "' to the chanlist.");
						}
					} else {
						this.i3.debug("Creating chanlist. Adding channel '"
								+ channel + "' to the chanlist.");
					}

					this.chanList.set(channel, hostInfo);

					if (!this.listening.contains(channel)
							&& this.defChannels.contains(channel.toString()))
						sendChannelListen((LPCString) channel, true);
				}
			}
		}

		saveChanlistConfig();
	}

	/**
	 * @param packet
	 */
	private void chanMessageHandler(Packet packet) {
		LPCString oMudName = packet.getLPCString(PacketBase.O_MUD.getIndex());

		if (oMudName == null)
			return;

		LPCString channel = packet.getLPCString(6);
		LPCString message = packet.getLPCString(8);

		if (channel == null || message == null)
			return;

		LPCString visName = packet.getLPCString(7);

		if (visName == null)
			visName = packet.getLPCString(PacketBase.O_USER.getIndex());

		String name;

		if (oMudName.toString().equals(this.i3.getServer().getServerName()))
			name = visName + "@" + oMudName;
		else
			name = visName.toString();

		Player[] players = this.i3.getServer().getOnlinePlayers();

		for (Player player : players)
			if (player.hasPermission("intermud3.msg"))
				player.sendMessage("[I3/" + channel + "] " + name + ": "
						+ message);
	}

	/**
	 * @param packet
	 */
	private void chanTargetHandler(Packet packet) {
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

	@Override
	public void create() {
		saveDefaultConfig();

		try {
			this.chanList.setLPCData(LPCVar.toObject(getChanlistConfig()
					.getString("chanList")));
		} catch (I3Exception e) {
			e.printStackTrace();
		}

		Services.addServiceName(this.toString());
		this.callout.setHeartBeat(this, this.hBeatDelay);
	}

	public void debugInfo() {
		this.i3.logInfo("Channel: listening: " + this.listening.toString());
		this.i3.logInfo("Channel: chanList:  "
				+ this.chanList.keySet().toString());
	}

	public FileConfiguration getChanlistConfig() {
		if (this.chanlistConfig == null)
			reloadChanlistConfig();

		return this.chanlistConfig;
	}

	public LPCArray getListening() {
		return (LPCArray) this.listening.clone();
	}

	public void heartBeat() {
		if (this.listening.size() == 0)
			requestChanList();
	}

	public void reloadChanlistConfig() {
		if (this.chanlistConfigFile == null)
			this.chanlistConfigFile = new File(this.i3.getDataFolder(),
					"chanlist.yml");

		this.chanlistConfig = YamlConfiguration
				.loadConfiguration(this.chanlistConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = this.i3.getResource("chanlist.yml");

		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			this.chanlistConfig.setDefaults(defConfig);
		}
	}

	@Override
	public void remove() {
		this.callout.removeHeartBeat(this);
		Services.removeServiceName(this.toString());
		saveChanlistConfig();

		LPCArray listeningCopy = (LPCArray) this.listening.clone();

		for (Object obj : listeningCopy)
			sendChannelListen((LPCString) obj, false);

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
		String namedType = packet.getLPCString(PacketBase.TYPE.getIndex())
				.toString();
		PacketTypes type = PacketTypes.getNamedType(namedType);

		switch (type) {
		case CHAN_EMOTE:
			chanEmoteHandler(packet);

			break;
		case CHAN_FILTER_REPLY:
			this.i3.logError("Filter reply not recognized.");
			this.i3.logError("Packet: " + packet.toMudMode());

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
			this.i3.logError("Unhandled channel reply handler: " + namedType);
			this.i3.logError("Packet: " + packet.toMudMode());

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
		String namedType = packet.getLPCString(PacketBase.TYPE.getIndex())
				.toString();
		PacketTypes type = PacketTypes.getNamedType(namedType);

		switch (type) {
		case CHAN_FILTER_REQ:
			this.i3.logError("Filter request not recognized.");
			this.i3.logError("Packet: " + packet.toMudMode());

			break;
		case CHAN_USER_REQ:
			chanUserReq(packet);

			break;
		case CHAN_WHO_REQ:
			chanWhoReq(packet);

			break;
		default:
			this.i3.logError("Unhandled channel request handler: " + namedType);
			this.i3.logError("Packet: " + packet.toMudMode());

			break;
		}
	}

	public void requestChanList() {
		if (this.network.isRouterConnected()) {
			this.network.setChanlistID(0);
			this.network.sendToRouter("chanlist-req", null, null);
		}
	}

	public void saveChanlistConfig() {
		if (this.chanlistConfig == null || this.chanlistConfigFile == null)
			return;

		getChanlistConfig().set("chanList", LPCVar.toMudMode(this.chanList));

		try {
			getChanlistConfig().save(this.chanlistConfigFile);
		} catch (IOException ioE) {
			this.i3.logError("Could not save config to "
					+ this.chanlistConfigFile, ioE);
		}
	}

	public void saveDefaultConfig() {
		if (this.chanlistConfigFile == null)
			this.chanlistConfigFile = new File(this.i3.getDataFolder(),
					"chanlist.yml");

		if (!this.chanlistConfigFile.exists())
			this.i3.saveResource("chanlist.yml", false);
	}

	private void sendChannelListen(LPCString channel, boolean flag) {
		tuneChannel(channel, flag);

		if (!flag)
			this.listening.remove(channel);
		else
			this.listening.add(channel);
	}

	public void sendChannelListen(String channel, boolean flag) {
		sendChannelListen(new LPCString(channel), flag);
	}

	public void sendEmote(String chan, Player player, String msg) {
		Packet payload = new Packet();

		payload.add(new LPCString(chan));
		payload.add(new LPCString(player.getName()));
		payload.add(new LPCString("$N " + msg));
		this.network.sendToAll(PacketTypes.CHAN_EMOTE, player.getName(),
				payload);
	}

	public void sendMessage(String chan, Player player, String msg) {
		Packet payload = new Packet();

		payload.add(new LPCString(chan));
		payload.add(new LPCString(player.getName()));
		payload.add(new LPCString(msg));
		this.network.sendToAll(PacketTypes.CHAN_MESSAGE, player.getName(),
				payload);
	}

	private void tuneChannel(LPCString channel, boolean flag) {
		Packet packet = new Packet();

		packet.add(channel);
		packet.add(flag ? new LPCInt(1) : new LPCInt(0));
		this.network.sendToRouter("channel-listen", null, packet);
	}

	public void tuneIn(String channel) {
		sendChannelListen(channel, true);
	}

	public void tuneOut(String channel) {
		sendChannelListen(channel, false);
	}
}

package uk.org.rockthehalo.intermud3.services;

import org.bukkit.ChatColor;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.PacketTypes.BasePayload;
import uk.org.rockthehalo.intermud3.LPC.PacketTypes.PingPayload;
import uk.org.rockthehalo.intermud3.LPC.PacketTypes.PacketType;

public class I3Ping extends ServiceTemplate {
	private final Intermud3 i3 = Intermud3.instance;
	private final int hBeatDelay = 60;
	private final int baseDelay = 5;
	private final int delay = 5;

	private int hBeat;

	public I3Ping() {
		hBeat = Utils.rnd(this.delay) + this.baseDelay;
	}

	public void create() {
		ServiceType.I3PING.setVisibleOnRouter(true);
		Intermud3.callout.addHeartBeat(this, this.hBeatDelay);
	}

	public void debugInfo() {
		Log.debug("I3Ping: " + (this.hBeat - 1) + " minutes to go.");
	}

	public void heartBeat() {
		this.hBeat--;

		if (this.hBeat == 1) {
			testConnection();

			return;
		}

		if (!Intermud3.network.isRouterConnected() || this.hBeat <= 0) {
			Log.warn("I3Ping: Not connected to the router. Re-connecting.");
			this.hBeat = 3;
			Intermud3.network.setRouterConnected(false);
			Intermud3.network.setReconnectWait(Intermud3.network.minRetryTime);
			Intermud3.network.reconnect();
		}
	}

	public void remove() {
		Intermud3.callout.removeHeartBeat(this);
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
		this.hBeat = Utils.rnd(this.delay) + this.baseDelay;
		Intermud3.network.setRouterConnected(true);

		/* Nothing more for now. */
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
		PacketType replyType;
		Packet extra = new Packet();
		String reply = "ping-reply";

		Log.debug(packet.toMudMode());

		if (packet.getLPCString(BasePayload.TYPE.getIndex()).toString()
				.equals("ping"))
			reply = "pong";

		replyType = PacketType.getNamedType(reply);

		if (packet.size() >= PingPayload.size())
			for (Object obj : packet.subList(BasePayload.size(), packet.size()))
				extra.add(obj);

		int oMud = BasePayload.O_MUD.getIndex();
		String oMudName = ChatColor.stripColor(packet.getLPCString(oMud)
				.toString());

		if (!oMudName.equals(ChatColor.stripColor(this.i3.getServer()
				.getServerName()))) {
			this.hBeat = Utils.rnd(this.delay) + this.baseDelay;
			Intermud3.network.setRouterConnected(true);
		}

		Intermud3.network.sendToMud(replyType, null, oMudName, extra);
	}

	public void send(String type, String tmud, Packet packet) {
		Intermud3.network.sendToMud(PacketType.getNamedType(type), null, tmud,
				packet);
	}

	private void testConnection() {
		long tm = System.currentTimeMillis();
		long hash = Utils.rnd(19720231L) + ((tm & 0x7fffffffL) ^ 0x25a5a5a5L);
		Packet packet = new Packet();

		packet.add(new LPCInt((int) (hash & 0x7fffffff)));
		Intermud3.network.sendToMud(PacketType.getNamedType("ping-req"), null,
				ChatColor.stripColor(this.i3.getServer().getServerName()),
				packet);
	}
}

package uk.org.rockthehalo.intermud3.services;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketBase;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketTypes;

public class I3Ping extends ServiceTemplate {
	private final Intermud3 i3 = Intermud3.instance;
	private final int hBeatDelay = 60;
	private final int baseDelay = 5;
	private final int delay = 5;

	private int hBeat = Utils.rnd(this.delay) + this.baseDelay;

	public I3Ping() {
		setServiceName("ping");
	}

	public void create() {
		Services.addServiceName(this.toString());
		Intermud3.heartbeat.setHeartBeat(this, this.hBeatDelay);
	}

	public void debugInfo() {
		Utils.logInfo("I3Ping: " + (this.hBeat - 1) + " minutes to go.");
	}

	public void heartBeat() {
		if (!Intermud3.network.isRouterConnected())
			return;

		this.hBeat--;

		if (this.hBeat == 1) {
			testConnection();

			return;
		}

		if (this.hBeat <= 0) {
			Utils.logWarn("I3Ping: Not connected to the router. Re-connecting.");
			this.hBeat = 3;
			Intermud3.network.setRouterConnected(false);
			Intermud3.network.reconnect(5);
		}
	}

	public void remove() {
		Intermud3.heartbeat.removeHeartBeat(this);
		Services.removeServiceName(this.toString());
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
		PacketTypes replyType;
		Packet extra = new Packet();
		String reply = "ping-reply";

		if (packet.getLPCString(PacketBase.TYPE.getIndex()).toString()
				.equals("ping"))
			reply = "pong";

		replyType = PacketTypes.getNamedType(reply);

		if (packet.size() >= 7)
			for (Object obj : packet.subList(6, packet.size()))
				extra.add(obj);

		int oMud = PacketBase.O_MUD.getIndex();
		String oMudName = packet.getLPCString(oMud).toString();

		if (!oMudName.equals(this.i3.getServer().getServerName())) {
			this.hBeat = Utils.rnd(this.delay) + this.baseDelay;
			Intermud3.network.setRouterConnected(true);
		}

		Intermud3.network
				.sendToMud(replyType, null, oMudName.toString(), extra);
	}

	public void send(String type, String tmud, Packet packet) {
		Intermud3.network.sendToMud(PacketTypes.getNamedType(type), null, tmud,
				packet);
	}

	private void testConnection() {
		long tm = System.currentTimeMillis();
		long hash = Utils.rnd(19720231) + ((tm & 0xffffffff) ^ 0xa5a5a5a5);
		Packet packet = new Packet();

		packet.add(new LPCInt((int) (hash & 0xffffffff)));
		Intermud3.network.sendToMud(PacketTypes.getNamedType("ping-req"), null,
				this.i3.getServer().getServerName(), packet);
	}
}

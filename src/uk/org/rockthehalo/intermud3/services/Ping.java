package uk.org.rockthehalo.intermud3.services;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Network;
import uk.org.rockthehalo.intermud3.LPC.CallOut;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketBase;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketTypes;

public class Ping extends ServiceTemplate {
	private final Intermud3 i3;
	private final CallOut callout;
	private final Network network;
	private final int hBeatDelay = 60;
	private final int baseDelay = 5;
	private final int delay = 5;

	private int hBeat = Intermud3.instance.rnd(delay) + baseDelay;

	public Ping() {
		this.i3 = Intermud3.instance;
		this.callout = CallOut.instance;
		this.network = Network.instance;

		super.setServiceName("ping");
		Services.addService(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.rockthehalo.intermud3.services.ServiceTemplate#create()
	 */
	@Override
	public void create() {
		Services.addServiceName(this.toString());
		this.callout.setHeartBeat(this, this.hBeatDelay);
	}

	public void debugInfo() {
		this.i3.logInfo("Ping: " + (this.hBeat - 1) + " minutes to go.");
	}

	public void heartBeat() {
		if (!this.network.isRouterConnected())
			return;

		this.hBeat--;

		if (this.hBeat == 1) {
			testConnection();

			return;
		}

		if (this.hBeat <= 0) {
			this.i3.logWarn("Ping: Not connected to the router. Re-connecting.");
			this.hBeat = 3;
			this.network.setRouterConnected(false);
			this.network.reconnect(5);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.rockthehalo.intermud3.services.ServiceTemplate#remove()
	 */
	@Override
	public void remove() {
		this.callout.removeHeartBeat(this);
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
		this.hBeat = this.i3.rnd(this.delay) + this.baseDelay;
		this.network.setRouterConnected(true);

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
			this.hBeat = this.i3.rnd(this.delay) + this.baseDelay;
			this.network.setRouterConnected(true);
		}

		this.network.sendToMud(replyType, null, oMudName.toString(), extra);
	}

	public void send(String type, String tmud, Packet packet) {
		this.network.sendToMud(PacketTypes.getNamedType(type), null, tmud,
				packet);
	}

	private void testConnection() {
		int tm = (int) (System.currentTimeMillis() / 1000);
		int hash = this.i3.rnd(470831) + ((tm & 0x00ffffff) ^ 0x00a5a5a5);
		Packet packet = new Packet();

		packet.add(new LPCInt(hash));
		this.network.sendToMud(PacketTypes.getNamedType("ping-req"), null,
				this.i3.getServer().getServerName(), packet);
	}
}

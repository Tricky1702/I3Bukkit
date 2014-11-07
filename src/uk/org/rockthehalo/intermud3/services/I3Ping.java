package uk.org.rockthehalo.intermud3.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.PacketTypes.PacketType;
import uk.org.rockthehalo.intermud3.Payload;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;

public class I3Ping extends ServiceTemplate {
	private final int hBeatDelay = 60;
	private final int baseDelay = 3;
	private final int delay = 12;
	private final Map<Integer, String> pinglist = new HashMap<Integer, String>(
			128);

	private int hBeat;

	public I3Ping() {
	}

	public void create() {
		ServiceType.I3PING.setVisibleOnRouter(true);
		this.hBeat = 3;
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
	public void replyHandler(final Packet packet) {
		final List<String> list = new ArrayList<String>();

		if (packet.size() > Payload.HEADERSIZE)
			for (final Object obj : packet.subList(Payload.HEADERSIZE,
					packet.size()))
				list.add(Utils.toMudMode(obj));

		final String extra = StringUtils.join(list, ", ");

		Log.debug("Ping reply from '" + packet.getLPCString(Payload.O_MUD)
				+ "'" + (list.isEmpty() ? "" : " - Extra info: " + extra));

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
	public void reqHandler(final Packet packet) {
		String reply = "ping-reply";

		if (packet.getLPCString(Payload.TYPE).toString().equals("ping"))
			reply = "pong";

		final Packet extra = new Packet();

		if (packet.size() > Payload.HEADERSIZE)
			for (final Object obj : packet.subList(Payload.HEADERSIZE,
					packet.size()))
				extra.add(obj);

		final String oMudName = Utils.stripColor(packet.getLPCString(
				Payload.O_MUD).toString());

		if (!oMudName.equals(Utils.getServerName())) {
			this.hBeat = Utils.rnd(this.delay) + this.baseDelay;
			Intermud3.network.setRouterConnected(true);
		}

		Intermud3.network.sendToMud(PacketType.getNamedType(reply), null,
				oMudName, extra);
	}

	public void send(final String type, final String tmud, final Packet packet) {
		Intermud3.network.sendToMud(PacketType.getNamedType(type), null, tmud,
				packet);
	}

	private void testConnection() {
		final long tm = System.currentTimeMillis();
		final long hash = Utils.rnd(19720231L)
				+ ((tm & 0x7fffffffL) ^ 0x25a5a5a5L);
		final Packet packet = new Packet();

		packet.add(new LPCInt(hash & 0x7fffffffL));
		Intermud3.network.sendToMud(PacketType.getNamedType("ping-req"), null,
				Utils.getServerName(), packet);

		final I3Mudlist i3Mudlist = ServiceType.I3MUDLIST.getService();

		if (i3Mudlist != null) {
			final Map<String, LPCArray> mudlist = i3Mudlist.getMudlist();

			if (!mudlist.isEmpty()) {
				this.pinglist.clear();

				int i = 0;

				for (final String mud : mudlist.keySet()) {
					if (Utils.stripColor(mud).equals(Utils.getServerName()))
						continue;

					final LPCArray info = mudlist.get(mud);

					if (info.getLPCInt(I3Mudlist.STATE).toNum() != -1)
						continue;

					if ((info.getLPCMapping(I3Mudlist.SERVICES).containsKey(
							"ping") && info.getLPCMapping(I3Mudlist.SERVICES)
							.getLPCInt("ping").toNum() == 1)
							|| info.getLPCString(I3Mudlist.MUDLIB)
									.toLowerCase().startsWith("dead souls"))
						this.pinglist.put(i++, mud);
				}

				if (i != 0) {
					i = Utils.rnd(i);

					final String pingmud = this.pinglist.get(i);

					Intermud3.network.sendToMud(
							PacketType.getNamedType("ping-req"), null, pingmud,
							packet);
				}
			}
		}
		// Intermud3.network.sendToMud(PacketType.getNamedType("ping-req"),
		// null,
		// "Rock the Halo", packet);
		// Intermud3.network.sendToMud(PacketType.getNamedType("ping-req"),
		// null,
		// "Dead Souls Dev", packet);
	}
}

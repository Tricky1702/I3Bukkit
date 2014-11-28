package uk.org.rockthehalo.intermud3.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Network;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.PacketTypes.PacketType;
import uk.org.rockthehalo.intermud3.Payload;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;

public class I3Ping extends ServiceTemplate {
	private final long baseDelay = 3L;
	private final long delay = 12L;
	private final long hBeatDelay = 60L;
	private final Map<Long, String> pinglist = new HashMap<Long, String>(128);

	private long hBeat;

	public I3Ping() {
		this.hBeat = 3L;
	}

	public void create() {
		ServiceType.I3PING.setVisibleOnRouter(true);
		this.hBeat = 3L;
		Intermud3.callout.addHeartBeat(this, this.hBeatDelay);
	}

	public void debugInfo() {
		Log.debug("I3Ping: " + (this.hBeat - 1L) + " minutes to go.");
	}

	public void heartBeat() {
		this.hBeat--;

		if (this.hBeat == 1L) {
			testConnection();

			return;
		}

		if (!Intermud3.network.isRouterConnected() || this.hBeat <= 0L) {
			Log.warn("I3Ping: Not connected to the router. Re-connecting.");
			this.hBeat = 3L;
			Intermud3.network.setRouterConnected(false);
			Intermud3.network.setReconnectWait(Network.MIN_RETRY_TIME);
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
			for (final Object obj : packet.subList(Payload.HEADERSIZE, packet.size()))
				list.add(Utils.toMudMode(obj));

		final String extra = StringUtils.join(list, ", ");

		Log.debug("Ping reply from '" + packet.getLPCString(Payload.O_MUD) + "'"
				+ (list.isEmpty() ? "" : " - Extra info: " + extra));

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

		if (packet.getLPCString(Payload.TYPE).equals("ping"))
			reply = "pong";

		final Packet extra = new Packet();

		if (packet.size() > Payload.HEADERSIZE)
			for (final Object obj : packet.subList(Payload.HEADERSIZE, packet.size()))
				extra.add(obj);

		final String oMudName = Utils.stripColor(packet.getLPCString(Payload.O_MUD).toString());

		if (!oMudName.equalsIgnoreCase(Utils.getServerName())) {
			this.hBeat = Utils.rnd(this.delay) + this.baseDelay;
			Intermud3.network.setRouterConnected(true);
		}

		final List<String> list = new ArrayList<String>();

		if (extra.size() > 0)
			for (final Object obj : extra)
				list.add(Utils.toMudMode(obj));

		final String extraInfo = StringUtils.join(list, ", ");

		Log.debug("Ping request from '" + packet.getLPCString(Payload.O_MUD) + "'"
				+ (list.isEmpty() ? "" : " - Extra info: " + extraInfo));
		send(reply, oMudName, extra);
	}

	public void send(final String type, final String tmud, final Packet packet) {
		Intermud3.network.sendToMud(PacketType.getNamedType(type), null, tmud, packet);
	}

	private void testConnection() {
		final long tm = System.currentTimeMillis();
		final long hash = Utils.rnd(19720231L) + ((tm & 0x7fffffffL) ^ 0x25a5a5a5L);
		final Packet packet = new Packet();
		final String serverName = Utils.getServerName();

		packet.add(new LPCInt(hash & 0x7fffffffL));
		send("ping-req", serverName, packet);
		Log.debug("Ping request to '" + serverName + "' - Extra info: " + packet.get(0));

		final I3Mudlist i3Mudlist = ServiceType.I3MUDLIST.getService();

		if (i3Mudlist != null) {
			final Map<String, LPCArray> mudlist = i3Mudlist.getMudlist();

			if (!mudlist.isEmpty()) {
				this.pinglist.clear();

				long i = 0L;

				for (final String mud : mudlist.keySet()) {
					if (Utils.stripColor(mud).equalsIgnoreCase(serverName))
						continue;

					final LPCArray info = mudlist.get(mud);

					if (info.getLPCInt(I3Mudlist.STATE).toNum() != -1L)
						continue;

					final LPCInt pingService = info.getLPCMapping(I3Mudlist.SERVICES).getLPCInt("ping");

					if ((pingService != null && pingService.toNum() == 1L)
							|| info.getLPCString(I3Mudlist.MUDLIB).toLowerCase().startsWith("dead souls"))
						this.pinglist.put(i++, mud);
				}

				if (i != 0L) {
					final String pingmud = this.pinglist.get(Utils.rnd(i));

					send("ping-req", pingmud, packet);
					Log.debug("Ping request to '" + pingmud + "' - Extra info: " + packet.get(0));
				}
			}
		}
	}
}

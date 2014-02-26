package uk.org.rockthehalo.intermud3.services;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.LPCInt;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.Packet.PacketBase;
import uk.org.rockthehalo.intermud3.Packet.PacketTypes;

public class Ping implements Runnable {
	private static Intermud3 i3Instance;
	private static int hbeat;

	private final int majorDelay = 5;
	private final int minorDelay = 5;

	public Ping() {
		i3Instance = Intermud3.instance;
		Services.addService(this, "ping");
		hbeat = Intermud3.rnd(minorDelay) + majorDelay;
	}

	public void send(String type, String tmud, Packet packet) {
		Packet payload = new Packet();

		payload.add(packet.get());
		Intermud3.network.sendToMud(PacketTypes.getType(type), null, tmud,
				payload);
	}

	public void replyHandler(Packet packet) {
		hbeat = Intermud3.rnd(minorDelay) + majorDelay;
		Intermud3.network.setConnected(true);

		/* Nothing more for now. */
	}

	public void reqHandler(Packet packet) {
		Packet extra = new Packet();
		String reply = "ping-reply";

		if (packet.getString(PacketBase.TYPE.getNum()).toString() == "ping")
			reply = "pong";

		if (packet.size() >= 7)
			extra.add(packet.get(6));

		if (packet.getString(PacketBase.O_MUD.getNum()).toString() != i3Instance
				.getServer().getServerName()) {
			hbeat = Intermud3.rnd(minorDelay) + majorDelay;
			Intermud3.network.setConnected(true);
		}

		Intermud3.network.sendToMud(PacketTypes.getType(reply), null, packet
				.getString(PacketBase.O_MUD.getNum()).toString(), extra);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(60000);
			} catch (InterruptedException ie) {
				return;
			}

			hbeat--;

			if (hbeat == 0)
				testConnection();

			if (!Intermud3.network.isConnected() || hbeat < 0) {
				i3Instance
						.logWarn("Ping: Not connected to the router. Re-connecting.");
				hbeat = Intermud3.rnd(minorDelay) + majorDelay;
				Intermud3.network.connect();
			}
		}
	}

	private void testConnection() {
		long tm = (int) System.currentTimeMillis() / 1000;
		long hash = Intermud3.rnd(470831) + ((tm & 0x00ffffff) ^ 0x00a5a5a5);
		Packet packet = new Packet();
		Packet payload = new Packet();

		packet.add(new LPCInt((int) hash));
		payload.add(packet.get());
		Intermud3.network.sendToMud(PacketTypes.getType("ping-req"), null,
				i3Instance.getServer().getServerName(), payload);
	}
}

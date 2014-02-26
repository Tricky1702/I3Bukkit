package uk.org.rockthehalo.intermud3.services;

import java.util.Locale;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.Packet.PacketTypes;

public class I3Error {
	public I3Error() {
	}

	public void send(Packet packet) {
		Packet payload;
		String oUser, tMud, tUser, errCode, errMsg;

		if (packet.getInt(0) != null)
			oUser = null;
		else if (packet.getPlayer(0) != null)
			oUser = packet.getPlayer(0).getName().toLowerCase(Locale.ENGLISH);
		else if (packet.getString(0) != null)
			oUser = packet.getString(0).toString().toLowerCase(Locale.ENGLISH);
		else
			oUser = null;

		if (packet.getInt(2) != null)
			tUser = null;
		else if (packet.getPlayer(2) != null)
			tUser = packet.getPlayer(2).getName().toLowerCase(Locale.ENGLISH);
		else if (packet.getString(2) != null)
			tUser = packet.getString(2).toString().toLowerCase(Locale.ENGLISH);
		else
			tUser = null;

		tMud = packet.get(1).toString();
		errCode = packet.get(3).toString();
		errMsg = packet.get(4).toString();

		payload = new Packet();
		payload.add(errCode);
		payload.add(errMsg);

		if (packet.get(6) == null)
			payload.add(0);
		else
			payload.add(packet.get(6));

		packet = new Packet();
		packet.add(payload.get());
		Intermud3.network.sendToUser(PacketTypes.ERROR, oUser, tMud, tUser,
				packet);
	}

	public void replyHandler(Packet packet) {
	}
}

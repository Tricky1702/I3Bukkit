package uk.org.rockthehalo.intermud3.services;

import java.util.Locale;

import org.bukkit.entity.Player;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.Packet.PacketTypes;

public class I3Error {
	public I3Error() {
	}

	public void send(Packet packet) {
		Packet payload;
		Object obj;
		String oUser, tMud, tUser, errCode, errMsg;

		try {
			obj = packet.get(0);

			if (obj instanceof Number)
				oUser = null;
			else if (obj instanceof Player)
				oUser = ((Player) obj).getName().toLowerCase(Locale.ENGLISH);
			else if (obj instanceof String)
				oUser = obj.toString().toLowerCase(Locale.ENGLISH);
			else
				oUser = null;

			obj = packet.get(2);

			if (obj instanceof Number)
				tUser = null;
			else if (obj instanceof Player)
				tUser = ((Player) obj).getName().toLowerCase(Locale.ENGLISH);
			else if (obj instanceof String)
				tUser = obj.toString().toLowerCase(Locale.ENGLISH);
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

			Intermud3.network.sendToUser(PacketTypes.ERROR, oUser, tMud, tUser,
					payload);
		} catch (I3Exception e) {
			e.printStackTrace();
		}
	}

	public void replyHandler(Packet packet) {
	}
}

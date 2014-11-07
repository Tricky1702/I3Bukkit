package uk.org.rockthehalo.intermud3.services;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.PacketTypes.PacketType;
import uk.org.rockthehalo.intermud3.Payload;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public class I3Error extends ServiceTemplate {
	public I3Error() {
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
		final String errorCode = packet.getLPCString(
				I3ErrorCodes.errorPayload.get("ERR_CODE")).toString();
		final I3ErrorCodes i3ErrorCode = I3ErrorCodes
				.getNamedErrorCode(errorCode);

		if (i3ErrorCode != null) {
			i3ErrorCode.handler(packet);

			return;
		}

		final LPCString errorMsg = packet
				.getLPCString(I3ErrorCodes.errorPayload.get("ERR_MESSAGE"));
		final LPCString originator = packet.getLPCString(Payload.O_MUD);

		Log.error("Unhandled Error: " + originator + "/" + errorCode + "/"
				+ errorMsg);
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
	}

	public void send(final Packet packet) {
		String oUser, tMud, tUser, errCode, errMsg;

		if (packet.getLPCInt(0) != null)
			oUser = null;
		else if (packet.getLPCString(0) != null)
			oUser = packet.getLPCString(0).toString();
		else
			oUser = null;

		if (packet.getLPCInt(2) != null)
			tUser = null;
		else if (packet.getLPCString(2) != null)
			tUser = packet.getLPCString(2).toString();
		else
			tUser = null;

		tMud = packet.get(1).toString();
		errCode = packet.get(3).toString();
		errMsg = packet.get(4).toString();

		final Packet payload = new Packet();

		payload.add(errCode);
		payload.add(errMsg);

		if (packet.size() != 6 || packet.getLPCArray(5) == null)
			payload.add(0);
		else
			for (final Object obj : packet.subList(5, packet.size()))
				payload.add(obj);

		Intermud3.network.sendToUser(PacketType.ERROR, oUser, tMud, tUser,
				payload);
	}
}

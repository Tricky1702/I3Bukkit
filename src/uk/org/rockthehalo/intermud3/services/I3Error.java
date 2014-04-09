package uk.org.rockthehalo.intermud3.services;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.PacketTypes.BasePayload;
import uk.org.rockthehalo.intermud3.LPC.PacketTypes.ErrorPayload;
import uk.org.rockthehalo.intermud3.LPC.PacketTypes.PacketType;

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
	public void replyHandler(Packet packet) {
		String errorCode = packet.getLPCString(ErrorPayload.CODE.getIndex())
				.toString();
		I3ErrorCodes i3ErrorCode = I3ErrorCodes.getNamedErrorCode(errorCode);

		if (i3ErrorCode != null) {
			i3ErrorCode.handler(packet);

			return;
		}

		LPCString errorMsg = packet
				.getLPCString(ErrorPayload.MESSAGE.getIndex());
		LPCString originator = packet.getLPCString(BasePayload.O_MUD.getIndex());

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
	public void reqHandler(Packet packet) {
	}

	public void send(Packet packet) {
		Packet payload;
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

		payload = new Packet();
		payload.add(errCode);
		payload.add(errMsg);

		if (packet.size() != 6 || packet.getLPCArray(5) == null)
			payload.add(0);
		else
			for (Object obj : packet.subList(5, packet.size()))
				payload.add(obj);

		Intermud3.network.sendToUser(PacketType.ERROR, oUser, tMud, tUser,
				payload);
	}
}

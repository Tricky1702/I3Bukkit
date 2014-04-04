package uk.org.rockthehalo.intermud3.services;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketBase;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketErrorBase;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketTypes;

public class I3Error extends ServiceTemplate {
	public enum I3ErrorCodes {
		BAD_MOJO("bad-mojo") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = packet
						.getLPCString(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					Utils.logError("Bad Mojo/" + errorMsg);
				else
					Utils.logError("Bad Mojo");

				Utils.logError("Reconnecting in 2 minutes.");
				Intermud3.network.setReconnectWait(Intermud3.network
						.getReconnectWait() - 10);
				Intermud3.network.reconnect((2 * 60) + 5);
				Intermud3.network.shutdown(2 * 60);
			}
		},
		BAD_PKT("bad-pkt") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = packet
						.getLPCString(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					Utils.logError("Bad Packet/" + errorMsg);
				else
					Utils.logError("Bad Packet");
			}
		},
		BAD_PROTO("bad-proto") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = packet
						.getLPCString(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null
						&& errorMsg.toString()
								.contains("MUD already connected")) {
					Utils.logError("Bad Proto/" + errorMsg);
					Utils.logError("Reconnecting in 5 minutes.");
					Intermud3.network.setReconnectWait(Intermud3.network
							.getReconnectWait() - 10);
					Intermud3.network.reconnect((5 * 60) + 5);
					Intermud3.network.shutdown(5 * 60);
				}
			}
		},
		NOT_ALLOWED("not-allowed") {
			@Override
			public void handler(Packet packet) {
				if (packet.size() > 8) {
					LPCArray data = packet
							.getLPCArray(PacketErrorBase.ERROR_PACKET
									.getIndex());

					if (data != null
							&& data.getLPCString(PacketBase.TYPE.getIndex())
									.toString().equals("channel-listen")) {
						LPCString channel = data.getLPCString(6);

						if (channel != null) {
							LPCString errorMsg = packet
									.getLPCString(PacketErrorBase.ERROR_MESSAGE
											.getIndex());

							if (errorMsg != null)
								Utils.logError("Not Allowed/" + errorMsg);
							else
								Utils.logError("Not Allowed");

							I3Channel service = (I3Channel) Services
									.getService("channel");

							if (service != null)
								service.tuneOut(channel.toString());
						}
					}
				}
			}
		},
		NOT_IMP("not-imp") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = packet
						.getLPCString(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					Utils.logError("Not Implemented/" + errorMsg);
				else
					Utils.logError("Not Implemented");
			}
		},
		UNK_CHANNEL("unk-channel") {
			@Override
			public void handler(Packet packet) {
				if (packet.size() > 8) {
					LPCArray data = packet
							.getLPCArray(PacketErrorBase.ERROR_PACKET
									.getIndex());

					if (data != null
							&& data.getLPCString(PacketBase.TYPE.getIndex())
									.toString().equals("channel-listen")) {
						LPCString channel = data.getLPCString(6);

						if (channel != null) {
							LPCString errorMsg = packet
									.getLPCString(PacketErrorBase.ERROR_MESSAGE
											.getIndex());

							if (errorMsg != null)
								Utils.logError("Unknown Channel/" + errorMsg);
							else
								Utils.logError("Unknown Channel");

							I3Channel service = (I3Channel) Services
									.getService("channel");

							if (service != null)
								service.tuneOut(channel.toString());
						}
					}
				}
			}
		},
		UNK_DST("unk-dst") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = packet
						.getLPCString(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					Utils.logError("Unknown Destination/" + errorMsg);
				else
					Utils.logError("Unknown Destination");
			}
		},
		UNK_SRC("unk-src") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = packet
						.getLPCString(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					Utils.logError("Unknown Source/" + errorMsg);
				else
					Utils.logError("Unknown Source");
			}
		},
		UNK_TYPE("unk-type") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = packet
						.getLPCString(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					Utils.logError("Unknown Packet Type/" + errorMsg);
				else
					Utils.logError("Unknown Packet Type");
			}
		},
		UNK_USER("unk-user") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = packet
						.getLPCString(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					Utils.logError("Unknown Target User/" + errorMsg);
				else
					Utils.logError("Unknown Target User");
			}
		};

		private static Map<String, I3ErrorCodes> nameToErrorCode = null;
		private String errorCodeName = null;

		private I3ErrorCodes(String errorCodeName) {
			this.errorCodeName = errorCodeName;
		}

		public String getErrorCodeName() {
			return this.errorCodeName;
		}

		public static I3ErrorCodes getNamedErrorCode(String errorCodeName) {
			if (I3ErrorCodes.nameToErrorCode == null) {
				I3ErrorCodes.initMapping();
			}

			return I3ErrorCodes.nameToErrorCode.get(errorCodeName);
		}

		private static void initMapping() {
			I3ErrorCodes.nameToErrorCode = new HashMap<String, I3ErrorCodes>();

			for (I3ErrorCodes type : I3ErrorCodes.values())
				I3ErrorCodes.nameToErrorCode.put(type.errorCodeName, type);
		}

		public abstract void handler(Packet packet);
	}

	public I3Error() {
		setServiceName("error");
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
		String errorCode = packet.getLPCString(
				PacketErrorBase.ERROR_CODE.getIndex()).toString();
		I3ErrorCodes i3ErrorCode = I3ErrorCodes.getNamedErrorCode(errorCode);

		if (i3ErrorCode != null) {
			i3ErrorCode.handler(packet);

			return;
		}

		LPCString errorMsg = packet.getLPCString(PacketErrorBase.ERROR_MESSAGE
				.getIndex());
		LPCString originator = packet.getLPCString(PacketBase.O_MUD.getIndex());

		Utils.logError("Unhandled Error: " + originator + "/" + errorCode + "/"
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
			oUser = packet.getLPCString(0).toString()
					.toLowerCase(Locale.ENGLISH);
		else
			oUser = null;

		if (packet.getLPCInt(2) != null)
			tUser = null;
		else if (packet.getLPCString(2) != null)
			tUser = packet.getLPCString(2).toString()
					.toLowerCase(Locale.ENGLISH);
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

		Intermud3.network.sendToUser(PacketTypes.ERROR, oUser, tMud, tUser,
				payload);
	}
}

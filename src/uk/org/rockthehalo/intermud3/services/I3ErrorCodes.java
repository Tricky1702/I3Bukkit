package uk.org.rockthehalo.intermud3.services;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.Payload;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public enum I3ErrorCodes {
	BAD_MOJO("bad-mojo") {
		@Override
		public void handler(Packet packet) {
			LPCString errorMsg = packet.getLPCString(errorPayload
					.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Bad Mojo/" + errorMsg);
			else
				Log.error("Bad Mojo");

			Log.error("Reconnecting in 2 minutes.");
			Intermud3.network.setReconnectWait(Intermud3.network
					.getReconnectWait() - 10);
			Intermud3.network.reconnect((2 * 60) + 5);
			Intermud3.network.shutdown(2 * 60);
		}
	},
	BAD_PKT("bad-pkt") {
		@Override
		public void handler(Packet packet) {
			LPCString errorMsg = packet.getLPCString(errorPayload
					.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Bad Packet/" + errorMsg);
			else
				Log.error("Bad Packet");
		}
	},
	BAD_PROTO("bad-proto") {
		@Override
		public void handler(Packet packet) {
			LPCString errorMsg = packet.getLPCString(errorPayload
					.get("ERR_MESSAGE"));

			if (errorMsg != null
					&& errorMsg.toString().contains("MUD already connected")) {
				Log.error("Bad Proto/" + errorMsg);
				Log.error("Reconnecting in 5 minutes.");
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
			if (packet.size() > errorPayload.size() - 1) {
				LPCArray data = packet.getLPCArray(errorPayload
						.get("ERR_PACKET"));

				if (data != null
						&& data.getLPCString(Payload.TYPE).toString()
								.equals("channel-listen")) {
					LPCString channel = data.getLPCString(6);

					if (channel != null) {
						LPCString errorMsg = packet.getLPCString(errorPayload
								.get("ERR_MESSAGE"));

						if (errorMsg != null)
							Log.error("Not Allowed/" + errorMsg);
						else
							Log.error("Not Allowed");

						I3Channel service = ServiceType.I3CHANNEL.getService();

						if (service != null) {
							service.sendChannelListen(channel, false);
							service.tuneOut(channel.toString());
						}
					}
				}
			}
		}
	},
	NOT_IMP("not-imp") {
		@Override
		public void handler(Packet packet) {
			LPCString errorMsg = packet.getLPCString(errorPayload
					.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Not Implemented/" + errorMsg);
			else
				Log.error("Not Implemented");
		}
	},
	UNK_CHANNEL("unk-channel") {
		@Override
		public void handler(Packet packet) {
			if (packet.size() > errorPayload.size() - 1) {
				LPCArray data = packet.getLPCArray(errorPayload
						.get("ERR_PACKET"));

				if (data != null
						&& data.getLPCString(Payload.TYPE).toString()
								.equals("channel-listen")) {
					LPCString channel = data.getLPCString(6);

					if (channel != null) {
						LPCString errorMsg = packet.getLPCString(errorPayload
								.get("ERR_MESSAGE"));

						if (errorMsg != null)
							Log.error("Unknown Channel/" + errorMsg);
						else
							Log.error("Unknown Channel");

						I3Channel service = ServiceType.I3CHANNEL.getService();

						if (service != null) {
							service.sendChannelListen(channel, false);
							service.tuneOut(channel.toString());
						}
					}
				}
			}
		}
	},
	UNK_DST("unk-dst") {
		@Override
		public void handler(Packet packet) {
			LPCString errorMsg = packet.getLPCString(errorPayload
					.get("ERR_MESSAGE"));
			if (errorMsg != null)
				Log.error("Unknown Destination/" + errorMsg);
			else
				Log.error("Unknown Destination");
		}
	},
	UNK_SRC("unk-src") {
		@Override
		public void handler(Packet packet) {
			LPCString errorMsg = packet.getLPCString(errorPayload
					.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Unknown Source/" + errorMsg);
			else
				Log.error("Unknown Source");
		}
	},
	UNK_TYPE("unk-type") {
		@Override
		public void handler(Packet packet) {
			LPCString errorMsg = packet.getLPCString(errorPayload
					.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Unknown Packet Type/" + errorMsg);
			else
				Log.error("Unknown Packet Type");
		}
	},
	UNK_USER("unk-user") {
		@Override
		public void handler(Packet packet) {
			LPCString errorMsg = packet.getLPCString(errorPayload
					.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Unknown Target User/" + errorMsg);
			else
				Log.error("Unknown Target User");
		}
	};

	private static Map<String, I3ErrorCodes> nameToErrorCode = null;
	private String errorCodeName = null;

	public static final Payload errorPayload = new Payload(Arrays.asList(
			"ERR_CODE", "ERR_MESSAGE", "ERR_PACKET"));

	private I3ErrorCodes(String errorCodeName) {
		this.errorCodeName = errorCodeName;
	}

	public String getErrorCodeName() {
		return this.errorCodeName;
	}

	public static I3ErrorCodes getNamedErrorCode(String errorCodeName) {
		if (I3ErrorCodes.nameToErrorCode == null)
			I3ErrorCodes.initMapping();

		return I3ErrorCodes.nameToErrorCode.get(errorCodeName);
	}

	private static void initMapping() {
		I3ErrorCodes.nameToErrorCode = new ConcurrentHashMap<String, I3ErrorCodes>(
				I3ErrorCodes.values().length);

		for (I3ErrorCodes type : I3ErrorCodes.values())
			I3ErrorCodes.nameToErrorCode.put(type.errorCodeName, type);
	}

	public abstract void handler(Packet packet);
}

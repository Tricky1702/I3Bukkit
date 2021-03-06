package uk.org.rockthehalo.intermud3.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.Payload;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public enum I3ErrorCodes {
	BAD_MOJO("bad-mojo") {
		@Override
		public void handler(final Packet packet) {
			final LPCString errorMsg = packet.getLPCString(errorPayload.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Bad mojo::" + errorMsg);
			else
				Log.error("Bad mojo!");

			Log.error(packet.toMudMode());
			Log.error("Reconnecting in 2 minutes.");
			Intermud3.network.setReconnectWait(Intermud3.network.getReconnectWait() - 10);
			Intermud3.network.reconnect((2 * 60) + 5);
			Intermud3.network.shutdown(2 * 60);
		}
	},
	BAD_PKT("bad-pkt") {
		@Override
		public void handler(final Packet packet) {
			final LPCString errorMsg = packet.getLPCString(errorPayload.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Bad packet::" + errorMsg);
			else
				Log.error("Bad packet!");

			Log.error(packet.toMudMode());
		}
	},
	BAD_PROTO("bad-proto") {
		@Override
		public void handler(final Packet packet) {
			final LPCString errorMsg = packet.getLPCString(errorPayload.get("ERR_MESSAGE"));

			if (errorMsg != null && errorMsg.contains("MUD already connected")) {
				Log.error("Bad proto::" + errorMsg);
				Log.error(packet.toMudMode());
				Log.error("Reconnecting in 5 minutes.");
				Intermud3.network.setReconnectWait(Intermud3.network.getReconnectWait() - 10);
				Intermud3.network.reconnect((5 * 60) + 5);
				Intermud3.network.shutdown(5 * 60);
			}
		}
	},
	NOT_ALLOWED("not-allowed") {
		@Override
		public void handler(final Packet packet) {
			if (packet.size() > errorPayload.size() - 1) {
				final LPCArray data = packet.getLPCArray(errorPayload.get("ERR_PACKET"));

				if (data != null && data.getLPCString(Payload.TYPE).equals("channel-listen")) {
					final LPCString channel = data.getLPCString(I3Channel.chanListenPayload.get("CHAN_CHANNAME"));

					if (channel != null) {
						final LPCString errorMsg = packet.getLPCString(errorPayload.get("ERR_MESSAGE"));

						if (errorMsg != null)
							Log.error("Not allowed::" + errorMsg);
						else
							Log.error("Not allowed!");

						Log.error(packet.toMudMode());

						final I3Channel service = ServiceType.I3CHANNEL.getService();

						if (service != null) {
							service.sendChannelListen(channel, false);
							service.tuneOut(channel.toString());
							service.removeAvailableChannel(channel);
						}
					}
				}
			}
		}
	},
	NOT_IMP("not-imp") {
		@Override
		public void handler(final Packet packet) {
			final LPCString errorMsg = packet.getLPCString(errorPayload.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Not Implemented::" + errorMsg);
			else
				Log.error("Not implemented!");

			Log.error(packet.toMudMode());
		}
	},
	UNK_CHANNEL("unk-channel") {
		@Override
		public void handler(final Packet packet) {
			if (packet.size() > errorPayload.size() - 1) {
				final LPCArray data = packet.getLPCArray(errorPayload.get("ERR_PACKET"));

				if (data != null && data.getLPCString(Payload.TYPE).equals("channel-listen")) {
					final LPCString channel = data.getLPCString(I3Channel.chanListenPayload.get("CHAN_CHANNAME"));

					if (channel != null) {
						final LPCString errorMsg = packet.getLPCString(errorPayload.get("ERR_MESSAGE"));

						if (errorMsg != null)
							Log.error("Unknown channel::" + errorMsg);
						else
							Log.error("Unknown channel!");

						Log.error(packet.toMudMode());

						final I3Channel service = ServiceType.I3CHANNEL.getService();

						if (service != null) {
							service.sendChannelListen(channel, false);
							service.tuneOut(channel.toString());
							service.removeAvailableChannel(channel);
						}
					}
				}
			}
		}
	},
	UNK_DST("unk-dst") {
		@Override
		public void handler(final Packet packet) {
			final LPCString errorMsg = packet.getLPCString(errorPayload.get("ERR_MESSAGE"));
			if (errorMsg != null)
				Log.error("Unknown destination::" + errorMsg);
			else
				Log.error("Unknown destination!");

			Log.error(packet.toMudMode());
		}
	},
	UNK_SRC("unk-src") {
		@Override
		public void handler(final Packet packet) {
			final LPCString errorMsg = packet.getLPCString(errorPayload.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Unknown source::" + errorMsg);
			else
				Log.error("Unknown source!");

			Log.error(packet.toMudMode());
		}
	},
	UNK_TYPE("unk-type") {
		@Override
		public void handler(final Packet packet) {
			final LPCString errorMsg = packet.getLPCString(errorPayload.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Unknown packet type::" + errorMsg);
			else
				Log.error("Unknown packet type!");

			Log.error(packet.toMudMode());
		}
	},
	UNK_USER("unk-user") {
		@Override
		public void handler(final Packet packet) {
			final LPCString errorMsg = packet.getLPCString(errorPayload.get("ERR_MESSAGE"));

			if (errorMsg != null)
				Log.error("Unknown target user::" + errorMsg);
			else
				Log.error("Unknown target user!");

			Log.error(packet.toMudMode());
		}
	};

	private static Map<String, I3ErrorCodes> nameToErrorCode = new HashMap<String, I3ErrorCodes>(values().length);

	private String errorCodeName = null;

	public static final Payload errorPayload = new Payload(Arrays.asList("ERR_CODE", "ERR_MESSAGE", "ERR_PACKET"));

	private I3ErrorCodes(final String errorCodeName) {
		this.errorCodeName = errorCodeName;
	}

	public String getErrorCodeName() {
		return errorCodeName;
	}

	public static I3ErrorCodes getNamedErrorCode(final String errorCodeName) {
		return nameToErrorCode.get(errorCodeName);
	}

	static {
		for (final I3ErrorCodes type : values())
			nameToErrorCode.put(type.errorCodeName, type);
	}

	public abstract void handler(final Packet packet);
}

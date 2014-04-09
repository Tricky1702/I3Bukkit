package uk.org.rockthehalo.intermud3.LPC;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.services.I3Channel;
import uk.org.rockthehalo.intermud3.services.I3Error;
import uk.org.rockthehalo.intermud3.services.I3Mudlist;
import uk.org.rockthehalo.intermud3.services.I3Ping;
import uk.org.rockthehalo.intermud3.services.I3Startup;
import uk.org.rockthehalo.intermud3.services.ServiceType;

public class PacketTypes {
	public enum BasePayload {
		TYPE(0), TTL(1), O_MUD(2), O_USER(3), T_MUD(4), T_USER(5);

		private int index;

		private BasePayload(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public static int size() {
			return BasePayload.values().length;
		}
	}

	public enum ErrorPayload {
		CODE(6), MESSAGE(7), PACKET(8);

		private int index;

		private ErrorPayload(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public static int size() {
			return BasePayload.size() + ErrorPayload.values().length;
		}
	}

	public enum PingPayload {
		PAYLOAD(6);

		private int index;

		private PingPayload(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}

		public static int size() {
			return BasePayload.size() + PingPayload.values().length;
		}
	}

	public enum PacketType {
		AUTH_MUD_REPLY("auth-mud-reply") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		AUTH_MUD_REQ("auth-mud-req") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		CHAN_FILTER_REPLY("chan-filter-reply") {
			@Override
			public void handler(Packet data) {
				I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_FILTER_REQ("chan-filter-req") {
			@Override
			public void handler(Packet data) {
				I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.reqHandler(data);
			}
		},
		CHAN_WHO_REPLY("chan-who-reply") {
			@Override
			public void handler(Packet data) {
				I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_WHO_REQ("chan-who-req") {
			@Override
			public void handler(Packet data) {
				I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.reqHandler(data);
			}
		},
		CHAN_USER_REPLY("chan-user-reply") {
			@Override
			public void handler(Packet data) {
				I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_USER_REQ("chan-user-req") {
			@Override
			public void handler(Packet data) {
				I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.reqHandler(data);
			}
		},
		CHANLIST_REPLY("chanlist-reply") {
			@Override
			public void handler(Packet data) {
				I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_EMOTE("channel-e") {
			@Override
			public void handler(Packet data) {
				I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_MESSAGE("channel-m") {
			@Override
			public void handler(Packet data) {
				I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_TARGET("channel-t") {
			@Override
			public void handler(Packet data) {
				I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		EMOTE("emoteto") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		ERROR("error") {
			@Override
			public void handler(Packet data) {
				I3Error service = ServiceType.I3ERROR.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		FINGER_REPLY("finger-reply") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		FINGER_REQ("finger-req") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		LOCATE_REPLY("locate-reply") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		LOCATE_REQ("locate-req") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		MUDLIST("mudlist") {
			@Override
			public void handler(Packet data) {
				I3Mudlist service = ServiceType.I3MUDLIST.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		PING("ping") {
			@Override
			public void handler(Packet data) {
				I3Ping service = ServiceType.I3PING.getService();

				if (service != null)
					service.reqHandler(data);
			}
		},
		PING_REPLY("ping-reply") {
			@Override
			public void handler(Packet data) {
				I3Ping service = ServiceType.I3PING.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		PING_REQ("ping-req") {
			@Override
			public void handler(Packet data) {
				I3Ping service = ServiceType.I3PING.getService();

				if (service != null)
					service.reqHandler(data);
			}
		},
		PONG("pong") {
			@Override
			public void handler(Packet data) {
				I3Ping service = ServiceType.I3PING.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		STARTUP_REPLY("startup-reply") {
			@Override
			public void handler(Packet data) {
				I3Startup service = ServiceType.I3STARTUP.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		TELL("tell") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		UCACHE_UPDATE("ucache-update") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		WHO_REQ("who-req") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		WHO_REPLY("who-reply") {
			@Override
			public void handler(Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		};

		private static Map<String, PacketType> nameToType = null;
		private String name = null;

		private PacketType(String name) {
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		public static PacketType getNamedType(String name) {
			if (PacketType.nameToType == null) {
				PacketType.initMapping();
			}

			return PacketType.nameToType.get(name);
		}

		private static void initMapping() {
			PacketType.nameToType = new ConcurrentHashMap<String, PacketType>(
					PacketType.values().length);

			for (PacketType type : PacketType.values()) {
				PacketType.nameToType.put(type.name, type);
			}
		}

		public abstract void handler(Packet data);
	}
}
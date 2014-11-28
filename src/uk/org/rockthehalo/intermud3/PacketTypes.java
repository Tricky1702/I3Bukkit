package uk.org.rockthehalo.intermud3;

import java.util.HashMap;
import java.util.Map;

import uk.org.rockthehalo.intermud3.services.I3Channel;
import uk.org.rockthehalo.intermud3.services.I3Error;
import uk.org.rockthehalo.intermud3.services.I3Mudlist;
import uk.org.rockthehalo.intermud3.services.I3Ping;
import uk.org.rockthehalo.intermud3.services.I3Startup;
import uk.org.rockthehalo.intermud3.services.I3UCache;
import uk.org.rockthehalo.intermud3.services.ServiceType;

public class PacketTypes {
	public enum PacketType {
		AUTH_MUD_REPLY("auth-mud-reply") {
			@Override
			public void handler(final Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		AUTH_MUD_REQ("auth-mud-req") {
			@Override
			public void handler(final Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		CHAN_FILTER_REPLY("chan-filter-reply") {
			@Override
			public void handler(final Packet data) {
				final I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_FILTER_REQ("chan-filter-req") {
			@Override
			public void handler(final Packet data) {
				final I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.reqHandler(data);
			}
		},
		CHAN_WHO_REPLY("chan-who-reply") {
			@Override
			public void handler(final Packet data) {
				final I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_WHO_REQ("chan-who-req") {
			@Override
			public void handler(final Packet data) {
				final I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.reqHandler(data);
			}
		},
		CHAN_USER_REPLY("chan-user-reply") {
			@Override
			public void handler(final Packet data) {
				final I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_USER_REQ("chan-user-req") {
			@Override
			public void handler(final Packet data) {
				final I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.reqHandler(data);
			}
		},
		CHANLIST_REPLY("chanlist-reply") {
			@Override
			public void handler(final Packet data) {
				final I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_EMOTE("channel-e") {
			@Override
			public void handler(final Packet data) {
				final I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_MESSAGE("channel-m") {
			@Override
			public void handler(final Packet data) {
				final I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_TARGET("channel-t") {
			@Override
			public void handler(final Packet data) {
				final I3Channel service = ServiceType.I3CHANNEL.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		EMOTE("emoteto") {
			@Override
			public void handler(final Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		ERROR("error") {
			@Override
			public void handler(final Packet data) {
				final I3Error service = ServiceType.I3ERROR.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		FINGER_REPLY("finger-reply") {
			@Override
			public void handler(final Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		FINGER_REQ("finger-req") {
			@Override
			public void handler(final Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		LOCATE_REPLY("locate-reply") {
			@Override
			public void handler(final Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		LOCATE_REQ("locate-req") {
			@Override
			public void handler(final Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		MUDLIST("mudlist") {
			@Override
			public void handler(final Packet data) {
				final I3Mudlist service = ServiceType.I3MUDLIST.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		PING("ping") {
			@Override
			public void handler(final Packet data) {
				final I3Ping service = ServiceType.I3PING.getService();

				if (service != null)
					service.reqHandler(data);
			}
		},
		PING_REPLY("ping-reply") {
			@Override
			public void handler(final Packet data) {
				final I3Ping service = ServiceType.I3PING.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		PING_REQ("ping-req") {
			@Override
			public void handler(final Packet data) {
				final I3Ping service = ServiceType.I3PING.getService();

				if (service != null)
					service.reqHandler(data);
			}
		},
		PONG("pong") {
			@Override
			public void handler(final Packet data) {
				final I3Ping service = ServiceType.I3PING.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		STARTUP_REPLY("startup-reply") {
			@Override
			public void handler(final Packet data) {
				final I3Startup service = ServiceType.I3STARTUP.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		TELL("tell") {
			@Override
			public void handler(final Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		UCACHE_UPDATE("ucache-update") {
			@Override
			public void handler(final Packet data) {
				final I3UCache service = ServiceType.I3UCACHE.getService();

				if (service != null)
					service.replyHandler(data);
			}
		},
		WHO_REQ("who-req") {
			@Override
			public void handler(final Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		},
		WHO_REPLY("who-reply") {
			@Override
			public void handler(final Packet data) {
				Log.warn(this.toString() + ": " + data.toMudMode());
			}
		};

		private static final Map<String, PacketType> nameToType = new HashMap<String, PacketType>(values().length);

		private String name = null;

		private PacketType(final String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public static PacketType getNamedType(final String name) {
			return nameToType.get(name);
		}

		public abstract void handler(final Packet data);

		static {
			for (final PacketType type : values())
				nameToType.put(type.name, type);
		}
	}
}

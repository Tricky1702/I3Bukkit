package uk.org.rockthehalo.intermud3.LPC;

import java.util.HashMap;
import java.util.Map;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.services.I3Channel;
import uk.org.rockthehalo.intermud3.services.I3Error;
import uk.org.rockthehalo.intermud3.services.Mudlist;
import uk.org.rockthehalo.intermud3.services.Ping;
import uk.org.rockthehalo.intermud3.services.Services;
import uk.org.rockthehalo.intermud3.services.Startup;

public class Packet extends LPCArray {
	public enum PacketBase {
		TYPE(0), TTL(1), O_MUD(2), O_USER(3), T_MUD(4), T_USER(5);

		private int index;

		private PacketBase(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}
	}

	public enum PacketTypes {
		AUTH_MUD_REPLY("auth-mud-reply") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		},
		AUTH_MUD_REQ("auth-mud-req") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		},
		CHAN_FILTER_REPLY("chan-filter-reply") {
			@Override
			public void handler(Packet data) {
				I3Channel service = (I3Channel) Services.getService("channel");

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_FILTER_REQ("chan-filter-req") {
			@Override
			public void handler(Packet data) {
				I3Channel service = (I3Channel) Services.getService("channel");

				if (service != null)
					service.reqHandler(data);
			}
		},
		CHAN_WHO_REPLY("chan-who-reply") {
			@Override
			public void handler(Packet data) {
				I3Channel service = (I3Channel) Services.getService("channel");

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_WHO_REQ("chan-who-req") {
			@Override
			public void handler(Packet data) {
				I3Channel service = (I3Channel) Services.getService("channel");

				if (service != null)
					service.reqHandler(data);
			}
		},
		CHAN_USER_REPLY("chan-user-reply") {
			@Override
			public void handler(Packet data) {
				I3Channel service = (I3Channel) Services.getService("channel");

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_USER_REQ("chan-user-req") {
			@Override
			public void handler(Packet data) {
				I3Channel service = (I3Channel) Services.getService("channel");

				if (service != null)
					service.reqHandler(data);
			}
		},
		CHANLIST_REPLY("chanlist-reply") {
			@Override
			public void handler(Packet data) {
				I3Channel service = (I3Channel) Services.getService("channel");

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_EMOTE("channel-e") {
			@Override
			public void handler(Packet data) {
				I3Channel service = (I3Channel) Services.getService("channel");

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_MESSAGE("channel-m") {
			@Override
			public void handler(Packet data) {
				I3Channel service = (I3Channel) Services.getService("channel");

				if (service != null)
					service.replyHandler(data);
			}
		},
		CHAN_TARGET("channel-t") {
			@Override
			public void handler(Packet data) {
				I3Channel service = (I3Channel) Services.getService("channel");

				if (service != null)
					service.replyHandler(data);
			}
		},
		EMOTE("emoteto") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		},
		ERROR("error") {
			@Override
			public void handler(Packet data) {
				I3Error service = (I3Error) Services.getService("error");

				if (service != null)
					service.replyHandler(data);
			}
		},
		FINGER_REPLY("finger-reply") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		},
		FINGER_REQ("finger-req") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		},
		LOCATE_REPLY("locate-reply") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		},
		LOCATE_REQ("locate-req") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		},
		MUDLIST("mudlist") {
			@Override
			public void handler(Packet data) {
				Mudlist service = (Mudlist) Services.getService("mudlist");

				if (service != null)
					service.replyHandler(data);
			}
		},
		PING("ping") {
			@Override
			public void handler(Packet data) {
				Ping service = (Ping) Services.getService("ping");

				if (service != null)
					service.reqHandler(data);
			}
		},
		PING_REPLY("ping-reply") {
			@Override
			public void handler(Packet data) {
				Ping service = (Ping) Services.getService("ping");

				if (service != null)
					service.replyHandler(data);
			}
		},
		PING_REQ("ping-req") {
			@Override
			public void handler(Packet data) {
				Ping service = (Ping) Services.getService("ping");

				if (service != null)
					service.reqHandler(data);
			}
		},
		PONG("pong") {
			@Override
			public void handler(Packet data) {
				Ping service = (Ping) Services.getService("ping");

				if (service != null)
					service.replyHandler(data);
			}
		},
		STARTUP_REPLY("startup-reply") {
			@Override
			public void handler(Packet data) {
				Startup service = (Startup) Services.getService("startup");

				if (service != null)
					service.replyHandler(data);
			}
		},
		TELL("tell") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		},
		UCACHE_UPDATE("ucache-update") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		},
		WHO_REQ("who-req") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		},
		WHO_REPLY("who-reply") {
			@Override
			public void handler(Packet data) {
				this.i3.logWarn(this.toString() + ": " + data.toMudMode());
			}
		};

		protected final Intermud3 i3;
		private static Map<String, PacketTypes> nameToType;
		private String name;

		private PacketTypes(String name) {
			this.i3 = Intermud3.instance;

			this.name = name;
		}

		public static PacketTypes getNamedType(String name) {
			if (nameToType == null) {
				initMapping();
			}

			return nameToType.get(name);
		}

		private static void initMapping() {
			nameToType = new HashMap<String, PacketTypes>();

			for (PacketTypes type : values()) {
				nameToType.put(type.name, type);
			}
		}

		public String getName() {
			return this.name;
		}

		public abstract void handler(Packet data);
	}

	/**
	 * Convert LPCArray to a format suitable for transmitting as MudMode.
	 * 
	 * @return the MudMode string
	 */
	public String toMudMode() {
		return LPCVar.toMudMode(this);
	}

	/**
	 * Converts a MudMode string into a LPCArray element.
	 * 
	 * @param mudModeString
	 *            the MudMode string to convert
	 */
	public void fromMudMode(String mudModeString) {
		Object obj = LPCVar.toObject(mudModeString);

		if (obj == null || !LPCVar.isArray(obj)) {
			this.setLPCData(new LPCArray());
		} else {
			this.setLPCData((LPCArray) obj);
		}
	}
}

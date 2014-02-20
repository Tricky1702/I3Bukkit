package uk.org.rockthehalo.intermud3;

import java.util.HashMap;
import java.util.Map;

import uk.org.rockthehalo.intermud3.services.Services;

public class Packet extends LPCData {
	public enum PacketBase {
		TYPE(0), TTL(1), O_MUD(2), O_USER(3), T_MUD(4), T_USER(5);

		private int num;
		private static Map<Integer, PacketBase> numToBase;

		private PacketBase(int num) {
			this.num = num;
		}

		public PacketBase getBase(int num) {
			if (numToBase == null) {
				initMapping();
			}

			return numToBase.get(num);
		}

		private static void initMapping() {
			numToBase = new HashMap<Integer, PacketBase>();

			for (PacketBase base : values()) {
				numToBase.put(base.num, base);
			}
		}

		public int getNum() {
			return this.num;
		}
	}

	public enum PacketTypes {
		STARTUP_REQ("startup-req-3") {
			@Override
			public void handler(Packet packet) {
				logWarn("Ignoring " + packet.toMudMode());
			}

			@Override
			public void send(Packet packet) {
				Services.startup.send(packet);
			}
		},
		STARTUP_REPLY("startup-reply") {
			@Override
			public void handler(Packet packet) {
				Services.startup.replyHandler(packet);
			}

			@Override
			public void send(Packet packet) {
				logWarn("Ignoring " + packet.toMudMode());
			}
		},
		SHUTDOWN("shutdown") {
			@Override
			public void handler(Packet packet) {
				logWarn("Ignoring " + packet.toMudMode());
			}

			@Override
			public void send(Packet packet) {
				Intermud3.network.sendToRouter(PacketTypes.SHUTDOWN, null,
						packet);
			}
		},
		ERROR("error") {
			@Override
			public void handler(Packet packet) {
				Services.i3Error.replyHandler(packet);
			}

			@Override
			public void send(Packet packet) {
				Services.i3Error.send(packet);
			}
		},
		TELL("tell") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		EMOTE("emoteto") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		WHO_REQ("who-req") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		WHO_REPLY("who-reply") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		FINGER_REQ("finger-req") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		FINGER_REPLY("finger-reply") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		LOCATE_REQ("locate-req") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		LOCATE_REPLY("locate-reply") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_LISTEN("channel-listen") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_MESSAGE("channel-m") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_EMOTE("channel-e") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_TARGET("channel-t") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_ADD("channel-add") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_REMOVE("channel-remove") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_ADMIN("channel-admin") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_FILTER_REQ("chan-filter-req") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_FILTER_REPLY("chan-filter-reply") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_WHO_REQ("chan-who-req") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_WHO_REPLY("chan-who-reply") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_USER_REQ("chan-user-req") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHAN_USER_REPLY("chan-user-reply") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		AUTH_MUD_REQ("auth-mud-req") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		AUTH_MUD_REPLY("auth-mud-reply") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		UCACHE_UPDATE("ucache-update") {
			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		CHANLIST_REQ("chanlist-req") {
			@Override
			public void send(Packet packet) {
				// TODO Auto-generated method stub

			}

			@Override
			public void handler(Packet packet) {
				// TODO Auto-generated method stub

			}
		},
		PING("ping") {
			@Override
			public void send(Packet packet) {
				Packet extra = new Packet();
				String tmud = Intermud3.instance.getServer().getServerName();

				try {
					tmud = (String) packet.get(PacketBase.T_MUD.getNum());

					if (packet.size() >= 7)
						extra = new Packet(packet.get(6));
				} catch (I3Exception e) {
					e.printStackTrace();
				}

				Services.ping.send("ping-req", tmud, extra);
			}

			@Override
			public void handler(Packet packet) {
				Services.ping.reqHandler(packet);
			}
		},
		PONG("pong") {
			@Override
			public void send(Packet packet) {
				Packet extra = new Packet();
				String tmud = Intermud3.instance.getServer().getServerName();

				try {
					tmud = (String) packet.get(PacketBase.T_MUD.getNum());

					if (packet.size() >= 7)
						extra = new Packet(packet.get(6));
				} catch (I3Exception e) {
					e.printStackTrace();
				}

				Services.ping.send("ping-reply", tmud, extra);
			}

			@Override
			public void handler(Packet packet) {
				Services.ping.replyHandler(packet);
			}
		},
		PING_REQ("ping-req") {
			@Override
			public void send(Packet packet) {
				Packet extra = new Packet();
				String tmud = Intermud3.instance.getServer().getServerName();

				try {
					tmud = (String) packet.get(PacketBase.T_MUD.getNum());

					if (packet.size() >= 7)
						extra = new Packet(packet.get(6));
				} catch (I3Exception e) {
					e.printStackTrace();
				}

				Services.ping.send("ping-req", tmud, extra);
			}

			@Override
			public void handler(Packet packet) {
				Services.ping.reqHandler(packet);
			}
		},
		PING_REPLY("ping-reply") {
			@Override
			public void send(Packet packet) {
				Packet extra = new Packet();
				String tmud = Intermud3.instance.getServer().getServerName();

				try {
					tmud = (String) packet.get(PacketBase.T_MUD.getNum());

					if (packet.size() >= 7)
						extra = new Packet(packet.get(6));
				} catch (I3Exception e) {
					e.printStackTrace();
				}

				Services.ping.send("ping-reply", tmud, extra);
			}

			@Override
			public void handler(Packet packet) {
				Services.ping.replyHandler(packet);
			}
		};

		private static Map<String, PacketTypes> nameToType;
		private String name;

		private PacketTypes(String name) {
			this.name = name;
		}

		public static PacketTypes getType(String name) {
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

		public abstract void send(Packet packet);

		public abstract void handler(Packet packet);

		private static void logWarn(String warning) {
			Intermud3.instance.logWarn(warning);
		}
	}

	public Packet() {
		super(LPCTypes.MIXEDARR);
	}

	public Packet(Object packet) {
		super.set(packet);
	}
}

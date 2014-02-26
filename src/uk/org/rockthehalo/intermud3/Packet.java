package uk.org.rockthehalo.intermud3;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.services.Services;

public class Packet extends LPCArray {
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
			public void handler(Packet data) {
				logWarn("Ignoring " + data.toMudMode());
			}

			@Override
			public void send(Packet data) {
				Services.startup.send(data);
			}

			@Override
			public void send(int data) {
			}
		},
		STARTUP_REPLY("startup-reply") {
			@Override
			public void handler(Packet data) {
				Services.startup.replyHandler(data);
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		SHUTDOWN("shutdown") {
			@Override
			public void handler(Packet data) {
				logWarn("Ignoring " + data.toMudMode());
			}

			@Override
			public void send(Packet data) {
				Packet packet = new Packet();

				packet.add(data.get());
				Intermud3.network.sendToRouter(PacketTypes.SHUTDOWN, null,
						packet);
			}

			@Override
			public void send(int data) {
				Packet packet = new Packet();

				packet.add(data);
				Intermud3.network.sendToRouter(PacketTypes.SHUTDOWN, null,
						packet);
			}
		},
		ERROR("error") {
			@Override
			public void handler(Packet data) {
				Services.i3Error.replyHandler(data);
			}

			@Override
			public void send(Packet data) {
				Services.i3Error.send(data);
			}

			@Override
			public void send(int data) {
			}
		},
		TELL("tell") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		EMOTE("emoteto") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		WHO_REQ("who-req") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		WHO_REPLY("who-reply") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		FINGER_REQ("finger-req") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		FINGER_REPLY("finger-reply") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		LOCATE_REQ("locate-req") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		LOCATE_REPLY("locate-reply") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_LISTEN("channel-listen") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_MESSAGE("channel-m") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_EMOTE("channel-e") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_TARGET("channel-t") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_ADD("channel-add") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_REMOVE("channel-remove") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_ADMIN("channel-admin") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_FILTER_REQ("chan-filter-req") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_FILTER_REPLY("chan-filter-reply") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_WHO_REQ("chan-who-req") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_WHO_REPLY("chan-who-reply") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_USER_REQ("chan-user-req") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHAN_USER_REPLY("chan-user-reply") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		AUTH_MUD_REQ("auth-mud-req") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		AUTH_MUD_REPLY("auth-mud-reply") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		UCACHE_UPDATE("ucache-update") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		CHANLIST_REQ("chanlist-req") {
			@Override
			public void handler(Packet data) {
			}

			@Override
			public void send(Packet data) {
			}

			@Override
			public void send(int data) {
			}
		},
		PING("ping") {
			@Override
			public void handler(Packet data) {
				Services.ping.reqHandler(data);
			}

			@Override
			public void send(Packet data) {
				Packet extra = new Packet();
				String tmud = Intermud3.instance.getServer().getServerName();

				tmud = (String) data.get(PacketBase.T_MUD.getNum());

				if (data.size() >= 7)
					extra.add(data.get(6));

				Services.ping.send("ping-req", tmud, extra);
			}

			@Override
			public void send(int data) {
			}
		},
		PONG("pong") {
			@Override
			public void handler(Packet data) {
				Services.ping.replyHandler(data);
			}

			@Override
			public void send(Packet data) {
				Packet extra = new Packet();
				String tmud = Intermud3.instance.getServer().getServerName();

				tmud = (String) data.get(PacketBase.T_MUD.getNum());

				if (data.size() >= 7)
					extra.add(data.get(6));

				Services.ping.send("ping-reply", tmud, extra);
			}

			@Override
			public void send(int data) {
			}
		},
		PING_REQ("ping-req") {
			@Override
			public void handler(Packet data) {
				Services.ping.reqHandler(data);
			}

			@Override
			public void send(Packet data) {
				Packet extra = new Packet();
				String tmud = Intermud3.instance.getServer().getServerName();

				tmud = (String) data.get(PacketBase.T_MUD.getNum());

				if (data.size() >= 7)
					extra.add(data.get(6));

				Services.ping.send("ping-req", tmud, extra);
			}

			@Override
			public void send(int data) {
			}
		},
		PING_REPLY("ping-reply") {
			@Override
			public void handler(Packet data) {
				Services.ping.replyHandler(data);
			}

			@Override
			public void send(Packet data) {
				Packet extra = new Packet();
				String tmud = Intermud3.instance.getServer().getServerName();

				tmud = (String) data.get(PacketBase.T_MUD.getNum());

				if (data.size() >= 7)
					extra.add(data.get(6));

				Services.ping.send("ping-reply", tmud, extra);
			}

			@Override
			public void send(int data) {
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

		public abstract void handler(Packet data);

		public abstract void send(Packet data);

		public abstract void send(int data);

		private static void logWarn(String warning) {
			Intermud3.instance.logWarn(warning);
		}
	}

	public Packet() {
		super();
	}

	private String p_toMudMode(Object obj) {
		if (obj instanceof Hashtable) {
			Hashtable<?, ?> set = (Hashtable<?, ?>) obj;
			Set<?> keySet = set.keySet();
			Vector<String> list = new Vector<String>(keySet.size());

			for (Object key : keySet) {
				Object value = set.get(key);

				list.add(p_toMudMode(key) + ":" + p_toMudMode(value));
			}

			if (list.isEmpty())
				return "([])";
			else
				return "([" + StringUtils.join(list, ",") + ",])";
		} else if (obj instanceof Vector) {
			Vector<?> objs = (Vector<?>) obj;
			Vector<String> list = new Vector<String>(objs.size());

			for (Object subObj : objs)
				list.add(p_toMudMode(subObj));

			if (list.isEmpty())
				return "({})";
			else
				return "({" + StringUtils.join(list, ",") + ",})";
		} else if (obj instanceof String) {
			String str = obj.toString();

			str = str.replace("\"", "\\\"");
			str = "\"" + str + "\"";
			str = str.replace("\\", "\\\\");
			str = str.replace("\\\"", "\"");
			str = str.replace("\n", "\\n");
			str = str.replace("\r", "");
			str = str.replace("\t", "\\t");
			str = str.replace("\b", "\\b");
			str = str.replace("\u00A0", " ");

			return str;
		} else {
			if (obj == null)
				return "0";

			return obj.toString();
		}
	}

	/**
	 * Convert LPCArray to a format suitable for transmitting as MudMode.
	 * 
	 * @return the MudMode string
	 */
	public String toMudMode() {
		return p_toMudMode(super.get());
	}

	private String replaceAndIngnoreInStrings(String str, String target,
			String replacement) {
		StringBuffer in = new StringBuffer("");
		int x = 0;

		if (!str.contains("\""))
			return str.replace(target, replacement);

		while (x < str.length()) {
			char c = str.charAt(x);

			if (c == '\\') {
				if (x + 1 < str.length()) {
					in.append("\\");
					x++;
					c = str.charAt(x);
				}

				in.append(c);
				x++;
			} else if (c == '"') {
				x++;

				if (x == str.length()) {
					in.append(c);
				} else {
					StringBuffer tmp = new StringBuffer("\"");

					while (x < str.length()) {
						c = str.charAt(x);
						tmp.append(c);
						x++;

						if (c == '"')
							break;
					}

					in.append(tmp);
				}
			} else {
				if (x + target.length() <= str.length()) {
					if (str.substring(x, x + target.length()).equals(target)) {
						in.append(replacement);
						x += target.length();
					} else {
						in.append(c);
						x++;
					}
				} else {
					in.append(c);
					x++;
				}
			}
		}

		return in.toString();
	}

	private String blankFromStrings(String str, String target) {
		StringBuffer in = new StringBuffer("");
		int x = 0;

		if (!str.contains("\""))
			return str;

		while (x < str.length()) {
			char c = str.charAt(x);

			if (c == '\\') {
				if (x + 1 < str.length()) {
					in.append("\\" + str.charAt(x + 1));
					x++;
				} else {
					in.append(c);
				}

				x++;
			} else if (c == '"') {
				x++;

				if (x == str.length()) {
					in.append(c);
				} else {
					StringBuffer tmpIn = new StringBuffer("\"");

					while (x < str.length()) {
						c = str.charAt(x);

						if (c == '\\') {
							if (x + 1 < str.length()) {
								tmpIn.append("\\" + str.charAt(x + 1));
								x++;
							} else {
								tmpIn.append(c);
							}

							x++;
						} else if (x + target.length() < str.length()
								&& str.substring(x, x + target.length())
										.equals(target)) {
							for (int i = 0; i < target.length(); i++)
								tmpIn.append(" ");

							x += target.length();
						} else {
							tmpIn.append(c);
							x++;

							if (c == '"')
								break;
						}
					}

					in.append(tmpIn);
				}
			} else {
				in.append(c);
				x++;
			}
		}

		return in.toString();
	}

	private String blankFromStrings(String str, String[] targets) {
		for (String target : targets)
			str = blankFromStrings(str, target);

		return str;
	}

	private int scanForward(String str, String leftTarget, String rightTarget) {
		int left = 0, right = str.indexOf(rightTarget);

		while (left < right) {
			left = str.indexOf(leftTarget, left + leftTarget.length());

			if (left == -1)
				break;

			if (left < right)
				right = str.indexOf(rightTarget, right + rightTarget.length());
		}

		return right;
	}

	private Object p_fromMudMode(String mudModeString) throws I3Exception {
		if (mudModeString == null)
			return null;

		mudModeString = mudModeString.trim();

		if (mudModeString.length() < 1) {
			return null;
		} else if (mudModeString.length() == 1) {
			try {
				int x = Integer.parseInt(mudModeString);

				return new LPCInt(Integer.valueOf(x)).get();
			} catch (NumberFormatException e) {
				throw new I3Exception("Invalid LPC Data in string: "
						+ mudModeString);
			}
		}

		if (mudModeString.charAt(0) == '(') {
			if (mudModeString.charAt(1) == '{') {
				LPCArray array = new LPCArray();
				String tmp = mudModeString;

				if (!mudModeString.contains("})"))
					throw new I3Exception("Invalid array format: "
							+ mudModeString);

				tmp = blankFromStrings(tmp, new String[] { "({", "})" });
				tmp = mudModeString = mudModeString.substring(2,
						scanForward(tmp, "({", "})")).trim();
				tmp = blankFromStrings(tmp, new String[] { "({", "})", "([",
						"])", ":", "," });

				while (mudModeString.length() > 0) {
					String value = "";
					int next = -1;

					if (tmp.charAt(0) == '(') {
						if (tmp.charAt(1) == '{') {
							next = scanForward(tmp, "({", "})") + 2;
						} else if (tmp.charAt(1) == '[') {
							next = scanForward(tmp, "([", "])") + 2;
						}
					} else {
						next = tmp.indexOf(',');
					}

					if (next < 0 || next >= mudModeString.length())
						value = mudModeString.trim();
					else
						value = mudModeString.substring(0, next).trim();

					array.add(p_fromMudMode(value));

					if (next < 0 || next >= mudModeString.length()) {
						mudModeString = "";
					} else {
						mudModeString = mudModeString.substring(next + 1)
								.trim();
						tmp = tmp.substring(next + 1).trim();
					}
				}

				return array.get();
			} else if (mudModeString.charAt(1) == '[') {
				LPCMapping mapping = new LPCMapping();
				String tmp = mudModeString;

				if (!mudModeString.contains("])")
						&& !mudModeString.contains(":"))
					throw new I3Exception("Invalid mapping format: "
							+ mudModeString);

				tmp = blankFromStrings(tmp, new String[] { "([", "])" });
				tmp = mudModeString = mudModeString.substring(2,
						scanForward(tmp, "([", "])")).trim();
				tmp = blankFromStrings(tmp, new String[] { "({", "})", "([",
						"])", ":", "," });

				while (mudModeString.length() > 0) {
					String[] kv = { "", "" };
					char delim;
					int next = -1;

					for (int i = 0; i < 2; i++) {
						delim = i == 0 ? ':' : ',';

						if (tmp.charAt(0) == '(') {
							if (tmp.charAt(1) == '{') {
								next = scanForward(tmp, "({", "})") + 2;
							} else if (tmp.charAt(1) == '[') {
								next = scanForward(tmp, "([", "])") + 2;
							}
						} else {
							next = tmp.indexOf(delim);
						}

						if (next < 0 || next >= mudModeString.length()) {
							kv[i] = mudModeString.trim();
							mudModeString = "";
						} else {
							kv[i] = mudModeString.substring(0, next).trim();
							mudModeString = mudModeString.substring(next + 1)
									.trim();
							tmp = tmp.substring(next + 1).trim();
						}
					}

					mapping.set(p_fromMudMode(kv[0]), p_fromMudMode(kv[1]));
				}

				return mapping.get();
			} else {
				throw new I3Exception("Invalid LPC Data in string: "
						+ mudModeString);
			}
		} else if (mudModeString.charAt(0) == '"') {
			StringBuffer in = new StringBuffer("");
			int x = 1;
			char c = '\0';

			while (x < mudModeString.length()) {
				c = mudModeString.charAt(x);

				switch (mudModeString.charAt(x)) {
				case '\\':
					if ((x + 1) < mudModeString.length()) {
						in.append(mudModeString.charAt(x + 1));
						x++;
					} else {
						in.append(c);
					}

					x++;

					break;
				case '"':
					return in.toString();
				default:
					in.append(c);
					x++;

					break;
				}
			}

			return new LPCString(in.toString()).get();
		} else if (Character.isDigit(mudModeString.charAt(0))
				|| mudModeString.charAt(0) == '-') {
			String tmp;
			int x = 0;

			if (mudModeString.length() > 1 && mudModeString.startsWith("0x")) {
				tmp = "0x";
				mudModeString = mudModeString.substring(2,
						mudModeString.length());
			} else if (mudModeString.length() > 1
					&& mudModeString.startsWith("-")) {
				tmp = "-";
				mudModeString = mudModeString.substring(1,
						mudModeString.length());
			} else {
				tmp = "";
			}

			while (!mudModeString.equals("")
					&& (Character.isDigit(mudModeString.charAt(0)))) {
				tmp += mudModeString.charAt(0);

				if (mudModeString.length() > 1)
					mudModeString = mudModeString.substring(1,
							mudModeString.length());
				else
					mudModeString = "";
			}

			try {
				x = Integer.parseInt(tmp);
			} catch (NumberFormatException e) {
				throw new I3Exception("Invalid number format: " + tmp);
			}

			return new LPCInt(Integer.valueOf(x)).get();
		}

		throw new I3Exception("Invalid MudMode string: " + mudModeString);
	}

	/**
	 * Converts a MudMode string into a LPCArray element.
	 * 
	 * @param mudModeString
	 *            the MudMode string to convert
	 */
	public void fromMudMode(String mudModeString) {
		mudModeString = replaceAndIngnoreInStrings(mudModeString, ",})", "})");
		mudModeString = replaceAndIngnoreInStrings(mudModeString, ",])", "])");

		try {
			super.set(p_fromMudMode(mudModeString));
		} catch (I3Exception e) {
			e.printStackTrace();
		}
	}
}

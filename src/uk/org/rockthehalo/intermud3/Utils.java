package uk.org.rockthehalo.intermud3;

import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCMixed;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.LPC.LPCVar;

public class Utils {
	private Utils() {
	}

	public static boolean isLPCArray(Object obj) {
		return LPCArray.class.isInstance(obj);
	}

	public static boolean isLPCInt(Object obj) {
		return LPCInt.class.isInstance(obj);
	}

	public static boolean isLPCMapping(Object obj) {
		return LPCMapping.class.isInstance(obj);
	}

	public static boolean isLPCMixed(Object obj) {
		return LPCMixed.class.isInstance(obj);
	}

	public static boolean isLPCString(Object obj) {
		return LPCString.class.isInstance(obj);
	}

	public static boolean isLPCVar(Object obj) {
		return isLPCArray(obj) || isLPCInt(obj) || isLPCMapping(obj)
				|| isLPCMixed(obj) || isLPCString(obj);
	}

	public static boolean isPlayer(Object player) {
		return Player.class.isInstance(player);
	}

	/**
	 * @param msg
	 *            Pinkfish coded message
	 * @return ChatColor version of Pinkfish coded message.
	 */
	public static String toChatColor(String msg) {
		if (!msg.contains("%^"))
			return msg;

		msg.replace("%^BOLD%^BLACK", ChatColor.DARK_GRAY + "");
		msg.replace("%^BOLD%^%^BLACK", ChatColor.DARK_GRAY + "");
		msg.replace("%^BLACK", ChatColor.BLACK + "");

		msg.replace("%^LIGHTRED", ChatColor.RED + "");
		msg.replace("%^RED", ChatColor.DARK_RED + "");
		msg.replace("%^DARKRED", ChatColor.DARK_RED + "");

		msg.replace("%^LIGHTGREEN", ChatColor.GREEN + "");
		msg.replace("%^GREEN", ChatColor.DARK_GREEN + "");
		msg.replace("%^DARKGREEN", ChatColor.DARK_GREEN + "");

		msg.replace("%^ORANGE", ChatColor.GOLD + "");
		msg.replace("%^LIGHTYELLOW", ChatColor.YELLOW + "");
		msg.replace("%^YELLOW", ChatColor.YELLOW + "");
		msg.replace("%^DARKYELLOW", ChatColor.GOLD + "");

		msg.replace("%^LIGHTBLUE", ChatColor.BLUE + "");
		msg.replace("%^BLUE", ChatColor.DARK_BLUE + "");
		msg.replace("%^DARKBLUE", ChatColor.DARK_BLUE + "");

		msg.replace("%^PINK", ChatColor.LIGHT_PURPLE + "");
		msg.replace("%^LIGHTMAGENTA", ChatColor.LIGHT_PURPLE + "");
		msg.replace("%^PURPLE", ChatColor.DARK_PURPLE + "");
		msg.replace("%^MAGENTA", ChatColor.DARK_PURPLE + "");
		msg.replace("%^DARKMAGENTA", ChatColor.DARK_PURPLE + "");

		msg.replace("%^LIGHTCYAN", ChatColor.AQUA + "");
		msg.replace("%^CYAN", ChatColor.DARK_AQUA + "");
		msg.replace("%^DARKCYAN", ChatColor.DARK_AQUA + "");

		msg.replace("%^LIGHTGREY", ChatColor.WHITE + "");
		msg.replace("%^LIGHTGRAY", ChatColor.WHITE + "");
		msg.replace("%^GREY", ChatColor.GRAY + "");
		msg.replace("%^GRAY", ChatColor.GRAY + "");
		msg.replace("%^DARKGREY", ChatColor.DARK_GRAY + "");
		msg.replace("%^DARKGRAY", ChatColor.DARK_GRAY + "");

		msg.replace("%^WHITE", ChatColor.GRAY + "");

		msg.replace("%^BOLD", ChatColor.BOLD + "");
		msg.replace("%^UNDERLINE", ChatColor.UNDERLINE + "");
		msg.replace("%^ITALIC", ChatColor.ITALIC + "");

		msg.replace("%^RESET", ChatColor.RESET + "");

		msg.replaceAll("%^[A-Z0-9_]%^", "");

		return msg.replace("%^", "");
	}

	/**
	 * @param msg
	 *            ChatColor coded message
	 * @return Pinkfish version of ChatColor coded message.
	 */
	public static String toPinkfish(String msg) {
		if (!msg.contains(ChatColor.COLOR_CHAR + ""))
			return msg;

		String output = new String();

		while (msg.length() > 1) {
			int i = msg.indexOf(ChatColor.COLOR_CHAR);

			if (i == -1)
				break;

			if (i != 0) {
				output += msg.substring(0, i);
				msg = msg.substring(i);
			}

			switch (msg.toLowerCase(Locale.ENGLISH).charAt(1)) {
			case '0':
				output += "%^BLACK%^";
				break; // BLACK
			case '1':
				output += "%^BLUE%^";
				break; // DARK_BLUE
			case '2':
				output += "%^GREEN%^";
				break; // DARK_GREEN
			case '3':
				output += "%^CYAN%^";
				break; // DARK_AQUA
			case '4':
				output += "%^RED%^";
				break; // DARK_RED
			case '5':
				output += "%^MAGENTA%^";
				break; // DARK_PURPLE
			case '6':
				output += "%^ORANGE%^";
				break; // GOLD
			case '7':
				output += "%^WHITE%^";
				break; // GRAY
			case '8':
				output += "%^BOLD%^%^BLACK%^";
				break; // DARK_GRAY
			case '9':
				output += "%^BOLD%^%^BLUE%^";
				break; // BLUE
			case 'a':
				output += "%^BOLD%^%^GREEN%^";
				break; // GREEN
			case 'b':
				output += "%^BOLD%^%^CYAN%^";
				break; // AQUA
			case 'c':
				output += "%^BOLD%^%^RED%^";
				break; // RED
			case 'd':
				output += "%^BOLD%^%^MAGENTA%^";
				break; // LIGHT_PURPLE
			case 'e':
				output += "%^YELLOW%^";
				break; // YELLOW
			case 'f':
				output += "%^BOLD%^%^WHITE%^";
				break; // WHITE
			case 'k':
				break; // Magic
			case 'l':
				output += "%^BOLD%^";
				break; // Bold
			case 'm':
				break; // Strikethrough
			case 'n':
				output += "%^UNDERLINE%^";
				break; // Underline
			case 'o':
				output += "%^ITALIC%^";
				break; // Italic
			case 'r':
				output += "%^RESET%^";
				break; // Reset
			default:
				msg = " " + msg;
			}

			msg = msg.substring(2);
		}

		output += msg;

		return output + "%^RESET%^";
	}

	public static int rnd(int range) {
		return (int) (Math.random() * range);
	}

	public static long rnd(long range) {
		return (long) (Math.random() * range);
	}

	public static String toMudMode(Object obj) {
		if (obj == null)
			return "0";

		switch (LPCVar.getType(obj)) {
		case ARRAY: {
			Iterator<Object> itr = ((LPCArray) obj).iterator();
			Vector<String> list = new Vector<String>();

			while (itr.hasNext()) {
				Object next = itr.next();

				list.add(toMudMode(next));
			}

			if (list.isEmpty())
				return "({})";
			else
				return "({" + StringUtils.join(list, ",") + ",})";
		}
		case INT:
			return ((LPCInt) obj).toString();
		case MAPPING: {
			Set<?> keySet = ((LPCMapping) obj).keySet();
			Vector<String> list = new Vector<String>();

			for (Object key : keySet)
				list.add(toMudMode(key) + ":"
						+ toMudMode(((LPCMapping) obj).get(key)));

			if (list.isEmpty())
				return "([])";
			else
				return "([" + StringUtils.join(list, ",") + ",])";
		}
		case STRING: {
			String str = ((LPCString) obj).toString();

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
		}
		default:
			return obj.toString();
		}
	}

	/**
	 * Converts a MudMode string into a LPC variable.
	 * 
	 * @param mudModeString
	 *            the MudMode string to convert
	 */
	public static Object toObject(String mudModeString) {
		mudModeString = replaceAndIngnoreInStrings(mudModeString, ",})", "})");
		mudModeString = replaceAndIngnoreInStrings(mudModeString, ",])", "])");

		Object obj = null;

		try {
			obj = p_fromMudMode(mudModeString);
		} catch (I3Exception i3E) {
			Log.error("", i3E);
		}

		return obj;
	}

	private static String replaceAndIngnoreInStrings(String str, String target,
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
					c = str.charAt(++x);
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
						c = str.charAt(x++);
						tmp.append(c);

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

	private static String blankFromStrings(String str, String target) {
		StringBuffer in = new StringBuffer("");
		int x = 0;

		if (!str.contains("\""))
			return str;

		while (x < str.length()) {
			char c = str.charAt(x);

			if (c == '\\') {
				if (x + 1 < str.length()) {
					in.append("\\");
					c = str.charAt(++x);
				}

				in.append(c);
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
								tmpIn.append("\\");
								c = str.charAt(++x);
							}

							tmpIn.append(c);
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

	private static String blankFromStrings(String str, String[] targets) {
		for (String target : targets)
			str = blankFromStrings(str, target);

		return str;
	}

	private static int scanForward(String str, String leftTarget,
			String rightTarget) {
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

	private static Object p_fromMudMode(String mudModeString)
			throws I3Exception {
		if (mudModeString == null)
			return null;

		mudModeString = mudModeString.trim();

		if (mudModeString.length() < 1) {
			return null;
		} else if (mudModeString.length() == 1) {
			try {
				int x = Integer.parseInt(mudModeString);

				return new LPCInt(Integer.valueOf(x));
			} catch (NumberFormatException nfE) {
				throw new I3Exception("Invalid LPC Data in string: "
						+ mudModeString, nfE);
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

					if (next < 0 || next >= mudModeString.length()) {
						value = mudModeString.trim();
						mudModeString = "";
					} else {
						value = mudModeString.substring(0, next).trim();
						mudModeString = mudModeString.substring(next + 1)
								.trim();
						tmp = tmp.substring(next + 1).trim();
					}

					array.add(p_fromMudMode(value));
				}

				return array;
			} else if (mudModeString.charAt(1) == '[') {
				LPCMapping mapping = new LPCMapping();
				String tmp = mudModeString;

				if (!mudModeString.contains("])"))
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

				return mapping;
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

				switch (c) {
				case '\\':
					if ((x + 1) < mudModeString.length()) {
						c = mudModeString.charAt(++x);
					}

					in.append(c);
					x++;

					break;
				case '"':
					return new LPCString(in.toString());
				default:
					in.append(c);
					x++;

					break;
				}
			}

			return new LPCString(in.toString());
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
			} catch (NumberFormatException nfE) {
				throw new I3Exception("Invalid number format: " + tmp, nfE);
			}

			return new LPCInt(Integer.valueOf(x));
		}

		throw new I3Exception("Invalid MudMode string: " + mudModeString);
	}
}

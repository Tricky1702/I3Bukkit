package uk.org.rockthehalo.intermud3;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCMixed;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.LPC.LPCVar;

public class Utils {
	private static final Pattern INVALIDPATHCHARS = Pattern.compile("[^A-Za-z0-9#': -]");
	private static final char COLOR_CHAR = '&';
	private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf(COLOR_CHAR) + "[0-9A-FK-OR]");
	private static final Pattern URL_PATTERN = Pattern.compile("(?i)https?://\\S+");

	private Utils() {
	}

	public static String getServerName() {
		return stripColor(Bukkit.getServer().getServerName());
	}

	public static boolean isLPCArray(final Object obj) {
		return LPCArray.class.isInstance(obj);
	}

	public static boolean isLPCInt(final Object obj) {
		return LPCInt.class.isInstance(obj);
	}

	public static boolean isLPCMapping(final Object obj) {
		return LPCMapping.class.isInstance(obj);
	}

	public static boolean isLPCMixed(final Object obj) {
		return LPCMixed.class.isInstance(obj);
	}

	public static boolean isLPCString(final Object obj) {
		return LPCString.class.isInstance(obj);
	}

	public static boolean isLPCVar(final Object obj) {
		return isLPCArray(obj) || isLPCInt(obj) || isLPCMapping(obj) || isLPCMixed(obj) || isLPCString(obj);
	}

	public static boolean isPlayer(final Object player) {
		return Player.class.isInstance(player);
	}

	public static List<Object> nullList(int size) {
		return Collections.nCopies(size, null);
	}

	public static int rnd(final int range) {
		return (int) (Math.random() * range);
	}

	public static long rnd(final long range) {
		return (long) (Math.random() * range);
	}

	public static String safePath(String path) {
		if (path == null)
			return null;

		path = stripColor(path);

		return INVALIDPATHCHARS.matcher(path).replaceAll("_");
	}

	public static String stripColor(String msg) {
		if (msg == null)
			return null;

		msg = toChatColor(msg);

		return ChatColor.stripColor(msg);
	}

	/**
	 * @param msg
	 *            Pinkfish coded message
	 * @return ChatColor version of Pinkfish coded message.
	 */
	public static String toChatColor(String msg) {
		if (msg == null || msg.isEmpty() || !msg.contains("%^"))
			return msg;

		msg = msg.replace("%^BOLD%^BLACK", ChatColor.DARK_GRAY + "").replace("%^BOLD%^%^BLACK", ChatColor.DARK_GRAY + "")
				.replace("%^BLACK", ChatColor.BLACK + "");

		msg = msg.replace("%^LIGHTRED", ChatColor.RED + "").replace("%^RED", ChatColor.DARK_RED + "")
				.replace("%^DARKRED", ChatColor.DARK_RED + "");

		msg = msg.replace("%^LIGHTGREEN", ChatColor.GREEN + "").replace("%^GREEN", ChatColor.DARK_GREEN + "")
				.replace("%^DARKGREEN", ChatColor.DARK_GREEN + "");

		msg = msg.replace("%^ORANGE", ChatColor.GOLD + "").replace("%^LIGHTYELLOW", ChatColor.YELLOW + "")
				.replace("%^YELLOW", ChatColor.YELLOW + "").replace("%^DARKYELLOW", ChatColor.GOLD + "");

		msg = msg.replace("%^LIGHTBLUE", ChatColor.BLUE + "").replace("%^BLUE", ChatColor.DARK_BLUE + "")
				.replace("%^DARKBLUE", ChatColor.DARK_BLUE + "");

		msg = msg.replace("%^PINK", ChatColor.LIGHT_PURPLE + "").replace("%^LIGHTMAGENTA", ChatColor.LIGHT_PURPLE + "")
				.replace("%^PURPLE", ChatColor.DARK_PURPLE + "").replace("%^MAGENTA", ChatColor.DARK_PURPLE + "")
				.replace("%^DARKMAGENTA", ChatColor.DARK_PURPLE + "");

		msg = msg.replace("%^LIGHTCYAN", ChatColor.AQUA + "").replace("%^CYAN", ChatColor.DARK_AQUA + "")
				.replace("%^DARKCYAN", ChatColor.DARK_AQUA + "");

		msg = msg.replace("%^LIGHTGREY", ChatColor.WHITE + "").replace("%^LIGHTGRAY", ChatColor.WHITE + "")
				.replace("%^GREY", ChatColor.GRAY + "").replace("%^GRAY", ChatColor.GRAY + "")
				.replace("%^DARKGREY", ChatColor.DARK_GRAY + "").replace("%^DARKGRAY", ChatColor.DARK_GRAY + "");

		msg = msg.replace("%^WHITE", ChatColor.GRAY + "");

		msg = msg.replace("%^BOLD", ChatColor.BOLD + "").replace("%^UNDERLINE", ChatColor.UNDERLINE + "")
				.replace("%^ITALIC", ChatColor.ITALIC + "");

		msg = msg.replace("%^RESET", ChatColor.RESET + "");

		// Cleanup.
		msg = msg.replaceAll("%^[A-Z0-9_]%^", "").replaceAll("%^", "").replaceAll(ChatColor.RESET + "[ \t]*$", "");

		return msg;
	}

	public static String toMudMode(final Object obj) {
		if (obj == null)
			return "0";

		if (!isLPCVar(obj))
			return obj.toString();

		switch (LPCVar.getType(obj)) {
		case ARRAY: {
			if (((LPCArray) obj).isEmpty())
				return "({})";

			final ArrayList<String> list = new ArrayList<String>(((LPCArray) obj).size());

			for (final Object elem : (LPCArray) obj)
				list.add(toMudMode(elem));

			return "({" + StringUtils.join(list, ",") + ",})";
		}
		case INT:
			return ((LPCInt) obj).toString();
		case MAPPING: {
			if (((LPCMapping) obj).isEmpty())
				return "([])";

			final ArrayList<String> list = new ArrayList<String>(((LPCMapping) obj).size());

			for (final Entry<Object, Object> mapping : ((LPCMapping) obj).entrySet())
				list.add(toMudMode(mapping.getKey()) + ":" + toMudMode(mapping.getValue()));

			return "([" + StringUtils.join(list, ",") + ",])";
		}
		case STRING: {
			String str = ((LPCString) obj).toString();

			str = str.replace("\"", "\\\"");
			str = "\"" + str + "\"";

			str = str.replace("\\", "\\\\").replace("\\\"", "\"").replace("\n", "\\n").replace("\r", "").replace("\t", "\\t")
					.replace("\b", "\\b").replace("\u00a0", " ");

			return str;
		}
		case MIXED:
			return toMudMode(((LPCMixed) obj).getLPCData());
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

	/**
	 * @param msg
	 *            ChatColor coded message
	 * @return Pinkfish version of ChatColor coded message.
	 */
	public static String toPinkfish(String msg) {
		if (msg == null || msg.isEmpty())
			return msg;

		if (COLOR_PATTERN.matcher(msg).lookingAt()) {
			Matcher matcher = URL_PATTERN.matcher(msg);

			while (matcher.find()) {
				final String match = matcher.group();
				final String replacement = match.replaceAll(COLOR_CHAR + "", COLOR_CHAR + "_");

				msg = msg.replace(match, replacement);
			}

			msg = ChatColor.translateAlternateColorCodes(COLOR_CHAR, msg);
			matcher = URL_PATTERN.matcher(msg);

			while (matcher.find()) {
				final String match = matcher.group();
				final String replacement = match.replaceAll(COLOR_CHAR + "_", COLOR_CHAR + "");

				msg = msg.replace(match, replacement);
			}
		}

		if (!msg.contains(ChatColor.COLOR_CHAR + ""))
			return msg;

		String output = "";

		while (msg.length() > 1) {
			final int i = msg.indexOf(ChatColor.COLOR_CHAR);

			if (i == -1)
				break;

			if (i != 0) {
				output += msg.substring(0, i);
				msg = msg.substring(i);
			}

			if (msg.length() == 1)
				break;

			switch (msg.toLowerCase().charAt(1)) {
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

	private static String blankFromStrings(final String str, final String target) {
		if (str == null || target == null)
			return null;

		if (str.isEmpty() || target.isEmpty() || !str.contains("\""))
			return str;

		StringBuffer in = new StringBuffer("");
		int x = 0;

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
						} else if (x + target.length() < str.length() && str.substring(x, x + target.length()).equals(target)) {
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

	private static String blankFromStrings(String str, final String[] targets) {
		if (str == null || targets == null)
			return null;

		for (final String target : targets)
			str = blankFromStrings(str, target);

		return str;
	}

	private static Object p_fromMudMode(String mudModeString) throws I3Exception {
		if (mudModeString == null)
			return null;

		mudModeString = mudModeString.trim();

		if (mudModeString.isEmpty()) {
			return null;
		} else if (mudModeString.length() == 1) {
			try {
				final long x = Long.parseLong(mudModeString);

				return new LPCInt(Long.valueOf(x));
			} catch (NumberFormatException nfE) {
				throw new I3Exception("Invalid LPC Data in string: " + mudModeString, nfE);
			}
		}

		if (mudModeString.charAt(0) == '(') {
			if (mudModeString.charAt(1) == '{') {
				final LPCArray array = new LPCArray();
				String tmp = mudModeString;

				if (!mudModeString.contains("})"))
					throw new I3Exception("Invalid array format: " + mudModeString);

				tmp = blankFromStrings(tmp, new String[] { "({", "})" });
				tmp = mudModeString = mudModeString.substring(2, scanForward(tmp, "({", "})")).trim();
				tmp = blankFromStrings(tmp, new String[] { "({", "})", "([", "])", ":", "," });

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
						mudModeString = mudModeString.substring(next + 1).trim();
						tmp = tmp.substring(next + 1).trim();
					}

					array.add(p_fromMudMode(value));
				}

				return array;
			} else if (mudModeString.charAt(1) == '[') {
				final LPCMapping mapping = new LPCMapping();
				String tmp = mudModeString;

				if (!mudModeString.contains("])"))
					throw new I3Exception("Invalid mapping format: " + mudModeString);

				tmp = blankFromStrings(tmp, new String[] { "([", "])" });
				tmp = mudModeString = mudModeString.substring(2, scanForward(tmp, "([", "])")).trim();
				tmp = blankFromStrings(tmp, new String[] { "({", "})", "([", "])", ":", "," });

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
							mudModeString = mudModeString.substring(next + 1).trim();
							tmp = tmp.substring(next + 1).trim();
						}
					}

					mapping.set(p_fromMudMode(kv[0]), p_fromMudMode(kv[1]));
				}

				return mapping;
			} else {
				throw new I3Exception("Invalid LPC Data in string: " + mudModeString);
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

			byte[] data = new byte[in.length()];

			try {
				data = in.toString().getBytes("ISO-8859-1");
			} catch (UnsupportedEncodingException ueE) {
				throw new I3Exception("Invalid character encoding: " + in.toString(), ueE);
			}

			for (int i = 0; i < data.length; i++) {
				final int ch = data[i] & 0xff;

				// 160 is a non-breaking space. We'll consider that
				// "printable".
				if (ch < 32 || (ch >= 127 && ch <= 159)) {
					// Java uses it as a replacement character,
					// so it's probably ok for us too.
					data[i] = '?';
				}
			}

			return new LPCString(data.toString());
		} else if (Character.isDigit(mudModeString.charAt(0)) || mudModeString.charAt(0) == '-') {
			String tmp;

			if (mudModeString.length() > 1 && mudModeString.startsWith("0x")) {
				tmp = "0x";
				mudModeString = mudModeString.substring(2);
			} else if (mudModeString.length() > 1 && mudModeString.startsWith("-")) {
				tmp = "-";
				mudModeString = mudModeString.substring(1);
			} else {
				tmp = "";
			}

			while (!mudModeString.isEmpty() && (Character.isDigit(mudModeString.charAt(0)))) {
				tmp += mudModeString.charAt(0);

				if (mudModeString.length() > 1)
					mudModeString = mudModeString.substring(1);
				else
					mudModeString = "";
			}

			try {
				final long x = Long.parseLong(tmp);

				return new LPCInt(Long.valueOf(x));
			} catch (NumberFormatException nfE) {
				throw new I3Exception("Invalid number format: " + tmp, nfE);
			}
		}

		throw new I3Exception("Invalid MudMode string: " + mudModeString);
	}

	private static String replaceAndIngnoreInStrings(final String str, final String target, String replacement) {
		if (str == null || target == null)
			return null;

		if (replacement == null)
			replacement = "";

		if (!str.contains("\""))
			return str.replace(target, replacement);

		StringBuffer in = new StringBuffer("");
		int x = 0;

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

	private static int scanForward(final String str, final String leftTarget, final String rightTarget) {
		if (str == null || leftTarget == null || rightTarget == null)
			return -1;

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
}

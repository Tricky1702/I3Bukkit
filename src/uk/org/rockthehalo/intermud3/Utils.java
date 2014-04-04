package uk.org.rockthehalo.intermud3;

import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {
	private Utils() {
	}

	/**
	 * @param msg
	 */
	public static void debug(String msg) {
		if (Intermud3.instance.getConfig().getBoolean("debug", false))
			logInfo("Debug: " + msg);
	}

	public static boolean isPlayer(Object player) {
		return Player.class.isInstance(player);
	}

	/**
	 * @param msg
	 * @param level
	 */
	private static void log(String msg, Level level) {
		Intermud3.instance.getLogger().log(level, msg);
	}

	/**
	 * @param msg
	 * @param level
	 * @param thrown
	 */
	private static void log(String msg, Level level, Throwable thrown) {
		Intermud3.instance.getLogger().log(level, msg, thrown);
	}

	/**
	 * @param msg
	 */
	public static void logError(String msg) {
		log("Error: " + msg, Level.SEVERE);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void logError(String msg, Throwable thrown) {
		log("Error: " + msg, Level.SEVERE, thrown);
	}

	/**
	 * @param msg
	 */
	public static void logInfo(String msg) {
		log(msg, Level.INFO);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void logInfo(String msg, Throwable thrown) {
		log(msg, Level.INFO, thrown);
	}

	/**
	 * @param msg
	 */
	public static void logWarn(String msg) {
		log("Warning: " + msg, Level.WARNING);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void logWarn(String msg, Throwable thrown) {
		log("Warning: " + msg, Level.WARNING, thrown);
	}

	/**
	 * @param msg
	 *            Pinkfish coded message
	 * @return ChatColor version of Pinkfish coded message.
	 */
	public static String toChatColor(String msg) {
		msg.replace("%^BOLD%^BLACK", ChatColor.DARK_GRAY.toString());
		msg.replace("%^BOLD%^%^BLACK", ChatColor.DARK_GRAY.toString());
		msg.replace("%^BLACK", ChatColor.BLACK.toString());

		msg.replace("%^BOLD%^RED", ChatColor.RED.toString());
		msg.replace("%^BOLD%^%^RED", ChatColor.RED.toString());
		msg.replace("%^LIGHTRED", ChatColor.RED.toString());
		msg.replace("%^RED", ChatColor.DARK_RED.toString());
		msg.replace("%^DARKRED", ChatColor.DARK_RED.toString());

		msg.replace("%^BOLD%^GREEN", ChatColor.GREEN.toString());
		msg.replace("%^BOLD%^%^GREEN", ChatColor.GREEN.toString());
		msg.replace("%^LIGHTGREEN", ChatColor.GREEN.toString());
		msg.replace("%^GREEN", ChatColor.DARK_GREEN.toString());
		msg.replace("%^DARKGREEN", ChatColor.DARK_GREEN.toString());

		msg.replace("%^BOLD%^ORANGE", ChatColor.YELLOW.toString());
		msg.replace("%^BOLD%^%^ORANGE", ChatColor.YELLOW.toString());
		msg.replace("%^ORANGE", ChatColor.GOLD.toString());
		msg.replace("%^LIGHTYELLOW", ChatColor.YELLOW.toString());
		msg.replace("%^YELLOW", ChatColor.YELLOW.toString());
		msg.replace("%^DARKYELLOW", ChatColor.GOLD.toString());

		msg.replace("%^BOLD%^BLUE", ChatColor.BLUE.toString());
		msg.replace("%^BOLD%^%^BLUE", ChatColor.BLUE.toString());
		msg.replace("%^LIGHTBLUE", ChatColor.BLUE.toString());
		msg.replace("%^BLUE", ChatColor.DARK_BLUE.toString());
		msg.replace("%^DARKBLUE", ChatColor.DARK_BLUE.toString());

		msg.replace("%^BOLD%^MAGENTA", ChatColor.LIGHT_PURPLE.toString());
		msg.replace("%^BOLD%^%^MAGENTA", ChatColor.LIGHT_PURPLE.toString());
		msg.replace("%^PINK", ChatColor.LIGHT_PURPLE.toString());
		msg.replace("%^LIGHTMAGENTA", ChatColor.LIGHT_PURPLE.toString());
		msg.replace("%^PURPLE", ChatColor.DARK_PURPLE.toString());
		msg.replace("%^MAGENTA", ChatColor.DARK_PURPLE.toString());
		msg.replace("%^DARKMAGENTA", ChatColor.DARK_PURPLE.toString());

		msg.replace("%^BOLD%^CYAN", ChatColor.AQUA.toString());
		msg.replace("%^BOLD%^%^CYAN", ChatColor.AQUA.toString());
		msg.replace("%^LIGHTCYAN", ChatColor.AQUA.toString());
		msg.replace("%^CYAN", ChatColor.DARK_AQUA.toString());
		msg.replace("%^DARKCYAN", ChatColor.DARK_AQUA.toString());

		msg.replace("%^GREY", ChatColor.GRAY.toString());
		msg.replace("%^GRAY", ChatColor.GRAY.toString());
		msg.replace("%^LIGHTGREY", ChatColor.WHITE.toString());
		msg.replace("%^LIGHTGRAY", ChatColor.WHITE.toString());

		msg.replace("%^BOLD%^WHITE", ChatColor.WHITE.toString());
		msg.replace("%^BOLD%^%^WHITE", ChatColor.WHITE.toString());
		msg.replace("%^WHITE", ChatColor.GRAY.toString());

		msg.replace("%^BOLD", ChatColor.BOLD.toString());
		msg.replace("%^UNDERLINE", ChatColor.UNDERLINE.toString());
		msg.replace("%^ITALIC", ChatColor.ITALIC.toString());

		msg.replace("%^RESET", ChatColor.RESET.toString());

		// Pattern STRIP = Pattern.compile("[%][^][A-Z_]");
		// STRIP.matcher(msg).replaceAll("");

		return msg.replace("%^", "") + ChatColor.RESET;
	}

	/**
	 * @param msg
	 *            ChatColor coded message
	 * @return Pinkfish version of ChatColor coded message.
	 */
	public static String toPinkfish(String msg) {
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
		output.replace("\\n", "\n");
		output.replace("\\r", "\r");
		output.replace("\\\"", "'");
		output.replace("\"", "");
		output.replace("\\\\", "\\");

		return output;
	}

	public static int rnd(int range) {
		return (int) (Math.random() * range);
	}

	public static long rnd(long range) {
		return (long) (Math.random() * range);
	}
}

package uk.org.rockthehalo.intermud3;

import java.util.logging.Level;

import org.bukkit.entity.Player;

public class Utils {
	private Utils() {
	}

	/**
	 * @param msg
	 */
	public static void debug(String msg) {
		if (Intermud3.instance.getConfig().getBoolean("debug", false))
			logInfo("[Intermud3] " + msg);
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
		log("[Intermud3] Error: " + msg, Level.SEVERE);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void logError(String msg, Throwable thrown) {
		log("[Intermud3] Error: " + msg, Level.SEVERE, thrown);
	}

	/**
	 * @param msg
	 */
	public static void logInfo(String msg) {
		log("[Intermud3] " + msg, Level.INFO);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void logInfo(String msg, Throwable thrown) {
		log("[Intermud3] " + msg, Level.INFO, thrown);
	}

	/**
	 * @param msg
	 */
	public static void logWarn(String msg) {
		log("[Intermud3] Warning: " + msg, Level.WARNING);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void logWarn(String msg, Throwable thrown) {
		log("[Intermud3] Warning: " + msg, Level.WARNING, thrown);
	}

	public static int rnd(int range) {
		return (int) (Math.random() * range);
	}

	public static long rnd(long range) {
		return (long) (Math.random() * range);
	}
}

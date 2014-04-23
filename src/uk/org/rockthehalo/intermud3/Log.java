package uk.org.rockthehalo.intermud3;

import java.util.logging.Level;

public class Log {
	private Log() {
	}

	/**
	 * @param msg
	 */
	public static void debug(String msg) {
		if (Intermud3.instance.getDebugFlag())
			log("Debug: " + msg, Level.INFO);
	}

	/**
	 * @param msg
	 */
	public static void debug(String msg, Throwable thrown) {
		if (Intermud3.instance.getDebugFlag())
			log("Debug: " + msg, Level.INFO, thrown);
	}

	/**
	 * @param msg
	 */
	public static void error(String msg) {
		log("Error: " + msg, Level.INFO);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void error(String msg, Throwable thrown) {
		log("Error: " + msg, Level.INFO, thrown);
	}

	/**
	 * @param msg
	 */
	public static void info(String msg) {
		log(msg, Level.INFO);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void info(String msg, Throwable thrown) {
		log(msg, Level.INFO, thrown);
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
	public static void warn(String msg) {
		log("Warning: " + msg, Level.INFO);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void warn(String msg, Throwable thrown) {
		log("Warning: " + msg, Level.INFO, thrown);
	}
}

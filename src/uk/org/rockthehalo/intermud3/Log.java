package uk.org.rockthehalo.intermud3;

import java.util.logging.Level;

public class Log {
	private Log() {
	}

	/**
	 * @param msg
	 */
	public static void debug(final String msg) {
		if (Intermud3.instance != null && Intermud3.instance.getDebugFlag())
			log("Debug: " + msg, Level.INFO);
	}

	/**
	 * @param msg
	 */
	public static void debug(final String msg, final Throwable thrown) {
		if (Intermud3.instance != null && Intermud3.instance.getDebugFlag())
			log("Debug: " + msg, Level.INFO, thrown);
	}

	/**
	 * @param msg
	 */
	public static void error(final String msg) {
		log("Error: " + msg, Level.INFO);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void error(final String msg, final Throwable thrown) {
		log("Error: " + msg, Level.INFO, thrown);
	}

	/**
	 * @param msg
	 */
	public static void info(final String msg) {
		log(msg, Level.INFO);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void info(final String msg, final Throwable thrown) {
		log(msg, Level.INFO, thrown);
	}

	/**
	 * @param msg
	 * @param level
	 */
	private static void log(final String msg, final Level level) {
		if (Intermud3.instance != null)
			Intermud3.instance.getLogger().log(level, msg);
	}

	/**
	 * @param msg
	 * @param level
	 * @param thrown
	 */
	private static void log(final String msg, final Level level,
			final Throwable thrown) {
		if (Intermud3.instance != null)
			Intermud3.instance.getLogger().log(level, msg, thrown);
	}

	/**
	 * @param msg
	 */
	public static void warn(final String msg) {
		log("Warning: " + msg, Level.INFO);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public static void warn(final String msg, final Throwable thrown) {
		log("Warning: " + msg, Level.INFO, thrown);
	}
}

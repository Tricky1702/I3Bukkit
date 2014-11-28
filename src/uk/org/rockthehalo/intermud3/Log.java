package uk.org.rockthehalo.intermud3;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
	private static Logger logger = Intermud3.plugin.getLogger();

	private Log() {
	}

	public static boolean getDebugFlag() {
		return Intermud3.config.getConfig().getBoolean("debug", false);
	}

	public static void setDebugFlag(final boolean flag) {
		Intermud3.config.getConfig().set("debug", flag);
	}

	/**
	 * @param msg
	 *            The string message (or a key in the message catalog)
	 */
	public static void debug(final String msg) {
		if (getDebugFlag())
			log(Level.INFO, "Debug: " + msg);
	}

	/**
	 * @param msg
	 *            The string message (or a key in the message catalog)
	 * @param thrown
	 *            Throwable associated with debug message.
	 */
	public static void debug(final String msg, final Throwable thrown) {
		if (getDebugFlag())
			log(Level.INFO, "Debug: " + msg, thrown);
	}

	/**
	 * @param msg
	 *            The string message (or a key in the message catalog)
	 */
	public static void error(final String msg) {
		log(Level.INFO, "Error: " + msg);
	}

	/**
	 * @param msg
	 *            The string message (or a key in the message catalog)
	 * @param thrown
	 *            Throwable associated with error message.
	 */
	public static void error(final String msg, final Throwable thrown) {
		log(Level.INFO, "Error: " + msg, thrown);
	}

	/**
	 * @param msg
	 *            The string message (or a key in the message catalog)
	 */
	public static void info(final String msg) {
		log(Level.INFO, msg);
	}

	/**
	 * @param msg
	 *            The string message (or a key in the message catalog)
	 * @param thrown
	 *            Throwable associated with log message.
	 */
	public static void info(final String msg, final Throwable thrown) {
		log(Level.INFO, msg, thrown);
	}

	/**
	 * Log a message, with no arguments.<br />
	 * <br />
	 * If the logger is currently enabled for the given message level then the
	 * given message is forwarded to all the registered output Handler objects.
	 * 
	 * @param level
	 *            One of the message level identifiers, e.g., SEVERE
	 * @param msg
	 *            The string message (or a key in the message catalog)
	 */
	private static void log(final Level level, final String msg) {
		logger.log(level, msg);
	}

	/**
	 * Log a message, with associated Throwable information.<br />
	 * <br />
	 * If the logger is currently enabled for the given message level then the
	 * given arguments are stored in a LogRecord which is forwarded to all
	 * registered output handlers.<br />
	 * <br />
	 * Note that the thrown argument is stored in the LogRecord thrown property,
	 * rather than the LogRecord parameters property. Thus is it processed
	 * specially by output Formatters and is not treated as a formatting
	 * parameter to the LogRecord message property.
	 * 
	 * @param level
	 *            One of the message level identifiers, e.g., SEVERE
	 * @param msg
	 *            The string message (or a key in the message catalog)
	 * @param thrown
	 *            Throwable associated with log message.
	 */
	private static void log(final Level level, final String msg, final Throwable thrown) {
		logger.log(level, msg, thrown);
	}

	/**
	 * @param msg
	 *            The string message (or a key in the message catalog)
	 */
	public static void warn(final String msg) {
		log(Level.INFO, "Warning: " + msg);
	}

	/**
	 * @param msg
	 *            The string message (or a key in the message catalog)
	 * @param thrown
	 *            Throwable associated with warn message.
	 */
	public static void warn(final String msg, final Throwable thrown) {
		log(Level.INFO, "Warning: " + msg, thrown);
	}
}

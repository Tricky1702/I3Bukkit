package uk.org.rockthehalo.intermud3;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import uk.org.rockthehalo.intermud3.LPC.CallOut;
import uk.org.rockthehalo.intermud3.LPC.HeartBeat;
import uk.org.rockthehalo.intermud3.services.Services;

public class Intermud3 extends JavaPlugin {
	private final long bootTime = System.currentTimeMillis();
	private final int hBeatDelay = 15 * 60;

	private boolean debugFlag = false;

	public static Intermud3 instance = null;
	public static Network network = null;
	public static CallOut callout = null;
	public static HeartBeat heartbeat = null;

	/**
	 * Constructor
	 */
	public Intermud3() {
		instance = this;
	}

	/**
	 * @param msg
	 */
	public void debug(String msg) {
		if (this.debugFlag)
			log("[Intermud3] " + msg, Level.INFO);
	}

	/**
	 * @return the bootTime
	 */
	public long getBootTime() {
		return this.bootTime;
	}

	public void heartBeat() {
		saveConfig();
	}

	/**
	 * @param msg
	 * @param level
	 */
	private void log(String msg, Level level) {
		getLogger().log(level, msg);
	}

	/**
	 * @param msg
	 * @param level
	 * @param thrown
	 */
	private void log(String msg, Level level, Throwable thrown) {
		getLogger().log(level, msg, thrown);
	}

	/**
	 * @param msg
	 */
	public void logError(String msg) {
		log("[Intermud3] Error: " + msg, Level.SEVERE);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public void logError(String msg, Throwable thrown) {
		log("[Intermud3] Error: " + msg, Level.SEVERE, thrown);
	}

	/**
	 * @param msg
	 */
	public void logInfo(String msg) {
		log("[Intermud3] " + msg, Level.INFO);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public void logInfo(String msg, Throwable thrown) {
		log("[Intermud3] " + msg, Level.INFO, thrown);
	}

	/**
	 * @param msg
	 */
	public void logWarn(String msg) {
		log("[Intermud3] Warning: " + msg, Level.WARNING);
	}

	/**
	 * @param msg
	 * @param thrown
	 */
	public void logWarn(String msg, Throwable thrown) {
		log("[Intermud3] Warning: " + msg, Level.WARNING, thrown);
	}

	@Override
	public void onDisable() {
		if (network != null && network.isConnected())
			network.shutdown(0);

		heartbeat.removeHeartBeat(this);
		logInfo(this.toString() + " has been disabled!");
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		this.debugFlag = getConfig().getBoolean("debug", false);

		callout = new CallOut();
		heartbeat = new HeartBeat();
		network = new Network();

		heartbeat.setHeartBeat(this, this.hBeatDelay);
		Services.addServices();
		getCommand("intermud3").setExecutor(new I3Command());

		logInfo(this.toString() + " has been enabled");
	}
}

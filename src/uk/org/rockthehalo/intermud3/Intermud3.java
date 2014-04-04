package uk.org.rockthehalo.intermud3;

import org.bukkit.plugin.java.JavaPlugin;

import uk.org.rockthehalo.intermud3.LPC.CallOut;
import uk.org.rockthehalo.intermud3.services.Services;

public class Intermud3 extends JavaPlugin {
	private final int hBeatDelay = 15 * 60;
	private static long bootTime;

	public static Intermud3 instance = null;
	public static Network network = null;
	public static CallOut callout = null;

	/**
	 * Constructor
	 */
	public Intermud3() {
		instance = this;
	}

	/**
	 * @return the bootTime
	 */
	public long getBootTime() {
		return bootTime;
	}

	public void heartBeat() {
		saveConfig();
	}

	@Override
	public void onDisable() {
		if (network != null && network.isConnected())
			network.shutdown(0);

		callout.removeAllCallOuts();
		callout.removeAllHeartBeats();
		Utils.logInfo(this.toString() + " has been disabled!");

		super.onDisable();
	}

	@Override
	public void onEnable() {
		super.onEnable();

		saveDefaultConfig();

		callout = new CallOut();
		network = new Network();

		callout.addHeartBeat(this, this.hBeatDelay);
		Services.addServices();
		getCommand("intermud3").setExecutor(new I3Command());

		Utils.logInfo(this.toString() + " has been enabled");
	}

	@Override
	public void onLoad() {
		super.onLoad();

		bootTime = System.currentTimeMillis();
	}
}

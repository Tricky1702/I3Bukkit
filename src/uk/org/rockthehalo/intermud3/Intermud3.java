package uk.org.rockthehalo.intermud3;

import org.bukkit.plugin.java.JavaPlugin;

import uk.org.rockthehalo.intermud3.LPC.CallOut;
import uk.org.rockthehalo.intermud3.services.ServiceManager;

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

		ServiceManager.removeServices();

		callout.removeAllCallOuts();
		callout.removeAllHeartBeats();

		instance = null;
		callout = null;
		network = null;
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();

		callout = new CallOut();
		network = new Network();

		callout.addHeartBeat(this, this.hBeatDelay);
		getCommand("intermud3").setExecutor(new I3Command());
	}

	@Override
	public void onLoad() {
		bootTime = System.currentTimeMillis();
	}
}

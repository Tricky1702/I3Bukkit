package uk.org.rockthehalo.intermud3;

import org.bukkit.plugin.java.JavaPlugin;

import uk.org.rockthehalo.intermud3.LPC.CallOut;
import uk.org.rockthehalo.intermud3.LPC.HeartBeat;
import uk.org.rockthehalo.intermud3.services.Services;

public class Intermud3 extends JavaPlugin {
	private final long bootTime = System.currentTimeMillis();
	private final int hBeatDelay = 15 * 60;

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
	 * @return the bootTime
	 */
	public long getBootTime() {
		return this.bootTime;
	}

	public void heartBeat() {
		saveConfig();
	}

	@Override
	public void onDisable() {
		if (network != null && network.isConnected())
			network.shutdown(0);

		heartbeat.remove(this);
		Utils.logInfo(this.toString() + " has been disabled!");
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();

		callout = new CallOut();
		heartbeat = new HeartBeat();
		network = new Network();

		heartbeat.add(this, this.hBeatDelay);
		Services.addServices();
		getCommand("intermud3").setExecutor(new I3Command());

		Utils.logInfo(this.toString() + " has been enabled");
	}
}

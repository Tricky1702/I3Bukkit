package uk.org.rockthehalo.intermud3;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import uk.org.rockthehalo.intermud3.LPC.CallOut;
import uk.org.rockthehalo.intermud3.services.ServiceManager;
import us.crast.bukkituuid.UUIDCache;

public class Intermud3 extends JavaPlugin {
	private final int hBeatDelay = 15 * 60;
	private static long bootTime;

	public static Intermud3 instance = null;
	public static CallOut callout = null;
	public static Network network = null;
	public static UUIDCache uuid = null;

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
		// Shut the network down if connected.
		if (network != null && network.isConnected())
			network.shutdown(0);

		// Remove all Intermud3 services.
		ServiceManager.removeServices();

		// Save the configuration data.
		saveConfig();

		// Remove all remaining callouts and heartbeats.
		callout.remove();

		// Remove references.
		instance = null;
		callout = null;
		network = null;
		uuid = null;
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();

		uuid = new UUIDCache(this);
		callout = new CallOut();
		network = new Network();

		callout.addHeartBeat(this, this.hBeatDelay);

		CommandExecutor cmd = new I3Command();

		getCommand("intermud3").setExecutor(cmd);
		getCommand("i3admin").setExecutor(cmd);
	}

	@Override
	public void onLoad() {
		bootTime = System.currentTimeMillis();
	}
}

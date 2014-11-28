package uk.org.rockthehalo.intermud3;

import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

import uk.org.rockthehalo.intermud3.LPC.CallOut;
import uk.org.rockthehalo.intermud3.services.ServiceManager;
import us.crast.bukkituuid.UUIDCache;

public class Intermud3 extends JavaPlugin {
	private final static long hBeatDelay = 15L * 60L;

	public static long bootTime = 0L;
	public static CallOut callout = null;
	public static Config config = null;
	public static Intermud3 plugin = null;
	public static Network network = null;
	public static UUIDCache uuid = null;

	/**
	 * Constructor
	 */
	public Intermud3() {
		plugin = this;
	}

	/**
	 * Force a config file save.
	 */
	public void heartBeat() {
		config.saveConfig();
	}

	@Override
	public void onDisable() {
		// Shut the network down if connected.
		if (network != null && network.isConnected())
			network.shutdown(0);

		// Remove all remaining callouts and heartbeats.
		callout.remove();

		// Remove all Intermud3 services.
		ServiceManager.removeServices();

		// Save the configuration data.
		config.saveConfig();

		// Remove references.
		callout = null;
		config = null;
		plugin = null;
		network = null;
		uuid = null;
	}

	@Override
	public void onEnable() {
		config = new Config();
		config.saveDefaultConfig();

		uuid = new UUIDCache(this);
		callout = new CallOut();
		network = new Network();

		// Call heartBeat() every 15 minutes.
		callout.addHeartBeat(this, hBeatDelay);

		CommandExecutor cmd = new I3Command();

		getCommand("intermud3").setExecutor(cmd);
		getCommand("i3admin").setExecutor(cmd);
	}

	@Override
	public void onLoad() {
		bootTime = System.currentTimeMillis();
	}
}

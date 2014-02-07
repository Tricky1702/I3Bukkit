package uk.org.rockthehalo.intermud3;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

public class Intermud3 extends JavaPlugin {
	private I3Command cmdExec = new I3Command(this);

	public final static Logger logger = Logger.getLogger("Minecraft");
	public static Intermud3 instance;
	public static String ver;

	@Override
	public void onEnable() {
		ver = this.getDescription().getVersion();
		Intermud3.instance = this;
		getCommand("intermud3").setExecutor(cmdExec);
		getCommand("i3reload").setExecutor(cmdExec);
		getCommand("i3emote").setExecutor(cmdExec);
		getCommand("i3msg").setExecutor(cmdExec);

		if (ver == null || ver.length() < 1) {
			ver = "0.1a";
		}

		this.log("Intermud3 v" + ver + " has been enabled", Level.INFO);
	}

	@Override
	public void onDisable() {
		this.log("Intermud3 v" + ver + " has been disabled!", Level.INFO);
	}

	/**
	 * @param msg
	 * @param level
	 */
	public void log(String msg, Level level) {
		Intermud3.logger.log(level, "[Intermud3] " + msg);
	}

	/**
	 * Send a packet to the I3 router.
	 * 
	 * @param player
	 * @param packetType
	 * @param chan
	 * @param msg
	 */
	public void sendPacket(String name, String packetType, String chan,
			String msg) {
		this.log("Player '" + name + "' sent packetType '" + packetType
				+ "' on channel '" + chan + "' with payload '" + msg + "'",
				Level.INFO);
	}

	/**
	 * Reload the plugin.
	 */
	public void reloadPlugin() {
		this.getServer().getPluginManager().disablePlugin(this);
		this.getServer().getPluginManager().enablePlugin(this);
	}
}

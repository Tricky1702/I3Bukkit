package uk.org.rockthehalo.intermud3;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.plugin.java.JavaPlugin;

import uk.org.rockthehalo.intermud3.services.Services;
import uk.org.rockthehalo.intermud3.testsuite.MudModeTest;

public class Intermud3 extends JavaPlugin {
	private MudModeTest mudmodeTest;

	private I3Command cmdExec;
	private String i3ServerName;
	private String i3ServerIP;
	private int i3ServerPort;
	private LPCData routerList;
	private LPCData routerPassword;
	private int mudlistID;
	private int chanlistID;
	private String adminEmail;
	private String pluginVersion;
	private long bootTime;

	public final static Logger logger = Logger.getLogger("Minecraft");
	public static Intermud3 instance;
	public static Network network;
	public static Services services;

	/**
	 * Constructor
	 */
	public Intermud3() {
		bootTime = System.currentTimeMillis();

		mudmodeTest = new MudModeTest();

		cmdExec = new I3Command();
		instance = this;
		network = null;
		services = null;

		i3ServerName = null;
		i3ServerIP = null;
		i3ServerPort = 0;
		routerList = null;
		routerPassword = new LPCData(0);
		pluginVersion = null;
	}

	public long getBootTime() {
		return bootTime;
	}

	/**
	 * @return the plugin version
	 */
	public String getPluginVersion() {
		return pluginVersion;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	private void setPluginVersion(String pluginVersion) {
		this.pluginVersion = pluginVersion;
	}

	/**
	 * @param routerList
	 *            the routerList to set
	 */
	public void setRouterList(LPCData routerList) {
		this.routerList = new LPCData(routerList);
	}

	/**
	 * @return the routerList
	 */
	public LPCData getRouterList() {
		return (LPCData) routerList.get();
	}

	/**
	 * @param routerPassword
	 *            the routerPassword to set
	 */
	public void setRouterPassword(int routerPassword) {
		this.routerPassword = new LPCData(routerPassword);
	}

	/**
	 * @return the routerPassword
	 */
	public int getRouterPassword() {
		return routerPassword.getInt();
	}

	/**
	 * @return the i3ServerName
	 */
	public String getServerName() {
		return i3ServerName;
	}

	/**
	 * @param i3ServerName
	 *            the i3ServerName to set
	 */
	public void setServerName(String i3ServerName) {
		this.i3ServerName = i3ServerName;
	}

	/**
	 * @return the i3ServerIP
	 */
	public String getServerIP() {
		return i3ServerIP;
	}

	/**
	 * @param i3ServerIP
	 *            the i3ServerIP to set
	 */
	public void setServerIP(String i3ServerIP) {
		this.i3ServerIP = i3ServerIP;
	}

	/**
	 * @return the i3ServerPort
	 */
	public int getServerPort() {
		return i3ServerPort;
	}

	/**
	 * @param i3ServerPort
	 *            the i3ServerPort to set
	 */
	public void setServerPort(int i3ServerPort) {
		this.i3ServerPort = i3ServerPort;
	}

	/**
	 * @return the mudlistID
	 */
	public int getMudlistID() {
		return mudlistID;
	}

	/**
	 * @param mudlistID
	 *            the mudlistID to set
	 */
	public void setMudlistID(int mudlistID) {
		this.mudlistID = mudlistID;
	}

	/**
	 * @return the chanlistID
	 */
	public int getChanlistID() {
		return chanlistID;
	}

	/**
	 * @param chanlistID
	 *            the chanlistID to set
	 */
	public void setChanlistID(int chanlistID) {
		this.chanlistID = chanlistID;
	}

	/**
	 * @return the adminEmail
	 */
	public String getAdminEmail() {
		return adminEmail;
	}

	/**
	 * @param adminEmail
	 *            the adminEmail to set
	 */
	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	@Override
	public void onEnable() {
		String version;

		mudmodeTest.test();

		getCommand("intermud3").setExecutor(cmdExec);

		setPluginVersion(getDescription().getVersion());
		version = getPluginVersion();

		if (version == null || version.length() < 1) {
			version = "0.1a";
			setPluginVersion(version);
		}

		saveDefaultConfig();
		setServerName(getConfig().getString("server.name"));
		setServerIP(getConfig().getString("server.ip"));
		setServerPort(getConfig().getInt("server.port"));
		setRouterPassword(getConfig().getInt("server.password"));
		setMudlistID(getConfig().getInt("server.mudlistid"));
		setChanlistID(getConfig().getInt("server.chanlistid"));
		setAdminEmail(getConfig().getString("adminEmail"));

		services = new Services();
		network = new Network();

		logInfo("Intermud3 v" + version + " has been enabled");
	}

	@Override
	public void onDisable() {
		if (network != null && network.isConnected()) {
			Intermud3.network.shutdown(0);
		}

		logInfo("Intermud3 v" + getPluginVersion() + " has been disabled!");
	}

	public static int rnd(int range) {
		return (int) (Math.random() * (double) range);
	}

	/**
	 * @param msg
	 * @param level
	 */
	private void log(String msg, Level level) {
		Intermud3.logger.log(level, msg);
	}

	/**
	 * @param msg
	 */
	public void logInfo(String msg) {
		log("[Intermud3] " + msg, Level.INFO);
	}

	/**
	 * @param msg
	 */
	public void logWarn(String msg) {
		log("[Intermud3] Warning: " + msg, Level.WARNING);
	}

	/**
	 * @param msg
	 */
	public void logError(String msg) {
		log("[Intermud3] Error: " + msg, Level.SEVERE);
	}
}

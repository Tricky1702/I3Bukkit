package uk.org.rockthehalo.intermud3.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import uk.org.rockthehalo.intermud3.Config;
import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.Payload;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public class I3Mudlist extends ServiceTemplate {
	private static final Payload mudlistPayload = new Payload(Arrays.asList(
			"ML_ID", "ML_INFO"));
	private static final int hBeatDelay = 5 * 60;

	private final Map<String, LPCArray> mudList = Collections
			.synchronizedMap(new HashMap<String, LPCArray>());
	private final Map<String, Long> lastUpdate = Collections
			.synchronizedMap(new HashMap<String, Long>());
	private final Map<String, Integer> stateCount = Collections
			.synchronizedMap(new HashMap<String, Integer>());

	private Config config = null;
	private int HBeat = 0;
	private LPCInt mudlistID = new LPCInt();

	public static final int STATE = 0;
	public static final int IP_ADDR = 1;
	public static final int PORT = 2;
	public static final int OOB_TCP_PORT = 3;
	public static final int OOB_UDP_PORT = 4;
	public static final int MUDLIB = 5;
	public static final int BASELIB = 6;
	public static final int DRIVER = 7;
	public static final int MUDTYPE = 8;
	public static final int MUDSTATUS = 9;
	public static final int EMAIL = 10;
	public static final int SERVICES = 11;
	public static final int OTHER = 12;

	public I3Mudlist() {
	}

	public void create() {
		this.config = new Config(Intermud3.instance, "mudlist.yml");

		if (!this.config.getFile().exists()) {
			final FileConfiguration root = this.config.getConfig();

			root.addDefault("mudlistID", 0L);
			root.addDefault("mudList", "{}");

			this.config.saveConfig();
		}

		reloadConfig();

		Intermud3.callout.addHeartBeat(this, hBeatDelay);
	}

	public void debugInfo() {
		Log.debug("I3Mudlist: mudList: "
				+ StringUtils.join(this.mudList.keySet().iterator(), ", "));
	}

	/**
	 * @return the mudlist
	 */
	public Map<String, LPCArray> getMudlist() {
		return Collections.synchronizedMap(this.mudList);
	}

	/**
	 * @return the mudlistID
	 */
	public LPCInt getMudlistID() {
		return this.mudlistID;
	}

	public void heartBeat() {
		this.HBeat++;

		if (this.HBeat >= 12 * 6)
			this.HBeat = 0;

		if (this.HBeat == 0) {
			final Map<String, Integer> oldStateCount = new HashMap<String, Integer>();

			oldStateCount.putAll(this.stateCount);

			for (final Entry<String, Integer> stateCount : oldStateCount
					.entrySet())
				this.stateCount.put(stateCount.getKey(), 0);
		}

		final Vector<String> muds = new Vector<String>();
		final long tm = System.currentTimeMillis() / 1000;

		for (final Entry<String, LPCArray> mudList : this.mudList.entrySet()) {
			final String mudname = mudList.getKey();
			final LPCArray mudInfo = mudList.getValue();
			final long timestamp;

			if (!this.lastUpdate.containsKey(mudname))
				timestamp = tm;
			else
				timestamp = this.lastUpdate.get(mudname);

			if (mudInfo != null && mudInfo.getLPCInt(STATE).toNum() != -1
					&& tm - timestamp > 7 * 24 * 60 * 60)
				muds.add(mudname);
		}

		if (muds.isEmpty()) {
			saveConfig();

			return;
		}

		if (muds.size() == 1) {
			Log.debug("Removing mud '" + muds.get(0) + "' from the mudlist.");
			removeMudFromList(muds.get(0));
		} else {
			Log.debug("Removing muds '"
					+ StringUtils.join(muds.subList(0, muds.size() - 1), "', '")
					+ "' and '" + muds.get(muds.size() - 1)
					+ "' from the mudlist.");

			for (final String mudname : muds)
				removeMudFromList(mudname);
		}

		saveConfig();
	}

	/**
	 * Reload the mudlist config file and setup the local variables.
	 */
	public void reloadConfig() {
		reloadConfig(false);
	}

	/**
	 * Reload the mudlist config file and setup the local variables.
	 */
	public void reloadConfig(final boolean flag) {
		this.config.reloadConfig();

		final FileConfiguration root = this.config.getConfig();

		this.mudlistID = new LPCInt(root.getLong("mudlistID", 0));
		this.mudList.clear();
		this.lastUpdate.clear();

		if (root.contains("mudUpdate")) {
			final LPCMapping oldMudList = (LPCMapping) Utils.toObject(root
					.getString("mudList", "([])"));

			if (oldMudList != null && !oldMudList.isEmpty())
				for (final Object mudname : oldMudList.keySet()) {
					final String name = Utils.safePath(mudname.toString());

					this.mudList.put(name, oldMudList.getLPCArray(mudname));
					this.lastUpdate
							.put(name, System.currentTimeMillis() / 1000);
				}

			final LPCMapping oldMudUpdate = (LPCMapping) Utils.toObject(root
					.getString("mudUpdate", "([])"));

			if (oldMudUpdate != null && !oldMudUpdate.isEmpty())
				for (final Object mudname : oldMudUpdate.keySet()) {
					final String name = Utils.safePath(mudname.toString());

					if (this.mudList.containsKey(name))
						this.lastUpdate.put(name,
								oldMudUpdate.getLPCInt(mudname).toNum());
				}
		} else {
			final ConfigurationSection mudListSection = root
					.getConfigurationSection("mudList");

			for (final String mudname : mudListSection.getKeys(false)) {
				final ConfigurationSection mudSection = mudListSection
						.getConfigurationSection(mudname);

				this.mudList.put(mudname, (LPCArray) Utils.toObject(mudSection
						.getString("mudInfo", "({})")));
				this.lastUpdate.put(
						mudname,
						mudSection.getLong("lastUpdate",
								System.currentTimeMillis() / 1000));
			}
		}

		if (flag)
			Log.info(this.config.getFile().getName() + " loaded.");
	}

	public void remove() {
		Intermud3.callout.removeHeartBeat(this);
		saveConfig();
		this.config.remove();
		mudlistPayload.remove();

		// Clear out all lists.
		this.mudList.clear();

		// Remove references.
		this.config = null;
		this.mudlistID = null;
	}

	private void removeMudFromList(final String mudname) {
		this.mudList.remove(mudname);
		this.lastUpdate.remove(mudname);
		this.stateCount.remove(mudname);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.org.rockthehalo.intermud3.services.ServiceTemplate#replyHandler(uk
	 * .org.rockthehalo.intermud3.LPC.Packet)
	 */
	@Override
	public void replyHandler(final Packet packet) {
		if (packet.size() != mudlistPayload.size()) {
			Log.error("We don't like mudlist packet size. Should be "
					+ mudlistPayload.size() + " but is " + packet.size());
			Log.error(packet.toMudMode());

			return;
		}

		final LPCString oMudName = packet.getLPCString(Payload.O_MUD);

		if (!oMudName.equals(Intermud3.network.getRouterName())) {
			Log.error("Illegal access. Not from the router.");
			Log.error(packet.toMudMode());

			return;
		}

		setMudlistID(packet.getLPCInt(mudlistPayload.get("ML_ID")));

		final LPCMapping info = packet.getLPCMapping(mudlistPayload
				.get("ML_INFO"));
		long tm = System.currentTimeMillis() / 1000;

		for (final Entry<Object, Object> mud : info.entrySet()) {
			final LPCString mudname = (LPCString) mud.getKey();
			final String name = Utils.safePath(mudname.toString());
			final Object value = mud.getValue();
			LPCArray infoData = null;

			if (Utils.isLPCArray(value))
				infoData = (LPCArray) value;

			if (infoData == null || infoData.isEmpty()
					|| infoData.getLPCInt(STATE) == null) {
				if (this.mudList.containsKey(name)) {
					removeMudFromList(name);
					Log.debug("Removing mud '"
							+ mudname
							+ "', reason: Router has purged it from the listing.");
				}

				continue;
			}

			String msg = "";
			Integer stateCount = this.stateCount.get(name);

			if (stateCount == null)
				stateCount = 0;

			this.stateCount.put(name, ++stateCount);

			final LPCArray mudInfo;

			if (this.mudList.containsKey(name)) {
				mudInfo = this.mudList.get(name);

				if (mudInfo.equals(mudInfo))
					continue;
			} else
				mudInfo = null;

			final long state = infoData.getLPCInt(STATE).toNum();

			if (state != -1) {
				if (state > 7 * 24 * 60 * 60) {
					removeMudFromList(name);
					msg = "Removing mud '" + mudname
							+ "', reason: Shutdown delay is too long.";
				} else {
					msg = "Mud '" + mudname + "' is down. Restart time: ";

					if (state == 0)
						msg += "unknown.";
					else if (state == 1)
						msg += "now.";
					else if (state <= 5 * 60)
						msg += state + " seconds.";
					else
						msg += "indefinate.";

					this.mudList.put(name, infoData);
					this.lastUpdate.put(name, tm);
				}
			} else {
				final LPCString elem5 = infoData.getLPCString(MUDLIB);
				final LPCString elem7 = infoData.getLPCString(DRIVER);

				String extra = "", libname = "", driver = "";

				if (elem5 != null) {
					final String str = elem5.toString().trim();

					infoData.set(MUDLIB, new LPCString(str));

					if (!str.isEmpty())
						libname = "Libname: " + str;
				}

				if (elem7 != null) {
					final String str = elem7.toString().trim();

					infoData.set(DRIVER, new LPCString(str));

					if (!str.isEmpty())
						driver = "Driver: " + str;
				}

				if (!libname.isEmpty() && driver.isEmpty())
					extra = " (" + libname + ")";
				else if (libname.isEmpty() && !driver.isEmpty())
					extra = " (" + driver + ")";
				else if (!libname.isEmpty() && !driver.isEmpty())
					extra = " (" + libname + ", " + driver + ")";

				if (mudInfo != null)
					msg = "Mud '" + mudname + "' is up." + extra;
				else
					msg = "Adding mud '" + mudname + "' to the mudlist."
							+ extra;

				this.mudList.put(name, infoData);
				this.lastUpdate.put(name, tm);
			}

			if (stateCount < 7 && !msg.isEmpty())
				Log.debug(msg);
		}

		saveConfig();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.org.rockthehalo.intermud3.services.ServiceTemplate#reqHandler(uk.org
	 * .rockthehalo.intermud3.LPC.Packet)
	 */
	@Override
	public void reqHandler(final Packet packet) {
	}

	public void saveConfig() {
		saveConfig(false);
	}

	public void saveConfig(final boolean flag) {
		// Clear the configuration.
		this.config.clearConfig();

		final FileConfiguration root = this.config.getConfig();

		root.set("mudlistID", this.mudlistID.toNum());

		final ConfigurationSection mudListSection = root
				.createSection("mudList");

		for (final Entry<String, LPCArray> mudList : this.mudList.entrySet()) {
			final String mudname = Utils.safePath(mudList.getKey());
			final ConfigurationSection mudSection = mudListSection
					.createSection(mudname);
			final LPCArray info = mudList.getValue();

			mudSection.set("mudInfo", Utils.toMudMode(info));

			Long lastUpdate = this.lastUpdate.get(mudname);

			if (lastUpdate == null)
				lastUpdate = System.currentTimeMillis() / 1000;

			mudSection.set("lastUpdate", lastUpdate);
		}

		this.config.saveConfig();

		if (flag)
			Log.info(this.config.getFile().getName() + " saved.");
	}

	/**
	 * @param mudlistID
	 *            the mudlistID to set
	 */
	public void setMudlistID(final long mudlistID) {
		this.mudlistID = new LPCInt(mudlistID);
		this.config.getConfig().set("mudlistID", mudlistID);
	}

	/**
	 * @param mudlistID
	 *            the mudlistID to set
	 */
	public void setMudlistID(final LPCInt mudlistID) {
		this.mudlistID = new LPCInt(mudlistID);
		this.config.getConfig().set("mudlistID", mudlistID.toNum());
	}
}

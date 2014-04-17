package uk.org.rockthehalo.intermud3.services;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;

import uk.org.rockthehalo.intermud3.Config;
import uk.org.rockthehalo.intermud3.I3Exception;
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

	private Config config = null;
	private int HBeat = 0;
	private LPCMapping mudList = new LPCMapping();
	private LPCMapping mudUpdate = new LPCMapping();
	private Map<String, Integer> mudStateCounter = new ConcurrentHashMap<String, Integer>();

	public I3Mudlist() {
	}

	public void create() {
		this.config = new Config(Intermud3.instance, "mudlist.yml");

		if (!this.config.getFile().exists()) {
			FileConfiguration root = this.config.getConfig();

			root.addDefault("mudList", "([])");
			root.addDefault("mudUpdate", "([])");

			this.config.saveConfig();
		}

		reloadConfig(false);

		Intermud3.callout.addHeartBeat(this, hBeatDelay);
	}

	public void debugInfo() {
		Log.debug("I3Mudlist: mudStateCounter:   "
				+ StringUtils.join(this.mudStateCounter.entrySet(), ", "));
		Log.debug("I3Mudlist: mudList:           "
				+ StringUtils.join(this.mudList.keySet().iterator(), ", "));
		Log.debug("I3Mudlist: mudUpdate:         "
				+ StringUtils.join(this.mudUpdate.entrySet().iterator(), ", "));
	}

	public void heartBeat() {
		this.HBeat++;

		if (this.HBeat >= 12)
			this.HBeat = 0;

		if (this.HBeat == 0) {
			Iterator<String> it = this.mudStateCounter.keySet().iterator();

			while (it.hasNext()) {
				String mudname = it.next();

				if (this.mudStateCounter.get(mudname) < 6)
					continue;

				this.mudStateCounter.put(mudname, 0);
			}
		}

		Vector<String> muds = new Vector<String>();
		int tm = (int) (System.currentTimeMillis() / 1000);

		for (Object mudname : this.mudUpdate.keySet()) {
			LPCMapping mudData = this.mudList.getLPCMapping(mudname);
			int timestamp = this.mudUpdate.getLPCInt(mudname).toInt();

			if (mudData != null && mudData.getLPCInt(0).toInt() != -1
					&& tm - timestamp > 7 * 24 * 60 * 60) {
				muds.add(mudname.toString());
			}
		}

		if (muds.isEmpty())
			return;

		if (muds.size() == 1) {
			Log.debug("Removing mud '" + muds.get(0) + "' from the mudlist.");
			this.removeMudFromList(new LPCString(muds.get(0)));
			this.removeMudFromUpdate(new LPCString(muds.get(0)));
		} else {
			Log.debug("Removing muds '"
					+ StringUtils.join(muds.subList(0, muds.size() - 1), "', '")
					+ "' and '" + muds.get(muds.size() - 1)
					+ "' from the mudlist.");

			for (String mudname : muds) {
				this.removeMudFromList(new LPCString(mudname));
				this.removeMudFromUpdate(new LPCString(mudname));
			}
		}

		saveConfig();
	}

	/**
	 * Reload the mudlist config file and setup the local variables.
	 */
	public void reloadConfig() {
		reloadConfig(true);
	}

	/**
	 * Reload the mudlist config file and setup the local variables.
	 */
	public void reloadConfig(boolean flag) {
		this.config.reloadConfig();

		try {
			this.mudList.setLPCData(Utils.toObject(this.config.getConfig()
					.getString("mudList")));
			this.mudUpdate.setLPCData(Utils.toObject(this.config.getConfig()
					.getString("mudUpdate")));
		} catch (I3Exception e) {
			e.printStackTrace();
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
		this.mudStateCounter.clear();
		this.mudUpdate.clear();

		// Remove references.
		this.config = null;
		this.mudList = null;
		this.mudStateCounter = null;
		this.mudUpdate = null;
	}

	private void removeMudFromList(Object mudname) {
		this.mudList.remove(mudname);
	}

	private void removeMudFromUpdate(Object mudname) {
		this.mudUpdate.remove(mudname);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.org.rockthehalo.intermud3.services.ServiceTemplate#replyHandler(uk
	 * .org.rockthehalo.intermud3.LPC.Packet)
	 */
	@Override
	public void replyHandler(Packet packet) {
		if (packet.size() != mudlistPayload.size()) {
			Log.error("We don't like mudlist packet size. Should be "
					+ mudlistPayload.size() + " but is " + packet.size());
			Log.error(packet.toMudMode());

			return;
		}

		int oMud = Payload.O_MUD;
		String oMudName = packet.getLPCString(oMud).toString();

		if (!oMudName.equals(Intermud3.network.getRouterName().toString())) {
			Log.error("Illegal access. Not from the router.");
			Log.error(packet.toMudMode());

			return;
		}

		LPCInt mudlistID = packet.getLPCInt(mudlistPayload.get("ML_ID"));
		LPCMapping info = packet.getLPCMapping(mudlistPayload.get("ML_INFO"));

		if (mudlistID.toInt() <= Intermud3.network.getMudlistID().toInt())
			Log.debug("We don't like packet element 6 ("
					+ mudlistID
					+ ") for '"
					+ info.keySet().toString()
					+ "'. It should be larger than the current one. Continuing anyway.");

		Intermud3.network.setMudlistID(mudlistID);
		Intermud3.instance.saveConfig();

		int tm = (int) (System.currentTimeMillis() / 1000);

		for (Object mudname : info.keySet()) {
			LPCArray infoData = info.getLPCArray(mudname);

			if (infoData == null || infoData.isEmpty()
					|| infoData.getLPCInt(0) == null) {
				if (this.mudList.getLPCString(mudname) != null) {
					this.mudStateCounter.remove(mudname.toString());
					removeMudFromList(mudname);
					removeMudFromUpdate(mudname);
					Log.debug("Removing mud '"
							+ mudname
							+ "', reason: Router has purged it from the listing.");
				}

				continue;
			}

			String msg = new String();
			Integer stateCounter = 0;

			if (this.mudStateCounter.containsKey(mudname.toString()))
				stateCounter = this.mudStateCounter.get(mudname.toString());

			stateCounter++;
			this.mudStateCounter.put(mudname.toString(), stateCounter);

			Integer state = infoData.getLPCInt(0).toInt();

			if (state != -1) {
				if (this.mudList.getLPCArray(mudname) != null) {
					LPCArray mudInfo = this.mudList.getLPCArray(mudname);

					if (mudInfo != null && mudInfo.getLPCInt(0) != null
							&& mudInfo.getLPCInt(0).toInt() == state)
						continue;

					if (state > 7 * 24 * 60 * 60) {
						removeMudFromList(mudname);
						removeMudFromUpdate(mudname);
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
					}

					this.mudList.set(mudname, infoData);
					this.mudUpdate.set(mudname, new LPCInt(tm));
				}
			} else {
				LPCArray mudData = this.mudList.getLPCArray(mudname);

				if (mudData != null && mudData.getLPCInt(0) != null
						&& mudData.getLPCInt(0).toInt() == -1)
					continue;

				LPCString elem5, elem7;
				String extra = "", libname = "", driver = "";

				elem5 = infoData.getLPCString(5);
				elem7 = infoData.getLPCString(7);

				if (elem5 != null) {
					String str = elem5.toString().trim();

					infoData.set(5, new LPCString(str));

					if (!str.isEmpty())
						libname = "Libname: " + str;
				}

				if (elem7 != null) {
					String str = elem7.toString().trim();

					infoData.set(7, new LPCString(str));

					if (!str.isEmpty())
						driver = "Driver: " + str;
				}

				if (!libname.isEmpty() && driver.isEmpty())
					extra = " (" + libname + ")";
				else if (libname.isEmpty() && !driver.isEmpty())
					extra = " (" + driver + ")";
				else if (!libname.isEmpty() && !driver.isEmpty())
					extra = " (" + libname + ", " + driver + ")";

				if (mudData != null)
					msg = "Mud '" + mudname + "' is up." + extra;
				else
					msg = "Adding mud '" + mudname + "' to the mudlist."
							+ extra;

				this.mudList.set(mudname, infoData);
				this.mudUpdate.set(mudname, new LPCInt(tm));
			}

			if (stateCounter < 7 && !msg.isEmpty())
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
	public void reqHandler(Packet packet) {
	}

	public void saveConfig() {
		this.config.getConfig().set("mudList", Utils.toMudMode(this.mudList));
		this.config.getConfig().set("mudUpdate",
				Utils.toMudMode(this.mudUpdate));
		this.config.saveConfig();
	}
}

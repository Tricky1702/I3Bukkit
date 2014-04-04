package uk.org.rockthehalo.intermud3.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.LPC.LPCVar;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketBase;

public class I3Mudlist extends ServiceTemplate {
	private final Intermud3 i3 = Intermud3.instance;
	private final int hBeatDelay = 5 * 60;

	private FileConfiguration mudlistConfig = null;
	private File mudlistConfigFile = null;
	private LPCMapping mudList = new LPCMapping();
	private LPCMapping mudUpdate = new LPCMapping();

	public I3Mudlist() {
		setServiceName("mudlist");
	}

	public void create() {
		saveDefaultConfig();

		try {
			mudList.setLPCData(LPCVar.toObject(getMudlistConfig().getString(
					"mudList")));
			mudUpdate.setLPCData(LPCVar.toObject(getMudlistConfig().getString(
					"mudUpdate")));
		} catch (I3Exception e) {
			e.printStackTrace();
		}

		Intermud3.callout.addHeartBeat(this, this.hBeatDelay);
	}

	public void debugInfo() {
		Utils.debug("I3Mudlist: mudList:   " + this.mudList.keySet().toString());
		Utils.debug("I3Mudlist: mudUpdate: " + this.mudUpdate.toString());
	}

	public FileConfiguration getMudlistConfig() {
		if (this.mudlistConfig == null)
			reloadMudlistConfig();

		return this.mudlistConfig;
	}

	public void heartBeat() {
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

		if (muds.size() == 1) {
			Utils.debug("Removing mud '" + muds.get(0) + "' from the mudlist.");
			this.removeMudFromList(new LPCString(muds.get(0)));
			this.removeMudFromUpdate(new LPCString(muds.get(0)));
		} else if (muds.size() > 1) {
			Utils.debug("Removing muds '"
					+ StringUtils.join(muds.subList(0, muds.size() - 1), "', '")
					+ "' and '" + muds.get(muds.size() - 1)
					+ "' from the mudlist.");

			for (String mudname : muds) {
				this.removeMudFromList(new LPCString(mudname));
				this.removeMudFromUpdate(new LPCString(mudname));
			}
		} else {
			return;
		}

		saveMudlistConfig();
	}

	public void reloadMudlistConfig() {
		if (this.mudlistConfigFile == null)
			this.mudlistConfigFile = new File(this.i3.getDataFolder(),
					"mudlist.yml");

		this.mudlistConfig = YamlConfiguration
				.loadConfiguration(this.mudlistConfigFile);

		// Look for defaults in the jar
		InputStream defConfigStream = this.i3.getResource("mudlist.yml");

		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			this.mudlistConfig.setDefaults(defConfig);
		}
	}

	public void remove() {
		Intermud3.callout.removeHeartBeat(this);
		saveMudlistConfig();
		this.mudList.clear();
		this.mudUpdate.clear();
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
		if (packet.size() != 8) {
			Utils.logError("We don't like mudlist packet size. Should be 8 but is "
					+ packet.size());
			Utils.logError(packet.toMudMode());

			return;
		}

		int oMud = PacketBase.O_MUD.getIndex();
		String oMudName = packet.getLPCString(oMud).toString();

		if (!oMudName.equals(Intermud3.network.getRouterName().toString())) {
			Utils.logError("Illegal access. Not from the router.");
			Utils.logError(packet.toMudMode());

			return;
		}

		LPCInt mudlistID = packet.getLPCInt(6);

		if (mudlistID.toInt() <= Intermud3.network.getMudlistID().toInt())
			Utils.debug("We don't like packet element 6 ("
					+ mudlistID
					+ "). It should be larger than the current one. Continuing anyway.");

		Intermud3.network.setMudlistID(mudlistID);
		this.i3.saveConfig();

		LPCMapping info = packet.getLPCMapping(7);
		int tm = (int) (System.currentTimeMillis() / 1000);

		for (Object mudname : info.keySet()) {
			LPCArray infoData = info.getLPCArray(mudname);

			if (infoData == null || infoData.isEmpty()
					|| infoData.getLPCInt(0) == null) {
				if (this.mudList.getLPCString(mudname) != null) {
					removeMudFromList(mudname);
					removeMudFromUpdate(mudname);
					Utils.debug("Removing mud '"
							+ mudname
							+ "', reason: Router has purged it from the listing.");
				}

				continue;
			}

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
						Utils.debug("Removing mud '" + mudname
								+ "', reason: Shutdown delay is too long.");

					} else {
						String msg = "Mud '" + mudname
								+ "' is down. Restart time: ";

						if (state == 0)
							msg += "unknown.";
						else if (state == 1)
							msg += "now.";
						else if (state <= 5 * 60)
							msg += state + " seconds.";
						else
							msg += "indefinate.";

						Utils.debug(msg);
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
					Utils.debug("Mud '" + mudname + "' is up." + extra);
				else
					Utils.debug("Adding mud '" + mudname + "' to the mudlist."
							+ extra);

				this.mudList.set(mudname, infoData);
				this.mudUpdate.set(mudname, new LPCInt(tm));
			}
		}

		saveMudlistConfig();
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

	public void saveDefaultConfig() {
		if (this.mudlistConfigFile == null)
			this.mudlistConfigFile = new File(this.i3.getDataFolder(),
					"mudlist.yml");

		if (!this.mudlistConfigFile.exists())
			this.i3.saveResource("mudlist.yml", false);
	}

	public void saveMudlistConfig() {
		if (this.mudlistConfig == null || this.mudlistConfigFile == null)
			return;

		getMudlistConfig().set("mudList", LPCVar.toMudMode(this.mudList));
		getMudlistConfig().set("mudUpdate", LPCVar.toMudMode(this.mudUpdate));

		try {
			getMudlistConfig().save(this.mudlistConfigFile);
		} catch (IOException ioE) {
			Utils.logError(
					"Could not save config to " + this.mudlistConfigFile, ioE);
		}
	}
}

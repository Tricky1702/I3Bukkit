package uk.org.rockthehalo.intermud3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;

import uk.org.rockthehalo.intermud3.PacketTypes.PacketType;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.services.I3Startup;
import uk.org.rockthehalo.intermud3.services.ServiceManager;
import uk.org.rockthehalo.intermud3.services.ServiceType;

public class Network implements Runnable {
	private volatile Thread inputThread = null;

	public final long maxRetryTime = 600;
	public final long minRetryTime = 30;
	public final long retryTimeStep = 20;

	private Socket sock = null;
	private DataOutputStream sockOut = null;
	private DataInputStream sockIn = null;

	private LPCString adminEmail = new LPCString();
	private boolean autoConnect = false;
	private LPCInt chanlistID = new LPCInt();
	private List<String> configRouterList = new ArrayList<String>();
	private LPCString defRouterIP = new LPCString();
	private LPCString defRouterName = new LPCString();
	private LPCInt defRouterPort = new LPCInt();
	private LPCString hostName = new LPCString();
	private long idleTimeout = 0;
	private LPCInt mudlistID = new LPCInt();
	private LPCArray preferredRouter = new LPCArray();
	private long reconnectWait = this.minRetryTime;
	private boolean routerConnected = false;
	private LPCString routerIP = new LPCString();
	private LPCArray routerList = new LPCArray();
	private LPCString routerName = new LPCString();
	private LPCInt routerPassword = new LPCInt();
	private LPCInt routerPort = new LPCInt();

	public Network() {
		create();

		if (this.autoConnect) {
			ServiceManager.createServices();
			connect();
		}
	}

	public void addRouter(LPCString routerName, LPCString routerIP,
			LPCInt routerPort) {
		LPCArray newRouterList = new LPCArray();
		LPCArray newRouterArray = new LPCArray();

		newRouterArray.add(new LPCString(routerName));
		newRouterArray.add(new LPCString(routerIP + " " + routerPort));
		newRouterList.add(newRouterArray);

		if (this.routerList != null) {
			ListIterator<Object> litr = this.routerList.listIterator();

			while (litr.hasNext()) {
				LPCArray arr = (LPCArray) litr.next();
				String name = arr.getLPCString(0).toString();
				String ipport = arr.getLPCString(1).toString();
				String[] router = StringUtils.split(ipport, " ");

				if (!name.equals(routerName.toString())
						|| !router[0].equals(routerIP.toString())
						|| Integer.parseInt(router[1]) != routerPort.toInt())
					newRouterList.add(arr);
			}
		}

		this.routerList = new LPCArray(newRouterList);
	}

	public void connect() {
		if (isConnected())
			return;

		if (this.preferredRouter == null || this.preferredRouter.size() < 2) {
			Log.error("No preferred router.");

			return;
		}

		String[] router = StringUtils.split(this.preferredRouter
				.getLPCString(1).toString(), " ");

		try {
			sock = new Socket(router[0].trim(), Integer.parseInt(router[1]
					.trim()));
			sockOut = new DataOutputStream(sock.getOutputStream());
		} catch (UnknownHostException uhE) {
			uhE.printStackTrace();

			return;
		} catch (IOException ioE) {
			ioE.printStackTrace();

			return;
		}

		this.inputThread = new Thread(null, this, "Intermud3");
		this.inputThread.start();

		this.routerConnected = false;
		this.idleTimeout = System.currentTimeMillis();

		I3Startup service = ServiceType.I3STARTUP.getService();

		if (service == null)
			Log.error("I3Startup service not found!");
		else
			Intermud3.callout.addCallOut(service, "send", 2);
	}

	public void create() {
		this.adminEmail = new LPCString(Utils.stripColor(Intermud3.instance
				.getConfig().getString("adminEmail")));
		this.autoConnect = Intermud3.instance.getConfig().getBoolean(
				"autoConnect", false);
		this.chanlistID = new LPCInt(Intermud3.instance.getConfig().getInt(
				"router.chanlistID"));
		this.configRouterList = new ArrayList<String>(Intermud3.instance
				.getConfig().getStringList("router.list"));
		this.hostName = new LPCString(Utils.stripColor(Intermud3.instance
				.getConfig().getString("hostName", "")));
		this.mudlistID = new LPCInt(Intermud3.instance.getConfig().getInt(
				"router.mudlistID"));
		this.routerPassword = new LPCInt(Intermud3.instance.getConfig().getInt(
				"router.password"));

		String preferredRouter = Intermud3.instance.getConfig().getString(
				"router.preferred");
		String[] parts, ipport;

		parts = StringUtils.split(preferredRouter, ",");
		ipport = StringUtils.split(parts[1].trim(), " ");

		this.preferredRouter = new LPCArray();
		this.preferredRouter.add(new LPCString(parts[0].trim()));
		this.preferredRouter.add(new LPCString(parts[1].trim()));

		this.routerName = new LPCString(parts[0].trim());
		this.routerIP = new LPCString(ipport[0].trim());
		this.routerPort = new LPCInt(Integer.parseInt(ipport[1].trim()));

		this.defRouterName = new LPCString(this.routerName);
		this.defRouterIP = new LPCString(this.routerIP);
		this.defRouterPort = new LPCInt(this.routerPort);
		addRouter(this.defRouterName, this.defRouterIP, this.defRouterPort);

		for (int i = 0; i < this.configRouterList.size(); i++) {
			String otherRouter = this.configRouterList.get(i);

			parts = StringUtils.split(otherRouter, ",");
			ipport = StringUtils.split(parts[1].trim(), " ");

			LPCString otherRouterName = new LPCString(parts[0].trim());
			LPCString otherRouterIP = new LPCString(ipport[0].trim());
			LPCInt otherRouterPort = new LPCInt(Integer.parseInt(ipport[1]
					.trim()));

			addRouter(otherRouterName, otherRouterIP, otherRouterPort);
		}
	}

	/**
	 * @return the adminEmail
	 */
	public LPCString getAdminEmail() {
		return this.adminEmail;
	}

	/**
	 * @return the chanlistID
	 */
	public LPCInt getChanlistID() {
		return this.chanlistID;
	}

	/**
	 * @return the configRouterList
	 */
	public List<String> getConfigRouterList() {
		return this.configRouterList;
	}

	/**
	 * @return the hostName
	 */
	public LPCString getHostName() {
		return this.hostName;
	}

	/**
	 * @return the idleTimeout
	 */
	public long getIdleTimeout() {
		return this.idleTimeout;
	}

	/**
	 * @return the mudlistID
	 */
	public LPCInt getMudlistID() {
		return this.mudlistID;
	}

	/**
	 * @return the preferredRouter
	 */
	public LPCArray getPreferredRouter() {
		return this.preferredRouter;
	}

	/**
	 * @return the reconnectWait
	 */
	public long getReconnectWait() {
		return this.reconnectWait;
	}

	/**
	 * @return the routerIP
	 */
	public LPCString getRouterIP() {
		return this.routerIP;
	}

	/**
	 * @return the routerList
	 */
	public LPCArray getRouterList() {
		return this.routerList;
	}

	/**
	 * @return the routerName
	 */
	public LPCString getRouterName() {
		return this.routerName;
	}

	/**
	 * @return the routerPassword
	 */
	public LPCInt getRouterPassword() {
		return this.routerPassword;
	}

	/**
	 * @return the routerPort
	 */
	public LPCInt getRouterPort() {
		return this.routerPort;
	}

	/**
	 * @return true if socket connected, false otherwise
	 */
	public boolean isConnected() {
		return this.sock != null && this.sockIn != null && this.sockOut != null
				&& this.inputThread != null;
	}

	/**
	 * @return true if router connected, false otherwise
	 */
	public boolean isRouterConnected() {
		return this.routerConnected;
	}

	public void reconnect() {
		addRouter(this.defRouterName, this.defRouterIP, this.defRouterPort);
		this.reconnectWait += this.retryTimeStep;

		if (this.reconnectWait > this.maxRetryTime)
			this.reconnectWait = this.maxRetryTime;

		if (isConnected()) {
			shutdown(0);
			this.reconnectWait -= this.retryTimeStep;
			Intermud3.instance.saveConfig();
			reconnect(5);

			return;
		}

		Intermud3.callout.addCallOut(this, "connect", 3);
		Intermud3.instance.saveConfig();
	}

	public void reconnect(long reconnectWait) {
		Intermud3.callout.addCallOut(this, "reconnect", reconnectWait);
	}

	public void remove() {
		remove(0);
	}

	public void remove(int arg) {
		shutdown(arg);

		// Clear out all lists.
		this.configRouterList.clear();
		this.preferredRouter.clear();
		this.routerList.clear();

		// Remove references.
		this.adminEmail = null;
		this.chanlistID = null;
		this.configRouterList = null;
		this.defRouterIP = null;
		this.defRouterName = null;
		this.defRouterPort = null;
		this.hostName = null;
		this.mudlistID = null;
		this.preferredRouter = null;
		this.routerIP = null;
		this.routerList = null;
		this.routerName = null;
		this.routerPassword = null;
		this.routerPort = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			this.sock.setSoTimeout(60000);
			this.sockIn = new DataInputStream(this.sock.getInputStream());
		} catch (IOException ioE) {
			this.sockIn = null;
			this.routerConnected = false;
		}

		while (isConnected()) {
			Packet packet;
			PacketType type = null;
			LPCString omud = null, ouser = null, tmud = null, tuser = null;
			String str, err = null, namedType = "";

			try {
				Thread.sleep(50);
			} catch (InterruptedException iE) {
				if (!isConnected()) {
					Log.warn("Shutdown!!!");

					return;
				}
			}

			try {
				byte[] tmp;
				int len = 0;

				while (isConnected()) {
					try {
						len = sockIn.readInt();

						break;
					} catch (IOException ioE) {
						if ((ioE.getMessage() == null)
								|| (ioE.getMessage().toUpperCase()
										.indexOf("TIMED OUT") < 0))
							throw ioE;

						try {
							Thread.sleep(1000);
						} catch (InterruptedException iE) {
						}

						continue;
					}
				}

				if (len > 65536) {
					int skipped = 0;

					try {
						while (skipped < len)
							skipped += sockIn.skipBytes(len);
					} catch (IOException e) {
					}

					Log.error("Got illegal packet: " + skipped + "/" + len
							+ " bytes.");

					continue;
				}

				tmp = new byte[len];

				while (isConnected()) {
					try {
						sockIn.readFully(tmp);

						break;
					} catch (IOException e) {
						if ((e.getMessage() == null)
								|| (e.getMessage().toUpperCase()
										.indexOf("TIMED OUT") < 0))
							throw e;

						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
						}

						Log.error("Timeout receiving packet sized " + len);

						continue;
					}
				}

				str = new String(tmp);
			} catch (IOException ioE) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException iE) {
					if (!isConnected()) {
						Log.warn("Shutdown!!!");

						return;
					}
				}

				String errMsg = ioE.getMessage() == null ? ioE.toString() : ioE
						.getMessage();

				if (errMsg != null)
					Log.error("inputThread: " + errMsg);

				this.routerConnected = false;
				reconnect(this.reconnectWait);

				return;
			}

			packet = new Packet();
			packet.fromMudMode(str);

			if (packet.isEmpty())
				continue;
			else if (!Utils.isLPCArray(packet))
				err = "packet not array";
			else if (packet.size() <= 6)
				err = "packet size too small";
			else {
				namedType = packet.getLPCString(Payload.TYPE).toString();
				type = PacketType.getNamedType(namedType);
				omud = packet.getLPCString(Payload.O_MUD);
				ouser = packet.getLPCString(Payload.O_USER);
				tmud = packet.getLPCString(Payload.T_MUD);
				tuser = packet.getLPCString(Payload.T_USER);

				if (tmud != null
						&& !tmud.toString().equals(
								Utils.stripColor(Intermud3.instance.getServer()
										.getServerName()))) {
					if (namedType.equals("mudlist")) {
						Log.warn("Wrong destination (" + tmud
								+ ") for mudlist packet.");
						packet.set(
								Payload.T_MUD,
								new LPCString(ChatColor
										.stripColor(Intermud3.instance
												.getServer().getServerName())));
					} else {
						err = "wrong destination mud (" + tmud + ")";
					}
				} else if (omud == null) {
					err = "originating mud not a string";
				} else if (type == null) {
					err = "SERVICE is not a string";
				}
			}

			if (err != null) {
				Log.error(err + ".");
				Log.error(packet.toMudMode());

				continue;
			}

			this.routerConnected = true;
			this.setIdleTimeout(System.currentTimeMillis());

			// Sanity check on the originator username
			if (ouser != null)
				packet.set(Payload.O_USER, new LPCString(ouser.toString()
						.toLowerCase()));

			// Sanity check on the target username
			if (tuser != null)
				packet.set(Payload.T_USER, new LPCString(tuser.toString()
						.toLowerCase()));

			if (type == null)
				Log.warn("Service handler for I3 packet " + packet.toMudMode()
						+ " not available.");
			else
				type.handler(packet);
		}
	}

	/**
	 * @param packet
	 *            the packet to send to the router
	 */
	public void send(Packet packet) {
		if (isConnected())
			send(packet.toMudMode());
	}

	/**
	 * @param str
	 *            the mudmode string to send to the router
	 */
	public void send(String str) {
		if (isConnected()) {
			try {
				byte[] packet = str.getBytes("ISO-8859-1");
				int size = packet.length;

				for (int i = 0; i < size; i++) {
					// 160 is a non-breaking space. We'll consider that
					// "printable".
					if ((packet[i] & 0xFF) < 32
							|| ((packet[i] & 0xFF) >= 127 && (packet[i] & 0xFF) <= 159)) {
						// Java uses it as a replacement character,
						// so it's probably ok for us too.
						packet[i] = '?';
					}
				}

				sockOut.writeInt(size);
				sockOut.write(packet);
				sockOut.flush();
			} catch (UnsupportedEncodingException ueE) {
				String errMsg = ueE.getMessage() == null ? ueE.toString() : ueE
						.getMessage();

				Log.error("Unsupported encoding: " + str);

				if (errMsg != null)
					Log.error(errMsg);
			} catch (IOException ioE) {
				String errMsg = ioE.getMessage() == null ? ioE.toString() : ioE
						.getMessage();

				Log.error("Problem sending data: " + str);

				if (errMsg != null)
					Log.error(errMsg);
			}
		}
	}

	public void sendPacket(String i3Type, String origUser, String targMud,
			String targUser, Packet payload) {
		Packet packet = new Packet();

		packet.add(new LPCString(i3Type));
		packet.add(new LPCInt(5));
		packet.add(new LPCString(Utils.stripColor(Intermud3.instance
				.getServer().getServerName())));

		if (origUser == null) {
			packet.add(new LPCInt(0));
		} else {
			packet.add(new LPCString(Utils.stripColor(origUser).toLowerCase()));
		}

		if (targMud == null) {
			packet.add(new LPCInt(0));
		} else {
			packet.add(new LPCString(Utils.stripColor(targMud)));
		}

		if (targUser == null) {
			packet.add(new LPCInt(0));
		} else {
			packet.add(new LPCString(Utils.stripColor(targUser).toLowerCase()));
		}

		if (payload == null)
			packet.add(new LPCInt(0));
		else
			for (Object obj : payload)
				packet.add(obj);

		send(packet);
	}

	public void sendToRouter(PacketType i3Type, String origUser, Packet packet) {
		sendPacket(i3Type.getName(), origUser, getRouterName().toString(),
				null, packet);
	}

	public void sendToRouter(String i3Type, String origUser, Packet packet) {
		sendPacket(i3Type, origUser, getRouterName().toString(), null, packet);
	}

	public void sendToMud(PacketType i3Type, String origUser, String targMud,
			Packet packet) {
		sendPacket(i3Type.getName(), origUser, targMud, null, packet);
	}

	public void sendToMud(String i3Type, String origUser, String targMud,
			Packet packet) {
		sendPacket(i3Type, origUser, targMud, null, packet);
	}

	public void sendToUser(PacketType i3Type, String origUser, String targMud,
			String targUser, Packet packet) {
		sendPacket(i3Type.getName(), origUser, targMud, targUser, packet);
	}

	public void sendToUser(String i3Type, String origUser, String targMud,
			String targUser, Packet packet) {
		sendPacket(i3Type, origUser, targMud, targUser, packet);
	}

	public void sendToAll(PacketType i3Type, String origUser, Packet packet) {
		sendPacket(i3Type.getName(), origUser, null, null, packet);
	}

	public void sendToAll(String i3Type, String origUser, Packet packet) {
		sendPacket(i3Type, origUser, null, null, packet);
	}

	/**
	 * @param adminEmail
	 *            the adminEmail to set
	 */
	public void setAdminEmail(LPCString adminEmail) {
		this.adminEmail = new LPCString(Utils.stripColor(adminEmail.toString()));
	}

	/**
	 * @param adminEmail
	 *            the adminEmail to set
	 */
	public void setAdminEmail(String adminEmail) {
		this.adminEmail = new LPCString(adminEmail);
	}

	/**
	 * @param chanlistID
	 *            the chanlistID to set
	 */
	public void setChanlistID(int chanlistID) {
		setChanlistID(new LPCInt(chanlistID));
	}

	/**
	 * @param chanlistID
	 *            the chanlistID to set
	 */
	public void setChanlistID(LPCInt chanlistID) {
		this.chanlistID = new LPCInt(chanlistID);
		Intermud3.instance.getConfig().set("router.chanlistID",
				chanlistID.toInt());
	}

	/**
	 * @param configRouterList
	 *            the configRouterList to set
	 */
	public void setConfigRouterList(List<String> configRouterList) {
		this.configRouterList = new ArrayList<String>(configRouterList);
	}

	/**
	 * @param adminEmail
	 *            the adminEmail to set
	 */
	public void setHostName(LPCString hostName) {
		this.hostName = new LPCString(Utils.stripColor(hostName.toString()));
	}

	/**
	 * @param adminEmail
	 *            the adminEmail to set
	 */
	public void setHostName(String hostName) {
		this.hostName = new LPCString(Utils.stripColor(hostName));
	}

	/**
	 * @param idleTimeout
	 *            the idleTimeout to set
	 */
	public void setIdleTimeout(long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	/**
	 * @param mudlistID
	 *            the mudlistID to set
	 */
	public void setMudlistID(int mudlistID) {
		setMudlistID(new LPCInt(mudlistID));
	}

	/**
	 * @param mudlistID
	 *            the mudlistID to set
	 */
	public void setMudlistID(LPCInt mudlistID) {
		this.mudlistID = new LPCInt(mudlistID);
		Intermud3.instance.getConfig().set("router.mudlistID",
				mudlistID.toInt());
	}

	/**
	 * @param preferredRouter
	 *            the preferredRouter to set
	 */
	public void setPreferredRouter(LPCArray preferredRouter) {
		this.preferredRouter = new LPCArray(preferredRouter);
	}

	/**
	 * @param reconnectWait
	 *            the reconnectWait to set
	 */
	public void setReconnectWait(long reconnectWait) {
		this.reconnectWait = reconnectWait;
	}

	/**
	 * @param flag
	 *            set routerConnected true or false
	 */
	public void setRouterConnected(boolean flag) {
		this.routerConnected = flag;
	}

	/**
	 * @param routerIP
	 *            the routerIP to set
	 */
	public void setRouterIP(LPCString routerIP) {
		this.routerIP = new LPCString(routerIP);
	}

	/**
	 * @param routerIP
	 *            the routerIP to set
	 */
	public void setRouterIP(String routerIP) {
		this.routerIP = new LPCString(routerIP);
	}

	/**
	 * @param routerList
	 *            the routerList to set
	 */
	public void setRouterList(LPCArray routerList) {
		this.routerList = new LPCArray(routerList);
	}

	/**
	 * @param routerName
	 *            the routerName to set
	 */
	public void setRouterName(LPCString routerName) {
		this.routerName = new LPCString(routerName);
	}

	/**
	 * @param routerName
	 *            the routerName to set
	 */
	public void setRouterName(String routerName) {
		this.routerName = new LPCString(routerName);
	}

	/**
	 * @param routerPassword
	 *            the routerPassword to set
	 */
	public void setRouterPassword(int routerPassword) {
		this.routerPassword = new LPCInt(routerPassword);
	}

	/**
	 * @param routerPassword
	 *            the routerPassword to set
	 */
	public void setRouterPassword(LPCInt routerPassword) {
		this.routerPassword = new LPCInt(routerPassword);
	}

	/**
	 * @param routerPort
	 *            the routerPort to set
	 */
	public void setRouterPort(int routerPort) {
		this.routerPort = new LPCInt(routerPort);
	}

	/**
	 * @param routerPort
	 *            the routerPort to set
	 */
	public void setRouterPort(LPCInt routerPort) {
		this.routerPort = new LPCInt(routerPort);
	}

	public void shutdown() {
		shutdown(0);
	}

	public void shutdown(int restartDelay) {
		if (isConnected() && isRouterConnected()) {
			Packet packet = new Packet();

			packet.add(new LPCInt(restartDelay));
			sendToRouter("shutdown", null, packet);
			this.routerConnected = false;
		}

		if (this.sockIn != null)
			try {
				this.sockIn.close();
			} catch (IOException ioE) {
				ioE.printStackTrace();
			}

		if (this.sockOut != null)
			try {
				this.sockOut.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		if (this.sock != null)
			try {
				this.sock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

		if (this.inputThread != null) {
			Thread moribund = this.inputThread;

			this.inputThread = null;
			moribund.interrupt();
		}
	}

	/**
	 * Reload the main config file and setup the local variables.
	 */
	public void reloadConfig() {
		this.routerList.clear();
		Intermud3.instance.reloadConfig();
		create();

		if (this.autoConnect && !isConnected()) {
			ServiceManager.removeServices();
			ServiceManager.createServices();
			connect();
		}
	}
}

package uk.org.rockthehalo.intermud3;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.file.FileConfiguration;

import uk.org.rockthehalo.intermud3.PacketTypes.PacketType;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.services.I3Startup;
import uk.org.rockthehalo.intermud3.services.ServiceManager;
import uk.org.rockthehalo.intermud3.services.ServiceType;

public class Network {
	private volatile Thread clentThread = null;

	public final long maxRetryTime = 600;
	public final long minRetryTime = 30;
	public final long retryTimeStep = 20;

	private I3Client client = null;

	private LPCString adminEmail = new LPCString();
	private boolean autoConnect = false;
	private List<String> configRouterList = new ArrayList<String>();
	private LPCString defRouterIP = new LPCString();
	private LPCString defRouterName = new LPCString();
	private LPCInt defRouterPort = new LPCInt();
	private LPCString hostName = new LPCString();
	private long idleTimeout = 0;
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

	public void addRouter(final LPCString routerName, final LPCString routerIP,
			final LPCInt routerPort) {
		final LPCArray newRouterList = new LPCArray();
		final LPCArray newRouterArray = new LPCArray();

		newRouterArray.add(new LPCString(routerName));
		newRouterArray.add(new LPCString(routerIP + " " + routerPort));
		newRouterList.add(newRouterArray);

		if (this.routerList != null) {
			final ListIterator<Object> litr = this.routerList.listIterator();

			while (litr.hasNext()) {
				final LPCArray arr = (LPCArray) litr.next();
				final String name = arr.getLPCString(0).toString();
				final String ipport = arr.getLPCString(1).toString();
				final String[] router = StringUtils.split(ipport, " ");

				if (!name.equals(routerName.toString())
						|| !router[0].equals(routerIP.toString())
						|| Integer.parseInt(router[1]) != routerPort.toNum())
					newRouterList.add(arr);
			}
		}

		this.routerList = new LPCArray(newRouterList);
	}

	public void connect() {
		if (isConnected())
			return;

		final I3Startup service = ServiceType.I3STARTUP.getService();

		if (service == null) {
			Log.error("I3Startup service not found!");

			return;
		}

		if (this.preferredRouter == null || this.preferredRouter.size() < 2) {
			Log.error("No preferred router.");

			return;
		}

		try {
			final String[] router = StringUtils.split(this.preferredRouter
					.getLPCString(1).toString(), " ");
			final String host = router[0].trim();
			final int port = Integer.parseInt(router[1].trim());

			this.client = new I3Client(InetAddress.getByName(host), port);

			this.clentThread = new Thread(this.client);
			this.clentThread.setName("I3Client");
			this.clentThread.setDaemon(true);
			this.clentThread.start();

			this.routerConnected = false;
			this.idleTimeout = System.currentTimeMillis();

			Intermud3.callout.addCallOut(service, "send", 5);
		} catch (NumberFormatException nfE) {
			nfE.printStackTrace();
		} catch (UnknownHostException uhE) {
			uhE.printStackTrace();
		} catch (IOException ioE) {
			ioE.printStackTrace();
		}
	}

	public void create() {
		FileConfiguration root = Intermud3.config.getConfig();

		root.set("router.chanlistID", null);
		root.set("router.mudlistID", null);

		this.adminEmail = new LPCString(Utils.stripColor(root
				.getString("adminEmail")));
		this.autoConnect = root.getBoolean("autoConnect", false);
		this.configRouterList = new ArrayList<String>(
				root.getStringList("router.list"));
		this.hostName = new LPCString(Utils.stripColor(root.getString(
				"hostName", "")));
		this.routerPassword = new LPCInt(root.getLong("router.password"));

		final String preferredRouter = root.getString("router.preferred");
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
			final String otherRouter = this.configRouterList.get(i);

			parts = StringUtils.split(otherRouter, ",");
			ipport = StringUtils.split(parts[1].trim(), " ");

			final LPCString otherRouterName = new LPCString(parts[0].trim());
			final LPCString otherRouterIP = new LPCString(ipport[0].trim());
			final LPCInt otherRouterPort = new LPCInt(
					Integer.parseInt(ipport[1].trim()));

			addRouter(otherRouterName, otherRouterIP, otherRouterPort);
		}

		saveConfig();
	}

	/**
	 * @return the adminEmail
	 */
	public LPCString getAdminEmail() {
		return this.adminEmail;
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
		return this.client != null && this.client.isConnected();
	}

	/**
	 * @return true if router connected, false otherwise
	 */
	public boolean isRouterConnected() {
		return isConnected() && this.routerConnected;
	}

	public void reconnect() {
		addRouter(this.defRouterName, this.defRouterIP, this.defRouterPort);
		this.reconnectWait += this.retryTimeStep;

		if (this.reconnectWait > this.maxRetryTime)
			this.reconnectWait = this.maxRetryTime;

		shutdown(0);
		Intermud3.callout.addCallOut(this, "connect", 5);
		saveConfig();
	}

	public void reconnect(final long reconnectWait) {
		Intermud3.callout.addCallOut(this, "reconnect", reconnectWait);
	}

	/**
	 * Reload the main config file and setup the local variables.
	 */
	public void reloadConfig() {
		reloadConfig(false);
	}

	/**
	 * Reload the main config file and setup the local variables.
	 */
	public void reloadConfig(final boolean flag) {
		this.routerList.clear();
		Intermud3.config.reloadConfig();
		create();

		if (flag)
			Log.info(Intermud3.config.getFile().getName() + " loaded.");

		if (this.autoConnect && !isConnected()) {
			ServiceManager.removeServices();
			ServiceManager.createServices();
			connect();
		}
	}

	public void remove() {
		remove(0);
	}

	public void remove(final long arg) {
		shutdown(arg);

		// Clear out all lists.
		this.configRouterList.clear();
		this.preferredRouter.clear();
		this.routerList.clear();

		// Remove references.
		this.adminEmail = null;
		this.configRouterList = null;
		this.defRouterIP = null;
		this.defRouterName = null;
		this.defRouterPort = null;
		this.hostName = null;
		this.preferredRouter = null;
		this.routerIP = null;
		this.routerList = null;
		this.routerName = null;
		this.routerPassword = null;
		this.routerPort = null;
	}

	public void saveConfig() {
		saveConfig(false);
	}

	public void saveConfig(final boolean flag) {
		Intermud3.config.saveConfig();

		if (flag)
			Log.info(Intermud3.config.getFile().getName() + " saved.");
	}

	/**
	 * @param packet
	 *            the packet to send to the router
	 */
	public void send(final Packet packet) {
		if (isConnected())
			send(packet.toMudMode());
	}

	/**
	 * @param str
	 *            the mudmode string to send to the router
	 */
	public void send(final String str) {
		if (isConnected()) {
			try {
				byte[] packet = str.getBytes("ISO-8859-1");
				final int size = packet.length;

				for (int i = 0; i < size; i++) {
					final int c = packet[i] & 0xff;

					// 160 is a non-breaking space. We'll consider that
					// "printable".
					if (c < 32 || (c >= 127 && c <= 159)) {
						// Java uses it as a replacement character,
						// so it's probably ok for us too.
						packet[i] = '?';
					}
				}

				this.client.send(packet);
			} catch (UnsupportedEncodingException ueE) {
				final String errMsg = ueE.getMessage() == null ? ueE.toString()
						: ueE.getMessage();

				Log.error("Unsupported encoding: " + str);

				if (errMsg != null)
					Log.error(errMsg);
			} catch (IOException ioE) {
				final String errMsg = ioE.getMessage() == null ? ioE.toString()
						: ioE.getMessage();

				Log.error("Problem sending data: " + str);

				if (errMsg != null)
					Log.error(errMsg);
			}
		}
	}

	public void sendPacket(final String i3Type, final String origUser,
			final String targMud, final String targUser, final Packet payload) {
		final Packet packet = new Packet();

		packet.add(new LPCString(i3Type));
		packet.add(new LPCInt(5));
		packet.add(new LPCString(Utils.getServerName()));

		if (origUser == null)
			packet.add(new LPCInt(0));
		else
			packet.add(new LPCString(Utils.stripColor(origUser).toLowerCase()));

		if (targMud == null)
			packet.add(new LPCInt(0));
		else
			packet.add(new LPCString(Utils.stripColor(targMud)));

		if (targUser == null)
			packet.add(new LPCInt(0));
		else
			packet.add(new LPCString(Utils.stripColor(targUser).toLowerCase()));

		if (payload == null)
			packet.add(new LPCInt(0));
		else
			for (final Object obj : payload)
				packet.add(obj);

		send(packet);
	}

	public void sendToRouter(final PacketType i3Type, final String origUser,
			final Packet packet) {
		sendPacket(i3Type.getName(), origUser, getRouterName().toString(),
				null, packet);
	}

	public void sendToRouter(final String i3Type, final String origUser,
			final Packet packet) {
		sendPacket(i3Type, origUser, getRouterName().toString(), null, packet);
	}

	public void sendToMud(final PacketType i3Type, final String origUser,
			final String targMud, final Packet packet) {
		sendPacket(i3Type.getName(), origUser, targMud, null, packet);
	}

	public void sendToMud(final String i3Type, final String origUser,
			final String targMud, final Packet packet) {
		sendPacket(i3Type, origUser, targMud, null, packet);
	}

	public void sendToUser(final PacketType i3Type, final String origUser,
			final String targMud, final String targUser, final Packet packet) {
		sendPacket(i3Type.getName(), origUser, targMud, targUser, packet);
	}

	public void sendToUser(final String i3Type, final String origUser,
			final String targMud, final String targUser, final Packet packet) {
		sendPacket(i3Type, origUser, targMud, targUser, packet);
	}

	public void sendToAll(final PacketType i3Type, final String origUser,
			final Packet packet) {
		sendPacket(i3Type.getName(), origUser, null, null, packet);
	}

	public void sendToAll(final String i3Type, final String origUser,
			final Packet packet) {
		sendPacket(i3Type, origUser, null, null, packet);
	}

	/**
	 * @param adminEmail
	 *            the adminEmail to set
	 */
	public void setAdminEmail(final LPCString adminEmail) {
		this.adminEmail = new LPCString(Utils.stripColor(adminEmail.toString()));
	}

	/**
	 * @param adminEmail
	 *            the adminEmail to set
	 */
	public void setAdminEmail(final String adminEmail) {
		this.adminEmail = new LPCString(adminEmail);
	}

	/**
	 * @param configRouterList
	 *            the configRouterList to set
	 */
	public void setConfigRouterList(final List<String> configRouterList) {
		this.configRouterList = new ArrayList<String>(configRouterList);
	}

	/**
	 * @param adminEmail
	 *            the adminEmail to set
	 */
	public void setHostName(final LPCString hostName) {
		this.hostName = new LPCString(Utils.stripColor(hostName.toString()));
	}

	/**
	 * @param adminEmail
	 *            the adminEmail to set
	 */
	public void setHostName(final String hostName) {
		this.hostName = new LPCString(Utils.stripColor(hostName));
	}

	/**
	 * @param idleTimeout
	 *            the idleTimeout to set
	 */
	public void setIdleTimeout(final long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	/**
	 * @param preferredRouter
	 *            the preferredRouter to set
	 */
	public void setPreferredRouter(final LPCArray preferredRouter) {
		this.preferredRouter = new LPCArray(preferredRouter);
	}

	/**
	 * @param reconnectWait
	 *            the reconnectWait to set
	 */
	public void setReconnectWait(final long reconnectWait) {
		this.reconnectWait = reconnectWait;
	}

	/**
	 * @param flag
	 *            set routerConnected true or false
	 */
	public void setRouterConnected(final boolean flag) {
		this.routerConnected = flag;
	}

	/**
	 * @param routerIP
	 *            the routerIP to set
	 */
	public void setRouterIP(final LPCString routerIP) {
		this.routerIP = new LPCString(routerIP);
	}

	/**
	 * @param routerIP
	 *            the routerIP to set
	 */
	public void setRouterIP(final String routerIP) {
		this.routerIP = new LPCString(routerIP);
	}

	/**
	 * @param routerList
	 *            the routerList to set
	 */
	public void setRouterList(final LPCArray routerList) {
		this.routerList = new LPCArray(routerList);
	}

	/**
	 * @param routerName
	 *            the routerName to set
	 */
	public void setRouterName(final LPCString routerName) {
		this.routerName = new LPCString(routerName);
	}

	/**
	 * @param routerName
	 *            the routerName to set
	 */
	public void setRouterName(final String routerName) {
		this.routerName = new LPCString(routerName);
	}

	/**
	 * @param routerPassword
	 *            the routerPassword to set
	 */
	public void setRouterPassword(final int routerPassword) {
		this.routerPassword = new LPCInt(
				new Integer(routerPassword).longValue());
	}

	/**
	 * @param routerPassword
	 *            the routerPassword to set
	 */
	public void setRouterPassword(final long routerPassword) {
		this.routerPassword = new LPCInt(routerPassword);
	}

	/**
	 * @param routerPassword
	 *            the routerPassword to set
	 */
	public void setRouterPassword(final LPCInt routerPassword) {
		this.routerPassword = new LPCInt(routerPassword);
	}

	/**
	 * @param routerPort
	 *            the routerPort to set
	 */
	public void setRouterPort(final int routerPort) {
		this.routerPort = new LPCInt(new Integer(routerPort).longValue());
	}

	/**
	 * @param routerPort
	 *            the routerPort to set
	 */
	public void setRouterPort(final long routerPort) {
		this.routerPort = new LPCInt(routerPort);
	}

	/**
	 * @param routerPort
	 *            the routerPort to set
	 */
	public void setRouterPort(final LPCInt routerPort) {
		this.routerPort = new LPCInt(routerPort);
	}

	public void shutdown() {
		shutdown(0);
	}

	public void shutdown(final long restartDelay) {
		if (isRouterConnected()) {
			final Packet packet = new Packet();

			Log.debug("Sending shutdown packet...");
			packet.add(new LPCInt(restartDelay));
			sendToRouter("shutdown", null, packet);
			this.routerConnected = false;
		}

		if (this.clentThread != null) {
			final Thread moribund = this.clentThread;

			this.clentThread = null;
			moribund.interrupt();
		}

		if (this.client != null) {
			this.client.remove();
			this.client = null;
		}
	}
}

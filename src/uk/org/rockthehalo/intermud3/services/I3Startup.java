package uk.org.rockthehalo.intermud3.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.Payload;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public class I3Startup extends ServiceTemplate {
	public I3Startup() {
	}

	public void remove() {
		Intermud3.callout.removeCallOuts(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.org.rockthehalo.intermud3.services.ServiceTemplate#replyHandler(uk.org
	 * .rockthehalo.intermud3.LPC.Packet)
	 */
	@Override
	public void replyHandler(Packet packet) {
		if (packet.size() != 8) {
			Log.error("We don't like startup-reply packet size. Should be 8 but is "
					+ packet.size());
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

		LPCArray routerList = new LPCArray();

		if (packet.getLPCArray(6) != null)
			routerList.setLPCData(packet.getLPCArray(6));

		if (routerList.isEmpty()) {
			Log.error("We don't like the absence of packet element 6.");
			Log.error(packet.toMudMode());

			return;
		}

		Intermud3.network.setRouterList(routerList);

		List<String> configRouterList = new ArrayList<String>();

		for (Object obj : routerList)
			configRouterList.add(((LPCArray) obj).getLPCString(0) + ", "
					+ ((LPCArray) obj).getLPCString(1));

		Intermud3.instance.getConfig().set("router.list", configRouterList);

		LPCArray preferredRouter = new LPCArray(routerList.getLPCArray(0));
		LPCString preferredName = preferredRouter.getLPCString(0);
		LPCString preferredAddr = preferredRouter.getLPCString(1);
		String[] router = StringUtils.split(preferredAddr.toString(), " ");
		LPCString preferredIP = new LPCString(router[0].trim());
		LPCInt preferredPort = new LPCInt(Integer.parseInt(router[1].trim()));

		Intermud3.instance.getConfig().set("router.preferred",
				preferredName + ", " + preferredAddr);
		Intermud3.network.setRouterName(preferredName);
		Intermud3.network.setRouterIP(preferredIP);
		Intermud3.network.setRouterPort(preferredPort);

		LPCArray currentRouter = new LPCArray(
				Intermud3.network.getPreferredRouter());
		LPCString currentName = currentRouter.getLPCString(0);
		LPCString currentAddr = currentRouter.getLPCString(1);

		if (preferredName.toString().equals(currentName.toString())
				&& preferredAddr.toString().equals(currentAddr.toString())) {
			LPCInt i = packet.getLPCInt(7);

			if (i != null) {
				Intermud3.instance.getConfig()
						.set("router.password", i.toInt());
				Intermud3.network.setRouterPassword(i);
			}
		} else {
			Log.info("Current router details are "
					+ Utils.toMudMode(currentRouter));
			Log.info("Changing router details to "
					+ Utils.toMudMode(preferredRouter));

			Intermud3.instance.getConfig().set("router.password", 0);
			Intermud3.instance.getConfig().set("router.chanlistID", 0);
			Intermud3.instance.getConfig().set("router.mudlistID", 0);
			Intermud3.instance.saveConfig();
			Intermud3.network.setPreferredRouter(preferredRouter);
			Intermud3.network.shutdown(0);
			Intermud3.callout.addCallOut(Intermud3.network, "connect", 1);

			return;
		}

		Intermud3.instance.saveConfig();

		Log.info("Connection established to I3 router " + preferredName
				+ " at " + preferredAddr);
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

	public void send() {
		if (Intermud3.network.isRouterConnected())
			return;

		I3Channel i3Channel = ServiceType.I3CHANNEL.getService();
		I3Mudlist i3Mudlist = ServiceType.I3MUDLIST.getService();
		Packet payload = new Packet();

		payload.add(Intermud3.network.getRouterPassword());

		if (i3Mudlist == null)
			payload.add(new LPCInt(0));
		else {
			payload.add(i3Mudlist.getMudlistID());
			i3Mudlist.setMudlistID(0);
		}

		if (i3Channel == null)
			payload.add(new LPCInt(0));
		else {
			payload.add(i3Channel.getChanlistID());
			i3Channel.setChanlistID(0);
		}

		payload.add(new LPCInt(Intermud3.instance.getServer().getPort()));
		payload.add(new LPCInt(0));
		payload.add(new LPCInt(0));

		LPCString mudlibVersion = new LPCString(Intermud3.instance.getServer()
				.getName()
				+ " "
				+ Intermud3.instance.getServer().getBukkitVersion());
		LPCString baseMudlibVersion = new LPCString(Intermud3.instance
				.getServer().getName()
				+ " "
				+ Intermud3.instance.getServer().getBukkitVersion());
		LPCString driverVersion = new LPCString("RtH "
				+ Intermud3.instance.getServer().getName() + " Client v"
				+ Intermud3.instance.getDescription().getVersion());
		LPCString mudType = new LPCString("Other."
				+ Intermud3.instance.getServer().getName());
		LPCString openStatus = new LPCString("beta testing");

		payload.add(mudlibVersion);
		payload.add(baseMudlibVersion);
		payload.add(driverVersion);
		payload.add(mudType);
		payload.add(openStatus);
		payload.add(Intermud3.network.getAdminEmail());
		payload.add(ServiceManager.getRouterServices());

		Date bootTime = new Date(Intermud3.instance.getBootTime());
		SimpleDateFormat fmt = new SimpleDateFormat(
				"E, d MMMM yyyy - HH:mm:ss zzz");
		String tmStr = fmt.format(bootTime);

		LPCMapping otherInfo = new LPCMapping();

		otherInfo.set(new LPCString("upsince"), new LPCString(tmStr));

		if (!Intermud3.network.getHostName().isEmpty())
			otherInfo.set(new LPCString("host"),
					Intermud3.network.getHostName());

		otherInfo.set(
				new LPCString("architecture"),
				new LPCString(System.getProperty("os.name") + " "
						+ System.getProperty("os.arch")));
		otherInfo.set(new LPCString("java"),
				new LPCString(System.getProperty("java.version")));

		payload.add(otherInfo);
		Log.debug("I3Startup packet: " + payload.toMudMode());
		Intermud3.network.sendToRouter("startup-req-3", null, payload);
	}
}

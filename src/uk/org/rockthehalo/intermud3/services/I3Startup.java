package uk.org.rockthehalo.intermud3.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.LPC.LPCVar;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketBase;

public class I3Startup extends ServiceTemplate {
	private final Intermud3 i3 = Intermud3.instance;

	public I3Startup() {
		setServiceName("startup");
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
			Utils.logError("We don't like startup-reply packet size. Should be 8 but is "
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

		LPCArray routerList = new LPCArray();

		if (packet.getLPCArray(6) != null)
			routerList.setLPCData(packet.getLPCArray(6));

		if (routerList.size() < 1) {
			Utils.logError("We don't like the absence of packet element 6.");
			Utils.logError(packet.toMudMode());

			return;
		}

		Intermud3.network.setRouterList(routerList);

		List<String> configRouterList = new ArrayList<String>();

		for (Object obj : routerList)
			configRouterList.add(((LPCArray) obj).getLPCString(0) + ", "
					+ ((LPCArray) obj).getLPCString(1));

		this.i3.getConfig().set("router.list", configRouterList);

		LPCArray preferredRouter = new LPCArray(routerList.getLPCArray(0));
		LPCString preferredName = preferredRouter.getLPCString(0);
		LPCString preferredAddr = preferredRouter.getLPCString(1);
		String[] router = StringUtils.split(preferredAddr.toString(), " ");
		LPCString preferredIP = new LPCString(router[0].trim());
		LPCInt preferredPort = new LPCInt(Integer.parseInt(router[1].trim()));

		this.i3.getConfig().set("router.preferred",
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
				this.i3.getConfig().set("router.password", i.toInt());
				Intermud3.network.setRouterPassword(i);
			}
		} else {
			Utils.logInfo("Current router details are "
					+ LPCVar.toMudMode(currentRouter));
			Utils.logInfo("Changing router details to "
					+ LPCVar.toMudMode(preferredRouter));

			this.i3.getConfig().set("router.password", 0);
			this.i3.getConfig().set("router.chanlistID", 0);
			this.i3.getConfig().set("router.mudlistID", 0);
			this.i3.saveConfig();
			Intermud3.network.setPreferredRouter(preferredRouter);
			Intermud3.network.shutdown(0);
			Intermud3.callout.addCallOut(Intermud3.network, "connect", 1);

			return;
		}

		this.i3.saveConfig();

		Utils.logInfo("Connection established to I3 router " + preferredName
				+ " at " + preferredAddr);

		Object service = Services.getService("channel");

		if (service != null)
			Intermud3.callout.addCallOut(service, "requestChanList", 5);
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

		Packet payload = new Packet();

		payload.add(Intermud3.network.getRouterPassword());
		payload.add(Intermud3.network.getMudlistID());
		payload.add(Intermud3.network.getChanlistID());
		payload.add(new LPCInt(this.i3.getServer().getPort()));
		payload.add(new LPCInt(0));
		payload.add(new LPCInt(0));

		LPCString mudlibVersion = new LPCString(this.i3.getServer().getName()
				+ " " + this.i3.getServer().getBukkitVersion());
		LPCString baseMudlibVersion = new LPCString(this.i3.getServer()
				.getName() + " " + this.i3.getServer().getBukkitVersion());
		LPCString driverVersion = new LPCString("RtH "
				+ this.i3.getServer().getName() + " Client v"
				+ this.i3.getDescription().getVersion());
		LPCString mudType = new LPCString("Other."
				+ this.i3.getServer().getName());
		LPCString openStatus = new LPCString("beta testing");

		payload.add(mudlibVersion);
		payload.add(baseMudlibVersion);
		payload.add(driverVersion);
		payload.add(mudType);
		payload.add(openStatus);
		payload.add(Intermud3.network.getAdminEmail());
		payload.add(Services.getRouterServices());

		Date bootTime = new Date(this.i3.getBootTime());
		SimpleDateFormat fmt = new SimpleDateFormat(
				"E, d MMMM yyyy - HH:mm:ss zzz");
		String tmStr = fmt.format(bootTime);

		LPCMapping otherInfo = new LPCMapping();
		LPCString url = new LPCString(this.i3.getConfig().getString("url"));

		otherInfo.set(new LPCString("upsince"), new LPCString(tmStr));
		otherInfo.set(new LPCString("connection url"), url);
		otherInfo.set(new LPCString("architecture"), new LPCString("Java"));

		payload.add(otherInfo);
		Utils.debug("I3Startup packet: " + payload.toMudMode());
		Intermud3.network.sendToRouter("startup-req-3", null, payload);
	}
}

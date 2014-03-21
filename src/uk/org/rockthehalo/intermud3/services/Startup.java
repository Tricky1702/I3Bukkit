package uk.org.rockthehalo.intermud3.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Network;
import uk.org.rockthehalo.intermud3.LPC.CallOut;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.LPC.LPCVar;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketBase;

public class Startup extends ServiceTemplate {
	private final Intermud3 i3;
	private final CallOut callout;
	private final Network network;

	public Startup() {
		this.i3 = Intermud3.instance;
		this.callout = CallOut.instance;
		this.network = Network.instance;

		super.setServiceName("startup");
		Services.addService(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.rockthehalo.intermud3.services.ServiceTemplate#create()
	 */
	@Override
	public void create() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.rockthehalo.intermud3.services.ServiceTemplate#remove()
	 */
	@Override
	public void remove() {
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
			this.i3.logError("We don't like startup-reply packet size. Should be 8 but is "
					+ packet.size());
			this.i3.logError(packet.toMudMode());

			return;
		}

		int oMud = PacketBase.O_MUD.getIndex();
		String oMudName = packet.getLPCString(oMud).toString();

		if (!oMudName.equals(this.network.getRouterName().toString())) {
			this.i3.logError("Illegal access. Not from the router.");
			this.i3.logError(packet.toMudMode());

			return;
		}

		LPCArray routerList = new LPCArray();

		if (packet.getLPCArray(6) != null)
			routerList.setLPCData(packet.getLPCArray(6));

		if (routerList.size() < 1) {
			this.i3.logError("We don't like the absence of packet element 6.");
			this.i3.logError(packet.toMudMode());

			return;
		}

		this.network.setRouterList(routerList);

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
		this.network.setRouterName(preferredName);
		this.network.setRouterIP(preferredIP);
		this.network.setRouterPort(preferredPort);

		LPCArray currentRouter = new LPCArray(this.network.getPreferredRouter());
		LPCString currentName = currentRouter.getLPCString(0);
		LPCString currentAddr = currentRouter.getLPCString(1);

		if (preferredName.toString().equals(currentName.toString())
				&& preferredAddr.toString().equals(currentAddr.toString())) {
			LPCInt i = packet.getLPCInt(7);

			if (i != null) {
				this.i3.getConfig().set("router.password", i.toInt());
				this.network.setRouterPassword(i);
			}
		} else {
			this.i3.logInfo("Current router details are "
					+ LPCVar.toMudMode(currentRouter));
			this.i3.logInfo("Changing router details to "
					+ LPCVar.toMudMode(preferredRouter));

			this.i3.getConfig().set("router.password", 0);
			this.i3.getConfig().set("router.chanlistID", 0);
			this.i3.getConfig().set("router.mudlistID", 0);
			this.i3.saveConfig();
			this.network.setPreferredRouter(preferredRouter);
			this.network.shutdown(0);
			this.callout.callOut(network, "connect", 1);

			return;
		}

		this.i3.saveConfig();

		this.i3.logInfo("Connection established to I3 router " + preferredName
				+ " at " + preferredAddr);

		Object service = Services.getService("channel");

		if (service != null)
			this.callout.callOut(service, "requestChanList", 5);
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
		if (this.network.isRouterConnected())
			return;

		Packet payload = new Packet();

		payload.add(this.network.getRouterPassword());
		payload.add(this.network.getMudlistID());
		payload.add(this.network.getChanlistID());
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
		payload.add(this.network.getAdminEmail());
		payload.add(Services.getRouterServices());

		Calendar cal = Calendar.getInstance();
		String ord = "th";

		switch (cal.get(Calendar.DAY_OF_MONTH)) {
		case 1:
		case 21:
		case 31:
			ord = "st";

			break;
		case 2:
		case 22:
			ord = "nd";

			break;
		case 3:
		case 23:
			ord = "rd";

			break;
		default:
			ord = "th";
		}

		Date bootTime = new Date(this.i3.getBootTime());
		SimpleDateFormat fmt = new SimpleDateFormat(
				"E, d'$ord$' MMMM yyyy - HH:mm:ss zzz");
		String tmStr = fmt.format(bootTime);

		tmStr = tmStr.replace("$ord$", ord);

		LPCMapping otherInfo = new LPCMapping();

		otherInfo.set(new LPCString("upsince"), new LPCString(tmStr));
		otherInfo.set(new LPCString("architecture"), new LPCString("Java"));

		payload.add(otherInfo);
		this.i3.debug("Startup packet: " + payload.toMudMode());
		this.network.sendToRouter("startup-req-3", null, payload);
	}
}

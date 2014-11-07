package uk.org.rockthehalo.intermud3.services;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;

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
	private static final Payload startupPayload = new Payload(Arrays.asList(
			"ST_ROUTERLIST", "ST_PASSWORD"));

	public I3Startup() {
	}

	public void remove() {
		Intermud3.callout.removeCallOuts(this);
		startupPayload.remove();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.org.rockthehalo.intermud3.services.ServiceTemplate#replyHandler(uk.org
	 * .rockthehalo.intermud3.LPC.Packet)
	 */
	@Override
	public void replyHandler(final Packet packet) {
		if (packet.size() != startupPayload.size()) {
			Log.error("We don't like startup-reply packet size. Should be "
					+ startupPayload.size() + " but is " + packet.size());
			Log.error(packet.toMudMode());

			return;
		}

		final LPCString oMudName = packet.getLPCString(Payload.O_MUD);

		if (!oMudName.equals(Intermud3.network.getRouterName())) {
			Log.error("Illegal access. Not from the router.");
			Log.error(packet.toMudMode());

			return;
		}

		final LPCArray routerList = new LPCArray();

		if (packet.getLPCArray(startupPayload.get("ST_ROUTERLIST")) != null)
			routerList.setLPCData(packet.getLPCArray(startupPayload
					.get("ST_ROUTERLIST")));

		if (routerList.isEmpty()) {
			Log.error("We don't like the absence of packet element 6.");
			Log.error(packet.toMudMode());

			return;
		}

		Intermud3.network.setRouterList(routerList);

		final List<String> configRouterList = new ArrayList<String>(
				routerList.size());

		for (final Object obj : routerList) {
			LPCArray router = (LPCArray) obj;

			configRouterList.add(router.getLPCString(0) + ", "
					+ router.getLPCString(1));
		}

		final FileConfiguration i3Root = Intermud3.config.getConfig();

		i3Root.set("router.list", configRouterList);

		final LPCArray preferredRouter = new LPCArray(routerList.getLPCArray(0));
		final LPCString preferredName = preferredRouter.getLPCString(0);
		final LPCString preferredAddr = preferredRouter.getLPCString(1);
		final String[] router = StringUtils
				.split(preferredAddr.toString(), " ");
		final LPCString preferredIP = new LPCString(router[0].trim());
		final LPCInt preferredPort = new LPCInt(Integer.parseInt(router[1]
				.trim()));

		i3Root.set("router.preferred", preferredName + ", " + preferredAddr);
		Intermud3.network.setRouterName(preferredName);
		Intermud3.network.setRouterIP(preferredIP);
		Intermud3.network.setRouterPort(preferredPort);

		final LPCArray currentRouter = new LPCArray(
				Intermud3.network.getPreferredRouter());
		final LPCString currentName = currentRouter.getLPCString(0);
		final LPCString currentAddr = currentRouter.getLPCString(1);

		if (preferredName.equals(currentName)
				&& preferredAddr.equals(currentAddr)) {
			final LPCInt i = packet
					.getLPCInt(startupPayload.get("ST_PASSWORD"));

			if (i != null) {
				i3Root.set("router.password", i.toNum());
				Intermud3.network.setRouterPassword(i);
			}
		} else {
			Log.info("Current router details are "
					+ Utils.toMudMode(currentRouter));
			Log.info("Changing router details to "
					+ Utils.toMudMode(preferredRouter));

			i3Root.set("router.password", 0);
			Intermud3.network.saveConfig();

			final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();
			final I3Mudlist i3Mudlist = ServiceType.I3MUDLIST.getService();

			if (i3Channel != null) {
				i3Channel.setChanlistID(0);
				i3Channel.saveConfig();
			}

			if (i3Mudlist != null) {
				i3Mudlist.setMudlistID(0);
				i3Mudlist.saveConfig();
			}

			Intermud3.network.setRouterPassword(0);
			Intermud3.network.setPreferredRouter(preferredRouter);
			Intermud3.network.shutdown(0);
			Intermud3.callout.addCallOut(Intermud3.network, "connect", 2);

			return;
		}

		Intermud3.network.saveConfig();

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
	public void reqHandler(final Packet packet) {
	}

	public void send() {
		if (Intermud3.network.isRouterConnected())
			return;

		final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();
		final I3Mudlist i3Mudlist = ServiceType.I3MUDLIST.getService();
		final Packet payload = new Packet();

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

		final Server server = Intermud3.instance.getServer();

		payload.add(new LPCInt(server.getPort()));
		payload.add(new LPCInt(0));
		payload.add(new LPCInt(0));

		final LPCString mudlibVersion = new LPCString(server.getName() + " "
				+ server.getBukkitVersion());
		final LPCString baseMudlibVersion = new LPCString(server.getName()
				+ " " + server.getBukkitVersion());
		final LPCString driverVersion = new LPCString("RtH " + server.getName()
				+ " Client v"
				+ Intermud3.instance.getDescription().getVersion());
		final LPCString mudType = new LPCString("Other." + server.getName());
		final LPCString openStatus = new LPCString("beta testing");

		payload.add(mudlibVersion);
		payload.add(baseMudlibVersion);
		payload.add(driverVersion);
		payload.add(mudType);
		payload.add(openStatus);
		payload.add(Intermud3.network.getAdminEmail());
		payload.add(ServiceManager.getRouterServices());

		final Date bootTime = new Date(Intermud3.instance.getBootTime());
		final Calendar cal = Calendar.getInstance();
		final String ord;

		cal.setTime(bootTime);

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

		final SimpleDateFormat fmt = new SimpleDateFormat(
				"EEEE, d'$ord$' MMMM yyyy - HH:mm:ss zzz");
		final String tmStr = fmt.format(bootTime);
		final LPCMapping otherInfo = new LPCMapping();

		otherInfo.put(new LPCString("upsince"),
				new LPCString(tmStr.replace("$ord$", ord)));

		if (!Intermud3.network.getHostName().isEmpty())
			otherInfo.put(new LPCString("host"),
					Intermud3.network.getHostName());

		otherInfo.put(
				new LPCString("architecture"),
				new LPCString(System.getProperty("os.name") + "/"
						+ System.getProperty("os.arch")));
		otherInfo.put(new LPCString("java"),
				new LPCString(System.getProperty("java.version")));

		payload.add(otherInfo);
		Intermud3.network.sendToRouter("startup-req-3", null, payload);
	}
}

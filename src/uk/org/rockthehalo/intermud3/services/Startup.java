package uk.org.rockthehalo.intermud3.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.LPCArray;
import uk.org.rockthehalo.intermud3.LPCInt;
import uk.org.rockthehalo.intermud3.LPCMapping;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.Packet.PacketBase;
import uk.org.rockthehalo.intermud3.Packet.PacketTypes;

public class Startup {
	private static Intermud3 i3Instance;

	public Startup() {
		i3Instance = Intermud3.instance;
	}

	public void replyHandler(Packet packet) {
		LPCArray routerArray = new LPCArray();
		LPCArray currentRouter = new LPCArray();
		LPCArray preferredRouter = new LPCArray();
		LPCArray newRouterList = new LPCArray();
		PacketBase oMud = PacketBase.O_MUD;
		Packet preferredRouterPacket = new Packet();
		Packet currentRouterPacket = new Packet();
		String[] server;
		String oMudName = packet.get(oMud.getNum()).toString();
		String preferredName, preferredAddr;
		String currentName, currentAddr;
		String preferredRouterStr, currentRouterStr;

		routerArray.set(i3Instance.getRouterList().getArray(0).get());
		currentRouter.set(routerArray.clone());

		if (!oMudName.equals(routerArray.getString(0).toString())) {
			i3Instance.logError("Illegal access. Not from the router.");
			i3Instance.logError(packet.toMudMode());

			return;
		}

		if (packet.size() != 8) {
			i3Instance
					.logError("We don't like startup-reply packet size. Should be 8 but is "
							+ packet.size());
			i3Instance.logError(packet.toMudMode());

			return;
		}

		newRouterList.set(packet.getArray(6).get());

		if (newRouterList.size() < 1) {
			i3Instance
					.logError("We don't like the absence of packet element 6.");
			i3Instance.logError(packet.toMudMode());

			return;
		}

		i3Instance.setRouterList(newRouterList);
		preferredRouter.set(newRouterList.getArray(0).get());
		preferredRouterPacket.set(preferredRouter.get());
		preferredRouterStr = preferredRouterPacket.toMudMode();

		preferredName = preferredRouter.getString(0).toString();
		preferredAddr = preferredRouter.getString(1).toString();
		currentName = currentRouter.getString(0).toString();
		currentAddr = currentRouter.getString(1).toString();

		if (preferredName.equals(currentName)
				&& preferredAddr.equals(currentAddr)) {
			LPCInt i = packet.getInt(7);

			if (i != null)
				i3Instance.setRouterPassword(i);
		} else {
			currentRouterPacket.set(currentRouter.get());
			currentRouterStr = currentRouterPacket.toMudMode();
			i3Instance
					.logInfo("Current router details are " + currentRouterStr);
			i3Instance.logInfo("Changing router details to "
					+ preferredRouterStr);
			Intermud3.network.shutdown(0);
			Intermud3.network.connect();

			return;
		}

		i3Instance.getConfig().set("server.name", preferredName);
		server = StringUtils.split(preferredAddr, " ");
		i3Instance.getConfig().set("server.ip", server[0]);
		i3Instance.getConfig().set("server.port", Integer.parseInt(server[1]));
		i3Instance.getConfig().set("server.password",
				Integer.parseInt(i3Instance.getRouterPassword().toString()));
		i3Instance.saveConfig();

		i3Instance.logInfo("Connection established to I3 router "
				+ preferredRouterStr);

		PacketTypes.CHANLIST_REQ.send(new Packet());
	}

	/**
	 * @param packet
	 */
	public void send(Packet packet) {
		LPCMapping otherInfo = new LPCMapping();
		Date bootTime;
		Packet payload = new Packet();
		Calendar cal;
		SimpleDateFormat fmt;
		String tmStr, ord;

		payload.add(i3Instance.getRouterPassword());
		payload.add(i3Instance.getMudlistID());
		payload.add(i3Instance.getChanlistID());
		payload.add(0);
		payload.add(0);
		payload.add(0);
		payload.add(i3Instance.getServer().getName() + " "
				+ i3Instance.getServer().getBukkitVersion());
		payload.add(i3Instance.getServer().getName() + " "
				+ i3Instance.getServer().getBukkitVersion());
		payload.add("RtH CraftBukkit Client v" + i3Instance.getPluginVersion());
		payload.add("Other.Bukkit");
		payload.add("beta testing");
		payload.add(i3Instance.getAdminEmail());
		payload.add(Intermud3.services.getServices());

		bootTime = new Date(i3Instance.getBootTime());
		fmt = new SimpleDateFormat("E, d'$ord$' MMMM yyyy - HH:mm:ss zzz");
		cal = Calendar.getInstance();

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

		tmStr = fmt.format(bootTime);
		tmStr = tmStr.replace("$ord$", ord);
		otherInfo.set("upsince", tmStr);
		otherInfo.set("architecture", "Java");

		payload.add(otherInfo.get());
		i3Instance.logInfo("Startup payload: " + payload.toMudMode());
		packet = new Packet();
		packet.add(payload.get());
		Intermud3.network.sendToRouter(PacketTypes.STARTUP_REQ, null, packet);
	}
}

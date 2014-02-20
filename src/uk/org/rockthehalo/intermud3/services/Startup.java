package uk.org.rockthehalo.intermud3.services;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.LPCData;
import uk.org.rockthehalo.intermud3.LPCData.LPCTypes;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.Packet.PacketBase;
import uk.org.rockthehalo.intermud3.Packet.PacketTypes;

public class Startup {
	private static Intermud3 i3Instance;

	public Startup() {
		i3Instance = Intermud3.instance;
	}

	public void replyHandler(Packet packet) {
		try {
			LPCData routerList = new LPCData(LPCTypes.MIXEDARR);
			LPCData routerArray = new LPCData(LPCTypes.STRINGARR);
			LPCData currentRouter = new LPCData(LPCTypes.STRING);
			LPCData preferredRouter = new LPCData(LPCTypes.STRINGARR);
			LPCData newRouterList = new LPCData(LPCTypes.MIXEDARR);
			PacketBase oMud = PacketBase.O_MUD;
			String[] server;
			String oMudName = packet.get(oMud.getNum()).toString();

			routerList = new LPCData(i3Instance.getRouterList().get());
			routerArray.set(routerList.get(0));
			currentRouter.set(routerArray.get());

			if (!oMudName.equals(routerArray.get(0))) {
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

			newRouterList.set(packet.get(6));

			if (newRouterList.size() < 1) {
				i3Instance
						.logError("We don't like the absence of packet element 6.");
				i3Instance.logError(packet.toMudMode());

				return;
			}

			i3Instance.setRouterList(newRouterList);
			preferredRouter.set(newRouterList.get(0));

			if (preferredRouter.toMudMode().equals(currentRouter.toMudMode())) {
				i3Instance.setRouterPassword(new LPCData(packet.get(7))
						.getInt());
			} else {
				i3Instance.logInfo("Changing router details to "
						+ preferredRouter.toMudMode());
				Intermud3.network.shutdown(0);
				Intermud3.network.connect();

				return;
			}

			i3Instance.getConfig().set("server.name",
					(String) preferredRouter.get(0));
			server = StringUtils.split(preferredRouter.get(1).toString(), " ");
			i3Instance.getConfig().set("server.ip", (String) server[0]);
			i3Instance.getConfig().set("server.port",
					Integer.parseInt(server[1]));
			i3Instance.getConfig().set("server.password",
					i3Instance.getRouterPassword());
			i3Instance.saveConfig();

			i3Instance.logInfo("Connection established to I3 router "
					+ preferredRouter.toMudMode());

			PacketTypes.CHANLIST_REQ.send(new Packet());
		} catch (I3Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param packet
	 */
	public void send(Packet packet) {
		try {
			Hashtable<String, Object> otherInfo = new Hashtable<String, Object>();
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
			payload.add("RtH CraftBukkit Client v"
					+ i3Instance.getPluginVersion());
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
			otherInfo.put("upsince", tmStr);
			otherInfo.put("architecture", "Java");

			payload.add(otherInfo);

			i3Instance.logInfo("Startup payload: " + payload.toMudMode());
			Intermud3.network.sendToRouter(PacketTypes.STARTUP_REQ, null,
					payload);
		} catch (I3Exception e) {
			e.printStackTrace();
		}
	}
}

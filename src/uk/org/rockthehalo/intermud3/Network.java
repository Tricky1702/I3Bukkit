package uk.org.rockthehalo.intermud3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.LPCData.LPCTypes;
import uk.org.rockthehalo.intermud3.Packet.PacketTypes;
import uk.org.rockthehalo.intermud3.services.Services;

public class Network implements Runnable {
	private static Intermud3 i3Instance;
	private volatile Thread inputThread;
	private Socket sock;
	private DataOutputStream sockOut;
	private DataInputStream sockIn;
	private boolean connected;
	private boolean shutdown;

	public Network() {
		LPCData routerList = new LPCData(LPCTypes.MIXEDARR);
		LPCData routerArray = new LPCData(LPCTypes.STRINGARR);

		i3Instance = Intermud3.instance;

		try {
			routerArray.add(i3Instance.getServerName());
			routerArray.add(i3Instance.getServerIP() + " "
					+ Integer.toString(i3Instance.getServerPort()));
			routerList.add(routerArray.get());
		} catch (I3Exception e) {
			e.printStackTrace();
		}

		i3Instance.setRouterList(routerList);

		inputThread = null;
		sock = null;
		sockOut = null;
		sockIn = null;
		connected = false;
		shutdown = true;
	}

	/**
	 * @return the isConnected
	 */
	public boolean isConnected() {
		return connected;
	}

	/**
	 * @param flag
	 *            set connected true or false
	 */
	public void setConnected(boolean flag) {
		connected = flag;
	}

	public void connect() {
		LPCData preferredRouter = new LPCData(LPCTypes.STRINGARR);
		String[] server = { "", "" };

		if (connected)
			return;

		try {
			preferredRouter.set(i3Instance.getRouterList().get(0));
			server = StringUtils.split(preferredRouter.get(1).toString(), " ");
		} catch (I3Exception e) {
			e.printStackTrace();
		}

		try {
			sock = new Socket(server[0], Integer.parseInt(server[1]));
			sockOut = new DataOutputStream(sock.getOutputStream());
		} catch (UnknownHostException e) {
			e.printStackTrace();

			return;
		} catch (IOException e) {
			e.printStackTrace();

			return;
		}

		PacketTypes.STARTUP_REQ.send(null);

		connected = true;
		shutdown = false;
		inputThread = new Thread(this);
		inputThread.setName("Intermud3");
		inputThread.start();
		Services.startHeartBeats();
	}

	public void shutdown() {
		shutdown(0);
	}

	public void shutdown(int restartDelay) {
		PacketTypes.SHUTDOWN.send(new Packet(restartDelay));

		Services.stopHeartBeats();
		connected = false;
		shutdown = true;

		try {
			Thread moribund = inputThread;

			inputThread = null;
			moribund.interrupt();

			if (sockIn != null)
				sockIn.close();
			if (sockOut != null)
				sockOut.close();
			if (sock != null)
				sock.close();
		} catch (IOException e) {
			e.printStackTrace();

			return;
		}
	}

	/**
	 * @param packet
	 */
	public void send(Packet packet) {
		if (sockOut != null)
			send(packet.toMudMode());
	}

	/**
	 * @param str
	 */
	public void send(String str) {
		if (sockOut != null) {
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
			} catch (UnsupportedEncodingException uee) {
				String errMsg = uee.getMessage() == null ? uee.toString() : uee
						.getMessage();

				i3Instance.logError("Unsupported encoding: " + str);

				if (errMsg != null)
					i3Instance.logError(errMsg);
			} catch (IOException e) {
				String errMsg = e.getMessage() == null ? e.toString() : e
						.getMessage();

				i3Instance.logError("Problem sending data: " + str);

				if (errMsg != null)
					i3Instance.logError(errMsg);
			}
		}
	}

	public void sendPacket(PacketTypes i3Type, String origUser, String targMud,
			String targUser, Packet payload) {
		try {
			Packet packet = new Packet();

			packet.add(i3Type.getName());
			packet.add(5);
			packet.add(i3Instance.getServer().getServerName());

			if (origUser == null) {
				packet.add(0);
			} else {
				packet.add(origUser.toLowerCase(Locale.ENGLISH));
			}

			if (targMud == null) {
				packet.add(0);
			} else {
				packet.add(targMud);
			}

			if (targUser == null) {
				packet.add(0);
			} else {
				packet.add(targUser.toLowerCase(Locale.ENGLISH));
			}

			packet.add(payload.get());
			send(packet);
		} catch (I3Exception e) {
			e.printStackTrace();
		}
	}

	public void sendToRouter(PacketTypes i3Type, String origUser, Packet packet) {
		sendPacket(i3Type, origUser, i3Instance.getServerName(), null, packet);
	}

	public void sendToMud(PacketTypes i3Type, String origUser, String targMud,
			Packet packet) {
		sendPacket(i3Type, origUser, targMud, null, packet);
	}

	public void sendToUser(PacketTypes i3Type, String origUser, String targMud,
			String targUser, Packet packet) {
		sendPacket(i3Type, origUser, targMud, targUser, packet);
	}

	public void sendToAll(PacketTypes i3Type, String origUser, Packet packet) {
		sendPacket(i3Type, origUser, null, null, packet);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			sock.setSoTimeout(60000);
			sockIn = new DataInputStream(sock.getInputStream());
		} catch (IOException ie) {
			sockIn = null;
			connected = false;
		}

		while (connected && !shutdown) {
			Packet data;
			String str, type = "";

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				if (shutdown) {
					i3Instance.logWarn("Shutdown!!!");

					return;
				}
			}

			try {
				byte[] tmp;
				int len = 0;

				while (!shutdown) {
					try {
						len = sockIn.readInt();

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

						continue;
					}
				}

				if (len > 65536) {
					int skipped = 0;

					try {
						while (skipped < len)
							skipped += sockIn.skipBytes(len);
					} catch (java.io.IOException e) {
					}

					i3Instance.logError("Got illegal packet: " + skipped + "/"
							+ len + " bytes.");

					continue;
				}

				tmp = new byte[len];

				while (!shutdown) {
					try {
						sockIn.readFully(tmp);

						break;
					} catch (java.io.IOException e) {
						if ((e.getMessage() == null)
								|| (e.getMessage().toUpperCase()
										.indexOf("TIMED OUT") < 0))
							throw e;

						try {
							Thread.sleep(1000);
						} catch (InterruptedException ie) {
						}

						i3Instance.logError("Timeout receiving packet sized "
								+ len);

						continue;
					}
				}

				str = new String(tmp);
			} catch (IOException e) {
				String errMsg;

				data = null;
				str = null;
				connected = false;

				try {
					Thread.sleep(1200);
				} catch (InterruptedException ie) {
					if (shutdown) {
						i3Instance.logWarn("Shutdown!!!");

						return;
					}
				}

				connect();

				errMsg = e.getMessage() == null ? e.toString() : e.getMessage();

				if (errMsg != null)
					i3Instance.logError(errMsg);

				return;
			}

			data = new Packet();

			try {
				data.fromMudMode(str);
				type = (String) data.get(0);
			} catch (I3Exception e) {
				e.printStackTrace();
			}

			if (PacketTypes.getType(type) == null) {
				i3Instance.logWarn("Unknown packet " + data.toMudMode());
			} else {
				PacketTypes.getType(type).handler(data);
			}
		}
	}
}

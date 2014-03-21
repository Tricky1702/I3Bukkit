package uk.org.rockthehalo.intermud3.services;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Network;
import uk.org.rockthehalo.intermud3.LPC.LPCString;
import uk.org.rockthehalo.intermud3.LPC.Packet;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketBase;
import uk.org.rockthehalo.intermud3.LPC.Packet.PacketTypes;

public class I3Error extends ServiceTemplate {
	private final Intermud3 i3;
	private final Network network;

	public enum PacketErrorBase {
		ERROR_CODE(6), ERROR_MESSAGE(7), ERROR_PACKET(8);

		private int index;

		private PacketErrorBase(int index) {
			this.index = index;
		}

		public int getIndex() {
			return this.index;
		}
	}

	public enum I3RouterErrorCodes {
		BAD_MOJO("bad-mojo") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = (LPCString) packet
						.get(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					this.i3.logError("Router: Bad Mojo/" + errorMsg);
				else
					this.i3.logError("Router: Bad Mojo");

				this.i3.logError("Reconnecting in 2 minutes.");
				this.network
						.setReconnectWait(this.network.getReconnectWait() - 10);
				this.network.reconnect((2 * 60) + 5);
				this.network.shutdown(2 * 60);
			}
		},
		BAD_PKT("bad-pkt") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = (LPCString) packet
						.get(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					this.i3.logError("Router: Bad Packet/" + errorMsg);
				else
					this.i3.logError("Router: Bad Packet");
			}
		},
		BAD_PROTO("bad-proto") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = (LPCString) packet
						.get(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null
						&& errorMsg.toString()
								.contains("MUD already connected")) {
					this.i3.logError("Router: Bad Proto/" + errorMsg);
					this.i3.logError("Reconnecting in 5 minutes.");
					this.network.setReconnectWait(this.network
							.getReconnectWait() - 10);
					this.network.reconnect((5 * 60) + 5);
					this.network.shutdown(5 * 60);
				}
			}
		},
		NOT_ALLOWED("not-allowed") {
			@Override
			public void handler(Packet packet) {
				if (packet.size() > 8) {
					Packet data = (Packet) packet
							.getLPCArray(PacketErrorBase.ERROR_PACKET
									.getIndex());

					if (data != null
							&& data.get(PacketBase.TYPE.getIndex()).toString()
									.equals("channel-listen")) {
						LPCString channel = data.getLPCString(6);

						if (channel != null) {
							LPCString errorMsg = (LPCString) packet
									.get(PacketErrorBase.ERROR_MESSAGE
											.getIndex());

							if (errorMsg != null)
								this.i3.logError("Router: Not Allowed/"
										+ errorMsg);
							else
								this.i3.logError("Router: Not Allowed");

							I3Channel service = (I3Channel) Services
									.getService("channel");

							if (service != null)
								service.tuneOut(channel.toString());
						}
					}
				}
			}
		},
		NOT_IMP("not-imp") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = (LPCString) packet
						.get(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					this.i3.logError("Router: Not Implemented/" + errorMsg);
				else
					this.i3.logError("Router: Not Implemented");
			}
		},
		UNK_DST("unk-dst") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = (LPCString) packet
						.get(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					this.i3.logError("Router: Unknown Destination/" + errorMsg);
				else
					this.i3.logError("Router: Unknown Destination");
			}
		},
		UNK_SRC("unk-src") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = (LPCString) packet
						.get(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					this.i3.logError("Router: Unknown Source/" + errorMsg);
				else
					this.i3.logError("Router: Unknown Source");
			}
		},
		UNK_TYPE("unk-type") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = (LPCString) packet
						.get(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					this.i3.logError("Router: Unknown Packet Type/" + errorMsg);
				else
					this.i3.logError("Router: Unknown Packet Type");
			}
		};

		protected final Intermud3 i3;
		protected final Network network;
		private static Map<String, I3RouterErrorCodes> nameToErrCode;
		private String errCodeName;

		private I3RouterErrorCodes(String errCodeName) {
			this.i3 = Intermud3.instance;
			this.network = Network.instance;

			this.errCodeName = errCodeName;
		}

		public String getErrCodeName() {
			return this.errCodeName;
		}

		public static I3RouterErrorCodes getNamedErrCode(String errCodeName) {
			if (nameToErrCode == null) {
				initMapping();
			}

			return nameToErrCode.get(errCodeName);
		}

		private static void initMapping() {
			nameToErrCode = new HashMap<String, I3RouterErrorCodes>();

			for (I3RouterErrorCodes type : values())
				nameToErrCode.put(type.errCodeName, type);
		}

		public abstract void handler(Packet packet);
	}

	public enum I3MudErrorCodes {
		BAD_PKT("bad-pkt") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = (LPCString) packet
						.get(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					this.i3.logError("Mud: Bad Packet/" + errorMsg);
				else
					this.i3.logError("Mud: Bad Packet");
			}
		},
		UNK_CHANNEL("unk-channel") {
			@Override
			public void handler(Packet packet) {
				if (packet.size() > 8) {
					Packet data = (Packet) packet
							.getLPCArray(PacketErrorBase.ERROR_PACKET
									.getIndex());

					if (data != null
							&& data.get(PacketBase.TYPE.getIndex()).toString()
									.equals("channel-listen")) {
						LPCString channel = data.getLPCString(6);

						if (channel != null) {
							LPCString errorMsg = (LPCString) packet
									.get(PacketErrorBase.ERROR_MESSAGE
											.getIndex());

							if (errorMsg != null)
								this.i3.logError("Mud: Unknown Channel/"
										+ errorMsg);
							else
								this.i3.logError("Mud: Unknown Channel");

							I3Channel service = (I3Channel) Services
									.getService("channel");

							if (service != null)
								service.tuneOut(channel.toString());
						}
					}
				}
			}
		},
		UNK_TYPE("unk-type") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = (LPCString) packet
						.get(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					this.i3.logError("Mud: Unknown Packet Type/" + errorMsg);
				else
					this.i3.logError("Mud: Unknown Packet Type");
			}
		},
		UNK_USER("unk-user") {
			@Override
			public void handler(Packet packet) {
				LPCString errorMsg = (LPCString) packet
						.get(PacketErrorBase.ERROR_MESSAGE.getIndex());

				if (errorMsg != null)
					this.i3.logError("Mud: Unknown Target User/" + errorMsg);
				else
					this.i3.logError("Mud: Unknown Target User");
			}
		};

		protected final Intermud3 i3;
		private static Map<String, I3MudErrorCodes> nameToErrCode;
		private String errCodeName;

		private I3MudErrorCodes(String errCodeName) {
			this.i3 = Intermud3.instance;

			this.errCodeName = errCodeName;
		}

		public String getErrCodeName() {
			return this.errCodeName;
		}

		public static I3MudErrorCodes getNamedErrCode(String errCodeName) {
			if (nameToErrCode == null) {
				initMapping();
			}

			return nameToErrCode.get(errCodeName);
		}

		private static void initMapping() {
			nameToErrCode = new HashMap<String, I3MudErrorCodes>();

			for (I3MudErrorCodes type : values())
				nameToErrCode.put(type.errCodeName, type);
		}

		public abstract void handler(Packet packet);
	}

	public I3Error() {
		this.i3 = Intermud3.instance;
		this.network = Network.instance;

		super.setServiceName("error");
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
		LPCString originator = packet.getLPCString(PacketBase.O_MUD.getIndex());
		LPCString errorCode = packet.getLPCString(PacketErrorBase.ERROR_CODE
				.getIndex());

		if (originator.toString().equals(
				this.network.getRouterName().toString())) {
			I3RouterErrorCodes routerErrCode = I3RouterErrorCodes
					.getNamedErrCode(errorCode.toString());

			if (routerErrCode != null) {
				routerErrCode.handler(packet);

				return;
			}
		} else {
			I3MudErrorCodes mudErrCode = I3MudErrorCodes
					.getNamedErrCode(errorCode.toString());

			if (mudErrCode != null) {
				mudErrCode.handler(packet);

				return;
			}
		}

		LPCString errorMsg = packet.getLPCString(PacketErrorBase.ERROR_MESSAGE
				.getIndex());

		this.i3.logError("Unhandled Error: " + originator + "/" + errorCode
				+ "/" + errorMsg);
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

	public void send(Packet packet) {
		Packet payload;
		String oUser, tMud, tUser, errCode, errMsg;

		if (packet.getLPCInt(0) != null)
			oUser = null;
		else if (packet.getPlayer(0) != null)
			oUser = packet.getPlayer(0).getName().toLowerCase(Locale.ENGLISH);
		else if (packet.getLPCString(0) != null)
			oUser = packet.getLPCString(0).toString()
					.toLowerCase(Locale.ENGLISH);
		else
			oUser = null;

		if (packet.getLPCInt(2) != null)
			tUser = null;
		else if (packet.getPlayer(2) != null)
			tUser = packet.getPlayer(2).getName().toLowerCase(Locale.ENGLISH);
		else if (packet.getLPCString(2) != null)
			tUser = packet.getLPCString(2).toString()
					.toLowerCase(Locale.ENGLISH);
		else
			tUser = null;

		tMud = packet.get(1).toString();
		errCode = packet.get(3).toString();
		errMsg = packet.get(4).toString();

		payload = new Packet();
		payload.add(errCode);
		payload.add(errMsg);

		if (packet.size() != 6 || packet.getLPCArray(5) == null)
			payload.add(0);
		else
			for (Object obj : packet.subList(5, packet.size()))
				payload.add(obj);

		this.network.sendToUser(PacketTypes.ERROR, oUser, tMud, tUser, payload);
	}
}

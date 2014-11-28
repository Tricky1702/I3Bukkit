package uk.org.rockthehalo.intermud3;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import uk.org.rockthehalo.intermud3.PacketTypes.PacketType;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public class I3Client implements Runnable {
	private class ChangeRequest {
		final public static int REGISTER = 1;
		final public static int CHANGEOPS = 2;

		final public SocketChannel socket;
		final public int type;
		final public int ops;

		public ChangeRequest(final SocketChannel socket, final int type, final int ops) {
			this.socket = socket;
			this.type = type;
			this.ops = ops;
		}
	}

	// Is the thread running?
	private boolean running = false;

	// The host:port combination to connect to
	private InetAddress hostAddress = null;
	private int port = -1;

	// The selector we'll be monitoring
	private Selector selector = null;

	// The socket channel
	private SocketChannel socket = null;

	// A list of change requests
	private List<ChangeRequest> pendingChanges = Collections.synchronizedList(new LinkedList<ChangeRequest>());

	// A map of socket channels to a list of ByteBuffer instances
	private Map<SocketChannel, List<ByteBuffer>> pendingData = Collections
			.synchronizedMap(new HashMap<SocketChannel, List<ByteBuffer>>());

	public I3Client(final InetAddress hostAddress, final int port) throws IOException {
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = initSelector();
		this.socket = initiateConnection();
	}

	public boolean isConnected() {
		return this.socket.isConnected();
	}

	public void remove() {
		if (this.running) {
			this.selector.wakeup();
			this.running = false;
		}

		Log.warn("Closing.");

		try {
			this.socket.close();
			this.selector.close();
		} catch (IOException ioE) {
			ioE.printStackTrace();
		}

		// Clear out all lists.
		this.pendingChanges.clear();
		this.pendingData.clear();

		// Remove references.
		this.hostAddress = null;
		this.pendingChanges = null;
		this.pendingData = null;
		this.selector = null;
		this.socket = null;
	}

	public void send(final byte[] data) throws IOException {
		// And queue the data we want written
		List<ByteBuffer> queue = this.pendingData.get(this.socket);

		if (queue == null) {
			queue = new ArrayList<ByteBuffer>();
			this.pendingData.put(this.socket, queue);
		}

		final byte[] header = ByteBuffer.allocate(4).putInt(data.length).array();

		queue.add(ByteBuffer.wrap(header));
		queue.add(ByteBuffer.wrap(data));

		this.pendingChanges.add(new ChangeRequest(this.socket, ChangeRequest.CHANGEOPS, SelectionKey.OP_READ
				| SelectionKey.OP_WRITE));

		// Finally, wake up our selecting thread so it can make the required
		// changes
		this.selector.wakeup();
	}

	@Override
	public void run() {
		this.running = true;

		while (this.running) {
			if (this.pendingChanges != null) {
				final Iterator<ChangeRequest> changes = this.pendingChanges.iterator();

				while (this.running && changes.hasNext()) {
					final ChangeRequest change = changes.next();

					switch (change.type) {
					case ChangeRequest.CHANGEOPS: {
						final SelectionKey key = change.socket.keyFor(this.selector);

						if (key == null || !key.isValid()) {
							this.running = false;

							break;
						}

						key.interestOps(change.ops);

						break;
					}
					case ChangeRequest.REGISTER: {
						try {
							change.socket.register(this.selector, change.ops);
						} catch (ClosedChannelException ccE) {
							this.running = false;
							Log.warn("Channel closed.");

							break;
						}

						break;
					}
					}
				}

				this.pendingChanges.clear();
			}

			try {
				// Wait for an event on one of the registered channels
				while (this.selector != null && this.selector.selectNow() > 0) {
					final Iterator<SelectionKey> selectedKeys;

					// Iterate over the set of keys for which events are
					// available
					try {
						selectedKeys = this.selector.selectedKeys().iterator();
					} catch (ClosedSelectorException csE) {
						this.running = false;
						Log.warn("Selector closed.");

						continue;
					}

					while (selectedKeys.hasNext()) {
						final SelectionKey key = selectedKeys.next();

						selectedKeys.remove();

						// Check what event is available and deal with it
						if (key != null && key.isValid()) {
							if (key.isConnectable())
								finishConnection(key);
							if (key.isValid() && key.isReadable())
								read(key);
							if (key.isValid() && key.isWritable())
								write(key);
						}

						try {
							Thread.sleep(50);
						} catch (InterruptedException iE) {
						}
					}
				}
			} catch (IOException ioE) {
				this.running = false;
				ioE.printStackTrace();
			} catch (NullPointerException npE) {
				npE.printStackTrace();
			}

			try {
				Thread.sleep(100);
			} catch (InterruptedException iE) {
			}
		}

		Log.warn("Shutdown.");
	}

	private ByteBuffer readBuf(final SelectionKey key, final int length) throws IOException {
		if (!key.isValid() || !key.isReadable() || length <= 0)
			return null;

		final SocketChannel socketChannel = (SocketChannel) key.channel();

		// The buffer into which we'll read data when it's available
		final ByteBuffer readBuffer = ByteBuffer.allocate(length);
		// Attempt to read off the channel
		int nBytes = 0;

		try {
			while (nBytes < length) {
				final int n = socketChannel.read(readBuffer);

				if (n == -1) {
					Log.warn("Connction closed.");
					// Remote entity shut the socket down cleanly. Do the
					// same from our end and cancel the channel.
					socketChannel.close();
					key.cancel();
					this.running = false;

					return null;
				}

				nBytes += n;
			}
		} catch (IOException ioE) {
			Log.warn("Connection reset by peer.");
			// The remote forcibly closed the connection, cancel
			// the selection key and close the channel.
			key.cancel();
			socketChannel.close();
			this.running = false;

			return null;
		}

		readBuffer.flip();

		return readBuffer;
	}

	private void read(final SelectionKey key) throws IOException {
		ByteBuffer buf = readBuf(key, 4);

		if (buf == null)
			return;

		final int header = buf.getInt();
		buf = readBuf(key, header);

		if (buf == null)
			return;

		final byte[] data = new byte[header];
		buf.get(data, 0, header);

		handleData(new String(data));
	}

	public void handleData(final String data) {
		Intermud3.network.setRouterConnected(true);
		Intermud3.network.setIdleTimeout(System.currentTimeMillis());

		final Packet packet = new Packet();
		final LPCString omud, tmud;

		PacketType type = null;
		LPCString namedType = null, ouser = null, tuser = null;
		String err = null;

		packet.fromMudMode(data);

		if (packet.isEmpty())
			err = "Packet is empty";
		else if (packet.size() <= Payload.HEADERSIZE)
			err = "Packet size too small";
		else {
			namedType = packet.getLPCString(Payload.TYPE);

			if (namedType == null)
				err = "SERVICE is not a string";

			// Sanity check on the service type
			namedType = new LPCString(namedType.toLowerCase());
			packet.set(Payload.TYPE, namedType);

			omud = packet.getLPCString(Payload.O_MUD);

			if (omud == null)
				err += "Originating mud not a string";

			tmud = packet.getLPCString(Payload.T_MUD);

			if (tmud != null && !tmud.equalsIgnoreCase(Utils.getServerName())) {
				if (namedType.equals("mudlist")) {
					Log.warn("Wrong destination (" + tmud + ") for mudlist packet.");
					packet.set(Payload.T_MUD, new LPCString(Utils.getServerName()));
				} else {
					err += "Wrong destination mud (" + tmud + ")";
				}
			}
		}

		if (err != null) {
			Log.error(err + ".");
			Log.error(packet.toMudMode());

			return;
		}

		ouser = packet.getLPCString(Payload.O_USER);

		// Sanity check on the originator username
		if (ouser != null)
			packet.set(Payload.O_USER, new LPCString(ouser.toLowerCase()));

		tuser = packet.getLPCString(Payload.T_USER);

		// Sanity check on the target username
		if (tuser != null)
			packet.set(Payload.T_USER, new LPCString(tuser.toLowerCase()));

		type = PacketType.getNamedType(namedType.toString());

		if (type == null)
			Log.warn("Service handler for I3 packet " + packet.toMudMode() + " not available.");
		else
			type.handler(packet);
	}

	private void write(final SelectionKey key) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) key.channel();
		final List<ByteBuffer> queue = this.pendingData.get(socketChannel);

		if (queue == null || queue.isEmpty())
			return;

		// Write until there's no more data ...
		while (!queue.isEmpty()) {
			final ByteBuffer buf = queue.get(0);

			while (true) {
				if (socketChannel == null || !socketChannel.isConnected())
					return;

				int nBytes = 0;

				try {
					nBytes = socketChannel.write(buf);
				} catch (Exception e) {
					break;
				}

				if (nBytes == 0 || !buf.hasRemaining())
					break;
			}

			queue.remove(0);

			// Avoid flooding the router.
			if (!queue.isEmpty())
				try {
					Thread.sleep(50);
				} catch (InterruptedException ex) {
				}
		}

		// Register an interest in reading on this channel
		if (key.isValid())
			key.interestOps(SelectionKey.OP_READ);
	}

	private void finishConnection(final SelectionKey key) throws IOException {
		final SocketChannel socketChannel = (SocketChannel) key.channel();

		// Finish the connection. If the connection operation failed
		// this will raise an IOException.
		try {
			if (socketChannel.isConnectionPending())
				while (true)
					if (socketChannel.finishConnect())
						break;
		} catch (IOException ioE) {
			ioE.printStackTrace();

			// Cancel the channel's registration with our selector
			key.cancel();
			this.running = false;

			return;
		}

		// Register an interest in reading on this channel
		key.interestOps(SelectionKey.OP_READ);
		// synchronized (this.pendingChanges) {
		// this.pendingChanges.add(new ChangeRequest(socketChannel,
		// ChangeRequest.CHANGEOPS, SelectionKey.OP_READ));
		// }
	}

	private SocketChannel initiateConnection() throws IOException {
		// Create a non-blocking socket channel
		final SocketChannel socketChannel = SocketChannel.open();

		socketChannel.configureBlocking(false);

		// Kick off connection establishment
		socketChannel.connect(new InetSocketAddress(this.hostAddress, this.port));

		this.pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.REGISTER, SelectionKey.OP_CONNECT
				| SelectionKey.OP_READ | SelectionKey.OP_WRITE));

		return socketChannel;
	}

	private Selector initSelector() throws IOException {
		// Create a new selector
		return SelectorProvider.provider().openSelector();
	}
}

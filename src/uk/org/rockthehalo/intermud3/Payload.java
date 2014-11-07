package uk.org.rockthehalo.intermud3;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Payload {
	private List<String> data = new ArrayList<String>();
	// Packet header.
	public static final int TYPE = 0;
	public static final int TTL = 1;
	public static final int O_MUD = 2;
	public static final int O_USER = 3;
	public static final int T_MUD = 4;
	public static final int T_USER = 5;

	// Size of the packet header.
	public static final int HEADERSIZE = 6;

	public Payload(final List<String> list) {
		this.data.addAll(list);
	}

	public void debugInfo() {
		Log.debug("Payload: " + StringUtils.join(data, ", "));
		Log.debug("* HEADERSIZE: " + HEADERSIZE);
		Log.debug("* data size:  " + data.size());
	}

	public int get(final String name) {
		if (!data.contains(name))
			return -1;

		return HEADERSIZE + data.indexOf(name);
	}

	public void remove() {
		// Clear out the list.
		this.data.clear();

		// Remove reference.
		this.data = null;
	}

	public int size() {
		return HEADERSIZE + data.size();
	}
}

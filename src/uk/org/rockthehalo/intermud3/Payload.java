package uk.org.rockthehalo.intermud3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class Payload {
	// Initialise with packet header.
	private List<String> data = new ArrayList<String>(Arrays.asList("TYPE",
			"TTL", "O_MUD", "O_USER", "T_MUD", "T_USER"));
	// Size of the packet header.
	public static final int HEADERSIZE = 6;

	// Quick reference.
	public static final int TYPE = 0;
	public static final int TTL = 1;
	public static final int O_MUD = 2;
	public static final int O_USER = 3;
	public static final int T_MUD = 4;
	public static final int T_USER = 5;

	public Payload(List<String> list) {
		this.data.addAll(list);
	}

	public void debugInfo() {
		Log.debug("Payload: " + StringUtils.join(this.data, ", "));
		Log.debug("* HEADERSIZE: " + HEADERSIZE);
		Log.debug("* data size:  " + this.data.size());
	}

	public int get(String name) {
		return this.data.indexOf(name);
	}

	public void remove() {
		// Clear out the list.
		this.data.clear();

		// Remove reference.
		this.data = null;
	}

	public int size() {
		return this.data.size();
	}
}

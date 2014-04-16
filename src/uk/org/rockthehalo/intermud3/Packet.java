package uk.org.rockthehalo.intermud3;

import uk.org.rockthehalo.intermud3.LPC.LPCArray;

public class Packet extends LPCArray {
	public Packet() {
	}

	/**
	 * Convert LPCArray to a format suitable for transmitting as MudMode.
	 * 
	 * @return the MudMode string
	 */
	public String toMudMode() {
		return Utils.toMudMode(this);
	}

	/**
	 * Converts a MudMode string into a LPCArray element.
	 * 
	 * @param mudModeString
	 *            the MudMode string to convert
	 */
	public void fromMudMode(String mudModeString) {
		Object obj = Utils.toObject(mudModeString);

		if (obj == null || !Utils.isLPCArray(obj)) {
			this.setLPCData(new LPCArray());
		} else {
			this.setLPCData((LPCArray) obj);
		}
	}
}

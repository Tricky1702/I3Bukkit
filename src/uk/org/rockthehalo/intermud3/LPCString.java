package uk.org.rockthehalo.intermud3;

import org.bukkit.entity.Player;

public class LPCString extends LPCMixed {
	private String lpcData;

	public LPCString() {
		this.lpcData = new String();
		super.setType(LPCTypes.STRING);
	}

	public LPCString(String lpcData) {
		this.lpcData = lpcData;
		super.setType(LPCTypes.STRING);
	}

	public int size() {
		return lpcData.length();
	}

	public boolean isEmpty() {
		return lpcData.isEmpty();
	}

	@Override
	public Object clone() {
		return new LPCString(lpcData);
	}

	public Object get() {
		return lpcData;
	}

	public Object get(Object index) throws I3Exception {
		int i = (Integer) index;

		if (i < 0 || i >= lpcData.length())
			throw new I3Exception(
					"Index out of range for get(index) in LPCString: " + index);

		return lpcData.charAt(i);
	}

	@Override
	public LPCString getString(Object index) throws I3Exception {
		LPCString s = new LPCString();
		int ind = (Integer) index;

		if (ind < 0 || ind >= lpcData.length())
			throw new I3Exception(
					"Index out of range for getString(index) in LPCString: "
							+ index);

		try {
			s.set(String.valueOf(lpcData.charAt(ind)));
		} catch (ClassCastException e) {
			s = null;
		}

		return s;
	}

	@Override
	public LPCInt getInt(Object index) throws I3Exception {
		LPCInt i = new LPCInt();
		int ind = (Integer) index;

		if (ind < 0 || ind >= lpcData.length())
			throw new I3Exception(
					"Index out of range for getInt(index) in LPCString: "
							+ index);

		try {
			i.set(Integer.parseInt(String.valueOf(lpcData.charAt(ind))));
		} catch (ClassCastException e) {
			i = null;
		}

		return i;
	}

	@Override
	public LPCArray getArray(Object index) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCString: getArray(index)");
	}

	@Override
	public LPCMapping getMapping(Object index) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCString: getMapping(index)");
	}

	@Override
	public Player getPlayer(Object index) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCString: getPlayer(index)");
	}

	public void set(Object lpcData) {
		this.lpcData = (String) lpcData;
	}

	public Object set(Object index, Object lpcData) throws I3Exception {
		char[] chars;
		char oldChar;
		int i = (Integer) index;

		if (i < 0 || i >= this.lpcData.length())
			throw new I3Exception(
					"Index out of range for set(index, lpcdata) in LPCString: "
							+ index);

		chars = this.lpcData.toCharArray();
		oldChar = chars[i];
		chars[i] = lpcData.toString().charAt(0);
		this.lpcData = String.copyValueOf(chars);

		return oldChar;
	}

	public boolean add(Object lpcData) {
		this.lpcData.concat(lpcData.toString());

		return true;
	}

	@Override
	public String toString() {
		return lpcData.toString();
	}
}

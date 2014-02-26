package uk.org.rockthehalo.intermud3;

import org.bukkit.entity.Player;

public class LPCInt extends LPCMixed {
	private Integer lpcData;

	public LPCInt() {
		this.lpcData = new Integer(0);
		super.setType(LPCTypes.INT);
	}

	public LPCInt(Integer lpcData) {
		this.lpcData = lpcData;
		super.setType(LPCTypes.INT);
	}

	public int size() {
		return 1;
	}

	public boolean isEmpty() {
		return false;
	}

	@Override
	public Object clone() {
		return new LPCInt(lpcData);
	}

	public Object get() {
		return lpcData;
	}

	public Object get(Object index) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: get(index)");
	}

	@Override
	public LPCString getString(Object index) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: getString(index)");
	}

	@Override
	public LPCInt getInt(Object index) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: getInt(index)");
	}

	@Override
	public LPCArray getArray(Object index) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: getArray(index)");
	}

	@Override
	public LPCMapping getMapping(Object index) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: getMapping(index)");
	}

	@Override
	public Player getPlayer(Object index) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: getPlayer(index)");
	}

	public void set(Object lpcData) {
		this.lpcData = (Integer) lpcData;
	}

	public Object set(Object index, Object lpcData) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCInt: set(index, lpcData)");
	}

	public boolean add(Object lpcData) {
		this.lpcData = Integer.parseInt(this.lpcData.toString())
				+ Integer.parseInt(lpcData.toString());

		return true;
	}

	@Override
	public String toString() {
		return lpcData.toString();
	}
}

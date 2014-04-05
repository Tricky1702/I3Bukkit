package uk.org.rockthehalo.intermud3.LPC;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Utils;

public abstract class LPCVar {
	public enum LPCTypes {
		ARRAY, INT, MAPPING, MIXED, STRING;
	}

	private LPCTypes lpcType = LPCTypes.MIXED;

	public LPCTypes getType() {
		return this.lpcType;
	}

	public static LPCTypes getType(Object lpcData) {
		if (Utils.isLPCArray(lpcData))
			return LPCTypes.ARRAY;

		if (Utils.isLPCInt(lpcData))
			return LPCTypes.INT;

		if (Utils.isLPCMapping(lpcData))
			return LPCTypes.MAPPING;

		if (Utils.isLPCString(lpcData))
			return LPCTypes.STRING;

		return LPCTypes.MIXED;
	}

	public void setType(LPCTypes lpcType) {
		this.lpcType = lpcType;
	}

	public abstract boolean add(Object lpcData) throws I3Exception;

	public abstract Object get(Object index) throws I3Exception;

	public abstract LPCArray getLPCArray(Object index) throws I3Exception;

	public abstract Object getLPCData();

	public abstract LPCInt getLPCInt(Object index) throws I3Exception;

	public abstract LPCMapping getLPCMapping(Object index) throws I3Exception;

	public abstract LPCString getLPCString(Object index) throws I3Exception;

	public abstract boolean isEmpty();

	public abstract Object set(Object index, Object lpcData) throws I3Exception;

	public abstract void setLPCData(LPCArray obj) throws I3Exception;

	public abstract void setLPCData(LPCInt obj) throws I3Exception;

	public abstract void setLPCData(LPCMapping obj) throws I3Exception;

	public abstract void setLPCData(LPCString obj) throws I3Exception;

	public abstract void setLPCData(Object obj) throws I3Exception;

	public abstract int size();

	@Override
	public abstract String toString();
}

package uk.org.rockthehalo.intermud3.LPC;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Utils;

public abstract class LPCVar implements Cloneable {
	public enum LPCTypes {
		ARRAY, INT, MAPPING, MIXED, STRING, INVALID;
	}

	private LPCTypes lpcType = LPCTypes.MIXED;

	public LPCTypes getType() {
		return lpcType;
	}

	public static LPCTypes getType(final Object o) {
		if (Utils.isLPCArray(o))
			return LPCTypes.ARRAY;
		if (Utils.isLPCInt(o))
			return LPCTypes.INT;
		if (Utils.isLPCMapping(o))
			return LPCTypes.MAPPING;
		if (Utils.isLPCMixed(o))
			return LPCTypes.MIXED;
		if (Utils.isLPCString(o))
			return LPCTypes.STRING;

		return LPCTypes.INVALID;
	}

	public void setType(final LPCTypes lpcType) {
		this.lpcType = lpcType;
	}

	public abstract boolean add(final Object o) throws I3Exception;

	@Override
	public abstract boolean equals(final Object o);

	public abstract Object get(final Object index) throws I3Exception;

	public abstract LPCArray getLPCArray(final Object index) throws I3Exception;

	public abstract Object getLPCData();

	public abstract LPCInt getLPCInt(final Object index) throws I3Exception;

	public abstract LPCMapping getLPCMapping(final Object index) throws I3Exception;

	public abstract LPCString getLPCString(final Object index) throws I3Exception;

	@Override
	public abstract int hashCode();

	public abstract boolean isEmpty();

	public abstract Object set(final Object index, final Object element) throws I3Exception;

	public abstract void setLPCData(final LPCArray o) throws I3Exception;

	public abstract void setLPCData(final LPCInt o) throws I3Exception;

	public abstract void setLPCData(final LPCMapping o) throws I3Exception;

	public abstract void setLPCData(final LPCString o) throws I3Exception;

	public abstract void setLPCData(final Object o) throws I3Exception;

	public abstract int size();

	@Override
	public abstract String toString();
}

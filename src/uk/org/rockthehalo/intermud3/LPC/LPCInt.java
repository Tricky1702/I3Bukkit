package uk.org.rockthehalo.intermud3.LPC;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Utils;

public class LPCInt extends LPCVar implements Comparable<Object> {
	private Long lpcData = new Long(0);

	public LPCInt() {
		super.setType(LPCTypes.INT);
	}

	public LPCInt(final Number o) {
		this();
		this.lpcData = new Long(o.longValue());
	}

	public LPCInt(final LPCInt o) {
		this(o.getLPCData());
	}

	@Override
	public boolean add(final Object o) throws I3Exception {
		if (Number.class.isInstance(o))
			this.lpcData += ((Number) o).longValue();
		else
			try {
				this.lpcData += Long.parseLong(o.toString());
			} catch (NumberFormatException nfE) {
				throw new I3Exception("Invalid argument for LPCInt: add(Object) '" + o + "'", nfE);
			}

		return true;
	}

	@Override
	public LPCInt clone() {
		return new LPCInt(this.lpcData);
	}

	@Override
	public int compareTo(Object o) {
		Long value;

		if (Number.class.isInstance(o))
			value = new Long(((Number) o).longValue());
		else if (Utils.isLPCInt(o))
			value = ((LPCInt) o).getLPCData();
		else
			try {
				value = Long.parseLong(o.toString());
			} catch (NumberFormatException nfE) {
				value = this.lpcData;
			}

		return this.lpcData.compareTo(value);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;

		if (Number.class.isInstance(o) && ((Number) o).longValue() == this.lpcData.longValue())
			return true;
		else if (Utils.isLPCInt(o) && ((LPCInt) o).getLPCData().equals(this.lpcData))
			return true;

		return false;
	}

	@Override
	public Object get(final Object index) {
		if (index == null)
			return null;

		final int i;

		if (Number.class.isInstance(index))
			i = ((Number) index).intValue();
		else
			return null;

		final String str = this.lpcData.toString();

		if (i < 0 || i >= str.length())
			return null;

		return str.charAt(i);
	}

	@Override
	public LPCArray getLPCArray(final Object index) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: getLPCArray(index)");
	}

	@Override
	public Long getLPCData() {
		return this.lpcData;
	}

	@Override
	public LPCInt getLPCInt(final Object index) throws I3Exception {
		final Object obj = get(index);

		if (obj == null)
			return null;

		final long i;

		if (Number.class.isInstance(obj))
			i = ((Number) obj).longValue();
		else
			return null;

		return new LPCInt(i);
	}

	@Override
	public LPCMapping getLPCMapping(final Object index) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: getLPCMapping(index)");
	}

	@Override
	public LPCString getLPCString(final Object index) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: getLPCString(index)");
	}

	@Override
	public int hashCode() {
		return this.lpcData.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Object set(final Object index, final Object element) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: set(index, element)");
	}

	@Override
	public void setLPCData(final LPCArray o) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: setLPCData(LPCArray)");
	}

	@Override
	public void setLPCData(final LPCInt o) {
		this.lpcData = new Long(o.getLPCData());
	}

	@Override
	public void setLPCData(final LPCMapping o) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCInt: setLPCData(LPCMapping)");
	}

	@Override
	public void setLPCData(final LPCString o) throws I3Exception {
		try {
			this.lpcData = new Long(Long.parseLong(o.toString()));
		} catch (NumberFormatException nfE) {
			throw new I3Exception("Invalid argument for LPCInt: setLPCData(LPCString) '" + o + "'", nfE);
		}
	}

	@Override
	public void setLPCData(final Object o) throws I3Exception {
		if (Utils.isLPCArray(o))
			setLPCData((LPCArray) o);
		else if (Utils.isLPCInt(o))
			setLPCData((LPCInt) o);
		else if (Utils.isLPCMapping(o))
			setLPCData((LPCMapping) o);
		else if (Utils.isLPCString(o))
			setLPCData((LPCString) o);
		else
			throw new I3Exception("Invalid argument for LPCInt: setLPCData(Object) '" + o + "'");
	}

	@Override
	public int size() {
		return 1;
	}

	public int intValue() {
		if (this.lpcData >= Integer.MAX_VALUE)
			return Integer.MAX_VALUE;

		if (this.lpcData <= Integer.MIN_VALUE)
			return Integer.MIN_VALUE;

		return this.lpcData.intValue();
	}

	public long longValue() {
		return this.lpcData.longValue();
	}

	public long toNum() {
		return this.lpcData;
	}

	@Override
	public String toString() {
		return this.lpcData.toString();
	}
}

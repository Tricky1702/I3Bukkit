package uk.org.rockthehalo.intermud3.LPC;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Utils;

public class LPCMixed extends LPCVar {
	private Object lpcData = null;

	public LPCMixed() {
		super.setType(LPCTypes.MIXED);
	}

	public LPCMixed(final Object o) {
		this();
		this.lpcData = o;
	}

	public LPCMixed(final LPCMixed o) {
		this(o.getLPCData());
	}

	@Override
	public boolean add(final Object o) throws I3Exception {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).add(o);
		case INT:
			return ((LPCInt) this.lpcData).add(o);
		case MAPPING:
			return ((LPCMapping) this.lpcData).add(o);
		case MIXED:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (LPCMixed) add(o)");
		case STRING:
			return ((LPCString) this.lpcData).add(o);
		default:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) add(o)");
		}
	}

	@Override
	public Object clone() {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).clone();
		case INT:
			return ((LPCInt) this.lpcData).clone();
		case MAPPING:
			return ((LPCMapping) this.lpcData).clone();
		case STRING:
			return ((LPCString) this.lpcData).clone();
		default:
			return new LPCMixed(this.lpcData);
		}
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;

		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).equals(o);
		case INT:
			return ((LPCInt) this.lpcData).equals(o);
		case MAPPING:
			return ((LPCMapping) this.lpcData).equals(o);
		case STRING:
			return ((LPCString) this.lpcData).equals(o);
		default:
			return false;
		}
	}

	@Override
	public Object get(final Object index) throws I3Exception {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).get(index);
		case INT:
			return ((LPCInt) this.lpcData).get(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).get(index);
		case MIXED:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (LPCMixed) get(index)");
		case STRING:
			return ((LPCString) this.lpcData).get(index);
		default:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) get(index)");
		}
	}

	@Override
	public LPCArray getLPCArray(final Object index) throws I3Exception {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).getLPCArray(index);
		case INT:
			return ((LPCInt) this.lpcData).getLPCArray(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).getLPCArray(index);
		case MIXED:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (LPCMixed) getLPCArray(index)");
		case STRING:
			return ((LPCString) this.lpcData).getLPCArray(index);
		default:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) getLPCArray(index)");
		}
	}

	@Override
	public Object getLPCData() {
		return this.lpcData;
	}

	@Override
	public LPCInt getLPCInt(final Object index) throws I3Exception {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).getLPCInt(index);
		case INT:
			return ((LPCInt) this.lpcData).getLPCInt(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).getLPCInt(index);
		case MIXED:
			if (index == null)
				return null;

			final int i;

			if (Number.class.isInstance(index))
				i = ((Number) index).intValue();
			else
				return null;

			final String str = toString();

			if (i < 0 || i >= str.length())
				return null;

			try {
				return new LPCInt(Long.parseLong(str.charAt(i) + ""));
			} catch (NumberFormatException nfE) {
				return null;
			}
		case STRING:
			return ((LPCString) this.lpcData).getLPCInt(index);
		default:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) getLPCInt(index)");
		}
	}

	@Override
	public LPCMapping getLPCMapping(final Object index) throws I3Exception {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).getLPCMapping(index);
		case INT:
			return ((LPCInt) this.lpcData).getLPCMapping(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).getLPCMapping(index);
		case MIXED:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (LPCMixed) getLPCMapping(index)");
		case STRING:
			return ((LPCString) this.lpcData).getLPCMapping(index);
		default:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) getLPCMapping(index)");
		}
	}

	@Override
	public LPCString getLPCString(final Object index) throws I3Exception {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).getLPCString(index);
		case INT:
			return ((LPCInt) this.lpcData).getLPCString(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).getLPCString(index);
		case MIXED:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (LPCMixed) getLPCString(index)");
		case STRING:
			return ((LPCString) this.lpcData).getLPCString(index);
		default:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) getLPCString(index)");
		}
	}

	@Override
	public int hashCode() {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).hashCode();
		case INT:
			return ((LPCInt) this.lpcData).hashCode();
		case MAPPING:
			return ((LPCMapping) this.lpcData).hashCode();
		case STRING:
			return ((LPCString) this.lpcData).hashCode();
		default:
			return this.lpcData.toString().hashCode();
		}
	}

	@Override
	public boolean isEmpty() {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).isEmpty();
		case INT:
			return ((LPCInt) this.lpcData).isEmpty();
		case MAPPING:
			return ((LPCMapping) this.lpcData).isEmpty();
		case STRING:
			return ((LPCString) this.lpcData).isEmpty();
		default:
			return this.lpcData != null;
		}
	}

	@Override
	public Object set(final Object index, final Object element)
			throws I3Exception {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).set(index, element);
		case INT:
			return ((LPCInt) this.lpcData).set(index, element);
		case MAPPING:
			return ((LPCMapping) this.lpcData).set(index, element);
		case MIXED:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (LPCMixed) set(index, element)");
		case STRING:
			return ((LPCString) this.lpcData).set(index, element);
		default:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) set(index, element)");
		}
	}

	@Override
	public void setLPCData(final LPCArray o) {
		this.lpcData = new LPCArray(o);
	}

	@Override
	public void setLPCData(final LPCInt o) {
		this.lpcData = new LPCInt(o);
	}

	@Override
	public void setLPCData(final LPCMapping o) {
		this.lpcData = new LPCMapping(o);
	}

	@Override
	public void setLPCData(final LPCString o) {
		this.lpcData = new LPCString(o);
	}

	@Override
	public void setLPCData(final Object o) throws I3Exception {
		switch (LPCVar.getType(o)) {
		case ARRAY:
			setLPCData((LPCArray) o);

			break;
		case INT:
			setLPCData((LPCInt) o);

			break;
		case MAPPING:
			setLPCData((LPCMapping) o);

			break;
		case MIXED:
			this.lpcData = o;

			break;
		case STRING:
			setLPCData((LPCString) o);

			break;
		default:
			throw new I3Exception(
					"Invalid data for LPCMixed: setLPCData(Object) '" + o + "'");
		}
	}

	@Override
	public int size() {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).size();
		case INT:
			return ((LPCInt) this.lpcData).size();
		case MAPPING:
			return ((LPCMapping) this.lpcData).size();
		case STRING:
			return ((LPCString) this.lpcData).size();
		default:
			return this.lpcData != null ? this.lpcData.toString().length() : 0;
		}
	}

	@Override
	public String toString() {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
		case INT:
		case MAPPING:
		case STRING:
			return Utils.toMudMode(this.lpcData);
		default:
			return this.lpcData.toString();
		}
	}
}

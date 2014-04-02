package uk.org.rockthehalo.intermud3.LPC;

import uk.org.rockthehalo.intermud3.I3Exception;

public class LPCString extends LPCVar implements Cloneable {
	private String lpcData;

	public LPCString() {
		this.lpcData = new String();
		this.setType(LPCTypes.STRING);
	}

	public LPCString(LPCString obj) {
		this.lpcData = new String(obj.lpcData);
		this.setType(LPCTypes.STRING);
	}

	public LPCString(String lpcData) {
		this.lpcData = new String(lpcData);
		this.setType(LPCTypes.STRING);
	}

	@Override
	public boolean add(Object lpcData) {
		this.lpcData.concat(lpcData.toString());

		return true;
	}

	@Override
	public Object clone() {
		return new LPCString(this.lpcData);
	}

	@Override
	public Object get(Object index) {
		if (index == null)
			return null;

		int ind = Integer.class.cast(index);

		if (ind < 0 || ind >= this.size())
			return null;

		return this.lpcData.charAt(ind);
	}

	@Override
	public LPCArray getLPCArray(Object index) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCString: getLPCArray(index)");
	}

	@Override
	public String getLPCData() {
		return this.lpcData;
	}

	@Override
	public LPCInt getLPCInt(Object index) throws I3Exception {
		Object obj = this.get(index);

		if (obj == null)
			return null;

		int i = 0;

		try {
			i = Integer.parseInt(obj.toString());
		} catch (NumberFormatException nfE) {
			throw new I3Exception(
					"Invalid operation for LPCString: getLPCInt(index)", nfE);
		}

		return new LPCInt(i);
	}

	@Override
	public LPCMapping getLPCMapping(Object index) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCString: getLPCMapping(index)");
	}

	@Override
	public LPCString getLPCString(Object index) {
		Object obj = this.get(index);

		if (obj == null)
			return null;

		return new LPCString(obj.toString());
	}

	@Override
	public boolean isEmpty() {
		return this.lpcData.isEmpty();
	}

	@Override
	public Object set(Object index, Object lpcData) throws I3Exception {
		int i = (Integer) index;

		if (i < 0 || i >= this.size())
			throw new I3Exception(
					"Index out of range for set(index, lpcdata) in LPCString: "
							+ index);

		char[] chars = this.lpcData.toCharArray();
		char oldChar = chars[i];

		chars[i] = lpcData.toString().charAt(0);
		this.lpcData = String.copyValueOf(chars);

		return oldChar;
	}

	@Override
	public void setLPCData(LPCArray obj) {
		this.lpcData = new String(LPCVar.toMudMode(obj));
	}

	@Override
	public void setLPCData(LPCInt obj) {
		this.lpcData = new String(LPCVar.toMudMode(obj));
	}

	@Override
	public void setLPCData(LPCMapping obj) {
		this.lpcData = new String(LPCVar.toMudMode(obj));
	}

	@Override
	public void setLPCData(LPCString obj) {
		this.lpcData = new String(LPCVar.toMudMode(obj));
	}

	@Override
	public void setLPCData(Object obj) throws I3Exception {
		if (LPCVar.isLPCVar(obj))
			this.lpcData = new String(LPCVar.toMudMode(obj));
		else
			throw new I3Exception(
					"Invalid data for LPCString: setLPCData(Object) '"
							+ obj.toString() + "'");
	}

	@Override
	public int size() {
		return this.lpcData.length();
	}

	@Override
	public String toString() {
		return this.lpcData;
	}
}

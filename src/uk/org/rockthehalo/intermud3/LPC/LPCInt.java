package uk.org.rockthehalo.intermud3.LPC;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Utils;

public class LPCInt extends LPCVar {
	private Integer lpcData = new Integer(0);

	public LPCInt() {
		super.setType(LPCTypes.INT);
	}

	public LPCInt(LPCInt obj) {
		super.setType(LPCTypes.INT);
		this.lpcData = new Integer(obj.getLPCData());
	}

	public LPCInt(Integer lpcData) {
		super.setType(LPCTypes.INT);
		this.lpcData = new Integer(lpcData);
	}

	@Override
	public boolean add(Object lpcData) {
		this.lpcData = Integer.parseInt(this.lpcData.toString())
				+ Integer.parseInt(lpcData.toString());

		return true;
	}

	@Override
	public LPCInt clone() {
		return new LPCInt(this.lpcData);
	}

	@Override
	public Object get(Object index) {
		if (index == null)
			return null;

		String str = this.lpcData.toString();
		int ind = Integer.class.cast(index);

		if (ind < 0 || ind >= str.length())
			return null;

		return str.charAt(ind);
	}

	@Override
	public LPCArray getLPCArray(Object index) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCInt: getLPCArray(index)");
	}

	@Override
	public Integer getLPCData() {
		return this.lpcData;
	}

	@Override
	public LPCInt getLPCInt(Object index) throws I3Exception {
		Object obj = this.get(index);

		if (obj == null)
			return null;

		int i = 0;

		try {
			i = Integer.valueOf(obj.toString());
		} catch (NumberFormatException nfE) {
			throw new I3Exception(
					"Invalid operation for LPCInt: getLPCInt(index)", nfE);
		}

		return new LPCInt(i);
	}

	@Override
	public LPCMapping getLPCMapping(Object index) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCInt: getLPCMapping(index)");
	}

	@Override
	public LPCString getLPCString(Object index) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCInt: getLPCString(index)");
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public Object set(Object index, Object lpcData) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCInt: set(index, lpcData)");
	}

	@Override
	public void setLPCData(LPCArray obj) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCInt: setLPCData(LPCArray)");
	}

	@Override
	public void setLPCData(LPCInt obj) {
		this.lpcData = new Integer(obj.toInt());
	}

	@Override
	public void setLPCData(LPCMapping obj) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCInt: setLPCData(LPCMapping)");
	}

	@Override
	public void setLPCData(LPCString obj) throws I3Exception {
		try {
			this.lpcData = new Integer(obj.getLPCData());
		} catch (NumberFormatException nfE) {
			throw new I3Exception(
					"Invalid operation for LPCInt: setLPCData(LPCString)", nfE);
		}
	}

	@Override
	public void setLPCData(Object obj) throws I3Exception {
		if (Utils.isLPCArray(obj))
			setLPCData((LPCArray) obj);
		else if (Utils.isLPCInt(obj))
			setLPCData((LPCInt) obj);
		else if (Utils.isLPCMapping(obj))
			setLPCData((LPCMapping) obj);
		else if (Utils.isLPCString(obj))
			setLPCData((LPCString) obj);
		else
			throw new I3Exception(
					"Invalid data for LPCInt: setLPCData(Object) '"
							+ obj.toString() + "'");
	}

	@Override
	public int size() {
		return 1;
	}

	public Integer toInt() {
		return this.lpcData;
	}

	@Override
	public String toString() {
		return this.lpcData.toString();
	}
}

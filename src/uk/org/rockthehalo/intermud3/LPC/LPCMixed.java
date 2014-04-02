package uk.org.rockthehalo.intermud3.LPC;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Vector;

import uk.org.rockthehalo.intermud3.I3Exception;

public class LPCMixed extends LPCVar implements Cloneable {
	private Object lpcData;

	public LPCMixed() {
		this.lpcData = null;
		this.setType(LPCTypes.MIXED);
	}

	public LPCMixed(LPCMixed obj) {
		this.lpcData = obj.getLPCData();
		this.setType(LPCTypes.MIXED);
	}

	public LPCMixed(Object lpcData) {
		this.lpcData = lpcData;
		this.setType(LPCTypes.MIXED);
	}

	@Override
	public boolean add(Object lpcData) throws I3Exception {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).add(lpcData);
		case INT:
			return ((LPCInt) this.lpcData).add(lpcData);
		case MAPPING:
			return ((LPCMapping) this.lpcData).add(lpcData);
		case MIXED:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (LPCMixed) add(lpcData)");
		case STRING:
			return ((LPCString) this.lpcData).add(lpcData);
		default:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) add(lpcData)");
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
			break;
		}

		return new LPCMixed(this.lpcData);
	}

	@Override
	public Object get(Object index) throws I3Exception {
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
	public LPCArray getLPCArray(Object index) throws I3Exception {
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
	public LPCInt getLPCInt(Object index) throws I3Exception {
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

			String str = this.toString();
			int ind = Integer.class.cast(index);

			if (ind < 0 || ind >= str.length())
				return null;

			char ch = str.charAt(ind);
			int i = 0;

			try {
				i = Integer.valueOf(ch);
			} catch (NumberFormatException nfE) {
				throw new I3Exception(
						"Invalid operation for LPCMixed: (LPCMixed) getLPCInt(index)",
						nfE);
			}

			return new LPCInt(i);
		case STRING:
			return ((LPCString) this.lpcData).getLPCInt(index);
		default:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) getLPCInt(index)");
		}
	}

	@Override
	public LPCMapping getLPCMapping(Object index) throws I3Exception {
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
	public LPCString getLPCString(Object index) throws I3Exception {
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
			break;
		}

		return this.lpcData != null;
	}

	@Override
	public Object set(Object index, Object lpcData) throws I3Exception {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
			return ((LPCArray) this.lpcData).set(index, lpcData);
		case INT:
			return ((LPCInt) this.lpcData).set(index, lpcData);
		case MAPPING:
			return ((LPCMapping) this.lpcData).set(index, lpcData);
		case MIXED:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (LPCMixed) set(index, lpcData)");
		case STRING:
			return ((LPCString) this.lpcData).set(index, lpcData);
		default:
			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) set(index, lpcData)");
		}
	}

	@Override
	public void setLPCData(LPCArray obj) {
		this.lpcData = new Vector<Object>();
		((LPCArray) this.lpcData).addAll(obj.getLPCData());
	}

	@Override
	public void setLPCData(LPCInt obj) {
		this.lpcData = new Integer(obj.getLPCData());
	}

	@Override
	public void setLPCData(LPCMapping obj) {
		this.lpcData = Collections
				.synchronizedMap(new LinkedHashMap<Object, Object>());
		((LPCMapping) this.lpcData).putAll(Collections.synchronizedMap(obj
				.getLPCData()));
	}

	@Override
	public void setLPCData(LPCString obj) {
		this.lpcData = new String(obj.getLPCData());
	}

	@Override
	public void setLPCData(Object obj) throws I3Exception {
		switch (LPCVar.getType(obj)) {
		case ARRAY:
			setLPCData((LPCArray) obj);

			break;
		case INT:
			setLPCData((LPCInt) obj);

			break;
		case MAPPING:
			setLPCData((LPCMapping) obj);

			break;
		case MIXED:
			this.lpcData = obj;

			break;
		case STRING:
			setLPCData((LPCString) obj);

			break;
		default:
			throw new I3Exception(
					"Invalid data for LPCMixed: setLPCData(Object) '"
							+ obj.toString() + "'");
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
			break;
		}

		return this.lpcData != null ? this.lpcData.toString().length() : 0;
	}

	@Override
	public String toString() {
		switch (LPCVar.getType(this.lpcData)) {
		case ARRAY:
		case INT:
		case MAPPING:
		case STRING:
			return LPCVar.toMudMode(this.lpcData);
		default:
			break;
		}

		return this.lpcData.toString();
	}
}

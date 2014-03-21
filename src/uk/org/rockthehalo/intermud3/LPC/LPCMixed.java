package uk.org.rockthehalo.intermud3.LPC;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.bukkit.entity.Player;

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
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).add(lpcData);
		case INT:
			return ((LPCInt) this.lpcData).add(lpcData);
		case MAPPING:
			return ((LPCMapping) this.lpcData).add(lpcData);
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).add(lpcData);
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).add(lpcData);
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).add(lpcData);
			else if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) add(lpcData)");
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).add(lpcData);
			else
				throw new I3Exception(
						"Invalid operation for LPCMixed: (LPCMixed) add(lpcData)");
		case STRING:
			return ((LPCString) this.lpcData).add(lpcData);
		default:
			if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) add(lpcData)");

			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) add(lpcData)");
		}
	}

	@Override
	public Object clone() {
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).clone();
		case INT:
			return ((LPCInt) this.lpcData).clone();
		case MAPPING:
			return ((LPCMapping) this.lpcData).clone();
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).clone();
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).clone();
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).clone();
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).clone();
			else
				return new LPCMixed(this.lpcData);
		case STRING:
			return ((LPCString) this.lpcData).clone();
		default:
			return new LPCMixed(this.lpcData);
		}
	}

	@Override
	public Object get(Object index) throws I3Exception {
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).get(index);
		case INT:
			return ((LPCInt) this.lpcData).get(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).get(index);
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).get(index);
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).get(index);
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).get(index);
			else if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) get(index)");
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).add(index);
			else
				throw new I3Exception(
						"Invalid operation for LPCMixed: (LPCMixed) get(index)");
		case STRING:
			return ((LPCString) this.lpcData).add(index);
		default:
			if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) get(index)");

			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) get(index)");
		}
	}

	@Override
	public LPCArray getLPCArray(Object index) throws I3Exception {
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).getLPCArray(index);
		case INT:
			return ((LPCInt) this.lpcData).getLPCArray(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).getLPCArray(index);
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).getLPCArray(index);
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).getLPCArray(index);
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).getLPCArray(index);
			else if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) getLPCArray(index)");
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).getLPCArray(index);
			else
				throw new I3Exception(
						"Invalid operation for LPCMixed: (LPCMixed) getLPCArray(index)");
		case STRING:
			return ((LPCString) this.lpcData).getLPCArray(index);
		default:
			if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) getLPCArray(index)");

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
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).getLPCInt(index);
		case INT:
			return ((LPCInt) this.lpcData).getLPCInt(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).getLPCInt(index);
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).getLPCInt(index);
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).getLPCInt(index);
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).getLPCInt(index);
			else if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) getLPCInt(index)");
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).getLPCInt(index);
			else {
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
			}
		case STRING:
			return ((LPCString) this.lpcData).getLPCInt(index);
		default:
			if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) getLPCInt(index)");

			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) getLPCInt(index)");
		}
	}

	@Override
	public LPCMapping getLPCMapping(Object index) throws I3Exception {
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).getLPCMapping(index);
		case INT:
			return ((LPCInt) this.lpcData).getLPCMapping(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).getLPCMapping(index);
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).getLPCMapping(index);
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).getLPCMapping(index);
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).getLPCMapping(index);
			else if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) getLPCMapping(index)");
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).getLPCMapping(index);
			else
				throw new I3Exception(
						"Invalid operation for LPCMixed: (LPCMixed) getLPCMapping(index)");
		case STRING:
			return ((LPCString) this.lpcData).getLPCMapping(index);
		default:
			if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) getLPCMapping(index)");

			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) getLPCMapping(index)");
		}
	}

	@Override
	public LPCString getLPCString(Object index) throws I3Exception {
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).getLPCString(index);
		case INT:
			return ((LPCInt) this.lpcData).getLPCString(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).getLPCString(index);
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).getLPCString(index);
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).getLPCString(index);
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).getLPCString(index);
			else if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) getLPCString(index)");
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).getLPCString(index);
			else
				throw new I3Exception(
						"Invalid operation for LPCMixed: (LPCMixed) getLPCString(index)");
		case STRING:
			return ((LPCString) this.lpcData).getLPCString(index);
		default:
			if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) getLPCString(index)");

			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) getLPCString(index)");
		}
	}

	@Override
	public Player getPlayer(Object index) throws I3Exception {
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).getPlayer(index);
		case INT:
			return ((LPCInt) this.lpcData).getPlayer(index);
		case MAPPING:
			return ((LPCMapping) this.lpcData).getPlayer(index);
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).getPlayer(index);
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).getPlayer(index);
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).getPlayer(index);
			else if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) getPlayer(index)");
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).getPlayer(index);
			else
				throw new I3Exception(
						"Invalid operation for LPCMixed: (LPCMixed) getPlayer(index)");
		case STRING:
			return ((LPCString) this.lpcData).getPlayer(index);
		default:
			if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) getPlayer(index)");

			throw new I3Exception(
					"Invalid operation for LPCMixed: (Unknown) getPlayer(index)");
		}
	}

	@Override
	public boolean isEmpty() {
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).isEmpty();
		case INT:
			return ((LPCInt) this.lpcData).isEmpty();
		case MAPPING:
			return ((LPCMapping) this.lpcData).isEmpty();
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).isEmpty();
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).isEmpty();
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).isEmpty();
			else if (LPCVar.isPlayer(this.lpcData))
				return ((Player) this.lpcData).getName().isEmpty();
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).isEmpty();
			else
				return this.lpcData != null;
		case STRING:
			return ((LPCString) this.lpcData).isEmpty();
		default:
			if (LPCVar.isPlayer(this.lpcData))
				return ((Player) this.lpcData).getName().isEmpty();

			break;
		}

		return this.lpcData != null;
	}

	@Override
	public Object set(Object index, Object lpcData) throws I3Exception {
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).set(index, lpcData);
		case INT:
			return ((LPCInt) this.lpcData).set(index, lpcData);
		case MAPPING:
			return ((LPCMapping) this.lpcData).set(index, lpcData);
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).set(index, lpcData);
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).set(index, lpcData);
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).set(index, lpcData);
			else if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) set(index, lpcData)");
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).set(index, lpcData);
			else if (LPCVar.isMixed(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (LPCMixed) set(index, lpcData)");
		case STRING:
			return ((LPCString) this.lpcData).set(index, lpcData);
		default:
			if (LPCVar.isPlayer(this.lpcData))
				throw new I3Exception(
						"Invalid operation for LPCMixed: (Player) set(index, lpcData)");

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
		if (LPCVar.isArray(obj))
			setLPCData((LPCArray) obj);
		else if (LPCVar.isInt(obj))
			setLPCData((LPCInt) obj);
		else if (LPCVar.isMapping(obj))
			setLPCData((LPCMapping) obj);
		else if (LPCVar.isString(obj))
			setLPCData((LPCString) obj);
		else if (LPCVar.isMixed(obj))
			this.lpcData = obj;
		else
			throw new I3Exception(
					"Invalid data for LPCMixed: setLPCData(Object) '"
							+ obj.toString() + "'");
	}

	@Override
	public int size() {
		switch (this.getType()) {
		case ARRAY:
			return ((LPCArray) this.lpcData).size();
		case INT:
			return ((LPCInt) this.lpcData).size();
		case MAPPING:
			return ((LPCMapping) this.lpcData).size();
		case MIXED:
			if (LPCVar.isArray(this.lpcData))
				return ((LPCArray) this.lpcData).size();
			else if (LPCVar.isInt(this.lpcData))
				return ((LPCInt) this.lpcData).size();
			else if (LPCVar.isMapping(this.lpcData))
				return ((LPCMapping) this.lpcData).size();
			else if (LPCVar.isPlayer(this.lpcData))
				return ((Player) this.lpcData).getName().length();
			else if (LPCVar.isString(this.lpcData))
				return ((LPCString) this.lpcData).size();
			else if (LPCVar.isMixed(this.lpcData))
				return this.lpcData != null ? this.toString().length() : 0;
		case STRING:
			return ((LPCString) this.lpcData).size();
		default:
			if (LPCVar.isPlayer(this.lpcData))
				return ((Player) this.lpcData).getName().length();

			break;
		}

		return this.lpcData != null ? this.toString().length() : 0;
	}

	@Override
	public String toString() {
		return this.lpcData.toString();
	}
}

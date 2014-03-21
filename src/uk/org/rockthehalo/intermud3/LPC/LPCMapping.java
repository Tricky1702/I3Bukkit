package uk.org.rockthehalo.intermud3.LPC;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;

import uk.org.rockthehalo.intermud3.I3Exception;

public class LPCMapping extends LPCVar implements Cloneable,
		Map<Object, Object> {
	private Map<Object, Object> lpcData;

	public LPCMapping() {
		this.lpcData = Collections
				.synchronizedMap(new LinkedHashMap<Object, Object>());
		this.setType(LPCTypes.MAPPING);
	}

	public LPCMapping(LPCMapping obj) {
		this.lpcData = Collections
				.synchronizedMap(new LinkedHashMap<Object, Object>());
		this.lpcData.putAll(Collections.synchronizedMap(obj.getLPCData()));
		this.setType(LPCTypes.MAPPING);
	}

	public LPCMapping(Map<Object, Object> lpcData) {
		this.lpcData = Collections
				.synchronizedMap(new LinkedHashMap<Object, Object>());
		this.lpcData.putAll(Collections.synchronizedMap(lpcData));
		this.setType(LPCTypes.MAPPING);
	}

	@Override
	public boolean add(Object lpcData) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCMapping: add(lpcData)");
	}

	@Override
	public void clear() {
		this.lpcData.clear();
	}

	@Override
	public Object clone() {
		return new LPCMapping(this.lpcData);
	}

	@Override
	public boolean containsKey(Object index) {
		if (index == null)
			return false;

		return this.getKey(index) != null;
	}

	@Override
	public boolean containsValue(Object lpcData) {
		if (lpcData == null)
			return false;

		return this.getValue(lpcData) != null;
	}

	@Override
	public Set<Entry<Object, Object>> entrySet() {
		return this.lpcData.entrySet();
	}

	@Override
	public Object get(Object index) {
		if (index == null)
			return null;

		Object realKey = this.getKey(index);

		if (realKey == null)
			return null;

		return this.lpcData.get(realKey);
	}

	public Object getKey(Object key) {
		if (key == null)
			return null;

		Class<? extends Object> kClass = key.getClass();
		String kString = key.toString();

		for (Object obj : this.keySet())
			if (kClass.isInstance(obj) && obj.toString().equals(kString))
				return obj;

		return null;
	}

	@Override
	public LPCArray getLPCArray(Object index) {
		Object obj = this.get(index);

		if (LPCVar.isArray(obj))
			return (LPCArray) obj;

		return null;
	}

	@Override
	public Map<Object, Object> getLPCData() {
		return this.lpcData;
	}

	@Override
	public LPCInt getLPCInt(Object index) {
		Object obj = this.get(index);

		if (LPCVar.isInt(obj))
			return (LPCInt) obj;

		return null;
	}

	@Override
	public LPCMapping getLPCMapping(Object index) {
		Object obj = this.get(index);

		if (LPCVar.isMapping(obj))
			return (LPCMapping) obj;

		return null;
	}

	@Override
	public LPCString getLPCString(Object index) {
		Object obj = this.get(index);

		if (LPCVar.isString(obj))
			return (LPCString) obj;

		return null;
	}

	@Override
	public Player getPlayer(Object index) {
		Object obj = this.get(index);

		if (LPCVar.isPlayer(obj))
			return (Player) obj;

		return null;
	}

	public Object getValue(Object value) {
		if (value == null)
			return null;

		Class<? extends Object> vClass = value.getClass();
		String vString = value.toString();

		for (Object obj : this.values())
			if (vClass.isInstance(obj) && obj.toString().equals(vString))
				return obj;

		return null;
	}

	@Override
	public boolean isEmpty() {
		return this.lpcData.isEmpty();
	}

	@Override
	public Set<Object> keySet() {
		return this.lpcData.keySet();
	}

	@Override
	public Object put(Object index, Object lpcData) {
		if (index == null || lpcData == null)
			return null;

		Object realKey = this.getKey(index);

		if (realKey == null)
			realKey = index;

		return this.lpcData.put(realKey, lpcData);
	}

	@Override
	public void putAll(Map<?, ?> map) {
		this.lpcData.putAll(map);
	}

	@Override
	public Object remove(Object key) {
		if (key == null)
			return null;

		Object realKey = this.getKey(key);

		if (realKey == null)
			return null;

		return this.lpcData.remove(realKey);
	}

	@Override
	public Object set(Object index, Object lpcData) {
		if (index == null || lpcData == null)
			return null;

		return this.put(index, lpcData);
	}

	@Override
	public void setLPCData(LPCArray obj) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCMapping: setLPCData(LPCArray)");
	}

	@Override
	public void setLPCData(LPCInt obj) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCMapping: setLPCData(LPCInt)");
	}

	@Override
	public void setLPCData(LPCMapping obj) {
		this.lpcData.clear();
		this.lpcData.putAll(Collections.synchronizedMap(obj.getLPCData()));
	}

	@Override
	public void setLPCData(LPCString obj) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCMapping: setLPCData(LPCString)");
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
		else
			throw new I3Exception(
					"Invalid data for LPCMapping: setLPCData(Object) '"
							+ obj.toString() + "'");
	}

	@Override
	public int size() {
		return this.lpcData.size();
	}

	@Override
	public String toString() {
		return this.lpcData.toString();
	}

	@Override
	public Collection<Object> values() {
		return this.lpcData.values();
	}
}

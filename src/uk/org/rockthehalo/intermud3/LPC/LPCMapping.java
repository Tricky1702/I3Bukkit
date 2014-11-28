package uk.org.rockthehalo.intermud3.LPC;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Utils;

public class LPCMapping extends LPCVar implements Map<Object, Object> {
	private Map<Object, Object> lpcData = Collections.synchronizedMap(new LinkedHashMap<Object, Object>());

	public LPCMapping() {
		super.setType(LPCTypes.MAPPING);
	}

	public LPCMapping(final int size) {
		this();
		this.lpcData = Collections.synchronizedMap(new LinkedHashMap<Object, Object>(size));
	}

	public LPCMapping(final Map<Object, Object> o) {
		this(o.size());
		this.putAll(Collections.synchronizedMap(o));
	}

	public LPCMapping(final LPCMapping o) {
		this(o.getLPCData());
	}

	@Override
	public boolean add(final Object o) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCMapping: add(o)");
	}

	@Override
	public void clear() {
		this.lpcData.clear();
	}

	@Override
	public LPCMapping clone() {
		return new LPCMapping(this.lpcData);
	}

	@Override
	public boolean containsKey(final Object key) {
		if (key == null)
			return false;

		if (String.class.isInstance(key))
			return getKey(new LPCString((String) key)) != null;
		else if (Number.class.isInstance(key))
			return getKey(new LPCInt((Number) key)) != null;
		else if (Utils.isLPCVar(key))
			return getKey(key) != null;

		return false;
	}

	@Override
	public boolean containsValue(final Object value) {
		if (value == null)
			return false;

		if (String.class.isInstance(value))
			return getValue(new LPCString((String) value)) != null;
		else if (Number.class.isInstance(value))
			return getValue(new LPCInt((Number) value)) != null;
		else if (Utils.isLPCVar(value))
			return getValue(value) != null;

		return false;
	}

	@Override
	public Set<Entry<Object, Object>> entrySet() {
		return this.lpcData.entrySet();
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;

		if (Utils.isLPCMapping(o) && Utils.toMudMode(o).equals(Utils.toMudMode(this)))
			return true;

		return false;
	}

	@Override
	public Object get(final Object key) {
		if (key == null)
			return null;

		final Object realKey = getKey(key);

		if (realKey == null)
			return null;

		return this.lpcData.get(realKey);
	}

	public Object getKey(Object key) {
		if (key == null)
			return null;

		if (String.class.isInstance(key))
			key = new LPCString((String) key);
		else if (Number.class.isInstance(key))
			key = new LPCInt((Number) key);

		final Class<? extends Object> kClass = key.getClass();

		for (final Object obj : keySet())
			if (kClass.isInstance(obj)) {
				switch (LPCVar.getType(obj)) {
				case ARRAY:
					if (((LPCArray) obj).equals(key))
						return obj;

					break;
				case INT:
					if (((LPCInt) obj).equals(key))
						return obj;

					break;
				case MAPPING:
					if (((LPCMapping) obj).equals(key))
						return obj;

					break;
				case MIXED:
					if (((LPCMixed) obj).equals(key))
						return obj;

					break;
				case STRING:
					if (((LPCString) obj).equals(key))
						return obj;

					break;
				default:
					break;
				}
			}

		return null;
	}

	@Override
	public LPCArray getLPCArray(final Object o) {
		final Object obj = get(o);

		if (Utils.isLPCArray(obj))
			return (LPCArray) obj;

		return null;
	}

	@Override
	public Map<Object, Object> getLPCData() {
		return this.lpcData;
	}

	@Override
	public LPCInt getLPCInt(final Object o) {
		final Object obj = get(o);

		if (Utils.isLPCInt(obj))
			return (LPCInt) obj;

		return null;
	}

	@Override
	public LPCMapping getLPCMapping(final Object o) {
		final Object obj = get(o);

		if (Utils.isLPCMapping(obj))
			return (LPCMapping) obj;

		return null;
	}

	@Override
	public LPCString getLPCString(final Object o) {
		final Object obj = get(o);

		if (Utils.isLPCString(obj))
			return (LPCString) obj;

		return null;
	}

	public Object getValue(Object value) {
		if (value == null)
			return null;

		if (String.class.isInstance(value))
			value = new LPCString((String) value);
		else if (Number.class.isInstance(value))
			value = new LPCInt((Number) value);

		final Class<? extends Object> vClass = value.getClass();

		for (final Object obj : values())
			if (vClass.isInstance(obj)) {
				switch (LPCVar.getType(obj)) {
				case ARRAY:
					if (((LPCArray) obj).equals(value))
						return obj;

					break;
				case INT:
					if (((LPCInt) obj).equals(value))
						return obj;

					break;
				case MAPPING:
					if (((LPCMapping) obj).equals(value))
						return obj;

					break;
				case MIXED:
					if (((LPCMixed) obj).equals(value))
						return obj;

					break;
				case STRING:
					if (((LPCString) obj).equals(value))
						return obj;

					break;
				default:
					break;
				}
			}

		return null;
	}

	@Override
	public int hashCode() {
		return Utils.toMudMode(this).hashCode();
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
	public Object put(final Object key, final Object value) {
		if (key == null)
			return null;

		Object realKey = getKey(key);

		if (realKey == null)
			realKey = key;

		return this.lpcData.put(realKey, value);
	}

	@Override
	public void putAll(Map<? extends Object, ? extends Object> m) {
		this.lpcData.putAll(m);
	}

	@Override
	public Object remove(final Object key) {
		if (key == null)
			return null;

		final Object realKey = getKey(key);

		if (realKey == null)
			return null;

		return this.lpcData.remove(realKey);
	}

	@Override
	public Object set(final Object key, final Object value) {
		return this.put(key, value);
	}

	@Override
	public void setLPCData(final LPCArray o) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCMapping: setLPCData(LPCArray)");
	}

	@Override
	public void setLPCData(final LPCInt o) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCMapping: setLPCData(LPCInt)");
	}

	@Override
	public void setLPCData(final LPCMapping o) {
		clear();
		putAll(Collections.synchronizedMap(o.getLPCData()));
	}

	@Override
	public void setLPCData(final LPCString o) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCMapping: setLPCData(LPCString)");
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
			throw new I3Exception("Invalid data for LPCMapping: setLPCData(Object) '" + o.toString() + "'");
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

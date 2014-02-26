package uk.org.rockthehalo.intermud3;

import java.util.Hashtable;

import org.bukkit.entity.Player;

public class LPCMapping extends LPCMixed {
	private Hashtable<Object, Object> lpcData;

	public LPCMapping() {
		this.lpcData = new Hashtable<Object, Object>();
		super.setType(LPCTypes.MAPPING);
	}

	public LPCMapping(Hashtable<Object, Object> lpcData) {
		this.lpcData = lpcData;
		super.setType(LPCTypes.MAPPING);
	}

	public int size() {
		return ((Hashtable<?, ?>) lpcData).size();
	}

	public boolean isEmpty() {
		return ((Hashtable<?, ?>) lpcData).isEmpty();
	}

	@Override
	public Object clone() {
		return new LPCMapping(lpcData);
	}

	public Object get() {
		return lpcData;
	}

	public Object get(Object index) {
		return lpcData.get(index);
	}

	@Override
	public LPCString getString(Object index) {
		LPCString s = new LPCString();

		try {
			s.set(lpcData.get(index));
		} catch (ClassCastException e) {
			s = null;
		}

		return s;
	}

	@Override
	public LPCInt getInt(Object index) {
		LPCInt i = new LPCInt();

		try {
			i.set(lpcData.get(index));
		} catch (ClassCastException e) {
			i = null;
		}

		return i;
	}

	@Override
	public LPCArray getArray(Object index) {
		LPCArray a = new LPCArray();

		try {
			a.set(lpcData.get(index));
		} catch (ClassCastException e) {
			a = null;
		}

		return a;
	}

	@Override
	public LPCMapping getMapping(Object index) {
		LPCMapping m = new LPCMapping();

		try {
			m.set(lpcData.get(index));
		} catch (ClassCastException e) {
			m = null;
		}

		return m;
	}

	@Override
	public Player getPlayer(Object index) {
		Object o = lpcData.get(index);

		if (o instanceof Player)
			return (Player) o;

		return null;
	}

	@SuppressWarnings("unchecked")
	public void set(Object lpcData) {
		this.lpcData = (Hashtable<Object, Object>) lpcData;
	}

	public Object set(Object index, Object lpcData) {
		return this.lpcData.put(index, lpcData);
	}

	public boolean add(Object lpcData) throws I3Exception {
		throw new I3Exception("Invalid operation for LPCMapping: add(lpcData)");
	}

	@Override
	public String toString() {
		return lpcData.toString();
	}
}

package uk.org.rockthehalo.intermud3;

import java.util.Vector;

import org.bukkit.entity.Player;

public class LPCArray extends LPCMixed {
	private Vector<Object> lpcData;

	public LPCArray() {
		this.lpcData = new Vector<Object>();
		super.setType(LPCTypes.ARRAY);
	}

	public LPCArray(Vector<Object> lpcData) {
		this.lpcData = lpcData;
		super.setType(LPCTypes.ARRAY);
	}

	public int size() {
		return ((Vector<?>) lpcData).size();
	}

	public boolean isEmpty() {
		return ((Vector<?>) lpcData).isEmpty();
	}

	@Override
	public Object clone() {
		return lpcData.clone();
	}

	public Object get() {
		return lpcData;
	}

	public Object get(Object index) {
		return lpcData.get((Integer) index);
	}

	@Override
	public LPCString getString(Object index) {
		LPCString s = new LPCString();

		try {
			s.set(lpcData.get((Integer) index));
		} catch (ClassCastException e) {
			s = null;
		}

		return s;
	}

	@Override
	public LPCInt getInt(Object index) {
		LPCInt i = new LPCInt();

		try {
			i.set(lpcData.get((Integer) index));
		} catch (ClassCastException e) {
			i = null;
		}

		return i;
	}

	@Override
	public LPCArray getArray(Object index) {
		LPCArray a = new LPCArray();

		try {
			a.set(lpcData.get((Integer) index));
		} catch (ClassCastException e) {
			a = null;
		}

		return a;
	}

	@Override
	public LPCMapping getMapping(Object index) {
		LPCMapping m = new LPCMapping();

		try {
			m.set(lpcData.get((Integer) index));
		} catch (ClassCastException e) {
			m = null;
		}

		return m;
	}

	@Override
	public Player getPlayer(Object index) {
		Object o = lpcData.get((Integer) index);

		if (o instanceof Player)
			return (Player) o;

		return null;
	}

	@SuppressWarnings("unchecked")
	public void set(Object lpcData) {
		this.lpcData = (Vector<Object>) lpcData;
	}

	public Object set(Object index, Object lpcData) {
		return ((Vector<Object>) this.lpcData).set((Integer) index, lpcData);
	}

	public boolean add(Object lpcData) {
		return this.lpcData.add(lpcData);
	}

	@Override
	public String toString() {
		return lpcData.toString();
	}
}

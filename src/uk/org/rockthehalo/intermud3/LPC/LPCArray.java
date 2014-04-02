package uk.org.rockthehalo.intermud3.LPC;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import uk.org.rockthehalo.intermud3.I3Exception;

public class LPCArray extends LPCVar implements Cloneable, List<Object> {
	private Vector<Object> lpcData;

	public LPCArray() {
		this.lpcData = new Vector<Object>();
		this.setType(LPCTypes.ARRAY);
	}

	public LPCArray(LPCArray obj) {
		this.lpcData = new Vector<Object>();
		this.lpcData.addAll(obj.getLPCData());
		this.setType(LPCTypes.ARRAY);
	}

	public LPCArray(Vector<Object> lpcData) {
		this.lpcData = new Vector<Object>();
		this.lpcData.addAll(lpcData);
		this.setType(LPCTypes.ARRAY);
	}

	public boolean add(LPCArray arr) {
		return this.lpcData.add(arr);
	}

	@Override
	public boolean add(Object lpcData) {
		return this.lpcData.add(lpcData);
	}

	@Override
	public void add(int index, Object lpcData) {
		this.lpcData.add(index, lpcData);
	}

	@Override
	public boolean addAll(Collection<? extends Object> c) {
		return this.lpcData.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends Object> c) {
		return this.lpcData.addAll(index, c);
	}

	@Override
	public void clear() {
		this.lpcData.clear();
	}

	@Override
	public Object clone() {
		return new LPCArray(this.lpcData);
	}

	@Override
	public boolean contains(Object o) {
		if (o == null)
			return false;

		return this.getValue(o) != null;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return this.lpcData.containsAll(c);
	}

	@Override
	public Object get(int index) {
		return this.lpcData.get(index);
	}

	@Override
	public Object get(Object index) {
		if (index == null)
			return null;

		int ind = Integer.class.cast(index);

		return this.get(ind);
	}

	@Override
	public LPCArray getLPCArray(Object index) {
		Object obj = this.get(index);

		if (LPCVar.isLPCArray(obj))
			return (LPCArray) obj;

		return null;
	}

	@Override
	public Vector<Object> getLPCData() {
		return this.lpcData;
	}

	@Override
	public LPCInt getLPCInt(Object index) {
		Object obj = this.get(index);

		if (LPCVar.isLPCInt(obj))
			return (LPCInt) obj;

		return null;
	}

	@Override
	public LPCMapping getLPCMapping(Object index) {
		Object obj = this.get(index);

		if (LPCVar.isLPCMapping(obj))
			return (LPCMapping) obj;

		return null;
	}

	@Override
	public LPCString getLPCString(Object index) {
		Object obj = this.get(index);

		if (LPCVar.isLPCString(obj))
			return (LPCString) obj;

		return null;
	}

	public Object getValue(Object key) {
		return this.getValue(key, 0);
	}

	public Object getValue(Object key, int index) {
		if (key == null)
			return null;

		Class<? extends Object> kClass = key.getClass();
		String kString = key.toString();
		int i = -1;

		for (Object obj : this.lpcData) {
			i++;

			if (i < index)
				continue;

			if (kClass.isInstance(obj) && obj.toString().equals(kString))
				return obj;
		}

		return null;
	}

	@Override
	public int indexOf(Object o) {
		if (o == null)
			return -1;

		return this.indexOf(o, 0);
	}

	public int indexOf(Object o, int index) {
		if (o == null)
			return -1;

		ListIterator<Object> litr = this.listIterator();
		Class<? extends Object> oClass = o.getClass();
		Object obj;
		String oString = o.toString();
		int i = -1;

		while (litr.hasNext()) {
			i++;

			if (i < index)
				continue;

			obj = litr.next();

			if (oClass.isInstance(obj) && obj.toString().equals(oString))
				return i;
		}

		return -1;
	}

	@Override
	public boolean isEmpty() {
		return this.lpcData.isEmpty();
	}

	@Override
	public Iterator<Object> iterator() {
		return this.lpcData.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		if (o == null)
			return -1;

		return this.lastIndexOf(o, this.size() - 1);
	}

	public int lastIndexOf(Object o, int index) {
		if (o == null)
			return -1;

		if (index < 0 || index >= this.size())
			return -1;

		ListIterator<Object> litr = this.listIterator();

		while (litr.hasNext())
			litr.next();

		Class<? extends Object> oClass = o.getClass();
		String oString = o.toString();
		int i = this.size();

		while (litr.hasPrevious()) {
			i--;

			if (i > index)
				continue;

			Object obj = litr.previous();

			if (oClass.isInstance(obj) && obj.toString().equals(oString))
				return i;
		}

		return -1;
	}

	@Override
	public ListIterator<Object> listIterator() {
		return this.lpcData.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(int index) {
		return this.lpcData.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		if (o == null)
			return false;

		int index = this.indexOf(o);

		if (index == -1)
			return false;

		this.remove(index);

		return true;
	}

	@Override
	public Object remove(int index) {
		return this.lpcData.remove(index);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return this.lpcData.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return this.lpcData.retainAll(c);
	}

	@Override
	public Object set(int index, Object lpcData) {
		return this.lpcData.set(index, lpcData);
	}

	@Override
	public Object set(Object index, Object lpcData) throws I3Exception {
		if (index == null)
			return null;

		int ind = Integer.class.cast(index);

		if (ind < 0 || ind >= this.size())
			throw new I3Exception(
					"Index out of range for set(index, lpcdata) in LPCArray: "
							+ index);

		return this.set(ind, lpcData);
	}

	@Override
	public void setLPCData(LPCArray obj) {
		this.lpcData.clear();
		this.lpcData.addAll(obj.getLPCData());
	}

	@Override
	public void setLPCData(LPCInt obj) {
		this.lpcData.clear();
		this.lpcData.add(obj);
	}

	@Override
	public void setLPCData(LPCMapping obj) {
		this.lpcData.clear();
		this.lpcData.add(obj);
	}

	@Override
	public void setLPCData(LPCString obj) {
		this.lpcData.clear();
		this.lpcData.add(obj);
	}

	@Override
	public void setLPCData(Object obj) throws I3Exception {
		if (LPCVar.isLPCArray(obj)) {
			this.lpcData.clear();
			this.lpcData.addAll(((LPCArray) obj).getLPCData());
		} else if (LPCVar.isLPCVar(obj)) {
			this.lpcData.clear();
			this.lpcData.add(obj);
		} else {
			throw new I3Exception(
					"Invalid data for LPCArray: setLPCData(Object) '"
							+ obj.toString() + "'");
		}
	}

	@Override
	public int size() {
		return this.lpcData.size();
	}

	@Override
	public List<Object> subList(int fromIndex, int toIndex) {
		return this.lpcData.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return this.lpcData.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return this.lpcData.toArray(a);
	}

	@Override
	public String toString() {
		return this.lpcData.toString();
	}
}

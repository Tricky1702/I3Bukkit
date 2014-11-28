package uk.org.rockthehalo.intermud3.LPC;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Vector;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Utils;

public class LPCArray extends LPCVar implements List<Object> {
	private Vector<Object> lpcData = new Vector<Object>(32);

	public LPCArray() {
		super.setType(LPCTypes.ARRAY);
	}

	public LPCArray(final int size) {
		this();
		this.lpcData = new Vector<Object>(Utils.nullList(size));
	}

	public LPCArray(final Vector<Object> vec) {
		this();
		this.lpcData = new Vector<Object>(vec.size());
		addAll(vec);
	}

	public LPCArray(final Set<Object> set) {
		this();
		this.lpcData = new Vector<Object>(set.size());
		addAll(set);
	}

	public LPCArray(final List<Object> list) {
		this();
		this.lpcData = new Vector<Object>(list.size());

		for (final Object o : list) {
			if (Utils.isLPCVar(o))
				this.add(o);
			else if (String.class.isInstance(o))
				this.add(new LPCString((String) o));
			else if (Integer.class.isInstance(o))
				this.add(new LPCInt((Integer) o));
			else if (Long.class.isInstance(o))
				this.add(new LPCInt((Long) o));
			else
				this.add(new LPCMixed(o));
		}
	}

	public LPCArray(final LPCArray o) {
		this(o.getLPCData());
	}

	public boolean add(final LPCArray o) {
		return this.lpcData.add(o);
	}

	@Override
	public boolean add(final Object o) {
		return this.lpcData.add(o);
	}

	@Override
	public void add(final int index, final Object element) {
		this.lpcData.add(index, element);
	}

	@Override
	public boolean addAll(final Collection<? extends Object> c) {
		return this.lpcData.addAll(c);
	}

	@Override
	public boolean addAll(final int index, final Collection<? extends Object> c) {
		return this.lpcData.addAll(index, c);
	}

	@Override
	public void clear() {
		this.lpcData.clear();
	}

	@Override
	public LPCArray clone() {
		return new LPCArray(this.lpcData);
	}

	@Override
	public boolean contains(final Object o) {
		if (o == null)
			return false;

		if (String.class.isInstance(o))
			return getValue(new LPCString((String) o)) != null;
		else if (Number.class.isInstance(o))
			return getValue(new LPCInt((Number) o)) != null;
		else if (Utils.isLPCVar(o))
			return getValue(o) != null;

		return false;
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		return this.lpcData.containsAll(c);
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;

		if (Utils.isLPCArray(o) && Utils.toMudMode(o).equals(Utils.toMudMode(this)))
			return true;

		return false;
	}

	@Override
	public Object get(final int index) {
		return this.lpcData.get(index);
	}

	@Override
	public Object get(final Object index) {
		if (index == null)
			return null;

		final int i;

		if (Number.class.isInstance(index))
			i = ((Number) index).intValue();
		else
			return null;

		return get(i);
	}

	@Override
	public LPCArray getLPCArray(final Object index) {
		final Object obj = get(index);

		if (Utils.isLPCArray(obj))
			return (LPCArray) obj;

		return null;
	}

	@Override
	public Vector<Object> getLPCData() {
		return this.lpcData;
	}

	@Override
	public LPCInt getLPCInt(final Object index) {
		final Object obj = get(index);

		if (Utils.isLPCInt(obj))
			return (LPCInt) obj;

		return null;
	}

	@Override
	public LPCMapping getLPCMapping(final Object index) {
		final Object obj = get(index);

		if (Utils.isLPCMapping(obj))
			return (LPCMapping) obj;

		return null;
	}

	@Override
	public LPCString getLPCString(final Object index) {
		final Object obj = get(index);

		if (Utils.isLPCString(obj))
			return (LPCString) obj;

		return null;
	}

	public Object getValue(final Object o) {
		return getValue(o, 0);
	}

	public Object getValue(final Object o, final int index) {
		if (o == null || index < 0 || index >= size())
			return null;

		final ListIterator<Object> litr = listIterator(index);
		final Class<? extends Object> oClass = o.getClass();

		while (litr.hasNext()) {
			final Object obj = litr.next();

			if (oClass.isInstance(obj)) {
				switch (LPCVar.getType(obj)) {
				case ARRAY:
					if (((LPCArray) obj).equals(o))
						return obj;

					break;
				case INT:
					if (((LPCInt) obj).equals(o))
						return obj;

					break;
				case MAPPING:
					if (((LPCMapping) obj).equals(o))
						return obj;

					break;
				case MIXED:
					if (((LPCMixed) obj).equals(o))
						return obj;

					break;
				case STRING:
					if (((LPCString) obj).equals(o))
						return obj;

					break;
				default:
					break;
				}
			}
		}

		return null;
	}

	@Override
	public int hashCode() {
		return Utils.toMudMode(this).hashCode();
	}

	@Override
	public int indexOf(final Object o) {
		return indexOf(o, 0);
	}

	public int indexOf(final Object o, int index) {
		if (o == null || index < 0 || index >= size())
			return -1;

		final ListIterator<Object> litr = listIterator(index);
		final Class<? extends Object> oClass = o.getClass();

		while (litr.hasNext()) {
			final Object obj = litr.next();

			if (oClass.isInstance(obj)) {
				switch (LPCVar.getType(obj)) {
				case ARRAY:
					if (((LPCArray) obj).equals(o))
						return index;

					break;
				case INT:
					if (((LPCInt) obj).equals(o))
						return index;

					break;
				case MAPPING:
					if (((LPCMapping) obj).equals(o))
						return index;

					break;
				case MIXED:
					if (((LPCMixed) obj).equals(o))
						return index;

					break;
				case STRING:
					if (((LPCString) obj).equals(o))
						return index;

					break;
				default:
					break;
				}
			}

			++index;
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
	public int lastIndexOf(final Object o) {
		return lastIndexOf(o, this.size());
	}

	public int lastIndexOf(final Object o, int index) {
		if (o == null || index < 0 || index > size())
			return -1;

		final ListIterator<Object> litr = listIterator(index);
		final Class<? extends Object> oClass = o.getClass();

		while (litr.hasPrevious()) {
			final Object obj = litr.previous();

			if (oClass.isInstance(obj)) {
				switch (LPCVar.getType(obj)) {
				case ARRAY:
					if (((LPCArray) obj).equals(o))
						return index;

					break;
				case INT:
					if (((LPCInt) obj).equals(o))
						return index;

					break;
				case MAPPING:
					if (((LPCMapping) obj).equals(o))
						return index;

					break;
				case MIXED:
					if (((LPCMixed) obj).equals(o))
						return index;

					break;
				case STRING:
					if (((LPCString) obj).equals(o))
						return index;

					break;
				default:
					break;
				}
			}

			--index;
		}

		return -1;
	}

	@Override
	public ListIterator<Object> listIterator() {
		return this.lpcData.listIterator();
	}

	@Override
	public ListIterator<Object> listIterator(final int index) {
		return this.lpcData.listIterator(index);
	}

	@Override
	public boolean remove(final Object o) {
		if (o == null)
			return false;

		final int index = indexOf(o);

		if (index == -1)
			return false;

		remove(index);

		return true;
	}

	@Override
	public Object remove(final int index) {
		return this.lpcData.remove(index);
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		return this.lpcData.removeAll(c);
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		return this.lpcData.retainAll(c);
	}

	@Override
	public Object set(final int index, final Object element) {
		return this.lpcData.set(index, element);
	}

	@Override
	public Object set(final Object index, final Object element) throws I3Exception {
		if (index == null)
			return null;

		final int i;

		if (Number.class.isInstance(index))
			i = ((Number) index).intValue();
		else
			throw new I3Exception("Invalid index type for set(index, element) in LPCArray: '" + index + "'");

		if (i < 0 || i >= size())
			throw new I3Exception("Index out of range for set(index, element) in LPCArray: " + index);

		return set(i, element);
	}

	@Override
	public void setLPCData(final LPCArray o) {
		clear();
		addAll(o.getLPCData());
	}

	@Override
	public void setLPCData(final LPCInt o) {
		clear();
		add(o);
	}

	@Override
	public void setLPCData(final LPCMapping o) {
		clear();
		add(o);
	}

	@Override
	public void setLPCData(final LPCString o) {
		clear();
		add(o);
	}

	@Override
	public void setLPCData(final Object o) throws I3Exception {
		if (Utils.isLPCArray(o)) {
			clear();
			addAll(((LPCArray) o).getLPCData());
		} else if (Utils.isLPCVar(o)) {
			clear();
			add(o);
		} else {
			throw new I3Exception("Invalid data for LPCArray: setLPCData(Object) '" + o + "'");
		}
	}

	@Override
	public int size() {
		return this.lpcData.size();
	}

	@Override
	public List<Object> subList(final int fromIndex, final int toIndex) {
		return this.lpcData.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return (this.lpcData.toArray());
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		return this.lpcData.toArray(a);
	}

	@Override
	public String toString() {
		return this.lpcData.toString();
	}
}

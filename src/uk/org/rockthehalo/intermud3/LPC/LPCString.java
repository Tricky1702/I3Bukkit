package uk.org.rockthehalo.intermud3.LPC;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Utils;

public class LPCString extends LPCVar {
	private String lpcData = new String();

	public LPCString() {
		super.setType(LPCTypes.STRING);
	}

	public LPCString(final String o) {
		this();
		this.lpcData = new String(o);
	}

	public LPCString(final LPCString o) {
		this(o.getLPCData());
	}

	@Override
	public boolean add(final Object o) {
		this.lpcData.concat(o.toString());

		return true;
	}

	@Override
	public LPCString clone() {
		return new LPCString(this.lpcData);
	}

	public boolean contains(final Object o) {
		if (o == null)
			return false;

		if (String.class.isInstance(o) && this.lpcData.contains((String) o))
			return true;
		else if (Utils.isLPCString(o)
				&& this.lpcData.contains(((LPCString) o).getLPCData()))
			return true;

		return false;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null)
			return false;

		if (String.class.isInstance(o) && ((String) o).equals(this.lpcData))
			return true;
		else if (Utils.isLPCString(o)
				&& ((LPCString) o).getLPCData().equals(this.lpcData))
			return true;

		return false;
	}

	public boolean equalsIgnoreCase(final Object o) {
		if (o == null)
			return false;

		if (String.class.isInstance(o)
				&& ((String) o).equalsIgnoreCase(this.lpcData))
			return true;
		else if (Utils.isLPCString(o)
				&& ((LPCString) o).getLPCData().equalsIgnoreCase(this.lpcData))
			return true;

		return false;
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

		if (i < 0 || i >= size())
			return null;

		return this.lpcData.charAt(i);
	}

	@Override
	public LPCArray getLPCArray(final Object index) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCString: getLPCArray(index)");
	}

	@Override
	public String getLPCData() {
		return this.lpcData;
	}

	@Override
	public LPCInt getLPCInt(final Object index) throws I3Exception {
		final Object obj = get(index);

		if (obj == null)
			return null;

		final long i;

		if (Number.class.isInstance(obj))
			i = ((Number) obj).longValue();
		else
			return null;

		return new LPCInt(i);
	}

	@Override
	public LPCMapping getLPCMapping(final Object index) throws I3Exception {
		throw new I3Exception(
				"Invalid operation for LPCString: getLPCMapping(index)");
	}

	@Override
	public LPCString getLPCString(final Object index) {
		final Object obj = get(index);

		if (obj == null)
			return null;

		return new LPCString(obj.toString());
	}

	@Override
	public int hashCode() {
		return this.lpcData.hashCode();
	}

	@Override
	public boolean isEmpty() {
		return this.lpcData.isEmpty();
	}

	public int length() {
		return size();
	}

	@Override
	public Object set(final Object index, final Object element)
			throws I3Exception {
		if (index == null || element == null)
			return null;

		final int i;

		if (Number.class.isInstance(index))
			i = ((Number) index).intValue();
		else
			throw new I3Exception(
					"Invalid index type for set(index, element) in LPCString: '"
							+ index + "'");

		if (i < 0 || i >= size())
			throw new I3Exception(
					"Index out of range for set(index, element) in LPCString: "
							+ index);

		final char[] chars = this.lpcData.toCharArray();
		final char oldChar = chars[i];

		chars[i] = element.toString().charAt(0);
		this.lpcData = String.copyValueOf(chars);

		return new LPCString(oldChar + "");
	}

	@Override
	public void setLPCData(final LPCArray o) {
		this.lpcData = new String(Utils.toMudMode(o));
	}

	@Override
	public void setLPCData(final LPCInt o) {
		this.lpcData = new String(Utils.toMudMode(o));
	}

	@Override
	public void setLPCData(final LPCMapping o) {
		this.lpcData = new String(Utils.toMudMode(o));
	}

	@Override
	public void setLPCData(final LPCString o) {
		this.lpcData = new String(Utils.toMudMode(o));
	}

	@Override
	public void setLPCData(final Object o) throws I3Exception {
		if (Utils.isLPCVar(o))
			this.lpcData = new String(Utils.toMudMode(o));
		else
			throw new I3Exception(
					"Invalid data for LPCString: setLPCData(Object) '" + o
							+ "'");
	}

	@Override
	public int size() {
		return this.lpcData.length();
	}

	public String toLowerCase() {
		return this.lpcData.toLowerCase();
	}

	@Override
	public String toString() {
		return this.lpcData;
	}
}

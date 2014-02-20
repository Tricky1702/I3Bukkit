package uk.org.rockthehalo.intermud3;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

public class LPCData {
	private Object mixedData;
	private LPCTypes lpcType;

	public enum LPCTypes {
		NONE, STRING, STRINGARR, STRINGMAP, INT, INTARR, INTMAP, MIXED, MIXEDARR, MIXEDMAP
	}

	public LPCData() {
		mixedData = null;
		lpcType = LPCTypes.NONE;
	}

	public LPCData(int data) {
		mixedData = new Integer(data);
		lpcType = LPCTypes.INT;
	}

	public LPCData(LPCTypes type) {
		switch (type) {
		case STRING:
			mixedData = new String("");

			break;
		case INT:
			mixedData = new Integer(0);

			break;
		case MIXED:
			mixedData = null;

			break;
		case STRINGARR:
			mixedData = new Vector<String>();

			break;
		case INTARR:
			mixedData = new Vector<Integer>();

			break;
		case MIXEDARR:
			mixedData = new Vector<Object>();

			break;
		case STRINGMAP:
			mixedData = new Hashtable<String, Object>();

			break;
		case INTMAP:
			mixedData = new Hashtable<Integer, Object>();

			break;
		case MIXEDMAP:
			mixedData = new Hashtable<Object, Object>();

			break;
		default:
			mixedData = null;

			break;
		}

		lpcType = type;
	}

	public LPCData(Object data) {
		if (data instanceof String) {
			mixedData = new String((String) data);
			lpcType = LPCTypes.STRING;
		} else if (data instanceof Integer) {
			mixedData = new Integer((Integer) data);
			lpcType = LPCTypes.INT;
		} else if (data instanceof Vector) {
			Vector<?> tmp = (Vector<?>) data;

			if (tmp.isEmpty()) {
				mixedData = new Vector<Object>();
				lpcType = LPCTypes.MIXEDARR;
			} else {
				int strs = 0, ints = 0, mixed = 0;

				for (Object obj : tmp) {
					if (obj instanceof String)
						strs++;
					else if (obj instanceof Integer)
						ints++;
					else
						mixed++;
				}

				if ((strs > 0 && ints > 0) || mixed > 0) {
					mixedData = (Vector<?>) tmp.clone();
					lpcType = LPCTypes.MIXEDARR;
				} else if (strs > 0 && ints == 0 && mixed == 0) {
					mixedData = (Vector<?>) tmp.clone();
					lpcType = LPCTypes.STRINGARR;
				} else if (strs == 0 && ints > 0 && mixed == 0) {
					mixedData = (Vector<?>) tmp.clone();
					lpcType = LPCTypes.INTARR;
				}
			}
		} else if (data instanceof Hashtable) {
			Hashtable<?, ?> tmp = (Hashtable<?, ?>) data;

			if (tmp.isEmpty()) {
				mixedData = new Hashtable<Object, Object>();
				lpcType = LPCTypes.MIXEDMAP;
			} else {
				Set<?> keys = tmp.keySet();
				int strs = 0, ints = 0, mixed = 0;

				for (Object key : keys) {
					if (key instanceof String)
						strs++;
					else if (key instanceof Integer)
						ints++;
					else
						mixed++;
				}

				if ((strs > 0 && ints > 0) || mixed > 0) {
					mixedData = (Hashtable<?, ?>) tmp.clone();
					lpcType = LPCTypes.MIXEDMAP;
				} else if (strs > 0 && ints == 0 && mixed == 0) {
					mixedData = (Hashtable<?, ?>) tmp.clone();
					lpcType = LPCTypes.STRINGMAP;
				} else if (strs == 0 && ints > 0 && mixed == 0) {
					mixedData = (Hashtable<?, ?>) tmp.clone();
					lpcType = LPCTypes.INTMAP;
				}
			}
		} else {
			mixedData = data;
			lpcType = LPCTypes.MIXED;
		}
	}

	/**
	 * @return
	 */
	public int size() {
		switch (lpcType) {
		case NONE:
			return 0;
		case STRING:
			return ((String) mixedData).length();
		case STRINGARR:
		case INTARR:
		case MIXEDARR:
			return ((Vector<?>) mixedData).size();
		case STRINGMAP:
		case INTMAP:
		case MIXEDMAP:
			return ((Hashtable<?, ?>) mixedData).size();
		default:
			return 1;
		}
	}

	public int getInt() {
		if (lpcType != LPCTypes.INT)
			return -1;

		return Integer.parseInt(mixedData.toString());
	}

	/**
	 * @return the LPCData
	 */
	public Object get() {
		switch (lpcType) {
		case STRING:
			return mixedData.toString();
		case INT:
			return Integer.parseInt(mixedData.toString());
		case MIXED:
			return mixedData;
		case STRINGARR:
		case INTARR:
		case MIXEDARR:
			return mixedData;
		case STRINGMAP:
		case INTMAP:
		case MIXEDMAP:
			return mixedData;
		default:
			return null;
		}
	}

	/**
	 * @param index
	 *            the index of the LPCData to get
	 * @return object at the specified index
	 * @throws I3Exception
	 */
	public Object get(int index) throws I3Exception {
		switch (lpcType) {
		case STRINGARR:
		case INTARR:
		case MIXEDARR:
			return ((Vector<?>) mixedData).get(index);
		default:
			throw new I3Exception("Invalid type for LPCData getter: " + lpcType);
		}
	}

	/**
	 * Sets the LPCData.
	 * 
	 * @param data
	 *            the LPCData to set
	 */
	public void set(Object data) {
		if (data instanceof String) {
			mixedData = new String((String) data);
			lpcType = LPCTypes.STRING;
		} else if (data instanceof Integer) {
			mixedData = new Integer((Integer) data);
			lpcType = LPCTypes.INT;
		} else if (data instanceof Vector) {
			Vector<?> tmp = (Vector<?>) data;

			if (tmp.isEmpty()) {
				mixedData = new Vector<Object>();
				lpcType = LPCTypes.MIXEDARR;
			} else {
				int strs = 0, ints = 0, mixed = 0;

				for (Object obj : tmp) {
					if (obj instanceof String)
						strs++;
					else if (obj instanceof Integer)
						ints++;
					else
						mixed++;
				}

				if ((strs > 0 && ints > 0) || mixed > 0) {
					mixedData = (Vector<?>) tmp.clone();
					lpcType = LPCTypes.MIXEDARR;
				} else if (strs > 0 && ints == 0 && mixed == 0) {
					mixedData = (Vector<?>) tmp.clone();
					lpcType = LPCTypes.STRINGARR;
				} else if (strs == 0 && ints > 0 && mixed == 0) {
					mixedData = (Vector<?>) tmp.clone();
					lpcType = LPCTypes.INTARR;
				}
			}
		} else if (data instanceof Hashtable) {
			Hashtable<?, ?> tmp = (Hashtable<?, ?>) data;

			if (tmp.isEmpty()) {
				mixedData = new Hashtable<Object, Object>();
				lpcType = LPCTypes.MIXEDMAP;
			} else {
				Set<?> keys = tmp.keySet();
				int strs = 0, ints = 0, mixed = 0;

				for (Object key : keys) {
					if (key instanceof String)
						strs++;
					else if (key instanceof Integer)
						ints++;
					else
						mixed++;
				}

				if ((strs > 0 && ints > 0) || mixed > 0) {
					mixedData = (Hashtable<?, ?>) tmp.clone();
					lpcType = LPCTypes.MIXEDMAP;
				} else if (strs > 0 && ints == 0 && mixed == 0) {
					mixedData = (Hashtable<?, ?>) tmp.clone();
					lpcType = LPCTypes.STRINGMAP;
				} else if (strs == 0 && ints > 0 && mixed == 0) {
					mixedData = (Hashtable<?, ?>) tmp.clone();
					lpcType = LPCTypes.INTMAP;
				}
			}
		} else {
			mixedData = data;
			lpcType = LPCTypes.MIXED;
		}
	}

	/**
	 * Sets the LPCData at the specified index.
	 * 
	 * @param index
	 *            the index
	 * @param data
	 *            the LPCData to set at index
	 * @throws I3Exception
	 */
	@SuppressWarnings("unchecked")
	public void set(int index, Object data) throws I3Exception {
		switch (lpcType) {
		case STRINGARR:
		case INTARR:
		case MIXEDARR:
			((Vector<Object>) mixedData).set(index, data);

			break;
		default:
			throw new I3Exception("Invalid type for LPCData setter: " + lpcType);
		}
	}

	/**
	 * @param data
	 *            the LPCData to add
	 * @throws I3Exception
	 */
	@SuppressWarnings("unchecked")
	public void add(Object data) throws I3Exception {
		switch (lpcType) {
		case STRINGARR:
		case INTARR:
		case MIXEDARR:
			((Vector<Object>) mixedData).add(data);

			break;
		default:
			throw new I3Exception("Invalid type for LPCData adder: " + lpcType);
		}
	}

	private String p_toMudMode(Object obj) {
		if (obj instanceof Hashtable) {
			Set<?> keySet = ((Hashtable<?, ?>) obj).keySet();
			Vector<String> list = new Vector<String>(keySet.size());

			for (Object key : keySet) {
				Object value = ((Hashtable<?, ?>) obj).get(key);

				list.add(p_toMudMode(key) + ":" + p_toMudMode(value));
			}

			if (list.isEmpty())
				return "([])";
			else
				return "([" + StringUtils.join(list, ",") + ",])";
		} else if (obj instanceof Vector) {
			Vector<?> objs = (Vector<?>) obj;
			Vector<String> list = new Vector<String>(objs.size());

			for (Object subObj : objs)
				list.add(p_toMudMode(subObj));

			if (list.isEmpty())
				return "({})";
			else
				return "({" + StringUtils.join(list, ",") + ",})";
		} else if (obj instanceof String) {
			String str = obj.toString();

			str = str.replace("\"", "\\\"");
			str = "\"" + str + "\"";
			str = str.replace("\\", "\\\\");
			str = str.replace("\\\"", "\"");
			str = str.replace("\n", "\\n");
			str = str.replace("\r", "");
			str = str.replace("\t", "\\t");
			str = str.replace("\b", "\\b");
			str = str.replace("\u00A0", " ");

			return str;
		} else {
			if (obj == null)
				return "0";

			return obj.toString();
		}
	}

	/**
	 * Convert LPCData to a format suitable for transmitting as MudMode.
	 * 
	 * @return the MudMode string
	 */
	public String toMudMode() {
		return p_toMudMode(mixedData);
	}

	private String replaceAndIngnoreInStrings(String str, String target,
			String replacement) {
		StringBuffer in = new StringBuffer("");
		int x = 0;

		if (!str.contains("\""))
			return str.replace(target, replacement);

		while (x < str.length()) {
			char c = str.charAt(x);

			if (c == '\\') {
				if (x + 1 < str.length()) {
					in.append("\\" + str.charAt(x + 1));
					x++;
				} else {
					in.append(c);
				}

				x++;
			} else if (c == '"') {
				x++;

				if (x == str.length()) {
					in.append(c);
				} else {
					StringBuffer tmp = new StringBuffer("\"");

					while (x < str.length()) {
						c = str.charAt(x);
						tmp.append(c);
						x++;

						if (c == '"')
							break;
					}

					in.append(tmp);
				}
			} else {
				if (x + target.length() <= str.length()) {
					if (str.substring(x, x + target.length()).equals(target)) {
						in.append(replacement);
						x += target.length();
					} else {
						in.append(c);
						x++;
					}
				} else {
					in.append(c);
					x++;
				}
			}
		}

		return in.toString();
	}

	private String blankFromStrings(String str, String target) {
		StringBuffer in = new StringBuffer("");
		int x = 0;

		if (!str.contains("\""))
			return str;

		while (x < str.length()) {
			char c = str.charAt(x);

			if (c == '\\') {
				if (x + 1 < str.length()) {
					in.append("\\" + str.charAt(x + 1));
					x++;
				} else {
					in.append(c);
				}

				x++;
			} else if (c == '"') {
				x++;

				if (x == str.length()) {
					in.append(c);
				} else {
					StringBuffer tmpIn = new StringBuffer("\"");

					while (x < str.length()) {
						c = str.charAt(x);

						if (c == '\\') {
							if (x + 1 < str.length()) {
								tmpIn.append("\\" + str.charAt(x + 1));
								x++;
							} else {
								tmpIn.append(c);
							}

							x++;
						} else if (x + target.length() < str.length()
								&& str.substring(x, x + target.length())
										.equals(target)) {
							for (int i = 0; i < target.length(); i++)
								tmpIn.append(" ");

							x += target.length();
						} else {
							tmpIn.append(c);
							x++;

							if (c == '"')
								break;
						}
					}

					in.append(tmpIn);
				}
			} else {
				in.append(c);
				x++;
			}
		}

		return in.toString();
	}

	private int scanForward(String str, String leftTarget, String rightTarget) {
		int left = 0, right = str.indexOf(rightTarget);

		while (left < right) {
			left = str.indexOf(leftTarget, left + leftTarget.length());

			if (left == -1)
				break;

			if (left < right)
				right = str.indexOf(rightTarget, right + rightTarget.length());
		}

		return right;
	}

	private Object p_fromMudMode(String mudModeString) throws I3Exception {
		if (mudModeString == null)
			return null;

		mudModeString = mudModeString.trim();

		if (mudModeString.length() < 1) {
			return null;
		} else if (mudModeString.length() == 1) {
			try {
				int x = Integer.parseInt(mudModeString);

				return Integer.valueOf(x);
			} catch (NumberFormatException e) {
				throw new I3Exception("Invalid LPC Data in string: "
						+ mudModeString);
			}
		}

		if (mudModeString.charAt(0) == '(') {
			if (mudModeString.charAt(1) == '{') {
				Vector<Object> array = new Vector<Object>();
				String tmp = mudModeString;

				if (!mudModeString.contains("})"))
					throw new I3Exception("Invalid array format: "
							+ mudModeString);

				tmp = blankFromStrings(tmp, "({");
				tmp = blankFromStrings(tmp, "})");
				tmp = mudModeString = mudModeString.substring(2,
						scanForward(tmp, "({", "})")).trim();
				tmp = blankFromStrings(tmp, "({");
				tmp = blankFromStrings(tmp, "})");
				tmp = blankFromStrings(tmp, "([");
				tmp = blankFromStrings(tmp, "])");
				tmp = blankFromStrings(tmp, ",");
				tmp = blankFromStrings(tmp, ":");

				while (mudModeString.length() > 0) {
					String value = "";
					int next = -1;

					if (tmp.charAt(0) == '(') {
						if (tmp.charAt(1) == '{') {
							next = scanForward(tmp, "({", "})") + 2;
						} else if (tmp.charAt(1) == '[') {
							next = scanForward(tmp, "([", "])") + 2;
						}
					} else {
						next = tmp.indexOf(',');
					}

					if (next < 0 || next >= mudModeString.length())
						value = mudModeString.trim();
					else
						value = mudModeString.substring(0, next).trim();

					array.add(p_fromMudMode(value));

					if (next < 0 || next >= mudModeString.length()) {
						mudModeString = "";
					} else {
						mudModeString = mudModeString.substring(next + 1)
								.trim();
						tmp = tmp.substring(next + 1).trim();
					}
				}

				return array;
			} else if (mudModeString.charAt(1) == '[') {
				Hashtable<Object, Object> mapping = new Hashtable<Object, Object>();
				String tmp = mudModeString;

				if (!mudModeString.contains("])")
						&& !mudModeString.contains(":"))
					throw new I3Exception("Invalid mapping format: "
							+ mudModeString);

				tmp = blankFromStrings(tmp, "([");
				tmp = blankFromStrings(tmp, "])");
				tmp = mudModeString = mudModeString.substring(2,
						scanForward(tmp, "([", "])")).trim();
				tmp = blankFromStrings(tmp, "({");
				tmp = blankFromStrings(tmp, "})");
				tmp = blankFromStrings(tmp, "([");
				tmp = blankFromStrings(tmp, "])");
				tmp = blankFromStrings(tmp, ":");
				tmp = blankFromStrings(tmp, ",");

				while (mudModeString.length() > 0) {
					String[] kv = { "", "" };
					char delim;
					int next = -1;

					for (int i = 0; i < 2; i++) {
						delim = i == 0 ? ':' : ',';

						if (tmp.charAt(0) == '(') {
							if (tmp.charAt(1) == '{') {
								next = scanForward(tmp, "({", "})") + 2;
							} else if (tmp.charAt(1) == '[') {
								next = scanForward(tmp, "([", "])") + 2;
							}
						} else {
							next = tmp.indexOf(delim);
						}

						if (next < 0 || next >= mudModeString.length()) {
							kv[i] = mudModeString.trim();
							mudModeString = "";
						} else {
							kv[i] = mudModeString.substring(0, next).trim();
							mudModeString = mudModeString.substring(next + 1)
									.trim();
							tmp = tmp.substring(next + 1).trim();
						}
					}

					mapping.put(p_fromMudMode(kv[0]), p_fromMudMode(kv[1]));
				}

				return mapping;
			} else {
				throw new I3Exception("Invalid LPC Data in string: "
						+ mudModeString);
			}
		} else if (mudModeString.charAt(0) == '"') {
			StringBuffer in = new StringBuffer("");
			int x = 1;
			char c = '\0';

			while (x < mudModeString.length()) {
				c = mudModeString.charAt(x);

				switch (mudModeString.charAt(x)) {
				case '\\':
					if ((x + 1) < mudModeString.length()) {
						in.append(mudModeString.charAt(x + 1));
						x++;
					} else {
						in.append(c);
					}

					x++;

					break;
				case '"':
					return in.toString();
				default:
					in.append(c);
					x++;

					break;
				}
			}

			return in.toString();
		} else if (Character.isDigit(mudModeString.charAt(0))
				|| mudModeString.charAt(0) == '-') {
			String tmp;
			int x = 0;

			if (mudModeString.length() > 1 && mudModeString.startsWith("0x")) {
				tmp = "0x";
				mudModeString = mudModeString.substring(2,
						mudModeString.length());
			} else if (mudModeString.length() > 1
					&& mudModeString.startsWith("-")) {
				tmp = "-";
				mudModeString = mudModeString.substring(1,
						mudModeString.length());
			} else {
				tmp = "";
			}

			while (!mudModeString.equals("")
					&& (Character.isDigit(mudModeString.charAt(0)))) {
				tmp += mudModeString.charAt(0);

				if (mudModeString.length() > 1)
					mudModeString = mudModeString.substring(1,
							mudModeString.length());
				else
					mudModeString = "";
			}

			try {
				x = Integer.parseInt(tmp);
			} catch (NumberFormatException e) {
				throw new I3Exception("Invalid number format: " + tmp);
			}

			return Integer.valueOf(x);
		}

		throw new I3Exception("Invalid MudMode string: " + mudModeString);
	}

	/**
	 * Converts a MudMode string into a LPCData element.
	 * 
	 * @param mudModeString
	 *            the MudMode string to convert
	 * @throws I3Exception
	 */
	public void fromMudMode(String mudModeString) throws I3Exception {
		mudModeString = replaceAndIngnoreInStrings(mudModeString, ",})", "})");
		mudModeString = replaceAndIngnoreInStrings(mudModeString, ",])", "])");
		set(p_fromMudMode(mudModeString));
	}
}

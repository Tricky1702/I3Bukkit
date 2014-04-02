package uk.org.rockthehalo.intermud3.LPC;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Intermud3;

public abstract class LPCVar {
	public enum LPCTypes {
		ARRAY, INT, MAPPING, MIXED, STRING;
	}

	private LPCTypes lpcType = LPCTypes.MIXED;

	public LPCVar() {
	}

	public LPCTypes getType() {
		return this.lpcType;
	}

	public static LPCTypes getType(Object lpcData) {
		if (LPCVar.isLPCArray(lpcData))
			return LPCTypes.ARRAY;

		if (LPCVar.isLPCInt(lpcData))
			return LPCTypes.INT;

		if (LPCVar.isLPCMapping(lpcData))
			return LPCTypes.MAPPING;

		if (LPCVar.isLPCString(lpcData))
			return LPCTypes.STRING;

		return LPCTypes.MIXED;
	}

	public void setType(LPCTypes lpcType) {
		this.lpcType = lpcType;
	}

	public static boolean isLPCArray(Object obj) {
		return LPCArray.class.isInstance(obj);
	}

	public static boolean isLPCInt(Object obj) {
		return LPCInt.class.isInstance(obj);
	}

	public static boolean isLPCMapping(Object obj) {
		return LPCMapping.class.isInstance(obj);
	}

	public static boolean isLPCMixed(Object obj) {
		return LPCMixed.class.isInstance(obj);
	}

	public static boolean isLPCString(Object obj) {
		return LPCString.class.isInstance(obj);
	}

	public static boolean isLPCVar(Object obj) {
		return isLPCArray(obj) || isLPCInt(obj) || isLPCMapping(obj)
				|| isLPCMixed(obj) || isLPCString(obj);
	}

	public static String toMudMode(Object obj) {
		if (obj == null)
			return "0";

		switch (getType(obj)) {
		case ARRAY: {
			Iterator<Object> itr = ((LPCArray) obj).iterator();
			Vector<String> list = new Vector<String>();

			while (itr.hasNext()) {
				Object next = itr.next();

				list.add(toMudMode(next));
			}

			if (list.isEmpty())
				return "({})";
			else
				return "({" + StringUtils.join(list, ",") + ",})";
		}
		case INT:
			return ((LPCInt) obj).toString();
		case MAPPING: {
			Set<?> keySet = ((LPCMapping) obj).keySet();
			Vector<String> list = new Vector<String>();

			for (Object key : keySet)
				list.add(toMudMode(key) + ":"
						+ toMudMode(((LPCMapping) obj).get(key)));

			if (list.isEmpty())
				return "([])";
			else
				return "([" + StringUtils.join(list, ",") + ",])";
		}
		case STRING: {
			String str = ((LPCString) obj).toString();

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
		}
		default:
			return obj.toString();
		}
	}

	private static String replaceAndIngnoreInStrings(String str, String target,
			String replacement) {
		StringBuffer in = new StringBuffer("");
		int x = 0;

		if (!str.contains("\""))
			return str.replace(target, replacement);

		while (x < str.length()) {
			char c = str.charAt(x);

			if (c == '\\') {
				if (x + 1 < str.length()) {
					in.append("\\");
					c = str.charAt(++x);
				}

				in.append(c);
				x++;
			} else if (c == '"') {
				x++;

				if (x == str.length()) {
					in.append(c);
				} else {
					StringBuffer tmp = new StringBuffer("\"");

					while (x < str.length()) {
						c = str.charAt(x++);
						tmp.append(c);

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

	private static String blankFromStrings(String str, String target) {
		StringBuffer in = new StringBuffer("");
		int x = 0;

		if (!str.contains("\""))
			return str;

		while (x < str.length()) {
			char c = str.charAt(x);

			if (c == '\\') {
				if (x + 1 < str.length()) {
					in.append("\\");
					c = str.charAt(++x);
				}

				in.append(c);
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
								tmpIn.append("\\");
								c = str.charAt(++x);
							}

							tmpIn.append(c);
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

	private static String blankFromStrings(String str, String[] targets) {
		for (String target : targets)
			str = blankFromStrings(str, target);

		return str;
	}

	private static int scanForward(String str, String leftTarget,
			String rightTarget) {
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

	private static Object p_fromMudMode(String mudModeString)
			throws I3Exception {
		if (mudModeString == null)
			return null;

		mudModeString = mudModeString.trim();

		if (mudModeString.length() < 1) {
			return null;
		} else if (mudModeString.length() == 1) {
			try {
				int x = Integer.parseInt(mudModeString);

				return new LPCInt(Integer.valueOf(x));
			} catch (NumberFormatException nfE) {
				throw new I3Exception("Invalid LPC Data in string: "
						+ mudModeString, nfE);
			}
		}

		if (mudModeString.charAt(0) == '(') {
			if (mudModeString.charAt(1) == '{') {
				LPCArray array = new LPCArray();
				String tmp = mudModeString;

				if (!mudModeString.contains("})"))
					throw new I3Exception("Invalid array format: "
							+ mudModeString);

				tmp = blankFromStrings(tmp, new String[] { "({", "})" });
				tmp = mudModeString = mudModeString.substring(2,
						scanForward(tmp, "({", "})")).trim();
				tmp = blankFromStrings(tmp, new String[] { "({", "})", "([",
						"])", ":", "," });

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

					if (next < 0 || next >= mudModeString.length()) {
						value = mudModeString.trim();
						mudModeString = "";
					} else {
						value = mudModeString.substring(0, next).trim();
						mudModeString = mudModeString.substring(next + 1)
								.trim();
						tmp = tmp.substring(next + 1).trim();
					}

					array.add(p_fromMudMode(value));
				}

				return array;
			} else if (mudModeString.charAt(1) == '[') {
				LPCMapping mapping = new LPCMapping();
				String tmp = mudModeString;

				if (!mudModeString.contains("])"))
					throw new I3Exception("Invalid mapping format: "
							+ mudModeString);

				tmp = blankFromStrings(tmp, new String[] { "([", "])" });
				tmp = mudModeString = mudModeString.substring(2,
						scanForward(tmp, "([", "])")).trim();
				tmp = blankFromStrings(tmp, new String[] { "({", "})", "([",
						"])", ":", "," });

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

					mapping.set(p_fromMudMode(kv[0]), p_fromMudMode(kv[1]));
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

				switch (c) {
				case '\\':
					if ((x + 1) < mudModeString.length()) {
						c = mudModeString.charAt(++x);
					}

					in.append(c);
					x++;

					break;
				case '"':
					return new LPCString(in.toString());
				default:
					in.append(c);
					x++;

					break;
				}
			}

			return new LPCString(in.toString());
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
			} catch (NumberFormatException nfE) {
				throw new I3Exception("Invalid number format: " + tmp, nfE);
			}

			return new LPCInt(Integer.valueOf(x));
		}

		throw new I3Exception("Invalid MudMode string: " + mudModeString);
	}

	/**
	 * Converts a MudMode string into a LPC variable.
	 * 
	 * @param mudModeString
	 *            the MudMode string to convert
	 */
	public static Object toObject(String mudModeString) {
		mudModeString = replaceAndIngnoreInStrings(mudModeString, ",})", "})");
		mudModeString = replaceAndIngnoreInStrings(mudModeString, ",])", "])");

		Object obj = null;

		try {
			obj = p_fromMudMode(mudModeString);
		} catch (I3Exception i3E) {
			Intermud3.instance.logError("", i3E);
		}

		return obj;
	}

	public abstract boolean add(Object lpcData) throws I3Exception;

	public abstract Object get(Object index) throws I3Exception;

	public abstract LPCArray getLPCArray(Object index) throws I3Exception;

	public abstract Object getLPCData();

	public abstract LPCInt getLPCInt(Object index) throws I3Exception;

	public abstract LPCMapping getLPCMapping(Object index) throws I3Exception;

	public abstract LPCString getLPCString(Object index) throws I3Exception;

	public abstract boolean isEmpty();

	public abstract Object set(Object index, Object lpcData) throws I3Exception;

	public abstract void setLPCData(LPCArray obj) throws I3Exception;

	public abstract void setLPCData(LPCInt obj) throws I3Exception;

	public abstract void setLPCData(LPCMapping obj) throws I3Exception;

	public abstract void setLPCData(LPCString obj) throws I3Exception;

	public abstract void setLPCData(Object obj) throws I3Exception;

	public abstract int size();

	@Override
	public abstract String toString();
}

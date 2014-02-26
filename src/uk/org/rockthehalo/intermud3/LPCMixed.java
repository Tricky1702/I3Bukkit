package uk.org.rockthehalo.intermud3;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public abstract class LPCMixed {
	private LPCTypes lpcType = LPCTypes.MIXED;

	public enum LPCTypes {
		MIXED("mixed"), STRING("string"), INT("int"), ARRAY("array"), MAPPING(
				"mapping");

		private static Map<String, LPCTypes> nameToType;
		private String name;

		private LPCTypes(String typeName) {
			this.name = typeName;
		}

		public String getName() {
			return this.name;
		}

		public LPCTypes getType(String typeName) {
			if (nameToType == null) {
				initMapping();
			}

			return nameToType.get(typeName);
		}

		private static void initMapping() {
			nameToType = new HashMap<String, LPCTypes>();

			for (LPCTypes type : values()) {
				nameToType.put(type.name, type);
			}
		}
	}

	public LPCTypes getType() {
		return lpcType;
	}

	public void setType(LPCTypes lpcType) {
		this.lpcType = lpcType;
	}

	public abstract LPCString getString(Object index) throws I3Exception;

	public abstract LPCInt getInt(Object index) throws I3Exception;

	public abstract LPCArray getArray(Object index) throws I3Exception;

	public abstract LPCMapping getMapping(Object index) throws I3Exception;

	public abstract Player getPlayer(Object index) throws I3Exception;
}

package uk.org.rockthehalo.intermud3.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import uk.org.rockthehalo.intermud3.Config;
import uk.org.rockthehalo.intermud3.I3Exception;
import uk.org.rockthehalo.intermud3.Intermud3;
import uk.org.rockthehalo.intermud3.Log;
import uk.org.rockthehalo.intermud3.Packet;
import uk.org.rockthehalo.intermud3.PacketTypes.PacketType;
import uk.org.rockthehalo.intermud3.Payload;
import uk.org.rockthehalo.intermud3.Utils;
import uk.org.rockthehalo.intermud3.LPC.LPCArray;
import uk.org.rockthehalo.intermud3.LPC.LPCInt;
import uk.org.rockthehalo.intermud3.LPC.LPCMapping;
import uk.org.rockthehalo.intermud3.LPC.LPCString;

public class I3UCache extends ServiceTemplate implements Listener {
	public enum Gender {
		MALE(0, "male"), FEMALE(1, "female"), NEUTER(2, "neuter"), NEUTRAL(3,
				"neutral");

		private static Map<Integer, Gender> numToGender = null;
		private static Map<String, Gender> nameToGender = null;
		private int gender = -1;
		private String name = null;

		private Gender(int gender, String name) {
			this.gender = gender;
			this.name = name;
		}

		public int getGender() {
			return this.gender;
		}

		public String getName() {
			return this.name;
		}

		public static Gender getNumGender(int gender) {
			if (Gender.numToGender == null) {
				Gender.initMapping();
			}

			return Gender.numToGender.get(gender);
		}

		public static Gender getNamedGender(String name) {
			if (Gender.nameToGender == null) {
				Gender.initMapping();
			}

			return Gender.nameToGender.get(name.toLowerCase());
		}

		private static void initMapping() {
			Gender.numToGender = new ConcurrentHashMap<Integer, Gender>(
					Gender.values().length);
			Gender.nameToGender = new ConcurrentHashMap<String, Gender>(
					Gender.values().length);

			for (Gender g : Gender.values()) {
				Gender.numToGender.put(g.gender, g);
				Gender.nameToGender.put(g.name, g);
			}
		}
	}

	private static final int hBeatDelay = 60;
	private static final LPCArray shadows = new LPCArray(Arrays.asList(
			"apresence", "ashadow", "aninvisibleimmortal", "someone"));
	private static final Payload ucachePayload = new Payload(Arrays.asList(
			"UC_USERNAME", "UC_VISNAME", "UC_GENDER"));
	private static final UUID zUuid = UUID
			.fromString("00000000-0000-0000-0000-000000000000");

	public static final int VISNAME = 0;
	public static final int GENDER = 1;
	public static final int LASTUPDATE = 2;
	public static final int LASTACTIVE = 3;

	public static final int UCACHESIZE = 4;

	// Extra fields for local users.
	public static final int TUNEIN = 4;
	public static final int ALIASES = 5;

	public static final int USERSSIZE = 6;

	private Config config = null;
	private LPCMapping i3UserCache = new LPCMapping();
	private Map<String, UUID> localUUIDs = new ConcurrentHashMap<String, UUID>();
	private Map<UUID, List<Object>> users = new ConcurrentHashMap<UUID, List<Object>>();

	public I3UCache() {
	}

	/**
	 * @param mudname
	 * @param username
	 * @param visname
	 * @param gender
	 */
	public void addUserCache(LPCString mudname, LPCString username,
			LPCString visname, LPCInt gender) {
		addUserCache(mudname.toString(), username.toString(),
				visname.toString(), gender.toInt(), false);
	}

	/**
	 * @param mudname
	 * @param username
	 * @param visname
	 * @param gender
	 */
	public void addUserCache(String mudname, String username, String visname,
			int gender) {
		addUserCache(mudname, username, visname, gender, false);
	}

	/**
	 * @param mudname
	 * @param username
	 * @param visname
	 * @param gender
	 * @param newuser
	 */
	public void addUserCache(LPCString mudname, LPCString username,
			LPCString visname, LPCInt gender, boolean newuser) {
		addUserCache(mudname.toString(), username.toString(),
				visname.toString(), gender.toInt(), newuser);
	}

	/**
	 * @param mudname
	 * @param username
	 * @param visname
	 * @param gender
	 * @param newuser
	 */
	public void addUserCache(String mudname, String username, String visname,
			int gender, boolean newuser) {
		if (mudname == null || mudname.isEmpty())
			return;

		if (username == null || username.isEmpty())
			return;
		else
			username = Utils.stripColor(username);

		if (visname == null || visname.isEmpty())
			return;

		Log.debug("Adding '" + username + "@" + mudname + "' [" + visname + "/"
				+ getGenderString(gender) + "]");

		int time = (int) (System.currentTimeMillis() / 1000);
		int lastUpdate = time;
		int lastActive = time;

		if (newuser)
			lastUpdate = -time;

		if (!mudname.equals(Utils.getServerName())) {
			LPCArray data = new LPCArray(UCACHESIZE);

			data.set(VISNAME, new LPCString(visname));
			data.set(GENDER, new LPCInt(gender));
			data.set(LASTUPDATE, new LPCInt(lastUpdate));
			data.set(LASTACTIVE, new LPCInt(lastActive));

			LPCMapping usernames = this.i3UserCache
					.getLPCMapping(new LPCString(mudname));

			if (usernames == null)
				usernames = new LPCMapping();

			usernames.set(new LPCString(Utils.safePath(username)), data);
			this.i3UserCache.set(new LPCString(Utils.safePath(mudname)),
					usernames);
		} else {
			UUID uuid = this.localUUIDs.get(username);
			List<Object> data = new ArrayList<Object>(USERSSIZE);

			for (int i = 0; i < USERSSIZE; i++)
				data.add(null);

			data.set(VISNAME, visname);
			data.set(GENDER, gender);
			data.set(LASTUPDATE, lastUpdate);
			data.set(LASTACTIVE, lastActive);

			I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

			if (this.users.get(uuid) != null)
				data.set(TUNEIN, this.users.get(uuid).get(TUNEIN));
			else {
				if (i3Channel == null)
					data.set(TUNEIN, null);
				else {
					List<String> tunein = new ArrayList<String>(
							i3Channel.getTunein());

					data.set(TUNEIN, tunein);
				}
			}

			if (this.users.get(uuid) != null)
				data.set(ALIASES, this.users.get(uuid).get(ALIASES));
			else {
				if (i3Channel == null)
					data.set(ALIASES, null);
				else {
					Map<String, String> aliases = new ConcurrentHashMap<String, String>(
							i3Channel.getAliases());

					data.set(ALIASES, aliases);
				}
			}

			this.users.put(uuid, data);
		}

		saveConfig();
	}

	public void checkUser(String mudname, String username, String visname,
			boolean stamp) {
		int gender;

		mudname = Utils.stripColor(mudname);
		username = Utils.stripColor(username);

		if (shadows.contains(username)) {
			visname = "A Shadow";
			gender = Gender.NEUTER.getGender();
			stamp = false;
		} else
			gender = getGender(mudname, username);

		UUID uuid = null;
		boolean local = mudname.equals(Utils.getServerName());

		if (local)
			uuid = this.localUUIDs.get(username.toLowerCase());

		if ((visname != null && visname.isEmpty()) || gender == -1) {
			Log.debug("Adding user '" + username + "@" + mudname + "'");

			if (visname.isEmpty()) {
				if (!local)
					visname = StringUtils.capitalize(username);
				else {
					if (uuid == null)
						visname = username;
					else
						visname = Intermud3.instance.getServer()
								.getOfflinePlayer(username).getName();
				}
			}

			gender = Gender.NEUTER.getGender();
			addUserCache(mudname, username, visname, gender, true);

			I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

			if (i3Channel != null)
				i3Channel.sendChanUserReq(mudname, username);
		} else if (stamp) {
			int time = (int) (System.currentTimeMillis() / 1000);
			int lastUpdate;

			if (local)
				lastUpdate = (Integer) this.users.get(uuid).get(LASTUPDATE);
			else
				lastUpdate = this.i3UserCache
						.getLPCMapping(new LPCString(Utils.safePath(mudname)))
						.getLPCArray(new LPCString(Utils.safePath(username)))
						.getLPCInt(LASTUPDATE).toInt();

			if (time - lastUpdate > 28 * 24 * 60 * 60) {
				Log.debug("Resetting user '" + username + "@" + mudname + "'");

				if (visname == null || visname.isEmpty()) {
					if (!local)
						visname = StringUtils.capitalize(username);
					else {
						if (uuid == null)
							visname = username;
						else
							visname = Intermud3.instance.getServer()
									.getOfflinePlayer(username).getName();
					}
				}

				gender = Gender.NEUTER.getGender();
				addUserCache(mudname, username, visname, gender);

				I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

				if (i3Channel != null)
					i3Channel.sendChanUserReq(mudname, username);
			} else {
				if (local) {
					List<Object> data = this.users.get(uuid);

					data.set(LASTACTIVE, time);
					this.users.put(uuid, data);
				} else {
					mudname = Utils.safePath(mudname);
					username = Utils.safePath(username);

					LPCArray data = this.i3UserCache.getLPCMapping(
							new LPCString(mudname)).getLPCArray(
							new LPCString(username));

					data.set(LASTACTIVE, new LPCInt(time));
					this.i3UserCache.getLPCMapping(new LPCString(mudname)).set(
							new LPCString(username), data);
				}
			}
		}

		saveConfig();
	}

	public void checkUser(LPCString mudname, LPCString username,
			LPCString visname, boolean stamp) {
		checkUser(mudname.toString(), username.toString(), visname.toString(),
				stamp);
	}

	public void create() {
		this.config = new Config(Intermud3.instance, "ucache.yml");

		if (!this.config.getFile().exists()) {
			FileConfiguration root = this.config.getConfig();

			root.addDefault("users", "{}");
			root.addDefault("i3users", "{}");

			this.config.saveConfig();
		}

		reloadConfig();

		Intermud3.instance.getServer().getPluginManager()
				.registerEvents(this, Intermud3.instance);
		ServiceType.I3UCACHE.setVisibleOnRouter(true);
		Intermud3.callout.addHeartBeat(this, hBeatDelay);
	}

	public Map<String, String> getAliases(String name) {
		return getAliases(this.localUUIDs.get(name.toLowerCase()));
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getAliases(UUID uuid) {
		List<Object> user = this.users.get(uuid);

		if (user == null)
			return null;

		return (Map<String, String>) user.get(ALIASES);
	}

	public int getGender(String mudname, String username) {
		username = Utils.stripColor(username);

		if (shadows.contains(username))
			return Gender.NEUTER.getGender();

		if (!mudname.equals(Utils.getServerName())) {
			LPCMapping usernames = this.i3UserCache
					.getLPCMapping(new LPCString(Utils.safePath(mudname)));

			if (usernames == null)
				return -1;

			LPCArray data = usernames.getLPCArray(new LPCString(Utils
					.safePath(username)));

			if (data == null)
				return -1;

			return data.getLPCInt(GENDER).toInt();
		} else {
			UUID uuid = this.localUUIDs.get(username.toLowerCase());

			if (uuid == null || !this.users.containsKey(uuid))
				return -1;

			List<Object> data = this.users.get(uuid);

			return (Integer) data.get(GENDER);
		}
	}

	public int getGender(LPCString mudname, LPCString username) {
		return getGender(mudname.toString(), username.toString());
	}

	public String getGenderString(int gender) {
		Gender g = Gender.getNumGender(gender);

		if (g == null)
			return "other(" + gender + ")";

		return g.getName();
	}

	public List<Object> getLocalUser(String name) {
		return this.users.get(this.localUUIDs.get(name.toLowerCase()));
	}

	public List<Object> getLocalUser(UUID uuid) {
		return this.users.get(uuid);
	}

	public Map<String, String> getReverseAliases(String name) {
		return getReverseAliases(this.localUUIDs.get(name.toLowerCase()));
	}

	public Map<String, String> getReverseAliases(UUID uuid) {
		List<Object> user = this.users.get(uuid);

		if (user == null)
			return null;

		@SuppressWarnings("unchecked")
		Map<String, String> aliases = (Map<String, String>) user.get(ALIASES);
		Map<String, String> reverse = new ConcurrentHashMap<String, String>(
				aliases.size());

		for (Entry<String, String> alias : aliases.entrySet())
			reverse.put(alias.getValue(), alias.getKey());

		return reverse;
	}

	public List<String> getTunein(String name) {
		return getTunein(this.localUUIDs.get(name.toLowerCase()));
	}

	@SuppressWarnings("unchecked")
	public List<String> getTunein(UUID uuid) {
		List<Object> user = this.users.get(uuid);

		if (user == null)
			return null;

		return (List<String>) user.get(TUNEIN);
	}

	public String getVisname(String mudname, String username) {
		username = Utils.stripColor(username);

		if (shadows.contains(username))
			return "A Shadow";

		if (!mudname.equals(Utils.getServerName())) {
			LPCMapping usernames = this.i3UserCache
					.getLPCMapping(new LPCString(Utils.safePath(mudname)));

			if (usernames == null)
				return null;

			LPCArray data = usernames.getLPCArray(new LPCString(Utils
					.safePath(username)));

			if (data == null)
				return null;

			return data.getLPCString(VISNAME).toString();
		} else {
			UUID uuid = this.localUUIDs.get(username.toLowerCase());

			if (uuid == null || !this.users.containsKey(uuid))
				return null;

			List<Object> data = this.users.get(uuid);

			return (String) data.get(VISNAME);
		}
	}

	public String getVisname(LPCString mudname, LPCString username) {
		return getVisname(mudname.toString(), username.toString());
	}

	public void heartBeat() {
		LPCMapping ucCopy = this.i3UserCache.clone();
		int time = (int) (System.currentTimeMillis() / 1000);

		for (Entry<Object, Object> mud : ucCopy.entrySet()) {
			LPCString mudname = (LPCString) mud.getKey();

			if (mudname.toString().startsWith("Dead_Souls_")
					|| mudname.toString().startsWith("Unnamed_CoffeeMUD#")) {
				removeUserCache(mudname);

				continue;
			}

			LPCMapping usernames = ucCopy.getLPCMapping(mudname);

			for (Entry<Object, Object> user : usernames.entrySet()) {
				LPCString username = (LPCString) user.getKey();
				LPCArray data = (LPCArray) user.getValue();
				LPCString visname = data.getLPCString(VISNAME);
				long lastActive = data.getLPCInt(LASTACTIVE).toInt();
				long lastUpdate = data.getLPCInt(LASTUPDATE).toInt();
				long lastActiveDiff = time - lastActive;
				long lastUpdateDiff = time - lastUpdate;

				if (shadows.get(username.toString()) != null)
					removeUserCache(mudname, username);
				else if (shadows.get(Utils.stripColor(visname.toString())
						.toLowerCase()) != null) {
					data.set(
							VISNAME,
							new LPCString(StringUtils.capitalize(username
									.toString())));
					usernames.put(new LPCString(username), data);
					this.i3UserCache.put(new LPCString(mudname), usernames);
				} else if (lastActive != 0 && lastActiveDiff > 7 * 24 * 60 * 60)
					removeUserCache(mudname, username);
				else if (lastUpdateDiff > 28 * 24 * 60 * 60) {
					Log.debug("heartBeat: Reseting user '" + username
							+ "' for mud '" + mudname + "'");
					data.set(GENDER, new LPCInt(-1));
					data.set(LASTUPDATE, new LPCInt(time));
					usernames.put(new LPCString(username), data);
					this.i3UserCache.put(new LPCString(mudname), usernames);
				}
			}
		}

		if (this.users.keySet().contains(zUuid)) {
			List<Object> data = this.users.get(zUuid);
			String visname = (String) data.get(VISNAME);

			this.users.remove(zUuid);
			this.users.put(this.localUUIDs.get(visname.toLowerCase()), data);
		}

		Map<UUID, List<Object>> uCopy = new ConcurrentHashMap<UUID, List<Object>>(
				this.users);

		for (Entry<UUID, List<Object>> user : uCopy.entrySet()) {
			UUID uuid = user.getKey();
			List<Object> data = user.getValue();
			long lastActive = (Long) data.get(LASTACTIVE);
			long lastUpdate = (Long) data.get(LASTUPDATE);
			long lastActiveDiff = (long) time - lastActive;
			long lastUpdateDiff = (long) time - lastUpdate;

			if (lastActive != 0 && lastActiveDiff > 7 * 24 * 60 * 60)
				this.users.remove(uuid);
			else if (lastUpdateDiff > 28 * 24 * 60 * 60) {
				String visname = (String) data.get(VISNAME);

				Log.debug("heartBeat: Reseting local user '" + visname + "'");
				data.set(GENDER, -1);
				data.set(LASTUPDATE, time);
				this.users.put(uuid, data);
			}
		}

		saveConfig();
	}

	private String tmpName;

	@EventHandler
	void onPlayerJoin(PlayerJoinEvent event) {
		this.tmpName = event.getPlayer().getName();
		Intermud3.instance.getServer().getScheduler()
				.runTaskLaterAsynchronously(Intermud3.instance, new Runnable() {
					@Override
					public void run() {
						sendUCacheUpdate(tmpName, false);
					}
				}, 2 * 20);
	}

	@EventHandler
	void onPlayerQuit(PlayerQuitEvent event) {
		saveConfig();
	}

	/**
	 * Reload the ucache config file and setup the local variables.
	 */
	public void reloadConfig() {
		reloadConfig(false);
	}

	/**
	 * Reload the ucache config file and setup the local variables.
	 */
	public void reloadConfig(boolean flag) {
		this.config.reloadConfig();

		FileConfiguration root = this.config.getConfig();

		if (root.contains("ucache")) {
			this.i3UserCache.clear();

			try {
				this.i3UserCache.setLPCData(Utils.toObject(root
						.getString("ucache")));
			} catch (I3Exception i3E) {
				i3E.printStackTrace();
			}
		}

		if (root.contains("users")) {
			this.users.clear();

			ConfigurationSection usersSection = root
					.getConfigurationSection("users");

			for (String user : usersSection.getKeys(false)) {
				UUID uuid;

				try {
					uuid = UUID.fromString(user);
				} catch (IllegalArgumentException iaE) {
					continue;
				}

				ConfigurationSection uuidSection = usersSection
						.getConfigurationSection(user);
				List<Object> data = new ArrayList<Object>(USERSSIZE);

				for (int i = 0; i < USERSSIZE; i++)
					data.add(null);

				data.set(VISNAME, uuidSection.getString("visname"));
				data.set(GENDER,
						Gender.getNamedGender(uuidSection.getString("gender"))
								.getGender());
				data.set(LASTUPDATE, uuidSection.getInt("lastupdate"));
				data.set(LASTACTIVE, uuidSection.getInt("lastactive"));
				data.set(TUNEIN, uuidSection.getList("tunein"));

				ConfigurationSection aliasesSection = uuidSection
						.getConfigurationSection("aliases");
				Set<String> aliases = aliasesSection.getKeys(false);
				Map<String, String> dataAliases = new ConcurrentHashMap<String, String>();

				for (String alias : aliases)
					dataAliases.put(alias, aliasesSection.getString(alias));

				data.set(ALIASES, dataAliases);

				this.users.put(uuid, data);
			}
		}

		if (root.contains("i3users")) {
			this.i3UserCache.clear();

			ConfigurationSection i3UsersSection = root
					.getConfigurationSection("i3users");

			for (String mudname : i3UsersSection.getKeys(false)) {
				ConfigurationSection mudSection = i3UsersSection
						.getConfigurationSection(mudname);
				LPCMapping users = new LPCMapping();

				for (String user : mudSection.getKeys(false)) {
					ConfigurationSection userSection = mudSection
							.getConfigurationSection(user);
					LPCArray data = new LPCArray();

					for (int i = 0; i < UCACHESIZE; i++)
						data.add(null);

					data.set(VISNAME,
							new LPCString(userSection.getString("visname")));
					data.set(
							GENDER,
							new LPCInt(Gender.getNamedGender(
									userSection.getString("gender"))
									.getGender()));
					data.set(LASTUPDATE,
							new LPCInt(userSection.getInt("lastupdate")));
					data.set(LASTACTIVE,
							new LPCInt(userSection.getInt("lastactive")));

					users.set(new LPCString(user), data);
				}

				this.i3UserCache.set(new LPCString(mudname), users);
			}
		}

		if (flag)
			Log.info(this.config.getFile().getName() + " loaded.");
	}

	public void remove() {
		HandlerList.unregisterAll(this);
		Intermud3.callout.removeCallOuts(this);
		Intermud3.callout.removeHeartBeat(this);
		saveConfig();
		this.config.remove();
		ucachePayload.remove();
		Intermud3.uuid.shutdown();

		// Clear out all lists.
		shadows.clear();
		this.i3UserCache.clear();
		this.localUUIDs.clear();
		this.users.clear();

		// Remove references.
		this.config = null;
		this.i3UserCache = null;
		this.localUUIDs = null;
		this.users = null;
	}

	public void removeUserCache() {
		LPCArray muds = new LPCArray(this.i3UserCache.keySet());

		for (Object obj : muds) {
			LPCString mud = (LPCString) obj;

			Log.debug("removeUserCache: Deleting mud '" + mud + "'");
			this.i3UserCache.remove(mud);
		}
	}

	/**
	 * @param mudname
	 */
	public void removeUserCache(String mudname) {
		removeUserCache(new LPCString(mudname));
	}

	/**
	 * @param mudname
	 */
	public void removeUserCache(LPCString mudname) {
		Log.debug("removeUserCache: Deleting mud '" + mudname + "'");
		this.i3UserCache.remove(mudname);
	}

	/**
	 * @param mudname
	 * @param username
	 */
	public void removeUserCache(String mudname, String username) {
		removeUserCache(new LPCString(mudname), new LPCString(username));
	}

	/**
	 * @param mudname
	 * @param username
	 */
	public void removeUserCache(LPCString mudname, LPCString username) {
		LPCArray muds = new LPCArray();

		if (mudname == null)
			muds.addAll(this.i3UserCache.keySet());
		else if (username == null)
			muds.add(mudname);
		else {
			Log.debug("removeUserCache: Deleting user '" + username
					+ "' from mud '" + mudname + "'");

			LPCMapping users = new LPCMapping(
					this.i3UserCache.getLPCMapping(mudname));

			users.remove(new LPCString(Utils.safePath(username.toString())));
			this.i3UserCache.put(mudname, users);

			if (users.isEmpty())
				muds.add(mudname);
		}

		for (Object obj : muds) {
			LPCString mud = (LPCString) obj;

			Log.debug("removeUserCache: Deleting mud '" + mud + "'");
			this.i3UserCache.remove(mud);
		}
	}

	@Override
	public void replyHandler(Packet packet) {
		addUserCache(packet.getLPCString(Payload.O_MUD),
				packet.getLPCString(ucachePayload.get("UC_USERNAME")),
				packet.getLPCString(ucachePayload.get("UC_VISNAME")),
				packet.getLPCInt(ucachePayload.get("UC_GENDER")), true);
	}

	@Override
	public void reqHandler(Packet packet) {
		// Not used.
	}

	/**
	 * @param name
	 */
	public void resetUCacheUpdate(String name) {
		UUID uuid = this.localUUIDs.get(name.toLowerCase());

		this.users.remove(uuid);
		saveConfig();
	}

	public void saveConfig() {
		saveConfig(false);
	}

	public void saveConfig(boolean flag) {
		// Clear the configuration.
		this.config.clearConfig();

		FileConfiguration root = this.config.getConfig();
		ConfigurationSection usersSection = root.createSection("users");

		for (Entry<UUID, List<Object>> user : this.users.entrySet()) {
			ConfigurationSection uuidSection = usersSection.createSection(user
					.getKey().toString());
			List<Object> data = user.getValue();

			uuidSection.set("visname", data.get(VISNAME));
			uuidSection.set("gender",
					Gender.getNumGender((Integer) data.get(GENDER)).getName());
			uuidSection.set("lastupdate", data.get(LASTUPDATE));
			uuidSection.set("lastactive", data.get(LASTACTIVE));
			uuidSection.set("tunein", data.get(TUNEIN));

			ConfigurationSection aliasesSection = uuidSection
					.createSection("aliases");
			@SuppressWarnings("unchecked")
			Map<String, String> aliases = (ConcurrentHashMap<String, String>) data
					.get(ALIASES);

			for (Entry<String, String> alias : aliases.entrySet())
				aliasesSection.set(alias.getKey(), alias.getValue());
		}

		ConfigurationSection i3UsersSection = root.createSection("i3users");

		for (Entry<Object, Object> mud : this.i3UserCache.entrySet()) {
			ConfigurationSection mudSection = i3UsersSection
					.createSection(Utils.safePath(mud.getKey().toString()));
			LPCMapping users = (LPCMapping) mud.getValue();

			for (Entry<Object, Object> user : users.entrySet()) {
				ConfigurationSection userSection = mudSection
						.createSection(Utils.safePath(user.getKey().toString()));
				LPCArray data = (LPCArray) user.getValue();

				userSection.set("visname", data.get(VISNAME).toString());
				userSection
						.set("gender",
								Gender.getNumGender(
										((LPCInt) data.get(GENDER)).toInt())
										.getName());
				userSection.set("lastupdate",
						((LPCInt) data.get(LASTUPDATE)).toInt());
				userSection.set("lastactive",
						((LPCInt) data.get(LASTACTIVE)).toInt());
			}
		}

		this.config.saveConfig();

		if (flag)
			Log.info(this.config.getFile().getName() + " saved.");
	}

	public void setAlias(Player player, String alias, String channel) {
		UUID uuid = this.localUUIDs.get(player.getName().toLowerCase());
		List<Object> user = this.users.get(uuid);

		if (uuid != null && user != null) {
			@SuppressWarnings("unchecked")
			Map<String, String> aliases = (Map<String, String>) user
					.get(ALIASES);

			if (aliases == null)
				aliases = new ConcurrentHashMap<String, String>();

			if (channel == null)
				aliases.remove(alias);
			else
				aliases.put(alias, channel);

			user.set(ALIASES, aliases);
			this.users.put(uuid, user);

			saveConfig();
		}
	}

	/**
	 * @param name
	 * @param update
	 */
	public void sendUCacheUpdate(String name, boolean update) {
		UUID localUUID = Bukkit.getPlayer(name).getUniqueId();
		String lname = name.toLowerCase();

		if (!update && this.localUUIDs.containsKey(lname)
				&& this.localUUIDs.get(lname) == localUUID)
			return;

		this.localUUIDs.put(lname, localUUID);

		UUID uuid = localUUID;

		if (!this.users.containsKey(localUUID)
				|| !this.users.get(localUUID).get(VISNAME).toString()
						.equals(name)) {
			update = true;
			uuid = Intermud3.uuid.getIdOptimistic(name);

			if (uuid == null || uuid.equals(zUuid))
				uuid = localUUID;

			if (uuid != null && !localUUID.equals(uuid))
				this.localUUIDs.put(lname, uuid);
		}

		if (this.users.containsKey(uuid)) {
			if (update)
				this.users.remove(uuid);
			else
				return;
		}

		Packet payload = new Packet();

		payload.add(new LPCString(lname));
		payload.add(new LPCString(name));
		payload.add(new LPCInt(Gender.NEUTER.getGender()));

		Intermud3.network.sendToAll(PacketType.UCACHE_UPDATE, null, payload);
	}

	public void tuneIn(Player player, String channel) {
		UUID uuid = this.localUUIDs.get(player.getName().toLowerCase());
		List<Object> user = this.users.get(uuid);

		if (uuid != null && user != null) {
			@SuppressWarnings("unchecked")
			List<String> tunein = (List<String>) user.get(TUNEIN);

			if (tunein == null)
				tunein = new ArrayList<String>();

			if (!tunein.contains(channel))
				tunein.add(channel);

			user.set(TUNEIN, tunein);
			this.users.put(uuid, user);

			saveConfig();
		}
	}

	public void tuneOut(Player player, String channel) {
		UUID uuid = this.localUUIDs.get(player.getName().toLowerCase());
		List<Object> user = this.users.get(uuid);

		if (uuid != null && user != null) {
			@SuppressWarnings("unchecked")
			List<String> tunein = (List<String>) user.get(TUNEIN);

			if (tunein == null)
				tunein = new ArrayList<String>();

			tunein.remove(channel);

			user.set(TUNEIN, tunein);
			this.users.put(uuid, user);

			saveConfig();
		}
	}
}

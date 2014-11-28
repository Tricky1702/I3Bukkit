package uk.org.rockthehalo.intermud3.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

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
		MALE(0L, "male"), FEMALE(1L, "female"), NEUTER(2L, "neuter"), NEUTRAL(3L, "neutral");

		private static final Map<String, Gender> nameToGender = new HashMap<String, Gender>(values().length);
		private static final Map<Long, Gender> numToGender = new HashMap<Long, Gender>(values().length);

		private long genderNum = -1L;
		private String genderName = null;

		private Gender(final long genderNum, final String genderName) {
			this.genderNum = genderNum;
			this.genderName = genderName;
		}

		public static long getGenderNum(final String name) {
			final Gender g = nameToGender.get(name.toLowerCase());

			if (g == null)
				return NEUTER.genderNum;

			return g.genderNum;
		}

		public static String getGenderName(final long gender) {
			final Gender g = numToGender.get(gender);

			if (g == null)
				return NEUTER.genderName;

			return g.genderName;
		}

		public static Gender getGenderByNumber(final long gender) {
			final Gender g = numToGender.get(gender);

			if (g == null)
				return NEUTER;

			return g;
		}

		public static Gender getGenderByName(final String name) {
			final Gender g = nameToGender.get(name.toLowerCase());

			if (g == null)
				return NEUTER;

			return g;
		}

		static {
			for (final Gender g : Gender.values()) {
				numToGender.put(g.genderNum, g);
				nameToGender.put(g.genderName, g);
			}
		}
	}

	private static final long hBeatDelay = 60L;
	private static final List<String> shadows = new ArrayList<String>(Arrays.asList("apresence", "ashadow", "aninvisibleimmortal",
			"someone"));
	private static final Payload ucachePayload = new Payload(Arrays.asList("UC_USERNAME", "UC_VISNAME", "UC_GENDER"));
	private static final UUID zUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

	private final Map<String, Map<String, Map<String, Object>>> i3UserCache = Collections
			.synchronizedMap(new LinkedHashMap<String, Map<String, Map<String, Object>>>());
	private final Map<UUID, Map<String, Object>> localUserCache = Collections
			.synchronizedMap(new LinkedHashMap<UUID, Map<String, Object>>());
	private final Map<String, UUID> localUUIDs = Collections.synchronizedMap(new LinkedHashMap<String, UUID>());

	private Config config = null;

	public I3UCache() {
	}

	/**
	 * @param m
	 *            mudname
	 * @param u
	 *            username
	 * @param v
	 *            visname
	 * @param g
	 *            gender
	 */
	@SuppressWarnings("unchecked")
	public void addUserCache(Object m, Object u, Object v, Object g) {
		if (m == null || u == null || v == null)
			return;

		final String mudname;

		if (!String.class.isInstance(m) && !Utils.isLPCString(m))
			return;

		mudname = Utils.stripColor(m.toString());

		if (mudname.isEmpty())
			return;

		final String username;

		if (!String.class.isInstance(u) && !Utils.isLPCString(u))
			return;

		username = Utils.stripColor(u.toString());

		if (username.isEmpty())
			return;

		final String visname;

		if (!String.class.isInstance(v) && !Utils.isLPCString(v))
			return;

		visname = v.toString();

		if (visname.isEmpty())
			return;

		final long gender;

		if (Long.class.isInstance(g))
			gender = (long) g;
		else if (Utils.isLPCInt(g))
			gender = ((LPCInt) g).toNum();
		else
			gender = Gender.NEUTER.genderNum;

		Log.debug("Adding '" + username + "@" + mudname + "' [" + visname + "/" + Gender.getGenderName(gender) + "]");

		final Map<String, Object> data = new LinkedHashMap<String, Object>(6);
		final long tm = System.currentTimeMillis() / 1000L;

		if (!mudname.equalsIgnoreCase(Utils.getServerName())) {
			data.put("VISNAME", visname);
			data.put("GENDER", gender);
			data.put("LASTUPDATE", tm);
			data.put("LASTACTIVE", tm);

			setI3User(mudname, username, data);
		} else {
			data.put("VISNAME", visname);
			data.put("GENDER", gender);
			data.put("LASTUPDATE", tm);
			data.put("LASTACTIVE", tm);

			final UUID uuid = getUUID(username);
			final Map<String, Object> localUser = getLocalUser(uuid);

			List<String> tunein = new ArrayList<String>();
			Map<String, String> aliases = new LinkedHashMap<String, String>();

			if (localUser != null) {
				tunein.addAll((List<String>) localUser.get("TUNEIN"));
				aliases.putAll((Map<String, String>) localUser.get("ALIASES"));
			} else {
				final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

				if (i3Channel != null) {
					tunein.addAll(i3Channel.getTunein());
					aliases.putAll(i3Channel.getAliases());
				}
			}

			data.put("TUNEIN", tunein);
			data.put("ALIASES", aliases);

			setLocalUser(uuid, data);
		}

		saveConfig();
	}

	public void checkUser(String mudname, String username, String visname, boolean stamp) {
		final long gender;

		mudname = Utils.stripColor(mudname);
		username = Utils.stripColor(username);

		if (shadows.contains(username.toLowerCase().replaceAll("[ \t]+", ""))) {
			visname = "A Shadow";
			gender = Gender.NEUTER.genderNum;
			stamp = false;
		} else
			gender = getGender(mudname, username);

		final boolean local = mudname.equalsIgnoreCase(Utils.getServerName());

		if ((visname != null && visname.isEmpty()) || gender == -1L) {
			Log.debug("Adding user '" + username + "@" + mudname + "'");

			if (visname.isEmpty()) {
				if (!local)
					visname = StringUtils.capitalize(username);
				else {
					final UUID uuid = getUUID(username);

					if (uuid == null)
						visname = username;
					else
						visname = Bukkit.getServer().getOfflinePlayer(username).getName();
				}
			}

			addUserCache(mudname, username, visname, Gender.NEUTER.genderNum);

			final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

			if (i3Channel != null)
				i3Channel.sendChanUserReq(mudname, username);
		} else if (stamp) {
			final long tm = System.currentTimeMillis() / 1000L;
			final Map<String, Object> data;

			if (!local)
				data = getI3User(mudname, username);
			else
				data = getLocalUser(username);

			final long lastUpdate = (long) data.get("LASTUPDATE");

			if (tm - lastUpdate > 28L * 24L * 60L * 60L) {
				Log.debug("Resetting user '" + username + "@" + mudname + "'");

				if (visname == null || visname.isEmpty())
					if (!local)
						visname = StringUtils.capitalize(username);
					else {
						final UUID uuid = getUUID(username);

						if (uuid == null)
							visname = username;
						else
							visname = Bukkit.getServer().getOfflinePlayer(username).getName();
					}

				addUserCache(mudname, username, visname, Gender.NEUTER.genderNum);

				final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

				if (i3Channel != null)
					i3Channel.sendChanUserReq(mudname, username);
			} else {
				data.put("LASTACTIVE", tm);

				if (!local)
					setI3User(mudname, username, data);
				else
					setLocalUser(username, data);
			}
		}

		saveConfig();
	}

	public void checkUser(final LPCString mudname, final LPCString username, final LPCString visname, final boolean stamp) {
		checkUser(mudname.toString(), username.toString(), visname.toString(), stamp);
	}

	public void create() {
		this.config = new Config("ucache.yml");

		if (!this.config.getFile().exists()) {
			FileConfiguration root = this.config.getConfig();

			root.set("users", this.localUserCache);
			root.set("i3users", this.i3UserCache);

			this.config.saveConfig();
		}

		reloadConfig();

		for (Player player : Bukkit.getServer().getOnlinePlayers())
			this.localUUIDs.put(player.getName().toLowerCase(), player.getUniqueId());

		Bukkit.getServer().getPluginManager().registerEvents(this, Intermud3.plugin);
		ServiceType.I3UCACHE.setVisibleOnRouter(true);
		Intermud3.callout.addHeartBeat(this, hBeatDelay);
	}

	public Map<String, String> getAliases(final String name) {
		return getAliases(getUUID(name));
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getAliases(final UUID uuid) {
		if (uuid == null || !this.localUserCache.containsKey(uuid))
			return null;

		return (Map<String, String>) getLocalUser(uuid).get("ALIASES");
	}

	public long getGender(String mudname, String username) {
		mudname = Utils.stripColor(mudname);
		username = Utils.stripColor(username);

		if (shadows.contains(username.toLowerCase().replaceAll("[ \t]+", "")))
			return Gender.NEUTER.genderNum;

		final Map<String, Object> data;

		if (!mudname.equalsIgnoreCase(Utils.getServerName()))
			data = getI3User(mudname, username);
		else
			data = getLocalUser(username);

		if (data == null)
			return -1L;

		return (long) data.get("GENDER");
	}

	public long getGender(final LPCString mudname, final LPCString username) {
		return getGender(mudname.toString(), username.toString());
	}

	public Map<String, Map<String, Object>> getI3Mud(final String mud) {
		return this.i3UserCache.get(Utils.safePath(mud));
	}

	public Map<String, Object> getI3User(final String mud, final String name) {
		if (getI3Mud(Utils.safePath(mud)) == null)
			return null;

		return getI3Mud(Utils.safePath(mud)).get(Utils.safePath(name));
	}

	public Map<String, Object> getLocalUser(final String name) {
		return getLocalUser(getUUID(name));
	}

	public Map<String, Object> getLocalUser(final UUID uuid) {
		if (uuid == null)
			return null;

		return this.localUserCache.get(uuid);
	}

	public Map<String, String> getReverseAliases(final String name) {
		return getReverseAliases(getUUID(name));
	}

	public Map<String, String> getReverseAliases(final UUID uuid) {
		if (uuid == null || !this.localUserCache.containsKey(uuid))
			return null;

		synchronized (this.localUserCache) {
			@SuppressWarnings("unchecked")
			final Map<String, String> aliases = (Map<String, String>) getLocalUser(uuid).get("ALIASES");
			final Map<String, String> reverse = new LinkedHashMap<String, String>(aliases.size());

			for (final Entry<String, String> alias : aliases.entrySet())
				reverse.put(alias.getValue(), alias.getKey());

			return reverse;
		}
	}

	public List<String> getTunein(final String name) {
		return getTunein(getUUID(name));
	}

	@SuppressWarnings("unchecked")
	public List<String> getTunein(final UUID uuid) {
		if (uuid == null || !this.localUserCache.containsKey(uuid))
			return null;

		return (List<String>) getLocalUser(uuid).get("TUNEIN");
	}

	public UUID getUUID(String name) {
		return this.localUUIDs.get(name.toLowerCase());
	}

	public String getVisname(final String mudname, String username) {
		username = Utils.stripColor(username);

		if (shadows.contains(username.toLowerCase().replaceAll("[ \t]+", "")))
			return "A Shadow";

		final Map<String, Object> data;

		if (!mudname.equalsIgnoreCase(Utils.getServerName()))
			data = getI3User(mudname, username);
		else
			data = getLocalUser(username);

		if (data == null)
			return null;

		return (String) data.get("VISNAME");
	}

	public String getVisname(final LPCString mudname, final LPCString username) {
		return getVisname(mudname.toString(), username.toString());
	}

	public void heartBeat() {
		final long tm = System.currentTimeMillis() / 1000L;

		synchronized (this.localUserCache) {
			final Map<UUID, Map<String, Object>> ucCopy = new LinkedHashMap<UUID, Map<String, Object>>(this.localUserCache);

			for (final Entry<UUID, Map<String, Object>> uuids : ucCopy.entrySet()) {
				final UUID uuid = uuids.getKey();
				Map<String, Object> data = uuids.getValue();
				final String visname = (String) data.get("VISNAME");
				final long lastUpdate = (long) data.get("LASTUPDATE");
				final long lastActive = (long) data.get("LASTACTIVE");
				final long lastUpdateDiff = tm - lastUpdate;
				final long lastActiveDiff = tm - lastActive;

				if (lastActive != 0 && lastActiveDiff > 7L * 24L * 60L * 60L)
					this.localUserCache.remove(uuid);
				else if (lastUpdateDiff > 28L * 24L * 60L * 60L) {
					Log.debug("heartBeat: Reset local user '" + visname + "'");
					data.put("GENDER", -1L);
					data.put("LASTUPDATE", tm);
					setLocalUser(visname, data);
				}
			}
		}

		synchronized (this.i3UserCache) {
			final Map<String, Map<String, Map<String, Object>>> ucCopy = new LinkedHashMap<String, Map<String, Map<String, Object>>>(
					this.i3UserCache);

			for (final Entry<String, Map<String, Map<String, Object>>> mud : ucCopy.entrySet()) {
				final String mudname = mud.getKey();

				if (mudname.toLowerCase().startsWith("dead_souls_") || mudname.toLowerCase().startsWith("unnamed_coffeemud#")) {
					removeUserCache(mudname);

					continue;
				}

				final Map<String, Map<String, Object>> usernames = ucCopy.get(mudname);

				for (final Entry<String, Map<String, Object>> user : usernames.entrySet()) {
					final String username = user.getKey();
					Map<String, Object> data = user.getValue();
					final String visname = (String) data.get("VISNAME");
					final long lastUpdate = (long) data.get("LASTUPDATE");
					final long lastActive = (long) data.get("LASTACTIVE");
					final long lastUpdateDiff = tm - lastUpdate;
					final long lastActiveDiff = tm - lastActive;

					if (shadows.contains(username.toLowerCase().replaceAll("[ \t]+", "")))
						removeUserCache(mudname, username);
					else if (shadows.contains(Utils.stripColor(visname.toLowerCase().replaceAll("[ \t]+", "")))) {
						data.put("VISNAME", StringUtils.capitalize(username));
						setI3User(mudname, username, data);
					} else if (lastActive != 0 && lastActiveDiff > 7L * 24L * 60L * 60L)
						removeUserCache(mudname, username);
					else if (lastUpdateDiff > 28L * 24L * 60L * 60L) {
						Log.debug("heartBeat: Reset user '" + username + "' for mud '" + mudname + "'");
						data.put("GENDER", -1L);
						data.put("LASTUPDATE", tm);
						setI3User(mudname, username, data);
					}
				}
			}
		}

		if (this.localUserCache.keySet().contains(zUuid)) {
			final Map<String, Object> data = getLocalUser(zUuid);
			final String visname = (String) data.get("VISNAME");

			this.localUserCache.remove(zUuid);
			setLocalUser(visname, data);
		}

		saveConfig();
	}

	@EventHandler
	void onPlayerJoin(final PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		final Object[] args = { p.getUniqueId(), p.getName(), false };

		Intermud3.callout.addCallOut(this, "sendUCacheUpdate", 2L, args);
	}

	@EventHandler
	void onPlayerQuit(final PlayerQuitEvent event) {
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
	public void reloadConfig(final boolean flag) {
		this.config.reloadConfig();

		final FileConfiguration root = this.config.getConfig();

		if (root.contains("ucache")) {
			this.i3UserCache.clear();

			final LPCMapping oldI3UserCache = (LPCMapping) Utils.toObject(root.getString("ucache", "([])"));

			for (final Entry<Object, Object> mud : oldI3UserCache.entrySet()) {
				final LPCMapping i3users = (LPCMapping) mud.getValue();

				for (final Entry<Object, Object> user : i3users.entrySet()) {
					final LPCArray data = (LPCArray) user.getValue();
					final Map<String, Object> newData = new LinkedHashMap<String, Object>();

					newData.put("VISNAME", data.get(0).toString());
					newData.put("GENDER", ((LPCInt) data.get(1)).toNum());
					newData.put("LASTUPDATE", ((LPCInt) data.get(2)).toNum());
					newData.put("LASTACTIVE", ((LPCInt) data.get(3)).toNum());
					setI3User(mud.getKey().toString(), user.getKey().toString(), newData);
				}
			}
		}

		if (root.contains("users")) {
			this.localUserCache.clear();

			final ConfigurationSection usersSection = root.getConfigurationSection("users");

			for (final String userUUID : usersSection.getKeys(false)) {
				final UUID uuid;

				try {
					uuid = UUID.fromString(userUUID);
				} catch (IllegalArgumentException iaE) {
					Log.warn("Illegal UUID: '" + userUUID + "'", iaE);

					continue;
				}

				final ConfigurationSection uuidSection = usersSection.getConfigurationSection(userUUID);

				Map<String, Object> data = new LinkedHashMap<String, Object>();

				data.put("VISNAME", uuidSection.getString("visname"));
				data.put("GENDER", Gender.getGenderNum(uuidSection.getString("gender")));

				final long tm = System.currentTimeMillis() / 1000L;

				long lastUpdate = uuidSection.getLong("lastupdate", tm);

				if (lastUpdate < 0L)
					lastUpdate = -lastUpdate;

				data.put("LASTUPDATE", lastUpdate);
				data.put("LASTACTIVE", uuidSection.getLong("lastactive", tm));
				data.put("TUNEIN", uuidSection.getList("tunein"));

				final ConfigurationSection aliasesSection = uuidSection.getConfigurationSection("aliases");
				final Set<String> aliases = aliasesSection.getKeys(false);

				Map<String, String> dataAliases = new LinkedHashMap<String, String>();

				for (final String alias : aliases)
					dataAliases.put(alias, aliasesSection.getString(alias));

				data.put("ALIASES", dataAliases);

				setLocalUser(uuid, data);
			}
		}

		if (root.contains("i3users")) {
			this.i3UserCache.clear();

			final ConfigurationSection i3UsersSection = root.getConfigurationSection("i3users");

			for (final String mudname : i3UsersSection.getKeys(false)) {
				final ConfigurationSection mudSection = i3UsersSection.getConfigurationSection(mudname);
				final Map<String, Map<String, Object>> i3users = new LinkedHashMap<String, Map<String, Object>>();

				for (final String user : mudSection.getKeys(false)) {
					final ConfigurationSection userSection = mudSection.getConfigurationSection(user);

					final Map<String, Object> data = new LinkedHashMap<String, Object>();

					data.put("VISNAME", userSection.getString("visname"));
					data.put("GENDER", Gender.getGenderNum(userSection.getString("gender")));

					long lastUpdate = userSection.getLong("lastupdate");

					if (lastUpdate < 0L)
						lastUpdate = -lastUpdate;

					data.put("LASTUPDATE", lastUpdate);
					data.put("LASTACTIVE", userSection.getLong("lastactive"));

					i3users.put(user, data);
				}

				setI3Mud(mudname, i3users);
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
		this.localUserCache.clear();
		this.localUUIDs.clear();

		// Remove references.
		this.config = null;
	}

	public void removeUserCache() {
		this.i3UserCache.clear();
	}

	/**
	 * @param mudname
	 */
	public void removeUserCache(final String mudname) {
		Log.debug("removeUserCache: Deleting mud '" + mudname + "'");
		this.i3UserCache.remove(mudname);
	}

	/**
	 * @param mudname
	 * @param username
	 */
	public void removeUserCache(final String mudname, final String username) {
		final List<String> muds = new ArrayList<String>();

		if (mudname == null)
			muds.addAll(this.i3UserCache.keySet());
		else if (username == null)
			muds.add(mudname);
		else {
			final Map<String, Map<String, Object>> i3users = getI3Mud(mudname);

			if (i3users != null) {
				Log.debug("removeUserCache: Deleting user '" + username + "' from mud '" + mudname + "'");
				i3users.remove(Utils.safePath(username));
				setI3Mud(mudname, i3users);
			}

			if (i3users == null || i3users.isEmpty())
				muds.add(mudname);
		}

		for (final String mud : muds)
			removeUserCache(mud);
	}

	@Override
	public void replyHandler(final Packet packet) {
		addUserCache(packet.get(Payload.O_MUD), packet.get(ucachePayload.get("UC_USERNAME")),
				packet.get(ucachePayload.get("UC_VISNAME")), packet.get(ucachePayload.get("UC_GENDER")));
	}

	@Override
	public void reqHandler(final Packet packet) {
		// Not used.
	}

	/**
	 * @param name
	 */
	public void resetUCacheUpdate(final String name) {
		final UUID uuid = getUUID(name);

		if (uuid != null) {
			this.localUserCache.remove(uuid);
			saveConfig();
		}
	}

	public void saveConfig() {
		saveConfig(false);
	}

	@SuppressWarnings("unchecked")
	public void saveConfig(final boolean flag) {
		// Clear the configuration.
		this.config.clearConfig();

		final FileConfiguration root = this.config.getConfig();
		final ConfigurationSection usersSection = root.createSection("users");

		synchronized (this.localUserCache) {
			for (final Entry<UUID, Map<String, Object>> user : this.localUserCache.entrySet()) {
				final Map<String, Object> data = user.getValue();
				final ConfigurationSection uuidSection = usersSection.createSection(user.getKey().toString());

				uuidSection.set("visname", data.get("VISNAME").toString());
				uuidSection.set("gender", Gender.getGenderName((long) data.get("GENDER")));
				uuidSection.set("lastupdate", (long) data.get("LASTUPDATE"));
				uuidSection.set("lastactive", (long) data.get("LASTACTIVE"));
				uuidSection.set("tunein", data.get("TUNEIN"));

				final Map<String, String> aliases = (Map<String, String>) data.get("ALIASES");
				final ConfigurationSection aliasesSection = uuidSection.createSection("aliases");

				for (final Entry<String, String> alias : aliases.entrySet())
					aliasesSection.set(alias.getKey(), alias.getValue());
			}
		}

		ConfigurationSection i3UsersSection = root.createSection("i3users");

		synchronized (this.i3UserCache) {
			for (final Entry<String, Map<String, Map<String, Object>>> mud : this.i3UserCache.entrySet()) {
				final Map<String, Map<String, Object>> i3users = mud.getValue();
				final ConfigurationSection mudSection = i3UsersSection.createSection(Utils.safePath(mud.getKey()));

				for (final Entry<String, Map<String, Object>> user : i3users.entrySet()) {
					final Map<String, Object> data = user.getValue();
					final ConfigurationSection userSection = mudSection.createSection(Utils.safePath(user.getKey().toString()));

					userSection.set("visname", data.get("VISNAME").toString());
					userSection.set("gender", Gender.getGenderName((long) data.get("GENDER")));
					userSection.set("lastupdate", (long) data.get("LASTUPDATE"));
					userSection.set("lastactive", (long) data.get("LASTACTIVE"));
				}
			}
		}

		this.config.saveConfig();

		if (flag)
			Log.info(this.config.getFile().getName() + " saved.");
	}

	/**
	 * @param name
	 * @param update
	 */
	public void sendUCacheUpdate(final UUID plrUUID, final String plrName, boolean update) {
		Log.debug("sendUCacheUpdate::Checking UUID: " + plrUUID + ", Name: " + plrName + ", update: " + update);
		Log.debug("sendUCacheUpdate::localUUIDs = " + this.localUUIDs);

		if (!update && plrUUID.equals(getUUID(plrName)))
			return;

		final String lname = plrName.toLowerCase();

		this.localUUIDs.put(lname, plrUUID);

		final Map<String, Object> localUser = getLocalUser(plrUUID);
		final String oldName = (localUser != null ? (localUser.get("VISNAME") != null ? localUser.get("VISNAME").toString()
				: plrName) : plrName);

		UUID uuid = plrUUID;

		if (localUser == null || !oldName.equals(plrName)) {
			if (localUser != null)
				this.localUUIDs.remove(oldName.toLowerCase());

			update = true;
			uuid = Intermud3.uuid.getIdOptimistic(plrName);

			if (uuid == null || uuid.equals(zUuid))
				uuid = plrUUID;

			if (!plrUUID.equals(uuid))
				this.localUUIDs.put(lname, uuid);
		}

		if (this.localUserCache.containsKey(uuid)) {
			if (update)
				this.localUserCache.remove(uuid);
			else
				return;
		}

		Packet payload = new Packet();

		payload.add(new LPCString(lname));
		payload.add(new LPCString(plrName));
		payload.add(new LPCInt(Gender.NEUTER.genderNum));

		Intermud3.network.sendToAll(PacketType.UCACHE_UPDATE, null, payload);
	}

	public void setAlias(final String playerName, final String alias, final String channel) {
		final UUID uuid = getUUID(playerName);
		final Map<String, Object> user = getLocalUser(uuid);

		if (uuid != null && user != null) {
			@SuppressWarnings("unchecked")
			Map<String, String> aliases = (Map<String, String>) user.get("ALIASES");

			if (aliases == null)
				aliases = new LinkedHashMap<String, String>();

			if (channel == null)
				aliases.remove(alias);
			else
				aliases.put(alias, channel);

			user.put("ALIASES", aliases);
			setLocalUser(uuid, user);

			saveConfig();
		}
	}

	public void setI3Mud(final String mud, final Map<String, Map<String, Object>> data) {
		this.i3UserCache.put(Utils.safePath(mud), data);
	}

	public void setI3User(final String mud, final String name, final Map<String, Object> data) {
		Map<String, Map<String, Object>> usernames = getI3Mud(mud);

		if (usernames == null)
			usernames = new LinkedHashMap<String, Map<String, Object>>();

		usernames.put(Utils.safePath(name), data);
		setI3Mud(mud, usernames);
	}

	public void setLocalUser(final String name, final Map<String, Object> data) {
		setLocalUser(getUUID(name), data);
	}

	public void setLocalUser(final UUID uuid, final Map<String, Object> data) {
		if (uuid == null)
			return;

		this.localUserCache.put(uuid, data);
	}

	public void tuneIn(final String playerName, final String channel) {
		final UUID uuid = getUUID(playerName);
		final Map<String, Object> user = getLocalUser(uuid);

		if (uuid != null && user != null) {
			@SuppressWarnings("unchecked")
			List<String> tunein = (List<String>) user.get("TUNEIN");

			if (tunein == null)
				tunein = new ArrayList<String>();

			if (!tunein.contains(channel))
				tunein.add(channel);

			user.put("TUNEIN", tunein);
			setLocalUser(uuid, user);

			saveConfig();
		}
	}

	public void tuneOut(final String playerName, final String channel) {
		final UUID uuid = getUUID(playerName);
		final Map<String, Object> user = getLocalUser(uuid);

		if (uuid != null && user != null) {
			@SuppressWarnings("unchecked")
			List<String> tunein = (List<String>) user.get("TUNEIN");

			if (tunein == null)
				tunein = new ArrayList<String>();

			tunein.remove(channel);

			user.put("TUNEIN", tunein);
			setLocalUser(uuid, user);

			saveConfig();
		}
	}
}

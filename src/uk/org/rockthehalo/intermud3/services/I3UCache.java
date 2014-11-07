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

		private static final Map<Long, Gender> numToGender = new HashMap<Long, Gender>(
				values().length);
		private static final Map<String, Gender> nameToGender = new HashMap<String, Gender>(
				values().length);

		private long gender = -1;
		private String name = null;

		private Gender(final long gender, final String name) {
			this.gender = gender;
			this.name = name;
		}

		public long getGender() {
			if (gender == -1)
				return NEUTER.getGender();

			return gender;
		}

		public String getName() {
			if (name == null)
				return NEUTER.getName();

			return name;
		}

		public static Gender getNumGender(final long gender) {
			final Gender g = numToGender.get(gender);

			if (g == null)
				return NEUTER;

			return g;
		}

		public static Gender getNamedGender(final String name) {
			final Gender g = nameToGender.get(name.toLowerCase());

			if (g == null)
				return NEUTER;

			return g;
		}

		static {
			for (final Gender g : Gender.values()) {
				numToGender.put(g.gender, g);
				nameToGender.put(g.name, g);
			}
		}
	}

	private static final int hBeatDelay = 60;
	private static final List<String> shadows = new ArrayList<String>(
			Arrays.asList("apresence", "ashadow", "aninvisibleimmortal",
					"someone"));
	private static final Payload ucachePayload = new Payload(Arrays.asList(
			"UC_USERNAME", "UC_VISNAME", "UC_GENDER"));
	private static final UUID zUuid = UUID
			.fromString("00000000-0000-0000-0000-000000000000");

	private final LPCMapping i3UserCache = new LPCMapping();
	private final Map<String, UUID> localUUIDs = Collections
			.synchronizedMap(new LinkedHashMap<String, UUID>());
	private final Map<UUID, List<Object>> users = Collections
			.synchronizedMap(new LinkedHashMap<UUID, List<Object>>());

	private Config config = null;

	// I3 user data indexes.
	public static final int VISNAME = 0;
	public static final int GENDER = 1;
	public static final int LASTUPDATE = 2;
	public static final int LASTACTIVE = 3;
	public static final int SZ_UCACHE = 4;
	// Local user data indexes. Shares VISNAME and GENDER.
	public static final int TUNEIN = 2;
	public static final int ALIASES = 3;
	public static final int SZ_USERS = 4;

	public I3UCache() {
	}

	/**
	 * @param m
	 * @param u
	 * @param v
	 * @param g
	 */
	public void addUserCache(Object m, Object u, Object v, Object g) {
		if (m == null || u == null || v == null)
			return;

		final String mudname;

		if (String.class.isInstance(m))
			mudname = (String) m;
		else if (Utils.isLPCString(m))
			mudname = ((LPCString) m).toString();
		else
			return;

		if (mudname.isEmpty())
			return;

		final String username;

		if (String.class.isInstance(u))
			username = Utils.stripColor((String) u);
		else if (Utils.isLPCString(u))
			username = Utils.stripColor(((LPCString) u).toString());
		else
			return;

		if (username.isEmpty())
			return;

		final String visname;

		if (String.class.isInstance(v))
			visname = (String) v;
		else if (Utils.isLPCString(v))
			visname = ((LPCString) v).toString();
		else
			return;

		if (visname.isEmpty())
			return;

		final long gender;

		if (Long.class.isInstance(g))
			gender = (long) g;
		else if (Utils.isLPCInt(g))
			gender = ((LPCInt) g).toNum();
		else
			gender = Gender.NEUTER.getGender();

		Log.debug("Adding '" + username + "@" + mudname + "' [" + visname + "/"
				+ getGenderString(gender) + "]");

		if (!mudname.equals(Utils.getServerName())) {
			final long time = System.currentTimeMillis() / 1000;
			final LPCArray data = new LPCArray(SZ_UCACHE);

			data.set(VISNAME, new LPCString(visname));
			data.set(GENDER, new LPCInt(gender));
			data.set(LASTUPDATE, new LPCInt(time));
			data.set(LASTACTIVE, new LPCInt(time));

			LPCMapping usernames = this.i3UserCache
					.getLPCMapping(new LPCString(mudname));

			if (usernames == null)
				usernames = new LPCMapping();

			usernames.put(new LPCString(Utils.safePath(username)), data);
			this.i3UserCache.put(new LPCString(Utils.safePath(mudname)),
					usernames);
		} else {
			final List<Object> data = new ArrayList<Object>(
					Utils.nullList(SZ_USERS));

			data.set(VISNAME, visname);
			data.set(GENDER, gender);

			final UUID uuid = this.localUUIDs.get(username.toLowerCase());

			if (uuid != null && this.users.get(uuid) != null)
				data.set(TUNEIN, this.users.get(uuid).get(TUNEIN));
			else {
				final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

				if (i3Channel == null)
					data.set(TUNEIN, null);
				else {
					final List<String> tunein = new ArrayList<String>(
							i3Channel.getTunein());

					data.set(TUNEIN, tunein);
				}
			}

			if (uuid != null && this.users.get(uuid) != null)
				data.set(ALIASES, this.users.get(uuid).get(ALIASES));
			else {
				final I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

				if (i3Channel == null)
					data.set(ALIASES, null);
				else {
					final Map<String, String> aliases = new LinkedHashMap<String, String>(
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
		final long gender;

		mudname = Utils.stripColor(mudname);
		username = Utils.stripColor(username);

		if (shadows.contains(username.toLowerCase().replaceAll("[ \t]+", ""))) {
			visname = "A Shadow";
			gender = Gender.NEUTER.getGender();
			stamp = false;
		} else
			gender = getGender(mudname, username);

		final boolean local = mudname.equals(Utils.getServerName());

		if ((visname != null && visname.isEmpty()) || gender == -1) {
			Log.debug("Adding user '" + username + "@" + mudname + "'");

			if (visname.isEmpty()) {
				if (!local)
					visname = StringUtils.capitalize(username);
				else {
					final UUID uuid = this.localUUIDs.get(username
							.toLowerCase());

					if (uuid == null)
						visname = username;
					else
						visname = Intermud3.instance.getServer()
								.getOfflinePlayer(username).getName();
				}
			}

			addUserCache(mudname, username, visname, Gender.NEUTER.getGender());

			I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

			if (i3Channel != null)
				i3Channel.sendChanUserReq(mudname, username);
		} else if (!local && stamp) {
			final long time = System.currentTimeMillis() / 1000;
			final long lastUpdate = this.i3UserCache
					.getLPCMapping(new LPCString(Utils.safePath(mudname)))
					.getLPCArray(new LPCString(Utils.safePath(username)))
					.getLPCInt(LASTUPDATE).toNum();

			if (time - lastUpdate > 28 * 24 * 60 * 60) {
				Log.debug("Resetting user '" + username + "@" + mudname + "'");

				if (visname == null || visname.isEmpty())
					visname = StringUtils.capitalize(username);

				addUserCache(mudname, username, visname,
						Gender.NEUTER.getGender());

				I3Channel i3Channel = ServiceType.I3CHANNEL.getService();

				if (i3Channel != null)
					i3Channel.sendChanUserReq(mudname, username);
			} else {
				mudname = Utils.safePath(mudname);
				username = Utils.safePath(username);

				LPCArray data = this.i3UserCache.getLPCMapping(
						new LPCString(mudname)).getLPCArray(
						new LPCString(username));

				data.set(LASTACTIVE, new LPCInt(time));
				this.i3UserCache.getLPCMapping(new LPCString(mudname)).put(
						new LPCString(username), data);
			}
		}

		saveConfig();
	}

	public void checkUser(final LPCString mudname, final LPCString username,
			final LPCString visname, final boolean stamp) {
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

	public Map<String, String> getAliases(final String name) {
		return getAliases(this.localUUIDs.get(name.toLowerCase()));
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getAliases(final UUID uuid) {
		if (uuid == null || !this.users.containsKey(uuid))
			return null;

		return (Map<String, String>) this.users.get(uuid).get(ALIASES);
	}

	public long getGender(final String mudname, String username) {
		username = Utils.stripColor(username);

		if (shadows.contains(username.toLowerCase().replaceAll("[ \t]+", "")))
			return Gender.NEUTER.getGender();

		if (!mudname.equals(Utils.getServerName())) {
			LPCMapping usernames = this.i3UserCache
					.getLPCMapping(new LPCString(Utils.safePath(mudname)));

			if (usernames == null)
				return -1;

			final LPCArray data = usernames.getLPCArray(new LPCString(Utils
					.safePath(username)));

			if (data == null)
				return -1;

			return data.getLPCInt(GENDER).toNum();
		} else {
			final UUID uuid = this.localUUIDs.get(username.toLowerCase());

			if (uuid == null || !this.users.containsKey(uuid))
				return -1;

			return (Long) this.users.get(uuid).get(GENDER);
		}
	}

	public long getGender(final LPCString mudname, final LPCString username) {
		return getGender(mudname.toString(), username.toString());
	}

	public String getGenderString(final long gender) {
		final Gender g = Gender.getNumGender(gender);

		if (g == null)
			return "other(" + gender + ")";

		return g.getName();
	}

	public List<Object> getLocalUser(final String name) {
		return getLocalUser(this.localUUIDs.get(name.toLowerCase()));
	}

	public List<Object> getLocalUser(final UUID uuid) {
		if (uuid == null)
			return null;

		return this.users.get(uuid);
	}

	public Map<String, String> getReverseAliases(final String name) {
		return getReverseAliases(this.localUUIDs.get(name.toLowerCase()));
	}

	public Map<String, String> getReverseAliases(final UUID uuid) {
		if (uuid == null || !this.users.containsKey(uuid))
			return null;

		synchronized (this.users) {
			@SuppressWarnings("unchecked")
			final Map<String, String> aliases = (Map<String, String>) this.users
					.get(uuid).get(ALIASES);
			final Map<String, String> reverse = new LinkedHashMap<String, String>(
					aliases.size());

			for (final Entry<String, String> alias : aliases.entrySet())
				reverse.put(alias.getValue(), alias.getKey());

			return reverse;
		}
	}

	public List<String> getTunein(final String name) {
		return getTunein(this.localUUIDs.get(name.toLowerCase()));
	}

	@SuppressWarnings("unchecked")
	public List<String> getTunein(final UUID uuid) {
		if (uuid == null || !this.users.containsKey(uuid))
			return null;

		return (List<String>) this.users.get(uuid).get(TUNEIN);
	}

	public String getVisname(final String mudname, String username) {
		username = Utils.stripColor(username);

		if (shadows.contains(username.toLowerCase().replaceAll("[ \t]+", "")))
			return "A Shadow";

		if (!mudname.equals(Utils.getServerName())) {
			final LPCMapping usernames = this.i3UserCache
					.getLPCMapping(new LPCString(Utils.safePath(mudname)));

			if (usernames == null)
				return null;

			final LPCArray data = usernames.getLPCArray(new LPCString(Utils
					.safePath(username)));

			if (data == null)
				return null;

			return data.getLPCString(VISNAME).toString();
		} else {
			final UUID uuid = this.localUUIDs.get(username.toLowerCase());

			if (uuid == null || !this.users.containsKey(uuid))
				return null;

			return (String) this.users.get(uuid).get(VISNAME);
		}
	}

	public String getVisname(final LPCString mudname, final LPCString username) {
		return getVisname(mudname.toString(), username.toString());
	}

	public void heartBeat() {
		synchronized (this.i3UserCache) {
			final LPCMapping ucCopy = this.i3UserCache.clone();
			final long time = System.currentTimeMillis() / 1000;

			for (final Entry<Object, Object> mud : ucCopy.entrySet()) {
				final LPCString mudname = (LPCString) mud.getKey();

				if (mudname.toString().startsWith("Dead_Souls_")
						|| mudname.toString().startsWith("Unnamed_CoffeeMUD#")) {
					removeUserCache(mudname);

					continue;
				}

				final LPCMapping usernames = ucCopy.getLPCMapping(mudname);

				for (final Entry<Object, Object> user : usernames.entrySet()) {
					final LPCString username = (LPCString) user.getKey();
					final LPCArray data = (LPCArray) user.getValue();
					final LPCString visname = data.getLPCString(VISNAME);
					final long lastUpdate = data.getLPCInt(LASTUPDATE).toNum();
					final long lastActive = data.getLPCInt(LASTACTIVE).toNum();
					final long lastUpdateDiff = time - lastUpdate;
					final long lastActiveDiff = time - lastActive;

					if (shadows.contains(username.toLowerCase().replaceAll(
							"[ \t]+", "")))
						removeUserCache(mudname, username);
					else if (shadows.contains(Utils.stripColor(visname
							.toLowerCase().replaceAll("[ \t]+", "")))) {
						data.set(
								VISNAME,
								new LPCString(StringUtils.capitalize(username
										.toString())));
						usernames.put(new LPCString(username), data);
						this.i3UserCache.put(new LPCString(mudname), usernames);
					} else if (lastActive != 0
							&& lastActiveDiff > 7 * 24 * 60 * 60)
						removeUserCache(mudname, username);
					else if (lastUpdateDiff > 28 * 24 * 60 * 60) {
						Log.debug("heartBeat: Reset user '" + username
								+ "' for mud '" + mudname + "'");
						data.set(GENDER, new LPCInt(-1));
						data.set(LASTUPDATE, new LPCInt(time));
						usernames.put(new LPCString(username), data);
						this.i3UserCache.put(new LPCString(mudname), usernames);
					}
				}
			}
		}

		if (this.users.keySet().contains(zUuid)) {
			final List<Object> data = this.users.get(zUuid);
			final String visname = (String) data.get(VISNAME);

			this.users.remove(zUuid);
			this.users.put(this.localUUIDs.get(visname.toLowerCase()), data);
		}

		saveConfig();
	}

	private String tmpName;

	@EventHandler
	void onPlayerJoin(final PlayerJoinEvent event) {
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

			try {
				this.i3UserCache.setLPCData(Utils.toObject(root
						.getString("ucache")));
			} catch (I3Exception i3E) {
				i3E.printStackTrace();
			}
		}

		if (root.contains("users")) {
			this.users.clear();

			final ConfigurationSection usersSection = root
					.getConfigurationSection("users");

			for (final String user : usersSection.getKeys(false)) {
				final UUID uuid;

				try {
					uuid = UUID.fromString(user);
				} catch (IllegalArgumentException iaE) {
					continue;
				}

				final ConfigurationSection uuidSection = usersSection
						.getConfigurationSection(user);
				final List<Object> data = new ArrayList<Object>(
						Utils.nullList(SZ_USERS));

				data.set(VISNAME, uuidSection.getString("visname"));
				data.set(GENDER,
						Gender.getNamedGender(uuidSection.getString("gender"))
								.getGender());
				data.set(TUNEIN, uuidSection.getList("tunein"));

				final ConfigurationSection aliasesSection = uuidSection
						.getConfigurationSection("aliases");
				final Set<String> aliases = aliasesSection.getKeys(false);
				final Map<String, String> dataAliases = new LinkedHashMap<String, String>();

				for (final String alias : aliases)
					dataAliases.put(alias, aliasesSection.getString(alias));

				data.set(ALIASES, dataAliases);

				this.users.put(uuid, data);
			}
		}

		if (root.contains("i3users")) {
			this.i3UserCache.clear();

			final ConfigurationSection i3UsersSection = root
					.getConfigurationSection("i3users");

			for (final String mudname : i3UsersSection.getKeys(false)) {
				final ConfigurationSection mudSection = i3UsersSection
						.getConfigurationSection(mudname);
				final LPCMapping users = new LPCMapping();

				for (final String user : mudSection.getKeys(false)) {
					final ConfigurationSection userSection = mudSection
							.getConfigurationSection(user);
					final LPCArray data = new LPCArray(SZ_UCACHE);

					data.set(VISNAME,
							new LPCString(userSection.getString("visname")));
					data.set(
							GENDER,
							new LPCInt(Gender.getNamedGender(
									userSection.getString("gender"))
									.getGender()));

					long lastUpdate = userSection.getLong("lastupdate");

					if (lastUpdate < 0)
						lastUpdate = -lastUpdate;

					data.set(LASTUPDATE, new LPCInt(lastUpdate));
					data.set(LASTACTIVE,
							new LPCInt(userSection.getLong("lastactive")));

					users.put(new LPCString(user), data);
				}

				this.i3UserCache.put(new LPCString(mudname), users);
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
	}

	public void removeUserCache() {
		final LPCArray muds = new LPCArray(this.i3UserCache.keySet());

		for (final Object obj : muds) {
			final LPCString mud = (LPCString) obj;

			Log.debug("removeUserCache: Deleting mud '" + mud + "'");
			this.i3UserCache.remove(mud);
		}
	}

	/**
	 * @param mudname
	 */
	public void removeUserCache(final String mudname) {
		removeUserCache(new LPCString(mudname));
	}

	/**
	 * @param mudname
	 */
	public void removeUserCache(final LPCString mudname) {
		Log.debug("removeUserCache: Deleting mud '" + mudname + "'");
		this.i3UserCache.remove(mudname);
	}

	/**
	 * @param mudname
	 * @param username
	 */
	public void removeUserCache(final String mudname, final String username) {
		removeUserCache(new LPCString(mudname), new LPCString(username));
	}

	/**
	 * @param mudname
	 * @param username
	 */
	public void removeUserCache(final LPCString mudname,
			final LPCString username) {
		final LPCArray muds = new LPCArray();

		if (mudname == null)
			muds.addAll(this.i3UserCache.keySet());
		else if (username == null)
			muds.add(mudname);
		else {
			Log.debug("removeUserCache: Deleting user '" + username
					+ "' from mud '" + mudname + "'");

			final LPCMapping users = new LPCMapping(
					this.i3UserCache.getLPCMapping(mudname));

			users.remove(new LPCString(Utils.safePath(username.toString())));
			this.i3UserCache.put(mudname, users);

			if (users.isEmpty())
				muds.add(mudname);
		}

		for (final Object obj : muds) {
			final LPCString mud = (LPCString) obj;

			Log.debug("removeUserCache: Deleting mud '" + mud + "'");
			this.i3UserCache.remove(mud);
		}
	}

	@Override
	public void replyHandler(final Packet packet) {
		addUserCache(packet.getLPCString(Payload.O_MUD),
				packet.getLPCString(ucachePayload.get("UC_USERNAME")),
				packet.getLPCString(ucachePayload.get("UC_VISNAME")),
				packet.getLPCInt(ucachePayload.get("UC_GENDER")));
	}

	@Override
	public void reqHandler(final Packet packet) {
		// Not used.
	}

	/**
	 * @param name
	 */
	public void resetUCacheUpdate(final String name) {
		final UUID uuid = this.localUUIDs.get(name.toLowerCase());

		if (uuid != null) {
			this.users.remove(uuid);
			saveConfig();
		}
	}

	public void saveConfig() {
		saveConfig(false);
	}

	public void saveConfig(final boolean flag) {
		// Clear the configuration.
		this.config.clearConfig();

		final FileConfiguration root = this.config.getConfig();
		final ConfigurationSection usersSection = root.createSection("users");

		synchronized (this.users) {
			for (final Entry<UUID, List<Object>> user : this.users.entrySet()) {
				final ConfigurationSection uuidSection = usersSection
						.createSection(user.getKey().toString());
				final List<Object> data = user.getValue();

				uuidSection.set("visname", data.get(VISNAME));
				uuidSection.set("gender",
						Gender.getNumGender((Long) data.get(GENDER)).getName());
				uuidSection.set("tunein", data.get(TUNEIN));

				final ConfigurationSection aliasesSection = uuidSection
						.createSection("aliases");
				@SuppressWarnings("unchecked")
				final Map<String, String> aliases = (LinkedHashMap<String, String>) data
						.get(ALIASES);

				for (final Entry<String, String> alias : aliases.entrySet())
					aliasesSection.set(alias.getKey(), alias.getValue());
			}
		}

		final ConfigurationSection i3UsersSection = root
				.createSection("i3users");

		synchronized (this.i3UserCache) {
			for (final Entry<Object, Object> mud : this.i3UserCache.entrySet()) {
				final ConfigurationSection mudSection = i3UsersSection
						.createSection(Utils.safePath(mud.getKey().toString()));
				final LPCMapping users = (LPCMapping) mud.getValue();

				for (final Entry<Object, Object> user : users.entrySet()) {
					final ConfigurationSection userSection = mudSection
							.createSection(Utils.safePath(user.getKey()
									.toString()));
					final LPCArray data = (LPCArray) user.getValue();

					userSection.set("visname", data.get(VISNAME).toString());

					Gender gender = Gender.getNumGender(((LPCInt) data
							.get(GENDER)).toNum());

					if (gender == null)
						gender = Gender.NEUTER;

					userSection.set("gender", gender.getName());
					userSection.set("lastupdate",
							((LPCInt) data.get(LASTUPDATE)).toNum());
					userSection.set("lastactive",
							((LPCInt) data.get(LASTACTIVE)).toNum());
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
	public void sendUCacheUpdate(final String name, boolean update) {
		final UUID bkUUID = Bukkit.getPlayer(name).getUniqueId();
		final String lname = name.toLowerCase();

		if (!update && bkUUID.equals(this.localUUIDs.get(lname)))
			return;

		this.localUUIDs.put(lname, bkUUID);

		UUID uuid = bkUUID;

		if (!this.users.containsKey(bkUUID)
				|| !this.users.get(bkUUID).get(VISNAME).toString().equals(name)) {
			update = true;
			uuid = Intermud3.uuid.getIdOptimistic(name);

			if (uuid == null || uuid.equals(zUuid))
				uuid = bkUUID;

			if (!bkUUID.equals(uuid))
				this.localUUIDs.put(lname, uuid);
		}

		if (this.users.containsKey(uuid)) {
			if (update)
				this.users.remove(uuid);
			else
				return;
		}

		final Packet payload = new Packet();

		payload.add(new LPCString(lname));
		payload.add(new LPCString(name));
		payload.add(new LPCInt(Gender.NEUTER.getGender()));

		Intermud3.network.sendToAll(PacketType.UCACHE_UPDATE, null, payload);
	}

	public void setAlias(final Player player, final String alias,
			final String channel) {
		final UUID uuid = this.localUUIDs.get(player.getName().toLowerCase());
		final List<Object> user = this.users.get(uuid);

		if (uuid != null && user != null) {
			@SuppressWarnings("unchecked")
			Map<String, String> aliases = (Map<String, String>) user
					.get(ALIASES);

			if (aliases == null)
				aliases = new LinkedHashMap<String, String>();

			if (channel == null)
				aliases.remove(alias);
			else
				aliases.put(alias, channel);

			user.set(ALIASES, aliases);
			this.users.put(uuid, user);

			saveConfig();
		}
	}

	public void tuneIn(final Player player, final String channel) {
		final UUID uuid = this.localUUIDs.get(player.getName().toLowerCase());
		final List<Object> user = this.users.get(uuid);

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

	public void tuneOut(final Player player, final String channel) {
		final UUID uuid = this.localUUIDs.get(player.getName().toLowerCase());
		final List<Object> user = this.users.get(uuid);

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

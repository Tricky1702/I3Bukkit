package uk.org.rockthehalo.intermud3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Config {
	private final JavaPlugin plugin;
	private final String resourceFileName;

	private FileConfiguration config = null;
	private File configFile = null;

	public Config(JavaPlugin plugin, String fileName) {
		this.plugin = plugin;
		this.resourceFileName = fileName;
		this.configFile = new File(plugin.getDataFolder(), fileName);
	}

	public Config(JavaPlugin plugin, String fileName, String resourceFileName) {
		this.plugin = plugin;
		this.resourceFileName = resourceFileName;
		this.configFile = new File(plugin.getDataFolder(), fileName);
	}

	public void clearConfig() {
		for (String key : this.config.getKeys(false))
			this.config.set(key, null);
	}

	public FileConfiguration getConfig() {
		if (this.config == null)
			reloadConfig();

		return this.config;
	}

	public File getFile() {
		return this.configFile;
	}

	public InputStream getResource() {
		return this.plugin.getResource(this.resourceFileName);
	}

	public void reloadConfig() {
		this.config = YamlConfiguration.loadConfiguration(this.configFile);

		// Look for defaults in the jar
		InputStream defConfigStream = getResource();

		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration
					.loadConfiguration(defConfigStream);
			this.config.setDefaults(defConfig);
		}
	}

	public void remove() {
		// Remove references.
		this.config = null;
		this.configFile = null;
	}

	public void saveConfig() {
		if (this.config == null || this.configFile == null)
			return;

		try {
			getConfig().save(this.configFile);
		} catch (IOException ioE) {
			Log.error("Could not save config to " + this.configFile, ioE);
		}
	}

	public void saveDefaultConfig() {
		if (!this.configFile.exists())
			this.plugin.saveResource(this.resourceFileName, false);
	}
}

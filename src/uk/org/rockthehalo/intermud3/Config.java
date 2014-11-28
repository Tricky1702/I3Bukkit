package uk.org.rockthehalo.intermud3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Config {
	private static final String defConfigFilename = "config.yml";

	private final String resourceFileName;
	private final File configFile;

	private FileConfiguration config = null;

	public Config() {
		this.resourceFileName = defConfigFilename;
		this.configFile = new File(Intermud3.plugin.getDataFolder(), defConfigFilename);
	}

	public Config(final String fileName) {
		this.resourceFileName = fileName;
		this.configFile = new File(Intermud3.plugin.getDataFolder(), fileName);
	}

	public Config(final String fileName, final String resourceFileName) {
		this.resourceFileName = resourceFileName;
		this.configFile = new File(Intermud3.plugin.getDataFolder(), fileName);
	}

	public void clearConfig() {
		for (final String key : this.config.getKeys(false))
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
		return Intermud3.plugin.getResource(this.resourceFileName);
	}

	public void reloadConfig() {
		this.config = YamlConfiguration.loadConfiguration(this.configFile);

		// Look for defaults in the jar
		final InputStream defConfigStream = getResource();

		if (defConfigStream != null) {
			YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
			this.config.setDefaults(defConfig);
		}
	}

	public void remove() {
		// Remove references.
		this.config = null;
	}

	public void saveConfig() {
		if (this.config == null || this.configFile == null)
			return;

		try {
			this.config.save(this.configFile);
		} catch (IOException ioE) {
			Log.error("Could not save config to " + this.configFile, ioE);
		}
	}

	public void saveDefaultConfig() {
		if (!this.configFile.exists())
			Intermud3.plugin.saveResource(this.resourceFileName, false);
	}
}

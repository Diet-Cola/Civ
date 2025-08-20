package me.josvth.randomspawn.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import me.josvth.randomspawn.RandomSpawn;
import me.josvth.randomspawn.config.config.Config;
import me.josvth.randomspawn.config.worlds.WorldConfig;
import org.apache.commons.lang3.function.Failable;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vg.civcraft.mc.civmodcore.config.errors.ConfigFileException;
import vg.civcraft.mc.civmodcore.config.ConfigHelpers;
import vg.civcraft.mc.civmodcore.config.errors.ConfigParseException;
import vg.civcraft.mc.civmodcore.config.ConfigValueType;

public class Configs {
    private static final Logger LOGGER = LoggerFactory.getLogger(Configs.class);

    public final Map<String, WorldConfig> worlds = new ConcurrentHashMap<>();
    public volatile Config config = Config.DEFAULT;

    private final File configFile;
    private final File worldsFile;

    public final FileConfiguration configYaml;
    public final FileConfiguration worldsYaml;

    public Configs(
        final @NotNull RandomSpawn plugin
    ) {
        Objects.requireNonNull(plugin);
        this.configFile = plugin.ensureDataFile("config.yml");
        this.worldsFile = plugin.ensureDataFile("worlds.yml");
        this.configYaml = new YamlConfiguration();
        this.worldsYaml = new YamlConfiguration();
    }

    public void loadConfigFile() throws ConfigFileException, ConfigParseException {
        try {
            this.configYaml.load(this.configFile);
        }
        catch (final FileNotFoundException e) {
            throw new ConfigFileException("Could not parse RandomSpawn config.yml as it's missing!", e);
        }
        catch (final IOException e) {
            throw new ConfigFileException("Could not parse RandomSpawn config.yml as it could not be read!", e);
        }
        catch (final InvalidConfigurationException e) {
            throw new ConfigFileException("Could not parse RandomSpawn config.yml as it's invalid YAML!", e);
        }
        parseConfigYaml();
    }

    public void parseConfigYaml() throws ConfigParseException {
        this.config = Config.parse(this.configYaml);
    }

    public void loadWorldsFile() throws ConfigFileException, ConfigParseException {
        try {
            this.worldsYaml.load(this.worldsFile);
        }
        catch (final FileNotFoundException e) {
            throw new ConfigFileException("Could not parse RandomSpawn worlds.yml as it's missing!", e);
        }
        catch (final IOException e) {
            throw new ConfigFileException("Could not parse RandomSpawn worlds.yml as it could not be read!", e);
        }
        catch (final InvalidConfigurationException e) {
            throw new ConfigFileException("Could not parse RandomSpawn worlds.yml as it's invalid YAML!", e);
        }
        parseWorldsYaml();
    }

    public void parseWorldsYaml() throws ConfigParseException {
        this.worlds.clear();
        for (final String worldName : this.worldsYaml.getKeys(false)) {
            final ConfigurationSection worldSection = ConfigHelpers.get(this.worldsYaml, worldName, ConfigValueType.SECTION);
            this.worlds.put(worldName, WorldConfig.parse(worldSection));
        }
    }

    public void saveConfigFile() {
        try {
            this.configYaml.save(this.configFile);
        }
        catch (final IOException e) {
            LOGGER.warn("Could not save config.yml file!", e);
        }
    }

    public void saveWorldsFile() {
        try {
            this.worldsYaml.save(this.worldsFile);
        }
        catch (final IOException e) {
            LOGGER.warn("Could not save worlds.yml file!", e);
            return;
        }
        Failable.run(this::parseWorldsYaml);
    }
}

package me.armar.plugins.autorank.config;

import me.armar.plugins.autorank.Autorank;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ConcurrentModificationException;

/**
 * This represents any YAML file that Autorank uses. <br>
 * It is used for the storage.yml, playerdata.yml and daily/monthly/weekly storage
 * files.
 *
 * @author Staartvin
 */
public class SimpleYamlConfiguration extends YamlConfiguration {

    File file;

    /**
     * Create a new YAML file.
     *
     * @param plugin   Plugin to create it for.
     * @param fileName Path of the file.
     * @param name     Name of the file that is used to show in the console.
     */
    public SimpleYamlConfiguration(final Autorank plugin, final String fileName, final String name) {
        /*
         * accepts null as configDefaults -> check for resource and copies it if
         * found, makes an empty config if nothing is found
         */
        final String folderPath = plugin.getDataFolder().getAbsolutePath() + File.separator;
        file = new File(folderPath + fileName);

        if (!file.exists()) {
            if (plugin.getResource(fileName) != null) {
                plugin.saveResource(fileName, false);
                plugin.debugMessage("New " + name + " file copied from jar");
                try {
                    this.load(file);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                this.load(file);
                plugin.debugMessage(name + " file loaded");
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Get the internal YAML file.
     */
    public File getInternalFile() {
        return file;
    }

    /**
     * Load the YAML file.
     */
    public void loadFile() {
        try {
            this.load(file);
        } catch (final FileNotFoundException e) {

        } catch (final IOException e) {

        } catch (final InvalidConfigurationException e) {

        }
    }

    /**
     * Reload the YAML file.
     */
    public void reloadFile() {
        loadFile();
        saveFile();
    }

    /**
     * Save the YAML file.
     */
    public void saveFile() {
        try {

            if (file == null) {
                Autorank.getInstance().debugMessage("Can't save file, because it's null!");
                return;
            }

            this.save(file);
        } catch (final ConcurrentModificationException e) {
            saveFile();
        } catch (NullPointerException npe) {
            Autorank.getInstance().debugMessage("Save file thrown NPE:" + npe.getMessage());
            Autorank.getInstance().debugMessage("FILE TO SAVE: " + file);
            npe.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}

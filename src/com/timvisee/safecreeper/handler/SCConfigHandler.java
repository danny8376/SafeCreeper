package com.timvisee.safecreeper.handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;

import com.garbagemule.MobArena.framework.Arena;
import com.timvisee.safecreeper.SafeCreeper;
import com.timvisee.safecreeper.event.config.SCGlobalConfigLoadEvent;
import com.timvisee.safecreeper.event.config.SCWorldConfigLoadEvent;
import com.timvisee.safecreeper.event.config.SCWorldConfigsLoadEvent;
import com.timvisee.safecreeper.util.SCUtils;

public class SCConfigHandler {

	private File globalConfigFile = new File("plugins/SafeCreeper/global.yml");
	private File worldConfigsDir = new File("plugins/SafeCreeper/worlds");
	private FileConfiguration globalConfig;
	private HashMap<String, FileConfiguration> worldConfigs = new HashMap<String, FileConfiguration>();

	/**
	 * Constructor
	 */
	public SCConfigHandler() {
		// Instantly setup the manager after construction it
		setup();
	}
	
	/**
	 * Constructor
	 * @param globalConfigFile Global config file path
	 * @param worldConfigsFolder World configs folder
	 */
	public SCConfigHandler(File globalConfigFile, File worldConfigsFolder) {
		this.globalConfigFile = globalConfigFile;
		this.worldConfigsDir = worldConfigsFolder;
		
		// Instantly setup the manager after construction it
		setup();
	}
	
	/**
	 * Setup the config manager
	 */
	public void setup() {
		// Reload the global config
		reloadGlobalConfig();
		
		// Reload the world configs
		reloadWorldConfigs();
	}
	
	/**
	 * Reload an external YAML config file
	 * @param file The file to load from
	 * @return the config file
	 */
	public FileConfiguration getConfigFromPath(File file) {
		if (file == null)
		    return null;
	    return (FileConfiguration) YamlConfiguration.loadConfiguration(file);
	}
    
	/**
	 * Reload all the configs
	 */
	public void reloadAllConfigs() {
		// Reload all the configs
		reloadGlobalConfig();
		reloadWorldConfigs();
	}
	
	/**
	 * Reload the global config file
	 * @return
	 */
    public void reloadGlobalConfig() {
    	// Store the time the file started to load
    	long t = System.currentTimeMillis();
    	
    	// Call the config load event
    	SCGlobalConfigLoadEvent event = new SCGlobalConfigLoadEvent(this.globalConfigFile);
    	Bukkit.getPluginManager().callEvent(event);
    	
    	// Get the file path to the global config file to load
    	File configFile = event.getConfigFile();
    	if(configFile == null)
    		configFile = this.globalConfigFile;
    	
    	// Load the config file
    	System.out.println("[SafeCreeper] Loading global config...");
    	this.globalConfig = getConfigFromPath(configFile);
    	
    	// Calculate the loading duration
    	long duration = System.currentTimeMillis() - t;
    	
    	// Show a status message
    	System.out.println("[SafeCreeper] Global config loaded, took " + duration + " ms!");
    }
    
    /**
     * Get the global config
     * @return The global config
     */
    public FileConfiguration getGlobalConfig() {
    	return this.globalConfig;
    }
    
    /**
     * Set the global config
     * @param c the new config
     */
    public void setGlobalConfig(FileConfiguration c) {
    	if(c != null)
    		this.globalConfig = c;
    }
    
    /**
     * Get the global config file
     * @return The global config file
     */
    public File getGlobalConfigFile() {
    	return this.globalConfigFile;
    }
    
    /**
     * Get the global config file
     * @return The global config file
     */
    public File getWorldConfigFile(String w) {
    	return new File(this.worldConfigsDir, w + ".yml");
    }
    
    /**
     * Save a world config file
     * @param w the world name
     * @return true if succeed
     */
    public boolean saveWorldConfig(String w) {
		long t = System.currentTimeMillis();
	
		System.out.println("[SafeCreeper] Saving world config '" + w + "' file...");
		
    	try {
    		if(this.worldConfigs.containsKey(w)) {
    			File worldFile = new File(worldConfigsDir, w + ".yml");
    			
        		if(!worldFile.exists())
        			worldFile.createNewFile();
        		
    			this.worldConfigs.get(w).save(worldFile);
    			
    			// Calculate the load duration
    			long duration = System.currentTimeMillis() - t;
    			System.out.println("[SafeCreeper] World config '" + w + "' saved, took " + String.valueOf(duration) + " ms!");
    		}
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Calculate the load duration
		long duration = System.currentTimeMillis() - t;
		System.out.println("[SafeCreeper] Error while saving world config '" + w + "' after " + String.valueOf(duration) + " ms!");
		return false;
    }
    
    public List<String> listConfigWorlds() {
    	File dir = this.worldConfigsDir;
    	List<File> worldFiles = Arrays.asList(dir.listFiles());
    	List<String> worlds = new ArrayList<String>();
    	
    	for(File f : worldFiles) {
    		worlds.add(f.getName().replaceFirst("[.][^.]+$", ""));
    	}
    	return worlds;
    }
    
    /**
     * Reload all world config files
     * @return true if succeed
     */
    public void reloadWorldConfigs() {
    	// Store the time the file started to load
    	long t = System.currentTimeMillis();
    	
    	// Show a status message
    	SafeCreeper.instance.getSCLogger().info("Loading world configs...");

    	// Call the configs load event
    	SCWorldConfigsLoadEvent configsLoadEvent = new SCWorldConfigsLoadEvent(this.worldConfigsDir);
    	Bukkit.getPluginManager().callEvent(configsLoadEvent);
    	
    	// Check if the event was cancelled
    	if(configsLoadEvent.isCancelled()) {
    		SafeCreeper.instance.getSCLogger().info("Loading cancelled!");
    		return;
    	}
    	
    	// Get the world configs directory
    	File worldConfigsDir = configsLoadEvent.getWorldConfigsDirectory();
    	if(worldConfigsDir == null)
    		worldConfigsDir = this.worldConfigsDir;
    	
    	// Clear the current list of world configs
    	worldConfigs.clear();

    	// List the files inside the world configs directory
    	List<File> worldConfigFolderFiles = Arrays.asList(worldConfigsDir.listFiles());
        
    	// Check each file and load the config if it's a world config file
        for(File item : worldConfigFolderFiles) {
    		if(getFileExtention(item.getAbsolutePath()).equals("yml") || getFileExtention(item.getAbsolutePath()).equals("yaml")) {
    			// Get the name of the world of the current entry
    			String worldName = getFileName(item.getAbsolutePath());
    			
    			// Call the world config load event
    	    	SCWorldConfigLoadEvent configLoadEvent = new SCWorldConfigLoadEvent(worldName, item);
    	    	Bukkit.getPluginManager().callEvent(configLoadEvent);
    	    	
    	    	// Chekc if the event was cancelled
    	    	if(configLoadEvent.isCancelled()) {
    	    		SafeCreeper.instance.getSCLogger().info("Loading cancelled for the world '" + worldName + "'!");
    	    		return;
    	    	}
    	    	
    	    	// Get the file path of the world
    	    	File configPath = configLoadEvent.getWorldConfigFile();
    	    	if(configPath == null)
    	    		configPath = item;
    			
    			// Load the world config file
    			worldConfigs.put(worldName, getConfigFromPath(item));
    		}
    	}
        
        // Calculate the loading duration
    	long duration = System.currentTimeMillis() - t;
    	
    	// Show a status message
    	SafeCreeper.instance.getSCLogger().info(String.valueOf(this.worldConfigs.size()) + " world configs loaded, took " + duration + " ms!");
    }
    
    /**
     * Get the config file of a world
     * @param w
     * @return
     */
    public FileConfiguration getWorldConfig(String w) {
    	return worldConfigExist(w) ? worldConfigs.get(w) : getGlobalConfig();
    }
    
    /**
     * Check if a world config file exists
     * @param w world name
     * @return return true if it exists
     */
    public boolean worldConfigExist(String w) {
    	return worldConfigs.containsKey(w);
    }
    
    /**
     * Get all loaded world configs
     * @return all loaded world configs
     */
    public List<FileConfiguration> getWorldConfigs() {
    	List<FileConfiguration> c = new ArrayList<FileConfiguration>();
    	for(FileConfiguration e : c)
    		c.add(e);
    	return c;
    }
    
    /**
     * Set a world config
     * @param w
     * @param c
     */
    public void setWorldConfig(String w, FileConfiguration c) {
    	if(this.worldConfigs.containsKey(w))
    		this.worldConfigs.remove(w);
    	this.worldConfigs.put(w, c);
    }
    
    /**
     * Add a world config
     * @param w the world name
     * @param c the config
     */
    public void addWorldConfig(String w, FileConfiguration c) {
    	if(this.worldConfigs.containsKey(w))
    		this.worldConfigs.remove(w);
    	this.worldConfigs.put(w, c);
    }
    
    /**
     * Save the global config to an external file
     * @return
     */
    public boolean saveGlobalConfig() {
		long t = System.currentTimeMillis();
		
    	System.out.println("[SafeCreeper] Saving global config file...");
    	try {
			this.globalConfig.save(globalConfigFile);
			
			// Calculate the load duration
			long duration = System.currentTimeMillis() - t;
			System.out.println("[SafeCreeper] Global config saved, took " + String.valueOf(duration) + " ms!");
			
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
		// Calculate the load duration
		long duration = System.currentTimeMillis() - t;
    	System.out.println("[SafeCreeper] Error while saving global config after " + String.valueOf(duration) + " ms!");
		return false;
    }
    
    /**
     * Handle the locations node inside controls
     * @param w World name
     * @param loc Location
     * @param locationsSection Path to the Locations section
     * @param keyName Key name
     * @return List of configuration section paths to apply
     */
    private List<String> handleLocations(String w, Location loc, String locationsSection, String keyName) {
    	// Create a new array list to store all the configuration sections in
    	List<String> out = new ArrayList<String>();
    	
    	// Make sure the world name is not null or empty
    	if(w == null)
    		return out;
    	if(w.trim().equals(""))
    		return out;
    	
    	// Make sure the locations section is not null
    	if(locationsSection == null)
    		return out;
    	
    	// Make sure the locations section is set
    	if(locationsSection.trim().equals(""))
    		return out;
    	
    	// Does the locations section contains the 'Levels' node
		if(configNodeExists(w, locationsSection + ".Levels")) {
			List<String> keysLevels = configGetKeys(w, locationsSection + ".Levels");
			
			// Levels
			for(String entry : keysLevels) {
				List<String> keysSplitted = Arrays.asList(entry.split(","));
				
				boolean useEntry = false;
				
				for(String entrySplitted : keysSplitted) {
					
					if(entrySplitted.trim().equals("*"))
						useEntry = true;
					
					else if(!entrySplitted.trim().contains("-")) {
						
						// If the value isn't an integer, if itisn't show an error and continue in the for loop
						if(!SCUtils.isInt(entrySplitted)) {
							System.out.println("[SafeCreeper] [ERROR] Value is not an integer: " + entrySplitted);
							continue;
						}
						
						int level = Integer.parseInt(entrySplitted);
						if(loc.getY() == level)
							useEntry = true;
						
					} else {
						
						List<String> values = Arrays.asList(entrySplitted.split("-"));
						
						// If the value isn't an integer, if itisn't show an error and continue in the for loop
						if(!SCUtils.isInt(values.get(0)) || !SCUtils.isInt(values.get(1))) {
							System.out.println("[SafeCreeper] [Error] Value is not an integer: " + entrySplitted);
							continue;
						}
						
						int minLevel = Math.min(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)));
						int maxLevel = Math.max(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)));
						if(loc.getY() >= minLevel && loc.getY() <= maxLevel)
							useEntry = true;
					}
				}
				
				// Add the section to the output list if it should be used and if it does exist
				if(useEntry)
					if(configNodeExists(w, locationsSection + ".Levels." + entry + "." + keyName))
						out.add(locationsSection + ".Levels." + entry + "." + keyName);
			}
		}
		
		// Check if MobArena is enabled, if so check if the control is enabled
		if(configNodeExists(w, locationsSection + ".MobArena")) {
			List<String> keysMobArena = configGetKeys(w, locationsSection + ".MobArena");
			
			// Mob Arena
			for(String entry : keysMobArena) {
				List<String> keysSplitted = Arrays.asList(entry.split(","));
				
				boolean useEntry = false;
				
				for(String entrySplitted : keysSplitted) {
					
					if(entrySplitted.trim().equals("*")) {
						
						// The location has to be in an arena
						if(!SafeCreeper.instance.getMobArenaManager().isInArena(loc))
							continue;
						
						useEntry = true;
					
					} else {
						
						// The location has to be in an arena
						if(!SafeCreeper.instance.getMobArenaManager().isInArena(loc))
							continue;
						
						Arena a = SafeCreeper.instance.getMobArenaManager().getArenaAt(loc);
						
						if(a.configName().equals(entrySplitted.trim()))
							useEntry = true;
					}
				}
				
				// Add the section to the output list if it should be used and if it does exist
				if(useEntry)
					if(configNodeExists(w, locationsSection + ".MobArena." + entry + "." + keyName))
						out.add(locationsSection + ".MobArena." + entry + "." + keyName);
			}
		}
		
		// Check if PVPArena is enabled, if so check if the control is enabled
		if(configNodeExists(w, locationsSection + ".PVPArena")) {
			List<String> keys = configGetKeys(w, locationsSection + ".PVPArena");
			
			// Mob Arena
			for(String entry : keys) {
				List<String> keysSplitted = Arrays.asList(entry.split(","));
				
				boolean useEntry = false;
				
				for(String entrySplitted : keysSplitted) {
					
					if(entrySplitted.trim().equals("*")) {
						if(SafeCreeper.instance.getPVPArenaManager().isPVPArenaAt(loc))
							useEntry = true;
					
					} else {
						
						// The location has to be in an arena
						if(!SafeCreeper.instance.getPVPArenaManager().isPVPArenaAt(loc))
							continue;
						
						String arenaName = SafeCreeper.instance.getPVPArenaManager().getPVPArenaAt(loc);
						
						if(arenaName.equals(entrySplitted.trim()))
							useEntry = true;
					}
				}

				// Add the section to the output list if it should be used and if it does exist
				if(useEntry)
					if(configNodeExists(w, locationsSection + ".PVPArena." + entry + "." + keyName))
						out.add(locationsSection + ".PVPArena." + entry + "." + keyName);
			}
		}
		
		// Check if Factions are enabled, if so check if the control is enabled
		if(configNodeExists(w, locationsSection + ".Factions")) {
			List<String> keys = configGetKeys(w, locationsSection + ".Factions");
			
			// Mob Arena
			for(String entry : keys) {
				List<String> keysSplitted = Arrays.asList(entry.split(","));
				
				boolean useEntry = false;
				
				for(String entrySplitted : keysSplitted) {
					
					if(entrySplitted.trim().equals("*")) {
						// There has to be a faction at the current location
						if(SafeCreeper.instance.getFactionsManager().isFactionAt(loc))
							useEntry = true;
					
					} else {
						
						// The location has to be in an faction
						if(!SafeCreeper.instance.getFactionsManager().isFactionAt(loc))
							continue;
						
						String fname = SafeCreeper.instance.getFactionsManager().getFactionAt(loc);
						
						if(fname.equals(entrySplitted.trim()))
							useEntry = true;
					}
				}

				// Add the section to the output list if it should be used and if it does exist
				if(useEntry)
					if(configNodeExists(w, locationsSection + ".Factions." + entry + "." + keyName))
						out.add(locationsSection + ".Factions." + entry + "." + keyName);
			}
		}
		
		// Return the generated list to output
		return out;
    }
    
	/**
	 * Get an option from a config file
	 * @param w the world
	 * @param controlName the control name
	 * @param keyName the key name
	 * @param def default value
	 * @param checkIfEnabled should be checked if the control is enabled
	 * @param loc the current location
	 * @return the value
	 */
	public boolean getOptionBoolean(World w, String controlName, String keyName, boolean def, boolean checkIfEnabled, Location loc) {
		// If should be checked if the control is enabled, do so
		if(checkIfEnabled)
			if(!isControlEnabled(w.getName(), controlName, false, loc))
				return def;
		
		// Create a variable to save the last value in
		boolean val = def;
		
		// Get the default value
		val = configGetBoolean(w.getName(), controlName + "." + keyName, def);
		
		// Handle the locations node
		String locationsSection = controlName + ".Locations";
		for(String section : handleLocations(w.getName(), loc, locationsSection, keyName))
			val = configGetBoolean(w.getName(), section, val);
		
		return val;
	}
    
	/**
	 * Get an option from a config file
	 * @param w the world
	 * @param controlName the control name
	 * @param keyName the key name
	 * @param def default value
	 * @param checkIfEnabled should be checked if the control is enabled
	 * @param loc the current location
	 * @return the value
	 */
	public int getOptionInt(World w, String controlName, String keyName, int def, boolean checkIfEnabled, Location loc) {
		// If should be checked if the control is enabled, do so
		if(checkIfEnabled)
			if(!isControlEnabled(w.getName(), controlName, false, loc))
				return def;
		
		// Create a variable to save the last value in
		int val = def;
		
		// Get the default value
		val = configGetInt(w.getName(), controlName + "." + keyName, def);
		
		// Handle the locations node
		String locationsSection = controlName + ".Locations";
		for(String section : handleLocations(w.getName(), loc, locationsSection, keyName))
			val = configGetInt(w.getName(), section, val);
		
		return val;
	}
    
	/**
	 * Get an option from a config file
	 * @param w the world
	 * @param controlName the control name
	 * @param keyName the key name
	 * @param def default value
	 * @param checkIfEnabled should be checked if the control is enabled
	 * @param loc the current location
	 * @return the value
	 */
	public String getOptionString(World w, String controlName, String keyName, String def, boolean checkIfEnabled, Location loc) {
		// If should be checked if the control is enabled, do so
		if(checkIfEnabled)
			if(!isControlEnabled(w.getName(), controlName, false, loc))
				return def;
		
		// Create a variable to save the last value in
		String val = def;
		
		// Get the default value
		val = configGetString(w.getName(), controlName + "." + keyName, def);
		
		// Handle the locations node
		String locationsSection = controlName + ".Locations";
		for(String section : handleLocations(w.getName(), loc, locationsSection, keyName))
			val = configGetString(w.getName(), section, val);
		
		return val;
	}
    
	/**
	 * Get an option from a config file
	 * @param w the world
	 * @param controlName the control name
	 * @param keyName the key name
	 * @param def default value
	 * @param checkIfEnabled should be checked if the control is enabled
	 * @param loc the current location
	 * @return the value
	 */
	public double getOptionDouble(World w, String controlName, String keyName, double def, boolean checkIfEnabled, Location loc) {
		// If should be checked if the control is enabled, do so
		if(checkIfEnabled)
			if(!isControlEnabled(w.getName(), controlName, false, loc))
				return def;
		
		// Create a variable to save the last value in
		double val = def;
		
		// Get the default value
		val = configGetDouble(w.getName(), controlName + "." + keyName, def);
		
		// Handle the locations node
		String locationsSection = controlName + ".Locations";
		for(String section : handleLocations(w.getName(), loc, locationsSection, keyName))
			val = configGetDouble(w.getName(), section, val);
		
		return val;
	}
    
	/**
	 * Get an option from a config file
	 * @param w the world
	 * @param controlName the control name
	 * @param keyName the key name
	 * @param def default value
	 * @param checkIfEnabled should be checked if the control is enabled
	 * @param loc the current location
	 * @return the value
	 */
	public List<String> getOptionKeysList(World w, String controlName, String keyName, List<String> def, boolean checkIfEnabled, Location loc) {
		// If should be checked if the control is enabled, do so
		if(checkIfEnabled)
			if(!isControlEnabled(w.getName(), controlName, false, loc))
				return def;
		
		// Create a variable to save the last value in
		List<String> val = def;
		
		// Get the default value
		val = configGetKeys(w.getName(), controlName + "." + keyName, def);
		
		// Handle the locations node
		String locationsSection = controlName + ".Locations";
		for(String section : handleLocations(w.getName(), loc, locationsSection, keyName))
			val = configGetKeys(w.getName(), section, val);
		
		return val;
	}
    
	/**
	 * Check if a control is enabled
	 * @param w the world name
	 * @param controlName the control name
	 * @param def the default value
	 * @param loc the current location
	 * @return true if the control is enabled
	 */
    public boolean isControlEnabled(String w, String controlName, boolean def, Location loc) {
    	boolean enabled = def;
		
    	// Get the default value
    	enabled = configGetBoolean(w, controlName + ".Enabled", def);
    	
		// Check if levels are enabled, if so check if the control is enabled
		if(configNodeExists(w, controlName + ".Locations.Levels")) {
			List<String> keys = configGetKeys(w, controlName + ".Locations.Levels");
			
			for(String entry : keys) {
				List<String> keysSplitted = Arrays.asList(entry.split(","));
				
				boolean useCur = false;
				
				for(String entrySplitted : keysSplitted) {
					
					if(entrySplitted.trim().equals("*"))
						useCur = true;
					
					else if(!entrySplitted.trim().contains("-")) {
						
						// If the value isn't an integer, if itisn't show an error and continue in the for loop
						if(!SCUtils.isInt(entrySplitted)) {
							System.out.println("[SafeCreeper] [Error] Value is not an integer: " + entrySplitted);
							continue;
						}
						
						int level = Integer.parseInt(entrySplitted);
						if(loc.getY() == level)
							useCur = true;
						
					} else {
						
						List<String> values = Arrays.asList(entrySplitted.split("-"));
						
						// If the value isn't an integer, if itisn't show an error and continue in the for loop
						if(!SCUtils.isInt(values.get(0)) || !SCUtils.isInt(values.get(1))) {
							System.out.println("[SafeCreeper] [Error] Value is not an integer: " + entrySplitted);
							continue;
						}
						
						int minLevel = Math.min(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)));
						int maxLevel = Math.max(Integer.parseInt(values.get(0)), Integer.parseInt(values.get(1)));
						if(loc.getY() >= minLevel && loc.getY() <= maxLevel)
							useCur = true;
					}
				}
				
				// If the current key should be used, get the value Make sure the default value is the current value
				// so the value won't be reset if the node wasn't found
				if(useCur)
					enabled = configGetBoolean(w, controlName + ".Locations.Levels." + entry + ".Enabled", enabled);
			}
		}
		
		// Check if MobArena is enabled, if so check if the control is enabled
		if(configNodeExists(w, controlName + ".Locations.MobArena")) {
			List<String> keysMobArena = configGetKeys(w, controlName + ".Locations.MobArena");
			
			// Mob Arena
			for(String entry : keysMobArena) {
				List<String> keysSplitted = Arrays.asList(entry.split(","));
				
				boolean useCur = false;
				
				for(String entrySplitted : keysSplitted) {
					
					if(entrySplitted.trim().equals("*")) {
						
						// The mob arena handler may not be null
						if(SafeCreeper.instance.getMobArenaManager() == null)
							continue;
						
						// The location has to be in an arena
						if(!SafeCreeper.instance.getMobArenaManager().isInArena(loc))
							continue;
						
						useCur = true;
					
					} else {
						
						// The mob arena handler may not be null
						if(SafeCreeper.instance.getMobArenaManager() == null)
							continue;
						
						// The location has to be in an arena
						if(!SafeCreeper.instance.getMobArenaManager().isInArena(loc))
							continue;
						
						Arena a = SafeCreeper.instance.getMobArenaManager().getArenaAt(loc);
						
						if(a.configName().equals(entrySplitted.trim()))
							useCur = true;
					}
				}
				
				// If the current key should be used, get the value. Make sure the default value is the current value
				// so the value won't be reset if the node wasn't found
				if(useCur)
					enabled = configGetBoolean(w, controlName + ".Locations.MobArena." + entry + ".Enabled", enabled);
			}
		}
		
		// Check if PVPArena is enabled, if so check if the control is enabled
		if(configNodeExists(w, controlName + ".Locations.PVPArena")) {
			List<String> keys = configGetKeys(w, controlName + ".Locations.PVPArena");
			
			// Mob Arena
			for(String entry : keys) {
				List<String> keysSplitted = Arrays.asList(entry.split(","));
				
				boolean useCur = false;
				
				for(String entrySplitted : keysSplitted) {
					
					if(entrySplitted.trim().equals("*")) {
						if(SafeCreeper.instance.getPVPArenaManager().isPVPArenaAt(loc))
							useCur = true;
					
					} else {
						
						// The location has to be in an arena
						if(!SafeCreeper.instance.getPVPArenaManager().isPVPArenaAt(loc))
							continue;
						
						String arenaName = SafeCreeper.instance.getPVPArenaManager().getPVPArenaAt(loc);
						
						if(arenaName.equals(entrySplitted.trim()))
							useCur = true;
					}
				}
				
				// If the current key should be used, get the value. Make sure the default value is the current value
				// so the value won't be reset if the node wasn't found
				if(useCur)
					enabled = configGetBoolean(w, controlName + ".Locations.PVPArena." + entry + ".Enabled", enabled);
			}
		}
		
		// Check if Factions are enabled, if so check if the control is enabled
		if(configNodeExists(w, controlName + ".Locations.Factions")) {
			List<String> keys = configGetKeys(w, controlName + ".Locations.Factions");
			
			// Mob Arena
			for(String entry : keys) {
				List<String> keysSplitted = Arrays.asList(entry.split(","));
				
				boolean useCur = false;
				
				for(String entrySplitted : keysSplitted) {
					
					if(entrySplitted.trim().equals("*")) {
						// There has to be a faction at the current location
						if(SafeCreeper.instance.getFactionsManager().isFactionAt(loc))
							useCur = true;
					
					} else {
						
						// The location has to be in an faction
						if(!SafeCreeper.instance.getFactionsManager().isFactionAt(loc))
							continue;
						
						String fname = SafeCreeper.instance.getFactionsManager().getFactionAt(loc);
						
						if(fname.equals(entrySplitted.trim()))
							useCur = true;
					}
				}
				
				// If the current key should be used, get the value. Make sure the default value is the current value
				// so the value won't be reset if the node wasn't found
				if(useCur)
					enabled = configGetBoolean(w, controlName + ".Locations.Factions." + entry + ".Enabled", enabled);
			}
		}
		
		return enabled;
    }
    
    public void playControlEffects(String controlName, String effectReason, Location loc) {
		
		Random rand = new Random();
    	
    	// Check if the effects are enabled
    	if(!getOptionBoolean(loc.getWorld(), controlName, "Effects.Enabled", false, true, loc)) {
    		// Effects are disabled!
    		return;
    	}
    	
    	// Check if the current effect is enabled
    	if(!getOptionBoolean(loc.getWorld(), controlName, "Effects.Triggers." + effectReason + ".Enabled", false, true, loc)) {
    		// Effect is disabled!
    		return;
    	}
    	
    	List<String> effects = configGetKeys(loc.getWorld().getName(), controlName + ".Effects.Triggers." + effectReason + ".Effects");
    	
    	for(String effect : effects) {
    		String effectType = getOptionString(loc.getWorld(), controlName, "Effects.Triggers." + effectReason + ".Effects." + effect + ".Type", "", true, loc).trim();
    		double effectRadius = getOptionDouble(loc.getWorld(), controlName, "Effects.Triggers." + effectReason + ".Effects." + effect + ".Radius", 0, true, loc);
    		int effectIterations = getOptionInt(loc.getWorld(), controlName, "Effects.Triggers." + effectReason + ".Effects." + effect + ".Iterations", 1, true, loc);
    		
    		// The effect type has to be set
    		if(effectType.trim().equals("")) {
				System.out.println("[SafeCreeper] [ERROR] Unknown effect: " + effectType);
    			continue;
    		}
    		
    		// Make sure the rotation is not smaller than zero
    		if(effectRadius < 0)
    			effectRadius *= -1;
    		
    		// The effect may not be null
    		if(effectIterations < 1) {
				System.out.println("[SafeCreeper] [ERROR] Invalid effect iteration: " + effectIterations);
    			continue;
    		}
    		
    		if(effectType.equalsIgnoreCase("Lightning")) {
    			for(int i = 0; i < effectIterations; i++) {
	    			Location newLoc = loc.clone();
	    			newLoc.add(rand.nextDouble()*(effectRadius*2)-effectRadius, rand.nextDouble()*(effectRadius*2)-effectRadius, rand.nextDouble()*(effectRadius*2)-effectRadius);
        			loc.getWorld().strikeLightningEffect(newLoc);
    			}
    			continue;
    			
    		} else if(effectType.equalsIgnoreCase("Effect")) {
    			String effectName = getOptionString(loc.getWorld(), controlName, "Effects.Triggers." + effectReason + ".Effects." + effect + ".Effect", "", true, loc).toUpperCase().replace(" ", "_");
    			int effectDataValue = getOptionInt(loc.getWorld(), controlName, "Effects.Triggers." + effectReason + ".Effects." + effect + ".DataValue", 1, true, loc);
	    		int effectViewingDisitance = getOptionInt(loc.getWorld(), controlName, "Effects.Triggers." + effectReason + ".Effects." + effect + ".ViewingDistance", 8, true, loc);
	    		
	    		// The effect type has to be set
	    		if(effectName.trim().equals("")) {
					System.out.println("[SafeCreeper] [ERROR] Unknown effect: " + effectName);
	    			continue;
	    		}
	    		
	    		Effect e = Effect.valueOf(effectName);
	    		
	    		// The effect may not be null
	    		if(e == null) {
					System.out.println("[SafeCreeper] [ERROR] Unknown effect: " + effectName);
	    			continue;
	    		}
	    		
	    		// The effect may not be null
	    		if(effectIterations < 0) {
					System.out.println("[SafeCreeper] [ERROR] Invalid effect iteration: " + effectIterations);
	    			continue;
	    		}
	    		
	    		for(int i = 0; i < effectIterations; i++) {
	    			Location newLoc = loc.clone();
	    			newLoc.add(rand.nextDouble()*(effectRadius*2)-effectRadius, rand.nextDouble()*(effectRadius*2)-effectRadius, rand.nextDouble()*(effectRadius*2)-effectRadius);
	    			loc.getWorld().playEffect(newLoc, e, effectDataValue, effectViewingDisitance);
	    		}
	    		
    		} else if(effectType.equalsIgnoreCase("Sound")) {
    			String soundName = getOptionString(loc.getWorld(), controlName, "Effects.Triggers." + effectReason + ".Effects." + effect + ".Sound", "", true, loc).toUpperCase().replace(" ", "_");
    			int soundPitch = getOptionInt(loc.getWorld(), controlName, "Effects.Triggers." + effectReason + ".Effects." + effect + ".Pitch", 1, true, loc);
	    		float soundVolume = (float) getOptionDouble(loc.getWorld(), controlName, "Effects.Triggers." + effectReason + ".Effects." + effect + ".Volume", 1, true, loc);
	    		
	    		// The effect type has to be set
	    		if(soundName.trim().equals("")) {
					System.out.println("[SafeCreeper] [ERROR] Unknown sound: " + soundName);
	    			continue;
	    		}
	    		
	    		Sound e = Sound.valueOf(soundName);
	    		
	    		// The effect may not be null
	    		if(e == null) {
					System.out.println("[SafeCreeper] [ERROR] Unknown sound: " + soundName);
	    			continue;
	    		}
	    		
	    		// The effect may not be null
	    		if(effectIterations < 0) {
					System.out.println("[SafeCreeper] [ERROR] Invalid sound iteration: " + effectIterations);
	    			continue;
	    		}
	    		
	    		for(int i = 0; i < effectIterations; i++) {
	    			Location newLoc = loc.clone();
	    			newLoc.add(rand.nextDouble()*(effectRadius*2)-effectRadius, rand.nextDouble()*(effectRadius*2)-effectRadius, rand.nextDouble()*(effectRadius*2)-effectRadius);
	    			loc.getWorld().playSound(newLoc, e, soundVolume, soundPitch);
	    		}
    		} else {
    			System.out.println("[SafeCreeper] [ERROR] Unknown effect type: " + effectType);
    			continue;
    		}
    	}
    	
    	// Effect to redable effect
    	//System.out.println(WordUtils.capitalize(Effect.POTION_BREAK.toString().trim().replace("_", " ").toLowerCase()).replace(" ", ""));
    	
    	//List<String> effects = getGlobalConfig().getConfigurationSection("CreeperControl.Effects.Spawning.Effects").getKeys(false)
    }
    
    /**
     * Get a string from a config
     * @param w the current world
     * @param node the node
     * @param def the default value used if the node doesn't exist
     * @return the value
     */
    private boolean configGetBoolean(String w, String node, boolean def) {
		if(worldConfigExist(w))
			return getWorldConfig(w).getBoolean(node,
					getGlobalConfig().getBoolean(node, def)
					);
		return getGlobalConfig().getBoolean(node, def);
	}

    /**
     * Get a string from a config
     * @param w the current world
     * @param node the node
     * @param def the default value used if the node doesn't exist
     * @return the value
     */
    private int configGetInt(String w, String node, int def) {
		if(worldConfigExist(w))
			return getWorldConfig(w).getInt(node, getGlobalConfig().getInt(node, def));
		return getGlobalConfig().getInt(node, def);
	}

    /**
     * Get a string from a config
     * @param w the current world
     * @param node the node
     * @param def the default value used if the node doesn't exist
     * @return the value
     */
    private double configGetDouble(String w, String node, double def) {
		if(worldConfigExist(w))
			return getWorldConfig(w).getDouble(node, getGlobalConfig().getDouble(node, def));
		return getGlobalConfig().getDouble(node, def);
	}
	
    /**
     * Get a string from a config
     * @param w the current world
     * @param node the node
     * @param def the default value used if the node doesn't exist
     * @return the value
     */
    private String configGetString(String w, String node, String def) {
		FileConfiguration globalConfig = getGlobalConfig();
		
		if(worldConfigExist(w)) {
			FileConfiguration worldConfig =  getWorldConfig(w);
			
			String globalSetting = globalConfig.getString(node, def);
			return worldConfig.getString(node , globalSetting);
		} else {
			String globalSetting = globalConfig.getString(node, def);
			return globalSetting;
		}
	}
	
    /**
     * Check if a node exists in the config files
     * @param w the current world
     * @param node the node to check
     * @return true if the node exists in the global or world configs
     */
    private boolean configNodeExists(String w, String node) {
		if(worldConfigExist(w)) {
			boolean b =  getWorldConfig(w).isSet(node);
			if(!b)
				b = getGlobalConfig().isSet(node);
			return b;
		}
		return getGlobalConfig().isSet(node);
	}
	
    /**
     * Get all keys from a node
     * @param w the current world
     * @param node the node to get the list from
     * @return the value
     */
    private List<String> configGetKeys(String w, String node) {
		return configGetKeys(w, node, new ArrayList<String>());
	}
	
    /**
     * Get all keys from a node
     * @param w the current world
     * @param node the node to get the list from
     * @return the value
     */
    @SuppressWarnings("unused")
	private List<String> configGetKeys(String w, String node, List<String> def) {
    	boolean globalConfig = false;
    	boolean worldConfig = false;
    	
    	// Check if this list exists in the global config file
		if(getGlobalConfig().contains(node))
			globalConfig = getGlobalConfig().isConfigurationSection(node);
    	
		// Check if the list exists in a world config file
    	if(worldConfigExist(w))
    		if(getWorldConfig(w).contains(node))
    			worldConfig = getWorldConfig(w).isConfigurationSection(node);
    	
    	// The key list must exist in any world or the global file, if not return the default value
    	if(!globalConfig && !worldConfig)
    		return def;
    	
    	if(worldConfig) {
			// Get the keys list (the safe way)
			List<String> keys = new ArrayList<String>();
			try {
				keys = new ArrayList<String>(getWorldConfig(w).getConfigurationSection(node).getKeys(false));
			} catch(NullPointerException ex) {
				return def;
			}

			List<String> keysGlobal = new ArrayList<String>();
			if(globalConfig) {
				// Get the keys list (the safe way)
				try {
					keysGlobal = new ArrayList<String>(getGlobalConfig().getConfigurationSection(node).getKeys(false));
				} catch(NullPointerException ex) {
					return def;
				}
			}
			
			// Return the values
			if(keys == null) {
				if(keysGlobal == null)
					return def;
				else
					return keysGlobal;
			} else
				return keys;
		} else {
			// Get the key list (the safe way)
			List<String> keys = new ArrayList<String>();
			try {
				keys = new ArrayList<String>(getGlobalConfig().getConfigurationSection(node).getKeys(false));
			} catch(NullPointerException ex) {
				return def;
			}
			return keys;
		}
	}
	
    public String getControlName(Entity e) {
    	return getControlName(e, "OtherControl");
    }
    
    public String getControlName(Entity e, String def) {
	    	
    	
    	// Try to find out the control name according to the entity type
    	try {
    		// Make sure the entity param is not null
	    	if(e == null)
	    		return def;
	    	
	    	// Make sure the entity type is not null
	    	if(e.getType() == null)
	    		return def;
	    	
	    	// Make sure the default value is not null
	    	if(def == null)
	    		def = "OtherControl";
	    	
	    	// Return the correct control name
    		switch(e.getType()) {
        	case FIREBALL:
        	case SMALL_FIREBALL:
        		return "FireballControl";
        		
        	case IRON_GOLEM:
        		return "IronGolemControl";
        		
        	case MAGMA_CUBE:
        		return "MagmaCubeControl";
        		
        	case MINECART_TNT:
        		return "TNTMinecartControl";
        		
        	case OCELOT:
        		return "OcelotControl";
        		
        	case PLAYER:
        		return "PlayerControl";
        		
        	case PRIMED_TNT:
        		return "TNTControl";
        		
        	case SNOWMAN:
        		return "SnowmanControl";
        		
        	case SKELETON:
        		Skeleton skel = (Skeleton) e;
        		if(skel.getSkeletonType().equals(SkeletonType.WITHER))
        			return "WitherSkeletonControl";
        		else
        			return "SkeletonControl";
        		
        	case WITHER:
        		return "WitherControl";
        		
        	case WITHER_SKULL:
        		return "WitherSkullControl";
        	
        	case ENDER_CRYSTAL:
        		return "EnderCrystalControl";
        		
        	default:
        		// Make sure the name of the entity doesn't return null, if it does, return the default value
        	  	if (e.getType().getName() == null)
        	  		return def;
        	  	
    	    	//return WordUtils.capitalize(e.getType().toString().trim().replace("_", " ").toLowerCase()).replace(" ", "") + "Control";
    	    	return e.getType().getName().trim().replace(" ", "") + "Control";
        	}
    		
    	} catch(Exception ex) {
    		// Error while retrieving control name, return the default value
    		return def;
    	}
    }
	
    public boolean isValidControl(String controlName) {
    	return getGlobalConfig().contains(controlName);
    }
	
	/**
	 * Get the file extention of a file name
	 * @param fname the file name
	 * @return the file extention
	 */
    public static String getFileExtention(String fname) {
        // Remove the path up to the filename.
        int lastSeparatorIndex = fname.lastIndexOf(".");
        if (lastSeparatorIndex == -1)
        	return fname;
        return fname.substring(lastSeparatorIndex + 1);
    }
    
    /**
     * Get the name of a file from it's file path
     * @param path the file path
     * @return the file name
     */
    public static String getFileName(String path) {
        String separator = System.getProperty("file.separator");
        String fname = "";

        // Remove the path up to the filename.
        int lastSepIndex = path.lastIndexOf(separator);
        if(lastSepIndex == -1)
            fname = path;
        else
            fname = path.substring(lastSepIndex + 1);

        // Remove the extension.
        int extIndex = fname.lastIndexOf(".");
        if (extIndex == -1)
            return fname;

        return fname.substring(0, extIndex);
    }
}

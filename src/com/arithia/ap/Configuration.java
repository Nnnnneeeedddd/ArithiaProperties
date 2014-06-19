package com.arithia.ap;

import org.bukkit.configuration.file.FileConfiguration;

public class Configuration {
	public static void addDefaults(FileConfiguration f){
		f.options().header("This is the default configuration file for ArithiaProperties");
		
		f.addDefault("messages.cannotPlaceBlock", "&c[ArithiaProperties] You cannot place blocks in this property");
		f.addDefault("messages.cannotBreakBlock", "&c[ArithiaProperties] You cannot break blocks in this property");
		f.addDefault("priority", 10);
		
		f.options().copyDefaults(true);
		f.options().copyHeader(true);
	}
}

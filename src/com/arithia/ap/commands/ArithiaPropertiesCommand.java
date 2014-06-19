package com.arithia.ap.commands;

//import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.ChatColor;
//import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.arithia.ap.ArithiaProperties;
import com.arithia.ap.MyEventHandler;
//import com.arithia.ap.datasaving.Property;
//import com.arithia.ap.datasaving.Property.Region;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

public class ArithiaPropertiesCommand implements CommandExecutor {
	private ArithiaProperties plugin;
	
	public ArithiaPropertiesCommand(ArithiaProperties ap){
		plugin = ap;
	}
	
	public boolean onCommand(CommandSender s, Command c, String l, String[] args) {//ap <command> <commandArgs>
																				   //0     1           2
		if(args.length == 0){
			return false;
		}
		
		if(args[0].equalsIgnoreCase("help")){
			listCommands(s);
		}
		
		/*
		 *creates Arithia property 
		 */
		else if(args[0].equalsIgnoreCase("createprop")){
			if(!(s instanceof Player)){
				s.sendMessage(ChatColor.RED+"Must be a player to perform this command");
				return true;
			}
			
			if(!(s.hasPermission("ap.admin"))){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You do not have permission to perform this command");
				return true;
			}
			Player player = (Player) s;
			Selection se = plugin.wep.getSelection(player);
			
			if(args.length != 2){
				player.sendMessage(ChatColor.RED+"[ArithiaProperties] You must specify a name: /ap createprop <name>");
				return true;
			}
			
			if(se == null){
				player.sendMessage(ChatColor.RED+"[ArithiaProperties] Please select a world edit area");
				return true;
			}
			int id = 1;
			if(plugin.p.getConfigurationSection("properties") != null){
				ArrayList<String> props = new ArrayList<>();
				props.addAll(plugin.p.getConfigurationSection("properties").getKeys(false));
				try{
					id = (Integer.parseInt(props.get(props.size()-1)))+1;
				}catch(Exception e) {
					ArithiaProperties.log.log(Level.SEVERE, "Property ID: "+props.get(props.size()-1)+" WAS NOT INTEGER (called in command)");
					ArithiaProperties.log.log(Level.SEVERE, "Plugin disabling.....");
					plugin.getServer().getPluginManager().disablePlugin(plugin);
				}
			}
			
			plugin.p.set("properties."+id+".name", args[1]);
			plugin.p.set("properties."+id+".owner", player.getUniqueId().toString());
			plugin.p.set("properties."+id+".regions.mainRegion.BVMinimum.x", se.getNativeMinimumPoint().getBlockX());
			plugin.p.set("properties."+id+".regions.mainRegion.BVMinimum.y", se.getNativeMinimumPoint().getBlockY());
			plugin.p.set("properties."+id+".regions.mainRegion.BVMinimum.z", se.getNativeMinimumPoint().getBlockZ());
			
			plugin.p.set("properties."+id+".regions.mainRegion.BVMaximum.x", se.getNativeMaximumPoint().getBlockX());
			plugin.p.set("properties."+id+".regions.mainRegion.BVMaximum.y", se.getNativeMaximumPoint().getBlockY());
			plugin.p.set("properties."+id+".regions.mainRegion.BVMaximum.z", se.getNativeMaximumPoint().getBlockZ());
			
			plugin.savePropertiesYaml();
			
			player.sendMessage(ChatColor.GOLD+"Successfully saved "+se.getArea()+" blocks as a property with ID: "+id);
		}
		
		/*
		 * adds to property area
		 */
		else if(args[0].equalsIgnoreCase("addarea")){
			if(args.length != 2){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] Usage: /ap addArea <propertyID>");
				return true;
			}
			
			if(!s.hasPermission("ap.admin")){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You do not have permission to perform this command");
				return true;
			}
			
			int id = 0;
			try{
				id = Integer.parseInt(args[1]);
			}catch(Exception e){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] Property ID: "+args[1]+" is not an integer");
				return true;
			}
			
			if(plugin.p.getConfigurationSection("properties") == null){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] There are no properties");
				return true;
			}
			
			Set<String> props = plugin.p.getConfigurationSection("properties").getKeys(true);
			
			if(!props.contains(String.valueOf(id))){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] There is no property of id: "+id);
				return true;
			}
			
			if(!(s instanceof Player)){
				s.sendMessage(ChatColor.RED+"You must be a player to perform this command");
				return true;
			}
			Player player = (Player) s;
			
			Selection se = plugin.wep.getSelection(player);
			
			if(se == null){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] Please select a worldedit selection");
				return true;
			}
			
			ArrayList<String> regions = new ArrayList<>();
			regions.addAll(plugin.p.getConfigurationSection("properties."+id+".regions").getKeys(false));
			int regID = 1;
			if(!(regions.size() <= 1)){
				try{
					regID = (Integer.parseInt(regions.get(regions.size()-1)))+1;
				}catch(Exception e){
					ArithiaProperties.log.log(Level.SEVERE, "[ArithiaProperties] region: "+
				regions.get(regions.size()-1)+", in property: "+id+", is NOT INTEGER");
					ArithiaProperties.log.log(Level.SEVERE, "[ArithiaProperties] disabling...");
					plugin.getServer().getPluginManager().disablePlugin(plugin);
				}
			}
			
			plugin.p.set("properties."+id+".regions."+regID+".BVMinimum.x", se.getNativeMinimumPoint().getBlockX());
			plugin.p.set("properties."+id+".regions."+regID+".BVMinimum.y", se.getNativeMinimumPoint().getBlockY());
			plugin.p.set("properties."+id+".regions."+regID+".BVMinimum.z", se.getNativeMinimumPoint().getBlockZ());
			
			plugin.p.set("properties."+id+".regions."+regID+".BVMaximum.x", se.getNativeMaximumPoint().getBlockX());
			plugin.p.set("properties."+id+".regions."+regID+".BVMaximum.y", se.getNativeMaximumPoint().getBlockY());
			plugin.p.set("properties."+id+".regions."+regID+".BVMaximum.z", se.getNativeMaximumPoint().getBlockZ());
			plugin.savePropertiesYaml();
			
			player.sendMessage(ChatColor.GREEN+"[ArithiaProperties] successfully added region: "+regID+" to property: "+id);
		}
		
		else if(args[0].equalsIgnoreCase("listprops")){
			Set<String> properties;
			if(plugin.p.getConfigurationSection("properties") == null){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] there are no properties");
				return true;
			}
			
			properties = plugin.p.getConfigurationSection("properties").getKeys(false);
			
			s.sendMessage(ChatColor.GREEN+"---------------ArithiaProperties---------------");
			s.sendMessage(" ");
			for(String property: properties){
				if(s.hasPermission("ap.admin")){
					s.sendMessage(ChatColor.GOLD+"ID: "+property+", NAME: "+plugin.p.getString("properties."+property+".name"));
				}else{
					s.sendMessage(ChatColor.GOLD+plugin.p.getString("properties."+property+".name"));
				}
			}
			s.sendMessage(" ");
			s.sendMessage(ChatColor.GREEN+"-----------------------------------------------");
		}
		
		else if(args[0].equalsIgnoreCase("delprop")){
			if(!s.hasPermission("ap.admin")){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You do not have permission to perform this command");
				return true;
			}
			
			Set<String> properties;
			if(plugin.p.getConfigurationSection("properties") == null){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] there are no properties");
				return true;
			}
			
			properties = plugin.p.getConfigurationSection("properties").getKeys(false);
			
			if(args.length != 2){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] Usage /ap delprop <propertyID>");
				return true;
			}
			
			if(properties.contains(args[1])){
				plugin.p.set("properties."+args[1], null);
				plugin.savePropertiesYaml();
				s.sendMessage(ChatColor.GREEN+"[ArithiaProperties] Successfully deleted property with ID: "+args[1]);
			}else{
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] There is no property of ID: "+args[1]);
			}
		}
		
		else if(args[0].equalsIgnoreCase("listregions")){
			if(args.length != 2){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] Please Specify Property ID Usage: /ap listRegions <propertyID>");
				return true;
			}
			
			if(!s.hasPermission("ap.admin")){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You do not have permission to perform this command");
				return true;
			}
			
			if(plugin.p.getConfigurationSection("properties."+args[1]+".regions") == null){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] property "+args[1]+" could not be found or did not have any regions");
				return true;
			}
			
			Set<String> regions = plugin.p.getConfigurationSection("properties."+args[1]+".regions").getKeys(false);
			s.sendMessage(ChatColor.RED+"--------RegionsForProperty: "+args[1]+"--------");
			s.sendMessage("");
			
			for(String region: regions){
				s.sendMessage(ChatColor.GOLD+"RegionID: "+region);
			}
			
			s.sendMessage("");
			s.sendMessage(ChatColor.GREEN+"------------------------------");
		}
		
		else if(args[0].equalsIgnoreCase("delregion")){
			if(args.length != 3){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] Please Specify Property ID Usage: /ap delregion <propertyID> <regionID>");
				return true;
			}
			
			if(!s.hasPermission("ap.admin")){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You do not have permission to perform this command");
				return true;
			}
			
			if(plugin.p.getConfigurationSection("properties."+args[1]+".regions") == null){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] property "+args[1]+" could not be found or did not have any regions");
				return true;
			}
			
			Set<String> properties = plugin.p.getConfigurationSection("properties").getKeys(false);
			Set<String> regions = plugin.p.getConfigurationSection("properties."+args[1]+".regions").getKeys(false);
			
			if(properties.contains(args[1])){
				if(regions.contains(args[2])){
					plugin.p.set("properties."+args[1]+".regions."+args[2], null);
					plugin.savePropertiesYaml();
					s.sendMessage(ChatColor.GREEN+"[ArithiaProperties] successfully deleted region "+args[2]+" from property "+args[1]);
				}else{
					s.sendMessage(ChatColor.RED+"[ArithiaProperties] There is no region of ID: "+args[2]+" in the property of ID: "+args[1]);
				}
			}else{
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] There is no property of ID: "+args[1]);
			}
		}
		
		else if(args[0].equalsIgnoreCase("setInfo")){//ap setInfo <propID> info goes here
			if(!s.hasPermission("ap.admin")){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You do not have permission to perform this command");
				return true;
			}
			
			if(!(s instanceof Player)){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You must be a player to perform this command");
				return true;
			}
			
			Player player = (Player) s;
			
			String info = "";
			for(int i = 0; i<args.length; i++){
				if(args[i] != args[0]){
					info += args[i]+" ";
				}
			}
			
			ArrayList<ProtectedCuboidRegion> regions =  MyEventHandler.getRegions();
			BlockVector bv = new BlockVector(player.getLocation().getBlockX(),
											 player.getLocation().getBlockY(),
											 player.getLocation().getBlockZ());
			if(regions != null){
				for(ProtectedCuboidRegion region: regions){
					if(region.contains(bv)){
						plugin.p.set("properties."+region.getId()+".info", info);
						plugin.savePropertiesYaml();
						s.sendMessage(ChatColor.GREEN+"[ArithiaProperties] successfully set info");
						return true;
					}
				}
			}
			
			player.sendMessage(ChatColor.RED+"[ArithiaProperties] You are not standing in a property");
		}
		
		else if(args[0].equalsIgnoreCase("saveSchematic")){
			s.sendMessage(ChatColor.RED+"[ArithiaProperties] /ap saveschematic not available in beta");
			return true;
			
			/*
			
			if(!s.hasPermission("ap.admin")){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You do not have permission to perform this command");
				return true;
			}
			
			if(!(s instanceof Player)){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You must be Player to perform this command");
				return true;
			}
			
			Player player = (Player) s;
			
			ArrayList<ProtectedCuboidRegion> regions =  MyEventHandler.getRegions();
			BlockVector bv = new BlockVector(player.getLocation().getBlockX(),
											 player.getLocation().getBlockY(),
											 player.getLocation().getBlockZ());
			
			for(ProtectedCuboidRegion region: regions){
				if(region.contains(bv)){
					
					Property p = new Property();
					try{
						p.set(Integer.parseInt(region.getId()), player.getWorld());
					}catch(NumberFormatException e){
						ArithiaProperties.log.log(Level.SEVERE, "[ArithiaProperties] Property of ID: "+region.getId()+" WAS NOT INTEGER");
						ArithiaProperties.log.log(Level.SEVERE, "[ArithiaProperties] plugin will be disabled");
						plugin.getServer().getPluginManager().disablePlugin(plugin);
						return true;
					}
					
					plugin.saveObject(
							new File(plugin.getDataFolder()+"\\schematics\\"+region.getId()+".sch"), p);
					
					
					s.sendMessage(ChatColor.GREEN+"[ArithiaProperties] Successfully saved property schematic (.sch)");
					
					return true;
				}
			}
			
			player.sendMessage(ChatColor.RED+"[ArithiaProperties] You are not standing in a property");
			
			*/
		}
		
		else if(args[0].equalsIgnoreCase("reset")){
			s.sendMessage(ChatColor.RED+"[ArithiaProperties] /ap reset not available in beta");
			return true;
			
			/*
			
			if(args.length != 2){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You must specify a property ID Usage: /ap reset <propID>");
				return true;
			}
			
			if(!s.hasPermission("ap.admin")){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] You do not have permission to perform this command");
				return true;
			}
			
			Set<String> properties;
			if(plugin.p.getConfigurationSection("properties") == null){
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] there are no properties");
				return true;
			}
			
			properties = plugin.p.getConfigurationSection("properties").getKeys(false);
			
			if(properties.contains(args[1])){
				
				
				File f = new File("\\ArithiaProperties\\schematics\\"+args[1]+".sch");
					
				if(!f.exists()){
					s.sendMessage(ChatColor.RED+"[ArithiaProperties] property schematic (.sch) could not be found");
					return true;
				}
				
				Property prop = (Property) plugin.loadObject(f);
				
				for(Region r: prop.getRegions()){
					BlockVector min = r.getMin();
					BlockVector max = r.getMax();
					
					for(int x = min.getBlockX(); x<max.getBlockX(); x++){
						for(int y = min.getBlockY(); y<max.getBlockY(); y++){
							for(int z = min.getBlockZ(); z<max.getBlockZ(); z++){
								Location loc = new Location(prop.getWorld(), x, y, z);
								loc.getBlock().setType(r.getBlockType(x, y, z));
							}
						}
					}
				}
				
				s.sendMessage(ChatColor.GREEN+"[ArithiaProperties] Successfully reset property to schematic");
				
			}else{
				s.sendMessage(ChatColor.RED+"[ArithiaProperties] property specified was not found");
			}
			
			*/
		}else{
			s.sendMessage(ChatColor.RED+"[ArithiaProperties] Unknown /ap command, Use /ap help for help");
		}
		
		return true;
	}
	
	private void listCommands(CommandSender s){
		String[][] commandsAndDescriptions = {
				
											  {"/ap help", "Displays This Message"},
											  {"/ap createprop <propName>", "Turns selected area into property"},
											  {"/ap addarea <propertyID>", "adds selected area to property"},
											  {"/ap listprops", "lists all properties"},
											  {"/ap delprop <propertyID>", "deletes property of specified ID"},
											  {"/ap listregions <propertyID>", "list regions inside specified property"},
											  {"/ap delregion <propertyID> <regionID>", "deletes a region inside a property"},
											  {"/ap setInfo [info here]", "Adds/Sets info to/of property you are standing in"},
											  {"/ap saveSchematic", "saves schemetic for property you are standing in"},
											  {"/ap reset <propID>", "resets property to a saved schematic"}
											  
											  };
		
		s.sendMessage(ChatColor.GOLD+"----------ArithiaPropertiesCommands----------");
		s.sendMessage(" ");
		
		for(int i0 = 0; i0<commandsAndDescriptions.length; i0++){
			String command = commandsAndDescriptions[i0][0];
			String des = commandsAndDescriptions[i0][1];
			
			s.sendMessage(ChatColor.GREEN+command+": "+des);
		}
		
		s.sendMessage(" ");
		s.sendMessage(ChatColor.GOLD+"---------------------------------------------");
	}
}

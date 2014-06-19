package com.arithia.ap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.arithia.ap.commands.ArithiaPropertiesCommand;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class ArithiaProperties extends JavaPlugin implements Runnable{
	public static final Logger log = Logger.getLogger("Minecraft");
	public HashMap<Player, Integer[]> playerClarifList = new HashMap<Player, Integer[]>();
	public YamlConfiguration p;
	public File pFile;
	public WorldGuardPlugin wgp;
	public WorldEditPlugin wep;
	public Economy eco;
	
	public void run(){
		
		for(Player player: playerClarifList.keySet()){
			if(playerClarifList.get(player)[0] >= 1){
				int time = playerClarifList.get(player)[0];
				int prop = playerClarifList.get(player)[1];
				int price = playerClarifList.get(player)[2];
				playerClarifList.remove(player);
				time--;
				
				Integer[] newArray = {time, prop, price};
				playerClarifList.put(player, newArray);
			}else{
				playerClarifList.remove(player);
				player.sendMessage(ChatColor.RED+"[ArithiaProperties] Buying process of property has been cancelled");
			}
		}
	}
	
	public void onEnable(){
		if(!setupEco()){
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		File schematics = new File(getDataFolder()+"\\schematics");
		schematics.mkdirs();
		
		getWorldGuardAndEdit();
		
		p = loadPropertiesFile();
		p.options().header("This is the memory .yml file for ArithiaProperties: Please do not manually edit!!");
		p.options().copyHeader(true);
		savePropertiesYaml();
		
		Configuration.addDefaults(getConfig());
		saveConfig();
		
		getServer().getPluginManager().registerEvents(new MyEventHandler(this), this);
		
		getServer().getPluginCommand("ap").setExecutor(new ArithiaPropertiesCommand(this));
		
		getServer().getScheduler().scheduleSyncRepeatingTask(this, this, 20L, 20L);
	}
	
	private boolean setupEco() {
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if(getServer().getPluginManager().getPlugin("Vault") == null){
			System.out.println("Vault Not Found");
			return false;
		}
		
		if (rsp == null) {
			System.out.println("RegisteredServiceProvider is null!!");
			return false;
		}
		eco = rsp.getProvider();
		return eco != null;
    }
	
	private void getWorldGuardAndEdit(){
		Plugin wgplugin = getServer().getPluginManager().getPlugin("WorldGuard");
		
		if(wgplugin == null || !(wgplugin instanceof WorldGuardPlugin)){
			log.log(Level.SEVERE, "[ArithiaProperties] Plugin \"WorldGuard\" could not be found");
			log.log(Level.SEVERE, "[ArithiaProperties] Disabling..");
			getServer().getPluginManager().disablePlugin(this);
		}
		
		wgp = (WorldGuardPlugin) wgplugin;
		try {
			wep = wgp.getWorldEdit();
		} catch (CommandException e) {
			log.log(Level.SEVERE, "[ArithiaProperties] Plugin \"WorldEdit\" could not be found");
			log.log(Level.SEVERE, "[ArithiaProperties] Disabling..");
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	public boolean saveObject(File f, Object o){
		try{
			f.createNewFile();
			
			FileOutputStream fos = new FileOutputStream(f);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.flush();
			oos.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public Object loadObject(File f){
		try{
			FileInputStream fis = new FileInputStream(f);
			ObjectInputStream in = new ObjectInputStream(fis);
			Object o = in.readObject();
			in.close();
			return o;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public void savePropertiesYaml(){
		try{
			p.save(pFile);
		}catch(IOException e){
			log.log(Level.SEVERE, "Error while saving properties.yml file: IOException");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	
	private YamlConfiguration loadPropertiesFile(){
		pFile = new File(getDataFolder()+File.separator+"properties.yml");
		
		if(!pFile.exists()){
			getDataFolder().mkdirs();
			try{
				pFile.createNewFile();
			}catch(IOException e){
				log.log(Level.SEVERE, "Error while creating properties.yml file: IOException");
				e.printStackTrace();
				getServer().getPluginManager().disablePlugin(this);
				return null;
			}
		}
		
		return YamlConfiguration.loadConfiguration(pFile);
	}
}

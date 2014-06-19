package com.arithia.ap;

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

public class MyEventHandler implements Listener{
	private static ArithiaProperties plugin;
	
	public MyEventHandler(ArithiaProperties ap){
		plugin = ap;
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e){
		if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
			if(e.getClickedBlock().getType() == Material.WALL_SIGN || e.getClickedBlock().getType() == Material.SIGN_POST){
				Sign sign = (Sign) e.getClickedBlock().getState();
				if(sign.getLine(0).equalsIgnoreCase(ChatColor.AQUA+"[ArithiaProp]")){
					
					if(!e.getPlayer().hasPermission("ap.player")){
						e.getPlayer().sendMessage(ChatColor.RED+"[ArithiaProperties] You do not have permission to buy properties");
						return;
					}
					
					String prop;
					if((prop = getPropFromSign(sign.getBlock())).equals("")){
						e.getPlayer().sendMessage(ChatColor.RED+"[ArithiaProperties] Sign not assigned to property (Please contact server administrators)");
						return;
					}
					
					if(getOwner(prop) == e.getPlayer()){
						e.getPlayer().sendMessage(ChatColor.GREEN+"[ArithiaProperties] You are already the owner of this property");
						return;
					}
					
					String info = plugin.p.getString("properties."+prop+".info");
					
					if(info != null){
						e.getPlayer().sendMessage(info.replace("&", "§"));
					}
					e.getPlayer().sendMessage(ChatColor.GREEN+"Are you sure you want to buy this property (Please type Y or N within the next 20 seconds)");
					
					Integer[] array = {20, Integer.parseInt(prop), Integer.parseInt(sign.getLine(2).replace(ChatColor.GREEN.toString(), "").replace(" Ducats", ""))};
					plugin.playerClarifList.put(e.getPlayer(), array);
				}
			}
		}
	}
	
	private void buyProp(Player player, int property, int price){
		if(price < 0)price = 0;
		
		plugin.eco.withdrawPlayer(player, price);
		plugin.p.set("properties."+property+".owner", player.getUniqueId().toString());
		plugin.savePropertiesYaml();
		
		player.sendMessage(ChatColor.GOLD+"[ArithiaProperties] You have successfully bought this property");
	}
	
	@EventHandler
	public void onChat(AsyncPlayerChatEvent e){
		if(plugin.playerClarifList.containsKey(e.getPlayer())){
			if(e.getMessage().equalsIgnoreCase("y")){
				buyProp(e.getPlayer(), plugin.playerClarifList.get(e.getPlayer())[1], plugin.playerClarifList.get(e.getPlayer())[2]);
			}else if(e.getMessage().equalsIgnoreCase("n")){
				plugin.playerClarifList.remove(e.getPlayer());
				e.getPlayer().sendMessage(ChatColor.GOLD+"[ArithiaProperties] Property buying CANCELLED");
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void signMade(SignChangeEvent e){
		for(ProtectedCuboidRegion pcr: getRegions()){
			if(pcr.contains(e.getBlock().getX(), e.getBlock().getY(), e.getBlock().getZ())){
				if(getOwner(pcr.getId()) == e.getPlayer()){
					if(e.getLine(0).equalsIgnoreCase("[arithiaprop]")){
						if(!e.getPlayer().hasPermission("ap.player")){
							
							return;
						}
						
						e.setLine(0, ChatColor.AQUA+"[ArithiaProp]");
						
						if(isInteger(e.getLine(2))){
							e.setLine(2, ChatColor.GREEN+e.getLine(2)+" Ducats");
							
							if(!(e.getLine(1).equals(""))){
								e.setLine(1, ChatColor.GOLD+"Buy: "+e.getLine(1));
							}else{
								
							}
							
							plugin.p.set("properties."+pcr.getId()+".sign.x", e.getBlock().getX());
							plugin.p.set("properties."+pcr.getId()+".sign.y", e.getBlock().getY());
							plugin.p.set("properties."+pcr.getId()+".sign.z", e.getBlock().getZ());
							plugin.savePropertiesYaml();
						}else{
							e.getPlayer().sendMessage(ChatColor.RED+"Line 2; the price must be a whole number");
							e.getBlock().setType(Material.AIR);
							e.getPlayer().getInventory().addItem(new ItemStack(Material.SIGN));
						}
					}
				}else{
					String message = plugin.getConfig().getString("messages.cannotPlaceBlock");
					message = message.replace("&", "§");
					message = message.replace("{{propertyid}}", pcr.getId());
					e.getPlayer().sendMessage(message);
					e.setCancelled(true);
				}
			}
		}
	}
	
	private boolean isInteger(String str){
		try{
			Integer.parseInt(str);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		if(blockPlacedOrBroken(true, e.getBlock(), e.getPlayer(), e))
			e.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		if(blockPlacedOrBroken(false, e.getBlock(), e.getPlayer(), e))
			e.setCancelled(true);
	}
	
	public boolean blockPlacedOrBroken(boolean blockPlaced, Block b, Player p, Event e){		
		ArrayList<ProtectedCuboidRegion> pcr = getRegions();
		
		if(pcr == null)
			return false;
		for(ProtectedCuboidRegion reg: getRegions()){
			
			if(getOwner(reg.getId()) == p){
				return false;
			}
			
			if(reg.contains(b.getX(), b.getY(), b.getZ())){
				String message="";
				if(blockPlaced)
					message = plugin.getConfig().getString("messages.cannotPlaceBlock");
				else
					message = plugin.getConfig().getString("messages.cannotBreakBlock");
				
				message = message.replace("&", "§");
				message = message.replace("{{propertyid}}", reg.getId());
				p.sendMessage(message);
				return true;
			}
		}
		
		return false;
	}
	
	private Player getOwner(String prop){
		
		Set<String> properties;
		if(plugin.p.getConfigurationSection("properties") == null){
			return null;
		}
		
		properties = plugin.p.getConfigurationSection("properties").getKeys(false);
		
		for(String property: properties){
			if(property.equals(prop)){
				String playerUID = plugin.p.getString("properties."+property+".owner");
				for(Player player: Bukkit.getOnlinePlayers()){
					if(player.getUniqueId().toString().equals(playerUID)){
						return player;
					}
				}
			}
		}
		
		return null;
	}
	
	public static String getPropFromSign(Block sign){
		if(plugin.p.getConfigurationSection("properties") == null)
			return null;
		Set<String> properties = plugin.p.getConfigurationSection("properties").getKeys(false);
		
		
		for(String property: properties){
			if(		plugin.p.getInt("properties."+property+".sign.x") == sign.getX() &&
					plugin.p.getInt("properties."+property+".sign.y") == sign.getY() &&
					plugin.p.getInt("properties."+property+".sign.z") == sign.getZ()){
				
				return property;
				
			}
		}
		
		
		return "";
	}
	
	public static ArrayList<ProtectedCuboidRegion> getRegions(){
		ArrayList<ProtectedCuboidRegion> pcr = new ArrayList<>();
		
		if(plugin.p.getConfigurationSection("properties") == null)
			return null;
		
		Set<String> properties = plugin.p.getConfigurationSection("properties").getKeys(false);
		
		for(String propertyS: properties){
			int property = 0;
			try{
				property = Integer.parseInt(propertyS);
			}catch(Exception e){
				ArithiaProperties.log.log(Level.SEVERE, "[ArithiaProperties] Property: "+propertyS+" WAS NOT INTEGER (called in event handler)");
				ArithiaProperties.log.log(Level.SEVERE, "[ArithiaProperties] disabling...");
				plugin.getServer().getPluginManager().disablePlugin(plugin);
				return null;
			}
			
			Set<String> regions = plugin.p.getConfigurationSection("properties."+property+".regions").getKeys(true);
			
			for(String region: regions){
				int MIx = plugin.p.getInt("properties."+property+".regions."+region+".BVMinimum.x");
				int MIy = plugin.p.getInt("properties."+property+".regions."+region+".BVMinimum.y");
				int MIz = plugin.p.getInt("properties."+property+".regions."+region+".BVMinimum.z");
				
				int MAx = plugin.p.getInt("properties."+property+".regions."+region+".BVMaximum.x");
				int MAy = plugin.p.getInt("properties."+property+".regions."+region+".BVMaximum.y");
				int MAz = plugin.p.getInt("properties."+property+".regions."+region+".BVMaximum.z");
				ProtectedCuboidRegion cr = new ProtectedCuboidRegion(property+"", new BlockVector(MIx, MIy, MIz), new BlockVector(MAx, MAy, MAz));
				cr.setPriority(plugin.getConfig().getInt("priority"));
				pcr.add(cr);
			}
			
		}
		
		return pcr;
	}
}

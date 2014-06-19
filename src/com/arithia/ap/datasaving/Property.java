package com.arithia.ap.datasaving;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.arithia.ap.MyEventHandler;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;

public class Property  implements Serializable{
	private static final long serialVersionUID = -3518021135893171975L;
	private World world;
	private ArrayList<Region> regions = new ArrayList<Region>();
	
	public void set(int propID, World world){
		this.world = world;
		
		ArrayList<ProtectedCuboidRegion> pcrs = MyEventHandler.getRegions();
		for(ProtectedCuboidRegion pcr: pcrs){
			if(pcr.getId().equals(String.valueOf(propID))){
				Region r = new Region();
				r.setBlocks(pcr);
				regions.add(r);
			}
		}
	}
	
	public World getWorld(){
		return world;
	}
	
	public ArrayList<Region> getRegions(){
		return regions;
	}
	
	public class Region implements Serializable{
		private static final long serialVersionUID = 1098735155562460387L;
		private HashMap<BlockVector, String> blockTypes = new HashMap<BlockVector, String>();
		private BlockVector min;
		private BlockVector max;
		
		public BlockVector getMin(){
			return min;
		}
		
		public BlockVector getMax(){
			return max;
		}
		
		public void setBlocks(ProtectedCuboidRegion pcr){
			min = pcr.getMinimumPoint();
			max = pcr.getMaximumPoint();
			
			for(int x = min.getBlockX(); x<max.getBlockX(); x++){
				for(int y = min.getBlockY(); y<max.getBlockY(); y++){
					for(int z = min.getBlockZ(); z<max.getBlockZ(); z++){
						blockTypes.put(new BlockVector(x, y, z), getMat(x, y, z));
					}
				}
			}
		}
		
		private String getMat(int x, int y, int z){
			Location l = new Location(world, x, y, z);
			Block b = l.getBlock();
			if(b == null){
				return Material.AIR.toString();
			}else{
				return b.getType().toString();
			}
		}
		
		public Material getBlockType(int x, int y, int z){
			BlockVector bv = new BlockVector(x, y, z);
			
			if(blockTypes.containsKey(bv)){
				return Material.getMaterial(blockTypes.get(bv));
			}else{
				return null;
			}
		}
	}
}

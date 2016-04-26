package me.badeye.plugins.horde;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Skull;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

public class PlayerManager implements Listener{

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static HashMap<String, PlayerData> players = new HashMap();
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static HashMap<Location, Inventory> tombstones = new HashMap();
	public static HashMap<Location, Long> tombstoneTimer = new HashMap();
	public static HashMap<Location, Material> tombstoneOldBlock = new HashMap();
	public static HashMap<Location, Byte> tombstoneOldBlockData = new HashMap();
	
	static main plugin;
	
  	public PlayerManager(main instance) {
  		plugin = instance;
  	}
  
  	public static boolean isAlreadyInWorld(Player p){
  		return plugin.getConfig().contains("Data." + p.getName());
  	}
  	
    public static boolean isNotPlaying(String p){
      return (Bukkit.getPlayer(p) == null) || (!players.containsKey(p));
    }
  
  
  	public static void loadPlayer(Player p){
  		if(PlayerManager.isAlreadyInWorld(p)){
  	        putPlayer(p, false);
  		}  
  		else {
  			putPlayer(p, true);
  			plugin.getConfig().set("Data." + p.getName() + ".bleeding", false);
  			plugin.getConfig().set("Data." + p.getName() + ".legs-broken", false);
  			plugin.getConfig().set("Data." + p.getName() + ".in-combat", false);
  			plugin.getConfig().set("Data." + p.getName() + ".scoreboard.kills", Integer.valueOf(0));
  			plugin.getConfig().set("Data." + p.getName() + ".scoreboard.murders", Integer.valueOf(0));
  			plugin.getConfig().set("Data." + p.getName() + ".scoreboard.minutes-survived", Integer.valueOf(0));
  			plugin.getConfig().set("Data." + p.getName() + ".scoreboard.money", Integer.valueOf(0));
  			plugin.getConfig().set("Data." + p.getName() + ".scoreboard.total-kills", Integer.valueOf(0));
  			plugin.getConfig().set("Data." + p.getName() + ".scoreboard.total-murders", Integer.valueOf(0));
  			plugin.getConfig().set("Data." + p.getName() + ".scoreboard.total-minutes-survived", Integer.valueOf(0));
  			plugin.getConfig().set("Data." + p.getName() + ".scoreboard.total-money", Integer.valueOf(0));
  			plugin.saveConfig();
  		}
  		PlayerManager.setMoneyItem(p);
  		ScoreboardHelper.createPlayer(p);
  		p.setLevel(0);
  		p.setCompassTarget(new Location(p.getWorld(), 0 , 64, -16777216)); //64^4 = 16777216
  	}
  
    private static void putPlayer(Player p, boolean defaults){
    	if (defaults) {
    		players.put(p.getName(), new PlayerData(0, 0, 0, 0, 0, 0, 0, 0, false, false, false));
    	} 
    	else {
    		players.put(p.getName(), new PlayerData(
    		plugin.getConfig().getInt("Data." + p.getName() + ".scoreboard.kills"), 
    		plugin.getConfig().getInt("Data." + p.getName() + ".scoreboard.murders"), 
    		plugin.getConfig().getInt("Data." + p.getName() + ".scoreboard.total-kills"), 
    		plugin.getConfig().getInt("Data." + p.getName() + ".scoreboard.total-murders"), 
    		plugin.getConfig().getInt("Data." + p.getName() + ".scoreboard.minutes-survived"),
    		plugin.getConfig().getInt("Data." + p.getName() + ".scoreboard.money"),
    		plugin.getConfig().getInt("Data." + p.getName() + ".scoreboard.total-minutes-survived"),
    		plugin.getConfig().getInt("Data." + p.getName() + ".scoreboard.total-money"),
    		plugin.getConfig().getBoolean("Data." + p.getName() + ".bleeding"), 
    		plugin.getConfig().getBoolean("Data." + p.getName() + ".legs-broken"),
    		plugin.getConfig().getBoolean("Data." + p.getName() + ".in-combat")));
      }
    }
  
  
  	public static void deadPlayer(Player p, int moneyDropAmount){
  		PlayerManager.getData(p.getName()).bleeding = false;
		PlayerManager.getData(p.getName()).bonesBroken = false;
		PlayerManager.getData(p.getName()).inCombat = false;
		PlayerManager.getData(p.getName()).kills = 0;
		PlayerManager.getData(p.getName()).murders = 0;
		PlayerManager.getData(p.getName()).minutesSurvived = 0;
		PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - moneyDropAmount;
  	}
  
  
  	public static void savePlayer(Player p){ //saves money : 1
		plugin.getConfig().set("Data." + p.getName() + ".bleeding", Boolean.valueOf(getData(p.getName()).bleeding));
		plugin.getConfig().set("Data." + p.getName() + ".legs-broken", Boolean.valueOf(getData(p.getName()).bonesBroken));
		plugin.getConfig().set("Data." + p.getName() + ".in-combat", Boolean.valueOf(getData(p.getName()).inCombat));
		plugin.getConfig().set("Data." + p.getName() + ".scoreboard.kills", Integer.valueOf(getData(p.getName()).kills));
		plugin.getConfig().set("Data." + p.getName() + ".scoreboard.murders", Integer.valueOf(getData(p.getName()).murders));
		plugin.getConfig().set("Data." + p.getName() + ".scoreboard.total-kills", Integer.valueOf(getData(p.getName()).totalKills));
		plugin.getConfig().set("Data." + p.getName() + ".scoreboard.total-murders", Integer.valueOf(getData(p.getName()).totalMurders));
		plugin.getConfig().set("Data." + p.getName() + ".scoreboard.minutes-survived", Integer.valueOf(getData(p.getName()).minutesSurvived));
		plugin.getConfig().set("Data." + p.getName() + ".scoreboard.money", Integer.valueOf(getData(p.getName()).money));
		plugin.getConfig().set("Data." + p.getName() + ".scoreboard.total-minutes-survived", Integer.valueOf(getData(p.getName()).playtime));
		plugin.getConfig().set("Data." + p.getName() + ".scoreboard.total-money", Integer.valueOf(getData(p.getName()).totalMoney));
		plugin.saveConfig();
		plugin.saveDefaultConfig();
  	}
  
  	public static PlayerData getData(String p){
  		return (PlayerData)players.get(p);
  	}
  	
  	public static void setMoneyItem(Player p){
	   	int moneySlotId = plugin.getConfig().getInt("money.money-slot");
	   	
  		p.getInventory().setItem(moneySlotId, new ItemStack(Material.NETHER_STAR, 1));
  		ItemStack moneyItem = p.getInventory().getItem(moneySlotId);
		ItemMeta money_meta = moneyItem.getItemMeta();
		money_meta.setDisplayName("§F" + getData(p.getName()).money + " §4Blood Drops");
		moneyItem.setItemMeta(money_meta);
		p.getInventory().setItem(moneySlotId, moneyItem);
		p.updateInventory();
  	}
  	
    public static boolean isInsideOfLobby(Player p)
    {
      Location lobby = getLobby(p);
      int radius = plugin.getConfig().getInt("lobby.radius");
      
      if(lobby.distance(p.getLocation()) <= radius) 
    	  PlayerManager.getData(p.getName()).inCombat = false;;
      return lobby.distance(p.getLocation()) <= radius;
    }
    
    public static Location getLobby(Player p)
    {
      Location lobby = p.getLocation();
      lobby.setX(plugin.getConfig().getDouble("lobby.x"));
      lobby.setY(plugin.getConfig().getDouble("lobby.y"));
      lobby.setZ(plugin.getConfig().getDouble("lobby.z"));
      
      return lobby;
    }
    
  	
    public static void onServerTick(long tickID)
    {
      @SuppressWarnings({ "rawtypes", "unchecked" })
      ArrayList<String> toRemove = new ArrayList();
      for (String pn : players.keySet()) {
        if (isNotPlaying(pn))
        {
          toRemove.add(pn);
        }
        else
        {
          Player p = Bukkit.getPlayer(pn);
          PlayerData data = (PlayerData)players.get(pn);
          if (tickID % 1200L == 0L && !isInsideOfLobby(p))
          {
            data.minutesSurvived += 1;
            data.playtime += 1;
          }
          if (tickID % (60L) == 0L)
          {
        	  
          }
          if (tickID % 10L == 0L)
          {
        	
            ScoreboardHelper.update(tickID);
        	
          }
          if (tickID % 120L == 0L && !isInsideOfLobby(p))
          {
            if (data.bleeding) {
              p.damage(1.0D);
            }
          }
          if ((data.bonesBroken) && (!p.hasPotionEffect(PotionEffectType.SLOW)) && !isInsideOfLobby(p)) {
            p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 999999, 2), true);
          }
          else if(!(data.bonesBroken) && (p.hasPotionEffect(PotionEffectType.SLOW)) || isInsideOfLobby(p)){
        	p.removePotionEffect(PotionEffectType.SLOW);
          }
        }
      }
      String pn;
      for (Iterator<String> i = toRemove.iterator(); i.hasNext(); players.remove(pn)) {
        pn = (String)i.next();
      }
      
      //Tombstones
      for(Location loc : tombstoneTimer.keySet()){
    	  if (tickID % (50L) == 0L)
          {
        	  if(tickID >= tombstoneTimer.get(loc) + 12000){
        		  removeTombstone(loc);
        	  }
          }
      }
    }
    
    @SuppressWarnings("deprecation")
	public static void createTombstone(Player p, Location loc, List<ItemStack> drops){
    	
    	Material blockAtDeath = loc.getBlock().getType();
    	
    	if(blockAtDeath == Material.STATIONARY_WATER || blockAtDeath == Material.WATER || blockAtDeath == Material.STATIONARY_LAVA || blockAtDeath == Material.LAVA){
    		for(int i = 0; i>0; i++){
    			loc.setY(loc.getY() + i);
    			if(loc.getBlock().getType() == Material.AIR){
    				break;
    			} else if(i == 30)
    				break;
    		}
    	}
    	
    		Material oldBlock = loc.getBlock().getType();
    		Byte oldBlockData = loc.getBlock().getData();
    		
    		Block replaceBlock = loc.getBlock();
    		replaceBlock.setType(Material.SKULL);
    		replaceBlock.setData((byte) 1);
    		
    		BlockState skullState = ((Skull) replaceBlock.getState());
    		
    		((Skull) skullState).setSkullType(SkullType.ZOMBIE);
    		((Skull) replaceBlock.getState()).update();
    		
    		Inventory inv = Bukkit.getServer().createInventory(p, 27, "Body of " + p.getDisplayName());
    		for(int i = 0; i<drops.size(); i++){
    			if(drops.get(i).getType() != Material.GHAST_TEAR && drops.get(i).getType() != Material.NETHER_STAR)
    				if(inv.firstEmpty() != -1)
    					inv.addItem(drops.get(i));
    		}
    		
    		tombstones.put(replaceBlock.getLocation(), inv);
    		tombstoneTimer.put(replaceBlock.getLocation(), main.tickID);
    		tombstoneOldBlock.put(replaceBlock.getLocation(), oldBlock);
    		System.out.println("create data: " + oldBlockData);
    		tombstoneOldBlockData.put(replaceBlock.getLocation(), oldBlockData);
    		
    		List<String> list = plugin.getConfig().getStringList("Tombstone");
    		Location reploc = replaceBlock.getLocation();
    		
    		list.add(String.valueOf(reploc.getX() + " " + reploc.getY() + " " + reploc.getZ()) + " " + oldBlock + " " + oldBlockData);
    		plugin.getConfig().set("Tombstone", list);
    		plugin.saveConfig();
    	
    }
    
    public static void removeTombstone(Location loc){
    	  Material oldBlock = tombstoneOldBlock.get(loc);
    	  Byte oldBlockData = tombstoneOldBlockData.get(loc);
    	  
		  PlayerManager.tombstones.remove(loc);
		  PlayerManager.tombstoneTimer.remove(loc);
		  
		  Block replaceBlock = loc.getBlock();
		  replaceBlock.setType(oldBlock);
		  System.out.println("Data" + oldBlockData);
		  replaceBlock.setData(oldBlockData, true);
		  replaceBlock.getState().update();
		  for (int i = 0; i < 8; i++)
  		    loc.getWorld().playEffect(loc, Effect.SMOKE, i);
		  	loc.getWorld().playEffect(loc, Effect.EXTINGUISH, 500);
  	  
		  
		  List<String> list = plugin.getConfig().getStringList("Tombstone");
		  list.remove(String.valueOf(loc.getX() + " " + loc.getY() + " " + loc.getZ()) + " " + oldBlock + " " + oldBlockData);
		  plugin.getConfig().set("Tombstone", list);
		  plugin.saveConfig();
    }
    
    public static void removeAllTombstones(){
    	List<String> list = plugin.getConfig().getStringList("Tombstone");
    	for(int i = 0; i < list.size(); i++){
    		String locPart[] = list.get(i).split(" ");
    		Location tombLoc = new Location(plugin.getServer().getWorld("world"), 0, 0, 0);
    		tombLoc.setX(Float.valueOf(locPart[0]));
    		tombLoc.setY(Float.valueOf(locPart[1]));
    		tombLoc.setZ(Float.valueOf(locPart[2]));
    		
    		Material oldBlockMat = Material.getMaterial(locPart[3]);
    		Byte oldBlockData = Byte.valueOf(locPart[4]);
    		Block replaceBlock = tombLoc.getBlock();
	    	replaceBlock.setType(oldBlockMat);
	    	replaceBlock.setData(oldBlockData);
	    	replaceBlock.getState().update();
    	}
    	list.clear();
    	plugin.getConfig().set("Tombstone", list);
		plugin.saveConfig();
    }
    
    public static void buyItems(Player p, String line_0, String line_2, String line_3, Location loc){
    	if(line_0.contains("Spawnkit Shop")){ //Horde sign
  		  PermissionUser user = PermissionsEx.getUser(p);
  		  int money = PlayerManager.getData(p.getName()).money;
  		  
  		//Makarov
	    	  if(line_2.contains("1 Makarov Mag") && line_3.matches( "" )){
	    		  if(money >= 100){
	    			  if(!p.hasPermission("Horde.kit.makarov1")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought 1 extra Makarov Magazine for 150 Blood Drops!");
	    				  user.addPermission("Horde.kit.makarov1");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 100;
	    				  PlayerManager.setMoneyItem(p);
	    				  for (int i = 0; i < 8; i++)
	    			  		    loc.getWorld().playEffect(loc, Effect.SMOKE, i);
	    				  		p.playSound(loc, Sound.ZOMBIE_METAL, 0.5F, 1.0F);
	    				  		p.playSound(loc, Sound.LEVEL_UP, 0.5F, 1.0F);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	  }
	    	  
	    	  else if(line_2.contains("1 Makarov Mag") && line_3.matches( " " )){
	    		  if(money >= 150){
	    			  if(!p.hasPermission("Horde.kit.makarov2")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought 1 extra Makarov Magazine for 150 Blood Drops!");
	    				  user.addPermission("Horde.kit.makarov2");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 150;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	  }
	    	  
	    	//bandage
	    	  else if(line_2.contains("1 Bandage") && line_3.matches( "" )){
	    		  if(money >= 100){
	    			  if(!p.hasPermission("Horde.kit.bandage1")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought 1 extra Bandage for 100 Blood Drops!");
	    				  user.addPermission("Horde.kit.bandage1");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 100;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	  }
	    	  
	    	  else if(line_2.contains("1 Bandage") && line_3.matches( " " )){
	    		  if(money >= 150){
	    			  if(!p.hasPermission("Horde.kit.bandage2")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought 1 extra Bandage for 100 Blood Drops!");
	    				  user.addPermission("Horde.kit.bandage2");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 150;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	  }
	    	  
	    	//morphine
	    	  else if(line_2.contains("1 Morphine") && line_3.matches( "" )){
	    		  if(money >= 200){
	    			  if(!p.hasPermission("Horde.kit.morphine1")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought 1 extra Morphine for 200 Blood Drops!");
	    				  user.addPermission("Horde.kit.morphine1");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 200;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	  }
	    	  
	    	  else if(line_2.contains("1 Morphine") && line_3.matches( " " )){
	    		  if(money >= 300){
	    			  if(!p.hasPermission("Horde.kit.morphine2")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought 1 extra Morphine for 200 Blood Drops!");
	    				  user.addPermission("Horde.kit.morphine2");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 300;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	  }
	    	  
	    	//steak
	    	  else if(line_2.contains("1 Steak") && line_3.matches( "" )){
	    		  if(money >= 100){
	    			  if(!p.hasPermission("Horde.kit.steak1")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought 1 extra Steak for 150 Blood Drops!");
	    				  user.addPermission("Horde.kit.steak1");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 100;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	  }
	    	  
	    	  else if(line_2.contains("1 Steak") && line_3.matches( " " )){
	    		  if(money >= 150){
	    			  if(!p.hasPermission("Horde.kit.steak2")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought 1 extra Steak for 150 Blood Drops!");
	    				  user.addPermission("Horde.kit.steak2");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 150;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	  }
	    	  
	    	//corwbar
	    	  else if(line_2.contains("Crowbar") && line_3.matches( "" )){
	    		  if(money >= 600){
	    			  if(!p.hasPermission("Horde.kit.crowbar")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought a Crowbar for 600 Blood Drops!");
	    				  user.addPermission("Horde.kit.crowbar");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 600;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	 }
	    	  
		  //remington
	    	  else if(line_2.contains("Remington") && line_3.matches( "" )){
	    		  if(money >= 1000){
	    			  if(!p.hasPermission("Horde.kit.remington")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought a Remington for 1000 Blood Drops!");
	    				  user.addPermission("Horde.kit.remington");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 1000;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	 }
	    	  
		//shot
	    	  else if(line_2.contains("16 Pellets") && line_3.matches( "" )){
	    		  if(money >= 200){
	    			  if(!p.hasPermission("Horde.kit.shot1")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought 16 extra Pellets for 200 Blood Drops!");
	    				  user.addPermission("Horde.kit.shot1");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 200;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	 }
	    	  else if(line_2.contains("16 Pellets") && line_3.matches( " " )){
	    		  if(money >= 300){
	    			  if(!p.hasPermission("Horde.kit.shot2")){
	    				  p.sendMessage(ChatColor.YELLOW + "You permanently bought 16 extra Pellets for 300 Blood Drops!");
	    				  user.addPermission("Horde.kit.shot2");
	    				  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - 300;
	    				  PlayerManager.setMoneyItem(p);
	    			  } else p.sendMessage(ChatColor.RED + "You have already bought this item.");
	    	  	  } else p.sendMessage(ChatColor.RED + "You do not have enough Blood Drops to buy this!");
	    	 }
	    	  
    	}
    }
}

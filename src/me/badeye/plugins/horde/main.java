package me.badeye.plugins.horde;

import java.util.HashMap;
import java.util.Random;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class main extends JavaPlugin{
    Logger log = Logger.getLogger("Horde"); //Logger to print stuff in console
    public static Plugin plugin;
    public static String name = "";
    public static String version = "";
    public static long tickID = 0L;
    
    public static HashMap<Player, Integer> movingPlayers = new HashMap();
    public static HashMap<Player, Location> playerLocations = new HashMap();
    
    
    
	@SuppressWarnings("unused")
	@Override
    public void onEnable() {
    	final FileConfiguration config = this.getConfig();
    	saveConfig();
    	plugin = this;
    	
    	name = plugin.getDescription().getName();
    	version = plugin.getDescription().getVersion();
        this.log.info("["+name+"] Version "+version+" is now enabled");
        
        registerEvents(this, new PlayerListener(this), new PlayerManager(this));
        
      //register sign class /modification/plugin
        Plugin insignsPlugin = getServer().getPluginManager().getPlugin("InSigns");
        if((insignsPlugin != null) && insignsPlugin.isEnabled()) {
            new SignSendListener(insignsPlugin);
            System.out.println("["+name+"] Plugin 'InSigns' found. Using it now.");
        } else {
            System.out.println("["+name+"] Plugin 'InSigns' not found. Additional sign features disabled.");
        }
        
      //Every server tick the method gets called => bleeding check etc
        getServer().getScheduler().runTaskTimer(this, new Runnable(){
          public void run(){
            main.tickID += 1L;
            PlayerManager.onServerTick(main.tickID);
          }
        }, 1L, 1L);
        ScoreboardHelper.setup();
        PlayerManager.removeAllTombstones();
    }

    @Override
    public void onDisable() {
        this.log.info("["+name+"] is now disabled");
        plugin = null; //To stop memory leaks
    }
    
    
  //Much eaisier then registering events in 10 diffirent methods
    public static void registerEvents(org.bukkit.plugin.Plugin plugin, Listener... listeners) {
    	for (Listener listener : listeners) {
    		Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
    	}
    }
    
  //To access the plugin variable from other classes
    public static Plugin getPlugin() {
    	return plugin;
    }
    
  //I am lazy, here go all commands
    @SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
    	Player p = null;
    	
  	  	if(cmd.getName().equalsIgnoreCase("fix")){
  	  		if(sender instanceof Player){
  	  			p = (Player) sender;
  	  			if(p.isOp()){
  	  				if ((args.length == 0)) {
  	  					PlayerManager.getData(sender.getName()).bleeding = false;
  	  					PlayerManager.getData(sender.getName()).bonesBroken = false;
  	  					sender.sendMessage(ChatColor.YELLOW + "Leathal wounds got fixed.");
  	  					return true;
  	  				}
  	  				else if ((args.length == 1) && (args[0].matches("-h"))) {
  	  					PlayerManager.getData(sender.getName()).bleeding = false;
  	  					PlayerManager.getData(sender.getName()).bonesBroken = false;
  	  					p.setHealth(20);
  	  					p.setFoodLevel(19);
  	  					p.setSaturation(5.0F);
  	  					sender.sendMessage(ChatColor.YELLOW + "Your body recovered fully.");
  	  					return true;
  	  				}
  	  			}
  	  		}
  	  	}
  	  	
  	  	else if(cmd.getName().equalsIgnoreCase("rs")){
  	  		if(sender instanceof Player){
  	  			p = (Player) sender;
  	  			if(args.length == 0 && p.isOp()){
  	  				int amtOfSpawns = -1;
  	  				
  	  				if(plugin.getConfig().getConfigurationSection("Spawns").getKeys(false) != null){
  	  					amtOfSpawns = plugin.getConfig().getConfigurationSection("Spawns").getKeys(false).size();
  	  				}
  	  				if(amtOfSpawns > 0){
  	  					Random rand = new Random();
  	  					int random = 0;
  	  					if(amtOfSpawns > 1)
  	  						random = rand.nextInt(amtOfSpawns);
  	  					else 
  	  						random = 0;
  	  					
  	  				
  	  					String cfgString = plugin.getConfig().getString("Spawns." + random);
  	  					if(cfgString != null){
  	  					Location rdmLoc = new Location(plugin.getServer().getWorld("world"), 0, 0, 0);
  	  					String locParts[] = cfgString.split(" ");
  	  					
  	  					rdmLoc.setX(Double.valueOf(locParts[0]));
  	  					rdmLoc.setY(Double.valueOf(locParts[1]));
  	  					rdmLoc.setZ(Double.valueOf(locParts[2]));
  	  					rdmLoc.setYaw(Float.valueOf(locParts[3]));
  	  					rdmLoc.setPitch(Float.valueOf(locParts[4]));
  	  					
  	  					p.teleport(rdmLoc);
  	  					p.sendMessage(ChatColor.YELLOW + "Wuuussshhh! Teleported to " + random);
  	  					}
  	  				}
  	  				else p.sendMessage(ChatColor.RED + "No spawns were found!");
  	  			}
  	  			else if(args.length > 0 && p.isOp()){
  	  				if(args[0].matches("set")){
  	  					if(args.length == 1)
  	  						p.sendMessage(ChatColor.RED + "Too few arguments. Usage: /rs [add|remove] [n]");
  	  					else if (args[1].matches("-?\\d+")){
  	  						if(plugin.getConfig().getString("Spawns." + args[1]) == null){
  	  							Location loc = p.getLocation();
  	  							double x = loc.getX();
  	  							double y = loc.getY();
  	  							double z = loc.getZ();
  	  							float yaw = loc.getYaw();
  	  							float pitch = loc.getPitch();
  	  							plugin.getConfig().set("Spawns." + args[1], (x + " " + y + " " + z + " " + yaw + " " + pitch));
  	  							plugin.saveConfig();
  	  						
  	  							p.sendMessage(ChatColor.YELLOW + "Spawn point " + args[1] + " set.");
  	  						}
  	  						else p.sendMessage(ChatColor.RED + "This spawn already exists!");
  	  				  	}
  	  				}
  	  				else if(args[0].matches("remove")){
  	  					if(args.length == 1)
  	  						p.sendMessage(ChatColor.RED + "Too few arguments. Usage: /rs [add|remove] [n]");
  	  					else if (args[1].matches("-?\\d+")){
  	  						if(plugin.getConfig().getString("Spawns." + args[1]) != null){
  	  							plugin.getConfig().set("Spawns." + args[1], null);
  	  							plugin.saveConfig();
  	  						
  	  							p.sendMessage(ChatColor.YELLOW + "Spawn point " + args[1] + " removed.");
  	  						}
  	  						else p.sendMessage(ChatColor.RED + "This spawn does not exist!");
  	  					}
	  				}
  	  				else p.sendMessage(ChatColor.RED + "Usage: /rs [add|remove] [n]");
  	  			}
  	  		}
  	  		return true;
  	  	}
  	  	
  	  	else if(cmd.getName().equalsIgnoreCase("money")){
  	  		if((sender instanceof Player || (sender instanceof ConsoleCommandSender && args.length== 3)) && sender.isOp()){
  	  			boolean error = false;
  	  			int amount = 0;
  	  			
  	  			if(args.length < 2){
  	  				if(sender instanceof Player){
  	  					sender.sendMessage(ChatColor.RED + "Usage: /money [set|add] [n] <Player>");
  	  				}
  	  				if(sender instanceof ConsoleCommandSender){
  	  					System.out.println("Usage: /money [set|add] [n] <Player>");
  	  				}
  	  				return false;
  	  			}
  	  			
  	  			if(args.length == 3 && error == false){
  	  				if(!args[2].matches("-?\\d+")){
  	  					p = Bukkit.getPlayerExact(args[2]);
  	  					if(p == null){
  	  						error = true;
  	  					}
  	  				}else{
  	  					error = true;
  	  				}
  	  			}
  	  			
  	  			else if(args.length == 2 && error == false){
  	  				p = (Player) sender;
  	  			}
  	  			if(error == false){
  	  				if(args[0].matches("add")){
  	  					if(args[1].matches("-?\\d+")){
  	  						PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money + Integer.valueOf(args[1]);
  	  						PlayerManager.getData(p.getName()).totalMoney = PlayerManager.getData(p.getName()).totalMoney + Integer.valueOf(args[1]);
  	  						PlayerManager.setMoneyItem(p);
  	    	  				if(sender instanceof Player){
  	    	  					sender.sendMessage(ChatColor.YELLOW + "" + Integer.valueOf(args[1]) +" blood drops added to "+ p.getName() +". He has now " + PlayerManager.getData(p.getName()).money);
  	    	  				}
  	  	  					System.out.println( sender.getName() + " has added "+ Integer.valueOf(args[1]) +" blood drops to " +p.getName() + ". He has now " + PlayerManager.getData(p.getName()).money);
  	  	  				
  	  					}else{
  	  						error = true;
  	  					}
  	  				}
  	  				else if(args[0].matches("set")){
  	  					if(args[1].matches("-?\\d+")){
  	  						int oldMoney = PlayerManager.getData(p.getName()).money;
  	  						PlayerManager.getData(p.getName()).money = Integer.valueOf(args[1]);
  	  						
  	  						int newMoney = PlayerManager.getData(p.getName()).money;
  	  						int difference = 0;
  	  						difference = newMoney - oldMoney;
  	  						PlayerManager.getData(p.getName()).totalMoney = (PlayerManager.getData(p.getName()).totalMoney + difference);
  	  						
  	  						
  	  						PlayerManager.setMoneyItem(p);
  	    	  				if(sender instanceof Player){
  	    	  					sender.sendMessage(ChatColor.YELLOW + "Blood drop balance of player " + p.getName() + " has been set to " + Integer.valueOf(args[1]));
  	    	  				}
  	  	  					System.out.println( sender.getName() + " has set the blood drops of " + p.getName() + " to " + Integer.valueOf(args[1]));
  	  	  				
  	  					}else{
  	  						error = true;
  	  					}
  	  				}
  	  			}
  	  			
  	  			if(error == true){
  	  				if(sender instanceof Player){
  	  					if(sender.isOp())
  	  					sender.sendMessage(ChatColor.RED + "Usage: /money [set|add] [n] <Player>");
  	  				}
  	  				if(sender instanceof ConsoleCommandSender){
  	  					System.out.println("Usage: /money [set|add] [n] <Player>");
  	  				}
  	  				return false;
  	  			}
  	  			
  	  			
  	  			return true;
  	  		}
  	  	}
  	  	
  	  	else if(cmd.getName().equalsIgnoreCase("lobby")){
  	  		if(sender instanceof Player && sender.isOp()){
  	  			p = (Player) sender;
  	  			
  	  			if(args.length == 0){
  	  				p.teleport(PlayerManager.getLobby(p));
  	  				p.sendMessage(ChatColor.YELLOW + "Wuuussshhh!");
  	  				return true;
  	  			}
  	  			else if(args.length > 0){
  	  				if(args[0].matches("set")){
  	  					Location lobby = p.getLocation();
  	  					plugin.getConfig().set("lobby.x", Double.valueOf(lobby.getX()));
  	  					plugin.getConfig().set("lobby.y", Double.valueOf(lobby.getY()));
  	  					plugin.getConfig().set("lobby.z", Double.valueOf(lobby.getZ()));
  	  					plugin.saveConfig();
  	  					plugin.saveDefaultConfig();
  	  					p.sendMessage(ChatColor.YELLOW + "Lobby has been set!");
  	  					return true;
  	  				}
	  			}
  	  		}
  	  	}
  	  	
  	  	else if(cmd.getName().equalsIgnoreCase("equip")){
  	  		if(sender instanceof Player && sender.isOp()){
  	  			p = (Player) sender;
  	  			
  	  			final int makarov = 27004;
  	  			final int makarovMag = 27005;
  	  			final int remington = 27030;
  	  			final int remingtonShot = 27032;
  	  			
  	  			ItemStack bandage = new ItemStack(Material.PAPER, 1);
  	  			ItemMeta bandage_meta = bandage.getItemMeta();
  	  			bandage_meta.setDisplayName("§FBandage");
  	  			bandage.setItemMeta(bandage_meta);
  	  			
  	  			ItemStack morphine = new ItemStack(Material.BLAZE_ROD, 1);
  	  			ItemMeta morphine_meta = morphine.getItemMeta();
  	  			morphine_meta.setDisplayName("§FMorphine");
  	  			morphine.setItemMeta(morphine_meta);
  	  			
  	  			ItemStack baseball = new ItemStack(Material.GOLD_SWORD, 1);
	  			ItemMeta baseball_meta = baseball.getItemMeta();
	  			baseball_meta.setDisplayName("§FBaseball Bat");
	  			baseball.setItemMeta(baseball_meta);
  	  			
	  			ItemStack crowbar = new ItemStack(Material.STONE_AXE, 1);
	  			ItemMeta crowbar_meta = crowbar.getItemMeta();
	  			crowbar_meta.setDisplayName("§FCrowbar");
	  			crowbar.setItemMeta(crowbar_meta);
	  			
	  			ItemStack clear = new ItemStack(Material.AIR, 1);
	  			
	  		//Clear slots
	  			for(int slot = 0; slot <= 35; slot++){
	  				int switchVariable = slot;
	  				
	  				if(slot == 27 || slot == 30 || slot  == 34 || slot == 35) //switch slots, money slot, delete slot, they should stay
	  				    switchVariable = 888;
	  				switch (switchVariable) 
	  				{ 
	  				    case 888:
	  				    	break;
	  				    default: 
	  				    	p.getInventory().setItem(slot, clear);
	  				    	break;
	  				} 
	  			}
	  			
	  		//Set defaults
	  			p.getInventory().setItem(0, baseball);
	  			p.getInventory().setItem(7, bandage);
	  			p.getInventory().setItem(8, new ItemStack(Material.COOKED_BEEF, 1));
	  			p.updateInventory();
	  			
	  		//If forge is enabled, these are more defaults to be added:
	  			if((args.length == 1) && (args[0].matches("-m"))) {
	  				p.getInventory().setItem(2, new ItemStack(makarov, 1));
	  				p.getInventory().setItem(9, new ItemStack(makarovMag, 1));
  	  				p.getInventory().setItem(10, new ItemStack(makarovMag, 1));
  	  				p.getInventory().setItem(18, new ItemStack(makarovMag, 1));
  	  				p.getInventory().setItem(19, new ItemStack(makarovMag, 1));
	  			}
  	  			
  	  			
	  		//Add extra permission items
  	  			if(p.hasPermission("Horde.kit.bandage1")){
  	  				p.getInventory().setItem(6, bandage);
  	  			}
  	  			if(p.hasPermission("Horde.kit.bandage2")){
  	  				p.getInventory().setItem(5, bandage);
  	  			}
  	  			if(p.hasPermission("Horde.kit.steak1")){
  	  				p.getInventory().setItem(16, new ItemStack(Material.COOKED_BEEF, 1));
  	  			}
  	  			if(p.hasPermission("Horde.kit.steak2")){
	  				p.getInventory().setItem(25, new ItemStack(Material.COOKED_BEEF, 1));
	  			}
  	  			if(p.hasPermission("Horde.kit.morphine1")){
  	  				p.getInventory().setItem(17, morphine);
  	  			}
  	  			if(p.hasPermission("Horde.kit.morphine2")){
	  				p.getInventory().setItem(26, morphine);
	  			}
  	  			if(p.hasPermission("Horde.kit.crowbar")){
	  				p.getInventory().setItem(0, crowbar);
  	  			}
  	  			
  	  		//If fore is enabled, these items get set aswell if palyers have the permission for it
  	  			if((args.length == 1) && (args[0].matches("-m"))) {
  	  				if(p.hasPermission("Horde.kit.remington")){
  	  					p.getInventory().setItem(1, new ItemStack(remington, 1));
  	  				}
  	  				if(p.hasPermission("Horde.kit.shot1")){
  	  					p.getInventory().setItem(12, new ItemStack(remingtonShot, 16));
  	  				}
  	  				if(p.hasPermission("Horde.kit.shot2")){
  	  					p.getInventory().setItem(21, new ItemStack(remingtonShot, 16));
  	  				}
  	  				if(p.hasPermission("Horde.kit.makarov1")){
  	  					p.getInventory().setItem(11, new ItemStack(makarovMag, 1));
  	  				}
  	  				if(p.hasPermission("Horde.kit.makarov2")){
  	  					p.getInventory().setItem(20, new ItemStack(makarovMag, 1));
  	  				}
  	  			}
  	  			
  	  			p.updateInventory();
  	  			return true;
  	  		}
  	  	}
  	  	return false;
    }
}

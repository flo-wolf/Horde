package me.badeye.plugins.horde;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class ScoreboardHelper{
	
  static main plugin;
  public ScoreboardHelper(main instance) {
		plugin = instance;
  }
  private static ScoreboardManager manager;
  public static HashMap<String, Integer> combatTime = new HashMap<String, Integer>();
  private static HashMap<String, Scoreboard> boards = new HashMap();
  private static HashMap<String, Scoreboard> boardsLobby = new HashMap();
  
  public static void setup()
  {
    manager = Bukkit.getScoreboardManager();
  }
  
  public static void newCombatTime(String pn, Integer time){
	  if(!combatTime.containsKey(pn)){
			combatTime.put(pn, 40);
	  }
	  combatTime.put(pn, 40);
  }
  
  public static void createPlayer(Player p)
  {
	  
    //Outside lobby scoreboard
    Scoreboard board = manager.getNewScoreboard();
    Objective stats = board.registerNewObjective("stats", "dummy");
    stats.setDisplayName(ChatColor.GREEN + "Not In Combat");
    stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Blood level")).setScore(0);
    stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Zombies killed")).setScore(0);
    stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Players killed")).setScore(0);
    stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Minutes played")).setScore(0);
    
    boards.put(p.getName(), board);
    
  //Inside lobby scoreboard
    Scoreboard boardLobby = manager.getNewScoreboard();
    Objective statsLobby = boardLobby.registerNewObjective("lobby", "dummy");
    statsLobby.setDisplayName(ChatColor.GRAY + "Alltime Stats");
    statsLobby.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Zombies killed")).setScore(0);
    statsLobby.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Players killed")).setScore(0);
    statsLobby.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Money made")).setScore(0);
    statsLobby.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Minutes played")).setScore(0);
    
    boardsLobby.put(p.getName(), boardLobby);
  }
  
  long oldTickID = 0;
  public static void update(long tickID)
  {
    ArrayList<String> toRemove = new ArrayList();
    for (String pn : boards.keySet()) {
    	if (Bukkit.getPlayer(pn) == null)
    	{
    		toRemove.add(pn);
    	}
    	else if(!PlayerManager.isInsideOfLobby((Player) Bukkit.getPlayer(pn))) //Not in lobby
    	{
    		Player p = (Player) Bukkit.getPlayer(pn);
    		Scoreboard board = (Scoreboard)boards.get(pn);
    		Objective stats = board.getObjective("stats");
        
    		if(PlayerManager.getData(pn).inCombat == true){
    			if(tickID % 20L == 0L){
    				if(!combatTime.containsKey(pn)){
    					combatTime.put(pn, 41);
    				}
    				
    				stats.setDisplayName(ChatColor.RED + "In Combat " + combatTime.get(pn));
    				combatTime.put(pn, combatTime.get(pn) - 1);
    				if(combatTime.get(pn) == 0){
    					combatTime.remove(pn);
    					PlayerManager.getData(pn).inCombat = false;
    				}
    			}
    		}
    		else if(PlayerManager.getData(pn).inCombat == false){
    			stats.setDisplayName(ChatColor.GREEN + "Not In Combat");
    		}
        
    		
    		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Blood level")).setScore((int) (p.getHealth() * 600.0D));
    		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Zombies killed")).setScore(PlayerManager.getData(pn).kills);
    		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Players killed")).setScore(PlayerManager.getData(pn).murders);
    		stats.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Minutes played")).setScore(PlayerManager.getData(pn).minutesSurvived);
    		if (stats.getDisplaySlot() != DisplaySlot.SIDEBAR) {
    			stats.setDisplaySlot(DisplaySlot.SIDEBAR);
    		}
    		if (p.getScoreboard() != board) {
    			p.setScoreboard(board);
    		}
    	}
    	
    	else if(PlayerManager.isInsideOfLobby((Player) Bukkit.getPlayer(pn))){ //in lobby
    		
    		Player p = (Player) Bukkit.getPlayer(pn);
    		Scoreboard boardLobby = (Scoreboard)boardsLobby.get(pn);
    		Objective statsLobby = boardLobby.getObjective("lobby");

    		statsLobby.setDisplayName(ChatColor.RED + "Alltime Stats");
    		statsLobby.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Zombies killed")).setScore(PlayerManager.getData(pn).totalKills);
    		statsLobby.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Players killed")).setScore(PlayerManager.getData(pn).totalMurders);
    		statsLobby.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Money made")).setScore(PlayerManager.getData(pn).totalMoney);
    		statsLobby.getScore(Bukkit.getOfflinePlayer(ChatColor.GRAY + "Minutes played")).setScore(PlayerManager.getData(pn).playtime);
    		
    		if (statsLobby.getDisplaySlot() != DisplaySlot.SIDEBAR) {
    			statsLobby.setDisplaySlot(DisplaySlot.SIDEBAR);
    		}
    		if (p.getScoreboard() != boardLobby) {
    			p.setScoreboard(boardLobby);
    		}
    	}
      
    }
    for (String pn : toRemove) {
      removePlayer(pn);
    }
  }
  
  public static void removePlayer(String pn)
  {
    Player p = Bukkit.getPlayer(pn);
    if ((p != null) && (boards.containsKey(pn))) {
      ((Scoreboard)boards.get(pn)).clearSlot(DisplaySlot.SIDEBAR);
    }
    boards.remove(pn);
  }
}

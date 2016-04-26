package me.badeye.plugins.horde;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PlayerVisibilityBar
{
  static main plugin;	
	
  public PlayerVisibilityBar(main instance) {
	plugin = instance;
  }	

  public static void updatePlayerVisibilityBar(Player p, Boolean playerWalking, Boolean playerJumping, Boolean sneaking)
  {
    float visibility = 0.4F;
    if (sneaking) {
      if (visibility > 0.1F) {
        visibility -= 0.2F;
      } else {
        visibility = 0.0F;
      }
    }
    if (p.isSprinting()) {
    	visibility += 0.2F;
    }
    Material blockTypeAtPlayerLoc = p.getLocation().getBlock().getType();
    if (blockTypeAtPlayerLoc != Material.AIR) {
      if (visibility > 0.2F) {
        visibility -= 0.15F;
      } else {
        visibility = 0.0F;
      }
    }
    if (p.isSleeping()) {
      visibility = 0.0F;
    }
    if (playerJumping){
        if (visibility + 0.2F <= 1.0F) {
          visibility += 0.2F;
        } else {
          visibility = 1.0F;
        }
      }
    if (playerWalking){
      if (visibility + 0.2F <= 1.0F) {
        visibility += 0.2F;
      } else {
        visibility = 1.0F;
      }
    }
    p.setExp(visibility);
  }
  
  public static float getVisibility(Player p)
  {
    return p.getExp();
  }
}


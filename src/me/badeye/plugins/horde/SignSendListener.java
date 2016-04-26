package me.badeye.plugins.horde;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import de.blablubbabc.insigns.Changer;
import de.blablubbabc.insigns.InSigns;

public class SignSendListener {
    
    public SignSendListener(Plugin insignsPlugin) {
        InSigns insigns = (InSigns) insignsPlugin;
        insigns.addChanger(new Changer("[buy]", "p") {
        	
        	
        	
        	@Override
            public String getValue(Player p, Location signLocation) {
        			Block locBlock = p.getWorld().getBlockAt(signLocation);
        			Sign sign = (Sign) locBlock.getState();
        			String line_0 = sign.getLine(0);
      		      	String line_1 = sign.getLine(1);
      		      	String line_2 = sign.getLine(2);
      		      	String line_3 = sign.getLine(3);
      		      	
      		      	//if item was bought already
      		      	if(p.hasPermission("Horde.kit.makarov1") && line_2.matches("1 Makarov Mag") && line_3.matches("")) 		return (ChatColor.GREEN + "Payed 100 BD");
      		      	else if(p.hasPermission("Horde.kit.makarov2") && line_2.matches("1 Makarov Mag") && line_3.matches(" ")) 	return (ChatColor.GREEN + "Payed 150 BD");
      		      	else if(p.hasPermission("Horde.kit.shot1") && line_2.matches("16 Pellets") && line_3.matches("")) 			return (ChatColor.GREEN + "Payed 200 BD");
      		      	else if(p.hasPermission("Horde.kit.shot2") && line_2.matches("16 Pellets") && line_3.matches(" ")) 		return (ChatColor.GREEN + "Payed 300 BD");
      		      	else if(p.hasPermission("Horde.kit.bandage1") && line_2.matches("1 Bandage") && line_3.matches("")) 		return (ChatColor.GREEN + "Payed 100 BD");
      		      	else if(p.hasPermission("Horde.kit.bandage2") && line_2.matches("1 Bandage") && line_3.matches(" ")) 		return (ChatColor.GREEN + "Payed 150 BD");
      		      	else if(p.hasPermission("Horde.kit.steak1") && line_2.matches("1 Steak") && line_3.matches("")) 			return (ChatColor.GREEN + "Payed 100 BD");
      		      	else if(p.hasPermission("Horde.kit.steak2") && line_2.matches("1 Steak") && line_3.matches(" ")) 			return (ChatColor.GREEN + "Payed 150 BD");
      		      	else if(p.hasPermission("Horde.kit.morphine1") && line_2.matches("1 Morphine") && line_3.matches("")) 		return (ChatColor.GREEN + "Payed 200 BD");
      		      	else if(p.hasPermission("Horde.kit.morphine2") && line_2.matches("1 Morphine") && line_3.matches(" ")) 	return (ChatColor.GREEN + "Payed 300 BD");
      		      	else if(p.hasPermission("Horde.kit.remington") && line_2.matches("Remington") && line_3.matches("")) 		return (ChatColor.GREEN + "Payed 1000 BD");
      		      	else if(p.hasPermission("Horde.kit.crowbar") && line_2.matches("Crowbar") && line_3.matches("")) 			return (ChatColor.GREEN + "Payed 600 BD");
      		      	
      		      	//not bought yet
      		      	else if(!p.hasPermission("Horde.kit.makarov1") && line_2.matches("1 Makarov Mag") && line_3.matches("")) 	return (ChatColor.DARK_RED + "Buy 100 BD");
    		      	else if(!p.hasPermission("Horde.kit.makarov2") && line_2.matches("1 Makarov Mag") && line_3.matches(" ")) 	return (ChatColor.DARK_RED + "Buy 150 BD");
    		      	else if(!p.hasPermission("Horde.kit.shot1") && line_2.matches("16 Pellets") && line_3.matches("")) 		return (ChatColor.DARK_RED + "Buy 200 BD");
    		      	else if(!p.hasPermission("Horde.kit.shot2") && line_2.matches("16 Pellets") && line_3.matches(" ")) 		return (ChatColor.DARK_RED + "Buy 300 BD");
    		      	else if(!p.hasPermission("Horde.kit.bandage1") && line_2.matches("1 Bandage") && line_3.matches("")) 		return (ChatColor.DARK_RED + "Buy 100 BD");
    		      	else if(!p.hasPermission("Horde.kit.bandage2") && line_2.matches("1 Bandage") && line_3.matches(" ")) 		return (ChatColor.DARK_RED + "Buy 150 BD");
    		      	else if(!p.hasPermission("Horde.kit.steak1") && line_2.matches("1 Steak") && line_3.matches("")) 			return (ChatColor.DARK_RED + "Buy 100 BD");
    		      	else if(!p.hasPermission("Horde.kit.steak2") && line_2.matches("1 Steak") && line_3.matches(" ")) 			return (ChatColor.DARK_RED + "Buy 150 BD");
    		      	else if(!p.hasPermission("Horde.kit.morphine1") && line_2.matches("1 Morphine") && line_3.matches("")) 	return (ChatColor.DARK_RED + "Buy 200 BD");
    		      	else if(!p.hasPermission("Horde.kit.morphine2") && line_2.matches("1 Morphine") && line_3.matches(" ")) 	return (ChatColor.DARK_RED + "Buy 300 BD");
    		      	else if(!p.hasPermission("Horde.kit.remington") && line_2.matches("Remington") && line_3.matches("")) 		return (ChatColor.DARK_RED + "Buy 1000 BD");
    		      	else if(!p.hasPermission("Horde.kit.crowbar") && line_2.matches("Crowbar") && line_3.matches("")) 			return (ChatColor.DARK_RED + "Buy 600 BD");
        		
                return "horde error";
            }
        });
    }
}
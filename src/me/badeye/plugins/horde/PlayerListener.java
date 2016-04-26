package me.badeye.plugins.horde;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetEvent.TargetReason;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Door;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener{
  
  static main plugin;
		 
  public PlayerListener(main instance) {
	  plugin = instance;
  }
  
  @EventHandler
  public void onBreakBlock(BlockBreakEvent e){
	  Player p = e.getPlayer();
	  if(!p.isOp() && plugin.getConfig().getBoolean("settings.no-block-break")){
		  e.setCancelled(true);
	  }
  }
  
  @EventHandler
  public void onPlayerDamage(EntityDamageEvent e){
	  if (e.getEntityType() == EntityType.PLAYER){
		  final Player p = (Player) e.getEntity();
		  
		  if(!PlayerManager.isInsideOfLobby(p)){
			  if ((plugin.getConfig().getBoolean("player.bleeding")) && (p.getGameMode() != GameMode.CREATIVE)) {
				  if (Math.random() >= 1.0D - plugin.getConfig().getDouble("player.bleeding-chance")){
					  PlayerManager.getData(p.getName()).bleeding = true;
			  		  p.sendMessage(ChatColor.RED + "You are bleeding!");
				  }
			  }
			  
		      int height = (int)(e.getDamage() + 3.0D);
		      
		      if ((e.getCause() == EntityDamageEvent.DamageCause.FALL) && (plugin.getConfig().getBoolean("player.leg-braking")) && 
		          (height >= plugin.getConfig().getInt("player.leg-braking-height")))
		      {
		          PlayerManager.getData(p.getName()).bonesBroken = true;
		          p.sendMessage(ChatColor.RED + "You broke your legs!");
		      }
		      
		      //Combat logging stuff:
		      PlayerManager.getData(p.getName()).inCombat = true;
		      ScoreboardHelper.newCombatTime(p.getName(), 40);
		  }
		  else{
			  e.setCancelled(true);
		  }
	  }
  }
  
  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent e){
	  e.getDamager();
	  if(e.getDamager() instanceof Player){
		  Player p = (Player) e.getDamager();
		  PlayerManager.getData(p.getName()).inCombat = true;
		  ScoreboardHelper.newCombatTime(p.getName(), 40);
		  if(e.getEntity() instanceof Monster){
			  Location mobLoc = e.getEntity().getLocation();
			  Location playerLoc = e.getDamager().getLocation();
			  if(mobLoc.distance(playerLoc) < 20){
				  ((Creature) e.getEntity()).setTarget((LivingEntity) e.getDamager());
			  }
		  }
	  }
  }
  
  @EventHandler
  public void onJoin(PlayerJoinEvent e){
	  Player p = e.getPlayer();
	  PlayerManager.loadPlayer(p);
	  
	  if(PlayerManager.getData(p.getName()).inCombat == true){
		  int money = PlayerManager.getData(p.getName()).money;
		  int moneyDropAmount = (money/plugin.getConfig().getInt("money.combat-logging-percentage-fine"));
		  
		  PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money - moneyDropAmount;
		  p.sendMessage(ChatColor.RED + "You lost " + moneyDropAmount + " blood drops due to combat logging!");
		  PlayerManager.setMoneyItem(p);
		  PlayerManager.getData(p.getName()).inCombat = false;
		  ScoreboardHelper.combatTime.remove(p.getName());
	  }
  }
  
  @EventHandler
  public void onQuit(PlayerQuitEvent e){
	  Player p = e.getPlayer();
	  PlayerManager.savePlayer(p);
  }
  
  @EventHandler
  public void onPlayerDeath (PlayerDeathEvent e){
	  Player p = e.getEntity();
	  
	//TOMBSTONE CREATION
	  PlayerManager.createTombstone(p, e.getEntity().getLocation(), e.getDrops());
	  
	//Money stuff
	  int difference = 0;
	  int moneyDropAmount = plugin.getConfig().getInt("money.player-drop-amount");
	  if(PlayerManager.getData(p.getName()).money - moneyDropAmount < 0){
		  difference = Math.abs(PlayerManager.getData(p.getName()).money - moneyDropAmount);
	  }
	  
	  ItemStack moneyItem = new ItemStack(Material.NETHER_STAR, 1);
	  ItemMeta money_meta = moneyItem.getItemMeta();
	  money_meta.setDisplayName("§F" + (moneyDropAmount - difference) + " §4Blood Drops");
	  moneyItem.setItemMeta(money_meta);

	  e.getDrops().clear(); //No drops, for later tombstone creation.
	  e.getDrops().add(moneyItem);
	  
	  PlayerManager.deadPlayer(p, (moneyDropAmount - difference));
	  e.setDeathMessage(p.getName() + " was killed");
	  
	
  }
  
  @EventHandler
  public void onEntityDeath (EntityDeathEvent e){
	  Player p = (Player) e.getEntity().getKiller();
	  
	  if((e.getEntity() instanceof LivingEntity) && (e.getEntity().getKiller() instanceof Player) && !(e.getEntity() instanceof Player)) {
		  PlayerManager.getData(p.getName()).kills = PlayerManager.getData(p.getName()).kills + 1;
		  PlayerManager.getData(p.getName()).totalKills = PlayerManager.getData(p.getName()).totalKills + 1;
		  
		  ItemStack moneyItem = new ItemStack(Material.NETHER_STAR, 1);
		  ItemMeta money_meta = moneyItem.getItemMeta();
		  money_meta.setDisplayName("§F" + 1 + " §4Blood Drops");
		  moneyItem.setItemMeta(money_meta);
		  
		  e.setDroppedExp(0);
		  e.getDrops().clear(); //No drops, for later tombstone creation.
		  e.getDrops().add(moneyItem);
	  }
	  else if((e.getEntity() instanceof Player) && (e.getEntity().getKiller() instanceof Player)) {
		  PlayerManager.getData(p.getName()).murders = PlayerManager.getData(p.getName()).murders + 1;
		  PlayerManager.getData(p.getName()).totalMurders = PlayerManager.getData(p.getName()).totalMurders + 1;
	  }
  }
  
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onCreatureSpawn(CreatureSpawnEvent e){
	  if(e.getEntity() instanceof Creature){
		  Location loc = e.getLocation();
		  loc.setY(loc.getY() - 1);
		  
		  Block blockUnderMob = loc.getWorld().getBlockAt(loc);
		  Material blockType = blockUnderMob.getType();
		  SpawnReason reason = e.getSpawnReason();
		  
		  
		  if(blockType == Material.GRASS && reason != SpawnReason.CUSTOM  && reason != SpawnReason.SPAWNER_EGG ){
			  if (Math.random() >= 1.0D - 0.8D)
				  e.setCancelled(true);
		  }
		  else if(e.isCancelled() && (reason == SpawnReason.CUSTOM  || reason == SpawnReason.SPAWNER_EGG)){
			  e.setCancelled(false);
		  	  e.getEntity().setCanPickupItems(false);
		  	  e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999999, 2), true);
		  }
		  
		  else if(!e.isCancelled()){
			  e.getEntity().setCanPickupItems(false);
			  e.getEntity().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 99999999, 2), true);
		  }
	  }
  }
  
  @EventHandler
  public void onRespawn(PlayerRespawnEvent e){
	  final Player p = e.getPlayer();
	  
	  
	  Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
		   public void run() {
			   p.setFoodLevel(19);
			   PlayerManager.setMoneyItem(p);
			   p.teleport(PlayerManager.getLobby(p));
		   } 
	  },1L); // 1 Sekunde (evtl 2*20L für 2 Sekunden)
  }
  
  @EventHandler
  public void onEntityBurn(EntityCombustEvent e){ //to allow bow flame burning, check the damagers item in hand if its a bow and has flame on it with p.getItemInHand
	  if(e.getEntityType() == EntityType.ZOMBIE){
		  if(e.getEntity().getFireTicks() == -1){
			  e.setCancelled(true);  
		  }
	  }
  }
  
  @SuppressWarnings("deprecation")
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent e){
	  Player p = e.getPlayer();
	  Material itemType = Material.AIR;
	  if(e.getItem() != null){
		  if(e.getItem().getType() != null){
			  ItemStack item = e.getItem();
			  itemType = item.getType();
		  }
	  }
	  Action action = e.getAction();
	  
	  int bandageId = plugin.getConfig().getInt("player.bandage-item");
	  Material bandage = Material.getMaterial(bandageId);
	  
	  int morphineId = plugin.getConfig().getInt("player.leg-fix-item");
	  Material morphine = Material.getMaterial(morphineId);
	  
	  if ((action == Action.RIGHT_CLICK_AIR) || (action == Action.RIGHT_CLICK_BLOCK)){
		  if (itemType == bandage){
	          if (plugin.getConfig().getBoolean("player.bleeding")){
	        	  if (p.getItemInHand().getAmount() < 2){
	        		  p.setItemInHand(new ItemStack(Material.AIR, 0));
	        	  } 
	        	  else {
	        		  p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
	        	  }
	        	  
	        	  PlayerManager.getData(p.getName()).bleeding = false;
	        	  p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 1.0F, 1.0F);
	        	  p.sendMessage(ChatColor.YELLOW + "You have fixed your wounds.");
	          }
	      }
		  else if (itemType == morphine){
	          if (plugin.getConfig().getBoolean("player.leg-braking")){
	        	  if (p.getItemInHand().getAmount() < 2){
	        		  p.setItemInHand(new ItemStack(Material.AIR, 0));
	        	  } 
	        	  else {
	        		  p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
	        	  }
	        	  
	        	  PlayerManager.getData(p.getName()).bonesBroken = false;
	        	  p.playSound(p.getLocation(), Sound.ENDERDRAGON_WINGS, 1.0F, 1.0F);
	        	  p.sendMessage(ChatColor.YELLOW + "You have fixed your broken legs.");
	          }
	      }
	  }
	  
	  //SIGN SHOP SHIT / Tombstone shit
	  
	  if(e.hasBlock()){
		  Block clickedBlock = e.getClickedBlock();
		  
		  if(clickedBlock.getType() == Material.WALL_SIGN){
			  
			  Location loc = clickedBlock.getLocation();
			  Sign sign = (Sign) clickedBlock.getState();
		      String line_0 = sign.getLine(0);
		      String line_1 = sign.getLine(1);
		      String line_2 = sign.getLine(2);
		      String line_3 = sign.getLine(3);
		      
		      PlayerManager.buyItems(p, line_0, line_2, line_3, loc);
		  }
		  else if(clickedBlock.getType() == Material.SKULL && e.getAction() == Action.RIGHT_CLICK_BLOCK){
			  if(PlayerManager.tombstones.containsKey(clickedBlock.getLocation())){
					  Inventory inv = PlayerManager.tombstones.get(clickedBlock.getLocation());
					  p.openInventory(inv);
					  //PlayerManager.tombstones.remove(clickedBlock.getLocation());
					  //PlayerManager.tombstoneTimer.remove(clickedBlock.getLocation());
				  
			  }
		  }
		  else if(clickedBlock.getType() == Material.SKULL && e.getAction() == Action.LEFT_CLICK_BLOCK){
			  if(p.getItemInHand().getType() == Material.STONE_AXE && PlayerManager.tombstones.containsKey(clickedBlock.getLocation())){
				  p.sendMessage(ChatColor.YELLOW + "You have destroyed a tombstone.");
				  PlayerManager.removeTombstone(clickedBlock.getLocation());
			  }
		  }
	  }
	  
	//Flansmod check for combat = true
	  int itemID = p.getItemInHand().getTypeId();
	  if(p.getItemInHand() != null){
		  if(itemID > 27000 && !PlayerManager.isInsideOfLobby(p)){
			  if(
				itemID == 27004 ||
				itemID == 27006 ||
				itemID == 27007 ||
				itemID == 27010 ||
				itemID == 27013 ||
				itemID == 27014 ||
				itemID == 27015 ||
				itemID == 27017 ||
				itemID == 27018 ||
				itemID == 27022 ||
				itemID == 27024 ||
				itemID == 27026 ||
				itemID == 27028 ||
				itemID == 27030 ||
				itemID == 27031 ||
				itemID == 27033 ||
				itemID == 27035  )
				  	PlayerManager.getData(p.getName()).inCombat = true;
			  		ScoreboardHelper.newCombatTime(p.getName(), 40);
		  }
	  }
  }
  
  @SuppressWarnings("deprecation")
  @EventHandler
  public void onPlayerPickupItem(PlayerPickupItemEvent e){
	  Player p = e.getPlayer();
	  
	  
	  ItemStack pick = e.getItem().getItemStack();
	  ItemMeta pick_Meta = pick.getItemMeta();
	  String dispName = pick_Meta.getDisplayName();
	  String[] dispNameParts; 
	  
	  int pickAmount = pick.getAmount();
	  int amount = pick.getAmount();
	 
	  if(dispName != null){
		  if(dispName.contains("Blood Drops")){
			  dispNameParts = dispName.split(" ");
			  amount = Integer.parseInt(dispNameParts[0].replaceAll("\\D", ""));
		  }
		  
		   if(plugin.getConfig().getBoolean("money.enable-money")){
			   	int moneyItemId = plugin.getConfig().getInt("money.money-item");
			   	
			   	Material moneyMaterial = Material.getMaterial(moneyItemId);
				if(moneyMaterial == e.getItem().getItemStack().getType()){ //Wir heben geld auf
					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 0.5F, 1.0F);
					
					//money + 1
					PlayerManager.getData(p.getName()).money = PlayerManager.getData(p.getName()).money + (amount * pickAmount);
					PlayerManager.getData(p.getName()).totalMoney = PlayerManager.getData(p.getName()).totalMoney + (amount * pickAmount);
					
					PlayerManager.setMoneyItem(p);
				}
		   }
	  }
  }
  
  @EventHandler
  public void onTarget(EntityTargetEvent e){
	  if(e.getTarget() != null){
		  if(e.getTarget().getType() == EntityType.PLAYER){ //semi working, npe's in this line o.o; canceling works depending on xp elvel, pathfinding has to stop.
			  //also, canceling atm ONLY cancels interactions in a radius of 14 entitys, should be the other way around. like every entity cancels it exept etc.
			  Player p = (Player) e.getTarget();
			  if(e.getReason() == TargetReason.TARGET_ATTACKED_OWNER){
				  e.setCancelled(true);
			  }
			  
			  else if(e.getReason() != TargetReason.FORGOT_TARGET && e.getReason() != TargetReason.CUSTOM && e.getReason() != TargetReason.TARGET_ATTACKED_ENTITY && e.getReason() != TargetReason.TARGET_ATTACKED_OWNER){
				  e.setCancelled(true);

			      float visibility = PlayerVisibilityBar.getVisibility(p);
			      List<Entity> nearbyEnts = null;
			      
			      if (visibility <= 0.05F) { //SneakStand + hide
			        nearbyEnts = p.getNearbyEntities(4.0D, 4.0D, 4.0D);
			      } else if (visibility <= 0.25F) { //sneakStand
			        nearbyEnts = p.getNearbyEntities(6.0D, 6.0D, 6.0D);
			      } else if (visibility <= 0.45F) { //stand and sneakwalk
			        nearbyEnts = p.getNearbyEntities(8.0D, 8.0D, 8.0D);
			      } else if (visibility <= 0.65F) { //walk
			        nearbyEnts = p.getNearbyEntities(10.0D, 10.0D, 10.0D);
			      } else if (visibility <= 0.85F) { //sprint
			        nearbyEnts = p.getNearbyEntities(14.0D, 14.0D,14.0D);
			      } else if (visibility <= 1.0F) { //jumpsprint
				       nearbyEnts = p.getNearbyEntities(16.0D, 16.0D, 16.0D);
				  }
			      if (nearbyEnts != null) {
			    	  for (Entity forEnt : nearbyEnts) {
			    		  if (forEnt.getType() == EntityType.ZOMBIE) {
			    			  e.setCancelled(false);
			    		  }
			    	  }
			      }
			  }
		  }
	  }

  }
  
  @EventHandler(priority=EventPriority.HIGHEST)
  public void onPlayerMove(PlayerMoveEvent event)
  {
	  
	  
      final Player p = event.getPlayer();
      
      double fromY = event.getFrom().getY();
      double toY = event.getTo().getY();
      boolean sneaking = false;
      
      if(p.isSneaking()){
    	  sneaking = true;
      }
      
      if(p.isSprinting()){
      }
      
      Location distanceFromWalk = event.getFrom();
      Location distanceToWalk = event.getTo();
      distanceFromWalk.setY(0);
      distanceToWalk.setY(0);
      double distanceWalking = distanceFromWalk.distance(distanceToWalk);
      

      double distanceJumping = Math.abs(fromY - toY);
      
      
      
      if(distanceWalking < 0.11D && distanceJumping % 1 == 0){ //double distanceJumping has no decimal numbers => is whole
    	  PlayerVisibilityBar.updatePlayerVisibilityBar(p, false, false, sneaking);
      }
      else if(distanceWalking < 0.11D && !(distanceJumping % 1 == 0)){
    	  PlayerVisibilityBar.updatePlayerVisibilityBar(p, false, true, sneaking);
      }
      else if(distanceWalking >= 0.11D && distanceJumping % 1 == 0){
    	  PlayerVisibilityBar.updatePlayerVisibilityBar(p, true, false, sneaking);
      }
      else if(distanceWalking >= 0.11D && !(distanceJumping % 1 == 0)){
    	  PlayerVisibilityBar.updatePlayerVisibilityBar(p, true, true, sneaking);
      }
  }
  
  @EventHandler(priority=EventPriority.HIGH)
  public void onFoodChange(FoodLevelChangeEvent e){
	  Player p = (Player) e.getEntity();
	  Material i = p.getItemInHand().getType();
	  
	  if(PlayerManager.isInsideOfLobby(p)){
		  e.setCancelled(true);
		  p.setFoodLevel(19);
	  } else if(e.getFoodLevel() > 19 && (
			  	i == Material.COOKED_BEEF || 
			    i == Material.APPLE || 
			    i == Material.BREAD || 
			    i == Material.COOKED_CHICKEN || 
			    i == Material.GOLDEN_APPLE || 
			    i == Material.MELON || 
			    i == Material.COOKED_FISH)){
		  e.setCancelled(true);
		  p.setFoodLevel(19);
		  p.setSaturation(5.0F);
	  }
  }
  
  @EventHandler
  public void onConsume(PlayerItemConsumeEvent e){
	  Material i = e.getItem().getType();
	  Player p = e.getPlayer();
	  
	  if(i == Material.COOKED_BEEF || 
		i == Material.APPLE || 
		i == Material.BREAD || 
		i == Material.COOKED_CHICKEN || 
		i == Material.GOLDEN_APPLE || 
		i == Material.MELON || 
		i == Material.COOKED_FISH){
		  p.setFoodLevel(19);
		  if(p.getFoodLevel() == 20){
			  p.setFoodLevel(19);
		  }
		  p.setSaturation(5.0F);
		  if(p.getHealth() + 3 >= 20){
			  p.setHealth(20);
		  }
		  else p.setHealth(p.getHealth() + 3);
		  e.setCancelled(true);
		  
		  if(p.getItemInHand().getAmount() == 1)
			  p.setItemInHand(null);
		   else if (p.getItemInHand().getAmount() > 1)
			   p.getItemInHand().setAmount(p.getItemInHand().getAmount() - 1);
		  p.updateInventory();
	  }
  }
  
  @SuppressWarnings("deprecation")
  @EventHandler
  public void onEntityBreakDoor(EntityBreakDoorEvent event)
  {
	  if(plugin.getConfig().getBoolean("settings.no-door-break")){
		  event.setCancelled(true);
	      
	      Block doorBlock = event.getBlock();
	      BlockState blockState = doorBlock.getState();
	      Door door = (Door)blockState.getData();
	      if (door.isTopHalf())
	      {
	        doorBlock = event.getEntity().getWorld().getBlockAt(event.getBlock().getLocation().add(0.0D, -1.0D, 0.0D));
	        blockState = doorBlock.getState();
	        door = (Door)blockState.getData();
	      }
	      door.setOpen(true);
	      blockState.update();
	      
	      if(((Creature) event.getEntity()).getTarget() != null){
	    	  if(((Creature) event.getEntity()).getTarget() instanceof Player){
	    		  Player p = (Player) ((Creature) event.getEntity()).getTarget();
	    		  p.playSound(event.getBlock().getLocation(), Sound.DOOR_OPEN, 0.5F, 1.0F);
	    	  }
	      }
	  }
  }
}









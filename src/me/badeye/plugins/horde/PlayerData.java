package me.badeye.plugins.horde;

public class PlayerData
{
  public int kills;
  public int murders;
  public int totalKills;
  public int totalMurders;
  public int minutesSurvived;
  public int money;
  public int totalMoney;
  public int playtime;
  public boolean bleeding;
  public boolean bonesBroken;
  public boolean inCombat;
  
  public PlayerData(int kills, int murders, int totalKills, int totalMurders, int minutesSurvived, int money, int playtime, int totalMoney, boolean bleeding, boolean bonesBroken, boolean inCombat)
  {
    this.kills = kills;
    this.murders = murders;
    this.totalKills = totalKills;
    this.totalMurders = totalMurders;
    this.minutesSurvived = minutesSurvived;
    this.money = money;
    this.totalMoney = totalMoney;
    this.playtime = playtime;
    this.bleeding = bleeding;
    this.bonesBroken = bonesBroken;
    this.inCombat = inCombat;
  }
}

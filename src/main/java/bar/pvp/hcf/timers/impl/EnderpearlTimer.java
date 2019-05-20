package bar.pvp.hcf.timers.impl;

import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.timers.TimerManager;
import bar.pvp.hcf.utils.CC;
import bar.pvp.hcf.timers.type.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class EnderpearlTimer extends PlayerTimer implements Listener {

    public EnderpearlTimer() {
        super("Enderpearl", 16000F);
        this.setDisplayName(this.getScoreboardConfig().getString("SCOREBOARD.TIMERS.ENDERPEARL"));
        this.setTrailing(true);

        Bukkit.getPluginManager().registerEvents(this, HCTeams.getInstance());
    }

    public void onExpire(Player player) {
        player.sendMessage(CC.translate("&aYou can now use your enderpearls again."));
    }

    @EventHandler
    public void onEnderpearl(PlayerInteractEvent event) {
        if(event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                if(event.getItem() != null && event.getItem().getType().equals(Material.ENDER_PEARL) && event.getPlayer().getGameMode() != GameMode.CREATIVE) {
                Player player = event.getPlayer();
                Profile profile = Profile.getProfile(player);
                if(profile.getPlayerTimers().contains(TimerManager.getEnderpearlTimer())) {
                    player.sendMessage(this.getMessageConfig().getString("TIMERS.STILL_ON_ENDERPEARL_COOLDOWN").replace("%COOLDOWN%", "" + profile.getByName("Enderpearl").getCooldowns().get(player) / 1000F));
                    event.setCancelled(true);
                } else {
                    TimerManager.getEnderpearlTimer().start(player);
                }
            }
        }
    }
}

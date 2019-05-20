package bar.pvp.hcf.timers.impl;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.timers.TimerManager;
import bar.pvp.hcf.timers.type.PlayerTimer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CombatTimer extends PlayerTimer implements Listener {

    public CombatTimer() {
        super("Combat", 30000F);
        this.setDisplayName(this.getScoreboardConfig().getString("SCOREBOARD.TIMERS.COMBAT"));

        Bukkit.getPluginManager().registerEvents(this, HCTeams.getInstance());
    }

    public void onExpire(Player player) {}

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if(!(event.getEntity() instanceof Player) || (!(event.getDamager() instanceof Player))) return;
        Player player = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        Profile profile = Profile.getProfile(player);
        Profile dProfile = Profile.getProfile(damager);

        if(profile.getFaction() != null && dProfile.getFaction() != null) {
            if(profile.getFaction().equals(dProfile.getFaction())) return;
            if(profile.getFaction().getAllies().contains(dProfile.getFaction())) return;
        }

        CombatTimer combatTimer = TimerManager.getCombatTimer();

        if(!profile.getPlayerTimers().contains(combatTimer)) {
            player.sendMessage(getMessageConfig().getString("TIMERS.ENGAGED_COMBAT"));
            start(player);
        }

        if(!dProfile.getPlayerTimers().contains(combatTimer)) {
            damager.sendMessage(getMessageConfig().getString("TIMERS.ENGAGED_COMBAT"));
            start(damager);
        }

        combatTimer.getCooldowns().put(player, this.getDuration());
        combatTimer.getCooldowns().put(damager, this.getDuration());
    }
}

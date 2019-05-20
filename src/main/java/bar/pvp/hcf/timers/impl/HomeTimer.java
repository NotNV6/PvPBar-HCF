package bar.pvp.hcf.timers.impl;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.timers.TimerManager;
import bar.pvp.hcf.timers.type.PlayerTimer;
import bar.pvp.hcf.utils.LocationSerialization;
import bar.pvp.hcf.utils.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;


public class HomeTimer extends PlayerTimer implements Listener {

    public HomeTimer() {
        super("Home", 10000L);
        this.setTrailing(true);
        this.setDisplayName(getScoreboardConfig().getString("SCOREBOARD.TIMERS.HOME"));

        Bukkit.getPluginManager().registerEvents(this, HCTeams.getInstance());
    }

    public void onExpire(Player player){
        player.teleport(LocationSerialization.deserializeLocation(Profile.getProfile(player).getFaction().getHome()));

    }

    public void addTimer(Player player) {
        Profile profile = Profile.getProfile(player);
        if(Claim.getProminentClaimAt(player.getLocation()) != null && (Claim.getProminentClaimAt(player.getLocation()).getTeam() instanceof PlayerTeam) && profile.getFaction() != null && Claim.getProminentClaimAt(player.getLocation()).equals(profile.getFaction())) {
            player.sendMessage(getMessageConfig().getString("TIMERS.HOME.OTHER_TERRITORY"));
            return;
        }

        if(profile.getPlayerTimers().contains(TimerManager.getCombatTimer())) {
            player.sendMessage(getMessageConfig().getString("TIMERS.HOME.IN_COMBAT").replace("%REMAINING%", TimeUtil.getRemainingTime(TimerManager.getCombatTimer().getCooldowns().get(player), true)));
            return;
        }

        this.start(player);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        Location from = event.getFrom();

        if(Profile.getProfile(player).getPlayerTimers().contains(this) && (to.getBlockX() != from.getBlockX() || to.getBlockY() != from.getBlockY() || to.getBlockZ() != from.getBlockZ())) {
            player.sendMessage(getMessageConfig().getString("TIMERS.HOME.CANCELLED_MOVE"));
            this.cancel(player, this);
        }
    }
}

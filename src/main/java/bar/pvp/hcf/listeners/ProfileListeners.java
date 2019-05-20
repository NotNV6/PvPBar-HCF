package bar.pvp.hcf.listeners;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.scoreboard.QueryTask;
import bar.pvp.hcf.utils.CC;
import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.events.player.PlayerDisbandTeamEvent;
import bar.pvp.hcf.teams.events.player.PlayerLeaveTeamEvent;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.utils.LocationSerialization;

import bar.pvp.hcf.utils.player.PlayerUtil;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.*;

import java.math.BigDecimal;
import java.util.Random;


@SuppressWarnings("unused")
public class ProfileListeners implements Listener {

    private HCTeams main = HCTeams.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerUtil.players.add(player);
        Profile profile = new Profile(player);
        profile.setLastInside(Claim.getProminentClaimAt(player.getLocation()));

        if (!player.hasPlayedBefore()) {
            SystemTeam faction = SystemTeam.getByName("Spawn");
            if (faction != null && faction.getHome() != null) {
                player.teleport(LocationSerialization.deserializeLocation(faction.getHome()));
            }
        }

        player.performCommand("team who");

        event.setJoinMessage(null);
    }



    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleportEvent(PlayerTeleportEvent event) {
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.ENDER_PEARL && event.getCause() != PlayerTeleportEvent.TeleportCause.NETHER_PORTAL && event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());

            profile.setLastInside(Claim.getProminentClaimAt(event.getTo()));
        }
    }

    @EventHandler
    public void onPlayerLeaveFactionEvent(PlayerLeaveTeamEvent event) {
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        if (profile.getClaimProfile() != null && profile.getClaimProfile().getTeam() == event.getFaction()) {
            profile.setClaimProfile(null);
        }
    }

    @EventHandler
    public void onFactionDisband(PlayerDisbandTeamEvent event) {
        for (Player player : event.getFaction().getOnlinePlayers()) {
            Profile profile = Profile.getByUuid(player.getUniqueId());
            if (profile.getClaimProfile() != null && profile.getClaimProfile().getTeam() == event.getFaction()) {
                profile.setClaimProfile(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerRespawnEvent(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        SystemTeam faction = SystemTeam.getByName("Spawn");
        if (faction != null && faction.getHome() != null) event.setRespawnLocation(LocationSerialization.deserializeLocation(faction.getHome()));

        profile.setKillStreak(0);

        profile.setLastInside(Claim.getProminentClaimAt(event.getRespawnLocation()));
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.setQuitMessage(null);

        PlayerUtil.players.remove(player);


        player.getInventory().remove(Team.getWand(main));
        Profile profile = Profile.getProfile(player);
        if (profile != null) {
            profile.save();
            Profile.getProfiles().remove(profile);

        }
    }

    @EventHandler
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
        event.getSource().remove(Team.getWand(main));
        event.getDestination().remove(Team.getWand(main));
    }

    @SuppressWarnings("deprecation")
    @EventHandler(ignoreCancelled = true)
    public void onDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player damaged = (Player) e.getEntity();
            Player damager;

            if (e.getDamager() instanceof Player) {
                damager = (Player) e.getDamager();
            } else if (e.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) e.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    damager = (Player) projectile.getShooter();
                } else {
                    return;
                }
            } else {
                return;
            }

            if (damager == damaged)
                return;


            PlayerTeam damagedFaction = Profile.getByUuid(damaged.getUniqueId()).getFaction();
            PlayerTeam damagerFaction = Profile.getByUuid(damager.getUniqueId()).getFaction();

            if (damagedFaction == null || damagerFaction == null) {
                return;
            }

            if (damagedFaction == damagerFaction) {
                damager.sendMessage(main.getLangConfig().getString("FACTION_OTHER.CANNOT_DAMAGE_FRIENDLY").replace("%PLAYER%", damaged.getName()));
                e.setCancelled(true);
                return;
            }

            if (damagedFaction.getAllies().contains(damagerFaction) && !main.getMainConfig().getBoolean("ALLIES.DAMAGE_ALLIES")) {
                damager.sendMessage(main.getLangConfig().getString("FACTION_OTHER.CANNOT_DAMAGE_ALLY").replace("%PLAYER%", damaged.getName()));
                e.setCancelled(true);
            }
        }
    }

    private String[] randomKill = {"%player% &ehas misteriously died.", "%player% &edied, but no one knows how!", "%player% &emade a dumb mistake, and died."};

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();


        if(event.getEntity().getKiller() == null)
            PlayerUtil.getOnlinePlayers().forEach(other -> other.sendMessage(randomKill[new Random().nextInt(randomKill.length)].replace("%player%", CC.RED + player.getName() + "&4[" + player.getStatistic(Statistic.PLAYER_KILLS) + "]")));
        else
            event.setDeathMessage(CC.translate(CC.RED + player.getName() + "&4[" + player.getStatistic(Statistic.PLAYER_KILLS) + "] &ewas slain by &c" + player.getKiller().getName() + "&4[" + player.getKiller().getStatistic(Statistic.PLAYER_KILLS) + "] &eusing &c" + player.getKiller().getItemInHand().getItemMeta().getDisplayName()  + "&e."));


        PlayerTeam playerFaction = PlayerTeam.getByPlayer(player);
        if (playerFaction != null) {
            if(!HCTeams.getInstance().isKitMap())
            playerFaction.freeze(main.getMainConfig().getInteger("FACTION_GENERAL.FREEZE_DURATION"));
            playerFaction.setDeathsTillRaidable(playerFaction.getDeathsTillRaidable().subtract(BigDecimal.ONE));
            playerFaction.sendMessage(main.getLangConfig().getString("ANNOUNCEMENTS.FACTION.PLAYER_DEATH").replace("%PLAYER%", player.getName()).replace("%DTR%", playerFaction.getDeathsTillRaidable() + "").replace("%MAX_DTR%", playerFaction.getMaxDeathsTillRaidable() + "").replace("%COLOR%", playerFaction.isRaidable() ? CC.RED + "": CC.GREEN + ""));
        }

        if(event.getEntity().getKiller() != null)
        Profile.getProfile(event.getEntity().getKiller()).setKillStreak(Profile.getProfile(event.getEntity().getKiller()).getKillStreak()+1);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Claim claim = Claim.getProminentClaimAt(event.getEntity().getLocation());
            if (claim != null) {
                if (claim.getTeam() instanceof SystemTeam && !((SystemTeam) claim.getTeam()).isDeathban())
                    event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority=EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        QueryTask.setupBoard(event);
        Profile profile = Profile.getByUuid(event.getPlayer().getUniqueId());
        Claim claim = Claim.getProminentClaimAt(event.getPlayer().getLocation());
        if (claim != null)
            profile.setLastInside(claim);
    }
}

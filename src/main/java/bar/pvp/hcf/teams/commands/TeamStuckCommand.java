package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.profiles.teleport.ProfileTeleportTask;
import bar.pvp.hcf.profiles.teleport.ProfileTeleportType;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.events.player.PlayerInitiateTeamTeleportEvent;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class TeamStuckCommand extends TeamCommand {
    @Command(name = "f.stuck", aliases = {"team.stuck", "teams.stuck"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getTeleportWarmup() != null) {
            return;
        }

        int time = 0;
        String worldName = player.getLocation().getWorld().getName();
        String root = "TELEPORT_COUNTDOWN.STUCK";


        for (String world : new String[]{"OVERWORLD", "NETHER", "END"}) {
            if (worldName.equalsIgnoreCase(mainConfig.getString(world))) {
                if (!mainConfig.getBoolean(root + "." + world + ".ENABLED")) {
                    player.sendMessage(langConfig.getString("ERROR.NO_STUCK_TELEPORT_IN_WORLD"));
                    return;
                } else {
                    time = mainConfig.getInteger(root + "." + world + ".TIME");
                }
            }
        }

        long hours = TimeUnit.SECONDS.toHours(time);
        long minutes = TimeUnit.SECONDS.toMinutes(time) - (hours * 60);
        long seconds = TimeUnit.SECONDS.toSeconds(time) - ((hours * 60 * 60) + (minutes * 60));

        String formatted;

        if (hours == 0 && minutes > 0 && seconds > 0) {
            formatted = minutes + " minutes and " + seconds + " seconds";
        } else if (hours == 0 && minutes > 0 && seconds == 0) {
            formatted = minutes + " minutes";
        } else if (hours == 0 && minutes == 0 && seconds > 0) {
            formatted = seconds + " seconds";
        } else if (hours > 0 && minutes > 0 && seconds == 0) {
            formatted = hours + " hours and " + minutes + " minutes";
        } else if (hours > 0 && minutes == 0 && seconds > 0) {
            formatted = hours + " hours and " + seconds + " seconds";
        } else {
            formatted = hours + "hours, " + minutes + " minutes and " + seconds + " seconds";
        }

        if (hours == 1) {
            formatted = formatted.replace("hours", "hour");
        }

        if (minutes == 1) {
            formatted = formatted.replace("minutes", "minute");
        }

        if (seconds == 1) {
            formatted = formatted.replace("seconds", "second");
        }


        Location location = null;
        int current = 5;
        while (location == null) {
            for (int x = player.getLocation().getBlockX() - current; x < player.getLocation().getBlockX() + current; x++) {
                for (int z = player.getLocation().getBlockZ() - current; z < player.getLocation().getBlockZ() + current; z++) {
                    Location newLocation = new Location(player.getLocation().getWorld(), x, 0, z);
                    List<Claim> claims = Claim.getClaimsAt(newLocation);
                    if (claims == null) {
                        location = newLocation;
                    } else {
                        for (Claim claim : claims) {
                            if (claim.getTeam() instanceof PlayerTeam) {
                                break;
                            }
                        }
                        location = newLocation;
                        break;
                    }
                }
            }
            current +=5;
        }

        location.setDirection(player.getLocation().getDirection());

        player.sendMessage(langConfig.getString("FACTION_OTHER.TELEPORTING_TO_STUCK").replace("%TIME%", formatted));
        profile.setTeleportWarmup(new ProfileTeleportTask(new PlayerInitiateTeamTeleportEvent(player, null, ProfileTeleportType.STUCK_TELEPORT, time, location.getWorld().getHighestBlockAt(location).getLocation(), player.getLocation())));
        profile.getTeleportWarmup().runTaskLaterAsynchronously(main, (long) (time * 20));
    }
}

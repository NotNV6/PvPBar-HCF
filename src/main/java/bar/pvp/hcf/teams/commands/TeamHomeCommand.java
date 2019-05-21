package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.timers.TimerManager;
import bar.pvp.hcf.utils.LocationSerialization;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class TeamHomeCommand extends TeamCommand {
    @Command(name = "f.home", aliases = {"team.home", "teams.home", "f.hq", "team.hq", "teams.hq"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        Profile profile = Profile.getByUuid(player.getUniqueId());

        if(profile.getPlayerTimers().contains(TimerManager.getEnderpearlTimer())) return;

        Team team;
        if (command.getArgs().length >= 1 && player.hasPermission("hcteams.admin")) {
            String name = command.getArgs(0);
            Team team1 = PlayerTeam.getAnyByString(name);
            if (team1 != null) {
                team = team1;
            } else {
                player.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_FOUND").replace("%NAME%", name));
                return;
            }
        } else {
            team = profile.getFaction();

            if (team == null) {
                player.sendMessage(langConfig.getString("ERROR.NOT_IN_FACTION"));
                return;
            }

        }

        if (team.getHome() == null) {
            player.sendMessage(langConfig.getString("ERROR.HOME_NOT_SET"));
            return;
        }

        Claim claim = Claim.getProminentClaimAt(player.getLocation());
        if (claim != null && claim.getTeam() instanceof SystemTeam && !((SystemTeam) claim.getTeam()).isDeathban()) {
            player.teleport(LocationSerialization.deserializeLocation(team.getHome()));
            return;
        }

        if (player.hasPermission("hcteams.admin") && command.getArgs().length >= 1) {
            player.teleport(LocationSerialization.deserializeLocation(team.getHome()));
        } else {
            int time = 0;
            String worldName = player.getLocation().getWorld().getName();
            String root = "TELEPORT_COUNTDOWN.HOME";


            for (String world : new String[]{"OVERWORLD", "NETHER", "END"}) {
                if (worldName.equalsIgnoreCase(mainConfig.getString(world))) {
                    if (!mainConfig.getBoolean(root + "." + world + ".ENABLED")) {
                        player.sendMessage(langConfig.getString("ERROR.NO_HOME_TELEPORT_IN_WORLD"));
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


            player.sendMessage(langConfig.getString("TIMERS.HOME.HOMING").replace("%TIME%", formatted + ""));
            TimerManager.getHomeTimer().addTimer(player);
        }
    }
}

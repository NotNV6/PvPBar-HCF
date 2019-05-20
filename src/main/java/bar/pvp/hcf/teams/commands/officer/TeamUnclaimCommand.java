package bar.pvp.hcf.teams.commands.officer;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.LocationSerialization;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TeamUnclaimCommand extends TeamCommand {
    @Command(name = "f.unclaim", aliases = {"team.unclaim", "teams.unclaim", "teams.unclaimall", "f.unclaimall", "team.unclaimall"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        String[] args = command.getArgs();
        int argPos = 1;
        boolean skip = false;

        Team team;
        PlayerTeam playerFaction = null;
        if (command.getArgs().length >= 2 && player.hasPermission("ekko.admin")) {
            String name = command.getArgs(1);
            Team team1 = PlayerTeam.getAnyByString(name);
            if (team1 != null) {
                if (team1 instanceof PlayerTeam) {
                    playerFaction = (PlayerTeam) team1;
                }
                team = team1;
                skip = true;
            } else {
                player.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_FOUND").replace("%NAME%", name));
                return;
            }
        } else {
            team = profile.getFaction();
            playerFaction = profile.getFaction();

            if (team == null) {
                Claim claim = Claim.getProminentClaimAt(player.getLocation());
                if (claim != null && player.hasPermission("ekko.admin")) {
                    team = claim.getTeam();
                    playerFaction = null;
                } else {
                    player.sendMessage(langConfig.getString("ERROR.NOT_IN_FACTION"));
                    return;
                }
            }

            if (playerFaction != null && !playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()) && !player.hasPermission("ekko.admin")) {
                player.sendMessage(langConfig.getString("ERROR.NOT_OFFICER_OR_LEADER"));
                return;
            }
        }

        if (command.getLabel().equalsIgnoreCase("f.unclaimall") || args.length >= argPos) {

            if (!command.getLabel().equalsIgnoreCase("f.unclaimall") && !skip && (args.length != argPos || !args[argPos - 1].equalsIgnoreCase("all"))) {
                player.sendMessage(langConfig.getString("INCORRECT_USAGE.UNCLAIM"));
                return;
            }

            if ((playerFaction != null && !playerFaction.getLeader().equals(player.getUniqueId())) && !player.hasPermission("ekko.admin")) {
                player.sendMessage(langConfig.getString("ERROR.NOT_LEADER"));
                return;
            }


            Set<Claim> claims = team.getClaims();
            if (claims.isEmpty()) {
                player.sendMessage(langConfig.getString("ERROR.NO_CLAIMS"));
                return;
            }
            for (Claim claim : new ArrayList<>(claims)) {
                if (team.getHome() != null && claim.isInside(LocationSerialization.deserializeLocation(team.getHome()))) {
                    team.setHome(null);
                }
                claim.remove();
            }

            String message = langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_UNCLAIM_ALL").replace("%PLAYER%", player.getName());

            if (playerFaction != null) {
                playerFaction.sendMessage(message);
                if (!(playerFaction.getOnlinePlayers().contains(player))) {
                    player.sendMessage(message);
                }
            } else {
                player.sendMessage(message);
            }

            return;
        }

        Location location = player.getLocation();
        List<Claim> claims = Claim.getClaimsAt(location);

        if (claims != null) {
            String message = langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_UNCLAIM").replace("%PLAYER%", player.getName());
            for (Claim claim : new ArrayList<>(claims)) {
                if (claim.getTeam() == team && claim.isInside(location)) {

                    if (team.getHome() != null && claim.isInside(LocationSerialization.deserializeLocation(team.getHome()))) {
                        team.setHome(null);
                    }

                    claim.remove();

                    if (playerFaction != null) {
                        playerFaction.sendMessage(message);
                        if (!(playerFaction.getOnlinePlayers().contains(player))) {
                            player.sendMessage(message);
                        }
                    } else {
                        player.sendMessage(message);
                    }

                    return;
                }
            }
        }

        player.sendMessage(langConfig.getString("ERROR.MUST_BE_IN_LAND_TO_UNCLAIM"));
    }
}

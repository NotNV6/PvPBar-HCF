package bar.pvp.hcf.teams.commands.officer;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.utils.LocationSerialization;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.entity.Player;

public class TeamSetHomeCommand extends TeamCommand {
    @Command(name = "f.sethome", aliases = {"team.sethome", "teams.sethome", "teams.sethq", "f.sethq", "team.sethq"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        Team team;
        PlayerTeam playerFaction = null;
        if (command.getArgs().length >= 1 && player.hasPermission("ekko.admin")) {
            String name = command.getArgs(0);
            Team team1 = PlayerTeam.getAnyByString(name);
            if (team1 != null) {
                if (team1 instanceof PlayerTeam) {
                    playerFaction = (PlayerTeam) team1;
                }
                team = team1;
                if (team instanceof SystemTeam) {
                    team.setHome(LocationSerialization.serializeLocation(player.getLocation()));
                    player.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_SET_HOME").replace("%PLAYER%", player.getName()));
                    return;
                }
            } else {
                player.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_FOUND").replace("%NAME%", name));
                return;
            }
        } else {
            team = profile.getFaction();
            playerFaction = profile.getFaction();

            if (team == null) {
                player.sendMessage(langConfig.getString("ERROR.NOT_IN_FACTION"));
                return;
            }

            if (!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()) && !player.hasPermission("ekko.admin")) {
                player.sendMessage(langConfig.getString("ERROR.NOT_OFFICER_OR_LEADER"));
                return;
            }
        }

        for (Claim claim : playerFaction.getClaims()) {
            if (claim.isInside(player.getLocation())) {
                playerFaction.setHome(LocationSerialization.serializeLocation(player.getLocation()));
                playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_SET_HOME").replace("%PLAYER%", player.getName()));
                return;
            }
        }

        player.sendMessage(langConfig.getString("ERROR.MUST_BE_IN_LAND_TO_SET_HOME"));
    }
}

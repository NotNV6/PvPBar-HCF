package bar.pvp.hcf.teams.commands.officer;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.claims.ClaimProfile;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeamClaimCommand extends TeamCommand {
    @Command(name = "f.claim", aliases = {"team.claim", "teams.claim", "teams.claimland", "f.claimland", "team.claimland"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());
        player.getInventory().remove(Team.getWand(main));

        Team team;
        if (command.getArgs().length >= 1 && player.hasPermission("hcteams.admin")) {
            String name = command.getArgs(0);
            Team team1 = PlayerTeam.getAnyByString(name);
            if (team1 != null) {
                team = team1;
                player.sendMessage(langConfig.getString("FACTION_OTHER.CLAIMING_FOR_OTHER").replace("%FACTION%", team.getName()));
            } else {
                player.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_FOUND").replace("%NAME%", name));
                return;
            }
        } else {
            team = Profile.getByUuid(player.getUniqueId()).getFaction();

            if (team == null) {
                player.sendMessage(langConfig.getString("ERROR.NOT_IN_FACTION"));
                return;
            }

            PlayerTeam playerFaction = (PlayerTeam) team;

            if (!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()) && !player.hasPermission("hcteams.system")) {
                player.sendMessage(langConfig.getString("ERROR.NOT_OFFICER_OR_LEADER"));
                return;
            }

        }

        if (!(profile.isViewingMap())) {
            if (!Claim.getNearbyClaimsAt(player.getLocation(), 64).isEmpty()) {
                Bukkit.dispatchCommand(player, "f map");
            }
        }

        player.getInventory().addItem(Team.getWand(main));
        player.sendMessage(langConfig.getString("FACTION_CLAIM.RECEIVED_WAND"));


        new ClaimProfile(player, team);
    }
}

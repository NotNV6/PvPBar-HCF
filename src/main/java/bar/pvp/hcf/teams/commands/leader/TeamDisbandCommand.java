package bar.pvp.hcf.teams.commands.leader;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.claims.Claim;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.events.player.PlayerDisbandTeamEvent;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.TeamMongo;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.mongodb.client.model.Filters.eq;

public class TeamDisbandCommand extends TeamCommand {
    @Command(name = "f.disband", aliases = {"team.disband", "teams.disband"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerTeam playerFaction;

        if (command.getArgs().length >= 1 && player.hasPermission("ekko.admin")) {
            String name = command.getArgs(0);
            Team team = PlayerTeam.getAnyByString(name);
            if (team != null) {
                if (team instanceof PlayerTeam) {
                    playerFaction = (PlayerTeam) team;
                } else {
                    player.sendMessage(langConfig.getString("SYSTEM_FACTION.DELETED").replace("%NAME%", team.getName()));

                    Team.getTeams().remove(team);

                    Set<Claim> claims = new HashSet<>(team.getClaims());
                    for (Claim claim : claims) {
                        claim.remove();
                    }


                    TeamMongo.getSystemTeams().deleteOne(eq("uuid", team.getUuid().toString()));
                    return;
                }
            } else {
                player.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_FOUND").replace("%NAME%", name));
                return;
            }
        } else {
            playerFaction = profile.getFaction();

            if (playerFaction == null) {
                player.sendMessage(langConfig.getString("ERROR.NOT_IN_FACTION"));
                return;
            }

            if (!playerFaction.getLeader().equals(player.getUniqueId())) {
                player.sendMessage(langConfig.getString("ERROR.NOT_LEADER"));
                return;
            }
        }

        for (UUID member : playerFaction.getAllPlayerUuids()) {
            Profile memberProfile = Profile.getByUuid(member);
            if (memberProfile != null && memberProfile.getFaction().equals(playerFaction)) {
                memberProfile.setFaction(null);
            }
        }

        main.getEconomy().addBalance(profile, playerFaction.getBalance());

        Bukkit.getPluginManager().callEvent(new PlayerDisbandTeamEvent(player, playerFaction));

        Bukkit.broadcastMessage(langConfig.getString("ANNOUNCEMENTS.FACTION_DISBANDED").replace("%PLAYER%", player.getName()).replace("%NAME%", playerFaction.getName()));
        Team.getTeams().remove(playerFaction);

        TeamMongo.getPlayerTeams().deleteOne(eq("uuid", playerFaction.getUuid().toString()));

        for (PlayerTeam ally : playerFaction.getAllies()) {
            ally.getAllies().remove(playerFaction);
        }

        Set<Claim> claims = new HashSet<>(playerFaction.getClaims());
        for (Claim claim : claims) {
            claim.remove();
        }
    }
}

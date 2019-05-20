package bar.pvp.hcf.teams.commands.leader;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamDemoteCommand extends TeamCommand {

    @SuppressWarnings("deprecation")
    @Command(name = "f.demote", aliases = {"team.demote", "teams.demote", "f.unmod", "teams.unmod", "team.unmod", "f.unofficer", "teams.unofficer", "team.unofficer", "team.uncaptain", "f.uncaptain", "team.uncaptain"}, inFactionOnly = true, isLeaderOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        if (command.getArgs().length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.DEMOTE"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerTeam playerFaction = profile.getFaction();

        UUID uuid;
        String name;
        Player toDemote = Bukkit.getPlayer(command.getArgs(0));

        if (toDemote == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(command.getArgs(0));
            if (offlinePlayer != null) {
                uuid = offlinePlayer.getUniqueId();
                name = offlinePlayer.getName();
            } else {
                player.sendMessage(langConfig.getString("ERROR.NOT_ONLINE").replace("%PLAYER%", command.getArgs(0)));
                return;
            }
        } else {
            uuid = toDemote.getUniqueId();
            name = toDemote.getName();
        }

        if (name.equalsIgnoreCase(player.getName()) && player.getUniqueId().equals(playerFaction.getLeader())) {
            player.sendMessage(langConfig.getString("ERROR.DEMOTE_YOURSELF"));
            return;
        }

        if (!playerFaction.getAllPlayerUuids().contains(uuid)) {
            player.sendMessage(langConfig.getString("ERROR.NOT_IN_YOUR_FACTION").replace("%PLAYER%", name));
            return;
        }
        
        if (!playerFaction.getOfficers().contains(uuid)) {
            player.sendMessage(langConfig.getString("ERROR.NOT_OFFICER").replace("%PLAYER%", name));
            return;
        }

        playerFaction.getOfficers().remove(uuid);
        playerFaction.getMembers().add(uuid);

        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_DEMOTED").replace("%PLAYER%", name).replace("%LEADER%", player.getName()));
    }
}

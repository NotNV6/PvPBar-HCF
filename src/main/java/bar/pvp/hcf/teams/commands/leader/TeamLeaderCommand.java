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

public class TeamLeaderCommand extends TeamCommand {

    @SuppressWarnings("deprecation")
    @Command(name = "f.leader", aliases = {"team.leader", "teams.leader", "f.owner", "teams.owner", "team.owner", "f.ownership", "teams.ownership", "team.ownership"}, inFactionOnly = true, isLeaderOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        if (command.getArgs().length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.LEADER"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerTeam playerFaction = profile.getFaction();

        UUID uuid;
        String name;
        Player toLeader = Bukkit.getPlayer(command.getArgs(0));

        if (toLeader == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(command.getArgs(0));
            if (offlinePlayer != null) {
                uuid = offlinePlayer.getUniqueId();
                name = offlinePlayer.getName();
            } else {
                player.sendMessage(langConfig.getString("ERROR.NOT_ONLINE").replace("%PLAYER%", command.getArgs(0)));
                return;
            }
        } else {
            uuid = toLeader.getUniqueId();
            name = toLeader.getName();
        }

        if (!playerFaction.getAllPlayerUuids().contains(uuid)) {
            player.sendMessage(langConfig.getString("ERROR.NOT_IN_YOUR_FACTION"));
            return;
        }

        if (player.getUniqueId().equals(playerFaction.getLeader()) && uuid.equals(playerFaction.getLeader())) {
            player.sendMessage(langConfig.getString("ERROR.ALREADY_LEADER"));
            return;
        }

        if (uuid.equals(playerFaction.getLeader()) && !uuid.equals(player.getUniqueId())) {
            player.sendMessage(langConfig.getString("ERROR.PLAYER_ALREADY_LEADER").replace("%PLAYER%", name));
            return;
        }

        playerFaction.getMembers().remove(uuid);
        playerFaction.getOfficers().remove(uuid);

        playerFaction.getOfficers().add(playerFaction.getLeader());
        playerFaction.setLeader(uuid);

        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_TRANSFER_LEADERSHIP").replace("%PLAYER%", name).replace("%LEADER%", player.getName()));
    }
}

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

public class TeamPromoteCommand extends TeamCommand {

    @SuppressWarnings("deprecation")
    @Command(name = "f.promote", aliases = {"team.promote", "teams.promote", "f.mod", "teams.mod", "team.mod", "f.officer", "teams.officer", "team.officer", "team.captain", "f.captain", "team.captain"}, inFactionOnly = true, isLeaderOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        if (command.getArgs().length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.PROMOTE"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerTeam playerFaction = profile.getFaction();


        UUID uuid;
        String name;
        Player toPromote = Bukkit.getPlayer(command.getArgs(0));

        if (toPromote == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(command.getArgs(0));
            if (offlinePlayer != null) {
                uuid = offlinePlayer.getUniqueId();
                name = offlinePlayer.getName();
            } else {
                player.sendMessage(langConfig.getString("ERROR.NOT_ONLINE").replace("%PLAYER%", command.getArgs(0)));
                return;
            }
        } else {
            uuid = toPromote.getUniqueId();
            name = toPromote.getName();
        }

        if (name.equalsIgnoreCase(player.getName()) && player.getUniqueId().equals(playerFaction.getLeader())) {
            player.sendMessage(langConfig.getString("ERROR.PROMOTE_YOURSELF"));
            return;
        }

        if (!playerFaction.getAllPlayerUuids().contains(uuid)) {
            player.sendMessage(langConfig.getString("ERROR.NOT_IN_YOUR_FACTION").replace("%PLAYER%", name));
            return;
        }

        if (playerFaction.getOfficers().contains(uuid)) {
            player.sendMessage(langConfig.getString("ERROR.ALREADY_OFFICER").replace("%PLAYER%", name));
            return;
        }

        playerFaction.getMembers().remove(uuid);
        playerFaction.getOfficers().add(uuid);

        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_PROMOTED").replace("%PLAYER%", name).replace("%LEADER%", player.getName()));
    }
}

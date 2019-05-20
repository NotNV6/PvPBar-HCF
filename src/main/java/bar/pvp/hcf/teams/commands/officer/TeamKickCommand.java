package bar.pvp.hcf.teams.commands.officer;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.events.player.PlayerLeaveTeamEvent;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamKickCommand extends TeamCommand {

    @SuppressWarnings("deprecation")
    @Command(name = "f.kick", aliases = {"team.kick", "teams.kick"}, inFactionOnly = true, isOfficerOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        if (command.getArgs().length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.KICK"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerTeam playerFaction = profile.getFaction();

        if (command.getArgs(0).equalsIgnoreCase(player.getName())) {
            player.sendMessage(langConfig.getString("ERROR.KICK_YOURSELF"));
            return;
        }

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

        if (!playerFaction.getAllPlayerUuids().contains(uuid)) {
            player.sendMessage(langConfig.getString("ERROR.NOT_IN_YOUR_FACTION").replace("%PLAYER%", name));
            return;
        }

        if (playerFaction.getLeader().equals(uuid)) {
            player.sendMessage(langConfig.getString("ERROR.CANT_KICK_LEADER"));
            return;
        }
        
        if (playerFaction.getOfficers().contains(uuid) && playerFaction.getOfficers().contains(player.getUniqueId())) {
            player.sendMessage(langConfig.getString("ERROR.CANT_KICK_OTHER_OFFICER"));
            return;
        }

        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_KICKED").replace("%KICKED_PLAYER%", name).replace("%PLAYER%", player.getName()));

        Profile kickProfile = Profile.getByUuid(uuid);
        kickProfile.setFaction(null);
        playerFaction.getOfficers().remove(uuid);
        playerFaction.getMembers().remove(uuid);

        Player kickPlayer = Bukkit.getPlayer(uuid);
        if (kickPlayer != null) {
            Bukkit.getPluginManager().callEvent(new PlayerLeaveTeamEvent(kickPlayer, playerFaction));
        }
    }
}

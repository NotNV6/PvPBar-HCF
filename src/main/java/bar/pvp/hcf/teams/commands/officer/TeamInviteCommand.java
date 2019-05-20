package bar.pvp.hcf.teams.commands.officer;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamInviteCommand extends TeamCommand {

    @SuppressWarnings("deprecation")
    @Command(name = "f.invite", aliases = {"team.invite", "teams.invite", "f.inv", "teams.inv", "team.inv"}, inFactionOnly = true, isOfficerOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        if (command.getArgs().length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.INVITE"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerTeam playerFaction = profile.getFaction();

        if (command.getArgs(0).equalsIgnoreCase(player.getName())) {
            player.sendMessage(langConfig.getString("ERROR.INVITE_YOURSELF"));
            return;
        }


        UUID uuid;
        String name;
        Player toInvite = Bukkit.getPlayer(command.getArgs(0));

        if (toInvite == null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(command.getArgs(0));
            if (offlinePlayer != null) {
                uuid = offlinePlayer.getUniqueId();
                name = offlinePlayer.getName();
            } else {
                player.sendMessage(langConfig.getString("ERROR.NOT_ONLINE").replace("%PLAYER%", command.getArgs(0)));
                return;
            }
        } else {
            uuid = toInvite.getUniqueId();
            name = toInvite.getName();
        }

        if (playerFaction.getAllPlayerUuids().contains(uuid)) {
            player.sendMessage(langConfig.getString("ERROR.INVITE_MEMBER").replace("%PLAYER%", name));
            return;
        }


        if (playerFaction.getInvitedPlayers().containsKey(uuid)) {
            player.sendMessage(langConfig.getString("ERROR.ALREADY_INVITED").replace("%PLAYER%", name));
            return;
        }

        if (toInvite != null) {
            FancyMessage message = new FancyMessage()
                    .text(langConfig.getString("FACTION_OTHER.INVITED_TO_JOIN").replace("%FACTION%", playerFaction.getName()))
                    .command("/f join " + playerFaction.getName().toLowerCase());
            message.send(toInvite);
        }

        playerFaction.getInvitedPlayers().put(uuid, player.getUniqueId());
        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_INVITED").replace("%PLAYER%", player.getName()).replace("%INVITED_PLAYER%", name));
    }
}

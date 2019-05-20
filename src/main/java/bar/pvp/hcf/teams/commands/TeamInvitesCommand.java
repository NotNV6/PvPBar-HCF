package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.UUID;

public class TeamInvitesCommand extends TeamCommand {
    @Command(name = "f.invites", aliases = {"team.invites", "teams.invites"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String invites = "";
        HashSet<PlayerTeam> factionsInvitedTo = new HashSet<>();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        PlayerTeam.getPlayerTeams().forEach(playerTeam -> {
            if (playerTeam.getInvitedPlayers().containsKey(player.getUniqueId()))
                factionsInvitedTo.add(playerTeam);

        });

        String splitter = langConfig.getString("FACTION_OTHER.INVITES.SPLITTER");

        if (factionsInvitedTo.isEmpty()) {
            invites = langConfig.getString("FACTION_OTHER.INVITES.PLAYER_INVITES_PLACEHOLDER");
        } else {
            for (PlayerTeam playerFaction : factionsInvitedTo) {
                invites = playerFaction.getName() + splitter;
            }
            invites = invites.substring(0, invites.lastIndexOf(splitter));
        }

        player.sendMessage(langConfig.getString("FACTION_OTHER.INVITES.PLAYER_INVITES").replace("%INVITES%", invites));

        if (profile.getFaction() != null) {
            PlayerTeam playerFaction = profile.getFaction();

            if (!playerFaction.getInvitedPlayers().isEmpty()) {
                String invitedPlayers = "";

                for (UUID invitedPlayer : playerFaction.getInvitedPlayers().keySet()) {
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(invitedPlayer);
                    if (offlinePlayer != null) {
                        invitedPlayers = invitedPlayers + offlinePlayer.getName() + splitter;
                    }
                }

                invitedPlayers = invitedPlayers.substring(0, invitedPlayers.lastIndexOf(splitter));
                player.sendMessage(langConfig.getString("FACTION_OTHER.INVITES.FACTION_INVITES").replace("%INVITES%", invitedPlayers));
            }
        }
    }
}

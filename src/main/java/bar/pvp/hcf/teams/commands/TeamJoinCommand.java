package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.events.player.PlayerJoinTeamEvent;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeamJoinCommand extends TeamCommand {
    @Command(name = "f.join", aliases = {"team.join", "teams.join", "f.accept", "teams.accept", "team.accept"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        if (command.getArgs().length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.JOIN"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getFaction() != null) {
            player.sendMessage(langConfig.getString("ERROR.ALREADY_IN_FACTION"));
            return;
        }

        String factionName = command.getArgs(0);
        Team team = Team.getByName(factionName);
        PlayerTeam playerFaction = null;

        if (team instanceof PlayerTeam) {
            playerFaction = (PlayerTeam) team;
        }

        if (team == null || (!(team instanceof PlayerTeam) || (!(playerFaction.getInvitedPlayers().containsKey(player.getUniqueId()))))) {
            playerFaction = PlayerTeam.getByPlayerName(factionName);

            if (playerFaction == null || !(playerFaction.getInvitedPlayers().containsKey(player.getUniqueId())) && !player.hasPermission("ekko.admin")) {
                player.sendMessage(langConfig.getString("ERROR.NOT_INVITED"));
                return;
            }
        }

        if (playerFaction.getAllPlayerUuids().size() >= mainConfig.getInteger("FACTION_GENERAL.MAX_PLAYERS")) {
            player.sendMessage(langConfig.getString("ERROR.MAX_PLAYERS").replace("%FACTION%", playerFaction.getName()));
            return;
        }

        player.sendMessage(langConfig.getString("FACTION_OTHER.JOINED").replace("%FACTION%", playerFaction.getName()));
        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_JOINED").replace("%PLAYER%", player.getName()));

        playerFaction.getInvitedPlayers().remove(player.getUniqueId());
        playerFaction.getMembers().add(player.getUniqueId());
        profile.setFaction(playerFaction);

        Bukkit.getPluginManager().callEvent(new PlayerJoinTeamEvent(player, playerFaction));
    }
}

package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.events.player.PlayerLeaveTeamEvent;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeamLeaveCommand extends TeamCommand {
    @Command(name = "f.leave", aliases = {"team.leave", "teams.leave", "f.quit", "teams.quit", "team.quit"}, inFactionOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        PlayerTeam playerFaction = profile.getFaction();

        if (playerFaction.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(langConfig.getString("ERROR.CANT_LEAVE_WHEN_LEADER"));
            return;
        }


        player.sendMessage(langConfig.getString("FACTION_OTHER.LEFT").replace("%FACTION%", playerFaction.getName()));

        playerFaction.getMembers().remove(player.getUniqueId());
        playerFaction.getOfficers().remove(player.getUniqueId());
        profile.setFaction(null);

        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_LEFT").replace("%PLAYER%", player.getName()));

        Bukkit.getPluginManager().callEvent(new PlayerLeaveTeamEvent(player, playerFaction));
    }
}

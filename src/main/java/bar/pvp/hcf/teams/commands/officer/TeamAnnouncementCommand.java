package bar.pvp.hcf.teams.commands.officer;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.entity.Player;

public class TeamAnnouncementCommand extends TeamCommand {
    @Command(name = "f.announcement", aliases = {"team.announcement", "teams.announcement", "f.anouncement", "team.anouncement", "teams.anouncement", "f.announce", "team.announce", "teams.announce", "f.description", "team.description", "teams.description"}, inFactionOnly = true, isOfficerOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.ANNOUNCEMENT"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerTeam playerFaction = profile.getFaction();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.getArgs().length; i++) {
            sb.append(command.getArgs()[i]).append(" ");
        }
        String message = sb.toString().trim();

        playerFaction.setAnnouncement(message);
        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_SET_ANNOUNCEMENT").replace("%PLAYER%", player.getName()).replace("%MESSAGE%", message).replace("%FACTION%", playerFaction.getName()));
    }
}

package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.entity.Player;

public class TeamMessageCommand extends TeamCommand {
    @Command(name = "f.msg", aliases = {"team.msg", "teams.msg", "f.message", "team.message", "teams.message"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.MESSAGE"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());

        PlayerTeam playerFaction = profile.getFaction();

        if (playerFaction == null) {
            player.sendMessage(langConfig.getString("ERROR.MUST_BE_IN_FACTION_FOR_CHAT_TYPE"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.getArgs().length; i++) { sb.append(command.getArgs()[i]).append(" "); }

        String message = sb.toString().trim();

        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_FACTION_CHAT").replace("%PLAYER%", player.getName()).replace("%MESSAGE%", message).replace("%FACTION%", playerFaction.getName()));
    }
}

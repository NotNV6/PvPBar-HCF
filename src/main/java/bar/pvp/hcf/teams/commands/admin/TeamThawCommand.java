package bar.pvp.hcf.teams.commands.admin;

import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.command.CommandSender;

public class TeamThawCommand extends TeamCommand {

    @Command(name = "f.thaw", aliases = {"team.thaw", "teams.thaw"}, permission = "hcteams.thaw", inGameOnly = false)
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();

        if (command.getArgs().length >= 1) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < command.getArgs().length; i++) {
                sb.append(command.getArgs()[i]).append(" ");
            }
            String name = sb.toString().trim().replace(" ", "");

            Team team = Team.getByName(name);
            PlayerTeam playerFaction = null;

            if (team instanceof PlayerTeam) {
                playerFaction = (PlayerTeam) team;
            }

            if (team == null || (!(team instanceof PlayerTeam))) {
                playerFaction = PlayerTeam.getByPlayerName(name);

                if (playerFaction == null) {
                    sender.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_FOUND").replace("%NAME%", name));
                    return;
                }
            }

            playerFaction.setFreezeInformation(null);
            sender.sendMessage(langConfig.getString("ADMIN.THAWED").replaceAll("%FACTION%", playerFaction.getName()));
        } else {
            sender.sendMessage(langConfig.getString("TOO_FEW_ARGS.THAW"));
        }
    }
}

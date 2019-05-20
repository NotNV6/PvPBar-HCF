package bar.pvp.hcf.teams.commands.admin;

import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;

public class TeamSetDtrCommand extends TeamCommand {

    @Command(name = "f.setdtr", aliases = {"team.setdtr", "teams.setdtr"}, permission = "hcteams.setdtr", inGameOnly = false)
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();

        if (command.getArgs().length == 2) {
            String name = command.getArgs()[0];

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

            playerFaction.setDeathsTillRaidable(BigDecimal.valueOf(Double.valueOf(command.getArgs()[1])));
            sender.sendMessage(langConfig.getString("ADMIN.SET_DTR").replaceAll("%FACTION%", playerFaction.getName()).replaceAll("%DTR%", playerFaction.getDeathsTillRaidable().doubleValue() + ""));
        } else {
            sender.sendMessage(langConfig.getString("TOO_FEW_ARGS.SET_DTR"));
        }
    }
}

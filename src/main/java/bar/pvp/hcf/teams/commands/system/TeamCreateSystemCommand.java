package bar.pvp.hcf.teams.commands.system;

import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.command.CommandSender;

public class TeamCreateSystemCommand extends TeamCommand {
    @Command(name = "f.createsystem", aliases = {"team.create", "teams.create"}, inGameOnly = false, permission = "hcteams.admin")
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getPlayer();

        String[] args = command.getArgs();

        if (args.length == 0) {
            sender.sendMessage(langConfig.getString("TOO_FEW_ARGS.CREATE_SYSTEM"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(command.getArgs()[i]).append(" ");
        }

        String name = sb.toString().trim();

        if (Team.getByName(name) != null) {
            sender.sendMessage(langConfig.getString("ERROR.NAME_TAKEN"));
            return;
        }

        new SystemTeam(name, null);
        sender.sendMessage(langConfig.getString("SYSTEM_FACTION.CREATED").replace("%NAME%", name));
    }
}

package bar.pvp.hcf.teams.commands.system;

import bar.pvp.hcf.utils.CC;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.command.CommandSender;

public class TeamColorCommand extends TeamCommand {
    @Command(name = "f.color", aliases = {"team.color", "teams.color"}, inGameOnly = false, permission = "hcteams.admin")
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getPlayer();

        String[] args = command.getArgs();

        if (args.length != 2) {
            sender.sendMessage(langConfig.getString("TOO_FEW_ARGS.SET_COLOR"));
            return;
        }

        String name = args[0];

        SystemTeam systemFaction = SystemTeam.getByName(name);

        if (systemFaction == null) {
            sender.sendMessage(langConfig.getString("ERROR.SYSTEM_FACTION_NOT_FOUND").replace("%NAME%", name));
            return;
        }

        CC color;
        try {
            color = CC.valueOf(args[1].toUpperCase());
        } catch (Exception exception) {
            sender.sendMessage(langConfig.getString("ERROR.INVALID_COLOR"));
            return;
        }

        systemFaction.setColor(color);
        sender.sendMessage(langConfig.getString("SYSTEM_FACTION.SET_COLOR").replace("%COLOR%", color + "").replace("%COLOR_NAME%", color.name()).replace("%FACTION%", systemFaction.getName()));
    }
}

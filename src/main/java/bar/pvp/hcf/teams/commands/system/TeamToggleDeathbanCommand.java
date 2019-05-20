package bar.pvp.hcf.teams.commands.system;

import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.command.CommandSender;

public class TeamToggleDeathbanCommand extends TeamCommand {
    @Command(name = "f.toggledeathban", aliases = {"team.toggledeathban", "teams.toggledeathban", "f.deathban", "team.deathban", "teams.deathban"}, inGameOnly = false, permission = "hcteams.admin")
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getPlayer();


        String[] args = command.getArgs();

        if (args.length == 0) {
            sender.sendMessage(langConfig.getString("TOO_FEW_ARGS.TOGGLE_DEATHBAN"));
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(command.getArgs()[i]).append(" ");
        }

        String name = sb.toString().trim();

        SystemTeam systemFaction = SystemTeam.getByName(name);

        if (systemFaction == null) {
            sender.sendMessage(langConfig.getString("ERROR.SYSTEM_FACTION_NOT_FOUND").replace("%NAME%", name));
            return;
        }


        systemFaction.setDeathban(!systemFaction.isDeathban());
        sender.sendMessage(langConfig.getString("SYSTEM_FACTION.TOGGLED_DEATHBAN").replace("%FACTION%", systemFaction.getName()).replace("%BOOLEAN%", systemFaction.isDeathban() + ""));
    }
}

package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.apache.commons.lang.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

public class TeamHelpCommand extends TeamCommand {

    @Command(name = "f", aliases = {"team", "teams"}, inGameOnly = false, description = "Base command for all team-related functions.")
    public void onCommand(CommandArgs command) {
        String[] args = command.getArgs();

        List<List<String>> help = new ArrayList<>();
        langConfig.getConfig().getConfigurationSection("FACTION_HELP").getKeys(false).forEach(string -> help.add(langConfig.getStringList("FACTION_HELP." + string)));

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("help")) {
                if (NumberUtils.isNumber(args[1])) {
                    int page = (int) Double.parseDouble(args[1]);
                    if (page > 0 && page <= help.size()) {
                        help.get(page-1).forEach(string -> command.getSender().sendMessage(string));
                        return;
                    }
                }
            }
        }

        help.get(0).forEach(string -> command.getSender().sendMessage(string));
    }
}

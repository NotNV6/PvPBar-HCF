package bar.pvp.hcf.teams.commands.admin;

import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.CommandSender;

public class TeamFreezeCommand extends TeamCommand {

    @Command(name = "team.freeze", aliases = {"f.freeze", "teams.freeze"}, permission = "hcteams.freeze", inGameOnly = false)
    public void onCommand(CommandArgs command) {
        CommandSender sender = command.getSender();

        if (command.getArgs().length == 2) {
            String name = command.getArgs(0);
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

            try {
                playerFaction.freeze(getTime(command.getArgs(1)));
                sender.sendMessage(langConfig.getString("ADMIN.FROZEN").replace("%FACTION%", playerFaction.getName()).replace("%TIME%", playerFaction.getFormattedFreezeDuration()));
            } catch (Exception e) {
                sender.sendMessage(langConfig.getString("ERROR.INVALID_TIME"));
            }
        } else {
            sender.sendMessage(langConfig.getString("TOO_FEW_ARGS.FREEZE"));
        }
    }

    private int getTime(String string) {
        int time = 0;
        if (string.contains("m")) {
            String timeStr = strip(string);
            if (NumberUtils.isNumber(timeStr)) {
                time = NumberUtils.toInt(timeStr) * 60;
            }
        } else if (string.contains("h")) {
            String timeStr = strip(string);
            if (NumberUtils.isNumber(timeStr)) {
                time = NumberUtils.toInt(timeStr) * 3600;
            }
        } else if (string.contains("d")) {
            String timeStr = strip(string);
            if (NumberUtils.isNumber(timeStr)) {
                time = NumberUtils.toInt(timeStr) * 86400;
            }
        } else if (string.contains("y")) {
            String timeStr = strip(string);
            if (NumberUtils.isNumber(timeStr)) {
                time = NumberUtils.toInt(timeStr) * 31536000;
            }
        } else {
            String timeStr = strip(string);
            if (NumberUtils.isNumber(timeStr)) {
                time = NumberUtils.toInt(timeStr);
            } else {
                throw new NumberFormatException("Invalid number");
            }
        }
        return time;
    }

    private String strip(String src) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < src.length(); i++) {
            char c = src.charAt(i);
            if (Character.isDigit(c)) {
                builder.append(c);
            }
        }
        return builder.toString();
    }
}

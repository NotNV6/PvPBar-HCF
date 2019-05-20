package bar.pvp.hcf.teams.commands.officer;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeamRenameCommand extends TeamCommand {
    @Command(name = "f.tag", aliases = {"team.tag", "teams.tag", "teams.rename", "f.rename", "team.rename"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        if (command.getArgs().length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.RENAME"));
            return;
        }

        Team team;
        if (command.getArgs().length >= 2 && player.hasPermission("ekko.admin")) {
            String name = command.getArgs(0);
            Team team1 = PlayerTeam.getAnyByString(name);
            if (team1 != null) {
                team = team1;
            } else {
                player.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_FOUND").replace("%NAME%", name));
                return;
            }
        } else {
            team = Profile.getByUuid(player.getUniqueId()).getFaction();

            if (team == null) {
                player.sendMessage(langConfig.getString("ERROR.NOT_IN_FACTION"));
                return;
            }

            PlayerTeam playerFaction = (PlayerTeam) team;

            if (!playerFaction.getLeader().equals(player.getUniqueId()) && !playerFaction.getOfficers().contains(player.getUniqueId()) && !player.hasPermission("ekko.system")) {
                player.sendMessage(langConfig.getString("ERROR.NOT_OFFICER_OR_LEADER"));
                return;
            }

        }

        StringBuilder sb = new StringBuilder();
        int start = 0;
        if (command.getArgs().length >= 2 && player.hasPermission("ekko.admin")) {
            start = 1;
        }
        for (int i = start; i < command.getArgs().length; i++) {
            sb.append(command.getArgs()[i]).append(" ");
        }

        String name;

        if (team instanceof PlayerTeam) {
            name = sb.toString().trim().replace(" ", "");
            if (name.length() < mainConfig.getInteger("FACTION_NAME.MIN_CHARACTERS")) {
                player.sendMessage(langConfig.getString("ERROR.TAG_TOO_SHORT"));
                return;
            }

            if (name.length() > mainConfig.getInteger("FACTION_NAME.MAX_CHARACTERS")) {
                player.sendMessage(langConfig.getString("ERROR.TAG_TOO_LONG"));
                return;
            }

            if (!(StringUtils.isAlphanumeric(name))) {
                player.sendMessage(langConfig.getString("ERROR.NOT_ALPHANUMERIC"));
                return;
            }

            for (String string : mainConfig.getStringList("FACTION_NAME.BLOCKED_NAMES")) {
                if (name.contains(string)) {
                    player.sendMessage(langConfig.getString("ERROR.BLOCKED_NAME"));
                    return;
                }
            }
        } else {
            name = sb.toString().trim();
        }

        Team otherTeam = Team.getByName(name);

        if (otherTeam != null) {
            if (otherTeam.equals(team)) {
                if (otherTeam.getName().equals(name)) { //allow case changing but not exact duplicates. e.g "Team" -> "factioN"
                    player.sendMessage(langConfig.getString("ERROR.NAME_TAKEN"));
                    return;
                }
            } else {
                player.sendMessage(langConfig.getString("ERROR.NAME_TAKEN"));
                return;
            }
        }

        if (team instanceof PlayerTeam) {
            Bukkit.broadcastMessage(langConfig.getString("ANNOUNCEMENTS.FACTION_RENAMED").replace("%OLD_NAME%", team.getName()).replace("%NEW_NAME%", name).replace("%PLAYER%", player.getName()));
        } else {
            player.sendMessage(langConfig.getString("SYSTEM_FACTION.RENAMED").replace("%OLD_NAME%", team.getName()).replace("%NEW_NAME%", name));
        }
        team.setName(name);
    }
}

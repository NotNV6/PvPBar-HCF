package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import mkremins.fanciful.FancyMessage;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.CommandSender;

import java.util.*;

public class TeamListCommand extends TeamCommand {
    @Command(name = "f.list", aliases = {"team.list", "teams.list"}, inGameOnly = false)
    public void onCommand(CommandArgs command) {
        String[] args = command.getArgs();
        CommandSender sender = command.getSender();

        final HashMap<PlayerTeam, Integer> factions = new HashMap<>();
        int page = 1;

        PlayerTeam.getPlayerTeams().forEach(playerTeam -> {
            if (playerTeam.getOnlinePlayers().size() > 0) {
                factions.put(playerTeam, playerTeam.getOnlinePlayers().size());
            }
        });

        List<PlayerTeam> sortedList = new ArrayList<>(factions.keySet());
        Collections.sort(sortedList, new Comparator<PlayerTeam>() {
            @Override
            public int compare(PlayerTeam firstFaction, PlayerTeam secondFaction) {
                return factions.get(firstFaction).compareTo(factions.get(secondFaction));
            }
        });

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("list")) {
                if (NumberUtils.isNumber(args[1])) {
                    page = (int) Double.parseDouble(args[1]);
                }
            }
        }

        if (sortedList.isEmpty()) {
            sender.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_TO_LIST"));
            return;
        }

        int listSize = Math.round(sortedList.size() / 10);
        if (listSize == 0) {
            listSize = 1;
        }

        if (page > listSize) {
            page = listSize;
        }

        for (String msg : langConfig.getStringList("FACTION_LIST")) {
            if (msg.contains("%FACTION%")) {
                for (int i = page * 10 - 10; i < page * 10; i++) {
                    if (sortedList.size() > i) {
                        PlayerTeam playerFaction = sortedList.get(i);
                        FancyMessage fancyMessage = new FancyMessage(msg.replace("%FACTION%", playerFaction.getName()).replace("%DTR%", playerFaction.getDeathsTillRaidable() + "").replace("%MAX_DTR%", playerFaction.getMaxDeathsTillRaidable() + "").replace("%BALANCE%", playerFaction.getBalance() + "").replace("%ONLINE_COUNT%", playerFaction.getOnlinePlayers().size() + "").replace("%MAX_COUNT%", playerFaction.getMembers().size() + 1 + playerFaction.getOfficers().size() + "").replace("%POSITION%", (i + 1) + "")).command("/f show " + playerFaction.getName().toLowerCase());
                        fancyMessage.send(sender);
                    }
                }
            } else {
                sender.sendMessage(msg.replace("%PAGE%", page + "").replace("%TOTAL_PAGES%", listSize + ""));
            }
        }
    }
}

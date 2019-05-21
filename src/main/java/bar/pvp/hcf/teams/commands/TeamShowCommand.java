package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.utils.CC;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.teams.type.SystemTeam;
import bar.pvp.hcf.utils.LocationSerialization;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class TeamShowCommand extends TeamCommand {
    @Command(name = "f.show", aliases = {"team.show", "teams.show", "f.i", "team.i", "teams.i", "f.info", "team.info", "teams.info", "f.who", "team.who", "teams.who"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (command.getArgs().length == 0) {
            PlayerTeam playerFaction = profile.getFaction();

            if (playerFaction == null) {
                player.sendMessage(langConfig.getString("ERROR.NOT_IN_FACTION"));
                return;
            }

            sendFactionInformation(player, playerFaction);
            return;
        }


        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.getArgs().length; i++) sb.append(command.getArgs()[i]).append(" ");

        String name = sb.toString().trim().replace(" ", "");

        Set<Team> matchedTeams = Team.getAllByString(name);

        if (matchedTeams.isEmpty()) {
            player.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_FOUND").replace("%NAME%", name));
            return;
        }

        matchedTeams.forEach(team -> sendFactionInformation(player, team));

    }

    private void sendFactionInformation(Player player, Team team) {
        List<String> toSend = new ArrayList<>();
        if (team instanceof PlayerTeam) {
            PlayerTeam playerFaction = (PlayerTeam) team;

            final String ROOT = "FACTION_SHOW.PLAYER_FACTION.";
            final String ROOT_SETTINGS = ROOT + "SETTINGS.";
            ChatColor offlineColor = ChatColor.valueOf(langConfig.getString(ROOT_SETTINGS + "OFFLINE_COLOR").toUpperCase());
            ChatColor onlineColor = ChatColor.valueOf(langConfig.getString(ROOT_SETTINGS + "ONLINE_COLOR").toUpperCase());
            ChatColor raidableColor =  ChatColor.valueOf(langConfig.getString(ROOT_SETTINGS + "DTR_COLOR.RAIDABLE").toUpperCase());
            ChatColor notRaidableColor =  ChatColor.valueOf(langConfig.getString(ROOT_SETTINGS + "DTR_COLOR.NOT_RAIDABLE").toUpperCase());
            String killFormat = langConfig.getString(ROOT_SETTINGS + "SHOW_KILLS.FORMAT");
            String splitNamesFormat = langConfig.getString(ROOT_SETTINGS + "SPLIT_NAMES.FORMAT");
            boolean splitNamesEnabled = langConfig.getBoolean(ROOT_SETTINGS + "SPLIT_NAMES.ENABLED");
            boolean killFormatEnabled = langConfig.getBoolean(ROOT_SETTINGS + "SHOW_KILLS.ENABLED");


            for (String string : langConfig.getStringList(ROOT + "MESSAGE")) {
                string = string.replace("%FACTION%", team.getName());
                string = string.replace("%ONLINE_COUNT%", playerFaction.getOnlinePlayers().size() + "");
                string = string.replace("%MAX_COUNT%", playerFaction.getAllPlayerUuids().size() + "");

                if (string.contains("%HOME%")) {
                    if (playerFaction.getHome() == null) {
                        string = string.replace("%HOME%", langConfig.getString(ROOT_SETTINGS + "HOME_PLACEHOLDER"));
                    } else {
                        Location homeLocation = LocationSerialization.deserializeLocation(playerFaction.getHome());
                        string = string.replace("%HOME%", homeLocation.getBlockX() + ", " + homeLocation.getBlockZ());
                    }
                }

                if (string.contains("%LEADER%")) {

                    String leaderString;
                    UUID leader = playerFaction.getLeader();
                    OfflinePlayer leaderPlayer = Bukkit.getOfflinePlayer(leader);

                    if (leaderPlayer == null)
                        continue;

                    if (Bukkit.getPlayer(leader) == null)
                        leaderString = offlineColor + leaderPlayer.getName();
                     else
                        leaderString = onlineColor + leaderPlayer.getName();

                    if(leaderPlayer.isOnline()) {
                        if (killFormatEnabled)
                            leaderString = leaderString + killFormat.replace("%KILLS%", leaderPlayer.getPlayer().getStatistic(Statistic.PLAYER_KILLS) + "");
                    }

                    string = string.replace("%LEADER%", leaderString);
                }

                if (string.contains("%OFFICERS%")) {
                    String officerString = "";

                    if (playerFaction.getOfficers().isEmpty()) {
                        continue;
                    }

                    for (UUID uuid : playerFaction.getOfficers()) {
                        OfflinePlayer officer = Bukkit.getOfflinePlayer(uuid);

                        if (officer == null) continue;


                        if (Bukkit.getPlayer(uuid) == null)
                            officerString = officerString + offlineColor + officer.getName();
                         else
                            officerString = officerString + onlineColor + officer.getName();

                        if(officer.isOnline()) {
                            if (killFormatEnabled)
                                officerString = officerString + killFormat.replace("%KILLS%", officer.getPlayer().getStatistic(Statistic.PLAYER_KILLS) + "");
                        }
                        if (splitNamesEnabled) officerString = officerString + splitNamesFormat;

                    }

                    string = string.replace("%OFFICERS%", officerString);
                }

                if (string.contains("%MEMBERS%")) {
                    String memberString = "";

                    if (playerFaction.getMembers().isEmpty()) continue;


                    for (UUID uuid : playerFaction.getMembers()) {
                        OfflinePlayer member = Bukkit.getOfflinePlayer(uuid);

                        if (member == null) continue;

                        if (Bukkit.getPlayer(uuid) == null)
                            memberString = memberString + offlineColor + member.getName();
                        else
                            memberString = memberString + onlineColor + member.getName();


                        if(member.isOnline()) {
                            if (killFormatEnabled)
                                memberString = memberString + killFormat.replace("%KILLS%", member.getPlayer().getStatistic(Statistic.PLAYER_KILLS) + "");
                        }

                        if (splitNamesEnabled) memberString = memberString + splitNamesFormat;

                    }

                    string = string.replace("%MEMBERS%", memberString);
                }

                if (string.contains("%ALLIES%")) {

                    if (playerFaction.getAllies().isEmpty()) {
                        continue;
                    }

                    CC allyColor = CC.PINK;
                    String allies = "";
                    for (PlayerTeam ally : playerFaction.getAllies()) {
                        allies = allies + allyColor + ally.getName();

                        if (splitNamesEnabled) allies = allies + splitNamesFormat;

                    }

                    string = string.replace("%ALLIES%", allies);
                }

                if (string.contains("%DTR%")) {
                    if (playerFaction.isRaidable()) {
                        string = string.replace("%DTR%", raidableColor + "" + playerFaction.getDeathsTillRaidable());
                    } else {
                        string = string.replace("%DTR%", notRaidableColor + "" + playerFaction.getDeathsTillRaidable());
                    }
                }

                if (string.contains("%DTR_SYMBOL%")) {
                    if (playerFaction.getDeathsTillRaidable().equals(playerFaction.getMaxDeathsTillRaidable())) {
                        string = string.replace("%DTR_SYMBOL%", langConfig.getString(ROOT_SETTINGS + "DTR_SYMBOL.FULL"));
                    } else {
                        if (playerFaction.isFrozen()) {
                            string = string.replace("%DTR_SYMBOL%", langConfig.getString(ROOT_SETTINGS + "DTR_SYMBOL.FROZEN"));
                        } else {
                            string = string.replace("%DTR_SYMBOL%", langConfig.getString(ROOT_SETTINGS + "DTR_SYMBOL.REGENERATING"));
                        }
                    }
                }

                string = string.replace("%BALANCE%", playerFaction.getBalance() + "");
                string = string.replace("%MAX_DTR%", playerFaction.getMaxDeathsTillRaidable() + "");

                if (string.contains("%ANNOUNCEMENT%")) {
                    if (playerFaction.getAnnouncement() == null || !playerFaction.getOnlinePlayers().contains(player)) {
                        continue;
                    }
                    string = string.replace("%ANNOUNCEMENT%", playerFaction.getAnnouncement());
                }

                if (string.contains("%REGEN_TIME%")) {

                    if (!(playerFaction.isFrozen())) continue;


                    string = string.replace("%REGEN_TIME%", playerFaction.getFormattedFreezeDuration());
                }

                if (splitNamesEnabled && string.contains(splitNamesFormat)) {
                    string = string.substring(0, string.lastIndexOf(splitNamesFormat));
                }

                toSend.add(string);
            }
        } else {
            SystemTeam systemFaction = (SystemTeam) team;
            String ROOT = "FACTION_SHOW.SYSTEM_FACTION.";
            langConfig.getStringList(ROOT + "MESSAGE").forEach(string -> {
                string = string.replace("%FACTION%", team.getName());
                string = string.replace("%COLOR%", systemFaction.getColor() + "");
                string = string.replace("%DEATHBAN%", systemFaction.isDeathban() ? "&e(&cDeathban&e)" : "&e(&aNon-Deathban&e)");
                toSend.add(string);
            });
        }

        toSend.forEach(string -> player.sendMessage(CC.translate(string)));
    }
}

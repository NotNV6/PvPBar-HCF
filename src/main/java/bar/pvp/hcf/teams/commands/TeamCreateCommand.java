package bar.pvp.hcf.teams.commands;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.events.player.PlayerCreateTeamEvent;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeamCreateCommand extends TeamCommand {
    @Command(name = "f.create", aliases = {"team.create", "teams.create"})
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();
        String[] args = command.getArgs();

        if (args.length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.CREATE"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());

        if (profile.getFaction() != null) {
            player.sendMessage(langConfig.getString("ERROR.ALREADY_IN_FACTION"));
            return;
        }


        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            sb.append(command.getArgs()[i]).append(" ");
        }

        String name = sb.toString().trim().replace(" ", "");

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
            if (name.toUpperCase().contains(string.toUpperCase())) {
                player.sendMessage(langConfig.getString("ERROR.BLOCKED_NAME"));
                return;
            }
        }

        if (Team.getByName(name) != null) {
            player.sendMessage(langConfig.getString("ERROR.NAME_TAKEN"));
            return;
        }

        PlayerTeam playerFaction = new PlayerTeam(name, player.getUniqueId(), null);
        profile.setFaction(playerFaction);

        Bukkit.broadcastMessage(langConfig.getString("ANNOUNCEMENTS.FACTION_CREATED").replace("%PLAYER%", player.getName()).replace("%NAME%", name));

        Bukkit.getPluginManager().callEvent(new PlayerCreateTeamEvent(player, playerFaction));
    }
}

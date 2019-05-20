package bar.pvp.hcf.teams.commands.officer;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.events.TeamAllyTeamEvent;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeamAllyCommand extends TeamCommand {

    public TeamAllyCommand(){
        if (!(mainConfig.getBoolean("FACTION_GENERAL.ALLIES.ENABLED"))) {
            main.getFramework().unregisterCommands(this);
        }
    }

    @Command(name = "f.ally", aliases = {"team.ally", "teams.ally", "f.alliance", "teams.alliance", "team.alliance"}, inFactionOnly = true, isOfficerOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        if (command.getArgs().length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.ALLY"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerTeam playerFaction = profile.getFaction();

        String factionName = command.getArgs(0);
        Team team = Team.getByName(factionName);
        PlayerTeam allyFaction = null;

        if (team instanceof PlayerTeam) {
            allyFaction = (PlayerTeam) team;
        }

        if (team == null || (!(team instanceof PlayerTeam))) {
            allyFaction = PlayerTeam.getByPlayerName(factionName);

            if (allyFaction == null) {
                player.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_FOUND").replace("%NAME%", factionName));
                return;
            }
        }

        if (playerFaction.getName().equals(allyFaction.getName())) {
            player.sendMessage(langConfig.getString("ERROR.CANT_ALLY_YOURSELF"));
            return;
        }

        if (allyFaction.getAllies().contains(playerFaction)) {
            player.sendMessage(langConfig.getString("ERROR.ALREADY_HAVE_RELATION").replace("%FACTION%", allyFaction.getName()));
            return;
        }

        if (playerFaction.getRequestedAllies().contains(allyFaction.getUuid())) {
            player.sendMessage(langConfig.getString("ERROR.ALREADY_REQUESTED").replace("%FACTION%", allyFaction.getName()));
            return;
        }

        if (allyFaction.getAllies().size() >= mainConfig.getInteger("FACTION_GENERAL.ALLIES.MAX_ALLIES")) {
            player.sendMessage(langConfig.getString("ERROR.MAX_ALLIES").replace("%FACTION%", allyFaction.getName()));
            return;
        }

        if (allyFaction.getRequestedAllies().contains(playerFaction.getUuid())) {
            allyFaction.getRequestedAllies().remove(playerFaction.getUuid());

            allyFaction.getAllies().add(playerFaction);
            playerFaction.getAllies().add(allyFaction);

            allyFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION_ALLIED").replace("%FACTION%", playerFaction.getName()));
            playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION_ALLIED").replace("%FACTION%", allyFaction.getName()));

            Bukkit.getPluginManager().callEvent(new TeamAllyTeamEvent(new PlayerTeam[]{playerFaction, allyFaction}));
        } else {
            playerFaction.getRequestedAllies().add(allyFaction.getUuid());
            playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION.PLAYER_SEND_ALLY_REQUEST").replace("%PLAYER%", player.getName()).replace("%FACTION%", allyFaction.getName()));
            for (Player allyPlayer : allyFaction.getOnlinePlayers()) {
                if (allyPlayer.getUniqueId().equals(allyFaction.getLeader())) {
                    new FancyMessage()
                            .text(langConfig.getString("ANNOUNCEMENTS.FACTION_RECEIVE_ALLY_REQUEST").replace("%FACTION%", playerFaction.getName()))
                            .command("/f ally " + playerFaction.getName())
                            .send(allyPlayer);
                } else {
                    allyPlayer.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION_RECEIVE_ALLY_REQUEST").replace("%FACTION%", playerFaction.getName()));
                }
            }
        }
    }
}

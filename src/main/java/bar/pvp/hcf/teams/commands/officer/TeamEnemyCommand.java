package bar.pvp.hcf.teams.commands.officer;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.commands.TeamCommand;
import bar.pvp.hcf.teams.events.TeamEnemyTeamEvent;
import bar.pvp.hcf.teams.type.PlayerTeam;
import bar.pvp.hcf.utils.command.Command;
import bar.pvp.hcf.utils.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TeamEnemyCommand extends TeamCommand {

    public TeamEnemyCommand(){
        if (!(mainConfig.getBoolean("FACTION_GENERAL.ALLIES.ENABLED"))) {
            main.getFramework().unregisterCommands(this);
        }
    }

    @Command(name = "f.enemy", aliases = {"team.enemy", "teams.enemy", "f.neutral", "teams.neutral", "team.neutral"}, inFactionOnly = true, isOfficerOnly = true)
    public void onCommand(CommandArgs command) {
        Player player = command.getPlayer();

        if (command.getArgs().length == 0) {
            player.sendMessage(langConfig.getString("TOO_FEW_ARGS.ENEMY"));
            return;
        }

        Profile profile = Profile.getByUuid(player.getUniqueId());
        PlayerTeam playerFaction = profile.getFaction();

        String factionName = command.getArgs(0);
        Team team = Team.getByName(factionName);
        PlayerTeam enemyFaction = null;

        if (team instanceof PlayerTeam) {
            enemyFaction = (PlayerTeam) team;
        }

        if (team == null || (!(team instanceof PlayerTeam))) {
            enemyFaction = PlayerTeam.getByPlayerName(factionName);

            if (enemyFaction == null) {
                player.sendMessage(langConfig.getString("ERROR.NO_FACTIONS_FOUND").replace("%NAME%", factionName));
                return;
            }
        }

        if (playerFaction.getName().equals(enemyFaction.getName())) {
            player.sendMessage(langConfig.getString("ERROR.CANT_ENEMY_YOURSELF"));
            return;
        }

        if (!enemyFaction.getAllies().contains(playerFaction)) {
            player.sendMessage(langConfig.getString("ERROR.ALREADY_HAVE_RELATION").replace("%FACTION%", enemyFaction.getName()));
            return;
        }

        enemyFaction.getAllies().remove(playerFaction);
        playerFaction.getAllies().remove(enemyFaction);

        playerFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION_NOW_ENEMY").replace("%FACTION%", enemyFaction.getName()));
        enemyFaction.sendMessage(langConfig.getString("ANNOUNCEMENTS.FACTION_NOW_ENEMY").replace("%FACTION%", playerFaction.getName()));

        Bukkit.getPluginManager().callEvent(new TeamEnemyTeamEvent(new PlayerTeam[]{playerFaction, enemyFaction}));
    }
}

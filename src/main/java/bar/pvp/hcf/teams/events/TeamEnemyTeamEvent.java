package bar.pvp.hcf.teams.events;

import bar.pvp.hcf.teams.type.PlayerTeam;
import lombok.Getter;

@Getter
public class TeamEnemyTeamEvent extends TeamEvent {

    private PlayerTeam[] factions;

    public TeamEnemyTeamEvent(PlayerTeam[] factions) {
        this.factions = factions;
    }

}

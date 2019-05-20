package bar.pvp.hcf.teams.events;

import bar.pvp.hcf.teams.type.PlayerTeam;
import lombok.Getter;

@Getter
public class TeamAllyTeamEvent extends TeamEvent {

    private PlayerTeam[] factions;

    public TeamAllyTeamEvent(PlayerTeam[] factions) {
        this.factions = factions;
    }

}

package bar.pvp.hcf.teams.events.player;

import bar.pvp.hcf.teams.events.TeamEvent;
import bar.pvp.hcf.teams.type.PlayerTeam;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PlayerCreateTeamEvent extends TeamEvent {

    private PlayerTeam faction;
    private Player player;

    public PlayerCreateTeamEvent(Player player, PlayerTeam faction) {
        this.player = player;
        this.faction = faction;
    }

}

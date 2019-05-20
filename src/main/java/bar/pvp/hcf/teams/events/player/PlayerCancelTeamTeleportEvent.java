package bar.pvp.hcf.teams.events.player;

import bar.pvp.hcf.profiles.teleport.ProfileTeleportType;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.events.TeamEvent;
import lombok.Getter;
import org.bukkit.entity.Player;

@Getter
public class PlayerCancelTeamTeleportEvent extends TeamEvent {

    private Team team;
    private Player player;
    private ProfileTeleportType teleportType;

    public PlayerCancelTeamTeleportEvent(Player player, Team team, ProfileTeleportType teleportType) {
        this.player = player;
        this.team = team;
        this.teleportType = teleportType;
    }


}

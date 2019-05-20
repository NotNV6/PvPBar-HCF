package bar.pvp.hcf.teams.events.player;

import bar.pvp.hcf.profiles.teleport.ProfileTeleportType;
import bar.pvp.hcf.teams.Team;
import bar.pvp.hcf.teams.events.TeamEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@Getter
public class PlayerInitiateTeamTeleportEvent extends TeamEvent {

    private Team team;
    private Player player;
    private ProfileTeleportType teleportType;
    private Location initialLocation;
    private long init;
    @Setter private double time;
    @Setter private Location location;
    @Setter private boolean cancelled;

    public PlayerInitiateTeamTeleportEvent(Player player, Team team, ProfileTeleportType teleportType, double time, Location location, Location initialLocation) {
        this.player = player;
        this.team = team;
        this.teleportType = teleportType;
        this.time = time;
        this.init = System.currentTimeMillis();
        this.location = location;
        this.initialLocation = initialLocation;
    }


}

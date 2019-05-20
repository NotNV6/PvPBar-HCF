package bar.pvp.hcf.teams.claims;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.teams.Team;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.Arrays;

@Getter @Setter public class ClaimProfile {

    private Player player;
    private Profile profile;
    private Team team;
    private boolean resetClicked;
    private ClaimPillar[] pillars;

    public ClaimProfile(Player player, Team team) {
        this.player = player;
        this.team = team;

        pillars = new ClaimPillar[2];

        profile = Profile.getByUuid(player.getUniqueId());
        profile.setClaimProfile(this);
    }

    public void removePillars() {
        Arrays.stream(pillars).forEach(pillar -> {
            if (pillar != null) pillar.remove();
        });
        pillars = new ClaimPillar[2];
    }
}

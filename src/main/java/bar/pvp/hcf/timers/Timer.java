package bar.pvp.hcf.timers;

import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.utils.Config;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Timer {

    private float duration;
    private String name, displayName;

    private boolean trailing;

    private Config messageConfig = HCTeams.getInstance().getLangConfig();
    private Config scoreboardConfig = HCTeams.getInstance().getScoreboardConfig();

    @Getter
    private static List<Timer> timers = new ArrayList<>();

    public Timer(String name, float duration) {
        this.name = name;
        this.duration = duration;
        this.trailing = false;

        timers.add(this);
    }

    public void start() {
        new TimerTask().runTaskTimer(HCTeams.getInstance(), 2, 2);
    }

    public static Timer getByName(String name) { return timers.stream().filter(timer -> timer.getName().equalsIgnoreCase(name)).findFirst().orElse(null); }

}

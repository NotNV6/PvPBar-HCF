package bar.pvp.hcf.timers.type;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.timers.Timer;
import bar.pvp.hcf.timers.TimerTask;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class PlayerTimer extends Timer {

    @Getter @Setter
    private Map<Player, Float> cooldowns;


    private static List<PlayerTimer> playerTimers = new ArrayList<>();

    public PlayerTimer(String name, float duration) {
        super(name, duration);
        this.cooldowns = new HashMap<>();

        playerTimers.add(this);
    }

    public abstract void onExpire(Player player);

    public void start(Player player) {
        cooldowns.put(player, getDuration());
        Profile.getProfile(player).getPlayerTimers().add(this);
        start();
    }

    public void cancel(Player player, PlayerTimer timer) {
        timer.getCooldowns().remove(player);
        Profile.getProfile(player).getPlayerTimers().remove(timer);
        TimerTask task = TimerTask.getTimers().get(player, timer);
        task.cancel();
    }
}

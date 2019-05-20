package bar.pvp.hcf.timers;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.timers.type.PlayerTimer;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


public class TimerTask extends BukkitRunnable {

    @Getter
    private static Table<Player, Timer, TimerTask> timers = HashBasedTable.create();

    @Override
    public void run() {
        Timer.getTimers().forEach(timer -> {
            if(timer instanceof PlayerTimer) {
                PlayerTimer playerTimer = (PlayerTimer) timer;
                if(playerTimer.getCooldowns().isEmpty() || playerTimer.getCooldowns().keySet().isEmpty()) return;
                for(Player player : playerTimer.getCooldowns().keySet()) {
                    if(!timers.contains(player, timer)) timers.put(player, timer, this);
                    if(playerTimer.getCooldowns().get(player) <= 0) {
                        playerTimer.onExpire(player);
                        playerTimer.getCooldowns().remove(player);
                        Profile.getProfile(player).getPlayerTimers().remove(timer);

                        timers.remove(player, timer);

                        this.cancel();
                    } else {
                        float f = 100F / timers.size();
                        playerTimer.getCooldowns().put(player, playerTimer.getCooldowns().get(player) - f);
                    }

                }
            }
        });
    }
}

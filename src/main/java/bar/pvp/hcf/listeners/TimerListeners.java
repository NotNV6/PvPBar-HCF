package bar.pvp.hcf.listeners;

import bar.pvp.hcf.profiles.Profile;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TimerListeners implements Listener {

    public void onQuit(PlayerQuitEvent event) {
        if(!Profile.getProfile(event.getPlayer()).getPlayerTimers().isEmpty()) Profile.getProfile(event.getPlayer()).getPlayerTimers().forEach(timer -> timer.cancel(event.getPlayer(), timer));
    }
}

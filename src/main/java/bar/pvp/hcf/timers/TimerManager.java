package bar.pvp.hcf.timers;

import bar.pvp.hcf.timers.impl.CombatTimer;
import bar.pvp.hcf.timers.impl.HomeTimer;
import bar.pvp.hcf.timers.impl.EnderpearlTimer;
import lombok.Getter;

public class TimerManager {

    @Getter
    private static EnderpearlTimer enderpearlTimer;

    @Getter
    private static CombatTimer combatTimer;

    @Getter
    private static HomeTimer homeTimer;

    public TimerManager() {
        combatTimer = new CombatTimer();
        enderpearlTimer = new EnderpearlTimer();
        homeTimer = new HomeTimer();
    }
}

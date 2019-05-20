package bar.pvp.hcf.scoreboard.layout;

import org.bukkit.entity.Player;

public interface LayoutProvider {

    BoardLayout getLayout(Player player);
}

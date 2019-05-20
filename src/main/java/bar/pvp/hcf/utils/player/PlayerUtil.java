package bar.pvp.hcf.utils.player;

import org.bukkit.entity.Player;

import java.util.*;

public class PlayerUtil {

    public static List<Player> players = new ArrayList<>();

    public static Collection<Player> getOnlinePlayers() {
        return players;
    }

    public static Set<String> getConvertedUuidSet(Set<UUID> uuids) {
        Set<String> toReturn = new HashSet<>();
        uuids.forEach(uuid -> toReturn.add(uuid.toString()));
        return toReturn;
    }
}

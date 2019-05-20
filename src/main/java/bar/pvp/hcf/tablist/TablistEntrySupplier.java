package bar.pvp.hcf.tablist;

import org.bukkit.entity.Player;

import com.google.common.collect.Table;

public interface TablistEntrySupplier {

    Table<Integer, Integer, String> getEntries(Player player);

    String getHeader(Player player);

    String getFooter(Player player);
}
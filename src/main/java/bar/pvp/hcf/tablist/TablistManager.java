package bar.pvp.hcf.tablist;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import bar.pvp.hcf.utils.player.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class TablistManager implements Listener {

    public static TablistManager INSTANCE;

    @Getter
    private final JavaPlugin plugin;

    private final Map<UUID, Tablist> tablists;

    @Setter @NonNull @Getter
    private TablistEntrySupplier supplier;

    private int updateTaskId = -1;

    public TablistManager(@NonNull JavaPlugin plugin, @NonNull TablistEntrySupplier supplier, long updateTime) {
        boolean startUpdater = true;
        
        TablistManager.INSTANCE = this;
        this.tablists = new ConcurrentHashMap<>();
        this.supplier = supplier;
        this.plugin = plugin;
        if (startUpdater) updateTaskId = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new TablistUpdateTask(), updateTime / 50, updateTime / 50).getTaskId();

        Bukkit.getPluginManager().registerEvents(this, plugin);
        PlayerUtil.getOnlinePlayers().forEach(player -> getTablist(player, true));
    }

    @Deprecated
    public Tablist getTablist(Player player) {
        return getTablist(player, false);
    }

    @Deprecated
    public Tablist getTablist(Player player, boolean create) {
        UUID uniqueId = player.getUniqueId();
        Tablist tablist = tablists.get(uniqueId);
        if (tablist == null && create) {
            tablists.put(uniqueId, tablist = new Tablist(player));
        }
        return tablist;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        getTablist(player, true);
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        if (event.getPlugin() == plugin) {
            tablists.forEach((id, tablist) -> {
                tablist.hideFakePlayers().clear();
            });
            tablists.clear();
            HandlerList.unregisterAll(this);
            if (updateTaskId != -1) {
                Bukkit.getScheduler().cancelTask(updateTaskId);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uniqueId = player.getUniqueId();
        Tablist tablist;
        if ((tablist = tablists.remove(uniqueId)) != null)
            tablist.hideFakePlayers().clear();

    }
}

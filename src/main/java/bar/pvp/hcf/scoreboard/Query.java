package bar.pvp.hcf.scoreboard;

import bar.pvp.hcf.scoreboard.layout.LayoutProvider;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


@Getter
public class Query {

    private JavaPlugin plugin;
    private long ticks;
    @Setter private LayoutProvider layout;
    @Getter private static Query instance;


    public Query(JavaPlugin plugin, LayoutProvider provider, long ticks) {
        this.plugin = plugin;
        this.ticks = ticks;
        this.layout = provider;
        instance = this;

        QueryTask queryTask = new QueryTask();
        Bukkit.getPluginManager().registerEvents(queryTask, plugin);
        queryTask.runTaskTimerAsynchronously(plugin, 20L, ticks);
    }

}

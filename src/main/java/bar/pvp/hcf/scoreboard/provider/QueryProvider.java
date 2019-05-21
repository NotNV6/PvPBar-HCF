package bar.pvp.hcf.scoreboard.provider;

import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.classes.classes.BardClass;
import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.scoreboard.layout.BoardLayout;
import bar.pvp.hcf.scoreboard.layout.LayoutProvider;
import bar.pvp.hcf.utils.Config;
import bar.pvp.hcf.utils.TimeUtil;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;

public class QueryProvider implements LayoutProvider {

    private Config config = HCTeams.getInstance().getScoreboardConfig();
    private String title = config.getString("SCOREBOARD.TITLE").replace("%straight_line%", "｜");


    @Override
    public BoardLayout getLayout(Player player) {
        BoardLayout layout = new BoardLayout();

        layout.setTitle(title);
        Profile profile = Profile.getProfile(player);

        config.getStringList("SCOREBOARD.HEADER").forEach(layout::add);
        if(HCTeams.getInstance().isKitMap()) config.getStringList("SCOREBOARD.KITMAP").forEach(string -> layout.add(string.replace("%kills%", player.getStatistic(Statistic.PLAYER_KILLS) + "").replace("%deaths%", player.getStatistic(Statistic.DEATHS) + "").replace("%balance%", profile.getEconomy() + "").replace("%killstreak%", "" + profile.getKillStreak())));
        
        profile.getPlayerTimers().forEach(timer -> layout.add(timer.getDisplayName() + "&7: " + config.getString("SCOREBOARD.VALUES") + TimeUtil.getRemainingTime(timer.getCooldowns().get(player), timer.isTrailing()) + 's'));

        if(profile.getPvpClass() != null) {
            if(profile.getPvpClass() instanceof BardClass)
                layout.add("&b&lBard Energy&7: &c" + (BardClass.getBardEnergy().get(player) / 1000));
        }

        config.getStringList("SCOREBOARD.FOOTER").forEach(layout::add);

        return layout;
    }

}

package bar.pvp.hcf.tablist;

import bar.pvp.hcf.utils.player.PlayerUtil;

public class TablistUpdateTask implements Runnable {

    @Override
    public void run() {
        TablistManager manager = TablistManager.INSTANCE;
        if (manager == null) return;
        PlayerUtil.getOnlinePlayers().forEach(player -> {
            Tablist tablist = manager.getTablist(player);
            if (tablist != null) {
                tablist.hideRealPlayers().update();
            }
        });
    }
}
package bar.pvp.hcf.classes;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.utils.player.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PvpClassTask extends BukkitRunnable {

    @Override
    public void run() {
        PlayerUtil.getOnlinePlayers().forEach(player ->
                PvpClass.getClasses().forEach(pvpClass -> {
                    if (Profile.getProfile(player).getPvpClass() != pvpClass && isArmor(player, pvpClass))
                        pvpClass.onEquip(player);


                    else if (Profile.getProfile(player).getPvpClass() != null && Profile.getProfile(player).getPvpClass().equals(pvpClass))
                        pvpClass.onUnequip(player);

                    if(Profile.getProfile(player).getPvpClass() != null && Profile.getProfile(player).getPvpClass().equals(pvpClass))
                        pvpClass.onTick(player);
                }));
    }

    private boolean isArmor(Player player, PvpClass pvpClass) {
        if(player.getInventory().getHelmet() == null || player.getInventory().getChestplate() == null || player.getInventory().getLeggings() == null || player.getInventory().getBoots() == null) return false;
        if(!player.getInventory().getHelmet().equals(pvpClass.getItems()[0])) return false;
        if(!player.getInventory().getChestplate().equals(pvpClass.getItems()[1])) return false;
        if(!player.getInventory().getLeggings().equals(pvpClass.getItems()[2])) return false;

        return player.getInventory().getBoots().equals(pvpClass.getItems()[3]);
    }
}

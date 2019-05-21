package bar.pvp.hcf.classes;

import bar.pvp.hcf.profiles.Profile;
import bar.pvp.hcf.utils.player.PlayerUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;

public class PvpClassTask extends BukkitRunnable {

    @Override
    public void run() {
        PlayerUtil.getOnlinePlayers().forEach(player ->
                PvpClass.getClasses().forEach(pvpClass -> {
                    if (!isArmor(player, pvpClass) && Profile.getProfile(player).getPvpClass() != null && Profile.getProfile(player).getPvpClass().equals(pvpClass))
                        pvpClass.onUnequip(player);

                    if (Profile.getProfile(player).getPvpClass() != pvpClass && isArmor(player, pvpClass))
                        pvpClass.onEquip(player);

                    if(Profile.getProfile(player).getPvpClass() != null && Profile.getProfile(player).getPvpClass().equals(pvpClass))
                        pvpClass.onTick(player);
                }));
    }

    private boolean isArmor(Player player, PvpClass pvpClass) {
        PlayerInventory inv = player.getInventory();
        if(player.getInventory().getHelmet() == null || player.getInventory().getChestplate() == null || player.getInventory().getLeggings() == null || player.getInventory().getBoots() == null) return false;
        if(inv.getHelmet() == null || !player.getInventory().getHelmet().equals(pvpClass.getItems()[0])) return false;
        if(inv.getChestplate() == null || !player.getInventory().getChestplate().equals(pvpClass.getItems()[1])) return false;
        if(inv.getChestplate() == null || !player.getInventory().getLeggings().equals(pvpClass.getItems()[2])) return false;

        return inv.getBoots() != null && player.getInventory().getBoots().equals(pvpClass.getItems()[3]);
    }
}

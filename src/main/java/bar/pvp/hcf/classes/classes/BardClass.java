package bar.pvp.hcf.classes.classes;

import bar.pvp.hcf.classes.PvpClass;
import bar.pvp.hcf.profiles.Profile;

import bar.pvp.hcf.utils.TimeUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BardClass extends PvpClass implements Listener {

    @Getter
    private static Map<Player, Float> bardEnergy = new HashMap<>();

    private PotionEffect[] potionEffects = new PotionEffect[] {
            new PotionEffect(PotionEffectType.SPEED, 20000, 1),
            new PotionEffect(PotionEffectType.REGENERATION, 20000, 0),
            new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20000, 1),
    };

    public BardClass() {
        super("Bard", "The bard class, used to give effects to team mates.", "&eBard", new ItemStack[]{
                new ItemStack(Material.GOLD_HELMET),
                new ItemStack(Material.GOLD_CHESTPLATE),
                new ItemStack(Material.GOLD_LEGGINGS),
                new ItemStack(Material.GOLD_BOOTS),
        });
    }

    @Override
    public void onEquip(Player player) {
        super.onEquip(player);
        Arrays.stream(potionEffects).forEach(player::addPotionEffect);
        player.sendMessage(getConfig().getString("CLASSES.BARD.EQUIP"));
        Profile.getProfile(player).setPvpClass(this);
    }

    public void onTick(Player player) {
        super.onTick(player);
        if(!bardEnergy.containsKey(player))
            bardEnergy.put(player, 100F);
        else if(bardEnergy.get(player) < 120000)
            bardEnergy.put(player, bardEnergy.get(player) + 100F);

        float energy = bardEnergy.get(player);
        if(energy == 10000 || energy == 20000 || energy == 30000 || energy == 40000 || energy == 50000 || energy == 60000 || energy == 70000 || energy == 80000 || energy == 90000 || energy == 120000)
            player.sendMessage(getConfig().getString("CLASSES.BARD.ENERGY").replace("%ENERGY%", TimeUtil.getRemainingTime(energy, true)));
    }
}

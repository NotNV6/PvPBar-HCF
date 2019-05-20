package bar.pvp.hcf.classes;

import bar.pvp.hcf.utils.Config;
import bar.pvp.hcf.HCTeams;
import bar.pvp.hcf.classes.classes.BardClass;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PvpClass {

    private Config config = HCTeams.getInstance().getLangConfig();

    @Getter
    private static List<PvpClass> classes = new ArrayList<>();

    private String name, description, displayName;
    private ItemStack[] items;

    public PvpClass(String name, String description, String displayName, ItemStack[] items) {
        this.name = name;
        this.description = description;
        this.displayName = displayName;
        this.items = items;

        classes.add(this);
    }

    public static void setup() {
        new BardClass();

        // Don't call this before registering classes, it iterates through every class.
        new PvpClassTask().runTaskTimer(HCTeams.getInstance(), 2, 2);
    }

    public void onEquip(Player player) { }

    public void onUnequip(Player player) { }

    public void onTick(Player player) { }
}

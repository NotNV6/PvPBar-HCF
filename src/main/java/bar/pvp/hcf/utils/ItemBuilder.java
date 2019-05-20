package bar.pvp.hcf.utils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemBuilder {

	private ItemStack item;
	private ItemMeta meta;
	private String displayName;
	private List<String> lore;

	public ItemBuilder(Material material) {
		if(material == null) material = Material.AIR;
		this.item = new ItemStack(material);
		this.meta = item.getItemMeta();

		this.lore = new ArrayList<>();
		this.displayName = "";
	}

	public ItemBuilder displayName(String displayName) {
		if(displayName == null) displayName = "";
		this.displayName = CC.translate(displayName);
		return this;
	}


	public ItemBuilder lore(List<String> lore) {
		if(lore.isEmpty()) lore = Collections.emptyList();
		lore.forEach(string -> this.lore.add(CC.translate(string)));
		return this;
	}

	public ItemStack create() {
		this.meta.setDisplayName(displayName);
		if(lore != null && !lore.isEmpty()) this.meta.setLore(lore);
		this.item.setItemMeta(meta);
		return this.item;
	}

}

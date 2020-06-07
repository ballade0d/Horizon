package xyz.hstudio.horizon.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemStackBuilder {

    private final ItemStack itemStack;

    public ItemStackBuilder(final Material mat) {
        this.itemStack = new ItemStack(mat);
    }

    public ItemStackBuilder(final ItemStack item) {
        this.itemStack = item;
    }

    public ItemStackBuilder withAmount(final int amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemStackBuilder withName(final String name) {
        final ItemMeta meta = itemStack.getItemMeta();
        meta.setDisplayName(name);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withLore(final String name) {
        final ItemMeta meta = itemStack.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(name);
        meta.setLore(lore);
        itemStack.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withEnchantment(final Enchantment enchantment, final int level) {
        itemStack.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemStackBuilder withEnchantment(final Enchantment enchantment) {
        itemStack.addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    public ItemStackBuilder withType(final Material material) {
        itemStack.setType(material);
        return this;
    }

    public ItemStack build() {
        return itemStack;
    }
}
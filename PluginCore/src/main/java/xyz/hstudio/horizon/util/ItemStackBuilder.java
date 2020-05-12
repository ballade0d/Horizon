package xyz.hstudio.horizon.util;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemStackBuilder {

    private final ItemStack ITEM_STACK;

    public ItemStackBuilder(final Material mat) {
        this.ITEM_STACK = new ItemStack(mat);
    }

    public ItemStackBuilder(final ItemStack item) {
        this.ITEM_STACK = item;
    }

    public ItemStackBuilder withAmount(final int amount) {
        ITEM_STACK.setAmount(amount);
        return this;
    }

    public ItemStackBuilder withName(final String name) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();
        meta.setDisplayName(name);
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withLore(final String name) {
        final ItemMeta meta = ITEM_STACK.getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>();
        }
        lore.add(name);
        meta.setLore(lore);
        ITEM_STACK.setItemMeta(meta);
        return this;
    }

    public ItemStackBuilder withEnchantment(final Enchantment enchantment, final int level) {
        ITEM_STACK.addUnsafeEnchantment(enchantment, level);
        return this;
    }

    public ItemStackBuilder withEnchantment(final Enchantment enchantment) {
        ITEM_STACK.addUnsafeEnchantment(enchantment, 1);
        return this;
    }

    public ItemStackBuilder withType(final Material material) {
        ITEM_STACK.setType(material);
        return this;
    }

    public ItemStack build() {
        return ITEM_STACK;
    }
}
package xyz.hstudio.horizon.wrapper;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.server.v1_8_R3.Enchantment;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.NBTTagList;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers;

public class ItemWrapper {

    protected final ItemStack itemStack;
    protected final TObjectIntMap<Enchantment> enchantments;

    public ItemWrapper(ItemStack itemStack) {
        this.itemStack = itemStack;
        this.enchantments = new TObjectIntHashMap<>();

        if (itemStack == null) {
            return;
        }
        NBTTagList tag = itemStack.getEnchantments();
        if (tag == null) {
            return;
        }
        for (int i = 0; i < tag.size(); i++) {
            Enchantment enchantment = Enchantment.getById(tag.get(i).getShort("id"));
            int level = tag.get(i).getShort("lvl");
            this.enchantments.put(enchantment, level);
        }
    }

    public boolean hasEnchantment(Enchantment enchantment) {
        return enchantments.containsKey(enchantment);
    }

    public int getEnchantmentLevel(Enchantment enchantment) {
        return enchantments.get(enchantment);
    }

    public Material type() {
        if (itemStack == null) {
            return Material.AIR;
        }

        return CraftMagicNumbers.getMaterial(itemStack.getItem());
    }

    public float breakSpeed(BlockWrapper block) {
        if (itemStack == null) {
            return 1f;
        }
        return itemStack.a(block.block);
    }

    public boolean canBreak(BlockWrapper block) {
        if (itemStack == null) {
            return false;
        }
        return itemStack.b(block.block);
    }
}
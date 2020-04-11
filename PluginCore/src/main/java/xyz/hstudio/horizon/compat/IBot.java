package xyz.hstudio.horizon.compat;

import org.bukkit.Material;
import xyz.hstudio.horizon.data.HoriPlayer;

public interface IBot {

    Material[] HELMET = new Material[]{Material.LEATHER_HELMET, Material.IRON_HELMET, Material.CHAINMAIL_HELMET, Material.DIAMOND_HELMET};
    Material[] CHESTPLATE = new Material[]{Material.LEATHER_CHESTPLATE, Material.IRON_CHESTPLATE, Material.CHAINMAIL_CHESTPLATE, Material.DIAMOND_CHESTPLATE};
    Material[] LEGGINGS = new Material[]{Material.LEATHER_LEGGINGS, Material.IRON_LEGGINGS, Material.CHAINMAIL_LEGGINGS, Material.DIAMOND_LEGGINGS};
    Material[] BOOTS = new Material[]{Material.LEATHER_BOOTS, Material.IRON_BOOTS, Material.CHAINMAIL_BOOTS, Material.DIAMOND_BOOTS};
    Material[] HAND = new Material[]{Material.GOLDEN_APPLE, Material.DIAMOND_SWORD, Material.IRON_SWORD, Material.STONE_SWORD, Material.DIAMOND_PICKAXE, Material.COOKED_BEEF};

    void spawn(final HoriPlayer player);

    void removeFromTabList(final HoriPlayer player);

    void despawn(final HoriPlayer player);

    void updatePing(final HoriPlayer player);

    void setSneaking(final boolean sneaking);

    void setSprinting(final boolean sprinting);

    void updateStatus(final HoriPlayer player);

    void swingArm(final HoriPlayer player);

    void damage(final HoriPlayer player);

    void move(final double x, final double y, final double z, final float yaw, final float pitch, final HoriPlayer player);

    void setArmor(final HoriPlayer player);

    int getId();

    long getSpawnTime();

    boolean isRealName();
}
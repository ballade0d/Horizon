package xyz.hstudio.horizon.util.enums;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;

public enum MatUtils {

    SWEET_BERRY_BUSH(),
    COCOA_BEANS("COCOA"),
    CHORUS_FLOWER(),
    CHORUS_PLANT(),
    BAMBOO(),
    BUBBLE_COLUMN(),
    END_ROD(),
    FARMLAND("SOIL"),
    ENCHANTED_GOLDEN_APPLE("GOLDEN_APPLE"),
    KELP(),
    KELP_PLANT(),
    LADDER("LADDER"),
    VINE("VINE"),
    SEA_PICKLE(),
    SCAFFOLDING(),
    REPEATER("DIODE"),
    LILY_PAD("WATER_LILY"),
    COBWEB("WEB");

    public static final Set<Material> BLOCKABLE = EnumSet.noneOf(Material.class);
    public static final Set<Material> LIQUID = EnumSet.noneOf(Material.class);

    static {
        LIQUID.add(Material.WATER);
        LIQUID.add(Material.LAVA);
        if (Version.VERSION == Version.v1_8_R3) {
            BLOCKABLE.add(Material.WOOD_SWORD);
            BLOCKABLE.add(Material.STONE_SWORD);
            BLOCKABLE.add(Material.IRON_SWORD);
            BLOCKABLE.add(Material.GOLD_SWORD);
            BLOCKABLE.add(Material.DIAMOND_SWORD);
            LIQUID.add(Material.getMaterial("STATIONARY_WATER"));
            LIQUID.add(Material.getMaterial("STATIONARY_LAVA"));
        } else {
            BLOCKABLE.add(Material.getMaterial("SHIELD"));
        }
    }

    private final String[] legacy;

    MatUtils(final String... legacy) {
        this.legacy = legacy;
    }

    public Material parse() {
        Material newMat = Material.getMaterial(this.name());
        if (newMat != null) {
            return newMat;
        }
        Material oldMat;
        for (int i = this.legacy.length - 1; i >= 0; i--) {
            oldMat = Material.getMaterial(this.legacy[i]);
            if (oldMat != null) {
                return oldMat;
            }
        }
        return null;
    }
}
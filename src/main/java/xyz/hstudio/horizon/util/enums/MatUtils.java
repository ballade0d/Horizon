package xyz.hstudio.horizon.util.enums;

import org.bukkit.Material;

import java.util.EnumSet;
import java.util.Set;

public enum MatUtils {

    COCOA_BEANS("COCOA"),
    CHORUS_FLOWER(),
    CHORUS_PLANT(),
    BAMBOO(),
    END_ROD(),
    FARMLAND("SOIL"),
    ENCHANTED_GOLDEN_APPLE("GOLDEN_APPLE"),
    SLIME_BLOCK("SLIME_BLOCK"),
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

    static {
        if (Version.VERSION == Version.v1_8_R3) {
            BLOCKABLE.add(Material.WOOD_SWORD);
            BLOCKABLE.add(Material.STONE_SWORD);
            BLOCKABLE.add(Material.IRON_SWORD);
            BLOCKABLE.add(Material.GOLD_SWORD);
            BLOCKABLE.add(Material.DIAMOND_SWORD);
        } else {
            BLOCKABLE.add(Material.getMaterial("SHIELD"));
        }
    }

    private final String[] legacy;

    MatUtils(final String... legacy) {
        this.legacy = legacy;
    }

    public static boolean isLiquid(final Material material) {
        return material.name().contains("WATER") || material.name().contains("LAVA");
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
package xyz.hstudio.horizon.util;

import lombok.var;
import org.bukkit.Material;

public enum MaterialUtils {

    CHORUS_FLOWER(),
    CHORUS_PLANT(),
    END_ROD(),
    FARMLAND("SOIL"),
    SEA_PICKLE();

    private final String[] legacy;

    MaterialUtils(String... legacy) {
        this.legacy = legacy;
    }

    public Material parse() {
        var material = Material.getMaterial(this.name());
        if (material != null) {
            return material;
        }
        for (int i = this.legacy.length - 1; i >= 0; i--) {
            if ((material = Material.getMaterial(legacy[i])) == null) {
                continue;
            }
            return material;
        }
        return null;
    }
}
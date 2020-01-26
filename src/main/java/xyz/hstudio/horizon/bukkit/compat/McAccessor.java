package xyz.hstudio.horizon.bukkit.compat;

import xyz.hstudio.horizon.bukkit.compat.v1_12_R1.McAccessor_v1_12_R1;
import xyz.hstudio.horizon.bukkit.compat.v1_13_R2.McAccessor_v1_13_R2;
import xyz.hstudio.horizon.bukkit.compat.v1_14_R1.McAccessor_v1_14_R1;
import xyz.hstudio.horizon.bukkit.compat.v1_15_R1.McAccessor_v1_15_R1;
import xyz.hstudio.horizon.bukkit.compat.v1_8_R3.McAccessor_v1_8_R3;
import xyz.hstudio.horizon.bukkit.util.Version;

public class McAccessor {

    public static final IMcAccessor INSTANCE;

    static {
        switch (Version.VERSION) {
            case v1_8_R3:
                INSTANCE = new McAccessor_v1_8_R3();
                break;
            case v1_12_R1:
                INSTANCE = new McAccessor_v1_12_R1();
                break;
            case v1_13_R2:
                INSTANCE = new McAccessor_v1_13_R2();
                break;
            case v1_14_R1:
                INSTANCE = new McAccessor_v1_14_R1();
                break;
            case v1_15_R1:
                INSTANCE = new McAccessor_v1_15_R1();
                break;
            default:
                INSTANCE = null;
                break;
        }
    }
}
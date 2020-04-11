package xyz.hstudio.horizon.compat;

import org.bukkit.entity.Player;
import xyz.hstudio.horizon.compat.v1_12_R1.Bot_v1_12_R1;
import xyz.hstudio.horizon.compat.v1_13_R2.Bot_v1_13_R2;
import xyz.hstudio.horizon.compat.v1_14_R1.Bot_v1_14_R1;
import xyz.hstudio.horizon.compat.v1_15_R1.Bot_v1_15_R1;
import xyz.hstudio.horizon.compat.v1_8_R3.Bot_v1_8_R3;
import xyz.hstudio.horizon.util.enums.Version;

public class Bot {

    public static IBot createBot(final Player player, final boolean realisticName) {
        switch (Version.VERSION) {
            case v1_8_R3:
                return new Bot_v1_8_R3(player, realisticName);
            case v1_12_R1:
                return new Bot_v1_12_R1(player, realisticName);
            case v1_13_R2:
                return new Bot_v1_13_R2(player, realisticName);
            case v1_14_R1:
                return new Bot_v1_14_R1(player, realisticName);
            case v1_15_R1:
                return new Bot_v1_15_R1(player, realisticName);
            default:
                return null;
        }
    }
}
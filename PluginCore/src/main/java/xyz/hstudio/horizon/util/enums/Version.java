package xyz.hstudio.horizon.util.enums;

import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Arrays;

public enum Version {

    v1_8_R3(47),
    v1_12_R1(335),
    v1_13_R2(393),
    v1_14_R1(477),
    v1_15_R1(573),
    UNKNOWN(0);

    public static final Version VERSION;

    static {
        String rawVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        VERSION = Arrays.stream(Version.values())
                .filter(v -> v.name().equals(rawVersion)).findFirst().orElse(Version.UNKNOWN);
    }

    @Getter
    private final int protocol;

    Version(int protocol) {
        this.protocol = protocol;
    }

    public static Version getNearest(final int protocol) {
        if (protocol >= v1_15_R1.protocol) {
            return v1_15_R1;
        } else if (protocol >= v1_14_R1.protocol) {
            return v1_14_R1;
        } else if (protocol >= v1_13_R2.protocol) {
            return v1_13_R2;
        } else if (protocol >= v1_12_R1.protocol) {
            return v1_12_R1;
        } else if (protocol >= v1_8_R3.protocol) {
            return v1_8_R3;
        }
        return UNKNOWN;
    }

    public boolean isNewer(final Version version) {
        return this.protocol >= version.getProtocol();
    }

    public boolean isOlder(final Version version) {
        return this.protocol <= version.getProtocol();
    }
}
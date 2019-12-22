package xyz.hstudio.horizon.bukkit.util;

import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.Arrays;

public enum Version {

    v1_8(47),
    v1_12(335),
    v1_13(393),
    v1_14(477),
    v1_15(573),
    UNKNOWN(0);

    @Getter
    private static final Version VERSION;

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
        if (protocol >= v1_15.protocol) {
            return v1_15;
        } else if (protocol >= v1_14.protocol) {
            return v1_14;
        } else if (protocol >= v1_13.protocol) {
            return v1_13;
        } else if (protocol >= v1_12.protocol) {
            return v1_12;
        } else if (protocol >= v1_8.protocol) {
            return v1_8;
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
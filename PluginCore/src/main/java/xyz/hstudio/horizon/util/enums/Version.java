package xyz.hstudio.horizon.util.enums;

import org.bukkit.Bukkit;

import java.util.Arrays;

public enum Version {

    v1_8_R3(47, 47),
    v1_12_R1(335, 340),
    v1_13_R2(393, 404),
    v1_14_R1(477, 498),
    v1_15_R1(573, 578),
    UNKNOWN(0, 0);

    public static final Version VERSION;

    static {
        String rawVersion = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        VERSION = Arrays.stream(Version.values())
                .filter(v -> v.name().equals(rawVersion)).findFirst().orElse(Version.UNKNOWN);
    }

    public final int minProtocol;
    public final int maxProtocol;

    Version(final int minProtocol, final int maxProtocol) {
        this.minProtocol = minProtocol;
        this.maxProtocol = maxProtocol;
    }

    public static Version getVersion(final int protocol) {
        return Arrays
                .stream(values())
                .filter(v -> v.maxProtocol >= protocol && v.minProtocol <= protocol)
                .findFirst()
                .orElse(Version.UNKNOWN);
    }
}
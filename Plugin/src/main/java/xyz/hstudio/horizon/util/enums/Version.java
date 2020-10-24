package xyz.hstudio.horizon.util.enums;

import lombok.Getter;

import java.util.stream.Stream;

public enum Version {

    v1_8_R3 {
        @Override
        public boolean validate() {
            try {
                Class.forName("net.minecraft.server.v1_8_R3.MinecraftServer");
                return true;
            } catch (ClassNotFoundException ignore) {
            }
            return false;
        }
    },
    UNKNOWN {
        @Override
        public boolean validate() {
            return false;
        }
    };

    @Getter
    public static final Version inst;

    static {
        inst = Stream.of(Version.values())
                .filter(Version::validate).findFirst().orElse(UNKNOWN);
    }

    public abstract boolean validate();
}
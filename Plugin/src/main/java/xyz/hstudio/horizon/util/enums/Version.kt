package xyz.hstudio.horizon.util.enums

import java.util.stream.Stream

enum class Version {
    V1_8_R3 {
        override fun validate(): Boolean {
            try {
                Class.forName("net.minecraft.server.v1_8_R3.MinecraftServer")
                return true
            } catch (ignore: ClassNotFoundException) {
            }
            return false
        }
    },
    UNKNOWN {
        override fun validate(): Boolean {
            return false
        }
    };

    companion object {
        var inst: Version = Stream.of(*values())
                .filter { obj: Version -> obj.validate() }
                .findFirst()
                .orElse(UNKNOWN)
    }

    abstract fun validate(): Boolean
}
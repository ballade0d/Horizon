package xyz.hstudio.horizon.bukkit.network.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import xyz.hstudio.horizon.lib.com.esotericsoftware.reflectasm.FieldAccess;
import xyz.hstudio.horizon.lib.com.esotericsoftware.reflectasm.MethodAccess;

@RequiredArgsConstructor
public class WrappedPacket {

    @Getter
    private final Object packet;

    public FieldAccess getFieldAccess() {
        return FieldAccess.get(this.packet.getClass());
    }

    public MethodAccess getMethodAccess() {
        return MethodAccess.get(this.packet.getClass());
    }
}
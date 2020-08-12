package xyz.hstudio.horizon.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import xyz.hstudio.horizon.wrapper.AccessorBase;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.WorldBase;
import xyz.hstudio.horizon.wrapper.v1_12_R1.Accessor_v1_12_R1;
import xyz.hstudio.horizon.wrapper.v1_12_R1.Entity_v1_12_R1;
import xyz.hstudio.horizon.wrapper.v1_12_R1.World_v1_12_R1;
import xyz.hstudio.horizon.wrapper.v1_8_R3.Accessor_v1_8_R3;
import xyz.hstudio.horizon.wrapper.v1_8_R3.Entity_v1_8_R3;
import xyz.hstudio.horizon.wrapper.v1_8_R3.World_v1_8_R3;

import java.util.stream.Stream;

public enum EnumVersion {

    v1_8_R3("v1_8_R3") {
        @Override
        public AccessorBase getAccessor() {
            return new Accessor_v1_8_R3();
        }

        @Override
        public EntityBase getEntity(Entity entity) {
            return new Entity_v1_8_R3(entity);
        }

        @Override
        public WorldBase getWorld(World world) {
            return new World_v1_8_R3(world);
        }
    },
    v1_12_R1("v1_12_R1") {
        @Override
        public AccessorBase getAccessor() {
            return new Accessor_v1_12_R1();
        }

        @Override
        public EntityBase getEntity(Entity entity) {
            return new Entity_v1_12_R1(entity);
        }

        @Override
        public WorldBase getWorld(World world) {
            return new World_v1_12_R1(world);
        }
    },
    UNKNOWN("UNKNOWN") {
        @Override
        public AccessorBase getAccessor() {
            return null;
        }

        @Override
        public EntityBase getEntity(Entity entity) {
            return null;
        }

        @Override
        public WorldBase getWorld(World world) {
            return null;
        }
    };

    public static final EnumVersion VERSION;

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        VERSION = Stream.of(EnumVersion.values())
                .filter(v -> v.getName().equals(version))
                .findFirst().orElse(EnumVersion.UNKNOWN);
    }

    @Getter
    private final String name;

    EnumVersion(String name) {
        this.name = name;
    }

    public abstract AccessorBase getAccessor();

    public abstract EntityBase getEntity(Entity entity);

    public abstract WorldBase getWorld(World world);
}
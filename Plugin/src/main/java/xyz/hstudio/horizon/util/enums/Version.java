package xyz.hstudio.horizon.util.enums;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import xyz.hstudio.horizon.wrapper.AccessorBase;
import xyz.hstudio.horizon.wrapper.EntityBase;
import xyz.hstudio.horizon.wrapper.PackerBase;
import xyz.hstudio.horizon.wrapper.WorldBase;
import xyz.hstudio.horizon.wrapper.v1_12.Accessor_v1_12;
import xyz.hstudio.horizon.wrapper.v1_12.Entity_v1_12;
import xyz.hstudio.horizon.wrapper.v1_12.Packer_v1_12;
import xyz.hstudio.horizon.wrapper.v1_12.World_v1_12;
import xyz.hstudio.horizon.wrapper.v1_8.Accessor_v1_8;
import xyz.hstudio.horizon.wrapper.v1_8.Entity_v1_8;
import xyz.hstudio.horizon.wrapper.v1_8.Packer_v1_8;
import xyz.hstudio.horizon.wrapper.v1_8.World_v1_8;

import java.util.stream.Stream;

public enum Version {

    v1_8_R3("v1_8_R3") {
        @Override
        public AccessorBase getAccessor() {
            return new Accessor_v1_8();
        }

        @Override
        public EntityBase getEntity(Entity entity) {
            return new Entity_v1_8(entity);
        }

        @Override
        public PackerBase getPacker() {
            return new Packer_v1_8();
        }

        @Override
        public WorldBase getWorld(World world) {
            return new World_v1_8(world);
        }
    },
    v1_12_R1("v1_12_R1") {
        @Override
        public AccessorBase getAccessor() {
            return new Accessor_v1_12();
        }

        @Override
        public EntityBase getEntity(Entity entity) {
            return new Entity_v1_12(entity);
        }

        @Override
        public PackerBase getPacker() {
            return new Packer_v1_12();
        }

        @Override
        public WorldBase getWorld(World world) {
            return new World_v1_12(world);
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
        public PackerBase getPacker() {
            return null;
        }

        @Override
        public WorldBase getWorld(World world) {
            return null;
        }
    };

    public static final Version VERSION;

    static {
        String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        VERSION = Stream.of(Version.values())
                .filter(v -> v.getName().equals(version))
                .findFirst().orElse(Version.UNKNOWN);
    }

    @Getter
    private final String name;

    Version(String name) {
        this.name = name;
    }

    public abstract AccessorBase getAccessor();

    public abstract EntityBase getEntity(Entity entity);

    public abstract PackerBase getPacker();

    public abstract WorldBase getWorld(World world);
}
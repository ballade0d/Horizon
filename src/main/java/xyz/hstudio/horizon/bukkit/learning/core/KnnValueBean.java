package xyz.hstudio.horizon.bukkit.learning.core;

public class KnnValueBean<T> {

    public final T value;
    public final String typeId;

    public KnnValueBean(final T value, final String typeId) {
        this.value = value;
        this.typeId = typeId;
    }
}
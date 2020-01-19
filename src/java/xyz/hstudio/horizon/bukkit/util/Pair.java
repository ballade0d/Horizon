package xyz.hstudio.horizon.bukkit.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Pair<K, V> {

    private K key;
    private V value;

    public Pair<K, V> setKey(final K key) {
        this.key = key;
        return this;
    }

    public Pair<K, V> setValue(final V value) {
        this.value = value;
        return this;
    }
}
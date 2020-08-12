package xyz.hstudio.horizon.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
public class Pair<K, V> {

    @Getter
    @Setter
    private K key;
    @Getter
    @Setter
    private V value;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Pair && ((Pair<?, ?>) obj).key.equals(key) && ((Pair<?, ?>) obj).value.equals(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return "[" + key.toString() + "] [" + value.toString() + "]";
    }
}
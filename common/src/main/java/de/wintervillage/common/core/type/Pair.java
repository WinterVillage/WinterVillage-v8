package de.wintervillage.common.core.type;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Pair<U, V> implements Tuple {

    private final U first;
    private final V second;

    protected Pair(
            final U first,
            final V second
    ) {
        this.first = first;
        this.second = second;
    }

    public static <U, V> Pair<U, V> of(
            final U first,
            final V second
    ) {
        return new Pair<>(first, second);
    }

    public final U first() {
        return this.first;
    }

    public final V second() {
        return this.second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        final Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(this.first(), pair.first()) && Objects.equals(this.second(), pair.second());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.first(), this.second());
    }

    @Override
    public String toString() {
        return String.format("Pair={%s, %s}", this.first(), this.second());
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public @NotNull Object @NotNull [] toArray() {
        final Object[] array = new Object[2];
        array[0] = this.first();
        array[1] = this.second();
        return array;
    }
}

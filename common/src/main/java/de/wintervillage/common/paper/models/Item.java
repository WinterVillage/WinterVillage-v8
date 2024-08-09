package de.wintervillage.common.paper.models;

import java.util.Arrays;

public record Item(byte[] bytes) {

    @Override
    public String toString() {
        return "Item{" +
                "bytes=" + Arrays.toString(this.bytes) +
                '}';
    }
}

package de.wintervillage.common.core.type;

import org.jetbrains.annotations.NotNull;

public interface Tuple {

    int size();

    @NotNull Object @NotNull [] toArray();
}

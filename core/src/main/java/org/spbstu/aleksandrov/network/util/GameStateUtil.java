package org.spbstu.aleksandrov.network.util;

public final class GameStateUtil {

    private GameStateUtil() {}

    public static int getMaxValueIndex(final double[] values) {
        int maxAt = 0;

        for (int i = 0; i < values.length; i++) {
            maxAt = values[i] > values[maxAt] ? i : maxAt;
        }

        return maxAt;
    }
}

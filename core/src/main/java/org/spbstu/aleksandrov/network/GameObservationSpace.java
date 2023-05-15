package org.spbstu.aleksandrov.network;

import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.spbstu.aleksandrov.network.util.NetworkUtil;

public class GameObservationSpace implements ObservationSpace<GameState> {
    private static final double[] LOWS = GameObservationSpace.createValueArray(NetworkUtil.LOW_VALUE);
    private static final double[] HIGHS = GameObservationSpace.createValueArray(NetworkUtil.HIGH_VALUE);

    @Override
    public String getName() {
        return "GameObservationSpace";
    }

    @Override
    public int[] getShape() {
        return new int[] {
                1, NetworkUtil.NUMBER_OF_INPUTS
        };
    }

    @Override
    public INDArray getLow() {
        return Nd4j.create(LOWS);
    }

    @Override
    public INDArray getHigh() {
        return Nd4j.create(HIGHS);
    }

    private static double[] createValueArray(final double value) {
        final double[] values = new double[NetworkUtil.NUMBER_OF_INPUTS];

        for (int i = 0; i < NetworkUtil.NUMBER_OF_INPUTS; i++) {
            values[i] = value;
        }

        return values;
    }
}

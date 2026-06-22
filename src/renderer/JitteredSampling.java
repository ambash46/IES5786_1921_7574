package renderer;

import java.util.Random;

/**
 * Jittered: n×n cells, each cell's sample offset randomly within that cell.
 * Combines grid regularity with stochastic variation — regenerated each call.
 */
class JitteredSampling implements SamplingPattern {

    static final JitteredSampling INSTANCE = new JitteredSampling();

    private static final Random RNG = new Random();

    private JitteredSampling() {}

    @Override
    public double[][] generateOffsets(int numSamples) {
        int n = (int) Math.ceil(Math.sqrt(numSamples));
        double cellSize = 1.0 / n;
        double[][] offsets = new double[n * n][2];
        int idx = 0;
        for (int row = 0; row < n; row++)
            for (int col = 0; col < n; col++) {
                offsets[idx][0]   = -0.5 + (col + RNG.nextDouble()) * cellSize;
                offsets[idx++][1] = -0.5 + (row + RNG.nextDouble()) * cellSize;
            }
        return offsets;
    }
}

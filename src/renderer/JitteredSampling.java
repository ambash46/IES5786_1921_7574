package renderer;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Jittered: n×n cells, each cell's sample offset randomly within that cell.
 * Combines grid regularity with stochastic variation — regenerated each call.
 */
class JitteredSampling implements SamplingPattern {

    static final JitteredSampling INSTANCE = new JitteredSampling();

    private JitteredSampling() {}

    @Override
    public double[][] generateOffsets(int numSamples) {
        int n = (int) Math.ceil(Math.sqrt(numSamples));
        double cellSize = 1.0 / n;
        double[][] offsets = new double[n * n][2];
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        int idx = 0;
        for (int row = 0; row < n; row++)
            for (int col = 0; col < n; col++) {
                offsets[idx][0]   = -0.5 + (col + rng.nextDouble()) * cellSize;
                offsets[idx++][1] = -0.5 + (row + rng.nextDouble()) * cellSize;
            }
        return offsets;
    }
}

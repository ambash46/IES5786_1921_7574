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
        // Same n×n cells as GridSampling -- reuse its cell centers and
        // displace each one randomly within its own cell.
        double[][] grid = GridSampling.INSTANCE.generateOffsets(numSamples);
        double cellSize = 1.0 / (int) Math.round(Math.sqrt(grid.length));
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        double[][] offsets = new double[grid.length][2];
        for (int i = 0; i < grid.length; i++) {
            offsets[i][0] = grid[i][0] + (rng.nextDouble() - 0.5) * cellSize;
            offsets[i][1] = grid[i][1] + (rng.nextDouble() - 0.5) * cellSize;
        }
        return offsets;
    }
}

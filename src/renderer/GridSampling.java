package renderer;

/**
 * Regular n×n grid: cell centers evenly spaced in [-0.5, 0.5]².
 * {@code n = ceil(sqrt(numSamples))}, so 9→3×3, 81→9×9, 289→17×17.
 * Results are deterministic and may be cached.
 */
class GridSampling implements SamplingPattern {

    static final GridSampling INSTANCE = new GridSampling();

    private GridSampling() {}

    @Override
    public double[][] generateOffsets(int numSamples) {
        int n = (int) Math.ceil(Math.sqrt(numSamples));
        double[][] offsets = new double[n * n][2];
        int idx = 0;
        for (int row = 0; row < n; row++)
            for (int col = 0; col < n; col++) {
                offsets[idx][0]   = -0.5 + (col + 0.5) / n;
                offsets[idx++][1] = -0.5 + (row + 0.5) / n;
            }
        return offsets;
    }

    @Override
    public boolean isCacheable() { return true; }
}

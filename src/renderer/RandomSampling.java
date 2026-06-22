package renderer;

import java.util.Random;

/**
 * Fully stochastic: {@code numSamples} independent uniform points in [-0.5, 0.5]².
 */
class RandomSampling implements SamplingPattern {

    static final RandomSampling INSTANCE = new RandomSampling();

    private static final Random RNG = new Random();

    private RandomSampling() {}

    @Override
    public double[][] generateOffsets(int numSamples) {
        double[][] offsets = new double[numSamples][2];
        for (int i = 0; i < numSamples; i++) {
            offsets[i][0] = RNG.nextDouble() - 0.5;
            offsets[i][1] = RNG.nextDouble() - 0.5;
        }
        return offsets;
    }
}

package renderer;

/**
 * Strategy for distributing sample points across a target area.
 *
 * <p>Implementations generate normalized 2D offsets in [-0.5, 0.5]²; the
 * {@link Blackboard} maps them to 3D points and optionally filters to a
 * circle.  The interface is {@code @FunctionalInterface} so callers can
 * supply a lambda for custom distributions.
 *
 * @author Ambash and Elyasaf
 */
@FunctionalInterface
public interface SamplingPattern {

    /**
     * Generates {@code numSamples} normalized 2D offsets in [-0.5, 0.5]².
     *
     * @param numSamples requested number of samples (after any over-generation
     *                   for circle filtering, managed by the caller)
     * @return array of [dx, dy] offsets; each row is one sample
     */
    double[][] generateOffsets(int numSamples);

    /**
     * Returns {@code true} if the generated offsets are deterministic and may
     * be cached across calls for the same {@code numSamples}.
     * Default is {@code false}; deterministic strategies (e.g. Grid) override
     * this to {@code true}.
     */
    default boolean isCacheable() { return false; }
}

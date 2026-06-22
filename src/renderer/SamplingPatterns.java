package renderer;

/**
 * Named constants for the built-in {@link SamplingPattern} implementations.
 *
 * <p>Usage mirrors {@code Comparator.naturalOrder()}: callers reference the
 * constant without knowing the concrete class, keeping the concrete
 * implementations package-private and the public API minimal.
 *
 * <pre>
 *     camera.setAntiAliasing(81, SamplingPatterns.GRID);
 *     camera.setAntiAliasing(81, SamplingPatterns.JITTERED);
 * </pre>
 *
 * @author Ambash and Elyasaf
 */
public final class SamplingPatterns {

    private SamplingPatterns() {}

    /** Regular n×n grid — deterministic, cached after first generation. */
    public static final SamplingPattern GRID     = GridSampling.INSTANCE;

    /** Fully stochastic — new random points every call. */
    public static final SamplingPattern RANDOM   = RandomSampling.INSTANCE;

    /** Grid cells with per-cell random jitter — regenerated every call. */
    public static final SamplingPattern JITTERED = JitteredSampling.INSTANCE;
}

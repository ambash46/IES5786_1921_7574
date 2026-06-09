package lighting;

import primitives.Color;

/**
 * Represents ambient light in a scene — a constant, non-directional light that
 * illuminates all objects equally regardless of position or orientation.
 * <p>
 * This class is immutable.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public final class AmbientLight extends Light {

    /**
     * A pre-built ambient light with no intensity (black).
     * Use this as the default when no ambient light is desired.
     */
    public static final AmbientLight NONE = new AmbientLight(Color.BLACK);

    /**
     * Constructs an ambient light with the given color intensity.
     *
     * @param intensity the color representing the ambient light's intensity
     */
    public AmbientLight(Color intensity) {
        super(intensity);
    }
}

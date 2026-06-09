package lighting;

import primitives.Color;

/**
 * Base class for all light sources in the scene.
 * <p>
 * Stores the original intensity of the light source.
 * Subclasses that represent positional or directional lights should also
 * implement {@link LightSource} to expose distance- and direction-dependent
 * calculations.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
abstract class Light {

    /**
     * The original intensity (color) of this light source.
     */
    protected final Color _intensity;

    /**
     * Constructs a light with the given intensity.
     *
     * @param intensity the original color intensity of the light source
     */
    protected Light(Color intensity) {
        _intensity = intensity;
    }

    /**
     * Returns the original intensity of this light source.
     *
     * @return the intensity color
     */
    public Color getIntensity() {
        return _intensity;
    }
}

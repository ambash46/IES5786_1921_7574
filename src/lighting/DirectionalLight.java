package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a directional light source — an infinitely distant light that
 * illuminates the scene from a fixed direction (e.g. the sun).
 * <p>
 * The intensity does not attenuate with distance.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public final class DirectionalLight extends Light implements LightSource {

    /** The normalized direction of the incoming light. */
    private final Vector _direction;

    /**
     * Constructs a directional light with the given intensity and direction.
     *
     * @param intensity the color intensity of the light source
     * @param direction the direction of the incoming light (will be normalized)
     */
    public DirectionalLight(Color intensity, Vector direction) {
        super(intensity);
        _direction = direction.normalize();
    }

    @Override
    public Vector getL(Point p) {
        return _direction;
    }

    @Override
    public Color getIntensity(Point p) {
        return _intensity;
    }

    @Override
    public double getDistance(Point point) {
        return Double.POSITIVE_INFINITY;
    }
}

package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a light source that illuminates points in the scene.
 * <p>
 * Unlike {@link Light#getIntensity()}, the methods here are point-dependent:
 * they account for the propagation of light from the source to a specific
 * point in the scene.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public interface LightSource {

    /**
     * Returns the normalized direction vector from this light source toward the
     * given point.
     *
     * @param p the illuminated point
     * @return unit vector pointing from the light source to {@code p}
     */
    Vector getL(Point p);

    /**
     * Returns the intensity of the light arriving at the given point from this
     * light source.
     *
     * @param p the illuminated point
     * @return the color intensity at {@code p}
     */
    Color getIntensity(Point p);

    /**
     * Returns the distance from this light source to the given point.
     * Directional lights return {@link Double#POSITIVE_INFINITY}.
     *
     * @param point the illuminated point
     * @return distance from the light source to {@code point}
     */
    double getDistance(Point point);
}

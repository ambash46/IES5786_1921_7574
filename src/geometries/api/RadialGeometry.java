package geometries.api;

import primitives.Util;

/**
 * Represents a geometric object defined by a radius.
 * <p>
 * This abstract base class stores the radius and its square for radial
 * geometries such as spheres, tubes, and cylinders.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public abstract class RadialGeometry extends Geometry {
    /**
     * The radius of the geometry.
     */
    protected final double _radius;
    /**
     * The squared radius of the geometry.
     */
    protected final double _radiusSquared;

    /**
     * Constructs a radial geometry with the given radius.
     *
     * @param radius the radius of the geometry
     * @throws IllegalArgumentException if the radius is zero
     */
    public RadialGeometry(double radius) {
        this._radius = radius;
        this._radiusSquared = radius * radius;
        if (Util.isZero(radius) || radius < 0)
            throw new IllegalArgumentException("Radius must be positive");
    }
}

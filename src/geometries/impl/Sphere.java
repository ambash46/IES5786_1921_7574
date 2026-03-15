package geometries.impl;

import geometries.api.RadialGeometry;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a sphere in a three-dimensional Cartesian coordinate system.
 * <p>
 * A sphere is defined by a center point and a radius.
 * </p>
 * @author Ambash and Elyasaf
 */
public final class Sphere extends RadialGeometry {
    /** The center point of the sphere. */
    private final Point _center;

    /**
     * Constructs a sphere from a center point and a radius.
     * @param center the center of the sphere
     * @param radius the radius of the sphere
     */
    public Sphere(Point center, double radius) {
        super(radius);
        this._center = center;

    }

    /**
     * Returns the normal vector of the sphere at the given point.
     * @param point a point on the sphere
     * @return the normal vector at the given point
     */
    @Override
    public final Vector getNormal(Point point) {
        return null;
    }
}

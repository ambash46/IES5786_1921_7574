package geometries.api;

import primitives.Point;
import primitives.Vector;

/**
 * Represents a geometric object in three-dimensional space.
 * <p>
 * A geometry can provide a normal vector at a given point on its surface.
 * </p>
 * @author Ambash and Elyasaf
 */
public abstract class Geometry extends Intersectable {
    /**
     * Returns the normal vector to the geometry at a given point.
     * @param point a point on the geometry
     * @return the normal vector at the given point
     */
    public abstract Vector getNormal(Point point);
}

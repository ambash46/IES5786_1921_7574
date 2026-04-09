package geometries.api;

import java.util.List;
import primitives.Point;
import primitives.Ray;

/**
 * Represents a geometric object that can be intersected by a ray.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("SpellCheckingInspection")
public abstract class Intersectable {
    /**
     * Finds all intersection points between this object and the given ray.
     *
     * @param ray the ray to intersect with
     * @return a list of intersection points
     */
    public abstract List<Point> findIntersections(Ray ray);
}

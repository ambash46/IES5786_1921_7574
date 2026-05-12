package geometries.api;

import java.util.List;
import primitives.Point;
import primitives.Ray;

/**
 * Represents a geometric object that can be intersected by a ray.
 *
 * @author Ambash and Elyasaf
 */

public abstract class Intersectable {

    /**
     * Default constructor for use by subclasses.
     */
    protected Intersectable() { /* no-op */ }

    /**
     * Implementation hook for {@link #calcIntersections(Ray)}.
     * Subclasses must override this method to provide their intersection logic.
     *
     * @param ray the ray to intersect with
     * @return a list of {@link Intersection}s, or {@code null} if there are none
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray);

    /**
     * Finds all intersections between this object and the given ray, returning
     * each hit together with the geometry it belongs to.
     *
     * @param ray the ray to intersect with
     * @return a list of {@link Intersection}s, or {@code null} if there are none
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersectionsHelper(ray);
    }

    /**
     * Finds all intersection points between this object and the given ray.
     *
     * @param ray the ray to intersect with
     * @return a list of intersection points, or {@code null} if there are no
     * intersections
     */
    public final List<Point> findIntersections(Ray ray) {
        var intersections = calcIntersections(ray);
        return intersections == null ? null
                : intersections.stream()
                        .map(intersection -> intersection.point)
                        .toList();
    }

    /**
     * A pairing of an intersection point with the geometry it belongs to.
     * <p>
     * Plain Data Structure: all fields are {@code public final}.
     * </p>
     */
    public static final class Intersection {

        /**
         * The geometry that was intersected.
         */
        public final Geometry geometry;

        /**
         * The intersection point on the geometry's surface.
         */
        public final Point point;

        /**
         * Constructs an Intersection.
         *
         * @param geometry the intersected geometry
         * @param point    the intersection point
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point = point;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Intersection other = (Intersection) obj;
            return geometry == other.geometry && point.equals(other.point);
        }

        @Override
        public String toString() {
            return "Intersection{geometry=" + geometry + ", point=" + point + "}";
        }
    }
}

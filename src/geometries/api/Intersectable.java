package geometries.api;

import java.util.List;
import lighting.LightSource;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents a geometric object that can be intersected by a ray.
 *
 * @author Ambash and Elyasaf
 */

public abstract class Intersectable {

    /** Cached bounding box — computed lazily on first call to {@link #getBoundingBox()}. */
    private AABB _box = null;

    /**
     * Default constructor for use by subclasses.
     */
    protected Intersectable() { /* no-op */ }

    /**
     * Returns the conservative bounding box, computing and caching it on the
     * first call. Infinite objects return {@code null} once and always.
     *
     * @return the bounding box, or {@code null} if this object is infinite
     */
    public final AABB getBoundingBox() {
        if (_box == null) _box = calcBoundingBox();
        return _box;
    }

    /**
     * Computes the bounding box for this object.
     * Called at most once per instance; the result is cached by {@link #getBoundingBox()}.
     * Infinite objects (planes, tubes) must return {@code null}.
     */
    protected abstract AABB calcBoundingBox();

    /**
     * Implementation hook for {@link #calcIntersections(Ray, double)}.
     * Subclasses must override this method to provide their intersection logic,
     * returning only intersections at distance ≤ {@code maxDistance} from the
     * ray origin.
     *
     * @param ray         the ray to intersect with
     * @param maxDistance upper bound on intersection distance (exclusive of points
     *                    beyond this distance)
     * @return a list of {@link Intersection}s within range, or {@code null} if none
     */
    protected abstract List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance);

    /**
     * Finds all intersections between this object and the given ray, returning
     * each hit together with the geometry it belongs to.
     *
     * @param ray the ray to intersect with
     * @return a list of {@link Intersection}s, or {@code null} if there are none
     */
    public final List<Intersection> calcIntersections(Ray ray) {
        return calcIntersections(ray, Double.POSITIVE_INFINITY);
    }

    /**
     * Finds all intersections between this object and the given ray that are
     * within {@code maxDistance} from the ray origin.
     * <p>
     * Always pre-checks the bounding box first: if the ray misses it, the
     * (potentially expensive) exact intersection calculation is skipped
     * entirely. Infinite objects (null bounding box) always fall through to
     * the exact calculation.
     *
     * @param ray         the ray to intersect with
     * @param maxDistance upper bound on intersection distance
     * @return a list of {@link Intersection}s within range, or {@code null} if none
     */
    public final List<Intersection> calcIntersections(Ray ray, double maxDistance) {
        AABB box = getBoundingBox(); // cached — computed only once
        if (box != null && !box.intersects(ray)) return null;
        return calcIntersectionsHelper(ray, maxDistance);
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
         * The material of the intersected geometry.
         */
        public final Material material;

        /** The surface normal at the intersection point. */
        public Vector      n;
        /** The direction of the incoming ray (toward the surface). */
        public Vector      v;
        /** Dot product of {@link #v} and {@link #n} — used to check ray-surface angle. */
        public double      vn;
        /** The active light source being evaluated. */
        public LightSource light;
        /** Direction from the light source to the intersection point. */
        public Vector      l;
        /** Dot product of {@link #l} and {@link #n} — used to check light-surface angle. */
        public double      ln;

        /**
         * Constructs an Intersection.
         * The material is taken from the geometry; if geometry is {@code null}
         * (e.g. when converting a plain Point in findClosestPoint), a default
         * material is used.
         *
         * @param geometry the intersected geometry
         * @param point    the intersection point
         */
        public Intersection(Geometry geometry, Point point) {
            this.geometry = geometry;
            this.point    = point;
            this.material = geometry != null ? geometry.getMaterial() : new Material();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Intersection other = (Intersection) obj;
            return geometry == other.geometry && point.equals(other.point);
        }

        /**
         * Mirrors {@link #equals(Object)}: {@code geometry} is compared there by
         * reference ({@code ==}), not by {@link Geometry#equals(Object)}, so it
         * is hashed via {@link System#identityHashCode(Object)} here rather than
         * {@code geometry.hashCode()} (which would be value-based and could
         * collide with unrelated {@code Intersection}s on a different geometry
         * instance that merely has the same value). {@code point} is compared
         * and hashed by value on both sides, consistently.
         */
        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(geometry) + point.hashCode();
        }

        @Override
        public String toString() {
            return "Intersection{geometry=" + geometry + ", point=" + point + "}";
        }
    }
}

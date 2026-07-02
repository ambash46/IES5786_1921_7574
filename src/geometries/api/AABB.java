package geometries.api;

import static primitives.Util.isZero;

import primitives.Double3;
import primitives.Point;
import primitives.Ray;

/**
 * Axis-Aligned Bounding Box — a conservative enclosing region whose edges
 * are parallel to the coordinate axes, defined by its minimum and maximum corners.
 *
 * <p>Used for early-rejection in CBR: if a ray misses the box it cannot
 * intersect the enclosed geometry, so the expensive intersection calculation
 * is skipped entirely.
 *
 * @author Ambash and Elyasaf
 */
public class AABB {

    /** Corner with the minimum x, y, z values. */
    private final Point _min;
    /** Corner with the maximum x, y, z values. */
    private final Point _max;

    /**
     * Constructs an AABB from its minimum and maximum corner points.
     *
     * @param min the corner with minimum x, y, z values
     * @param max the corner with maximum x, y, z values
     */
    public AABB(Point min, Point max) {
        _min = min;
        _max = max;
    }

    /**
     * Returns the smallest AABB that contains both this box and {@code other}.
     *
     * @param other the box to merge with
     * @return the union bounding box
     */
    public AABB union(AABB other) {
        Double3 a = _min.getCoordinates(), b = _max.getCoordinates();
        Double3 c = other._min.getCoordinates(), d = other._max.getCoordinates();
        return new AABB(
                new Point(Math.min(a._d1(), c._d1()), Math.min(a._d2(), c._d2()), Math.min(a._d3(), c._d3())),
                new Point(Math.max(b._d1(), d._d1()), Math.max(b._d2(), d._d2()), Math.max(b._d3(), d._d3()))
        );
    }

    /**
     * Returns the length of this box along the given axis (max − min).
     *
     * @param axis 0=X, 1=Y, 2=Z
     * @return size along that axis
     */
    public double size(int axis) {
        Double3 min = _min.getCoordinates();
        Double3 max = _max.getCoordinates();
        return switch (axis) {
            case 0 -> max._d1() - min._d1();
            case 1 -> max._d2() - min._d2();
            default -> max._d3() - min._d3();
        };
    }

    /**
     * Returns the midpoint coordinate of this box along the given axis.
     * Used as the centroid estimate when building a BVH.
     *
     * @param axis 0=X, 1=Y, 2=Z
     * @return mid value along that axis
     */
    public double midpoint(int axis) {
        Double3 min = _min.getCoordinates();
        Double3 max = _max.getCoordinates();
        return switch (axis) {
            case 0 -> (min._d1() + max._d1()) / 2;
            case 1 -> (min._d2() + max._d2()) / 2;
            default -> (min._d3() + max._d3()) / 2;
        };
    }

    /**
     * Builds the tightest AABB that encloses all the given points.
     *
     * @param  points one or more points to enclose
     * @return        the bounding box
     * @throws IllegalArgumentException if no points are given (there would be
     *                                  no valid box to compute — without this
     *                                  check, {@code min} and {@code max}
     *                                  would silently come out inverted)
     */
    public static AABB fromPoints(Point... points) {
        if (points.length == 0)
            throw new IllegalArgumentException("Cannot build an AABB from zero points");
        double minX = Double.POSITIVE_INFINITY,  minY = Double.POSITIVE_INFINITY,  minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY,  maxY = Double.NEGATIVE_INFINITY,  maxZ = Double.NEGATIVE_INFINITY;
        for (Point p : points) {
            Double3 c = p.getCoordinates();
            minX = Math.min(minX, c._d1()); maxX = Math.max(maxX, c._d1());
            minY = Math.min(minY, c._d2()); maxY = Math.max(maxY, c._d2());
            minZ = Math.min(minZ, c._d3()); maxZ = Math.max(maxZ, c._d3());
        }
        return new AABB(new Point(minX, minY, minZ), new Point(maxX, maxY, maxZ));
    }

    /**
     * Tests whether {@code ray} intersects this box using the slab method.
     * No intersection point is computed — this is a boolean test only.
     *
     * @param ray the ray to test
     * @return {@code true} if the ray hits the box, {@code false} if it misses
     */
    public boolean intersects(Ray ray) {
        Double3 o   = ray.origin().getCoordinates();
        Double3 dir = ray.direction().getCoordinates();
        Double3 min = _min.getCoordinates();
        Double3 max = _max.getCoordinates();

        double tMin = Double.NEGATIVE_INFINITY;
        double tMax = Double.POSITIVE_INFINITY;

        // X slab
        if (!isZero(dir._d1())) {
            double t1 = (min._d1() - o._d1()) / dir._d1();
            double t2 = (max._d1() - o._d1()) / dir._d1();
            tMin = Math.max(tMin, Math.min(t1, t2));
            tMax = Math.min(tMax, Math.max(t1, t2));
        } else if (o._d1() < min._d1() || o._d1() > max._d1()) return false;

        // Y slab
        if (!isZero(dir._d2())) {
            double t1 = (min._d2() - o._d2()) / dir._d2();
            double t2 = (max._d2() - o._d2()) / dir._d2();
            tMin = Math.max(tMin, Math.min(t1, t2));
            tMax = Math.min(tMax, Math.max(t1, t2));
        } else if (o._d2() < min._d2() || o._d2() > max._d2()) return false;

        // Z slab
        if (!isZero(dir._d3())) {
            double t1 = (min._d3() - o._d3()) / dir._d3();
            double t2 = (max._d3() - o._d3()) / dir._d3();
            tMin = Math.max(tMin, Math.min(t1, t2));
            tMax = Math.min(tMax, Math.max(t1, t2));
        } else if (o._d3() < min._d3() || o._d3() > max._d3()) return false;

        return tMax >= tMin && tMax >= 0;
    }
}

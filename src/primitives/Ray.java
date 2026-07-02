package primitives;

import static geometries.api.Intersectable.Intersection;
import static primitives.Util.isZero;

import java.util.List;
import java.util.Objects;

/**
 * Represents a ray in a three-dimensional Cartesian coordinate system.
 * <p>
 * A ray is defined by an origin point and a direction vector.
 * The stored direction is normalized.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public final class Ray {

    /**
     * Small offset used to push secondary-ray origins off the surface, to
     * avoid self-intersection (shadow/reflection/transparency acne).
     * <p>
     * Lowered from the original {@code 0.1} after an empirical sweep (sphere
     * + triangle + floor scene, high-sample soft shadows, pixel-diffed
     * against the {@code 0.1} baseline): the diff count plateaued at a
     * constant value confined to the shadow-penumbra edge for every DELTA
     * from {@code 1e-4} down to {@code 1e-8}, with no growth (i.e. no
     * accumulating acne) and no visible artifacts. If acne reappears in some
     * scene this wasn't tested against, raise this value back up.
     * <p>
     * Package-private (not {@code private}) so {@code RayTests} can assert
     * against it directly instead of duplicating the value — a class outside
     * {@code primitives} still can't see it.
     */
    static final double DELTA = 0.00000001;

    /**
     * The starting point of the ray.
     */
    private final Point _origin;
    /**
     * The normalized direction of the ray.
     */
    private final Vector _direction;

    /**
     * Constructs a ray from an origin point and a direction vector.
     *
     * @param origin    the starting point of the ray
     * @param direction the direction of the ray
     */
    public Ray(Point origin, Vector direction) {
        _origin = origin;
        _direction = direction.normalize();
    }

    /**
     * Constructs a ray with its origin offset along {@code normal} to avoid
     * self-intersection (shadow/reflection/transparency acne).
     * <p>
     * The origin is moved by {@link #DELTA} in the direction of {@code normal}
     * when {@code direction · normal > 0}, and in the opposite direction otherwise.
     * </p>
     *
     * @param origin    the surface point
     * @param direction the desired ray direction
     * @param normal    the surface normal at {@code origin}
     */
    public Ray(Point origin, Vector direction, Vector normal) {
        double dn = direction.dotProduct(normal);
        _origin    = origin.add(normal.scale(dn > 0 ? DELTA : -DELTA));
        _direction = direction.normalize();
    }

    /**
     * Returns the normalized direction vector of the ray.
     *
     * @return the ray direction
     */
    public Vector direction() {
        return _direction;
    }

    /**
     * Returns the origin point of the ray.
     *
     * @return the starting point of the ray
     */
    public Point origin() {
        return _origin;
    }

    /**
     * Returns the point on the ray at parameter {@code t}.
     * <p>
     * The point is computed as {@code origin + t * direction}.
     * When {@code t} is zero, the origin itself is returned — checked
     * explicitly up front rather than inferred from a zero-vector exception.
     * The surrounding {@code try/catch} is a defensive fallback, not the
     * detection mechanism: it only guards against an unanticipated edge case
     * still producing a zero-length scaled direction, so this method never
     * throws for any value of {@code t} (positive, negative, or zero).
     * </p>
     *
     * @param t the scalar parameter along the ray direction
     * @return the point {@code origin + t * direction}, or the origin when
     * {@code t} is effectively zero
     */
    public Point getPoint(double t) {
        if (isZero(t)) return _origin;
        try {
            return _origin.add(_direction.scale(t));
        } catch (IllegalArgumentException e) {
            return _origin;
        }
    }

    /**
     * Finds the intersection in the given list whose point is closest to the
     * ray's origin.
     * <p>
     * Uses squared distances to avoid unnecessary square-root computations.
     * </p>
     *
     * @param intersections a list of candidate intersections, or {@code null}
     * @return the intersection closest to the origin, or {@code null} if the
     *         list is {@code null} or empty
     */
    public Intersection findClosestIntersection(List<Intersection> intersections) {
        if (intersections == null) return null;
        Intersection closest = null;
        double minDistSq = Double.POSITIVE_INFINITY;
        for (Intersection i : intersections) {
            double distSq = i.point.distanceSquared(_origin);
            if (distSq < minDistSq) {
                minDistSq = distSq;
                closest = i;
            }
        }
        return closest;
    }

    /**
     * Finds the point in the given list that is closest to the ray's origin.
     *
     * @param points a list of candidate points, or {@code null}
     * @return the point closest to the origin, or {@code null} if {@code points}
     *         is {@code null}
     */
    public Point findClosestPoint(List<Point> points) {
        return points == null ? null
                : findClosestIntersection(
                        points.stream()
                                .map(point -> new Intersection(null, point))
                                .toList()
                ).point;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ray other = (Ray) obj;
        return Objects.equals(_origin, other._origin)
                && Objects.equals(_direction, other._direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }

    @Override
    public String toString() {
        return "Ray{origin=" + _origin + ", direction=" + _direction + "}";
    }
}

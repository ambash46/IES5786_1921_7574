package primitives;

import java.util.Objects;

import static primitives.Util.isZero;

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
     * Only positive values of {@code t} represent points in front of the ray origin;
     * this method accepts any value of {@code t} (positive, negative, or zero).
     * </p>
     *
     * @param t the scalar parameter along the ray direction
     * @return the point {@code origin + t * direction}
     */
    public Point getPoint(double t) {
        return isZero(t) ? _origin : _origin.add(_direction.scale(t));
    }

    /**
     * Compares this ray with another object.
     *
     * @param obj the object to compare with
     * @return {@code true} if the other object is a ray with equal origin and
     * direction
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ray other = (Ray) obj;
        return Objects.equals(_origin, other._origin)
                && Objects.equals(_direction, other._direction);
    }

    /**
     * Returns a hash code for this ray.
     *
     * @return the hash code of the origin and direction
     */
    @Override
    public int hashCode() {
        return Objects.hash(_origin, _direction);
    }

    /**
     * Returns a string representation of this ray.
     *
     * @return the ray origin and direction
     */
    @Override
    public String toString() {
        return "Ray{origin=" + _origin + ", direction=" + _direction + "}";
    }
}

package primitives;

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
        this._origin = origin;
        this._direction = direction.normalize();
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

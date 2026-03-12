package primitives;

/**
 * Represents a ray in a three-dimensional Cartesian coordinate system.
 * <p>
 * A ray is defined by an origin point and a direction vector.
 * The stored direction is normalized.
 * </p>
 * @author Ambash and Elyasaf
 */
public class Ray {
    /** The starting point of the ray. */
    public final Point _origin;
    /** The normalized direction of the ray. */
    public final Vector _direction;

    /**
     * Constructs a ray from an origin point and a direction vector.
     * @param origin the starting point of the ray
     * @param direction the direction of the ray
     */
    public Ray(Point origin, Vector direction) {
        this._origin = origin;
        this._direction = direction.normalize();
    }


}

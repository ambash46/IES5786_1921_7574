package geometries.impl;

import geometries.api.RadialGeometry;
import java.util.Objects;
import primitives.Point;
import primitives.Vector;

import static primitives.Util.isZero;

/**
 * Represents a sphere in a three-dimensional Cartesian coordinate system.
 * <p>
 * A sphere is defined by a center point and a radius.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public final class Sphere extends RadialGeometry {
    /**
     * The center point of the sphere.
     */
    private final Point _center;

    /**
     * Constructs a sphere from a center point and a radius.
     *
     * @param center the center of the sphere
     * @param radius the radius of the sphere
     */
    public Sphere(Point center, double radius) {
        super(radius);
        this._center = center;

    }

    /**
     * Returns the normal vector of the sphere at the given point.
     *
     * @param point a point on the sphere
     * @return the normal vector at the given point
     */
    @Override
    public Vector getNormal(Point point) {
        return point.subtract(_center).normalize();
    }

    /**
     * Compares this sphere with another object.
     *
     * @param obj the object to compare with
     * @return {@code true} if the other object is a sphere with equal center
     * and radius
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sphere other = (Sphere) obj;
        return isZero(other._radius - _radius)
                && Objects.equals(_center, other._center);
    }

    /**
     * Returns a hash code for this sphere.
     *
     * @return the hash code of the center and radius
     */
    @Override
    public int hashCode() {
        return Objects.hash(_center, _radius);
    }

    /**
     * Returns a string representation of this sphere.
     *
     * @return the sphere center and radius
     */
    @Override
    public String toString() {
        return "Sphere{center=" + _center + ", radius=" + _radius + "}";
    }
}

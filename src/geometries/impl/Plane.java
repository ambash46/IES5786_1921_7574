package geometries.impl;

import geometries.api.Geometry;
import java.util.Objects;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a plane in a three-dimensional Cartesian coordinate system.
 * <p>
 * This geometry can be defined either by three points on the plane or by a
 * point and a normal vector.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public final class Plane extends Geometry {
    /**
     * A point belonging to the plane.
     */
    private final Point _point;
    /**
     * The normal vector of the plane.
     */
    private final Vector _normal;

    /**
     * Constructs a plane from three points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @param p3 the third point
     */
    public Plane(Point p1, Point p2, Point p3) {
        _point = p1;
        Vector u = p2.subtract(p1);
        Vector v = p3.subtract(p1);
        _normal = u.crossProduct(v).normalize();
    }

    /**
     * Constructs a plane from a point and a normal vector.
     *
     * @param point  a point on the plane
     * @param normal a vector normal to the plane
     */
    public Plane(Point point, Vector normal) {
        this._point = point;
        this._normal = normal.normalize();
    }

    /**
     * Returns the normal vector of the plane.
     *
     * @param point a point on the plane
     * @return the normalized normal vector of the plane
     */
    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Plane other = (Plane) obj;
        return Objects.equals(_point, other._point)
                && Objects.equals(_normal, other._normal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_point, _normal);
    }

    @Override
    public String toString() {
        return "Plane{point=" + _point + ", normal=" + _normal + "}";
    }
}

package geometries.impl;

import static geometries.api.Intersectable.Intersection;

import geometries.api.Geometry;
import java.util.List;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

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
     * @throws IllegalArgumentException if the points are not distinct or do
     *                                  not define a unique plane
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
     * @throws IllegalArgumentException if {@code normal} is the zero vector
     */
    public Plane(Point point, Vector normal) {
        this._point = point;
        this._normal = normal.normalize();
    }

    @Override
    public Vector getNormal(Point point) {
        return _normal;
    }

    /* Returns null when n·v=0 (parallel), when origin is on the plane, t<=0, or t>maxDistance. */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        Point p0 = ray.origin();
        Vector v = ray.direction();

        if (_point.equals(p0)) return null;

        double nv = _normal.dotProduct(v);
        if (isZero(nv)) return null;

        double t = alignZero(_normal.dotProduct(_point.subtract(p0)) / nv);
        return (t <= 0 || alignZero(maxDistance - t) < 0) ? null : List.of(new Intersection(this, ray.getPoint(t)));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Plane other = (Plane) obj;
        if (!_normal.isParallel(other._normal)) return false;
        if (_point.equals(other._point)) return true;
        return isZero(_normal.dotProduct(other._point.subtract(_point)));
    }

    @Override
    public int hashCode() {
        return Objects.hash(_normal, _point);
    }

    @Override
    public String toString() {
        return "Plane{point=" + _point + ", normal=" + _normal + "}";
    }
}

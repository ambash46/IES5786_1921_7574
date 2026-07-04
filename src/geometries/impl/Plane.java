package geometries.impl;

import geometries.api.Geometry;
import java.util.List;
import java.util.Objects;
import primitives.Double3;
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

    @Override
    protected geometries.api.AABB calcBoundingBox() { return null; }

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

    /**
     * Returns this plane's normal oriented to a canonical sign so that two
     * planes whose normals point in opposite directions — both accepted as
     * equal by {@link #equals(Object)} via {@link Vector#isParallel(Vector)}
     * — produce the same canonical form for hashing.
     *
     * @return the normal, negated if needed so its first non-zero component is positive
     */
    private Vector canonicalNormal() {
        Double3 c = _normal.getCoordinates();
        boolean negate = !isZero(c._d1()) ? c._d1() < 0
                : !isZero(c._d2()) ? c._d2() < 0
                : c._d3() < 0;
        return negate ? _normal.scale(-1) : _normal;
    }

    /**
     * Signed offset of {@code point} along {@code normal} from the coordinate
     * origin, computed via raw {@link Double3} arithmetic (not
     * {@code Point.subtract}) so it stays well-defined even when
     * {@code point} coincides with the origin.
     *
     * @param  normal the (unit) normal to project onto
     * @param  point  the point to project
     * @return        {@code normal · point}
     */
    private static double planeOffset(Vector normal, Point point) {
        Double3 n = normal.getCoordinates();
        Double3 p = point.getCoordinates();
        return n._d1() * p._d1() + n._d2() * p._d2() + n._d3() * p._d3();
    }

    @Override
    public int hashCode() {
        Vector normal = canonicalNormal();
        Double3 n = normal.getCoordinates();
        double offset = planeOffset(normal, _point);
        return Objects.hash(
                Double3.quantize(n._d1()), Double3.quantize(n._d2()), Double3.quantize(n._d3()),
                Double3.quantize(offset));
    }

    @Override
    public String toString() {
        return "Plane{point=" + _point + ", normal=" + _normal + "}";
    }
}

package geometries.impl;

import geometries.api.RadialGeometry;
import java.util.List;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
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
     * Finds all intersection points between the sphere and the given ray.
     * <p>
     * The method projects the vector from the ray origin to the sphere center
     * onto the ray direction. This gives {@code tm}, the signed distance from
     * the ray origin to the closest point on the ray to the center.
     * From that projection we compute {@code d^2}, the squared distance from
     * the center to the ray. If {@code d} is greater than or equal to the
     * sphere radius, the ray misses the sphere or is tangent to it, and no
     * intersection is returned.
     * </p>
     * <p>
     * Otherwise, the half-chord length {@code th} is computed and the two
     * candidate ray parameters are {@code t1 = tm - th} and
     * {@code t2 = tm + th}. Only positive {@code t} values are accepted,
     * because intersections behind the ray origin or exactly at the origin are
     * not considered valid ray intersections.
     * </p>
     *
     * @param ray the ray to intersect with
     * @return the intersection points, or {@code null} if there is no intersection
     */
    @Override
    public List<Point> findIntersections(Ray ray) {
        Point p0 = ray.origin();
        Vector v = ray.direction();

        // Special case: if the ray starts at the center, there is exactly one
        // forward intersection at distance radius along the ray direction.
        if (_center.equals(p0)) {
            return List.of(p0.add(v.scale(_radius)));
        }

        Vector u = _center.subtract(p0);
        // tm is the projection of the center vector on the ray direction.
        double tm = alignZero(v.dotProduct(u));
        // d^2 is the squared distance from the sphere center to the ray.
        double dSquared = alignZero(u.lengthSquared() - tm * tm);
        if (dSquared > _radiusSquared || isZero(dSquared - _radiusSquared)) return null;

        // th is half the chord length inside the sphere.
        double th = Math.sqrt(_radiusSquared - dSquared);
        double t1 = alignZero(tm - th);
        double t2 = alignZero(tm + th);

        // Keep only intersections that are strictly in front of the ray origin.
        if (t1 > 0 && t2 > 0) {
            return List.of(ray.getPoint(t1), ray.getPoint(t2));
        }
        if (t1 > 0) {
            return List.of(ray.getPoint(t1));
        }
        if (t2 > 0) {
            return List.of(ray.getPoint(t2));
        }
        return null;
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
                && isZero(_center.distance(other._center));
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

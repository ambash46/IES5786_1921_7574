package geometries.impl;

import static geometries.api.Intersectable.Intersection;

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
     * @throws IllegalArgumentException if the radius is not positive
     */
    public Sphere(Point center, double radius) {
        super(radius);
        this._center = center;
    }

    @Override
    public Vector getNormal(Point point) {
        return point.subtract(_center).normalize();
    }

    /*
     * Projects the ray-to-center vector onto the ray direction (tm), then
     * computes d², the squared distance from the center to the ray.
     * If d >= radius the ray misses or is tangent → null.
     * Otherwise, half-chord th gives t1 = tm-th, t2 = tm+th; only t > 0 kept.
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        Point p0 = ray.origin();
        Vector v = ray.direction();

        // Special case: if the ray starts at the center, there is exactly one
        // forward intersection at distance radius along the ray direction.
        if (_center.equals(p0)) {
            return List.of(new Intersection(this, p0.add(v.scale(_radius))));
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
            return List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)));
        }
        if (t1 > 0) {
            return List.of(new Intersection(this, ray.getPoint(t1)));
        }
        if (t2 > 0) {
            return List.of(new Intersection(this, ray.getPoint(t2)));
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Sphere other = (Sphere) obj;
        return isZero(other._radius - _radius)
                && isZero(_center.distance(other._center));
    }

    @Override
    public int hashCode() {
        return Objects.hash(_center, _radius);
    }

    @Override
    public String toString() {
        return "Sphere{center=" + _center + ", radius=" + _radius + "}";
    }
}

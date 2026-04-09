package geometries.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a finite cylinder in a three-dimensional Cartesian coordinate
 * system.
 * <p>
 * A cylinder is defined by an axis ray, a radius, and a height.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public final class Cylinder extends Tube {
    /**
     * Small step used to test whether a candidate point is a true entry/exit
     * of the finite solid rather than a tangent touch or an overlap case.
     */
    private static final double INTERIOR_EPS = 1e-7;

    /**
     * The height of the cylinder.
     */
    private final double _height;

    /**
     * Constructs a cylinder from a radius, an axis ray, and a height.
     *
     * @param radius the radius of the cylinder
     * @param axis   the axis ray of the cylinder
     * @param height the height of the cylinder
     * @throws IllegalArgumentException if the height is zero
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        this._height = height;
        if (Util.isZero(height) || height < 0)
            throw new IllegalArgumentException("Height must be positive");
    }

    /**
     * Returns the normal vector of the cylinder at the given point.
     *
     * @param point a point on the cylinder
     * @return the normal vector at the given point
     */
    @Override
    public Vector getNormal(Point point) {
        Point origin = _axis.origin();
        Vector direction = _axis.direction();
        if (point.equals(origin))
            return direction.scale(-1);

        double projection = direction.dotProduct(point.subtract(origin));

        if (isZero(projection)) {
            return direction.scale(-1);
        }

        if (isZero(projection - _height)) {
            return direction;
        }
        return super.getNormal(point);
    }

    /**
     * Finds all intersection points between the cylinder and the given ray.
     * <p>
     * The finite cylinder is treated as the closed solid bounded by:
     * the infinite tube mantle and the two cap disks. The method first gathers
     * all boundary candidates from the mantle and from the cap planes. It then
     * keeps only candidates where the ray truly crosses the cylinder interior.
     * This rejects tangency and overlap cases, which do not create discrete
     * intersection points in the tested convention.
     * </p>
     *
     * @param ray the ray to intersect with
     * @return the intersection points, or {@code null} if there is no
     * intersection
     */
    @Override
    public List<Point> findIntersections(Ray ray) {
        List<Double> candidateParameters = new ArrayList<>();

        // Mantle candidates come from the infinite tube.
        List<Point> tubeIntersections = super.findIntersections(ray);
        if (tubeIntersections != null) {
            for (Point point : tubeIntersections) {
                addCandidate(candidateParameters, rayParameter(ray, point));
            }
        }

        // Cap candidates are the ray hits with the bottom and top cap planes.
        Point rayOrigin = ray.origin();
        Vector rayDirection = ray.direction();
        double originProjection = axisProjection(rayOrigin);
        double directionProjection = alignZero(_axis.direction().dotProduct(rayDirection));

        if (!isZero(directionProjection)) {
            addCandidate(candidateParameters, alignZero(-originProjection / directionProjection));
            addCandidate(candidateParameters, alignZero((_height - originProjection) / directionProjection));
        }

        if (candidateParameters.isEmpty()) return null;

        candidateParameters.sort(Double::compare);
        List<Point> intersections = new ArrayList<>();

        for (double t : candidateParameters) {
            Point point = rayPoint(ray, t);
            if (!isOnCylinderBoundary(point)) continue;
            if (crossesCylinderInterior(ray, t)) {
                addIntersection(intersections, point);
            }
        }

        return intersections.isEmpty() ? null : intersections;
    }

    /**
     * Adds a positive candidate parameter unless an equivalent candidate is
     * already present.
     *
     * @param candidates list of candidate parameters
     * @param t          candidate ray parameter
     */
    private static void addCandidate(List<Double> candidates, double t) {
        if (t <= 0) return;
        for (double existing : candidates) {
            if (isZero(existing - t)) return;
        }
        candidates.add(t);
    }

    /**
     * Adds an intersection point unless an equivalent point is already present.
     *
     * @param intersections current intersection list
     * @param point         intersection point to add
     */
    private static void addIntersection(List<Point> intersections, Point point) {
        for (Point existing : intersections) {
            if (isZero(existing.distance(point))) return;
        }
        intersections.add(point);
    }

    /**
     * Returns the parameter of a point along the given ray.
     *
     * @param ray   the ray
     * @param point a point on the ray
     * @return the ray parameter of the point
     */
    private static double rayParameter(Ray ray, Point point) {
        return point.equals(ray.origin())
                ? 0d
                : alignZero(ray.direction().dotProduct(point.subtract(ray.origin())));
    }

    /**
     * Returns the point at the given ray parameter.
     *
     * @param ray the ray
     * @param t   the parameter
     * @return the point on the ray at parameter {@code t}
     */
    private static Point rayPoint(Ray ray, double t) {
        return ray.getPoint(t);
    }

    /**
     * Returns the signed projection of a point on the cylinder axis, measured
     * from the bottom-cap center.
     *
     * @param point the point to project
     * @return the axis projection
     */
    private double axisProjection(Point point) {
        Point axisOrigin = _axis.origin();
        return point.equals(axisOrigin)
                ? 0d
                : alignZero(_axis.direction().dotProduct(point.subtract(axisOrigin)));
    }

    /**
     * Returns the point on the cylinder axis at the given projection value.
     *
     * @param projection signed distance along the axis from the bottom-cap
     *                   center
     * @return the axis point at that projection
     */
    private Point axisPoint(double projection) {
        return _axis.getPoint(projection);
    }

    /**
     * Checks whether the given point lies on the finite cylinder boundary.
     *
     * @param point candidate boundary point
     * @return {@code true} if the point lies on the mantle or on one of the
     * caps, {@code false} otherwise
     */
    private boolean isOnCylinderBoundary(Point point) {
        double projection = axisProjection(point);
        if (projection < 0 || alignZero(projection - _height) > 0) return false;

        double radialDelta = alignZero(point.distanceSquared(axisPoint(projection)) - _radiusSquared);
        boolean onCap = isZero(projection) || isZero(projection - _height);
        return onCap ? radialDelta <= 0 : isZero(radialDelta);
    }

    /**
     * Checks whether the point at the given parameter lies strictly inside the
     * finite cylinder volume.
     *
     * @param ray the tested ray
     * @param t   the ray parameter
     * @return {@code true} if the point is strictly inside the solid
     */
    private boolean isInsideCylinder(Ray ray, double t) {
        Point point = rayPoint(ray, t);
        double projection = axisProjection(point);
        if (projection <= 0 || alignZero(projection - _height) >= 0) return false;

        double radialDelta = alignZero(point.distanceSquared(axisPoint(projection)) - _radiusSquared);
        return radialDelta < 0;
    }

    /**
     * Checks whether the ray crosses the cylinder interior at the given
     * candidate parameter.
     *
     * @param ray the tested ray
     * @param t   candidate boundary parameter
     * @return {@code true} if the ray enters or exits the cylinder interior at
     * the candidate point
     */
    private boolean crossesCylinderInterior(Ray ray, double t) {
        double offset = Math.min(INTERIOR_EPS, t / 2d);
        boolean insideBefore = isInsideCylinder(ray, t - offset);
        boolean insideAfter = isInsideCylinder(ray, t + offset);
        return insideBefore != insideAfter;
    }

    /**
     * Compares this cylinder with another object.
     *
     * @param obj the object to compare with
     * @return {@code true} if the other object is a cylinder with equal tube
     * data and height
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Cylinder other = (Cylinder) obj;
        return isZero(other._height - _height);
    }

    /**
     * Returns a hash code for this cylinder.
     *
     * @return the hash code of the inherited tube data and height
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _height);
    }

    /**
     * Returns a string representation of this cylinder.
     *
     * @return the cylinder axis, radius and height
     */
    @Override
    public String toString() {
        return "Cylinder{axis=" + _axis + ", radius=" + _radius + ", height=" + _height + "}";
    }
}

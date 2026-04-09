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
 * Represents an infinite tube in a three-dimensional Cartesian coordinate
 * system.
 * <p>
 * A tube is defined by a central axis ray and a radius.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public class Tube extends RadialGeometry {
    /**
     * The axis ray of the tube.
     */
    protected final Ray _axis;

    /**
     * Constructs a tube from a radius and an axis ray.
     *
     * @param radius the radius of the tube
     * @param axis   the axis ray of the tube
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        this._axis = axis;
    }

    /**
     * Returns the normal vector of the tube at the given point.
     *
     * @param point a point on the tube
     * @return the normal vector at the given point
     */
    @Override
    public Vector getNormal(Point point) {
        Point origin = _axis.origin();
        Vector direction = _axis.direction();
        double projection = direction.dotProduct(point.subtract(origin));
        Point axisPoint = isZero(projection) ? origin : origin.add(direction.scale(projection));
        return point.subtract(axisPoint).normalize();
    }

    /**
     * Finds all intersection points between the tube and the given ray.
     * <p>
     * The infinite tube is defined by all points whose perpendicular distance
     * from the axis ray is equal to the tube radius. For the tested ray, the
     * method removes the components parallel to the tube axis from both the ray
     * direction and the vector from the axis origin to the ray origin. This
     * produces a quadratic equation in the ray parameter {@code t}. Positive
     * roots correspond to forward intersections with the tube surface.
     * </p>
     *
     * @param ray the ray to intersect with
     * @return the intersection points, or {@code null} if there is no
     * intersection
     */
    @Override
    public List<Point> findIntersections(Ray ray) {
        Point p0 = ray.origin();
        Point pa = _axis.origin();
        Vector v = ray.direction();
        Vector va = _axis.direction();

        double vVa = v.dotProduct(va);

        if (p0.equals(pa)) {
            double aAtAxis = alignZero(v.dotProduct(v) - vVa * vVa);
            if (isZero(aAtAxis)) return null;
            double t = alignZero(_radius / Math.sqrt(aAtAxis));
            return t <= 0 ? null : List.of(ray.getPoint(t));
        }

        Vector deltaP = p0.subtract(pa);
        double dpVa = deltaP.dotProduct(va);

        double a = alignZero(v.dotProduct(v) - vVa * vVa);
        if (isZero(a)) return null;

        double b = alignZero(2d * (v.dotProduct(deltaP) - vVa * dpVa));
        double c = alignZero(deltaP.dotProduct(deltaP) - dpVa * dpVa - _radiusSquared);

        double discriminant = alignZero(b * b - 4d * a * c);
        if (discriminant <= 0) return null;

        double sqrtDiscriminant = Math.sqrt(discriminant);
        double denominator = 2d * a;
        double t1 = alignZero((-b - sqrtDiscriminant) / denominator);
        double t2 = alignZero((-b + sqrtDiscriminant) / denominator);

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
     * Compares this tube with another object.
     *
     * @param obj the object to compare with
     * @return {@code true} if the other object is a tube with equal axis and
     * radius
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tube other = (Tube) obj;
        return isZero(other._radius - _radius)
                && Objects.equals(_axis, other._axis);
    }

    /**
     * Returns a hash code for this tube.
     *
     * @return the hash code of the axis ray and radius
     */
    @Override
    public int hashCode() {
        return Objects.hash(_axis, _radius);
    }

    /**
     * Returns a string representation of this tube.
     *
     * @return the tube axis and radius
     */
    @Override
    public String toString() {
        return "Tube{axis=" + _axis + ", radius=" + _radius + "}";
    }
}

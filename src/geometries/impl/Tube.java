package geometries.impl;

import geometries.api.RadialGeometry;
import java.util.List;
import java.util.Objects;
import primitives.Double3;
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
     * @throws IllegalArgumentException if the radius is not positive
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        this._axis = axis;
    }

    @Override
    public Vector getNormal(Point point) {
        Vector direction = _axis.direction();
        Point origin = _axis.origin();
        // Guard: point.subtract(origin) would produce the zero vector if the two
        // points coincide, causing an IllegalArgumentException inside the Vector
        // constructor.  A point that equals the axis origin cannot lie on the tube
        // surface (radius > 0), so this is an invalid input.
        if (point.equals(origin))
            throw new IllegalArgumentException(
                    "Point coincides with the tube axis origin and is not on the tube surface");
        double projection = direction.dotProduct(point.subtract(origin));
        return point.subtract(_axis.getPoint(projection)).normalize();
    }

    @Override
    protected geometries.api.AABB calcBoundingBox() { return null; }

    /*
     * Strips axis-parallel components from both the ray direction (v) and the
     * delta vector (ΔP = p0 - pa), then solves the resulting quadratic
     * a·t²+b·t+c=0 for the tube surface. Only t > 0 roots are kept.
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        // p0 = ray origin, pa = axis origin, v = ray direction (unit), va = axis direction (unit)
        Point p0 = ray.origin();
        Point pa = _axis.origin();
        Vector v = ray.direction();
        Vector va = _axis.direction();

        // Scalar projection of the ray direction onto the axis direction.
        // Used to strip the axis-parallel component from v.
        double vVa = v.dotProduct(va);

        // --- Special case: ray origin coincides with the axis origin ---
        // deltaP = p0 - pa would be the zero vector, which is not a valid Vector
        // object. We handle this branch separately to avoid that construction.
        if (p0.equals(pa)) {
            // |v_perp|² = |v|² - (v·va)²  (since v is a unit vector, |v|² = 1)
            // This is the squared magnitude of the component of v that is
            // perpendicular to the axis. If it is zero, v is parallel to the
            // axis and the ray never reaches the tube surface.
            double aAtAxis = alignZero(v.dotProduct(v) - vVa * vVa);
            if (isZero(aAtAxis)) return null;

            // The ray starts exactly on the axis and shoots outward. The tube
            // surface is hit at the single positive parameter:
            //   r² = (t·v_perp)²  =>  t = r / |v_perp|
            double t = alignZero(_radius / Math.sqrt(aAtAxis));
            return (t <= 0 || alignZero(maxDistance - t) < 0) ? null : List.of(new Intersection(this, ray.getPoint(t)));
        }

        // --- General case ---
        // ΔP = p0 - pa  (vector from axis origin to ray origin)
        Vector deltaP = p0.subtract(pa);

        // Scalar projection of ΔP onto the axis direction.
        // Used to strip the axis-parallel component from ΔP.
        double dpVa = deltaP.dotProduct(va);

        // Coefficients of the quadratic  a·t² + b·t + c = 0
        // that results from substituting the ray equation P(t) = p0 + t·v
        // into the tube-surface condition |P_perp - axis_perp|² = r²,
        // where "_perp" denotes the component perpendicular to the axis.
        //
        //   a = |v_perp|²    = v·v  - (v·va)²
        //   b = 2·(v_perp · ΔP_perp)  = 2·[v·ΔP - (v·va)(ΔP·va)]
        //   c = |ΔP_perp|² - r²       = ΔP·ΔP - (ΔP·va)² - r²

        // a = |v_perp|²  (zero iff the ray is parallel to the axis)
        double a = alignZero(v.dotProduct(v) - vVa * vVa);
        if (isZero(a)) return null;   // ray is parallel to the axis → no intersection

        double b = alignZero(2d * (v.dotProduct(deltaP) - vVa * dpVa));
        double c = alignZero(deltaP.dotProduct(deltaP) - dpVa * dpVa - _radiusSquared);

        // discriminant = b² - 4ac
        // > 0  → two distinct intersections
        // = 0  → tangent (one touching point, not counted as an intersection)
        // < 0  → no real intersection
        double discriminant = alignZero(b * b - 4d * a * c);
        if (discriminant <= 0) return null;

        double sqrtDiscriminant = Math.sqrt(discriminant);
        double denominator = 2d * a;

        // t1 ≤ t2 always (t1 uses the minus sign in the quadratic formula)
        double t1 = alignZero((-b - sqrtDiscriminant) / denominator);
        double t2 = alignZero((-b + sqrtDiscriminant) / denominator);

        // Keep only intersections strictly in front of the ray and within maxDistance.
        if (t1 > 0 && t2 > 0) {
            boolean in1 = alignZero(maxDistance - t1) >= 0;
            boolean in2 = alignZero(maxDistance - t2) >= 0;
            if (in1 && in2)  return List.of(new Intersection(this, ray.getPoint(t1)), new Intersection(this, ray.getPoint(t2)));
            if (in1)         return List.of(new Intersection(this, ray.getPoint(t1)));
            if (in2)         return List.of(new Intersection(this, ray.getPoint(t2)));
            return null;
        }
        if (t1 > 0) return alignZero(maxDistance - t1) >= 0 ? List.of(new Intersection(this, ray.getPoint(t1))) : null;
        if (t2 > 0) return alignZero(maxDistance - t2) >= 0 ? List.of(new Intersection(this, ray.getPoint(t2))) : null;
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tube other = (Tube) obj;
        if (!isZero(other._radius - _radius)) return false;
        // Two infinite tubes are equal when their axis *lines* are the same,
        // regardless of which point was chosen as the ray origin or whether the
        // direction is pointing the same way or the opposite.
        // The axis lines are the same when:
        //   (1) the directions are parallel (same or opposite), AND
        //   (2) the vector between the two origins is also parallel to the direction
        //       (i.e. the other origin lies on this tube's axis line).
        Vector dir = _axis.direction();
        if (!dir.isParallel(other._axis.direction())) return false;
        if (_axis.origin().equals(other._axis.origin())) return true;
        return dir.isParallel(other._axis.origin().subtract(_axis.origin()));
    }

    /**
     * Returns the tube's axis direction oriented to a canonical sign, so that
     * two tubes whose directions point oppositely — both accepted as equal by
     * {@link #equals(Object)} — produce the same canonical form for hashing.
     *
     * @return the axis direction, negated if needed so its first non-zero component is positive
     */
    private Vector canonicalDirection() {
        Double3 c = _axis.direction().getCoordinates();
        boolean negate = !isZero(c._d1()) ? c._d1() < 0
                : !isZero(c._d2()) ? c._d2() < 0
                : c._d3() < 0;
        return negate ? _axis.direction().scale(-1) : _axis.direction();
    }

    /**
     * Returns the point on this tube's axis line closest to the coordinate
     * origin — a canonical representative of the line that is the same
     * regardless of which point along the line was chosen as the axis
     * ray's origin, computed via raw {@link Double3} arithmetic so it stays
     * well-defined even when the axis origin coincides with the coordinate
     * origin.
     *
     * @param  direction the canonical (fixed-sign) axis direction
     * @return the foot of the perpendicular from the origin to the axis line
     */
    private Double3 canonicalFoot(Vector direction) {
        Double3 p = _axis.origin().getCoordinates();
        Double3 d = direction.getCoordinates();
        double pDotD = p._d1() * d._d1() + p._d2() * d._d2() + p._d3() * d._d3();
        return new Double3(p._d1() - pDotD * d._d1(), p._d2() - pDotD * d._d2(), p._d3() - pDotD * d._d3());
    }

    @Override
    public int hashCode() {
        Vector dir = canonicalDirection();
        Double3 d = dir.getCoordinates();
        Double3 foot = canonicalFoot(dir);
        return Objects.hash(
                Double3.quantize(d._d1()), Double3.quantize(d._d2()), Double3.quantize(d._d3()),
                Double3.quantize(foot._d1()), Double3.quantize(foot._d2()), Double3.quantize(foot._d3()),
                Double3.quantize(_radius));
    }

    @Override
    public String toString() {
        return "Tube{axis=" + _axis + ", radius=" + _radius + "}";
    }
}

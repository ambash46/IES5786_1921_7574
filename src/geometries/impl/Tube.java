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

    /*
     * Strips axis-parallel components from both the ray direction (v) and the
     * delta vector (ΔP = p0 - pa), then solves the resulting quadratic
     * a·t²+b·t+c=0 for the tube surface. Only t > 0 roots are kept.
     */
    @SuppressWarnings("SpellCheckingInspection")
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
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
            return t <= 0 ? null : List.of(new Intersection(this, ray.getPoint(t)));
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

        // Only positive t values represent intersections in front of the ray origin.
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

    @Override
    public int hashCode() {
        return Objects.hash(_axis, _radius);
    }

    @Override
    public String toString() {
        return "Tube{axis=" + _axis + ", radius=" + _radius + "}";
    }
}

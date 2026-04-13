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
     * <p>
     * The value must satisfy two constraints simultaneously:
     * </p>
     * <ul>
     *   <li><b>Large enough</b> to be above floating-point noise: must be well
     *       above {@code Util.ACCURACY} (~1e-12) so that the probed point is
     *       reliably classified as inside or outside.</li>
     *   <li><b>Small enough</b> to stay within the same geometric region: must
     *       be much smaller than the minimum expected distance between two
     *       consecutive intersection parameters, so the probe does not jump
     *       across a neighbouring boundary.</li>
     * </ul>
     * <p>
     * {@code 1e-7} sits comfortably between these two constraints for
     * scene-scale geometries.
     * </p>
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
     * @throws IllegalArgumentException if the height is zero or negative
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

        if (isZero(projection))
            return direction.scale(-1);
        if (isZero(projection - _height))
            return direction;
        return super.getNormal(point);
    }

    /**
     * Finds all intersection points between the cylinder and the given ray.
     * <p>
     * A finite cylinder is a closed solid bounded by three surfaces:
     * the curved mantle (an infinite tube clipped to the height interval)
     * and two flat circular caps at the bottom ({@code projection = 0}) and
     * top ({@code projection = height}).
     * </p>
     * <p>
     * <b>Strategy — candidate gathering then filtering:</b><br>
     * Rather than handling every geometric configuration with its own branch,
     * the method works in two clean phases.
     * </p>
     * <ol>
     *   <li><b>Gather candidates.</b> Compute every ray parameter {@code t}
     *       at which the ray could possibly touch the cylinder boundary:
     *       the (up to 2) mantle hits from the parent {@link Tube} solver, plus
     *       the (up to 2) cap-plane hits.</li>
     *   <li><b>Filter.</b> For each candidate {@code t}, keep it only when two
     *       conditions hold simultaneously:
     *       (a) the resulting point actually lies on the finite cylinder boundary
     *           ({@code isOnCylinderBoundary}), and
     *       (b) the ray truly crosses the cylinder interior at that point, i.e.
     *           it is not a tangency or a cap-plane overlap
     *           ({@code crossesCylinderInterior}).</li>
     * </ol>
     *
     * @param ray the ray to intersect with
     * @return the intersection points, or {@code null} if there is no
     * intersection
     */
    @Override
    public List<Point> findIntersections(Ray ray) {
        List<Double> candidates = new ArrayList<>();

        // ── Phase 1a: mantle candidates ──────────────────────────────────────
        // Delegate to the parent Tube solver, which solves the quadratic
        // |P_perp(t) - axis_perp|² = r².  It can return 0, 1, or 2 candidate
        // points anywhere on the *infinite* tube.
        // We convert each hit point back to its ray parameter t using
        //   t = (point - rayOrigin) · rayDir   (direction is a unit vector).
        // The special case point == rayOrigin guards against a zero-vector subtraction.
        List<Point> tubeHits = super.findIntersections(ray);
        if (tubeHits != null) {
            for (Point p : tubeHits) {
                double t = p.equals(ray.origin())
                        ? 0d
                        : alignZero(ray.direction().dotProduct(p.subtract(ray.origin())));
                // Keep only forward (t > 0) non-duplicate values.
                // Deduplication is needed for rim points where the mantle candidate
                // and a cap candidate can coincide at the same t.
                if (t > 0 && isNewCandidate(candidates, t))
                    candidates.add(t);
            }
        }

        // ── Phase 1b: cap-plane candidates ───────────────────────────────────
        // The two caps lie in the planes:
        //   bottom:  (P - axisOrigin) · axisDir = 0
        //   top:     (P - axisOrigin) · axisDir = height
        //
        // Substituting P(t) = rayOrigin + t·rayDir gives the linear equation
        //   originProj + t · dirProj = capHeight
        //   t = (capHeight - originProj) / dirProj
        //
        // originProj = (rayOrigin - axisOrigin) · axisDir
        //   — how far the ray origin is along the axis from the bottom cap.
        // dirProj = rayDir · axisDir
        //   — rate at which t advances along the axis per unit of t.
        //   — zero means the ray is parallel to the caps; it never hits them.
        double originProj = axisProjection(ray.origin());
        double dirProj    = alignZero(_axis.direction().dotProduct(ray.direction()));

        if (!isZero(dirProj)) {
            // t for the bottom cap (capHeight = 0):
            double tBottom = alignZero(-originProj / dirProj);
            if (tBottom > 0 && isNewCandidate(candidates, tBottom))
                candidates.add(tBottom);

            // t for the top cap (capHeight = _height):
            double tTop = alignZero((_height - originProj) / dirProj);
            if (tTop > 0 && isNewCandidate(candidates, tTop))
                candidates.add(tTop);
        }

        if (candidates.isEmpty()) return null;

        // ── Phase 2: filter candidates ────────────────────────────────────────
        // Sort so that the first intersection in front of the ray comes first.
        candidates.sort(Double::compare);
        List<Point> result = new ArrayList<>();

        for (double t : candidates) {
            Point p = ray.getPoint(t);

            // Guard 1 — is this point actually on the finite cylinder boundary?
            // A mantle candidate might lie outside the height interval; a cap
            // candidate might lie outside the cap radius.
            if (!isOnCylinderBoundary(p)) continue;

            // Guard 2 — does the ray genuinely cross the interior here?
            // Probes a tiny step before and after t. If the ray transitions
            // between inside and outside the solid, the intersection is real.
            // If both sides are outside (tangency) or both inside (overlap
            // along the cap face), the candidate is discarded.
            if (!crossesCylinderInterior(ray, t)) continue;

            // Deduplicate geometric points. A rim point can pass both guards
            // via two distinct t-values that map to the same location.
            boolean dup = false;
            for (Point q : result) if (isZero(q.distance(p))) { dup = true; break; }
            if (!dup) result.add(p);
        }

        return result.isEmpty() ? null : result;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the signed scalar projection of {@code point} onto the cylinder
     * axis, measured from the bottom-cap center.
     * <p>
     * Formally: {@code (point - axisOrigin) · axisDirection}.<br>
     * The result is {@code 0} when {@code point} is in the bottom-cap plane,
     * {@code _height} when it is in the top-cap plane, negative below the
     * bottom cap, and greater than {@code _height} above the top cap.
     * The special case where {@code point} equals the axis origin avoids
     * constructing a zero-length vector.
     * </p>
     *
     * @param point the point to project
     * @return the signed distance along the axis from the bottom-cap center
     */
    private double axisProjection(Point point) {
        Point axisOrigin = _axis.origin();
        return point.equals(axisOrigin)
                ? 0d
                : alignZero(_axis.direction().dotProduct(point.subtract(axisOrigin)));
    }

    /**
     * Checks whether the given point lies on the finite cylinder boundary.
     * <p>
     * A point belongs to the boundary when it satisfies the axial constraint
     * {@code 0 ≤ projection ≤ height} <em>and</em> one of two radial rules:
     * </p>
     * <ul>
     *   <li><b>Cap face</b> ({@code projection = 0} or {@code projection = height}):
     *       the point may be anywhere inside or on the rim of the disk,
     *       i.e. {@code distanceSquared ≤ r²}.</li>
     *   <li><b>Mantle</b> ({@code 0 < projection < height}):
     *       the point must be exactly on the cylindrical surface,
     *       i.e. {@code distanceSquared = r²} (within floating-point tolerance).</li>
     * </ul>
     *
     * @param point candidate boundary point
     * @return {@code true} if the point lies on the mantle or on one of the
     * caps, {@code false} otherwise
     */
    private boolean isOnCylinderBoundary(Point point) {
        // Axial constraint: reject anything outside [0, height].
        // projection already went through alignZero in axisProjection, so a
        // plain < 0 comparison is safe (near-zero values were already snapped to 0).
        double projection = axisProjection(point);
        if (projection < 0 || alignZero(projection - _height) > 0) return false;

        // Radial constraint: compare squared distance to the nearest axis point
        // with r² (no square-root needed).
        double radialDelta = alignZero(
                point.distanceSquared(_axis.getPoint(projection)) - _radiusSquared);
        boolean onCap = isZero(projection) || isZero(projection - _height);
        return onCap ? radialDelta <= 0   // anywhere inside or on the rim of the disk
                     : isZero(radialDelta); // exactly on the mantle surface
    }

    /**
     * Checks whether the ray genuinely crosses the cylinder interior at the
     * given candidate boundary parameter.
     * <p>
     * The idea is to probe the ray at {@code t - offset} and {@code t + offset}
     * for a small {@code offset}:
     * </p>
     * <ul>
     *   <li>If one side is inside and the other outside, the ray crosses the
     *       boundary — this is a real intersection.</li>
     *   <li>If both sides are outside (tangency to the mantle, or a ray that
     *       grazes the rim), there is no crossing.</li>
     *   <li>If both sides are inside (ray lying flat in a cap plane, overlapping
     *       the disk in a whole segment), there is no discrete crossing.</li>
     * </ul>
     * <p>
     * "Strictly inside" means {@code 0 < axisProjection < height} AND
     * {@code distanceSquared < r²} — both boundary surfaces excluded.
     * The offset is capped at {@code t / 2} so that the before-probe never
     * goes behind the ray origin.
     * </p>
     *
     * @param ray the tested ray
     * @param t   candidate boundary parameter
     * @return {@code true} if the ray enters or exits the cylinder interior at
     * the candidate point
     */
    private boolean crossesCylinderInterior(Ray ray, double t) {
        // Choose a step small enough to stay within the same geometric region
        // on each side, but not so small that floating-point errors dominate.
        double offset = Math.min(INTERIOR_EPS, t / 2d);

        // Probe strictly inside: 0 < axisProj < height  AND  distanceSq < r²
        boolean insideBefore = isStrictlyInside(ray, t - offset);
        boolean insideAfter  = isStrictlyInside(ray, t + offset);

        // XOR: the ray crosses the boundary only when the two sides differ.
        return insideBefore != insideAfter;
    }

    /**
     * Returns {@code true} if the point at ray parameter {@code t} lies
     * strictly inside the finite cylinder volume (not on its boundary).
     *
     * @param ray the tested ray
     * @param t   ray parameter
     * @return {@code true} if the point is strictly inside the solid
     */
    private boolean isStrictlyInside(Ray ray, double t) {
        Point p = ray.getPoint(t);
        double proj = axisProjection(p);
        // Strict axial bounds — cap planes themselves are excluded.
        if (proj <= 0 || alignZero(proj - _height) >= 0) return false;
        // Strict radial bound — the mantle surface itself is excluded.
        return alignZero(p.distanceSquared(_axis.getPoint(proj)) - _radiusSquared) < 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Small inline-style utilities kept as private methods for readability
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns {@code true} if {@code list} does not yet contain a value within
     * floating-point tolerance of {@code t} (i.e. {@code t} is a new candidate).
     *
     * @param list the list of already-collected candidate parameters
     * @param t    the candidate parameter to check
     * @return {@code true} if no existing entry in {@code list} is within
     *         floating-point tolerance of {@code t}
     */
    private static boolean isNewCandidate(List<Double> list, double t) {
        for (double v : list) if (isZero(v - t)) return false;
        return true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Object overrides
    // ─────────────────────────────────────────────────────────────────────────

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
        Cylinder other = (Cylinder) obj;
        if (!isZero(other._radius - _radius)) return false;
        // Two finite cylinders represent the same shape when their radii are equal
        // and their two cap centers are the same pair of points (in either order).
        // This handles cylinders defined with reversed axis directions, where the
        // bottom cap of one is the top cap of the other.
        Point b1 = _axis.origin();
        Point t1 = _axis.getPoint(_height);
        Point b2 = other._axis.origin();
        Point t2 = other._axis.getPoint(other._height);
        return (isZero(b1.distance(b2)) && isZero(t1.distance(t2)))
                || (isZero(b1.distance(t2)) && isZero(t1.distance(b2)));
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

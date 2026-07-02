package renderer;

import java.util.ArrayList;
import java.util.List;
import primitives.Point;
import primitives.Vector;

/**
 * Generates a beam of 3D sample points distributed across a rectangular or
 * circular target area.
 *
 * <p>Feature-agnostic: receives a center, two local axes, and a size at call
 * time and returns a list of 3D points. The caller decides what ray to build
 * through each point.
 *
 * <p><b>Caching:</b> cacheable strategies (GRID) compute offsets once;
 * any setter clears the cache.
 *
 * <p><b>Circle shape:</b> when enabled, Blackboard over-requests by ×(4/π)
 * to compensate for the ~21.5% of square points that fall outside the
 * inscribed circle. The strategy itself stays unaware of shape.
 *
 * @author Ambash and Elyasaf
 */
class Blackboard {

    private int             _numSamples = 1;
    private SamplingPattern _strategy   = SamplingPatterns.GRID;
    private boolean         _circle     = false;

    /** Cached offsets for cacheable strategies (e.g. Grid). */
    private double[][] _cache = null;

    // ── setters ────────────────────────────────────────────────────────────

    Blackboard setNumSamples(int n) {
        if (n < 1) throw new IllegalArgumentException("numSamples must be >= 1, got: " + n);
        _numSamples = n;
        _cache = null;
        return this;
    }

    Blackboard setStrategy(SamplingPattern strategy) {
        if (strategy == null) throw new IllegalArgumentException("SamplingPattern must not be null");
        _strategy = strategy;
        _cache = null;
        return this;
    }

    boolean isMultiSample() { return _numSamples > 1; }

    Blackboard setCircleShape(boolean circle) {
        _circle = circle;
        _cache = null;
        return this;
    }

    // ── query ──────────────────────────────────────────────────────────────

    // ── main API ───────────────────────────────────────────────────────────

    /**
     * Generates sample points on a rectangular target area.
     *
     * @param center the center of the target area in world space
     * @param vX     local X axis (unit vector)
     * @param vY     local Y axis (unit vector, perpendicular to vX)
     * @param width  full extent along vX
     * @param height full extent along vY
     * @return list of 3D sample points covering the area
     */
    List<Point> generateTargetPoints(Point center, Vector vX, Vector vY,
                                     double width, double height) {
        double[][] offsets = getOffsets();
        List<Point> points = new ArrayList<>(offsets.length);
        for (double[] off : offsets) {
            double dx = off[0] * width;
            double dy = off[1] * height;
            Point p = center;
            if (dx != 0) p = p.add(vX.scale(dx));
            if (dy != 0) p = p.add(vY.scale(dy));
            points.add(p);
        }
        return points;
    }

    /**
     * Overload for a square target area ({@code width == height}).
     */
    List<Point> generateTargetPoints(Point center, Vector vX, Vector vY, double size) {
        return generateTargetPoints(center, vX, vY, size, size);
    }

    // ── internal offset pipeline ───────────────────────────────────────────

    private double[][] getOffsets() {
        // Validated here (against the final combined state) rather than in the
        // individual setters, so the outcome doesn't depend on call order:
        // setStrategy(RANDOM).setNumSamples(9) and setNumSamples(9).setStrategy(RANDOM)
        // must behave identically.
        if (_numSamples == 1 && _strategy != SamplingPatterns.GRID)
            throw new IllegalArgumentException(
                    "numSamples=1 supports only GRID (non-deterministic patterns require >1 samples)");

        // Over-generate for circle: only π/4 ≈ 78.5% of square points
        // fall inside the inscribed circle, so request ×(4/π) to compensate.
        int requestCount = _circle
                ? (int) (_numSamples * (4.0 / Math.PI) + 1)
                : _numSamples;

        if (_strategy.isCacheable()) {
            if (_cache == null)
                _cache = applyShape(_strategy.generateOffsets(requestCount));
            return _cache;
        }
        return applyShape(_strategy.generateOffsets(requestCount));
    }

    /** Filters offsets to the inscribed circle (radius 0.5) when circle shape is active. */
    private double[][] applyShape(double[][] offsets) {
        if (!_circle) return offsets;
        List<double[]> filtered = new ArrayList<>(offsets.length);
        for (double[] off : offsets)
            if (off[0] * off[0] + off[1] * off[1] <= 0.25)
                filtered.add(off);
        return filtered.toArray(new double[0][]);
    }
}

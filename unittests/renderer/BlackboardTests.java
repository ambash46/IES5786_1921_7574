package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Blackboard} and the three {@link SamplingPattern} implementations.
 * Each test prints an ASCII visualisation so the distribution can be inspected visually.
 */
class BlackboardTests {

    // ── shared geometry ────────────────────────────────────────────────────
    private static final Point  CENTER = new Point(0, 0, 0);
    private static final Vector VX     = new Vector(1, 0, 0);
    private static final Vector VY     = new Vector(0, 1, 0);

    // ── ASCII helper ───────────────────────────────────────────────────────

    /**
     * Prints a 10×10 ASCII grid showing where each offset lands.
     * Offset (-0.5,-0.5) → top-left; (0.5,0.5) → bottom-right.
     */
    private void printOffsets(double[][] offsets, String title) {
        int RES = 10;
        char[][] grid = new char[RES][RES];
        for (char[] row : grid) java.util.Arrays.fill(row, '.');

        for (double[] off : offsets) {
            int col = (int) Math.min((off[0] + 0.5) * RES, RES - 1);
            int row = (int) Math.min((off[1] + 0.5) * RES, RES - 1);
            if (col >= 0 && col < RES && row >= 0 && row < RES)
                grid[row][col] = 'X';
        }

        System.out.println("=== " + title + " (" + offsets.length + " points) ===");
        for (int r = RES - 1; r >= 0; r--)
            System.out.println(new String(grid[r]));
        System.out.println();
    }

    // ── GRID ───────────────────────────────────────────────────────────────

    /**
     * 3×3 GRID: 9 points at exact cell centres.
     * For n=3, centres are at offsets -1/3, 0, +1/3 on each axis.
     */
    @Test
    void gridDistribution() {
        double[][] offsets = GridSampling.INSTANCE.generateOffsets(9);
        printOffsets(offsets, "GRID 3×3");

        assertEquals(9, offsets.length, "3×3 grid must produce 9 offsets");

        // All offsets in [-0.5, 0.5]²
        for (double[] off : offsets) {
            assertTrue(off[0] >= -0.5 && off[0] <= 0.5, "X out of range: " + off[0]);
            assertTrue(off[1] >= -0.5 && off[1] <= 0.5, "Y out of range: " + off[1]);
        }

        // Check expected values: -1/3, 0, +1/3 on each axis
        double step = 1.0 / 3;
        double[] expected = {-step, 0, step};
        for (double ex : expected)
            for (double ey : expected) {
                final double fx = ex, fy = ey;
                assertTrue(java.util.Arrays.stream(offsets)
                                .anyMatch(o -> Math.abs(o[0] - fx) < 1e-10 && Math.abs(o[1] - fy) < 1e-10),
                        "Missing expected grid point (" + ex + ", " + ey + ")");
            }
    }

    /**
     * Grid with numSamples=10 → ceil(√10)=4 → 4×4=16 offsets produced.
     */
    @Test
    void gridOverGenerates() {
        double[][] offsets = GridSampling.INSTANCE.generateOffsets(10);
        printOffsets(offsets, "GRID ceil(√10)=4 → 4×4");
        assertEquals(16, offsets.length, "ceil(√10)=4 → 4×4=16 points");
    }

    // ── RANDOM ────────────────────────────────────────────────────────────

    /**
     * RANDOM: exactly numSamples points, all inside [-0.5, 0.5]².
     */
    @Test
    void randomDistribution() {
        double[][] offsets = RandomSampling.INSTANCE.generateOffsets(25);
        printOffsets(offsets, "RANDOM 25 samples");

        assertEquals(25, offsets.length);
        for (double[] off : offsets) {
            assertTrue(off[0] >= -0.5 && off[0] < 0.5, "X out of range");
            assertTrue(off[1] >= -0.5 && off[1] < 0.5, "Y out of range");
        }
    }

    /**
     * Two consecutive RANDOM calls must produce different distributions.
     */
    @Test
    void randomIsStochastic() {
        double[][] first  = RandomSampling.INSTANCE.generateOffsets(9);
        double[][] second = RandomSampling.INSTANCE.generateOffsets(9);
        boolean allEqual = true;
        for (int i = 0; i < first.length; i++)
            if (first[i][0] != second[i][0] || first[i][1] != second[i][1]) { allEqual = false; break; }
        assertFalse(allEqual, "Two random calls must not produce identical results");
    }

    // ── JITTERED ──────────────────────────────────────────────────────────

    /**
     * JITTERED 3×3: 9 points, each confined to its own cell.
     */
    @Test
    void jitteredDistribution() {
        double[][] offsets = JitteredSampling.INSTANCE.generateOffsets(9);
        printOffsets(offsets, "JITTERED 3×3");

        assertEquals(9, offsets.length);
        int n = 3;
        double cellSize = 1.0 / n;

        for (int i = 0; i < offsets.length; i++) {
            int col = i % n;
            int row = i / n;
            double minX = -0.5 + col * cellSize;
            double minY = -0.5 + row * cellSize;
            assertTrue(offsets[i][0] >= minX && offsets[i][0] < minX + cellSize,
                    "X not in cell[" + col + "]: " + offsets[i][0]);
            assertTrue(offsets[i][1] >= minY && offsets[i][1] < minY + cellSize,
                    "Y not in cell[" + row + "]: " + offsets[i][1]);
        }
    }

    // ── BLACKBOARD integration ─────────────────────────────────────────────

    /**
     * Blackboard maps Grid 3×3 offsets to 3D points on the XY plane at Z=5.
     * All returned points must have Z=5 and lie within a 1×1 area.
     */
    @Test
    void blackboardTargetPoints() {
        Blackboard bb = new Blackboard().setNumSamples(9);
        Point center = new Point(0, 0, 5);
        List<Point> points = bb.generateTargetPoints(center, VX, VY, 1.0);

        assertEquals(9, points.size(), "Must return 9 points");

        System.out.println("=== BLACKBOARD target points (center=0,0,5; size=1) ===");
        for (Point p : points) System.out.println("  " + p);

        // All points within the 1×1 square → distanceSquared from center ≤ 0.5 (= (0.5√2)²)
        for (Point p : points)
            assertTrue(p.distanceSquared(center) <= 0.5 + 1e-10,
                    "Point outside 1×1 area: " + p);
    }

    /**
     * GRID cache: two calls with the same configuration must return identical points.
     */
    @Test
    void blackboardGridCacheConsistency() {
        Blackboard bb = new Blackboard().setNumSamples(9);
        List<Point> first  = bb.generateTargetPoints(CENTER, VX, VY, 2.0);
        List<Point> second = bb.generateTargetPoints(CENTER, VX, VY, 2.0);

        assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); i++)
            assertEquals(first.get(i), second.get(i), "Cache must return identical points at index " + i);

        System.out.println("Grid cache consistency: PASS");
    }

    /**
     * Cache invalidates when numSamples changes.
     */
    @Test
    void blackboardCacheInvalidation() {
        Blackboard bb = new Blackboard().setNumSamples(9);
        List<Point> before = bb.generateTargetPoints(CENTER, VX, VY, 1.0);
        assertEquals(9, before.size());

        bb.setNumSamples(25);
        List<Point> after = bb.generateTargetPoints(CENTER, VX, VY, 1.0);
        assertEquals(25, after.size(), "Cache must be invalidated after setNumSamples");
    }

    /**
     * Circle shape: all points must lie within radius 0.5 of centre.
     */
    @Test
    void blackboardCircleShape() {
        Blackboard bb = new Blackboard().setNumSamples(81).setCircleShape(true);
        List<Point> points = bb.generateTargetPoints(CENTER, VX, VY, 1.0);

        // Over-generation compensates for circle filtering: we request ×(4/π) points
        // so after filtering we end up approximately at numSamples.
        // The exact count depends on ceil(sqrt) rounding — allow reasonable tolerance.
        System.out.println("Circle shape: " + points.size() + " points (target ~81)");
        assertTrue(points.size() >= 70 && points.size() <= 110,
                "Expected ~81 points after circle filter, got: " + points.size());

        double radius = 0.5;
        for (Point p : points)
            assertTrue(p.distanceSquared(CENTER) <= radius * radius + 1e-10,
                    "Point outside circle: " + p);
    }
}

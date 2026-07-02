package renderer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the built-in {@link SamplingPattern} implementations exposed
 * via {@link SamplingPatterns} ({@code GRID}, {@code RANDOM}, {@code JITTERED}).
 * The tests verify:
 * <ul>
 * <li>{@link SamplingPatterns#GRID}</li>
 * <li>{@link SamplingPatterns#RANDOM}</li>
 * <li>{@link SamplingPatterns#JITTERED}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class SamplingPatternsTests {

    /** Default constructor to satisfy JavaDoc generator */
    SamplingPatternsTests() { /* to satisfy JavaDoc generator */ }

    /** Verifies every offset in {@code offsets} lies within [-0.5,0.5]^2. */
    private static void assertAllWithinUnitSquare(double[][] offsets, String message) {
        for (double[] off : offsets) {
            assertTrue(off[0] >= -0.5 && off[0] <= 0.5, message + " (x out of range: " + off[0] + ")");
            assertTrue(off[1] >= -0.5 && off[1] <= 0.5, message + " (y out of range: " + off[1] + ")");
        }
    }

    /**
     * Test method for {@link SamplingPatterns#GRID}.
     * Verifies exact offset count for a perfect square, the rounding-up
     * behavior for a non-perfect-square request, determinism, and cacheability.
     */
    @Test
    void testGrid() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: a perfect square request (9) yields exactly 9 offsets (3x3)
        double[][] offsets9 = SamplingPatterns.GRID.generateOffsets(9);
        assertEquals(9, offsets9.length, "GRID with numSamples=9 should produce a 3x3 grid (9 offsets)");
        assertAllWithinUnitSquare(offsets9, "GRID offsets");

        // TC02: GRID is cacheable and deterministic — repeated calls give identical offsets
        double[][] offsets9Again = SamplingPatterns.GRID.generateOffsets(9);
        assertEquals(offsets9.length, offsets9Again.length, "GRID should be deterministic across calls");
        for (int i = 0; i < offsets9.length; i++)
            assertEquals(offsets9[i][0], offsets9Again[i][0], 1e-12, "GRID offset X should be identical across calls");

        // =============== Boundary Values Tests ==================

        // TC11: a non-perfect-square request (10) rounds up to the next perfect square (16, 4x4)
        double[][] offsets10 = SamplingPatterns.GRID.generateOffsets(10);
        assertEquals(16, offsets10.length, "GRID with numSamples=10 should round up to a 4x4 grid (16 offsets)");

        // TC12: a single sample (1) yields exactly one offset, at the center
        double[][] offsets1 = SamplingPatterns.GRID.generateOffsets(1);
        assertEquals(1, offsets1.length, "GRID with numSamples=1 should produce exactly 1 offset");
        assertEquals(0d, offsets1[0][0], 1e-12, "A single GRID sample should be centered (x=0)");
        assertEquals(0d, offsets1[0][1], 1e-12, "A single GRID sample should be centered (y=0)");

        // TC13: GRID reports itself as cacheable
        assertTrue(SamplingPatterns.GRID.isCacheable(), "GRID should be cacheable");
    }

    /**
     * Test method for {@link SamplingPatterns#RANDOM}.
     * Verifies the exact requested offset count, value range, and that
     * repeated calls are not cached (each call produces independent output).
     */
    @Test
    void testRandom() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: RANDOM produces exactly numSamples offsets (no rounding, unlike GRID)
        double[][] offsets = SamplingPatterns.RANDOM.generateOffsets(10);
        assertEquals(10, offsets.length, "RANDOM should produce exactly the requested number of offsets");
        assertAllWithinUnitSquare(offsets, "RANDOM offsets");

        // TC02: RANDOM is not cacheable
        assertFalse(SamplingPatterns.RANDOM.isCacheable(), "RANDOM should not be cacheable");

        // =============== Boundary Values Tests ==================

        // TC11: a single sample still respects the [-0.5,0.5] bounds
        double[][] offsets1 = SamplingPatterns.RANDOM.generateOffsets(1);
        assertEquals(1, offsets1.length, "RANDOM with numSamples=1 should produce exactly 1 offset");
        assertAllWithinUnitSquare(offsets1, "RANDOM single offset");
    }

    /**
     * Test method for {@link SamplingPatterns#JITTERED}.
     * Verifies the grid-cell count (like GRID, rounds up to a perfect square),
     * that each jittered point stays within its own cell, and non-cacheability.
     */
    @Test
    void testJittered() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: a perfect square request (9) yields exactly 9 offsets (3x3 cells)
        double[][] offsets = SamplingPatterns.JITTERED.generateOffsets(9);
        assertEquals(9, offsets.length, "JITTERED with numSamples=9 should produce 9 offsets (3x3 cells)");
        assertAllWithinUnitSquare(offsets, "JITTERED offsets");

        // TC02: JITTERED is not cacheable (regenerated with fresh jitter every call)
        assertFalse(SamplingPatterns.JITTERED.isCacheable(), "JITTERED should not be cacheable");

        // =============== Boundary Values Tests ==================

        // TC11: a non-perfect-square request rounds up like GRID does
        double[][] offsets10 = SamplingPatterns.JITTERED.generateOffsets(10);
        assertEquals(16, offsets10.length, "JITTERED with numSamples=10 should round up to 16 offsets (4x4 cells)");
    }
}

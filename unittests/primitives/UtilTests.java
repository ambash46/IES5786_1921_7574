package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static primitives.Util.alignZero;
import static primitives.Util.compareSign;
import static primitives.Util.isZero;
import static primitives.Util.random;

/**
 * Unit tests for class {@link Util}.
 * The tests verify:
 * <ul>
 * <li>{@link Util#isZero(double)}</li>
 * <li>{@link Util#alignZero(double)}</li>
 * <li>{@link Util#compareSign(double, double)}</li>
 * <li>{@link Util#random(double, double)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class UtilTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    UtilTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Error message for wrong isZero() result.
     */
    private static final String IS_ZERO_ERROR = "Util.isZero() returned an unexpected result";
    /**
     * Error message for wrong alignZero() result.
     */
    private static final String ALIGN_ZERO_ERROR = "Util.alignZero() returned an unexpected result";
    /**
     * Error message for wrong compareSign() result.
     */
    private static final String COMPARE_SIGN_ERROR = "Util.compareSign() returned an unexpected result";
    /**
     * Error message for a random() value outside the requested range.
     */
    private static final String RANDOM_RANGE_ERROR = "Util.random() returned a value outside the requested range";

    /**
     * Test method for {@link Util#isZero(double)}.
     */
    @Test
    void testIsZero() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: A clearly non-zero positive number
        assertFalse(isZero(5.0), IS_ZERO_ERROR);
        // TC02: A clearly non-zero negative number
        assertFalse(isZero(-5.0), IS_ZERO_ERROR);
        // TC03: Extremely small floating-point noise is considered zero
        assertTrue(isZero(1e-50), IS_ZERO_ERROR);
        assertTrue(isZero(-1e-50), IS_ZERO_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Exact zero
        assertTrue(isZero(0.0), IS_ZERO_ERROR);
        // TC12: A small but "real" (non-noise) value is not zero
        assertFalse(isZero(1e-6), IS_ZERO_ERROR);
    }

    /**
     * Test method for {@link Util#alignZero(double)}.
     */
    @Test
    void testAlignZero() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: A regular positive number is returned unchanged
        assertEquals(5.0, alignZero(5.0), ALIGN_ZERO_ERROR);
        // TC02: A regular negative number is returned unchanged
        assertEquals(-5.0, alignZero(-5.0), ALIGN_ZERO_ERROR);
        // TC03: Floating-point noise is aligned to exactly 0.0
        assertEquals(0.0, alignZero(1e-50), ALIGN_ZERO_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Exact zero stays zero
        assertEquals(0.0, alignZero(0.0), ALIGN_ZERO_ERROR);
    }

    /**
     * Test method for {@link Util#compareSign(double, double)}.
     */
    @Test
    void testCompareSign() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Both numbers positive
        assertTrue(compareSign(3.0, 7.0), COMPARE_SIGN_ERROR);
        // TC02: Both numbers negative
        assertTrue(compareSign(-3.0, -7.0), COMPARE_SIGN_ERROR);
        // TC03: One positive, one negative
        assertFalse(compareSign(3.0, -7.0), COMPARE_SIGN_ERROR);
        assertFalse(compareSign(-3.0, 7.0), COMPARE_SIGN_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: One of the numbers is zero (zero is neither positive nor negative)
        assertFalse(compareSign(0.0, 5.0), COMPARE_SIGN_ERROR);
        assertFalse(compareSign(5.0, 0.0), COMPARE_SIGN_ERROR);
        // TC12: Both numbers are zero
        assertFalse(compareSign(0.0, 0.0), COMPARE_SIGN_ERROR);
    }

    /**
     * Test method for {@link Util#random(double, double)}.
     */
    @Test
    void testRandom() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Repeated sampling stays within the requested [min, max) range
        for (int i = 0; i < 1000; i++) {
            double value = random(2.0, 5.0);
            assertTrue(value >= 2.0 && value < 5.0, RANDOM_RANGE_ERROR);
        }

        // =============== Boundary Values Tests ==================
        // TC11: A zero-width range always returns the boundary value
        assertEquals(3.0, random(3.0, 3.0), RANDOM_RANGE_ERROR);
    }
}

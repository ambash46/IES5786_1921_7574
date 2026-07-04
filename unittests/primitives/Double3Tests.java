package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for class {@link Double3}.
 * The tests verify:
 * <ul>
 * <li>{@link Double3#Double3(double)}</li>
 * <li>{@link Double3#add(Double3)}</li>
 * <li>{@link Double3#subtract(Double3)}</li>
 * <li>{@link Double3#scale(double)}</li>
 * <li>{@link Double3#divide(double)}</li>
 * <li>{@link Double3#product(Double3)}</li>
 * <li>{@link Double3#isLowerThan(double)}</li>
 * <li>{@link Double3#isNotLowerThan(double)}</li>
 * <li>{@link Double3#isLowerThan(Double3)}</li>
 * <li>{@link Double3#equals(Object)}</li>
 * <li>{@link Double3#hashCode()}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class Double3Tests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    Double3Tests() { /* to satisfy JavaDoc generator */ }

    /**
     * Error message for wrong single-value constructor result.
     */
    private static final String CONSTRUCTOR_ERROR = "Double3(double) constructor produced an unexpected result";
    /**
     * Error message for wrong addition result.
     */
    private static final String ADD_ERROR = "Double3.add() returned an unexpected result";
    /**
     * Error message for wrong subtraction result.
     */
    private static final String SUBTRACT_ERROR = "Double3.subtract() returned an unexpected result";
    /**
     * Error message for wrong scaling result.
     */
    private static final String SCALE_ERROR = "Double3.scale() returned an unexpected result";
    /**
     * Error message for wrong division result.
     */
    private static final String DIVIDE_ERROR = "Double3.divide() returned an unexpected result";
    /**
     * Error message for wrong component-wise product result.
     */
    private static final String PRODUCT_ERROR = "Double3.product() returned an unexpected result";
    /**
     * Error message for wrong isLowerThan(double) result.
     */
    private static final String IS_LOWER_THAN_SCALAR_ERROR = "Double3.isLowerThan(double) returned an unexpected result";
    /**
     * Error message for wrong isNotLowerThan(double) result.
     */
    private static final String IS_NOT_LOWER_THAN_ERROR = "Double3.isNotLowerThan(double) returned an unexpected result";
    /**
     * Error message for wrong isLowerThan(Double3) result.
     */
    private static final String IS_LOWER_THAN_TRIAD_ERROR = "Double3.isLowerThan(Double3) returned an unexpected result";
    /**
     * Error message for wrong equals() result.
     */
    private static final String EQUALS_ERROR = "Double3.equals() returned an unexpected result";
    /**
     * Error message for wrong hashCode() behavior.
     */
    private static final String HASHCODE_ERROR = "Double3.hashCode() should be equal for equal objects";

    /**
     * Base triad for most tests.
     */
    private static final Double3 T1 = new Double3(1, 2, 3);
    /**
     * Triad equal to {@link #T1}.
     */
    private static final Double3 T1_COPY = new Double3(1, 2, 3);
    /**
     * Triad used as the second operand for arithmetic tests.
     */
    private static final Double3 T2 = new Double3(4, 5, 6);
    /**
     * Triad with negative components.
     */
    private static final Double3 NEGATIVE = new Double3(-1, -2, -3);

    /**
     * Test method for {@link Double3#Double3(double)}.
     */
    @Test
    void testConstructorSingleValue() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: All three components equal a positive value
        assertEquals(new Double3(5, 5, 5), new Double3(5), CONSTRUCTOR_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: All three components equal zero
        assertEquals(Double3.ZERO, new Double3(0), CONSTRUCTOR_ERROR);
    }

    /**
     * Test method for {@link Double3#add(Double3)}.
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Adding two regular triads
        assertEquals(new Double3(5, 7, 9), assertDoesNotThrow(() -> T1.add(T2), ADD_ERROR), ADD_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Adding the zero triad
        assertEquals(T1, T1.add(Double3.ZERO), ADD_ERROR);
        // TC12: Adding the opposite triad results in zero
        assertEquals(Double3.ZERO, T1.add(NEGATIVE), ADD_ERROR);
    }

    /**
     * Test method for {@link Double3#subtract(Double3)}.
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Subtracting two regular triads
        assertEquals(new Double3(-3, -3, -3), assertDoesNotThrow(() -> T1.subtract(T2), SUBTRACT_ERROR), SUBTRACT_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Subtracting a triad from itself results in zero
        assertEquals(Double3.ZERO, T1.subtract(T1), SUBTRACT_ERROR);
    }

    /**
     * Test method for {@link Double3#scale(double)}.
     */
    @Test
    void testScale() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Scaling by a positive factor
        assertEquals(new Double3(2, 4, 6), assertDoesNotThrow(() -> T1.scale(2), SCALE_ERROR), SCALE_ERROR);
        // TC02: Scaling by a negative factor
        assertEquals(NEGATIVE, T1.scale(-1), SCALE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Scaling by zero results in the zero triad
        assertEquals(Double3.ZERO, T1.scale(0), SCALE_ERROR);
        // TC12: Scaling by one leaves the triad unchanged
        assertEquals(T1, T1.scale(1), SCALE_ERROR);
    }

    /**
     * Test method for {@link Double3#divide(double)}.
     */
    @Test
    void testDivide() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Dividing by a positive factor
        assertEquals(new Double3(0.5, 1, 1.5), assertDoesNotThrow(() -> T1.divide(2), DIVIDE_ERROR), DIVIDE_ERROR);
        // TC02: Dividing by a negative factor
        assertEquals(NEGATIVE, T1.divide(-1), DIVIDE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Dividing by one leaves the triad unchanged
        assertEquals(T1, T1.divide(1), DIVIDE_ERROR);
    }

    /**
     * Test method for {@link Double3#product(Double3)}.
     */
    @Test
    void testProduct() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Component-wise product of two regular triads
        assertEquals(new Double3(4, 10, 18), assertDoesNotThrow(() -> T1.product(T2), PRODUCT_ERROR), PRODUCT_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Product with the zero triad results in zero
        assertEquals(Double3.ZERO, T1.product(Double3.ZERO), PRODUCT_ERROR);
        // TC12: Product with the ONE triad leaves the triad unchanged
        assertEquals(T1, T1.product(Double3.ONE), PRODUCT_ERROR);
    }

    /**
     * Test method for {@link Double3#isLowerThan(double)}.
     */
    @Test
    void testIsLowerThanScalar() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: All components strictly lower than k
        assertTrue(new Double3(1, 2, 3).isLowerThan(10), IS_LOWER_THAN_SCALAR_ERROR);
        // TC02: All components strictly greater than k
        assertFalse(new Double3(11, 12, 13).isLowerThan(10), IS_LOWER_THAN_SCALAR_ERROR);
        // TC03: Only some components lower than k
        assertFalse(new Double3(1, 20, 3).isLowerThan(10), IS_LOWER_THAN_SCALAR_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: A component exactly equal to k is not considered lower
        assertFalse(new Double3(10, 1, 1).isLowerThan(10), IS_LOWER_THAN_SCALAR_ERROR);
    }

    /**
     * Test method for {@link Double3#isNotLowerThan(double)}.
     * Documented as the logical negation of {@link Double3#isLowerThan(double)}.
     */
    @Test
    void testIsNotLowerThan() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: All components strictly lower than k -> not "greater"
        assertFalse(new Double3(1, 2, 3).isNotLowerThan(10), IS_NOT_LOWER_THAN_ERROR);
        // TC02: All components strictly greater than k -> "greater"
        assertTrue(new Double3(11, 12, 13).isNotLowerThan(10), IS_NOT_LOWER_THAN_ERROR);
        // TC03: Only one component at/above k is enough to be "greater"
        assertTrue(new Double3(1, 20, 3).isNotLowerThan(10), IS_NOT_LOWER_THAN_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: A component exactly equal to k counts as "greater" (negation of strict lower-than)
        assertTrue(new Double3(10, 1, 1).isNotLowerThan(10), IS_NOT_LOWER_THAN_ERROR);
        // TC12: isNotLowerThan(k) must always be the exact logical negation of isLowerThan(k)
        for (Double3 d : new Double3[]{T1, T2, NEGATIVE, Double3.ZERO}) {
            assertEquals(!d.isLowerThan(100), d.isNotLowerThan(100), IS_NOT_LOWER_THAN_ERROR);
        }
    }

    /**
     * Test method for {@link Double3#isLowerThan(Double3)}.
     */
    @Test
    void testIsLowerThanTriad() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Every component strictly lower than the corresponding one
        assertTrue(new Double3(1, 2, 3).isLowerThan(new Double3(4, 5, 6)), IS_LOWER_THAN_TRIAD_ERROR);
        // TC02: Every component strictly greater than the corresponding one
        assertFalse(new Double3(4, 5, 6).isLowerThan(new Double3(1, 2, 3)), IS_LOWER_THAN_TRIAD_ERROR);
        // TC03: Mixed - only some components lower
        assertFalse(new Double3(1, 5, 3).isLowerThan(new Double3(4, 5, 6)), IS_LOWER_THAN_TRIAD_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Identical triads - no component is strictly lower
        assertFalse(T1.isLowerThan(T1), IS_LOWER_THAN_TRIAD_ERROR);
    }

    /**
     * Test method for {@link Double3#equals(Object)} and {@link Double3#hashCode()}.
     */
    @Test
    void testEqualsAndHashCode() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Equal triads constructed independently
        assertEquals(T1, T1_COPY, EQUALS_ERROR);
        assertEquals(T1.hashCode(), T1_COPY.hashCode(), HASHCODE_ERROR);
        // TC02: Different triads are not equal
        assertFalse(T1.equals(T2), EQUALS_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: A triad equals itself
        assertEquals(T1, T1, EQUALS_ERROR);
        // TC12: Not equal to null / a different type
        assertFalse(T1.equals(null), EQUALS_ERROR);
        assertFalse(T1.equals("not a Double3"), EQUALS_ERROR);
        // TC13: Values that differ only by floating-point noise are still equal,
        // and (being far below the hashCode quantization grid) also hash the same
        Double3 noisy = new Double3(1 + 1e-14, 2, 3);
        assertEquals(T1, noisy, EQUALS_ERROR);
        assertEquals(T1.hashCode(), noisy.hashCode(), HASHCODE_ERROR);
    }

    /**
     * Test method for {@link Double3#toString()}.
     */
    @Test
    void testToString() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: String representation contains all three components
        assertEquals("(1.0,2.0,3.0)", T1.toString(), "Double3.toString() returned an unexpected representation");
    }
}

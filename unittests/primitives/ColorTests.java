package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for class {@link Color}.
 * The tests verify:
 * <ul>
 * <li>{@link Color#Color(double, double, double)}</li>
 * <li>{@link Color#Color(java.awt.Color)}</li>
 * <li>{@link Color#getColor()}</li>
 * <li>{@link Color#add(Color...)}</li>
 * <li>{@link Color#scale(Double3)}</li>
 * <li>{@link Color#scale(double)}</li>
 * <li>{@link Color#reduce(int)}</li>
 * <li>{@link Color#equals(Object)}</li>
 * <li>{@link Color#equalColors(Color...)}</li>
 * <li>{@link Color#hashCode()}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class ColorTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    ColorTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Error message for wrong constructor result.
     */
    private static final String CONSTRUCTOR_ERROR = "Color constructor produced an unexpected result";
    /**
     * Error message for missing negative-component exception.
     */
    private static final String NEGATIVE_COMPONENT_ERROR = "Color constructor should reject a negative component";
    /**
     * Error message for wrong getColor() result.
     */
    private static final String GET_COLOR_ERROR = "Color.getColor() returned an unexpected java.awt.Color";
    /**
     * Error message for wrong add() result.
     */
    private static final String ADD_ERROR = "Color.add() returned an unexpected color";
    /**
     * Error message for wrong scale(Double3) result.
     */
    private static final String SCALE_TRIAD_ERROR = "Color.scale(Double3) returned an unexpected color";
    /**
     * Error message for missing negative-scale(Double3) exception.
     */
    private static final String SCALE_TRIAD_EXCEPTION_ERROR = "Color.scale(Double3) should reject a negative factor";
    /**
     * Error message for wrong scale(double) result.
     */
    private static final String SCALE_SCALAR_ERROR = "Color.scale(double) returned an unexpected color";
    /**
     * Error message for missing negative-scale(double) exception.
     */
    private static final String SCALE_SCALAR_EXCEPTION_ERROR = "Color.scale(double) should reject a negative factor";
    /**
     * Error message for wrong reduce() result.
     */
    private static final String REDUCE_ERROR = "Color.reduce() returned an unexpected color";
    /**
     * Error message for missing invalid-reduce exception.
     */
    private static final String REDUCE_EXCEPTION_ERROR = "Color.reduce() should reject a factor smaller than 1";
    /**
     * Error message for wrong equals() result.
     */
    private static final String EQUALS_ERROR = "Color.equals() returned an unexpected result";
    /**
     * Error message for wrong equalColors() result.
     */
    private static final String EQUAL_COLORS_ERROR = "Color.equalColors() returned an unexpected result";

    /**
     * A basic mid-range color used across several tests.
     */
    private static final Color GRAY = new Color(100, 100, 100);

    /**
     * Test method for {@link Color#Color(double, double, double)}.
     */
    @Test
    void testConstructorFromComponents() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Regular positive components
        assertDoesNotThrow(() -> new Color(10, 20, 30), CONSTRUCTOR_ERROR);
        // TC02: Components greater than 255 are allowed (light intensities)
        assertDoesNotThrow(() -> new Color(500, 600, 700), CONSTRUCTOR_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: All components zero (black) is allowed
        assertDoesNotThrow(() -> new Color(0, 0, 0), CONSTRUCTOR_ERROR);
        // TC12: A single negative component is rejected
        assertThrows(IllegalArgumentException.class, () -> new Color(-1, 10, 10), NEGATIVE_COMPONENT_ERROR);
        assertThrows(IllegalArgumentException.class, () -> new Color(10, -1, 10), NEGATIVE_COMPONENT_ERROR);
        assertThrows(IllegalArgumentException.class, () -> new Color(10, 10, -1), NEGATIVE_COMPONENT_ERROR);
    }

    /**
     * Test method for {@link Color#Color(java.awt.Color)}.
     */
    @Test
    void testConstructorFromAwtColor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Constructing from a regular java.awt.Color
        Color color = assertDoesNotThrow(() -> new Color(new java.awt.Color(10, 20, 30)), CONSTRUCTOR_ERROR);
        assertEquals(new Color(10, 20, 30), color, CONSTRUCTOR_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Constructing from black
        assertEquals(Color.BLACK, new Color(new java.awt.Color(0, 0, 0)), CONSTRUCTOR_ERROR);
    }

    /**
     * Test method for {@link Color#getColor()}.
     */
    @Test
    void testGetColor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Regular in-range color
        assertEquals(new java.awt.Color(10, 20, 30), new Color(10, 20, 30).getColor(), GET_COLOR_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Components above 255 are clamped to 255
        assertEquals(new java.awt.Color(255, 255, 255), new Color(500, 600, 700).getColor(), GET_COLOR_ERROR);
        // TC12: Black stays black
        assertEquals(new java.awt.Color(0, 0, 0), Color.BLACK.getColor(), GET_COLOR_ERROR);
    }

    /**
     * Test method for {@link Color#add(Color...)}.
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Adding a single color
        assertEquals(new Color(30, 40, 50),
                assertDoesNotThrow(() -> new Color(10, 20, 30).add(new Color(20, 20, 20)), ADD_ERROR), ADD_ERROR);
        // TC02: Adding several colors at once
        assertEquals(new Color(60, 90, 120),
                new Color(10, 20, 30).add(new Color(20, 30, 40), new Color(30, 40, 50)), ADD_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Adding black does not change the color
        assertEquals(GRAY, GRAY.add(Color.BLACK), ADD_ERROR);
        // TC12: Adding no colors at all returns an equal color
        assertEquals(GRAY, GRAY.add(), ADD_ERROR);
    }

    /**
     * Test method for {@link Color#scale(Double3)}.
     */
    @Test
    void testScaleTriad() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Scaling by different positive factors per channel
        assertEquals(new Color(10, 40, 90),
                assertDoesNotThrow(() -> new Color(10, 20, 30).scale(new Double3(1, 2, 3)), SCALE_TRIAD_ERROR),
                SCALE_TRIAD_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Scaling by ONE leaves the color unchanged
        assertEquals(GRAY, GRAY.scale(Double3.ONE), SCALE_TRIAD_ERROR);
        // TC12: Scaling by ZERO results in black
        assertEquals(Color.BLACK, GRAY.scale(Double3.ZERO), SCALE_TRIAD_ERROR);
        // TC13: A negative component is rejected
        assertThrows(IllegalArgumentException.class, () -> GRAY.scale(new Double3(-1, 1, 1)), SCALE_TRIAD_EXCEPTION_ERROR);
    }

    /**
     * Test method for {@link Color#scale(double)}.
     */
    @Test
    void testScaleScalar() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Scaling by a positive factor
        assertEquals(new Color(20, 40, 60),
                assertDoesNotThrow(() -> new Color(10, 20, 30).scale(2), SCALE_SCALAR_ERROR), SCALE_SCALAR_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Scaling by one leaves the color unchanged
        assertEquals(GRAY, GRAY.scale(1), SCALE_SCALAR_ERROR);
        // TC12: Scaling by zero results in black
        assertEquals(Color.BLACK, GRAY.scale(0), SCALE_SCALAR_ERROR);
        // TC13: A negative factor is rejected
        assertThrows(IllegalArgumentException.class, () -> GRAY.scale(-1), SCALE_SCALAR_EXCEPTION_ERROR);
    }

    /**
     * Test method for {@link Color#reduce(int)}.
     */
    @Test
    void testReduce() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Reducing by a factor greater than one
        assertEquals(new Color(5, 10, 15),
                assertDoesNotThrow(() -> new Color(10, 20, 30).reduce(2), REDUCE_ERROR), REDUCE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Reducing by exactly one leaves the color unchanged
        assertEquals(GRAY, GRAY.reduce(1), REDUCE_ERROR);
        // TC12: Reducing by less than one is rejected
        assertThrows(IllegalArgumentException.class, () -> GRAY.reduce(0), REDUCE_EXCEPTION_ERROR);
    }

    /**
     * Test method for {@link Color#equals(Object)} and {@link Color#hashCode()}.
     */
    @Test
    void testEqualsAndHashCode() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Equal colors constructed independently
        assertEquals(new Color(10, 20, 30), new Color(10, 20, 30), EQUALS_ERROR);
        assertEquals(new Color(10, 20, 30).hashCode(), new Color(10, 20, 30).hashCode(),
                "Color.hashCode() should be equal for equal objects");
        // TC02: Different colors are not equal
        assertFalse(GRAY.equals(new Color(10, 10, 10)), EQUALS_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Not equal to null / a different type
        assertFalse(GRAY.equals(null), EQUALS_ERROR);
        assertFalse(GRAY.equals("not a Color"), EQUALS_ERROR);
        // TC12: Colors within the DELTA tolerance are considered equal
        assertEquals(new Color(10, 10, 10), new Color(10.2, 10.2, 10.2), EQUALS_ERROR);
    }

    /**
     * Test method for {@link Color#equalColors(Color...)}.
     */
    @Test
    void testEqualColors() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: All given colors equal this color
        assertTrue(GRAY.equalColors(new Color(100, 100, 100), new Color(100, 100, 100)), EQUAL_COLORS_ERROR);
        // TC02: One of the given colors differs
        assertFalse(GRAY.equalColors(new Color(100, 100, 100), new Color(1, 1, 1)), EQUAL_COLORS_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: No colors given is vacuously true
        assertTrue(GRAY.equalColors(), EQUAL_COLORS_ERROR);
    }

    /**
     * Test method for {@link Color#toString()}.
     */
    @Test
    void testToString() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: String representation is prefixed and reflects the RGB triad
        assertTrue(new Color(1, 2, 3).toString().startsWith("rgb:"), "Color.toString() returned an unexpected representation");
    }
}

package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Vector}.
 * The tests verify:
 * <ul>
 * <li>{@link Vector#Vector(double, double, double)}</li>
 * <li>{@link Vector#Vector(Double3)}</li>
 * <li>{@link Vector#add(Vector)}</li>
 * <li>{@link Point#subtract(Point)}</li>
 * <li>{@link Vector#scale(double)}</li>
 * <li>{@link Vector#dotProduct(Vector)}</li>
 * <li>{@link Vector#crossProduct(Vector)}</li>
 * <li>{@link Vector#lengthSquared()}</li>
 * <li>{@link Vector#length()}</li>
 * <li>{@link Vector#normalize()}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class VectorTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    VectorTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-10;

    /**
     * Error message for wrong construction result.
     */
    private static final String CONSTRUCTOR_ERROR = "Vector constructor produced an unexpected result";
    /**
     * Error message for missing zero-vector exception.
     */
    private static final String ZERO_VECTOR_ERROR = "Vector constructor should reject the zero vector";
    /**
     * Error message for wrong addition result.
     */
    private static final String ADD_ERROR = "Vector.add() returned an unexpected vector";
    /**
     * Error message for wrong subtraction result.
     */
    private static final String SUBTRACT_ERROR = "Vector.subtract() returned an unexpected vector";
    /**
     * Error message for wrong scaling result.
     */
    private static final String SCALE_ERROR = "Vector.scale() returned an unexpected vector";
    /**
     * Error message for wrong dot product.
     */
    private static final String DOT_PRODUCT_ERROR = "Vector.dotProduct() returned an unexpected value";
    /**
     * Error message for wrong cross product.
     */
    private static final String CROSS_PRODUCT_ERROR = "Vector.crossProduct() returned an unexpected vector";
    /**
     * Error message for wrong squared length.
     */
    private static final String LENGTH_SQUARED_ERROR = "Vector.lengthSquared() returned an unexpected value";
    /**
     * Error message for wrong length.
     */
    private static final String LENGTH_ERROR = "Vector.length() returned an unexpected value";
    /**
     * Error message for wrong normalization result.
     */
    private static final String NORMALIZE_ERROR = "Vector.normalize() returned an unexpected vector";

    /**
     * Base vector for most vector tests
     */
    private static final Vector V1 = new Vector(1, 2, 3);
    /**
     * Vector opposite to {@link #V1}
     */
    private static final Vector V1_OPPOSITE = new Vector(-1, -2, -3);
    /**
     * Vector parallel to {@link #V1}
     */
    private static final Vector V2 = new Vector(-2, -4, -6);
    /**
     * Vector orthogonal to {@link #V1}
     */
    private static final Vector V3 = new Vector(0, 3, -2);
    /**
     * Vector with length 3
     */
    private static final Vector V4 = new Vector(1, 2, 2);
    /**
     * Vector with negative coordinates
     */
    private static final Vector V5 = new Vector(-1, -2, -4);
    /**
     * Vector used for a Pythagorean length case
     */
    private static final Vector V6 = new Vector(0, 3, 4);

    /**
     * Test method for {@link Vector#Vector(double, double, double)}.
     */
    @Test
    void testConstructorFromCoordinates() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Constructing a regular vector from coordinates
        assertDoesNotThrow(() -> new Vector(1, 2, 3), CONSTRUCTOR_ERROR);
        // TC02: Constructing a vector with negative coordinates
        assertDoesNotThrow(() -> new Vector(-1, -2, -4), CONSTRUCTOR_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Constructing the zero vector from coordinates
        assertThrows(IllegalArgumentException.class, () -> new Vector(0, 0, 0), ZERO_VECTOR_ERROR);
        // TC12: Constructing an almost-zero vector
        assertThrows(IllegalArgumentException.class, () -> new Vector(1e-50, 0, 0), ZERO_VECTOR_ERROR);
    }

    /**
     * Test method for {@link Vector#Vector(Double3)}.
     */
    @Test
    void testConstructorFromDouble3() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Constructing a regular vector from Double3
        assertEquals(V1, assertDoesNotThrow(() -> new Vector(new Double3(1, 2, 3)), CONSTRUCTOR_ERROR), CONSTRUCTOR_ERROR);
        // TC02: Constructing a vector with negative coordinates from Double3
        assertEquals(V5, assertDoesNotThrow(() -> new Vector(new Double3(-1, -2, -4)), CONSTRUCTOR_ERROR), CONSTRUCTOR_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Constructing the zero vector from Double3.ZERO
        assertThrows(IllegalArgumentException.class, () -> new Vector(Double3.ZERO), ZERO_VECTOR_ERROR);
        // TC12: Constructing an almost-zero vector from Double3
        assertThrows(IllegalArgumentException.class, () -> new Vector(new Double3(0, 0, 1e-50)), ZERO_VECTOR_ERROR);
    }

    /**
     * Test method for {@link Vector#add(Vector)}.
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Adding two non-opposite vectors
        assertEquals(new Vector(1, 5, 1), assertDoesNotThrow(() -> V1.add(V3), ADD_ERROR), ADD_ERROR);
        // TC02: Adding a unit axis vector
        assertEquals(new Vector(2, 2, 3), assertDoesNotThrow(() -> V1.add(Vector.AXIS_X), ADD_ERROR), ADD_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Adding opposite vectors
        assertThrows(IllegalArgumentException.class, () -> V1.add(V1_OPPOSITE), ADD_ERROR);
    }

    /**
     * Test method for the inherited {@link Point#subtract(Point)} as used by {@link Vector}.
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Subtracting a different vector
        assertEquals(new Vector(1, -1, 5), assertDoesNotThrow(() -> V1.subtract(V3), SUBTRACT_ERROR), SUBTRACT_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Subtracting a vector from itself
        assertThrows(IllegalArgumentException.class, () -> V1.subtract(V1), SUBTRACT_ERROR);
    }

    /**
     * Test method for {@link Vector#scale(double)}.
     */
    @Test
    void testScale() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Scaling by a non-zero factor
        assertEquals(new Vector(2, 4, 6), assertDoesNotThrow(() -> V1.scale(2), SCALE_ERROR), SCALE_ERROR);
        // TC02: Scaling by a negative factor
        assertEquals(V1_OPPOSITE, assertDoesNotThrow(() -> V1.scale(-1), SCALE_ERROR), SCALE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Scaling by zero
        assertThrows(IllegalArgumentException.class, () -> V1.scale(0), SCALE_ERROR);
    }

    /**
     * Test method for {@link Vector#dotProduct(Vector)}.
     */
    @Test
    void testDotProduct() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Dot product of regular non-orthogonal vectors
        assertEquals(-28d, assertDoesNotThrow(() -> V1.dotProduct(V2), DOT_PRODUCT_ERROR), DELTA, DOT_PRODUCT_ERROR);
        // TC02: Dot product of a vector with itself
        assertEquals(V1.lengthSquared(), V1.dotProduct(V1), DELTA, DOT_PRODUCT_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Dot product of orthogonal vectors
        assertEquals(0d, V1.dotProduct(V3), DELTA, DOT_PRODUCT_ERROR);
    }

    /**
     * Test method for {@link Vector#crossProduct(Vector)}.
     */
    @Test
    void testCrossProduct() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Cross product of non-parallel vectors
        Vector crossProduct = assertDoesNotThrow(() -> V1.crossProduct(V3), CROSS_PRODUCT_ERROR);
        assertEquals(new Vector(-13, 2, 3), crossProduct, CROSS_PRODUCT_ERROR);
        assertEquals(0d, crossProduct.dotProduct(V1), DELTA, CROSS_PRODUCT_ERROR);
        assertEquals(0d, crossProduct.dotProduct(V3), DELTA, CROSS_PRODUCT_ERROR);
        // TC02: Cross product of the x and y axes
        assertEquals(Vector.AXIS_Z, assertDoesNotThrow(() -> Vector.AXIS_X.crossProduct(Vector.AXIS_Y), CROSS_PRODUCT_ERROR), CROSS_PRODUCT_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Cross product of parallel vectors
        assertThrows(IllegalArgumentException.class, () -> V1.crossProduct(V2), CROSS_PRODUCT_ERROR);
    }

    /**
     * Test method for {@link Vector#lengthSquared()}.
     */
    @Test
    void testLengthSquared() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Squared length of a regular vector
        assertEquals(9d, assertDoesNotThrow(V4::lengthSquared, LENGTH_SQUARED_ERROR), DELTA, LENGTH_SQUARED_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Squared length of a unit vector
        assertEquals(1d, Vector.AXIS_Z.lengthSquared(), DELTA, LENGTH_SQUARED_ERROR);
    }

    /**
     * Test method for {@link Vector#length()}.
     */
    @Test
    void testLength() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Length of a regular vector
        assertEquals(3d, assertDoesNotThrow(V4::length, LENGTH_ERROR), DELTA, LENGTH_ERROR);
        // TC02: Length in a Pythagorean case
        assertEquals(5d, assertDoesNotThrow(V6::length, LENGTH_ERROR), DELTA, LENGTH_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Length of a unit vector
        assertEquals(1d, Vector.AXIS_Y.length(), DELTA, LENGTH_ERROR);
    }

    /**
     * Test method for {@link Vector#normalize()}.
     */
    @Test
    void testNormalize() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Normalizing a regular vector
        Vector normalized = assertDoesNotThrow(V1::normalize, NORMALIZE_ERROR);
        assertEquals(1d, normalized.length(), DELTA, NORMALIZE_ERROR);
        assertEquals(V1.length(), normalized.dotProduct(V1), DELTA, NORMALIZE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Normalizing a unit vector
        assertEquals(Vector.AXIS_X, Vector.AXIS_X.normalize(), NORMALIZE_ERROR);
    }
}

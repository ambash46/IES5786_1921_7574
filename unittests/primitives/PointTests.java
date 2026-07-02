package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Point}.
 * The tests verify:
 * <ul>
 * <li>{@link Point#Point(Double3)}</li>
 * <li>{@link Point#subtract(Point)}</li>
 * <li>{@link Point#add(Vector)}</li>
 * <li>{@link Point#distanceSquared(Point)}</li>
 * <li>{@link Point#distance(Point)}</li>
 * <li>{@link Point#getCoordinates()}</li>
 * <li>{@link Point#equals(Object)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class PointTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    PointTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-10;

    /**
     * Error message for wrong subtraction result.
     */
    private static final String SUBTRACT_RESULT_ERROR = "Point.subtract() returned an unexpected vector";
    /**
     * Error message for missing subtraction exception.
     */
    private static final String SUBTRACT_EXCEPTION_ERROR = "Point.subtract() should reject subtracting the same point";
    /**
     * Error message for wrong addition result.
     */
    private static final String ADD_RESULT_ERROR = "Point.add() returned an unexpected point";
    /**
     * Error message for wrong squared distance.
     */
    private static final String DISTANCE_SQUARED_ERROR = "Point.distanceSquared() returned an unexpected value";
    /**
     * Error message for wrong distance.
     */
    private static final String DISTANCE_ERROR = "Point.distance() returned an unexpected value";
    /**
     * Error message for wrong getCoordinates() result.
     */
    private static final String COORDINATES_ERROR = "Point.getCoordinates() returned an unexpected value";
    /**
     * Error message for wrong equals() result.
     */
    private static final String EQUALS_ERROR = "Point.equals() returned an unexpected result";

    /**
     * First point for subtraction and addition tests
     */
    private static final Point P1 = new Point(1, 2, 3);
    /**
     * Second point for point operation tests
     */
    private static final Point P2 = new Point(2, 4, 6);
    /**
     * Third point for distance tests
     */
    private static final Point P3 = new Point(2, 4, 5);
    /**
     * Point with negative coordinates
     */
    private static final Point NEGATIVE_POINT = new Point(-1, -2, -3);
    /**
     * Point on the x-axis
     */
    private static final Point AXIS_POINT = new Point(1, 0, 0);
    /**
     * Point used for a Pythagorean distance case
     */
    private static final Point PYTHAGOREAN_POINT = new Point(3, 4, 0);
    /**
     * Point extremely close to the origin
     */
    private static final Point ALMOST_ZERO_POINT = new Point(1e-50, 1e-50, 1e-50);

    /**
     * Vector from {@link #P1} to {@link #P2}
     */
    private static final Vector V1 = new Vector(1, 2, 3);
    /**
     * Vector opposite to {@link #V1}
     */
    private static final Vector V1_OPPOSITE = new Vector(-1, -2, -3);
    /**
     * Negative vector for addition tests
     */
    private static final Vector NEGATIVE_VECTOR = new Vector(-2, -4, -6);

    /**
     * Test method for {@link Point#subtract(Point)}.
     */
    @Test
    void testSubtract() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Subtracting two different points
        assertEquals(V1, assertDoesNotThrow(() -> P2.subtract(P1), SUBTRACT_RESULT_ERROR), SUBTRACT_RESULT_ERROR);
        // TC02: Subtracting a point with negative coordinates
        assertEquals(new Vector(3, 6, 9), assertDoesNotThrow(() -> P2.subtract(NEGATIVE_POINT), SUBTRACT_RESULT_ERROR), SUBTRACT_RESULT_ERROR);
        // TC03: Subtracting the origin from an axis point
        assertEquals(Vector.AXIS_X, assertDoesNotThrow(() -> AXIS_POINT.subtract(Point.ZERO), SUBTRACT_RESULT_ERROR), SUBTRACT_RESULT_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Subtracting a point from itself
        assertThrows(IllegalArgumentException.class, () -> P1.subtract(P1), SUBTRACT_EXCEPTION_ERROR);
        // TC12: Subtracting an almost identical point
        assertThrows(IllegalArgumentException.class, () -> ALMOST_ZERO_POINT.subtract(Point.ZERO), SUBTRACT_EXCEPTION_ERROR);
    }

    /**
     * Test method for {@link Point#add(Vector)}.
     */
    @Test
    void testAdd() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Adding a regular vector
        assertEquals(P2, assertDoesNotThrow(() -> P1.add(V1), ADD_RESULT_ERROR), ADD_RESULT_ERROR);
        // TC02: Adding a negative vector
        assertEquals(new Point(0, 0, 0), assertDoesNotThrow(() -> P2.add(NEGATIVE_VECTOR), ADD_RESULT_ERROR), ADD_RESULT_ERROR);
        // TC03: Adding an axis vector to the origin
        assertEquals(AXIS_POINT, assertDoesNotThrow(() -> Point.ZERO.add(Vector.AXIS_X), ADD_RESULT_ERROR), ADD_RESULT_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Adding the opposite vector
        assertEquals(Point.ZERO, assertDoesNotThrow(() -> P1.add(V1_OPPOSITE), ADD_RESULT_ERROR), ADD_RESULT_ERROR);
    }

    /**
     * Test method for {@link Point#distanceSquared(Point)}.
     */
    @Test
    void testDistanceSquared() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Squared distance between two different points
        double distanceSquared = assertDoesNotThrow(() -> P1.distanceSquared(P3), DISTANCE_SQUARED_ERROR);
        assertEquals(9d, distanceSquared, DELTA, DISTANCE_SQUARED_ERROR);
        assertEquals(9d, P3.distanceSquared(P1), DELTA, DISTANCE_SQUARED_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Squared distance from a point to itself
        assertEquals(0d, P1.distanceSquared(P1), DELTA, DISTANCE_SQUARED_ERROR);
        // TC12: Squared distance on one axis only
        assertEquals(1d, Point.ZERO.distanceSquared(AXIS_POINT), DELTA, DISTANCE_SQUARED_ERROR);
    }

    /**
     * Test method for {@link Point#distance(Point)}.
     */
    @Test
    void testDistance() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Distance between two different points
        double distance = assertDoesNotThrow(() -> P1.distance(P3), DISTANCE_ERROR);
        assertEquals(3d, distance, DELTA, DISTANCE_ERROR);
        assertEquals(3d, P3.distance(P1), DELTA, DISTANCE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Distance from a point to itself
        assertEquals(0d, P1.distance(P1), DELTA, DISTANCE_ERROR);
        // TC12: Distance in a Pythagorean case
        assertEquals(5d, Point.ZERO.distance(PYTHAGOREAN_POINT), DELTA, DISTANCE_ERROR);
    }

    /**
     * Test method for {@link Point#Point(Double3)}.
     */
    @Test
    void testConstructorFromDouble3() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Constructing a regular point from a Double3
        assertEquals(P1, new Point(new Double3(1, 2, 3)), "Point(Double3) constructor produced an unexpected point");

        // =============== Boundary Values Tests ==================
        // TC11: Constructing the origin from Double3.ZERO
        assertEquals(Point.ZERO, new Point(Double3.ZERO), "Point(Double3) constructor produced an unexpected point");
    }

    /**
     * Test method for {@link Point#getCoordinates()}.
     */
    @Test
    void testGetCoordinates() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Coordinates of a regular point
        assertEquals(new Double3(1, 2, 3), P1.getCoordinates(), COORDINATES_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Coordinates of the origin
        assertEquals(Double3.ZERO, Point.ZERO.getCoordinates(), COORDINATES_ERROR);
    }

    /**
     * Test method for {@link Point#equals(Object)}.
     */
    @Test
    void testEquals() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Equal points constructed independently
        assertEquals(new Point(1, 2, 3), P1, EQUALS_ERROR);
        // TC02: Different points are not equal
        assertNotEquals(P1, P2, EQUALS_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Not equal to null / a different type
        assertNotEquals(P1, null, EQUALS_ERROR);
        assertNotEquals(P1, "not a Point", EQUALS_ERROR);
        // TC12: A Point never equals a Vector with the same coordinates
        assertNotEquals(new Point(1, 2, 3), V1, EQUALS_ERROR);
    }
}

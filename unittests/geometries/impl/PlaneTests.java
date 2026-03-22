package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Plane}.
 * The tests verify:
 * <ul>
 * <li>{@link Plane#Plane(Point, Vector)}</li>
 * <li>{@link Plane#Plane(Point, Point, Point)}</li>
 * <li>{@link Plane#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
class PlaneTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    PlaneTests() { /* to satisfy JavaDoc generator */ }

    /**
     * First point used in plane tests
     */
    private static final Point POINT1 = new Point(1, 0, 0);
    /**
     * Second point used in plane tests
     */
    private static final Point POINT2 = new Point(0, 1, 0);
    /**
     * Third point used in plane tests
     */
    private static final Point POINT3 = new Point(0, 0, 1);
    /**
     * Additional point on the plane that is not a constructor point
     */
    private static final Point POINT4 = new Point(0.25, 0.25, 0.5);
    /**
     * Point located on the line through the first two points
     */
    private static final Point POINT5 = new Point(2, -1, 0);
    /**
     * Non-normalized normal vector used in plane tests
     */
    private static final Vector VECTOR1 = new Vector(1, 2, 3);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for wrong plane construction from point and vector
     */
    private static final String ERROR_CONSTRUCTOR_POINT_VECTOR = "Failed constructing a correct plane from point and vector";
    /**
     * Error message for wrong plane construction from three points
     */
    private static final String ERROR_CONSTRUCTOR_POINT_POINT_POINT = "Failed constructing a correct plane";
    /**
     * Error message for duplicate points in plane construction
     */
    private static final String ERROR_DUPLICATE_POINTS = "Constructed a plane with duplicate points";
    /**
     * Error message for identical points in plane construction
     */
    private static final String ERROR_IDENTICAL_POINTS = "Constructed a plane with all points identical";
    /**
     * Error message for collinear points in plane construction
     */
    private static final String ERROR_COLLINEAR_POINTS = "Constructed a plane with collinear points";
    /**
     * Error message for wrong getNormal execution
     */
    private static final String ERROR_GET_NORMAL = "getNormal() threw unexpected exception";
    /**
     * Error message for non-unit plane normal
     */
    private static final String ERROR_NORMAL_LENGTH = "Plane normal is not a unit vector";
    /**
     * Error message for wrong orthogonality to the first edge
     */
    private static final String ERROR_ORTHOGONAL_EDGE1 = "Plane normal is not orthogonal to edge 1";
    /**
     * Error message for wrong orthogonality to the second edge
     */
    private static final String ERROR_ORTHOGONAL_EDGE2 = "Plane normal is not orthogonal to edge 2";
    /**
     * Error message for wrong normal direction relative to the given vector
     */
    private static final String ERROR_PARALLEL_VECTOR = "Plane normal is not parallel to the given vector";

    /**
     * Test method for {@link Plane#Plane(Point, Vector)}.
     * Verifies correct plane construction from a point and a normal vector.
     */
    @Test
    void testConstructorPointVector() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct plane from a point and a non-normalized normal vector
        Plane plane = assertDoesNotThrow(() -> new Plane(POINT1, VECTOR1),
                ERROR_CONSTRUCTOR_POINT_VECTOR);
        Vector result = plane.getNormal(POINT1);
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        assertEquals(VECTOR1.length(), result.dotProduct(VECTOR1), DELTA,
                ERROR_PARALLEL_VECTOR);
    }

    /**
     * Test method for {@link Plane#Plane(Point, Point, Point)}.
     * Verifies correct and incorrect plane constructions.
     */
    @Test
    void testConstructorPointPointPoint() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct plane from three non-collinear points
        assertDoesNotThrow(() -> new Plane(POINT1, POINT2, POINT3),
                ERROR_CONSTRUCTOR_POINT_POINT_POINT);

        // =============== Boundary Values Tests ==================

        // TC11: First and second points are the same
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT1, POINT1, POINT3),
                ERROR_DUPLICATE_POINTS);

        // TC12: First and third points are the same
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT1, POINT2, POINT1),
                ERROR_DUPLICATE_POINTS);

        // TC13: Second and third points are the same
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT1, POINT2, POINT2),
                ERROR_DUPLICATE_POINTS);

        // TC14: All three points are the same
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT1, POINT1, POINT1),
                ERROR_IDENTICAL_POINTS);

        // TC15: All points are on the same line
        assertThrows(IllegalArgumentException.class, () -> new Plane(POINT1, POINT2, POINT5),
                ERROR_COLLINEAR_POINTS);
    }

    /**
     * Test method for {@link Plane#getNormal(Point)}.
     * Verifies that the returned normal vector is unit length and orthogonal
     * to the plane edges.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        Plane plane = new Plane(POINT1, POINT2, POINT3);

        // TC01: A point on the plane that is not a constructor point
        assertDoesNotThrow(() -> plane.getNormal(POINT4), ERROR_GET_NORMAL);
        Vector result = plane.getNormal(POINT4);
        // Ensure |n| = 1
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        // Ensure normal is orthogonal to the plane edges
        assertEquals(0d, result.dotProduct(POINT2.subtract(POINT1)), DELTA, ERROR_ORTHOGONAL_EDGE1);
        assertEquals(0d, result.dotProduct(POINT3.subtract(POINT1)), DELTA, ERROR_ORTHOGONAL_EDGE2);

        // =============== Boundary Values Tests ==================

        // TC11: A constructor point on the plane
        // Ensure method does not throw exception
        assertDoesNotThrow(() -> plane.getNormal(POINT1), ERROR_GET_NORMAL);
        result = plane.getNormal(POINT1);
        // Ensure |n| = 1
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        // Ensure normal is orthogonal to the plane edges
        assertEquals(0d, result.dotProduct(POINT2.subtract(POINT1)), DELTA, ERROR_ORTHOGONAL_EDGE1);
        assertEquals(0d, result.dotProduct(POINT3.subtract(POINT1)), DELTA, ERROR_ORTHOGONAL_EDGE2);
    }
}

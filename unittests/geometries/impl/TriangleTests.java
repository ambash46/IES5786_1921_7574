package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Triangle}.
 * The tests verify:
 * <ul>
 * <li>{@link Triangle#Triangle(Point, Point, Point)}</li>
 * <li>{@link Triangle#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *  * @author Ambash and Elyasaf
 */
class TriangleTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    TriangleTests() { /* to satisfy JavaDoc generator */ }

    /**
     * First vertex used in triangle tests
     */
    private static final Point POINT1 = new Point(1, 0, 0);
    /**
     * Second vertex used in triangle tests
     */
    private static final Point POINT2 = new Point(0, 1, 0);
    /**
     * Third vertex used in triangle tests
     */
    private static final Point POINT3 = new Point(0, 0, 1);
    /**
     * Point inside the triangle and not on a vertex
     */
    private static final Point POINT4 = new Point(0.25, 0.25, 0.5);

    /**
     * Point equal to the first vertex
     */
    private static final Point POINT5 = new Point(1, 0, 0);
    /**
     * Point located on the line through the first two vertices
     */
    private static final Point POINT6 = new Point(2, -1, 0);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for wrong triangle construction
     */
    private static final String ERROR_CONSTRUCTOR = "Failed constructing a correct triangle";
    /**
     * Error message for duplicate points in triangle construction
     */
    private static final String ERROR_DUPLICATE_POINTS = "Constructed a triangle with duplicate points";
    /**
     * Error message for collinear points in triangle construction
     */
    private static final String ERROR_COLLINEAR_POINTS = "Constructed a triangle with collinear points";
    /**
     * Error message for wrong getNormal execution
     */
    private static final String ERROR_GET_NORMAL = "getNormal() threw unexpected exception";
    /**
     * Error message for non-unit triangle normal
     */
    private static final String ERROR_NORMAL_LENGTH = "Triangle normal is not a unit vector";
    /**
     * Error message for wrong orthogonality to the first edge
     */
    private static final String ERROR_ORTHOGONAL_EDGE1 = "Triangle normal is not orthogonal to edge 1";
    /**
     * Error message for wrong orthogonality to the second edge
     */
    private static final String ERROR_ORTHOGONAL_EDGE2 = "Triangle normal is not orthogonal to edge 2";

    /**
     * Test method for {@link Triangle#Triangle(Point, Point, Point)}.
     * Verifies correct and incorrect triangle constructions.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct triangle from three non-collinear points
        assertDoesNotThrow(() -> new Triangle(POINT1, POINT2, POINT3), ERROR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================

        // TC11: First and second points are the same
        assertThrows(IllegalArgumentException.class, () -> new Triangle(POINT1, POINT5, POINT3),
                ERROR_DUPLICATE_POINTS);

        // TC12: All points are on the same line
        assertThrows(IllegalArgumentException.class, () -> new Triangle(POINT1, POINT2, POINT6),
                ERROR_COLLINEAR_POINTS);
    }

    /**
     * Test method for {@link Triangle#getNormal(Point)}.
     * Verifies that the returned normal vector is unit length and orthogonal
     * to the triangle edges.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        Triangle triangle = new Triangle(POINT1, POINT2, POINT3);
        // TC01: A point inside the triangle that is not a vertex
        assertDoesNotThrow(() -> triangle.getNormal(POINT4), ERROR_GET_NORMAL);
        Vector result = triangle.getNormal(POINT4);
        // Ensure |n| = 1
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        // Ensure normal is orthogonal to the triangle edges
        assertEquals(0d, result.dotProduct(POINT2.subtract(POINT1)), DELTA, ERROR_ORTHOGONAL_EDGE1);
        assertEquals(0d, result.dotProduct(POINT3.subtract(POINT1)), DELTA, ERROR_ORTHOGONAL_EDGE2);
    }
}

package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link Tube}.
 * The tests verify:
 * <ul>
 * <li>{@link Tube#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
class TubeTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    TubeTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Axis ray used in tube tests
     */
    private static final Ray AXIS = new Ray(Point.ZERO, Vector.AXIS_Z);
    /**
     * Point on the tube surface opposite an interior axis point
     */
    private static final Point POINT1 = new Point(1, 0, 1);
    /**
     * Point on the tube surface opposite another interior axis point
     */
    private static final Point POINT2 = new Point(0, 1, 2);
    /**
     * Point on the tube surface opposite the axis head
     */
    private static final Point POINT3 = new Point(1, 0, 0);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for wrong getNormal execution
     */
    private static final String ERROR_GET_NORMAL = "getNormal() threw unexpected exception";
    /**
     * Error message for non-unit tube normal
     */
    private static final String ERROR_NORMAL_LENGTH = "Tube normal is not a unit vector";
    /**
     * Error message for wrong first tube normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION1 = "Tube normal has wrong direction for the first point";
    /**
     * Error message for wrong second tube normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION2 = "Tube normal has wrong direction for the second point";
    /**
     * Error message for wrong boundary tube normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION3 = "Tube normal has wrong direction for the boundary point";

    /**
     * Test method for {@link Tube#getNormal(Point)}.
     * Verifies that the returned normal vector is unit length and points from
     * the axis to the given surface point.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        Tube tube = new Tube(1d, AXIS);

        // TC01: A point on the tube surface opposite an interior axis point
        assertDoesNotThrow(() -> tube.getNormal(POINT1), ERROR_GET_NORMAL);
        Vector result = tube.getNormal(POINT1);
        // Ensure |n| = 1
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        // Ensure normal direction is correct
        assertEquals(Vector.AXIS_X, result, ERROR_NORMAL_DIRECTION1);

        // TC02: A point on the tube surface opposite another interior axis point
        assertDoesNotThrow(() -> tube.getNormal(POINT2), ERROR_GET_NORMAL);
        result = tube.getNormal(POINT2);
        // Ensure |n| = 1
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        // Ensure normal direction is correct
        assertEquals(Vector.AXIS_Y, result, ERROR_NORMAL_DIRECTION2);

        // =============== Boundary Values Tests ==================

        // TC11: A point on the tube surface opposite the axis head
        assertDoesNotThrow(() -> tube.getNormal(POINT3), ERROR_GET_NORMAL);
        result = tube.getNormal(POINT3);
        // Ensure |n| = 1
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        // Ensure normal direction is correct
        assertEquals(Vector.AXIS_X, result, ERROR_NORMAL_DIRECTION3);
    }
}

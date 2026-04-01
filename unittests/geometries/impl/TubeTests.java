package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Tube}.
 * The tests verify:
 * <ul>
 * <li>{@link Tube#Tube(double, Ray)}</li>
 * <li>{@link Tube#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author Ambash and Elyasaf
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
     * Error message for wrong tube construction
     */
    private static final String ERROR_CONSTRUCTOR = "Failed constructing a correct tube";
    /**
     * Error message for non-positive tube radius
     */
    private static final String ERROR_RADIUS = "Constructed a tube with a non-positive radius";
    /**
     * Error message for wrong getNormal execution
     */
    private static final String ERROR_GET_NORMAL = "Tube.getNormal() should not throw for valid points on the tube";
    /**
     * Error message for non-unit tube normal
     */
    private static final String ERROR_NORMAL_LENGTH = "Tube.getNormal() should return a unit-length normal vector";
    /**
     * Error message for wrong first tube normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION1 =
            "Tube.getNormal() returned the wrong normal for a side point opposite an interior axis point";
    /**
     * Error message for wrong second tube normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION2 =
            "Tube.getNormal() returned the wrong normal for a second side point opposite an interior axis point";
    /**
     * Error message for wrong boundary tube normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION3 =
            "Tube.getNormal() returned the wrong normal for the boundary point opposite the axis head";

    /**
     * Test method for {@link Tube#Tube(double, Ray)}.
     * Verifies correct and incorrect tube constructions.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct tube with positive radius
        assertDoesNotThrow(() -> new Tube(1d, AXIS), ERROR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================

        // TC11: Zero radius
        assertThrows(IllegalArgumentException.class, () -> new Tube(0d, AXIS), ERROR_RADIUS);

        // TC12: Negative radius
        assertThrows(IllegalArgumentException.class, () -> new Tube(-1d, AXIS), ERROR_RADIUS);
    }

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

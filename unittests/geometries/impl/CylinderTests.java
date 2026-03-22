package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link Cylinder}.
 * The tests verify:
 * <ul>
 * <li>{@link Cylinder#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
class CylinderTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    CylinderTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Axis ray used in cylinder tests
     */
    private static final Ray AXIS = new Ray(Point.ZERO, Vector.AXIS_Z);
    /**
     * Cylinder used in getNormal tests
     */
    private static final Cylinder CYLINDER = new Cylinder(1d, AXIS, 2d);

    /**
     * Point on the curved surface opposite an interior axis point
     */
    private static final Point POINT1 = new Point(1, 0, 1);
    /**
     * Point on the bottom base and not at the center
     */
    private static final Point POINT2 = new Point(0.5, 0, 0);
    /**
     * Point on the top base and not at the center
     */
    private static final Point POINT3 = new Point(0.5, 0, 2);
    /**
     * Point on the curved surface opposite the axis head
     */
    private static final Point POINT4 = new Point(1, 0, 0);
    /**
     * Point on the curved surface opposite the top axis point
     */
    private static final Point POINT5 = new Point(1, 0, 2);
    /**
     * Center point of the bottom base
     */
    private static final Point POINT6 = Point.ZERO;
    /**
     * Center point of the top base
     */
    private static final Point POINT7 = new Point(0, 0, 2);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for wrong getNormal execution
     */
    private static final String ERROR_GET_NORMAL = "getNormal() threw unexpected exception";
    /**
     * Error message for non-unit cylinder normal
     */
    private static final String ERROR_NORMAL_LENGTH = "Cylinder normal is not a unit vector";
    /**
     * Error message for wrong first cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION1 = "Cylinder normal has wrong direction for the first point";
    /**
     * Error message for wrong second cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION2 = "Cylinder normal has wrong direction for the second point";
    /**
     * Error message for wrong third cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION3 = "Cylinder normal has wrong direction for the third point";
    /**
     * Error message for wrong fourth cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION4 = "Cylinder normal has wrong direction for the fourth point";
    /**
     * Error message for wrong fifth cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION5 = "Cylinder normal has wrong direction for the fifth point";
    /**
     * Error message for wrong sixth cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION6 = "Cylinder normal has wrong direction for the sixth point";
    /**
     * Error message for wrong seventh cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION7 = "Cylinder normal has wrong direction for the seventh point";

    /**
     * Test method for {@link Cylinder#getNormal(Point)}.
     * Verifies that the returned normal vector is unit length and points in
     * the correct direction on the curved surface and on both bases.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: A point on the curved surface opposite an interior axis point
        assertDoesNotThrow(() -> CYLINDER.getNormal(POINT1), ERROR_GET_NORMAL);
        Vector result = CYLINDER.getNormal(POINT1);
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        assertEquals(Vector.AXIS_X, result, ERROR_NORMAL_DIRECTION1);

        // TC02: A point on the bottom base and not at the center
        assertDoesNotThrow(() -> CYLINDER.getNormal(POINT2), ERROR_GET_NORMAL);
        result = CYLINDER.getNormal(POINT2);
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        assertEquals(Vector.AXIS_Z.scale(-1), result, ERROR_NORMAL_DIRECTION2);

        // TC03: A point on the top base and not at the center
        assertDoesNotThrow(() -> CYLINDER.getNormal(POINT3), ERROR_GET_NORMAL);
        result = CYLINDER.getNormal(POINT3);
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        assertEquals(Vector.AXIS_Z, result, ERROR_NORMAL_DIRECTION3);

        // =============== Boundary Values Tests ==================

        // TC11: A point on the curved surface opposite the axis head
        assertDoesNotThrow(() -> CYLINDER.getNormal(POINT4), ERROR_GET_NORMAL);
        result = CYLINDER.getNormal(POINT4);
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        assertEquals(Vector.AXIS_Z.scale(-1), result, ERROR_NORMAL_DIRECTION4);

        // TC12: A point on the curved surface opposite the top axis point
        assertDoesNotThrow(() -> CYLINDER.getNormal(POINT5), ERROR_GET_NORMAL);
        result = CYLINDER.getNormal(POINT5);
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        assertEquals(Vector.AXIS_Z, result, ERROR_NORMAL_DIRECTION5);

        // TC13: The center point of the bottom base
        assertDoesNotThrow(() -> CYLINDER.getNormal(POINT6), ERROR_GET_NORMAL);
        result = CYLINDER.getNormal(POINT6);
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        assertEquals(Vector.AXIS_Z.scale(-1), result, ERROR_NORMAL_DIRECTION6);

        // TC14: The center point of the top base
        assertDoesNotThrow(() -> CYLINDER.getNormal(POINT7), ERROR_GET_NORMAL);
        result = CYLINDER.getNormal(POINT7);
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        assertEquals(Vector.AXIS_Z, result, ERROR_NORMAL_DIRECTION7);
    }
}

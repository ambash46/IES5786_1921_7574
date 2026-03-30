package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
 *  * @author Ambash and Elyasaf
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
     * Plane used in findIntersections tests.
     */
    private static final Plane INTERSECTION_PLANE = new Plane(new Point(2, 3, 4), new Vector(2, 3, 4));
    /**
     * Reference point used in plane intersection tests.
     */
    private static final Point REF_POINT = new Point(2, 3, 4);
    /**
     * Additional point on the plane used in plane intersection tests.
     */
    private static final Point PLANE_POINT = new Point(4, 3, 3);
    /**
     * Point above the plane on the line through the reference point.
     */
    private static final Point ABOVE_REF = new Point(4, 6, 8);
    /**
     * Point above the plane not on the line through the reference point.
     */
    private static final Point ABOVE_NON_REF = new Point(6, 4, 8);
    /**
     * Additional point above the plane used with the negative normal direction.
     */
    private static final Point ABOVE_NON_REF_NEG = new Point(6, 6, 7);
    /**
     * Point below the plane on the line through the reference point.
     */
    private static final Point BELOW_REF = new Point(-2, -3, -4);
    /**
     * Point below the plane not on the line through the reference point.
     */
    private static final Point BELOW_NON_REF = new Point(2, 0, -1);
    /**
     * Point above the plane for a perpendicular-to-normal general case.
     */
    private static final Point ABOVE_PARALLEL_GENERAL = new Point(1, 8, 8);
    /**
     * Point below the plane for a perpendicular-to-normal general case.
     */
    private static final Point BELOW_PARALLEL_GENERAL = new Point(-5, -1, -4);
    /**
     * Point on the plane before the reference point along a parallel line.
     */
    private static final Point ON_PLANE_BEFORE_REF = new Point(-1, 5, 4);
    /**
     * Point on the plane after the reference point along a parallel line.
     */
    private static final Point ON_PLANE_AFTER_REF = new Point(5, 1, 4);
    /**
     * Point below the plane before the reference point for a general ray.
     */
    private static final Point BELOW_GENERAL_REF = new Point(-3, 2, 0);
    /**
     * Point below the plane not on the line through the reference point for a general ray.
     */
    private static final Point BELOW_GENERAL_NON_REF = new Point(-1, 2, -1);
    /**
     * Point above the plane after the reference point for a general ray.
     */
    private static final Point ABOVE_GENERAL_REF = new Point(7, 4, 8);
    /**
     * Point above the plane not on the line through the reference point for a general ray.
     */
    private static final Point ABOVE_GENERAL_NON_REF = new Point(6, 3, 7);
    /**
     * Point above the plane before the reference point for a general ray.
     */
    private static final Point ABOVE_GENERAL_REF_NEG = new Point(1, 8, 8);
    /**
     * Point above the plane not on the line through the reference point for a negative general ray.
     */
    private static final Point ABOVE_GENERAL_NON_REF_NEG = new Point(3, 8, 7);
    /**
     * Point below the plane after the reference point for a negative general ray.
     */
    private static final Point BELOW_GENERAL_REF_NEG = new Point(3, -2, 0);
    /**
     * Point below the plane not on the line through the reference point for a negative general ray.
     */
    private static final Point BELOW_GENERAL_NON_REF_NEG = new Point(5, -2, -1);
    /**
     * Error message for wrong plane intersection result.
     */
    private static final String ERROR_PLANE_INTERSECTION = "Wrong plane intersection result";

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

    /**
     * Test method for {@link Plane#findIntersections(primitives.Ray)}.
     * Verifies plane-ray intersections in three groups:
     * rays parallel to the normal, rays perpendicular to the normal,
     * and general rays.
     */
    @Test
    void testFindIntersections() {

        // ============ Group 1: Ray parallel to normal ============
        // ============ Equivalence Partitions Tests ==============

        // TC01: Direction +normal, starts above the plane, line through reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(ABOVE_REF, new Vector(2, 3, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC02: Direction +normal, starts above the plane, line not through reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(ABOVE_NON_REF, new Vector(2, 3, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC03: Direction +normal, starts below the plane, line through reference point (1 point)
        assertEquals(java.util.List.of(REF_POINT),
                INTERSECTION_PLANE.findIntersections(new Ray(BELOW_REF, new Vector(2, 3, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC04: Direction +normal, starts below the plane, line not through reference point (1 point)
        assertEquals(java.util.List.of(PLANE_POINT),
                INTERSECTION_PLANE.findIntersections(new Ray(BELOW_NON_REF, new Vector(2, 3, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC05: Direction -normal, starts above the plane, line through reference point (1 point)
        assertEquals(java.util.List.of(REF_POINT),
                INTERSECTION_PLANE.findIntersections(new Ray(ABOVE_REF, new Vector(-2, -3, -4))),
                ERROR_PLANE_INTERSECTION);

        // TC06: Direction -normal, starts above the plane, line not through reference point (1 point)
        assertEquals(java.util.List.of(PLANE_POINT),
                INTERSECTION_PLANE.findIntersections(new Ray(ABOVE_NON_REF_NEG, new Vector(-2, -3, -4))),
                ERROR_PLANE_INTERSECTION);

        // TC07: Direction -normal, starts below the plane, line through reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(BELOW_REF, new Vector(-2, -3, -4))),
                ERROR_PLANE_INTERSECTION);

        // TC08: Direction -normal, starts below the plane, line not through reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(BELOW_NON_REF, new Vector(-2, -3, -4))),
                ERROR_PLANE_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC09: Direction +normal, starts on the plane at the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(REF_POINT, new Vector(2, 3, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC10: Direction +normal, starts on the plane away from the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(PLANE_POINT, new Vector(2, 3, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC11: Direction -normal, starts on the plane at the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(REF_POINT, new Vector(-2, -3, -4))),
                ERROR_PLANE_INTERSECTION);

        // TC12: Direction -normal, starts on the plane away from the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(PLANE_POINT, new Vector(-2, -3, -4))),
                ERROR_PLANE_INTERSECTION);

        // ============ Group 2: Ray perpendicular to normal ============
        // ============ Equivalence Partitions Tests ==============

        // TC13: Starts above the plane, projected line passes through the plane area in a general case (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(ABOVE_PARALLEL_GENERAL, new Vector(3, -2, 0))),
                ERROR_PLANE_INTERSECTION);

        // TC14: Starts below the plane, projected line passes through the plane area in a general case (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(BELOW_PARALLEL_GENERAL, new Vector(3, -2, 0))),
                ERROR_PLANE_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC15: Starts on the plane, line passes the reference point, before it (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(ON_PLANE_BEFORE_REF, new Vector(3, -2, 0))),
                ERROR_PLANE_INTERSECTION);

        // TC16: Starts on the plane, line passes the reference point, at it (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(REF_POINT, new Vector(3, -2, 0))),
                ERROR_PLANE_INTERSECTION);

        // TC17: Starts on the plane, line passes the reference point, after it (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(ON_PLANE_AFTER_REF, new Vector(3, -2, 0))),
                ERROR_PLANE_INTERSECTION);

        // TC18: Starts on the plane, line does not pass the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(PLANE_POINT, new Vector(3, -2, 0))),
                ERROR_PLANE_INTERSECTION);

        // ============ Group 3: General ray ============
        // ============ Equivalence Partitions Tests ==============

        // TC19: Positive normal component, starts below, line passes the reference point, before it (1 point)
        assertEquals(java.util.List.of(REF_POINT),
                INTERSECTION_PLANE.findIntersections(new Ray(BELOW_GENERAL_REF, new Vector(5, 1, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC20: Positive normal component, starts below, line does not pass the reference point (1 point)
        assertEquals(java.util.List.of(PLANE_POINT),
                INTERSECTION_PLANE.findIntersections(new Ray(BELOW_GENERAL_NON_REF, new Vector(5, 1, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC21: Positive normal component, starts above, line passes the reference point, after it (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(ABOVE_GENERAL_REF, new Vector(5, 1, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC22: Positive normal component, starts above, line does not pass the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(ABOVE_GENERAL_NON_REF, new Vector(5, 1, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC23: Negative normal component, starts above, line passes the reference point, before it (1 point)
        assertEquals(java.util.List.of(REF_POINT),
                INTERSECTION_PLANE.findIntersections(new Ray(ABOVE_GENERAL_REF_NEG, new Vector(1, -5, -4))),
                ERROR_PLANE_INTERSECTION);

        // TC24: Negative normal component, starts above, line does not pass the reference point (1 point)
        assertEquals(java.util.List.of(PLANE_POINT),
                INTERSECTION_PLANE.findIntersections(new Ray(ABOVE_GENERAL_NON_REF_NEG, new Vector(1, -5, -4))),
                ERROR_PLANE_INTERSECTION);

        // TC25: Negative normal component, starts below, line passes the reference point, after it (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(BELOW_GENERAL_REF_NEG, new Vector(1, -5, -4))),
                ERROR_PLANE_INTERSECTION);

        // TC26: Negative normal component, starts below, line does not pass the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(BELOW_GENERAL_NON_REF_NEG, new Vector(1, -5, -4))),
                ERROR_PLANE_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC27: Positive normal component, starts on the plane at the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(REF_POINT, new Vector(5, 1, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC28: Positive normal component, starts on the plane away from the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(PLANE_POINT, new Vector(5, 1, 4))),
                ERROR_PLANE_INTERSECTION);

        // TC29: Negative normal component, starts on the plane at the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(REF_POINT, new Vector(1, -5, -4))),
                ERROR_PLANE_INTERSECTION);

        // TC30: Negative normal component, starts on the plane away from the reference point (0 points)
        assertNull(INTERSECTION_PLANE.findIntersections(new Ray(PLANE_POINT, new Vector(1, -5, -4))),
                ERROR_PLANE_INTERSECTION);
    }
}

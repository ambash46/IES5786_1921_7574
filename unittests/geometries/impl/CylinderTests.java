package geometries.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Cylinder}.
 * The tests verify:
 * <ul>
 * <li>{@link Cylinder#Cylinder(double, Ray, double)}</li>
 * <li>{@link Cylinder#getNormal(Point)}</li>
 * <li>{@link Cylinder#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author Ambash and Elyasaf
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
     * Error message for wrong cylinder construction
     */
    private static final String ERROR_CONSTRUCTOR = "Failed constructing a correct cylinder";
    /**
     * Error message for non-positive cylinder radius
     */
    private static final String ERROR_RADIUS = "Constructed a cylinder with a non-positive radius";
    /**
     * Error message for non-positive cylinder height
     */
    private static final String ERROR_HEIGHT = "Constructed a cylinder with a non-positive height";
    /**
     * Error message for wrong getNormal execution
     */
    private static final String ERROR_GET_NORMAL = "Cylinder.getNormal() should not throw for valid points on the cylinder";
    /**
     * Error message for non-unit cylinder normal
     */
    private static final String ERROR_NORMAL_LENGTH = "Cylinder.getNormal() should return a unit-length normal vector";
    /**
     * Error message for wrong first cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION1 =
            "Cylinder.getNormal() returned the wrong normal on the curved surface";
    /**
     * Error message for wrong second cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION2 =
            "Cylinder.getNormal() returned the wrong normal on the bottom base";
    /**
     * Error message for wrong third cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION3 =
            "Cylinder.getNormal() returned the wrong normal on the top base";
    /**
     * Error message for wrong fourth cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION4 =
            "Cylinder.getNormal() returned the wrong normal at the bottom rim";
    /**
     * Error message for wrong fifth cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION5 =
            "Cylinder.getNormal() returned the wrong normal at the top rim";
    /**
     * Error message for wrong sixth cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION6 =
            "Cylinder.getNormal() returned the wrong normal at the bottom-base center";
    /**
     * Error message for wrong seventh cylinder normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION7 =
            "Cylinder.getNormal() returned the wrong normal at the top-base center";
    /**
     * Error message for wrong intersection result for a ray parallel to the axis.
     */
    private static final String ERROR_INTERSECTION_PARALLEL =
            "Cylinder.findIntersections() returned wrong result for a ray parallel to the axis";
    /**
     * Error message for wrong intersection result for a ray that misses the cylinder.
     */
    private static final String ERROR_INTERSECTION_MISS =
            "Cylinder.findIntersections() returned wrong result for a ray that misses the cylinder";

    /**
     * Direction of the cylinder axis used in the intersection tests.
     */
    private static final Vector INTERSECTION_AXIS_DIRECTION = new Vector(0, 3, 4).normalize();
    /**
     * Cylinder axis used in the intersection tests.
     */
    private static final Ray INTERSECTION_AXIS = new Ray(new Point(1, 2, 3), INTERSECTION_AXIS_DIRECTION);
    /**
     * Cylinder radius used in the intersection tests.
     */
    private static final double INTERSECTION_RADIUS = 4d;
    /**
     * Cylinder height used in the intersection tests.
     */
    private static final double INTERSECTION_HEIGHT = 10d;
    /**
     * Cylinder used in the intersection tests.
     */
    private static final Cylinder INTERSECTION_CYLINDER =
            new Cylinder(INTERSECTION_RADIUS, INTERSECTION_AXIS, INTERSECTION_HEIGHT);
    /**
     * Center of the bottom cap of the intersection cylinder.
     */
    private static final Point BOTTOM_CAP_CENTER = new Point(1, 2, 3);
    /**
     * Center of the top cap of the intersection cylinder.
     */
    private static final Point TOP_CAP_CENTER = new Point(1, 8, 11);

    /**
     * Verifies the exact list of intersection points up to a small tolerance.
     *
     * @param expected expected intersection points in ray order
     * @param actual   actual intersection list
     * @param message  assertion message
     */
    private static void assertIntersectionsEquals(List<Point> expected, List<Point> actual, String message) {
        assertNotNull(actual, message);
        assertEquals(expected.size(), actual.size(), message);
        for (int i = 0; i < expected.size(); ++i) {
            assertEquals(0d, expected.get(i).distance(actual.get(i)), DELTA, message);
        }
    }

    /**
     * Test method for {@link Cylinder#Cylinder(double, Ray, double)}.
     * Verifies correct and incorrect cylinder constructions.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct cylinder with positive radius and height
        assertDoesNotThrow(() -> new Cylinder(1d, AXIS, 2d), ERROR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================

        // TC11: Zero radius
        assertThrows(IllegalArgumentException.class, () -> new Cylinder(0d, AXIS, 2d), ERROR_RADIUS);

        // TC12: Negative radius
        assertThrows(IllegalArgumentException.class, () -> new Cylinder(-1d, AXIS, 2d), ERROR_RADIUS);

        // TC13: Zero height
        assertThrows(IllegalArgumentException.class, () -> new Cylinder(1d, AXIS, 0d), ERROR_HEIGHT);

        // TC14: Negative height
        assertThrows(IllegalArgumentException.class, () -> new Cylinder(1d, AXIS, -2d), ERROR_HEIGHT);
    }

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

    /**
     * Group 1: ray parallel to the cylinder axis, same direction (+).
     * A parallel ray never intersects the mantle. It can only enter or exit through
     * the flat caps when the ray passes through the cylinder's interior cross-section.
     * Four radial positions are tested: outside, on the mantle, inside (not on axis), on the axis.
     * For each, the ray head is placed at five axial positions:
     * before the bottom cap / on the bottom cap / between the caps / on the top cap / after the top cap.
     */
    @Test
    void testFindIntersectionsGroup1ParallelToAxis() {
        Vector direction   = INTERSECTION_AXIS_DIRECTION;
        Point outsideBase  = new Point(7, 2, 3);   // distance 6 from axis > radius 4
        Point mantleBase   = new Point(5, 2, 3);   // distance 4 from axis = radius
        Point insideBase   = new Point(3, 2, 3);   // distance 2 from axis < radius
        Point topCapInside = new Point(3, 8, 11);  // insideBase projected onto the top cap

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray head outside the cylinder, before the bottom cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(outsideBase.add(direction.scale(-5)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC02: Ray head outside the cylinder, on the bottom cap plane (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(outsideBase, direction)), ERROR_INTERSECTION_PARALLEL);
        // TC03: Ray head outside the cylinder, between the caps (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(outsideBase.add(direction.scale(5)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC04: Ray head outside the cylinder, on the top cap plane (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(outsideBase.add(direction.scale(10)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC05: Ray head outside the cylinder, after the top cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(outsideBase.add(direction.scale(15)), direction)), ERROR_INTERSECTION_PARALLEL);

        // TC06: Ray head on the mantle, before the bottom cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleBase.add(direction.scale(-5)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC07: Ray head on the mantle, on the bottom cap plane (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleBase, direction)), ERROR_INTERSECTION_PARALLEL);
        // TC08: Ray head on the mantle, between the caps (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleBase.add(direction.scale(5)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC09: Ray head on the mantle, on the top cap plane (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleBase.add(direction.scale(10)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC10: Ray head on the mantle, after the top cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleBase.add(direction.scale(15)), direction)), ERROR_INTERSECTION_PARALLEL);

        // =============== Boundary Values Tests ==================

        // TC11: Ray head inside the cylinder (not on axis), before the bottom cap (2 points)
        assertIntersectionsEquals(List.of(insideBase, topCapInside),
                INTERSECTION_CYLINDER.findIntersections(new Ray(insideBase.add(direction.scale(-5)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC12: Ray head inside the cylinder (not on axis), on the bottom cap (1 point)
        assertIntersectionsEquals(List.of(topCapInside),
                INTERSECTION_CYLINDER.findIntersections(new Ray(insideBase, direction)), ERROR_INTERSECTION_PARALLEL);
        // TC13: Ray head inside the cylinder (not on axis), between the caps (1 point)
        assertIntersectionsEquals(List.of(topCapInside),
                INTERSECTION_CYLINDER.findIntersections(new Ray(insideBase.add(direction.scale(5)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC14: Ray head inside the cylinder (not on axis), on the top cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topCapInside, direction)), ERROR_INTERSECTION_PARALLEL);
        // TC15: Ray head inside the cylinder (not on axis), after the top cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(insideBase.add(direction.scale(15)), direction)), ERROR_INTERSECTION_PARALLEL);

        // TC16: Ray head on the axis, before the bottom cap (2 points)
        assertIntersectionsEquals(List.of(BOTTOM_CAP_CENTER, TOP_CAP_CENTER),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(direction.scale(-5)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC17: Ray head on the axis, on the bottom cap (1 point)
        assertIntersectionsEquals(List.of(TOP_CAP_CENTER),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER, direction)), ERROR_INTERSECTION_PARALLEL);
        // TC18: Ray head on the axis, between the caps (1 point)
        assertIntersectionsEquals(List.of(TOP_CAP_CENTER),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(direction.scale(5)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC19: Ray head on the axis, on the top cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER, direction)), ERROR_INTERSECTION_PARALLEL);
        // TC20: Ray head on the axis, after the top cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER.add(direction.scale(5)), direction)), ERROR_INTERSECTION_PARALLEL);
    }

    /**
     * Group 2: ray perpendicular to the cylinder axis, line does not intersect the cylinder.
     * Three distinct reasons for missing:
     * (TC01) the ray line crosses the infinite tube but both intersections lie before the bottom cap,
     * (TC02) the ray line crosses the infinite tube but both intersections lie after the top cap,
     * (TC03) the ray line misses the infinite tube altogether.
     * Expected: always 0 intersection points.
     */
    @Test
    void testFindIntersectionsGroup2PerpendicularMiss() {
        Vector direction = Vector.AXIS_X;

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray line crosses the infinite tube, both intersections before the bottom cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(new Point(3, -1, -1), direction)), ERROR_INTERSECTION_MISS);
        // TC02: Ray line crosses the infinite tube, both intersections after the top cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(new Point(3, 11, 15), direction)), ERROR_INTERSECTION_MISS);
        // TC03: Ray line misses the infinite tube entirely (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(new Point(6, 5, 7), direction)), ERROR_INTERSECTION_MISS);
    }
}

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
 * Unit tests for class {@link Tube}.
 * The tests verify:
 * <ul>
 * <li>{@link Tube#Tube(double, Ray)}</li>
 * <li>{@link Tube#getNormal(Point)}</li>
 * <li>{@link Tube#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
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
     * Error message for wrong tube intersection result.
     */
    private static final String ERROR_INTERSECTION = "Wrong tube intersection result";

    /**
     * Origin point of the tube axis used in the intersection tests.
     */
    private static final Point AXIS_HEAD = new Point(1, -2, 3);
    /**
     * Direction of the tube axis used in the intersection tests.
     */
    private static final Vector AXIS_DIRECTION = new Vector(0, 1, 1).normalize();
    /**
     * General tube axis used in the intersection tests.
     */
    private static final Ray INTERSECTION_AXIS = new Ray(AXIS_HEAD, AXIS_DIRECTION);

    /**
     * Tube radius used in the intersection tests.
     */
    private static final double INTERSECTION_RADIUS = 1d;
    /**
     * Tube used in the intersection tests.
     */
    private static final Tube INTERSECTION_TUBE = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);


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

    /**
     * Group 1: ray parallel to the tube axis.
     * A parallel ray never intersects the tube surface, regardless of whether
     * the ray head is outside, inside, on the mantle, or on the axis, and
     * regardless of its position relative to the reference plane.
     * Expected: always 0 intersection points.
     */
    @Test
    void testFindIntersectionsGroup1ParallelToAxis() {
        Vector direction = AXIS_DIRECTION;
        Point onPlaneO = new Point(4, -2, 3);
        Point onPlaneI = new Point(3d / 2d, -2, 3);
        Point onPlaneM = new Point(2, -2, 3);
        Point onPlaneX = AXIS_HEAD;

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray head outside the tube, before the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneO.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC02: Ray head outside the tube, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneO, direction)), ERROR_INTERSECTION);
        // TC03: Ray head outside the tube, after the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneO.add(direction), direction)), ERROR_INTERSECTION);

        // TC04: Ray head inside the tube, before the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneI.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC05: Ray head inside the tube, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneI, direction)), ERROR_INTERSECTION);
        // TC06: Ray head inside the tube, after the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneI.add(direction), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Ray head on the tube mantle, before the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneM.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC12: Ray head on the tube mantle, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneM, direction)), ERROR_INTERSECTION);
        // TC13: Ray head on the tube mantle, after the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneM.add(direction), direction)), ERROR_INTERSECTION);

        // TC14: Ray head on the tube axis, before the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneX.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC15: Ray head on the tube axis, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneX, direction)), ERROR_INTERSECTION);
        // TC16: Ray head on the tube axis, after the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneX.add(direction), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 2: ray perpendicular to the tube axis, line passes through the axis.
     * The ray's line crosses the axis center of the cross-section, so the two
     * potential intersections are symmetric about the axis. The number of returned
     * points (2, 1, or 0) depends on the ray head's position relative to those
     * intersections. Cases are tested both on and off the reference plane.
     */
    @Test
    void testFindIntersectionsGroup2PerpendicularThroughAxis() {
        Vector direction = Vector.AXIS_X;
        Point onPlaneCenter = AXIS_HEAD;
        Point offPlaneCenter = AXIS_HEAD.add(AXIS_DIRECTION.scale(2 * Math.sqrt(2)));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray head before first intersection, not on the reference plane (2 points)
        assertIntersectionsEquals(List.of(new Point(0, 0, 5), new Point(2, 0, 5)),
                INTERSECTION_TUBE.findIntersections(new Ray(offPlaneCenter.add(direction.scale(-2)), direction)), ERROR_INTERSECTION);
        // TC02: Ray head on first intersection, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                INTERSECTION_TUBE.findIntersections(new Ray(offPlaneCenter.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC03: Ray head inside tube before center, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                INTERSECTION_TUBE.findIntersections(new Ray(offPlaneCenter.add(direction.scale(-0.5)), direction)), ERROR_INTERSECTION);
        // TC04: Ray head at center, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                INTERSECTION_TUBE.findIntersections(new Ray(offPlaneCenter, direction)), ERROR_INTERSECTION);
        // TC05: Ray head inside tube after center, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                INTERSECTION_TUBE.findIntersections(new Ray(offPlaneCenter.add(direction.scale(0.5)), direction)), ERROR_INTERSECTION);
        // TC06: Ray head on second intersection, not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(offPlaneCenter.add(direction), direction)), ERROR_INTERSECTION);
        // TC07: Ray head after second intersection, not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(offPlaneCenter.add(direction.scale(2)), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Ray head before first intersection, on the reference plane (2 points)
        assertIntersectionsEquals(List.of(new Point(0, -2, 3), new Point(2, -2, 3)),
                INTERSECTION_TUBE.findIntersections(new Ray(onPlaneCenter.add(direction.scale(-2)), direction)), ERROR_INTERSECTION);
        // TC12: Ray head on first intersection, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, -2, 3)),
                INTERSECTION_TUBE.findIntersections(new Ray(onPlaneCenter.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC13: Ray head inside tube before center, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, -2, 3)),
                INTERSECTION_TUBE.findIntersections(new Ray(onPlaneCenter.add(direction.scale(-0.5)), direction)), ERROR_INTERSECTION);
        // TC14: Ray head at center, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, -2, 3)),
                INTERSECTION_TUBE.findIntersections(new Ray(onPlaneCenter, direction)), ERROR_INTERSECTION);
        // TC15: Ray head inside tube after center, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, -2, 3)),
                INTERSECTION_TUBE.findIntersections(new Ray(onPlaneCenter.add(direction.scale(0.5)), direction)), ERROR_INTERSECTION);
        // TC16: Ray head on second intersection, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneCenter.add(direction), direction)), ERROR_INTERSECTION);
        // TC17: Ray head after second intersection, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneCenter.add(direction.scale(2)), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 3: ray perpendicular to the tube axis, line does not pass through the axis but intersects twice.
     * The ray's line is a chord of the circular cross-section, producing two
     * intersections that are not symmetric about the axis. The number of returned
     * points (2, 1, or 0) depends on the ray head's position relative to those
     * intersections. Cases are tested both on and off the reference plane.
     */
    @Test
    void testFindIntersectionsGroup3PerpendicularOffAxis() {
        Vector direction = Vector.AXIS_X;
        Point pointInsideCircle = new Point(1, -1.5, 2.5);
        Point pointInsideCircleAfter = pointInsideCircle.add(new Vector(0, 2, 2));
        Point first1 = new Point((2 - Math.sqrt(2d)) / 2, -1.5, 2.5);
        Point second1 = new Point((2 + Math.sqrt(2d)) / 2, -1.5, 2.5);
        Point first2 = new Point((2 - Math.sqrt(2d)) / 2, 0.5, 4.5);
        Point second2 = new Point((2 + Math.sqrt(2d)) / 2, 0.5, 4.5);


        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray head before first intersection, not on the reference plane (2 points)
        assertIntersectionsEquals(List.of(first2, second2),
                INTERSECTION_TUBE.findIntersections(new Ray(pointInsideCircleAfter.add(direction.scale(-2)), direction)), ERROR_INTERSECTION);
        // TC02: Ray head on first intersection, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(second2),
                INTERSECTION_TUBE.findIntersections(new Ray(first2, direction)), ERROR_INTERSECTION);
        // TC03: Ray head between intersections, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(second2),
                INTERSECTION_TUBE.findIntersections(new Ray(pointInsideCircleAfter, direction)), ERROR_INTERSECTION);
        // TC04: Ray head on second intersection, not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(second2, direction)), ERROR_INTERSECTION);
        // TC05: Ray head after second intersection, not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(pointInsideCircleAfter.add(direction.scale(2)), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Ray head before first intersection, on the reference plane (2 points)
        assertIntersectionsEquals(List.of(first1, second1),
                INTERSECTION_TUBE.findIntersections(new Ray(pointInsideCircle.add(direction.scale(-2)), direction)), ERROR_INTERSECTION);
        // TC12: Ray head on first intersection, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(second1),
                INTERSECTION_TUBE.findIntersections(new Ray(first1, direction)), ERROR_INTERSECTION);
        // TC13: Ray head between intersections, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(second1),
                INTERSECTION_TUBE.findIntersections(new Ray(pointInsideCircle, direction)), ERROR_INTERSECTION);
        // TC14: Ray head on second intersection, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(second1, direction)), ERROR_INTERSECTION);
        // TC15: Ray head after second intersection, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray((pointInsideCircle.add(direction.scale(2))), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 4: ray perpendicular to the tube axis, line is tangent to the tube.
     * The ray's line touches the tube at exactly one point but does not cross it,
     * so no intersection is counted regardless of the ray head's position relative
     * to the tangent point. Cases are tested with the tangent on and off the reference plane.
     * Expected: always 0 intersection points.
     */
    @Test
    void testFindIntersectionsGroup4PerpendicularTangent() {
        Vector direction = Vector.AXIS_X;
        Point onPlaneTangent = new Point(1, -2 + 1 / Math.sqrt(2), 3 - 1 / Math.sqrt(2));
        Point offPlaneTangent = onPlaneTangent.add(AXIS_DIRECTION.scale(-2));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Tangent on reference plane, ray head before tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneTangent.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC02: Tangent on reference plane, ray head on tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneTangent, direction)), ERROR_INTERSECTION);
        // TC03: Tangent on reference plane, ray head after tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneTangent.add(direction), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Tangent not on reference plane, ray head before tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(offPlaneTangent.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC12: Tangent not on reference plane, ray head on tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(offPlaneTangent, direction)), ERROR_INTERSECTION);
        // TC13: Tangent not on reference plane, ray head after tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(offPlaneTangent.add(direction), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 5: ray perpendicular to the tube axis, line misses the tube entirely.
     * The ray's line does not intersect the tube's circular cross-section at all.
     * Cases are tested with the ray head on and off the reference plane.
     * Expected: always 0 intersection points.
     */
    @Test
    void testFindIntersectionsGroup5PerpendicularMiss() {
        Vector direction = Vector.AXIS_X;

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray misses the tube, ray head on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(new Point(-8, -3, 4), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Ray misses the tube, ray head not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(new Point(1, 5, 7), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 6: general ray (not parallel, not perpendicular), line passes through the axis
     * but not through the axis head.
     * Three sub-patterns based on the intersections' positions relative to the reference plane:
     * (A) both intersections before the plane, (B) second intersection on the plane,
     * (C) intersections straddle the plane (BVA). Within each pattern the ray head
     * is placed at all significant positions along the ray.
     */
    @Test
    void testFindIntersectionsGroup6GeneralThroughAxis() {
        Vector direction = new Vector(1, 0.5, 0.5);
        Point centerA = new Point(1, 8, 13);
        Point centerB = new Point(1, -2.5, 2.5);
        Point centerC = new Point(1, -7d / 4, 13d / 4);
        Point firstA = new Point(0, 7.5, 12.5);
        Point secondA = new Point(2, 8.5, 13.5);
        Point firstB = new Point(0, -3, 2);
        Point secondB = new Point(2, -2, 3);
        Point firstC = new Point(0, -9d / 4, 11d / 4);
        Point secondC = new Point(2, -5d / 4, 15d / 4);

        // ============ Equivalence Partitions Tests ==============

        // EP: both intersections are before the reference plane
        // TC01: Ray head before first intersection, both intersections before reference plane (2 points)
        assertIntersectionsEquals(List.of(firstA, secondA),
                INTERSECTION_TUBE.findIntersections(new Ray(centerA.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // TC02: Ray head on first intersection, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondA),
                INTERSECTION_TUBE.findIntersections(new Ray(firstA, direction)), ERROR_INTERSECTION);
        // TC03: Ray head between intersections before center, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondA),
                INTERSECTION_TUBE.findIntersections(new Ray(centerA.add(direction.scale(-1d / 2d)), direction)), ERROR_INTERSECTION);
        // TC04: Ray head at center, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondA),
                INTERSECTION_TUBE.findIntersections(new Ray(centerA, direction)), ERROR_INTERSECTION);
        // TC05: Ray head between intersections after center, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondA),
                INTERSECTION_TUBE.findIntersections(new Ray(centerA.add(direction.scale(1d / 2d)), direction)), ERROR_INTERSECTION);
        // TC06: Ray head on second intersection, both intersections before reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondA, direction)), ERROR_INTERSECTION);
        // TC07: Ray head after second intersection, both intersections before reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(centerA.add(direction.scale(3)), direction)), ERROR_INTERSECTION);

        // EP: second intersection is on the reference plane
        // TC08: Ray head before first intersection, second intersection on reference plane (2 points)
        assertIntersectionsEquals(List.of(firstB, secondB),
                INTERSECTION_TUBE.findIntersections(new Ray(centerB.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // TC09: Ray head on first intersection, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondB),
                INTERSECTION_TUBE.findIntersections(new Ray(firstB, direction)), ERROR_INTERSECTION);
        // TC10: Ray head between intersections before center, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondB),
                INTERSECTION_TUBE.findIntersections(new Ray(centerB.add(direction.scale(-1d / 2d)), direction)), ERROR_INTERSECTION);
        // TC11: Ray head at center, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondB),
                INTERSECTION_TUBE.findIntersections(new Ray(centerB, direction)), ERROR_INTERSECTION);
        // TC12: Ray head between intersections after center, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondB),
                INTERSECTION_TUBE.findIntersections(new Ray(centerB.add(direction.scale(1d / 2d)), direction)), ERROR_INTERSECTION);
        // TC13: Ray head on second intersection, second intersection on reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondB, direction)), ERROR_INTERSECTION);
        // TC14: Ray head after second intersection, second intersection on reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(centerB.add(direction.scale(3)), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // BVA: reference plane lies between the two intersections
        // TC15: Ray head before first intersection, reference plane between intersections (2 points)
        assertIntersectionsEquals(List.of(firstC, secondC),
                INTERSECTION_TUBE.findIntersections(new Ray(centerC.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // TC16: Ray head on first intersection, reference plane between intersections (1 point)
        assertIntersectionsEquals(List.of(secondC),
                INTERSECTION_TUBE.findIntersections(new Ray(firstC, direction)), ERROR_INTERSECTION);
        // TC17: Ray head between intersections before center, reference plane between intersections (1 point)
        assertIntersectionsEquals(List.of(secondC),
                INTERSECTION_TUBE.findIntersections(new Ray(centerC.add(direction.scale(-0.5)), direction)), ERROR_INTERSECTION);
        // TC18: Ray head at center on the reference plane, reference plane between intersections (1 point)
        assertIntersectionsEquals(List.of(secondC),
                INTERSECTION_TUBE.findIntersections(new Ray(centerC, direction)), ERROR_INTERSECTION);
        // TC19: Ray head between intersections after center, reference plane between intersections (1 point)
        assertIntersectionsEquals(List.of(secondC),
                INTERSECTION_TUBE.findIntersections(new Ray(centerC.add(direction.scale(0.5)), direction)), ERROR_INTERSECTION);
        // TC20: Ray head on second intersection, reference plane between intersections (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondC, direction)), ERROR_INTERSECTION);
        // TC21: Ray head after second intersection, reference plane between intersections (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(centerC.add(direction.scale(3)), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 7: general ray (not parallel, not perpendicular), line does not pass through
     * the axis but produces two intersections.
     * Three sub-patterns based on the intersections' positions relative to the reference plane:
     * (A) both intersections before the plane, (B) second intersection on the plane,
     * (C) intersections straddle the plane (BVA). Within each pattern the ray head
     * is placed at all significant positions along the ray.
     */
    @Test
    void testFindIntersectionsGroup7GeneralOffAxis() {
        Vector direction = new Vector(1, 1.5, 0.5).normalize();
        Point centerA = new Point(1, -0.5, 5.5);
        Point centerB = new Point(4d / 3, -3, 8d / 3);
        Point centerC = new Point(4d / 3, -2, 11d / 3);
        Point firstA = new Point(2d / 3, -1, 16d / 3);
        Point secondA = new Point(2, 1, 6);
        Point firstB = new Point(2d / 3, -4, 7d / 3);
        Point secondB = new Point(2, -2, 3);
        Point firstC = new Point(2d / 3, -3, 10d / 3);
        Point secondC = new Point(2, -1, 4);

        // ============ Equivalence Partitions Tests ==============

        // EP: both intersections are before the reference plane
        // TC01: Ray head before first intersection, both intersections before reference plane (2 points)
        assertIntersectionsEquals(List.of(firstA, secondA),
                INTERSECTION_TUBE.findIntersections(new Ray(centerA.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // TC02: Ray head on first intersection, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondA),
                INTERSECTION_TUBE.findIntersections(new Ray(firstA, direction)), ERROR_INTERSECTION);
        // TC03: Ray head between intersections, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondA),
                INTERSECTION_TUBE.findIntersections(new Ray(centerA, direction)), ERROR_INTERSECTION);
        // TC04: Ray head on second intersection, both intersections before reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondA, direction)), ERROR_INTERSECTION);
        // TC05: Ray head after second intersection, both intersections before reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(centerA.add(direction.scale(3)), direction)), ERROR_INTERSECTION);

        // EP: second intersection is on the reference plane
        // TC06: Ray head before first intersection, second intersection on reference plane (2 points)
        assertIntersectionsEquals(List.of(firstB, secondB),
                INTERSECTION_TUBE.findIntersections(new Ray(centerB.add(direction.scale(-9)), direction)), ERROR_INTERSECTION);
        // TC07: Ray head on first intersection, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondB),
                INTERSECTION_TUBE.findIntersections(new Ray(firstB, direction)), ERROR_INTERSECTION);
        // TC08: Ray head between intersections, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondB),
                INTERSECTION_TUBE.findIntersections(new Ray(centerB, direction)), ERROR_INTERSECTION);
        // TC09: Ray head on second intersection, second intersection on reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondB, direction)), ERROR_INTERSECTION);
        // TC10: Ray head after second intersection, second intersection on reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(centerB.add(direction.scale(9)), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // BVA: reference plane lies between the two intersections
        // TC11: Ray head before first intersection, reference plane between intersections (2 points)
        assertIntersectionsEquals(List.of(firstC, secondC),
                INTERSECTION_TUBE.findIntersections(new Ray(centerC.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // TC12: Ray head on first intersection, reference plane between intersections (1 point)
        assertIntersectionsEquals(List.of(secondC),
                INTERSECTION_TUBE.findIntersections(new Ray(firstC, direction)), ERROR_INTERSECTION);
        // TC13: Ray head between intersections, before the reference plane (1 point)
        assertIntersectionsEquals(List.of(secondC),
                INTERSECTION_TUBE.findIntersections(new Ray(centerC.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC14: Ray head between intersections, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(secondC),
                INTERSECTION_TUBE.findIntersections(new Ray(centerC, direction)), ERROR_INTERSECTION);
        // TC15: Ray head between intersections, after the reference plane (1 point)
        assertIntersectionsEquals(List.of(secondC),
                INTERSECTION_TUBE.findIntersections(new Ray(centerC.add(direction), direction)), ERROR_INTERSECTION);
        // TC16: Ray head on second intersection, reference plane between intersections (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondC, direction)), ERROR_INTERSECTION);
        // TC17: Ray head after second intersection, reference plane between intersections (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(centerC.add(direction.scale(3)), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 8: general ray (not parallel, not perpendicular), line passes through
     * the axis head (origin of the axis ray).
     * This is a boundary case of Group 6 where the reference plane is crossed at the
     * axis head itself. The ray head is placed at all significant positions along the ray.
     */
    @Test
    void testFindIntersectionsGroup8GeneralThroughAxisHead() {
        Vector direction = new Vector(1, 0.5, 0.5);
        Point center = AXIS_HEAD;
        Point first = new Point(0, -2.5, 2.5);
        Point second = new Point(2, -1.5, 3.5);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray head before first intersection, line through axis head (2 points)
        assertIntersectionsEquals(List.of(first, second),
                INTERSECTION_TUBE.findIntersections(new Ray(center.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // TC02: Ray head on first intersection, line through axis head (1 point)
        assertIntersectionsEquals(List.of(second),
                INTERSECTION_TUBE.findIntersections(new Ray(first, direction)), ERROR_INTERSECTION);
        // TC03: Ray head inside tube before center, line through axis head (1 point)
        assertIntersectionsEquals(List.of(second),
                INTERSECTION_TUBE.findIntersections(new Ray(center.add(direction.scale(-1d / 2d)), direction)), ERROR_INTERSECTION);
        // TC04: Ray head at the axis head (center), line through axis head (1 point)
        assertIntersectionsEquals(List.of(second),
                INTERSECTION_TUBE.findIntersections(new Ray(center, direction)), ERROR_INTERSECTION);
        // TC05: Ray head inside tube after center, line through axis head (1 point)
        assertIntersectionsEquals(List.of(second),
                INTERSECTION_TUBE.findIntersections(new Ray(center.add(direction.scale(1d / 2d)), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Ray head on second intersection, line through axis head (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(second, direction)), ERROR_INTERSECTION);
        // TC12: Ray head after second intersection, line through axis head (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(center.add(direction.scale(3)), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 9: general ray (not parallel, not perpendicular), line is tangent to the tube.
     * The ray's line touches the tube surface at exactly one point but does not enter it,
     * so no intersection is counted. Cases are tested with the tangent on and off
     * the reference plane.
     * Expected: always 0 intersection points.
     */
    @Test
    void testFindIntersectionsGroup9GeneralTangent() {
        Vector direction = Vector.AXIS_Z;
        Point onPlaneTangent = new Point(0, -2, 3);
        Point offPlaneTangent = onPlaneTangent.add(AXIS_DIRECTION.scale(-2));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Tangent on reference plane, ray head before tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneTangent.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC02: Tangent on reference plane, ray head on tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneTangent, direction)), ERROR_INTERSECTION);
        // TC03: Tangent on reference plane, ray head after tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneTangent.add(direction), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Tangent not on reference plane, ray head before tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(offPlaneTangent.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // TC12: Tangent not on reference plane, ray head on tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(offPlaneTangent, direction)), ERROR_INTERSECTION);
        // TC13: Tangent not on reference plane, ray head after tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(offPlaneTangent.add(direction), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 10: general ray (not parallel, not perpendicular), line misses the tube entirely.
     * The ray's line does not intersect the tube at all. Cases are tested with the
     * ray head on and off the reference plane.
     * Expected: always 0 intersection points.
     */
    @Test
    void testFindIntersectionsGroup10GeneralMiss() {
        Vector direction = Vector.AXIS_Z;
        Point onPlaneMiss = new Point(4, -4, 5);
        Point offPlaneMiss = onPlaneMiss.add(direction.scale(2));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray misses the tube, ray head on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(onPlaneMiss, direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Ray misses the tube, ray head not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(offPlaneMiss, direction)), ERROR_INTERSECTION);
    }
}

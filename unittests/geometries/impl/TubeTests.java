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
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *  * @author Ambash and Elyasaf
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
     * General tube axis used in the intersection tests.
     */
    private static final Ray INTERSECTION_AXIS =
            new Ray(new Point(1, -2, 3), new Vector(0, 1, 1));

    /**
     * Tube radius used in the intersection tests.
     */
    private static final double INTERSECTION_RADIUS = 1d;
    /**
     * 1/sqrt(2) used by the general tube axis and several general rays.
     */
    private static final double INV_SQRT2 = 1d / Math.sqrt(2d);
    /**
     * sqrt(3)/2 used in offset intersection cases.
     */
    private static final double SQRT3_OVER_2 = Math.sqrt(3d) / 2d;

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
     */
    @Test
    void testFindIntersectionsGroup1ParallelToAxis() {
        Tube tube = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);
        Vector direction = new Vector(0, 1, 1);
        Point onPlaneO = new Point(4, -2, 3);
        Point onPlaneI = new Point(3d / 2d, -2, 3);
        Point onPlaneM = new Point(2, -2, 3);
        Point onPlaneX = new Point(1, -2, 3);

        // ============ Equivalence Partitions Tests ==============

        // Case: ray head is outside the tube, before the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneO.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // Case: ray head is outside the tube, on the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneO, direction)), ERROR_INTERSECTION);
        // Case: ray head is outside the tube, after the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneO.add(direction), direction)), ERROR_INTERSECTION);

        // Case: ray head is inside the tube, before the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneI.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // Case: ray head is inside the tube, on the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneI, direction)), ERROR_INTERSECTION);
        // Case: ray head is inside the tube, after the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneI.add(direction), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // Case: ray head is on the tube mantle, before the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneM.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the tube mantle, on the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneM, direction)), ERROR_INTERSECTION);
        // Case: ray head is on the tube mantle, after the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneM.add(direction), direction)), ERROR_INTERSECTION);

        // Case: ray head is on the tube axis, before the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneX.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the tube axis, on the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneX, direction)), ERROR_INTERSECTION);
        // Case: ray head is on the tube axis, after the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneX.add(direction), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 2: ray perpendicular to the tube axis and its line passes through the axis.
     */
    @Test
    void testFindIntersectionsGroup2PerpendicularThroughAxis() {
        Tube tube = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);
        Vector direction = Vector.AXIS_X;
        Point onPlaneCenter = new Point(1, -2, 3);
        Point offPlaneCenter = onPlaneCenter.add(new Vector(0, 2, 2));
        Vector halfX = Vector.AXIS_X.scale(1d / 2d);
        Vector oneX = Vector.AXIS_X;
        Vector twoX = Vector.AXIS_X.scale(2);

        // ============ Equivalence Partitions Tests ==============

        // Case: ray head is before the first intersection, on the reference plane
        assertIntersectionsEquals(List.of(new Point(0, -2, 3), new Point(2, -2, 3)),
                tube.findIntersections(new Ray(onPlaneCenter.add(direction.scale(-2)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the first intersection, on the reference plane
        assertIntersectionsEquals(List.of(new Point(2, -2, 3)),
                tube.findIntersections(new Ray(onPlaneCenter.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // Case: ray head is inside the tube before the center, on the reference plane
        assertIntersectionsEquals(List.of(new Point(2, -2, 3)),
                tube.findIntersections(new Ray(onPlaneCenter.add(direction.scale(-0.5)), direction)), ERROR_INTERSECTION);
        // Case: ray head is at the center, on the reference plane
        assertIntersectionsEquals(List.of(new Point(2, -2, 3)),
                tube.findIntersections(new Ray(onPlaneCenter, direction)), ERROR_INTERSECTION);
        // Case: ray head is inside the tube after the center, on the reference plane
        assertIntersectionsEquals(List.of(new Point(2, -2, 3)),
                tube.findIntersections(new Ray(onPlaneCenter.add(direction.scale(0.5)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the second intersection, on the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneCenter.add(direction), direction)), ERROR_INTERSECTION);
        // Case: ray head is after the second intersection, on the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneCenter.add(direction.scale(2)), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // Case: ray head is before the first intersection, not on the reference plane
        assertIntersectionsEquals(List.of(new Point(0, 0, 5), new Point(2, 0, 5)),
                tube.findIntersections(new Ray(offPlaneCenter.add(direction.scale(-2)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the first intersection, not on the reference plane
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                tube.findIntersections(new Ray(offPlaneCenter.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // Case: ray head is inside the tube before the center, not on the reference plane
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                tube.findIntersections(new Ray(offPlaneCenter.add(direction.scale(-0.5)), direction)), ERROR_INTERSECTION);
        // Case: ray head is at the center, not on the reference plane
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                tube.findIntersections(new Ray(offPlaneCenter, direction)), ERROR_INTERSECTION);
        // Case: ray head is inside the tube after the center, not on the reference plane
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                tube.findIntersections(new Ray(offPlaneCenter.add(direction.scale(0.5)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the second intersection, not on the reference plane
        assertNull(tube.findIntersections(new Ray(offPlaneCenter.add(direction), direction)), ERROR_INTERSECTION);
        // Case: ray head is after the second intersection, not on the reference plane
        assertNull(tube.findIntersections(new Ray(offPlaneCenter.add(direction.scale(2)), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 3: ray perpendicular to the tube axis and its line does not pass through the axis.
     */
    @Test
    void testFindIntersectionsGroup3PerpendicularOffAxis() {
        Tube tube = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);
        Vector direction = Vector.AXIS_X;
        Vector axisDirection = INTERSECTION_AXIS.direction();
        Point pointInsideCircle = new Point(1, -1.5, 2.5);
        Point pointInsideCircleAfter = pointInsideCircle.add(new Vector(0, 2, 2));
        Point first1 = new Point((2 - Math.sqrt(2d)) / 2, -1.5, 2.5);
        Point second1 = new Point((2 + Math.sqrt(2d)) / 2, -1.5, 2.5);
        Point first2 = new Point((2 - Math.sqrt(2d)) / 2, 0.5, 4.5);
        Point second2 = new Point((2 + Math.sqrt(2d)) / 2, 0.5, 4.5);


        // ============ Equivalence Partitions Tests ==============

        // Case: ray head is before the first intersection, on the reference plane
        assertIntersectionsEquals(List.of(first1, second1),
                tube.findIntersections(new Ray(pointInsideCircle.add(direction.scale(-2)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the first intersection, on the reference plane
        assertIntersectionsEquals(List.of(second1),
                tube.findIntersections(new Ray(first1, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, on the reference plane
        assertIntersectionsEquals(List.of(second1),
                tube.findIntersections(new Ray(pointInsideCircle, direction)), ERROR_INTERSECTION);
        // Case: ray head is on the second intersection, on the reference plane
        assertNull(tube.findIntersections(new Ray(second1, direction)), ERROR_INTERSECTION);
        // Case: ray head is after the second intersection, on the reference plane
        assertNull(tube.findIntersections(new Ray((pointInsideCircle.add(direction.scale(2))), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // Case: ray head is before the first intersection, not on the reference plane
        assertIntersectionsEquals(List.of(first2, second2),
                tube.findIntersections(new Ray(pointInsideCircleAfter.add(direction.scale(-2)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the first intersection, not on the reference plane
        assertIntersectionsEquals(List.of(second2),
                tube.findIntersections(new Ray(first2, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, not on the reference plane
        assertIntersectionsEquals(List.of(second2),
                tube.findIntersections(new Ray(pointInsideCircleAfter, direction)), ERROR_INTERSECTION);
        // Case: ray head is on the second intersection, not on the reference plane
        assertNull(tube.findIntersections(new Ray(second2, direction)), ERROR_INTERSECTION);
        // Case: ray head is after the second intersection, not on the reference plane
        assertNull(tube.findIntersections(new Ray(pointInsideCircleAfter.add(direction.scale(2)), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 4: ray perpendicular to the tube axis and its line is tangent to the tube.
     */
    @Test
    void testFindIntersectionsGroup4PerpendicularTangent() {
        Tube tube = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);
        Vector direction = Vector.AXIS_X;
        Vector axisDirection = INTERSECTION_AXIS.direction();
        Point onPlaneTangent = new Point(1, -2 + 1 / Math.sqrt(2), 3 - 1 / Math.sqrt(2));
        Point offPlaneTangent = onPlaneTangent.add(axisDirection.scale(-2));

        // ============ Equivalence Partitions Tests ==============

        // EP: tangent point on the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneTangent.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        assertNull(tube.findIntersections(new Ray(onPlaneTangent, direction)), ERROR_INTERSECTION);
        assertNull(tube.findIntersections(new Ray(onPlaneTangent.add(direction), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // BVA: tangent point away from the reference plane
        assertNull(tube.findIntersections(new Ray(offPlaneTangent.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        assertNull(tube.findIntersections(new Ray(offPlaneTangent, direction)), ERROR_INTERSECTION);
        assertNull(tube.findIntersections(new Ray(offPlaneTangent.add(direction), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 5: ray perpendicular to the tube axis and its line does not hit the tube.
     */
    @Test
    void testFindIntersectionsGroup5PerpendicularMiss() {
        Tube tube = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);
        Vector direction = Vector.AXIS_X;
        Vector axisDirection = INTERSECTION_AXIS.direction();
        Vector missOffset = new Vector(0, 2d * INV_SQRT2, -2d * INV_SQRT2);
        Point onPlaneMiss = new Point(1, -2, 3).add(missOffset);
        Point offPlaneMiss = onPlaneMiss.add(axisDirection.scale(2));

        // ============ Equivalence Partitions Tests ==============

        // EP: miss on the reference plane
        assertNull(tube.findIntersections(new Ray(new Point(-8, -3, 4), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // BVA: miss away from the reference plane
        assertNull(tube.findIntersections(new Ray(new Point(1, 5, 7), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 6: general ray, not parallel and not perpendicular to the axis,
     * whose line passes through the tube axis but not through the axis head.
     */
    @Test
    void testFindIntersectionsGroup6GeneralThroughAxis() {
        Tube tube = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);
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
        // Case: ray head is before the first intersection
        assertIntersectionsEquals(List.of(firstA, secondA),
                tube.findIntersections(new Ray(centerA.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the first intersection
        assertIntersectionsEquals(List.of(secondA),
                tube.findIntersections(new Ray(firstA, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, before the center
        assertIntersectionsEquals(List.of(secondA),
                tube.findIntersections(new Ray(centerA.add(direction.scale(-1d / 2d)), direction)), ERROR_INTERSECTION);
        // Case: ray head is at the center
        assertIntersectionsEquals(List.of(secondA),
                tube.findIntersections(new Ray(centerA, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, after the center
        assertIntersectionsEquals(List.of(secondA),
                tube.findIntersections(new Ray(centerA.add(direction.scale(1d / 2d)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the second intersection
        assertNull(tube.findIntersections(new Ray(secondA, direction)), ERROR_INTERSECTION);
        // Case: ray head is after the second intersection
        assertNull(tube.findIntersections(new Ray(centerA.add(direction.scale(3)), direction)), ERROR_INTERSECTION);

        // EP: the second intersection is on the reference plane
        // Case: ray head is before the first intersection
        assertIntersectionsEquals(List.of(firstB, secondB),
                tube.findIntersections(new Ray(centerB.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the first intersection
        assertIntersectionsEquals(List.of(secondB),
                tube.findIntersections(new Ray(firstB, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, before the center
        assertIntersectionsEquals(List.of(secondB),
                tube.findIntersections(new Ray(centerB.add(direction.scale(-1d / 2d)), direction)), ERROR_INTERSECTION);
        // Case: ray head is at the center
        assertIntersectionsEquals(List.of(secondB),
                tube.findIntersections(new Ray(centerB, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, after the center
        assertIntersectionsEquals(List.of(secondB),
                tube.findIntersections(new Ray(centerB.add(direction.scale(1d / 2d)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the second intersection
        assertNull(tube.findIntersections(new Ray(secondB, direction)), ERROR_INTERSECTION);
        // Case: ray head is after the second intersection
        assertNull(tube.findIntersections(new Ray(centerB.add(direction.scale(3)), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // BVA: the reference plane lies between the two intersections
        // Case: ray head is before the first intersection
        assertIntersectionsEquals(List.of(firstC, secondC),
                tube.findIntersections(new Ray(centerC.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the first intersection
        assertIntersectionsEquals(List.of(secondC),
                tube.findIntersections(new Ray(firstC, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, before the center
        assertIntersectionsEquals(List.of(secondC),
                tube.findIntersections(new Ray(centerC.add(direction.scale(-0.5)), direction)), ERROR_INTERSECTION);
        // Case: ray head is at the center on the reference plane
        assertIntersectionsEquals(List.of(secondC),
                tube.findIntersections(new Ray(centerC, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, after the center
        assertIntersectionsEquals(List.of(secondC),
                tube.findIntersections(new Ray(centerC.add(direction.scale(0.5)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the second intersection
        assertNull(tube.findIntersections(new Ray(secondC, direction)), ERROR_INTERSECTION);
        // Case: ray head is after the second intersection
        assertNull(tube.findIntersections(new Ray(centerC.add(direction.scale(3)), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 7: general ray, not parallel and not perpendicular to the axis,
     * whose line does not pass through the tube axis but intersects twice.
     */
    @Test
    void testFindIntersectionsGroup7GeneralOffAxis() {
        Tube tube = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);
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
        // Case: ray head is before the first intersection
        assertIntersectionsEquals(List.of(firstA, secondA),
                tube.findIntersections(new Ray(centerA.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the first intersection
        assertIntersectionsEquals(List.of(secondA),
                tube.findIntersections(new Ray(firstA, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections
        assertIntersectionsEquals(List.of(secondA),
                tube.findIntersections(new Ray(centerA, direction)), ERROR_INTERSECTION);
        // Case: ray head is on the second intersection
        assertNull(tube.findIntersections(new Ray(secondA, direction)), ERROR_INTERSECTION);
        // Case: ray head is after the second intersection
        assertNull(tube.findIntersections(new Ray(centerA.add(direction.scale(3)), direction)), ERROR_INTERSECTION);

        // EP: the second intersection is on the reference plane
        // Case: ray head is before the first intersection
        assertIntersectionsEquals(List.of(firstB, secondB),
                tube.findIntersections(new Ray(centerB.add(direction.scale(-9)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the first intersection
        assertIntersectionsEquals(List.of(secondB),
                tube.findIntersections(new Ray(firstB, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections
        assertIntersectionsEquals(List.of(secondB),
                tube.findIntersections(new Ray(centerB, direction)), ERROR_INTERSECTION);
        // Case: ray head is on the second intersection
        assertNull(tube.findIntersections(new Ray(secondB, direction)), ERROR_INTERSECTION);
        // Case: ray head is after the second intersection
        assertNull(tube.findIntersections(new Ray(centerB.add(direction.scale(9)), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // BVA: the reference plane lies between the two intersections
        // Case: ray head is before the first intersection
        assertIntersectionsEquals(List.of(firstC, secondC),
                tube.findIntersections(new Ray(centerC.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the first intersection
        assertIntersectionsEquals(List.of(secondC),
                tube.findIntersections(new Ray(firstC, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, before the reference plane
        assertIntersectionsEquals(List.of(secondC),
                tube.findIntersections(new Ray(centerC.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, on the reference plane
        assertIntersectionsEquals(List.of(secondC),
                tube.findIntersections(new Ray(centerC, direction)), ERROR_INTERSECTION);
        // Case: ray head is between the intersections, after the reference plane
        assertIntersectionsEquals(List.of(secondC),
                tube.findIntersections(new Ray(centerC.add(direction), direction)), ERROR_INTERSECTION);
        // Case: ray head is on the second intersection
        assertNull(tube.findIntersections(new Ray(secondC, direction)), ERROR_INTERSECTION);
        // Case: ray head is after the second intersection
        assertNull(tube.findIntersections(new Ray(centerC.add(direction.scale(3)), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 8: general ray whose line passes through the axis head itself.
     */
    @Test
    void testFindIntersectionsGroup8GeneralThroughAxisHead() {
        Tube tube = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);
        Vector direction = new Vector(1, 0.5, 0.5);
        Point center = new Point(1, -2, 3);
        Point first = new Point(0, -2.5, 2.5);
        Point second = new Point(2, -1.5, 3.5);

        // ============ Equivalence Partitions Tests ==============

        // EP: line passes through the axis head
        // Case: starts before the first intersection
        assertIntersectionsEquals(List.of(first, second),
                tube.findIntersections(new Ray(center.add(direction.scale(-3)), direction)), ERROR_INTERSECTION);
        // Case: starts on the first intersection
        assertIntersectionsEquals(List.of(second),
                tube.findIntersections(new Ray(first, direction)), ERROR_INTERSECTION);
        // Case: starts inside before the center
        assertIntersectionsEquals(List.of(second),
                tube.findIntersections(new Ray(center.add(direction.scale(-1d / 2d)), direction)), ERROR_INTERSECTION);
        // Case: starts at the center
        assertIntersectionsEquals(List.of(second),
                tube.findIntersections(new Ray(center, direction)), ERROR_INTERSECTION);
        // Case: starts inside after the center
        assertIntersectionsEquals(List.of(second),
                tube.findIntersections(new Ray(center.add(direction.scale(1d / 2d)), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // BVA: start position on/after the second intersection
        assertNull(tube.findIntersections(new Ray(second, direction)), ERROR_INTERSECTION);
        assertNull(tube.findIntersections(new Ray(center.add(direction.scale(3)), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 9: general tangent line.
     */
    @Test
    void testFindIntersectionsGroup9GeneralTangent() {
        Tube tube = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);
        Vector direction = Vector.AXIS_Z;
        Vector axisDirection = INTERSECTION_AXIS.direction();
        Point onPlaneTangent = new Point(0, -2, 3);
        Point offPlaneTangent = onPlaneTangent.add(axisDirection.scale(-2));

        // ============ Equivalence Partitions Tests ==============

        // EP: tangent point on the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneTangent.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        assertNull(tube.findIntersections(new Ray(onPlaneTangent, direction)), ERROR_INTERSECTION);
        assertNull(tube.findIntersections(new Ray(onPlaneTangent.add(direction), direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // BVA: tangent point away from the reference plane
        assertNull(tube.findIntersections(new Ray(offPlaneTangent.add(direction.scale(-1)), direction)), ERROR_INTERSECTION);
        assertNull(tube.findIntersections(new Ray(offPlaneTangent, direction)), ERROR_INTERSECTION);
        assertNull(tube.findIntersections(new Ray(offPlaneTangent.add(direction), direction)), ERROR_INTERSECTION);
    }

    /**
     * Group 10: general line that misses the tube.
     */
    @Test
    void testFindIntersectionsGroup10GeneralMiss() {
        Tube tube = new Tube(INTERSECTION_RADIUS, INTERSECTION_AXIS);
        Vector direction = Vector.AXIS_Z;
        Vector axisDirection = INTERSECTION_AXIS.direction();
        Point onPlaneMiss = new Point(4, -4, 5);
        Point offPlaneMiss = onPlaneMiss.add(direction.scale(2));

        // ============ Equivalence Partitions Tests ==============

        // EP: miss on the reference plane
        assertNull(tube.findIntersections(new Ray(onPlaneMiss, direction)), ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // BVA: miss away from the reference plane
        assertNull(tube.findIntersections(new Ray(offPlaneMiss, direction)), ERROR_INTERSECTION);
    }
}

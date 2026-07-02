package geometries.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Tube}.
 * The tests verify:
 * <ul>
 * <li>{@link Tube#Tube(double, Ray)}</li>
 * <li>{@link Tube#getNormal(Point)}</li>
 * <li>{@link Tube#findIntersections(Ray)}</li>
 * <li>{@link Tube#equals(Object)}</li>
 * <li>{@link geometries.api.Geometry#setEmission(Color)}</li>
 * <li>{@link geometries.api.Geometry#setMaterial(Material)}</li>
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
     * Error message for wrong intersection result for a ray parallel to the axis.
     */
    private static final String ERROR_INTERSECTION_PARALLEL =
            "Tube.findIntersections() returned wrong result for a ray parallel to the axis";
    /**
     * Error message for wrong intersection result for a perpendicular ray through the axis.
     */
    private static final String ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS =
            "Tube.findIntersections() returned wrong result for a perpendicular ray through the axis";
    /**
     * Error message for wrong intersection result for a perpendicular ray off the axis.
     */
    private static final String ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS =
            "Tube.findIntersections() returned wrong result for a perpendicular ray off the axis";
    /**
     * Error message for wrong intersection result for a tangent ray.
     */
    private static final String ERROR_INTERSECTION_TANGENT =
            "Tube.findIntersections() returned wrong result for a tangent ray";
    /**
     * Error message for wrong intersection result for a ray that misses the tube.
     */
    private static final String ERROR_INTERSECTION_MISS =
            "Tube.findIntersections() returned wrong result for a ray that misses the tube";
    /**
     * Error message for wrong intersection result for a general ray.
     */
    private static final String ERROR_INTERSECTION_GENERAL =
            "Tube.findIntersections() returned wrong result for a general ray";

    /**
     * Origin point of the tube axis used in the intersection tests.
     */
    private static final Point AXIS_HEAD = new Point(1, -2, 3);
    /**
     * Right surface point on the reference plane (axis + radius in the +X direction).
     */
    private static final Point SURFACE_RIGHT_ON_PLANE = new Point(2, -2, 3);
    /**
     * Left surface point on the reference plane (axis - radius in the +X direction).
     */
    private static final Point SURFACE_LEFT_ON_PLANE = new Point(0, -2, 3);
    /**
     * Tube radius used in the intersection tests.
     */
    private static final double INTERSECTION_RADIUS = 1d;
    /**
     * Direction of the tube axis used in the intersection tests.
     */
    private static final Vector AXIS_DIRECTION = new Vector(0, 1, 1).normalize();
    /**
     * General tube axis used in the intersection tests.
     */
    private static final Ray INTERSECTION_AXIS = new Ray(AXIS_HEAD, AXIS_DIRECTION);
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

        // TC11: A point on the tube surface opposite the axis head (projection = 0)
        assertDoesNotThrow(() -> tube.getNormal(POINT3), ERROR_GET_NORMAL);
        result = tube.getNormal(POINT3);
        // Ensure |n| = 1
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        // Ensure normal direction is correct
        assertEquals(Vector.AXIS_X, result, ERROR_NORMAL_DIRECTION3);

        // TC12: Point coinciding with the axis origin — not on the surface, must throw
        assertThrows(IllegalArgumentException.class,
                () -> tube.getNormal(Point.ZERO),
                "getNormal() with a point at the axis origin should throw IllegalArgumentException");
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
        Vector direction = new Vector(0, 1, 1);
        Point outsidePoint = new Point(4, -2, 3);
        Point insidePoint = new Point(3d / 2d, -2, 3);
        Point mantlePoint = SURFACE_RIGHT_ON_PLANE;

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray head outside the tube, before the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(outsidePoint.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC02: Ray head outside the tube, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(outsidePoint, direction)), ERROR_INTERSECTION_PARALLEL);
        // TC03: Ray head outside the tube, after the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(outsidePoint.add(direction), direction)), ERROR_INTERSECTION_PARALLEL);

        // TC04: Ray head inside the tube, before the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(insidePoint.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC05: Ray head inside the tube, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(insidePoint, direction)), ERROR_INTERSECTION_PARALLEL);
        // TC06: Ray head inside the tube, after the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(insidePoint.add(direction), direction)), ERROR_INTERSECTION_PARALLEL);

        // =============== Boundary Values Tests ==================

        // TC11: Ray head on the tube mantle, before the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(mantlePoint.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC12: Ray head on the tube mantle, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(mantlePoint, direction)), ERROR_INTERSECTION_PARALLEL);
        // TC13: Ray head on the tube mantle, after the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(mantlePoint.add(direction), direction)), ERROR_INTERSECTION_PARALLEL);

        // TC14: Ray head on the tube axis, before the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_PARALLEL);
        // TC15: Ray head on the tube axis, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD, direction)), ERROR_INTERSECTION_PARALLEL);
        // TC16: Ray head on the tube axis, after the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction), direction)), ERROR_INTERSECTION_PARALLEL);
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
        Point axisOffPlane = AXIS_HEAD.add(AXIS_DIRECTION.scale(2 * Math.sqrt(2)));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray head before first intersection, not on the reference plane (2 points)
        assertIntersectionsEquals(List.of(new Point(0, 0, 5), new Point(2, 0, 5)),
                INTERSECTION_TUBE.findIntersections(new Ray(axisOffPlane.add(direction.scale(-2)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC02: Ray head on first intersection, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                INTERSECTION_TUBE.findIntersections(new Ray(axisOffPlane.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC03: Ray head inside tube before center, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                INTERSECTION_TUBE.findIntersections(new Ray(axisOffPlane.add(direction.scale(-0.5)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC04: Ray head at center, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                INTERSECTION_TUBE.findIntersections(new Ray(axisOffPlane, direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC05: Ray head inside tube after center, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(new Point(2, 0, 5)),
                INTERSECTION_TUBE.findIntersections(new Ray(axisOffPlane.add(direction.scale(0.5)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC06: Ray head on second intersection, not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(axisOffPlane.add(direction), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC07: Ray head after second intersection, not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(axisOffPlane.add(direction.scale(2)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);

        // =============== Boundary Values Tests ==================

        // TC11: Ray head before first intersection, on the reference plane (2 points)
        assertIntersectionsEquals(List.of(SURFACE_LEFT_ON_PLANE, SURFACE_RIGHT_ON_PLANE),
                INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction.scale(-2)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC12: Ray head on first intersection, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(SURFACE_RIGHT_ON_PLANE),
                INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC13: Ray head inside tube before center, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(SURFACE_RIGHT_ON_PLANE),
                INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction.scale(-0.5)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC14: Ray head at center, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(SURFACE_RIGHT_ON_PLANE),
                INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD, direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC15: Ray head inside tube after center, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(SURFACE_RIGHT_ON_PLANE),
                INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction.scale(0.5)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC16: Ray head on second intersection, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC17: Ray head after second intersection, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction.scale(2)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
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
        Point chordMidOnPlane = new Point(1, -1.5, 2.5);
        Point chordMidOffPlane = chordMidOnPlane.add(new Vector(0, 2, 2));
        Point firstOnPlane = new Point((2 - Math.sqrt(2d)) / 2, -1.5, 2.5);
        Point secondOnPlane = new Point((2 + Math.sqrt(2d)) / 2, -1.5, 2.5);
        Point firstOffPlane = new Point((2 - Math.sqrt(2d)) / 2, 0.5, 4.5);
        Point secondOffPlane = new Point((2 + Math.sqrt(2d)) / 2, 0.5, 4.5);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray head before first intersection, not on the reference plane (2 points)
        assertIntersectionsEquals(List.of(firstOffPlane, secondOffPlane),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidOffPlane.add(direction.scale(-2)), direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC02: Ray head on first intersection, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(secondOffPlane),
                INTERSECTION_TUBE.findIntersections(new Ray(firstOffPlane, direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC03: Ray head between intersections, not on the reference plane (1 point)
        assertIntersectionsEquals(List.of(secondOffPlane),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidOffPlane, direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC04: Ray head on second intersection, not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondOffPlane, direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC05: Ray head after second intersection, not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(chordMidOffPlane.add(direction.scale(2)), direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);

        // =============== Boundary Values Tests ==================

        // TC11: Ray head before first intersection, on the reference plane (2 points)
        assertIntersectionsEquals(List.of(firstOnPlane, secondOnPlane),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidOnPlane.add(direction.scale(-2)), direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC12: Ray head on first intersection, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(secondOnPlane),
                INTERSECTION_TUBE.findIntersections(new Ray(firstOnPlane, direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC13: Ray head between intersections, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(secondOnPlane),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidOnPlane, direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC14: Ray head on second intersection, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondOnPlane, direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC15: Ray head after second intersection, on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray((chordMidOnPlane.add(direction.scale(2))), direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
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
        Point tangentOnPlane = new Point(1, -2 + 1 / Math.sqrt(2), 3 - 1 / Math.sqrt(2));
        Point tangentOffPlane = tangentOnPlane.add(AXIS_DIRECTION.scale(-2));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Tangent on reference plane, ray head before tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOnPlane.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC02: Tangent on reference plane, ray head on tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOnPlane, direction)), ERROR_INTERSECTION_TANGENT);
        // TC03: Tangent on reference plane, ray head after tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOnPlane.add(direction), direction)), ERROR_INTERSECTION_TANGENT);

        // =============== Boundary Values Tests ==================

        // TC11: Tangent not on reference plane, ray head before tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOffPlane.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC12: Tangent not on reference plane, ray head on tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOffPlane, direction)), ERROR_INTERSECTION_TANGENT);
        // TC13: Tangent not on reference plane, ray head after tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOffPlane.add(direction), direction)), ERROR_INTERSECTION_TANGENT);
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
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(new Point(-8, -3, 4), direction)), ERROR_INTERSECTION_MISS);

        // =============== Boundary Values Tests ==================

        // TC11: Ray misses the tube, ray head not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(new Point(1, 5, 7), direction)), ERROR_INTERSECTION_MISS);
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
        Point axisPointA = new Point(1, 8, 13);
        Point axisPointB = new Point(1, -2.5, 2.5);
        Point axisPointC = new Point(1, -7d / 4, 13d / 4);
        Point firstHitA = new Point(0, 7.5, 12.5);
        Point secondHitA = new Point(2, 8.5, 13.5);
        Point firstHitB = new Point(0, -3, 2);
        Point secondHitB = SURFACE_RIGHT_ON_PLANE;
        Point firstHitC = new Point(0, -9d / 4, 11d / 4);
        Point secondHitC = new Point(2, -5d / 4, 15d / 4);

        // ============ Equivalence Partitions Tests ==============

        // EP: both intersections are before the reference plane
        // TC01: Ray head before first intersection, both intersections before reference plane (2 points)
        assertIntersectionsEquals(List.of(firstHitA, secondHitA),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointA.add(direction.scale(-3)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC02: Ray head on first intersection, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitA),
                INTERSECTION_TUBE.findIntersections(new Ray(firstHitA, direction)), ERROR_INTERSECTION_GENERAL);
        // TC03: Ray head between intersections before center, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitA),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointA.add(direction.scale(-1d / 2d)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC04: Ray head at center, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitA),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointA, direction)), ERROR_INTERSECTION_GENERAL);
        // TC05: Ray head between intersections after center, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitA),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointA.add(direction.scale(1d / 2d)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC06: Ray head on second intersection, both intersections before reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondHitA, direction)), ERROR_INTERSECTION_GENERAL);
        // TC07: Ray head after second intersection, both intersections before reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(axisPointA.add(direction.scale(3)), direction)), ERROR_INTERSECTION_GENERAL);

        // EP: second intersection is on the reference plane
        // TC08: Ray head before first intersection, second intersection on reference plane (2 points)
        assertIntersectionsEquals(List.of(firstHitB, secondHitB),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointB.add(direction.scale(-3)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC09: Ray head on first intersection, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitB),
                INTERSECTION_TUBE.findIntersections(new Ray(firstHitB, direction)), ERROR_INTERSECTION_GENERAL);
        // TC10: Ray head between intersections before center, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitB),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointB.add(direction.scale(-1d / 2d)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC11: Ray head at center, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitB),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointB, direction)), ERROR_INTERSECTION_GENERAL);
        // TC12: Ray head between intersections after center, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitB),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointB.add(direction.scale(1d / 2d)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC13: Ray head on second intersection, second intersection on reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondHitB, direction)), ERROR_INTERSECTION_GENERAL);
        // TC14: Ray head after second intersection, second intersection on reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(axisPointB.add(direction.scale(3)), direction)), ERROR_INTERSECTION_GENERAL);

        // =============== Boundary Values Tests ==================

        // BVA: reference plane lies between the two intersections
        // TC15: Ray head before first intersection, reference plane between intersections (2 points)
        assertIntersectionsEquals(List.of(firstHitC, secondHitC),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointC.add(direction.scale(-3)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC16: Ray head on first intersection, reference plane between intersections (1 point)
        assertIntersectionsEquals(List.of(secondHitC),
                INTERSECTION_TUBE.findIntersections(new Ray(firstHitC, direction)), ERROR_INTERSECTION_GENERAL);
        // TC17: Ray head between intersections before center, reference plane between intersections (1 point)
        assertIntersectionsEquals(List.of(secondHitC),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointC.add(direction.scale(-0.5)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC18: Ray head at center on the reference plane, reference plane between intersections (1 point)
        assertIntersectionsEquals(List.of(secondHitC),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointC, direction)), ERROR_INTERSECTION_GENERAL);
        // TC19: Ray head between intersections after center, reference plane between intersections (1 point)
        assertIntersectionsEquals(List.of(secondHitC),
                INTERSECTION_TUBE.findIntersections(new Ray(axisPointC.add(direction.scale(0.5)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC20: Ray head on second intersection, reference plane between intersections (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondHitC, direction)), ERROR_INTERSECTION_GENERAL);
        // TC21: Ray head after second intersection, reference plane between intersections (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(axisPointC.add(direction.scale(3)), direction)), ERROR_INTERSECTION_GENERAL);
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
        Point chordMidA = new Point(1, -0.5, 5.5);
        Point chordMidB = new Point(4d / 3, -3, 8d / 3);
        Point chordMidC = new Point(4d / 3, -2, 11d / 3);
        Point firstHitA = new Point(2d / 3, -1, 16d / 3);
        Point secondHitA = new Point(2, 1, 6);
        Point firstHitB = new Point(2d / 3, -4, 7d / 3);
        Point secondHitB = SURFACE_RIGHT_ON_PLANE;
        Point firstHitC = new Point(2d / 3, -3, 10d / 3);
        Point secondHitC = new Point(2, -1, 4);

        // ============ Equivalence Partitions Tests ==============

        // EP: both intersections are before the reference plane
        // TC01: Ray head before first intersection, both intersections before reference plane (2 points)
        assertIntersectionsEquals(List.of(firstHitA, secondHitA),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidA.add(direction.scale(-3)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC02: Ray head on first intersection, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitA),
                INTERSECTION_TUBE.findIntersections(new Ray(firstHitA, direction)), ERROR_INTERSECTION_GENERAL);
        // TC03: Ray head between intersections, both intersections before reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitA),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidA, direction)), ERROR_INTERSECTION_GENERAL);
        // TC04: Ray head on second intersection, both intersections before reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondHitA, direction)), ERROR_INTERSECTION_GENERAL);
        // TC05: Ray head after second intersection, both intersections before reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(chordMidA.add(direction.scale(3)), direction)), ERROR_INTERSECTION_GENERAL);

        // EP: second intersection is on the reference plane
        // TC06: Ray head before first intersection, second intersection on reference plane (2 points)
        assertIntersectionsEquals(List.of(firstHitB, secondHitB),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidB.add(direction.scale(-9)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC07: Ray head on first intersection, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitB),
                INTERSECTION_TUBE.findIntersections(new Ray(firstHitB, direction)), ERROR_INTERSECTION_GENERAL);
        // TC08: Ray head between intersections, second intersection on reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitB),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidB, direction)), ERROR_INTERSECTION_GENERAL);
        // TC09: Ray head on second intersection, second intersection on reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondHitB, direction)), ERROR_INTERSECTION_GENERAL);
        // TC10: Ray head after second intersection, second intersection on reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(chordMidB.add(direction.scale(9)), direction)), ERROR_INTERSECTION_GENERAL);

        // =============== Boundary Values Tests ==================

        // BVA: reference plane lies between the two intersections
        // TC11: Ray head before first intersection, reference plane between intersections (2 points)
        assertIntersectionsEquals(List.of(firstHitC, secondHitC),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidC.add(direction.scale(-3)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC12: Ray head on first intersection, reference plane between intersections (1 point)
        assertIntersectionsEquals(List.of(secondHitC),
                INTERSECTION_TUBE.findIntersections(new Ray(firstHitC, direction)), ERROR_INTERSECTION_GENERAL);
        // TC13: Ray head between intersections, before the reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitC),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidC.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC14: Ray head between intersections, on the reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitC),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidC, direction)), ERROR_INTERSECTION_GENERAL);
        // TC15: Ray head between intersections, after the reference plane (1 point)
        assertIntersectionsEquals(List.of(secondHitC),
                INTERSECTION_TUBE.findIntersections(new Ray(chordMidC.add(direction), direction)), ERROR_INTERSECTION_GENERAL);
        // TC16: Ray head on second intersection, reference plane between intersections (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondHitC, direction)), ERROR_INTERSECTION_GENERAL);
        // TC17: Ray head after second intersection, reference plane between intersections (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(chordMidC.add(direction.scale(3)), direction)), ERROR_INTERSECTION_GENERAL);
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
        Point firstHit = new Point(0, -2.5, 2.5);
        Point secondHit = new Point(2, -1.5, 3.5);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray head before first intersection, line through axis head (2 points)
        assertIntersectionsEquals(List.of(firstHit, secondHit),
                INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction.scale(-3)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC02: Ray head on first intersection, line through axis head (1 point)
        assertIntersectionsEquals(List.of(secondHit),
                INTERSECTION_TUBE.findIntersections(new Ray(firstHit, direction)), ERROR_INTERSECTION_GENERAL);
        // TC03: Ray head inside tube before the axis head, line through axis head (1 point)
        assertIntersectionsEquals(List.of(secondHit),
                INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction.scale(-1d / 2d)), direction)), ERROR_INTERSECTION_GENERAL);
        // TC04: Ray head at the axis head, line through axis head (1 point)
        assertIntersectionsEquals(List.of(secondHit),
                INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD, direction)), ERROR_INTERSECTION_GENERAL);
        // TC05: Ray head inside tube after the axis head, line through axis head (1 point)
        assertIntersectionsEquals(List.of(secondHit),
                INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction.scale(1d / 2d)), direction)), ERROR_INTERSECTION_GENERAL);

        // =============== Boundary Values Tests ==================

        // TC11: Ray head on second intersection, line through axis head (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(secondHit, direction)), ERROR_INTERSECTION_GENERAL);
        // TC12: Ray head after second intersection, line through axis head (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(AXIS_HEAD.add(direction.scale(3)), direction)), ERROR_INTERSECTION_GENERAL);
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
        Point tangentOnPlane = SURFACE_LEFT_ON_PLANE;
        Point tangentOffPlane = tangentOnPlane.add(AXIS_DIRECTION.scale(-2));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Tangent on reference plane, ray head before tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOnPlane.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC02: Tangent on reference plane, ray head on tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOnPlane, direction)), ERROR_INTERSECTION_TANGENT);
        // TC03: Tangent on reference plane, ray head after tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOnPlane.add(direction), direction)), ERROR_INTERSECTION_TANGENT);

        // =============== Boundary Values Tests ==================

        // TC11: Tangent not on reference plane, ray head before tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOffPlane.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC12: Tangent not on reference plane, ray head on tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOffPlane, direction)), ERROR_INTERSECTION_TANGENT);
        // TC13: Tangent not on reference plane, ray head after tangent point (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(tangentOffPlane.add(direction), direction)), ERROR_INTERSECTION_TANGENT);
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
        Point missOnPlane = new Point(4, -4, 5);
        Point missOffPlane = missOnPlane.add(direction.scale(2));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray misses the tube, ray head on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(missOnPlane, direction)), ERROR_INTERSECTION_MISS);

        // =============== Boundary Values Tests ==================

        // TC11: Ray misses the tube, ray head not on the reference plane (0 points)
        assertNull(INTERSECTION_TUBE.findIntersections(new Ray(missOffPlane, direction)), ERROR_INTERSECTION_MISS);
    }

    /**
     * Test method for {@link Tube#equals(Object)} and {@link Tube#hashCode()}.
     */
    @Test
    void testEqualsAndHashCode() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Equal tubes constructed independently
        Tube t1 = new Tube(1d, AXIS);
        Tube t2 = new Tube(1d, new Ray(Point.ZERO, Vector.AXIS_Z));
        assertEquals(t1, t2, "Equal tubes should compare equal");
        assertEquals(t1.hashCode(), t2.hashCode(), "Equal tubes should have equal hashCode");
        // TC02: Different radius
        assertNotEquals(t1, new Tube(2d, AXIS), "Tubes with different radii should not be equal");
        // TC03: Different axis line
        assertNotEquals(t1, new Tube(1d, new Ray(new Point(5, 0, 0), Vector.AXIS_Z)),
                "Tubes on different axis lines should not be equal");

        // =============== Boundary Values Tests ==================
        // TC11: Same axis line, reversed direction and a different origin point on that line —
        // the exact case that used to break the equals/hashCode contract
        Tube reversed = new Tube(1d, new Ray(new Point(0, 0, 10), Vector.AXIS_Z.scale(-1)));
        assertEquals(t1, reversed, "Tubes on the same axis line with reversed direction should be equal");
        assertEquals(t1.hashCode(), reversed.hashCode(),
                "Tubes on the same axis line with reversed direction should have equal hashCode");
        // TC12: A tube equals itself
        assertEquals(t1, t1, "A tube should equal itself");
        // TC13: Not equal to null / a different type
        assertNotEquals(t1, null, "A tube should not equal null");
        assertNotEquals(t1, "not a Tube", "A tube should not equal an object of a different type");
    }

    /**
     * Test method for the inherited {@link geometries.api.Geometry#setEmission(Color)},
     * {@link geometries.api.Geometry#getEmission()}, {@link geometries.api.Geometry#setMaterial(Material)}
     * and {@link geometries.api.Geometry#getMaterial()}.
     */
    @Test
    void testEmissionAndMaterial() {
        // ============ Equivalence Partitions Tests ==============
        Tube tube = new Tube(1d, AXIS);
        Color emission = new Color(10, 20, 30);
        Material material = new Material().setKD(0.5);

        // TC01: setEmission returns the same instance (chaining) and stores the value
        assertSame(tube, tube.setEmission(emission), "setEmission() should return the same instance for chaining");
        assertEquals(emission, tube.getEmission(), "getEmission() did not return the emission color that was set");

        // TC02: setMaterial returns the same instance (chaining) and stores the value
        assertSame(tube, tube.setMaterial(material), "setMaterial() should return the same instance for chaining");
        assertSame(material, tube.getMaterial(), "getMaterial() did not return the material that was set");

        // =============== Boundary Values Tests ==================
        // TC11: A freshly constructed geometry defaults to black emission and a default material
        Tube fresh = new Tube(1d, AXIS);
        assertEquals(Color.BLACK, fresh.getEmission(), "A new geometry should default to black emission");
        assertNotNull(fresh.getMaterial(), "A new geometry should default to a non-null material");
    }

    /**
     * Test method for {@link Tube#getBoundingBox()}.
     * A tube is infinite, so it must have no bounding box.
     */
    @Test
    void testGetBoundingBox() {
        // =============== Boundary Values Tests ==================
        // TC11: An infinite geometry has a null bounding box
        assertNull(new Tube(1d, AXIS).getBoundingBox(), "A tube is infinite and should have a null bounding box");
    }
}

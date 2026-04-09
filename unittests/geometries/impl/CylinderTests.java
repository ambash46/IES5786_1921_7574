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
 *
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
     * Error message for wrong intersection result for a perpendicular tangent ray.
     */
    private static final String ERROR_INTERSECTION_TANGENT =
            "Cylinder.findIntersections() returned wrong result for a perpendicular tangent ray";
    /**
     * Error message for wrong intersection result for a perpendicular ray through the axis.
     */
    private static final String ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS =
            "Cylinder.findIntersections() returned wrong result for a perpendicular ray through the axis";
    /**
     * Error message for wrong intersection result for a perpendicular ray off the axis.
     */
    private static final String ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS =
            "Cylinder.findIntersections() returned wrong result for a perpendicular ray off the axis";
    /**
     * Error message for wrong intersection result for a general ray that intersects only the bases.
     */
    private static final String ERROR_INTERSECTION_BASES_ONLY =
            "Cylinder.findIntersections() returned wrong result for a general ray that intersects only the bases";
    /**
     * Error message for wrong intersection result for a general ray that intersects the body twice.
     */
    private static final String ERROR_INTERSECTION_GENERAL_BODY_TWICE =
            "Cylinder.findIntersections() returned wrong result for a general ray that intersects the body twice";
    /**
     * Error message for wrong intersection result for a general ray that intersects one base and the body.
     */
    private static final String ERROR_INTERSECTION_BASE_AND_BODY =
            "Cylinder.findIntersections() returned wrong result for a general ray that intersects one base and the body";
    /**
     * Error message for wrong intersection result for a general tangent ray.
     */
    private static final String ERROR_INTERSECTION_GENERAL_TANGENT =
            "Cylinder.findIntersections() returned wrong result for a general tangent ray";
    /**
     * Error message for wrong intersection result for a general ray that misses the cylinder.
     */
    private static final String ERROR_INTERSECTION_GENERAL_MISS =
            "Cylinder.findIntersections() returned wrong result for a general ray that misses the cylinder";

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
     * Direction perpendicular to the cylinder axis used to locate rim and
     * off-axis points on the caps and mantle.
     */
    private static final Vector CAP_PERPENDICULAR = new Vector(0, 4, -3).normalize();
    /**
     * Midpoint of the cylinder axis at half the cylinder height.
     */
    private static final Point AXIS_MID =
            BOTTOM_CAP_CENTER.add(INTERSECTION_AXIS_DIRECTION.scale(INTERSECTION_HEIGHT / 2d));

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
        Vector direction = INTERSECTION_AXIS_DIRECTION;
        Point outsideBase = new Point(7, 2, 3);   // distance 6 from axis > radius 4
        Point mantleBase = new Point(5, 2, 3);   // distance 4 from axis = radius
        Point insideBase = new Point(3, 2, 3);   // distance 2 from axis < radius
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
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(new Point(-10, -1, -1), direction)), ERROR_INTERSECTION_MISS);
        // TC02: Ray line crosses the infinite tube, both intersections after the top cap (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(new Point(-10, 11, 15), direction)), ERROR_INTERSECTION_MISS);
        // TC03: Ray line misses the infinite tube entirely (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(new Point(6, 12, 7), direction)), ERROR_INTERSECTION_MISS);
    }

    /**
     * Group 3: ray perpendicular to the cylinder axis, tangent and cap-overlap cases.
     * The ray direction is perpendicular to the axis.
     * Cases cover:
     * (1) true tangency to the cylinder mantle at one point,
     * (2) true tangency to the rim of the bottom and top caps,
     * (3) rays lying in a cap plane along a chord that does not pass through the cap center,
     * (4) rays lying in a cap plane along a diameter through the cap center.
     * These cap-plane cases overlap the cap in a segment rather than crossing the solid,
     * so no discrete intersection points are expected.
     */
    @Test
    void testFindIntersectionsGroup3PerpendicularTangent() {
        Vector direction = Vector.AXIS_X;

        Point mantleTangentPoint = AXIS_MID.add(CAP_PERPENDICULAR.scale(INTERSECTION_RADIUS));

        Point bottomRimTangentPoint = BOTTOM_CAP_CENTER.add(CAP_PERPENDICULAR.scale(INTERSECTION_RADIUS));
        Point topRimTangentPoint = TOP_CAP_CENTER.add(CAP_PERPENDICULAR.scale(INTERSECTION_RADIUS));

        Point bottomChordMid = BOTTOM_CAP_CENTER.add(CAP_PERPENDICULAR.scale(2d));
        Point topChordMid = TOP_CAP_CENTER.add(CAP_PERPENDICULAR.scale(2d));
        double chordHalfLength = 2d * Math.sqrt(3d);
        Point bottomChordFirst = bottomChordMid.add(direction.scale(-chordHalfLength));
        Point bottomChordSecond = bottomChordMid.add(direction.scale(chordHalfLength));
        Point topChordFirst = topChordMid.add(direction.scale(-chordHalfLength));
        Point topChordSecond = topChordMid.add(direction.scale(chordHalfLength));

        Point bottomDiameterFirst = BOTTOM_CAP_CENTER.add(direction.scale(-INTERSECTION_RADIUS));
        Point bottomDiameterSecond = BOTTOM_CAP_CENTER.add(direction.scale(INTERSECTION_RADIUS));
        Point topDiameterFirst = TOP_CAP_CENTER.add(direction.scale(-INTERSECTION_RADIUS));
        Point topDiameterSecond = TOP_CAP_CENTER.add(direction.scale(INTERSECTION_RADIUS));

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray tangent to the cylinder mantle, ray head before the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleTangentPoint.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC02: Ray tangent to the cylinder mantle, ray head on the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleTangentPoint, direction)), ERROR_INTERSECTION_TANGENT);
        // TC03: Ray tangent to the cylinder mantle, ray head after the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleTangentPoint.add(direction), direction)), ERROR_INTERSECTION_TANGENT);

        // TC04: Ray tangent to the bottom cap at the rim, ray head before the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRimTangentPoint.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC05: Ray tangent to the bottom cap at the rim, ray head on the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRimTangentPoint, direction)), ERROR_INTERSECTION_TANGENT);
        // TC06: Ray tangent to the bottom cap at the rim, ray head after the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRimTangentPoint.add(direction), direction)), ERROR_INTERSECTION_TANGENT);

        // TC07: Ray lying in the bottom cap plane along a chord, ray head before the first edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomChordMid.add(direction.scale(-4)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC08: Ray lying in the bottom cap plane along a chord, ray head on the first edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomChordFirst, direction)), ERROR_INTERSECTION_TANGENT);
        // TC09: Ray lying in the bottom cap plane along a chord, ray head between the two edge points (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomChordMid, direction)), ERROR_INTERSECTION_TANGENT);
        // TC10: Ray lying in the bottom cap plane along a chord, ray head on the second edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomChordSecond, direction)), ERROR_INTERSECTION_TANGENT);
        // TC11: Ray lying in the bottom cap plane along a chord, ray head after the second edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomChordMid.add(direction.scale(4)), direction)), ERROR_INTERSECTION_TANGENT);

        // TC12: Ray lying in the bottom cap plane through the center, ray head before the first edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(direction.scale(-5)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC13: Ray lying in the bottom cap plane through the center, ray head on the first edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomDiameterFirst, direction)), ERROR_INTERSECTION_TANGENT);
        // TC14: Ray lying in the bottom cap plane through the center, ray head after the first edge point and before the center (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC15: Ray lying in the bottom cap plane through the center, ray head on the center point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER, direction)), ERROR_INTERSECTION_TANGENT);
        // TC16: Ray lying in the bottom cap plane through the center, ray head after the center point and before the second edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(direction), direction)), ERROR_INTERSECTION_TANGENT);
        // TC17: Ray lying in the bottom cap plane through the center, ray head on the second edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomDiameterSecond, direction)), ERROR_INTERSECTION_TANGENT);
        // TC18: Ray lying in the bottom cap plane through the center, ray head after the second edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(direction.scale(5)), direction)), ERROR_INTERSECTION_TANGENT);

        // =============== Boundary Values Tests ==================

        // TC19: Ray tangent to the top cap at the rim, ray head before the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRimTangentPoint.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC20: Ray tangent to the top cap at the rim, ray head on the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRimTangentPoint, direction)), ERROR_INTERSECTION_TANGENT);
        // TC21: Ray tangent to the top cap at the rim, ray head after the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRimTangentPoint.add(direction), direction)), ERROR_INTERSECTION_TANGENT);

        // TC22: Ray lying in the top cap plane along a chord, ray head before the first edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topChordMid.add(direction.scale(-4)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC23: Ray lying in the top cap plane along a chord, ray head on the first edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topChordFirst, direction)), ERROR_INTERSECTION_TANGENT);
        // TC24: Ray lying in the top cap plane along a chord, ray head between the two edge points (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topChordMid, direction)), ERROR_INTERSECTION_TANGENT);
        // TC25: Ray lying in the top cap plane along a chord, ray head on the second edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topChordSecond, direction)), ERROR_INTERSECTION_TANGENT);
        // TC26: Ray lying in the top cap plane along a chord, ray head after the second edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topChordMid.add(direction.scale(4)), direction)), ERROR_INTERSECTION_TANGENT);

        // TC27: Ray lying in the top cap plane through the center, ray head before the first edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER.add(direction.scale(-5)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC28: Ray lying in the top cap plane through the center, ray head on the first edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topDiameterFirst, direction)), ERROR_INTERSECTION_TANGENT);
        // TC29: Ray lying in the top cap plane through the center, ray head after the first edge point and before the center (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_TANGENT);
        // TC30: Ray lying in the top cap plane through the center, ray head on the center point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER, direction)), ERROR_INTERSECTION_TANGENT);
        // TC31: Ray lying in the top cap plane through the center, ray head after the center point and before the second edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER.add(direction), direction)), ERROR_INTERSECTION_TANGENT);
        // TC32: Ray lying in the top cap plane through the center, ray head on the second edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topDiameterSecond, direction)), ERROR_INTERSECTION_TANGENT);
        // TC33: Ray lying in the top cap plane through the center, ray head after the second edge point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER.add(direction.scale(5)), direction)), ERROR_INTERSECTION_TANGENT);
    }

    /**
     * Group 4: ray perpendicular to the cylinder axis, line intersects the cylinder body twice.
     * Two line families are tested:
     * (1) the line passes through the axis of the cylinder,
     * (2) the line is off axis but still crosses the mantle twice.
     * In each family, the ray head is placed at all relevant positions along the ray.
     */
    @Test
    void testFindIntersectionsGroup4PerpendicularBodyTwice() {
        Vector direction = Vector.AXIS_X;

        Point firstAxisHit = AXIS_MID.add(direction.scale(-INTERSECTION_RADIUS));
        Point secondAxisHit = AXIS_MID.add(direction.scale(INTERSECTION_RADIUS));

        Point chordMid = AXIS_MID.add(CAP_PERPENDICULAR.scale(2d));
        double chordHalfLength = 2d * Math.sqrt(3d);
        Point firstChordHit = chordMid.add(direction.scale(-chordHalfLength));
        Point secondChordHit = chordMid.add(direction.scale(chordHalfLength));

        // ============ Equivalence Partitions Tests ==============

        // Through axis

        // TC01: Ray head before the first body intersection, line through the axis (2 points)
        assertIntersectionsEquals(List.of(firstAxisHit, secondAxisHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(AXIS_MID.add(direction.scale(-5)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC02: Ray head on the first body intersection, line through the axis (1 point)
        assertIntersectionsEquals(List.of(secondAxisHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(firstAxisHit, direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC03: Ray head after the first intersection and before the axis, line through the axis (1 point)
        assertIntersectionsEquals(List.of(secondAxisHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(AXIS_MID.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC04: Ray head on the axis, line through the axis (1 point)
        assertIntersectionsEquals(List.of(secondAxisHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(AXIS_MID, direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC05: Ray head after the axis and before the second intersection, line through the axis (1 point)
        assertIntersectionsEquals(List.of(secondAxisHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(AXIS_MID.add(direction), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC06: Ray head on the second body intersection, line through the axis (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(secondAxisHit, direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);
        // TC07: Ray head after the second body intersection, line through the axis (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(AXIS_MID.add(direction.scale(5)), direction)), ERROR_INTERSECTION_PERPENDICULAR_THROUGH_AXIS);

        // =============== Boundary Values Tests ==================

        // Off axis

        // TC08: Ray head before the first body intersection, line off axis (2 points)
        assertIntersectionsEquals(List.of(firstChordHit, secondChordHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(chordMid.add(direction.scale(-4)), direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC09: Ray head on the first body intersection, line off axis (1 point)
        assertIntersectionsEquals(List.of(secondChordHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(firstChordHit, direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC10: Ray head between the two body intersections, line off axis (1 point)
        assertIntersectionsEquals(List.of(secondChordHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(chordMid, direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC11: Ray head on the second body intersection, line off axis (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(secondChordHit, direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
        // TC12: Ray head after the second body intersection, line off axis (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(chordMid.add(direction.scale(4)), direction)), ERROR_INTERSECTION_PERPENDICULAR_OFF_AXIS);
    }

    /**
     * Group 5: general ray intersects only the cylinder bases.
     * The ray enters through the bottom cap and exits through the top cap
     * without crossing the mantle. Seven entry/exit combinations are tested:
     * rim-to-rim, rim-to-inner, rim-to-center, inner-to-rim, inner-to-center,
     * center-to-rim, and center-to-inner.
     * For each combination, the ray head is placed before the entry point,
     * on the entry point, between the two base intersections, on the exit point,
     * and after the exit point.
     */
    @Test
    void testFindIntersectionsGroup5GeneralBasesOnly() {
        Point bottomRim = BOTTOM_CAP_CENTER.add(Vector.AXIS_X.scale(INTERSECTION_RADIUS));
        Point topRim = TOP_CAP_CENTER.add(CAP_PERPENDICULAR.scale(INTERSECTION_RADIUS));
        Point bottomInner = BOTTOM_CAP_CENTER.add(Vector.AXIS_X.scale(2)).add(CAP_PERPENDICULAR);
        Point topInner = TOP_CAP_CENTER.add(Vector.AXIS_X.scale(2)).add(CAP_PERPENDICULAR);

        Vector rimToRim = topRim.subtract(bottomRim);
        Vector rimToInner = topInner.subtract(bottomRim);
        Vector rimToCenter = TOP_CAP_CENTER.subtract(bottomRim);
        Vector innerToRim = topRim.subtract(bottomInner);
        Vector innerToCenter = TOP_CAP_CENTER.subtract(bottomInner);
        Vector centerToRim = topRim.subtract(BOTTOM_CAP_CENTER);
        Vector centerToInner = topInner.subtract(BOTTOM_CAP_CENTER);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray enters through the bottom rim and exits through the top rim, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(bottomRim, topRim),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim.add(rimToRim.scale(-1)), rimToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC02: Ray enters through the bottom rim and exits through the top rim, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(topRim),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim, rimToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC03: Ray enters through the bottom rim and exits through the top rim, ray head between the two base intersections (1 point)
        assertIntersectionsEquals(List.of(topRim),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim.add(rimToRim.scale(1d / 2d)), rimToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC04: Ray enters through the bottom rim and exits through the top rim, ray head on the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRim, rimToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC05: Ray enters through the bottom rim and exits through the top rim, ray head after the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRim.add(rimToRim), rimToRim)), ERROR_INTERSECTION_BASES_ONLY);

        // TC06: Ray enters through the bottom rim and exits through an inner point of the top cap, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(bottomRim, topInner),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim.add(rimToInner.scale(-1)), rimToInner)), ERROR_INTERSECTION_BASES_ONLY);
        // TC07: Ray enters through the bottom rim and exits through an inner point of the top cap, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(topInner),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim, rimToInner)), ERROR_INTERSECTION_BASES_ONLY);
        // TC08: Ray enters through the bottom rim and exits through an inner point of the top cap, ray head between the two base intersections (1 point)
        assertIntersectionsEquals(List.of(topInner),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim.add(rimToInner.scale(1d / 2d)), rimToInner)), ERROR_INTERSECTION_BASES_ONLY);
        // TC09: Ray enters through the bottom rim and exits through an inner point of the top cap, ray head on the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topInner, rimToInner)), ERROR_INTERSECTION_BASES_ONLY);
        // TC10: Ray enters through the bottom rim and exits through an inner point of the top cap, ray head after the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topInner.add(rimToInner), rimToInner)), ERROR_INTERSECTION_BASES_ONLY);

        // TC11: Ray enters through an inner point of the bottom cap and exits through the top rim, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(bottomInner, topRim),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomInner.add(innerToRim.scale(-1)), innerToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC12: Ray enters through an inner point of the bottom cap and exits through the top rim, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(topRim),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomInner, innerToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC13: Ray enters through an inner point of the bottom cap and exits through the top rim, ray head between the two base intersections (1 point)
        assertIntersectionsEquals(List.of(topRim),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomInner.add(innerToRim.scale(1d / 2d)), innerToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC14: Ray enters through an inner point of the bottom cap and exits through the top rim, ray head on the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRim, innerToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC15: Ray enters through an inner point of the bottom cap and exits through the top rim, ray head after the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRim.add(innerToRim), innerToRim)), ERROR_INTERSECTION_BASES_ONLY);

        // =============== Boundary Values Tests ==================

        // TC16: Ray enters through the bottom rim and exits through the top cap center, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(bottomRim, TOP_CAP_CENTER),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim.add(rimToCenter.scale(-1)), rimToCenter)), ERROR_INTERSECTION_BASES_ONLY);
        // TC17: Ray enters through the bottom rim and exits through the top cap center, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(TOP_CAP_CENTER),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim, rimToCenter)), ERROR_INTERSECTION_BASES_ONLY);
        // TC18: Ray enters through the bottom rim and exits through the top cap center, ray head between the two base intersections (1 point)
        assertIntersectionsEquals(List.of(TOP_CAP_CENTER),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim.add(rimToCenter.scale(1d / 2d)), rimToCenter)), ERROR_INTERSECTION_BASES_ONLY);
        // TC19: Ray enters through the bottom rim and exits through the top cap center, ray head on the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER, rimToCenter)), ERROR_INTERSECTION_BASES_ONLY);
        // TC20: Ray enters through the bottom rim and exits through the top cap center, ray head after the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER.add(rimToCenter), rimToCenter)), ERROR_INTERSECTION_BASES_ONLY);

        // TC21: Ray enters through an inner point of the bottom cap and exits through the top cap center, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(bottomInner, TOP_CAP_CENTER),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomInner.add(innerToCenter.scale(-1)), innerToCenter)), ERROR_INTERSECTION_BASES_ONLY);
        // TC22: Ray enters through an inner point of the bottom cap and exits through the top cap center, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(TOP_CAP_CENTER),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomInner, innerToCenter)), ERROR_INTERSECTION_BASES_ONLY);
        // TC23: Ray enters through an inner point of the bottom cap and exits through the top cap center, ray head between the two base intersections (1 point)
        assertIntersectionsEquals(List.of(TOP_CAP_CENTER),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomInner.add(innerToCenter.scale(1d / 2d)), innerToCenter)), ERROR_INTERSECTION_BASES_ONLY);
        // TC24: Ray enters through an inner point of the bottom cap and exits through the top cap center, ray head on the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER, innerToCenter)), ERROR_INTERSECTION_BASES_ONLY);
        // TC25: Ray enters through an inner point of the bottom cap and exits through the top cap center, ray head after the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER.add(innerToCenter), innerToCenter)), ERROR_INTERSECTION_BASES_ONLY);

        // TC26: Ray enters through the bottom cap center and exits through the top rim, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(BOTTOM_CAP_CENTER, topRim),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(centerToRim.scale(-1)), centerToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC27: Ray enters through the bottom cap center and exits through the top rim, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(topRim),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER, centerToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC28: Ray enters through the bottom cap center and exits through the top rim, ray head between the two base intersections (1 point)
        assertIntersectionsEquals(List.of(topRim),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(centerToRim.scale(1d / 2d)), centerToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC29: Ray enters through the bottom cap center and exits through the top rim, ray head on the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRim, centerToRim)), ERROR_INTERSECTION_BASES_ONLY);
        // TC30: Ray enters through the bottom cap center and exits through the top rim, ray head after the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRim.add(centerToRim), centerToRim)), ERROR_INTERSECTION_BASES_ONLY);

        // TC31: Ray enters through the bottom cap center and exits through an inner point of the top cap, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(BOTTOM_CAP_CENTER, topInner),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(centerToInner.scale(-1)), centerToInner)), ERROR_INTERSECTION_BASES_ONLY);
        // TC32: Ray enters through the bottom cap center and exits through an inner point of the top cap, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(topInner),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER, centerToInner)), ERROR_INTERSECTION_BASES_ONLY);
        // TC33: Ray enters through the bottom cap center and exits through an inner point of the top cap, ray head between the two base intersections (1 point)
        assertIntersectionsEquals(List.of(topInner),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(centerToInner.scale(1d / 2d)), centerToInner)), ERROR_INTERSECTION_BASES_ONLY);
        // TC34: Ray enters through the bottom cap center and exits through an inner point of the top cap, ray head on the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topInner, centerToInner)), ERROR_INTERSECTION_BASES_ONLY);
        // TC35: Ray enters through the bottom cap center and exits through an inner point of the top cap, ray head after the exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topInner.add(centerToInner), centerToInner)), ERROR_INTERSECTION_BASES_ONLY);
    }

    /**
     * Group 6: general ray intersects the cylinder body twice.
     * Two line families are tested:
     * (1) a general line that passes through the cylinder axis,
     * (2) a general line that is off axis but still cuts the mantle twice.
     * The intersections are strictly on the body, not on either cap.
     */
    @Test
    void testFindIntersectionsGroup6GeneralBodyTwice() {
        Vector direction = (Vector.AXIS_X.add(INTERSECTION_AXIS_DIRECTION)).normalize();

        double throughAxisDistance = 4d * Math.sqrt(2d);
        Point firstAxisHit = AXIS_MID.add(direction.scale(-throughAxisDistance));
        Point secondAxisHit = AXIS_MID.add(direction.scale(throughAxisDistance));

        Point chordMid = AXIS_MID.add(CAP_PERPENDICULAR.scale(2d));
        double offAxisDistance = 2d * Math.sqrt(6d);
        Point firstChordHit = chordMid.add(direction.scale(-offAxisDistance));
        Point secondChordHit = chordMid.add(direction.scale(offAxisDistance));

        // ============ Equivalence Partitions Tests ==============

        // Through axis

        // TC01: Ray head before the first body intersection, general line through the axis (2 points)
        assertIntersectionsEquals(List.of(firstAxisHit, secondAxisHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(firstAxisHit.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
        // TC02: Ray head on the first body intersection, general line through the axis (1 point)
        assertIntersectionsEquals(List.of(secondAxisHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(firstAxisHit, direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
        // TC03: Ray head after the first intersection and before the axis, general line through the axis (1 point)
        assertIntersectionsEquals(List.of(secondAxisHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(AXIS_MID.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
        // TC04: Ray head on the axis, general line through the axis (1 point)
        assertIntersectionsEquals(List.of(secondAxisHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(AXIS_MID, direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
        // TC05: Ray head after the axis and before the second intersection, general line through the axis (1 point)
        assertIntersectionsEquals(List.of(secondAxisHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(AXIS_MID.add(direction), direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
        // TC06: Ray head on the second body intersection, general line through the axis (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(secondAxisHit, direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
        // TC07: Ray head after the second body intersection, general line through the axis (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(secondAxisHit.add(direction), direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);

        // =============== Boundary Values Tests ==================

        // Off axis

        // TC08: Ray head before the first body intersection, general line off axis (2 points)
        assertIntersectionsEquals(List.of(firstChordHit, secondChordHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(firstChordHit.add(direction.scale(-1)), direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
        // TC09: Ray head on the first body intersection, general line off axis (1 point)
        assertIntersectionsEquals(List.of(secondChordHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(firstChordHit, direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
        // TC10: Ray head between the two body intersections, general line off axis (1 point)
        assertIntersectionsEquals(List.of(secondChordHit),
                INTERSECTION_CYLINDER.findIntersections(new Ray(chordMid, direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
        // TC11: Ray head on the second body intersection, general line off axis (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(secondChordHit, direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
        // TC12: Ray head after the second body intersection, general line off axis (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(secondChordHit.add(direction), direction)), ERROR_INTERSECTION_GENERAL_BODY_TWICE);
    }

    /**
     * Group 7: general ray enters through one base and exits through the cylinder body.
     * Only the '-' direction family is tested: the ray starts outside the cylinder,
     * first intersects one of the caps, then exits through the mantle.
     * Six entry/exit combinations are covered:
     * bottom rim -> body, bottom inner -> body, bottom center -> body,
     * top rim -> body, top inner -> body, top center -> body.
     * For each, the ray head is placed before the entry point, on the entry point,
     * between the two intersections, on the body exit point, and after the body exit point.
     */
    @Test
    void testFindIntersectionsGroup7GeneralBaseAndBody() {
        Point bottomRim = BOTTOM_CAP_CENTER.add(Vector.AXIS_X.scale(INTERSECTION_RADIUS));
        Point topRim = TOP_CAP_CENTER.add(Vector.AXIS_X.scale(INTERSECTION_RADIUS));
        Point bottomInner = BOTTOM_CAP_CENTER.add(Vector.AXIS_X.scale(2)).add(CAP_PERPENDICULAR);
        Point topInner = TOP_CAP_CENTER.add(Vector.AXIS_X.scale(2)).add(CAP_PERPENDICULAR);

        Point bodyExitFromBottom = AXIS_MID.add(CAP_PERPENDICULAR.scale(INTERSECTION_RADIUS));
        Point bodyExitFromTop = AXIS_MID.add(CAP_PERPENDICULAR.scale(-INTERSECTION_RADIUS));

        Vector bottomRimToBody = bodyExitFromBottom.subtract(bottomRim);
        Vector bottomInnerToBody = bodyExitFromBottom.subtract(bottomInner);
        Vector bottomCenterToBody = bodyExitFromBottom.subtract(BOTTOM_CAP_CENTER);
        Vector topRimToBody = bodyExitFromTop.subtract(topRim);
        Vector topInnerToBody = bodyExitFromTop.subtract(topInner);
        Vector topCenterToBody = bodyExitFromTop.subtract(TOP_CAP_CENTER);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray enters through the bottom rim and exits through the body, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(bottomRim, bodyExitFromBottom),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim.add(bottomRimToBody.scale(-1)), bottomRimToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC02: Ray enters through the bottom rim and exits through the body, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromBottom),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim, bottomRimToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC03: Ray enters through the bottom rim and exits through the body, ray head between the two intersections (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromBottom),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRim.add(bottomRimToBody.scale(1d / 2d)), bottomRimToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC04: Ray enters through the bottom rim and exits through the body, ray head on the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromBottom, bottomRimToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC05: Ray enters through the bottom rim and exits through the body, ray head after the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromBottom.add(bottomRimToBody), bottomRimToBody)), ERROR_INTERSECTION_BASE_AND_BODY);

        // TC06: Ray enters through an inner point of the bottom cap and exits through the body, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(bottomInner, bodyExitFromBottom),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomInner.add(bottomInnerToBody.scale(-1)), bottomInnerToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC07: Ray enters through an inner point of the bottom cap and exits through the body, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromBottom),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomInner, bottomInnerToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC08: Ray enters through an inner point of the bottom cap and exits through the body, ray head between the two intersections (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromBottom),
                INTERSECTION_CYLINDER.findIntersections(new Ray(bottomInner.add(bottomInnerToBody.scale(1d / 2d)), bottomInnerToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC09: Ray enters through an inner point of the bottom cap and exits through the body, ray head on the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromBottom, bottomInnerToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC10: Ray enters through an inner point of the bottom cap and exits through the body, ray head after the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromBottom.add(bottomInnerToBody), bottomInnerToBody)), ERROR_INTERSECTION_BASE_AND_BODY);

        // TC11: Ray enters through the bottom cap center and exits through the body, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(BOTTOM_CAP_CENTER, bodyExitFromBottom),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(bottomCenterToBody.scale(-1)), bottomCenterToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC12: Ray enters through the bottom cap center and exits through the body, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromBottom),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER, bottomCenterToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC13: Ray enters through the bottom cap center and exits through the body, ray head between the two intersections (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromBottom),
                INTERSECTION_CYLINDER.findIntersections(new Ray(BOTTOM_CAP_CENTER.add(bottomCenterToBody.scale(1d / 2d)), bottomCenterToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC14: Ray enters through the bottom cap center and exits through the body, ray head on the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromBottom, bottomCenterToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC15: Ray enters through the bottom cap center and exits through the body, ray head after the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromBottom.add(bottomCenterToBody), bottomCenterToBody)), ERROR_INTERSECTION_BASE_AND_BODY);

        // =============== Boundary Values Tests ==================

        // TC16: Ray enters through the top rim and exits through the body, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(topRim, bodyExitFromTop),
                INTERSECTION_CYLINDER.findIntersections(new Ray(topRim.add(topRimToBody.scale(-1)), topRimToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC17: Ray enters through the top rim and exits through the body, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromTop),
                INTERSECTION_CYLINDER.findIntersections(new Ray(topRim, topRimToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC18: Ray enters through the top rim and exits through the body, ray head between the two intersections (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromTop),
                INTERSECTION_CYLINDER.findIntersections(new Ray(topRim.add(topRimToBody.scale(1d / 2d)), topRimToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC19: Ray enters through the top rim and exits through the body, ray head on the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromTop, topRimToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC20: Ray enters through the top rim and exits through the body, ray head after the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromTop.add(topRimToBody), topRimToBody)), ERROR_INTERSECTION_BASE_AND_BODY);

        // TC21: Ray enters through an inner point of the top cap and exits through the body, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(topInner, bodyExitFromTop),
                INTERSECTION_CYLINDER.findIntersections(new Ray(topInner.add(topInnerToBody.scale(-1)), topInnerToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC22: Ray enters through an inner point of the top cap and exits through the body, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromTop),
                INTERSECTION_CYLINDER.findIntersections(new Ray(topInner, topInnerToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC23: Ray enters through an inner point of the top cap and exits through the body, ray head between the two intersections (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromTop),
                INTERSECTION_CYLINDER.findIntersections(new Ray(topInner.add(topInnerToBody.scale(1d / 2d)), topInnerToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC24: Ray enters through an inner point of the top cap and exits through the body, ray head on the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromTop, topInnerToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC25: Ray enters through an inner point of the top cap and exits through the body, ray head after the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromTop.add(topInnerToBody), topInnerToBody)), ERROR_INTERSECTION_BASE_AND_BODY);

        // TC26: Ray enters through the top cap center and exits through the body, ray head before the entry point (2 points)
        assertIntersectionsEquals(List.of(TOP_CAP_CENTER, bodyExitFromTop),
                INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER.add(topCenterToBody.scale(-1)), topCenterToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC27: Ray enters through the top cap center and exits through the body, ray head on the entry point (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromTop),
                INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER, topCenterToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC28: Ray enters through the top cap center and exits through the body, ray head between the two intersections (1 point)
        assertIntersectionsEquals(List.of(bodyExitFromTop),
                INTERSECTION_CYLINDER.findIntersections(new Ray(TOP_CAP_CENTER.add(topCenterToBody.scale(1d / 2d)), topCenterToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC29: Ray enters through the top cap center and exits through the body, ray head on the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromTop, topCenterToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
        // TC30: Ray enters through the top cap center and exits through the body, ray head after the body exit point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bodyExitFromTop.add(topCenterToBody), topCenterToBody)), ERROR_INTERSECTION_BASE_AND_BODY);
    }

    /**
     * Group 8: general tangent ray.
     * Three tangent families are tested:
     * (1) tangent to the cylinder body at one interior mantle point,
     * (2) tangent at the rim of the bottom cap,
     * (3) tangent at the rim of the top cap.
     * In each family the ray head is placed before the tangent point,
     * on the tangent point, and after the tangent point.
     * Expected: always 0 intersection points.
     */
    @Test
    void testFindIntersectionsGroup8GeneralTangent() {
        Point mantleTangentPoint = AXIS_MID.add(CAP_PERPENDICULAR.scale(INTERSECTION_RADIUS));
        Point bottomRimTangentPoint = BOTTOM_CAP_CENTER.add(CAP_PERPENDICULAR.scale(INTERSECTION_RADIUS));
        Point topRimTangentPoint = TOP_CAP_CENTER.add(CAP_PERPENDICULAR.scale(INTERSECTION_RADIUS));

        // Tangent to the circular cross-section at the chosen rim point.
        Vector tangentialDirection = INTERSECTION_AXIS_DIRECTION.crossProduct(CAP_PERPENDICULAR).normalize();
        // General tangent to the mantle: tangential around the cylinder plus axial motion.
        Vector bodyDirection = tangentialDirection.add(INTERSECTION_AXIS_DIRECTION).normalize();
        // At the bottom rim we choose a direction that is tangent at the rim and immediately goes below the bottom cap.
        Vector bottomRimDirection = tangentialDirection.add(INTERSECTION_AXIS_DIRECTION.scale(-1)).normalize();
        // At the top rim we choose a direction that is tangent at the rim and immediately goes above the top cap.
        Vector topRimDirection = tangentialDirection.add(INTERSECTION_AXIS_DIRECTION).normalize();

        // ============ Equivalence Partitions Tests ==============

        // TC01: General ray tangent to the cylinder body, ray head before the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleTangentPoint.add(bodyDirection.scale(-1)), bodyDirection)), ERROR_INTERSECTION_GENERAL_TANGENT);
        // TC02: General ray tangent to the cylinder body, ray head on the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleTangentPoint, bodyDirection)), ERROR_INTERSECTION_GENERAL_TANGENT);
        // TC03: General ray tangent to the cylinder body, ray head after the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(mantleTangentPoint.add(bodyDirection), bodyDirection)), ERROR_INTERSECTION_GENERAL_TANGENT);

        // TC04: General ray tangent at the bottom rim, ray head before the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRimTangentPoint.add(bottomRimDirection.scale(-1)), bottomRimDirection)), ERROR_INTERSECTION_GENERAL_TANGENT);
        // TC05: General ray tangent at the bottom rim, ray head on the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRimTangentPoint, bottomRimDirection)), ERROR_INTERSECTION_GENERAL_TANGENT);
        // TC06: General ray tangent at the bottom rim, ray head after the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(bottomRimTangentPoint.add(bottomRimDirection), bottomRimDirection)), ERROR_INTERSECTION_GENERAL_TANGENT);

        // =============== Boundary Values Tests ==================

        // TC07: General ray tangent at the top rim, ray head before the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRimTangentPoint.add(topRimDirection.scale(-1)), topRimDirection)), ERROR_INTERSECTION_GENERAL_TANGENT);
        // TC08: General ray tangent at the top rim, ray head on the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRimTangentPoint, topRimDirection)), ERROR_INTERSECTION_GENERAL_TANGENT);
        // TC09: General ray tangent at the top rim, ray head after the tangent point (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(topRimTangentPoint.add(topRimDirection), topRimDirection)), ERROR_INTERSECTION_GENERAL_TANGENT);
    }

    /**
     * Group 9: general ray that does not intersect the cylinder.
     * Three miss families are tested:
     * (1) a regular complete miss,
     * (2) a ray that would intersect the infinite tube twice off axis,
     * (3) a ray that would intersect the infinite tube through the axis.
     * In all three cases, the finite cylinder is missed completely.
     */
    @Test
    void testFindIntersectionsGroup9GeneralMiss() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Regular general miss - the ray misses both the infinite tube and the finite cylinder (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(new Point(10, 15, 0), new Vector(1, -1, 0.5))), ERROR_INTERSECTION_GENERAL_MISS);

        // TC02: The ray would intersect the infinite tube twice off axis, but both intersections lie outside the finite cylinder (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray(AXIS_MID.add(CAP_PERPENDICULAR.scale(2)).add(INTERSECTION_AXIS_DIRECTION.scale(8)), new Vector(1, -1, 0.5))), ERROR_INTERSECTION_GENERAL_MISS);

        // TC03: The ray would intersect the infinite tube through the axis, but both intersections lie outside the finite cylinder (0 points)
        assertNull(INTERSECTION_CYLINDER.findIntersections(new Ray((AXIS_MID.add(CAP_PERPENDICULAR.scale(2)).add(INTERSECTION_AXIS_DIRECTION.scale(-8))), new Vector(1, -1, 0.5))), ERROR_INTERSECTION_GENERAL_MISS);
    }
}

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
 * Unit tests for class {@link Sphere}.
 * The tests verify:
 * <ul>
 * <li>{@link Sphere#Sphere(Point, double)}</li>
 * <li>{@link Sphere#getNormal(Point)}</li>
 * <li>{@link Sphere#findIntersections(primitives.Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author Ambash and Elyasaf
 */
class SphereTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    SphereTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Center point used in sphere tests
     */
    private static final Point CENTER = new Point(2, 1, -1);
    /**
     * Point on the sphere surface
     */
    private static final Point POINT1 = new Point(2, 1, 1);
    /**
     * Radius used in sphere tests.
     */
    private static final double RADIUS = 2d;
    /**
     * First intersection point for a ray crossing the sphere off-center.
     */
    private static final Point FIRST_INTERSECTION_POINT = new Point(2 - Math.sqrt(3d), 2, -1);
    /**
     * Second intersection point for a ray crossing the sphere off-center.
     */
    private static final Point SECOND_INTERSECTION_POINT = new Point(2 + Math.sqrt(3d), 2, -1);
    /**
     * First intersection point for a ray passing through the sphere center.
     */
    private static final Point FIRST_CENTER_INTERSECTION_POINT = new Point(0, 1, -1);
    /**
     * Second intersection point for a ray passing through the sphere center.
     */
    private static final Point SECOND_CENTER_INTERSECTION_POINT = new Point(4, 1, -1);
    /**
     * Intersection point for an orthogonal ray that starts inside the sphere.
     */
    private static final Point ORTHOGONAL_INSIDE_INTERSECTION_POINT = new Point(3, 1 + Math.sqrt(3d), -1);
    /**
     * Ray origin for a non-intersecting ray that starts outside the sphere.
     */
    private static final Point OUTSIDE_MISS_ORIGIN = new Point(-1, 4, -1);
    /**
     * Ray origin for an orthogonal ray that starts outside the sphere.
     */
    private static final Point ORTHOGONAL_OUTSIDE_ORIGIN = new Point(-1, 1, -1);
    /**
     * Ray origin for an orthogonal ray that starts inside the sphere.
     */
    private static final Point ORTHOGONAL_INSIDE_ORIGIN = new Point(3, 1, -1);
    /**
     * Ray origin before a tangent point.
     */
    private static final Point TANGENT_BEFORE_ORIGIN = new Point(-1, 3, -1);
    /**
     * Ray origin on a tangent point.
     */
    private static final Point TANGENT_POINT_ORIGIN = new Point(2, 3, -1);
    /**
     * Ray origin after a tangent point.
     */
    private static final Point TANGENT_AFTER_ORIGIN = new Point(4, 3, -1);
    /**
     * Ray origin before the sphere on an off-center crossing line.
     */
    private static final Point OFF_CENTER_BEFORE_ORIGIN = new Point(-1, 2, -1);
    /**
     * Ray origin inside the sphere on an off-center crossing line.
     */
    private static final Point OFF_CENTER_INSIDE_ORIGIN = new Point(2, 2, -1);
    /**
     * Ray origin after the sphere on an off-center crossing line.
     */
    private static final Point OFF_CENTER_AFTER_ORIGIN = new Point(5, 2, -1);
    /**
     * Ray origin before the sphere on a line through the center.
     */
    private static final Point THROUGH_CENTER_BEFORE_ORIGIN = new Point(-1, 1, -1);
    /**
     * Ray origin inside the sphere before the center on a line through the center.
     */
    private static final Point THROUGH_CENTER_INSIDE_BEFORE_ORIGIN = new Point(1, 1, -1);
    /**
     * Ray origin inside the sphere after the center on a line through the center.
     */
    private static final Point THROUGH_CENTER_INSIDE_AFTER_ORIGIN = new Point(3, 1, -1);
    /**
     * Ray origin after the sphere on a line through the center.
     */
    private static final Point THROUGH_CENTER_AFTER_ORIGIN = new Point(5, 1, -1);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for wrong sphere construction
     */
    private static final String ERROR_CONSTRUCTOR = "Failed constructing a correct sphere";
    /**
     * Error message for non-positive sphere radius
     */
    private static final String ERROR_RADIUS = "Constructed a sphere with a non-positive radius";
    /**
     * Error message for wrong getNormal execution
     */
    private static final String ERROR_GET_NORMAL = "getNormal() threw unexpected exception";
    /**
     * Error message for non-unit sphere normal
     */
    private static final String ERROR_NORMAL_LENGTH = "Sphere normal is not a unit vector";
    /**
     * Error message for wrong sphere normal direction
     */
    private static final String ERROR_NORMAL_DIRECTION = "Sphere normal has wrong direction";
    /**
     * Error message for wrong sphere intersection result.
     */
    private static final String ERROR_INTERSECTION_OUTSIDE =
            "Sphere.findIntersections() returned an unexpected result for rays starting outside the sphere";
    /**
     * Error message for orthogonal sphere-ray cases.
     */
    private static final String ERROR_INTERSECTION_ORTHOGONAL =
            "Sphere.findIntersections() returned an unexpected result for orthogonal sphere-ray cases";
    /**
     * Error message for tangent sphere-ray cases.
     */
    private static final String ERROR_INTERSECTION_TANGENT =
            "Sphere.findIntersections() returned an unexpected result for tangent sphere-ray cases";
    /**
     * Error message for sphere-ray cases that pass through the center.
     */
    private static final String ERROR_INTERSECTION_THROUGH_CENTER =
            "Sphere.findIntersections() returned an unexpected result for rays passing through the sphere center";
    /**
     * Error message for sphere-ray cases that start inside the sphere.
     */
    private static final String ERROR_INTERSECTION_INSIDE =
            "Sphere.findIntersections() returned an unexpected result for rays starting inside the sphere";

    /**
     * Test method for {@link Sphere#Sphere(Point, double)}.
     * Verifies correct and incorrect sphere constructions.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct sphere with positive radius
        assertDoesNotThrow(() -> new Sphere(CENTER, RADIUS), ERROR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================

        // TC11: Zero radius
        assertThrows(IllegalArgumentException.class, () -> new Sphere(CENTER, 0d), ERROR_RADIUS);

        // TC12: Negative radius
        assertThrows(IllegalArgumentException.class, () -> new Sphere(CENTER, -RADIUS), ERROR_RADIUS);
    }

    /**
     * Test method for {@link Sphere#getNormal(Point)}.
     * Verifies that the returned normal vector is unit length and points away
     * from the center through the surface point.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        Sphere sphere = new Sphere(CENTER, RADIUS);
        // TC01: A point on the sphere surface
        assertDoesNotThrow(() -> sphere.getNormal(POINT1), ERROR_GET_NORMAL);
        Vector result = sphere.getNormal(POINT1);
        // Ensure |n| = 1
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        // Ensure normal direction is correct
        assertEquals(Vector.AXIS_Z, result, ERROR_NORMAL_DIRECTION);
    }

    /**
     * Test method for {@link Sphere#findIntersections(primitives.Ray)}.
     * Verifies exactly the 18 requested sphere-ray intersection cases.
     */
    @Test
    void testFindIntersections() {
        Sphere sphere = new Sphere(CENTER, RADIUS);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Regular case - ray does not intersect the sphere at all
        assertNull(sphere.findIntersections(new Ray(OUTSIDE_MISS_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_OUTSIDE);

        // TC02: Orthogonal case - ray starts outside the sphere
        assertNull(sphere.findIntersections(new Ray(ORTHOGONAL_OUTSIDE_ORIGIN, Vector.AXIS_Y)),
                ERROR_INTERSECTION_ORTHOGONAL);

        // TC03: Orthogonal case - ray starts inside the sphere
        assertEquals(java.util.List.of(ORTHOGONAL_INSIDE_INTERSECTION_POINT),
                sphere.findIntersections(new Ray(ORTHOGONAL_INSIDE_ORIGIN, Vector.AXIS_Y)),
                ERROR_INTERSECTION_ORTHOGONAL);

        // TC04: Tangent case - ray starts before the tangent point
        assertNull(sphere.findIntersections(new Ray(TANGENT_BEFORE_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_TANGENT);

        // TC05: Tangent case - ray starts on the tangent point
        assertNull(sphere.findIntersections(new Ray(TANGENT_POINT_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_TANGENT);

        // TC06: Tangent case - ray starts after the tangent point
        assertNull(sphere.findIntersections(new Ray(TANGENT_AFTER_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_TANGENT);

        // =============== Boundary Values Tests ==================

        // TC11: Ray crosses the sphere twice, starts before the sphere
        assertEquals(java.util.List.of(FIRST_INTERSECTION_POINT, SECOND_INTERSECTION_POINT),
                sphere.findIntersections(new Ray(OFF_CENTER_BEFORE_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_OUTSIDE);

        // TC12: Ray crosses the sphere twice, starts on the first intersection point
        assertEquals(java.util.List.of(SECOND_INTERSECTION_POINT),
                sphere.findIntersections(new Ray(FIRST_INTERSECTION_POINT, Vector.AXIS_X)),
                ERROR_INTERSECTION_OUTSIDE);

        // TC13: Ray crosses the sphere twice, starts inside the sphere
        assertEquals(java.util.List.of(SECOND_INTERSECTION_POINT),
                sphere.findIntersections(new Ray(OFF_CENTER_INSIDE_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_INSIDE);

        // TC14: Ray crosses the sphere twice, starts on the second intersection point
        assertNull(sphere.findIntersections(new Ray(SECOND_INTERSECTION_POINT, Vector.AXIS_X)),
                ERROR_INTERSECTION_OUTSIDE);

        // TC15: Ray crosses the sphere twice, starts after the sphere
        assertNull(sphere.findIntersections(new Ray(OFF_CENTER_AFTER_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_OUTSIDE);

        // TC16: Ray crosses through the center, starts before the sphere
        assertEquals(java.util.List.of(FIRST_CENTER_INTERSECTION_POINT, SECOND_CENTER_INTERSECTION_POINT),
                sphere.findIntersections(new Ray(THROUGH_CENTER_BEFORE_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_THROUGH_CENTER);

        // TC17: Ray crosses through the center, starts on the first intersection point
        assertEquals(java.util.List.of(SECOND_CENTER_INTERSECTION_POINT),
                sphere.findIntersections(new Ray(FIRST_CENTER_INTERSECTION_POINT, Vector.AXIS_X)),
                ERROR_INTERSECTION_THROUGH_CENTER);

        // TC18: Ray crosses through the center, starts inside the sphere before the center
        assertEquals(java.util.List.of(SECOND_CENTER_INTERSECTION_POINT),
                sphere.findIntersections(new Ray(THROUGH_CENTER_INSIDE_BEFORE_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_THROUGH_CENTER);

        // TC19: Ray crosses through the center, starts at the center
        assertEquals(java.util.List.of(SECOND_CENTER_INTERSECTION_POINT),
                sphere.findIntersections(new Ray(CENTER, Vector.AXIS_X)),
                ERROR_INTERSECTION_THROUGH_CENTER);

        // TC20: Ray crosses through the center, starts inside the sphere after the center
        assertEquals(java.util.List.of(SECOND_CENTER_INTERSECTION_POINT),
                sphere.findIntersections(new Ray(THROUGH_CENTER_INSIDE_AFTER_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_THROUGH_CENTER);

        // TC21: Ray crosses through the center, starts on the second intersection point
        assertNull(sphere.findIntersections(new Ray(SECOND_CENTER_INTERSECTION_POINT, Vector.AXIS_X)),
                ERROR_INTERSECTION_THROUGH_CENTER);

        // TC22: Ray crosses through the center, starts after the sphere
        assertNull(sphere.findIntersections(new Ray(THROUGH_CENTER_AFTER_ORIGIN, Vector.AXIS_X)),
                ERROR_INTERSECTION_THROUGH_CENTER);
    }
}

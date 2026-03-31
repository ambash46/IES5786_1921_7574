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
 *  * @author Ambash and Elyasaf
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
    private static final String ERROR_INTERSECTION = "Wrong sphere intersection result";

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
        double s = Math.sqrt(3d);

        Point first = new Point(2 - s, 2, -1);
        Point second = new Point(2 + s, 2, -1);
        Point firstCenter = new Point(0, 1, -1);
        Point secondCenter = new Point(4, 1, -1);
        Point orthogonalInside = new Point(3, 1 + s, -1);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Regular case - ray does not intersect the sphere at all
        assertNull(sphere.findIntersections(new Ray(new Point(-1, 4, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC02: Orthogonal case - ray starts outside the sphere
        assertNull(sphere.findIntersections(new Ray(new Point(-1, 1, -1), Vector.AXIS_Y)),
                ERROR_INTERSECTION);

        // TC03: Orthogonal case - ray starts inside the sphere
        assertEquals(java.util.List.of(orthogonalInside),
                sphere.findIntersections(new Ray(new Point(3, 1, -1), Vector.AXIS_Y)),
                ERROR_INTERSECTION);

        // TC04: Tangent case - ray starts before the tangent point
        assertNull(sphere.findIntersections(new Ray(new Point(-1, 3, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC05: Tangent case - ray starts on the tangent point
        assertNull(sphere.findIntersections(new Ray(new Point(2, 3, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC06: Tangent case - ray starts after the tangent point
        assertNull(sphere.findIntersections(new Ray(new Point(4, 3, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Ray crosses the sphere twice, starts before the sphere
        assertEquals(java.util.List.of(first, second),
                sphere.findIntersections(new Ray(new Point(-1, 2, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC12: Ray crosses the sphere twice, starts on the first intersection point
        assertEquals(java.util.List.of(second),
                sphere.findIntersections(new Ray(first, Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC13: Ray crosses the sphere twice, starts inside the sphere
        assertEquals(java.util.List.of(second),
                sphere.findIntersections(new Ray(new Point(2, 2, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC14: Ray crosses the sphere twice, starts on the second intersection point
        assertNull(sphere.findIntersections(new Ray(second, Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC15: Ray crosses the sphere twice, starts after the sphere
        assertNull(sphere.findIntersections(new Ray(new Point(5, 2, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC16: Ray crosses through the center, starts before the sphere
        assertEquals(java.util.List.of(firstCenter, secondCenter),
                sphere.findIntersections(new Ray(new Point(-1, 1, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC17: Ray crosses through the center, starts on the first intersection point
        assertEquals(java.util.List.of(secondCenter),
                sphere.findIntersections(new Ray(firstCenter, Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC18: Ray crosses through the center, starts inside the sphere before the center
        assertEquals(java.util.List.of(secondCenter),
                sphere.findIntersections(new Ray(new Point(1, 1, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC19: Ray crosses through the center, starts at the center
        assertEquals(java.util.List.of(secondCenter),
                sphere.findIntersections(new Ray(CENTER, Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC20: Ray crosses through the center, starts inside the sphere after the center
        assertEquals(java.util.List.of(secondCenter),
                sphere.findIntersections(new Ray(new Point(3, 1, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC21: Ray crosses through the center, starts on the second intersection point
        assertNull(sphere.findIntersections(new Ray(secondCenter, Vector.AXIS_X)),
                ERROR_INTERSECTION);

        // TC22: Ray crosses through the center, starts after the sphere
        assertNull(sphere.findIntersections(new Ray(new Point(5, 1, -1), Vector.AXIS_X)),
                ERROR_INTERSECTION);
    }
}

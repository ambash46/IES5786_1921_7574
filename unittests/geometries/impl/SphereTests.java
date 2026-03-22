package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Sphere}.
 * The tests verify:
 * <ul>
 * <li>{@link Sphere#Sphere(Point, double)}</li>
 * <li>{@link Sphere#getNormal(Point)}</li>
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
    private static final Point CENTER = new Point(1, 1, 1);
    /**
     * Point on the sphere surface
     */
    private static final Point POINT1 = new Point(1, 1, 2);

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
     * Test method for {@link Sphere#Sphere(Point, double)}.
     * Verifies correct and incorrect sphere constructions.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct sphere with positive radius
        assertDoesNotThrow(() -> new Sphere(CENTER, 1d), ERROR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================

        // TC11: Zero radius
        assertThrows(IllegalArgumentException.class, () -> new Sphere(CENTER, 0d), ERROR_RADIUS);

        // TC12: Negative radius
        assertThrows(IllegalArgumentException.class, () -> new Sphere(CENTER, -1d), ERROR_RADIUS);
    }

    /**
     * Test method for {@link Sphere#getNormal(Point)}.
     * Verifies that the returned normal vector is unit length and points away
     * from the center through the surface point.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        Sphere sphere = new Sphere(CENTER, 1d);
        // TC01: A point on the sphere surface
        assertDoesNotThrow(() -> sphere.getNormal(POINT1), ERROR_GET_NORMAL);
        Vector result = sphere.getNormal(POINT1);
        // Ensure |n| = 1
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        // Ensure normal direction is correct
        assertEquals(Vector.AXIS_Z, result, ERROR_NORMAL_DIRECTION);
    }
}

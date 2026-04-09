package geometries.impl;

import java.util.List;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for class {@link Geometries}.
 * The tests verify:
 * <ul>
 * <li>{@link Geometries#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class GeometriesTests {
    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    GeometriesTests() { /* to satisfy JavaDoc generator */ }

    /**
     * A sphere centered at (1,0,0) with radius 1, tangent to the YZ plane.
     */
    private static final Sphere SPHERE = new Sphere(new Point(1, 0, 0), 1d);
    /**
     * A triangle in the XY plane around the origin.
     */
    private static final Triangle TRIANGLE = new Triangle(
            new Point(-1, -1, 0),
            new Point(3, -1, 0),
            new Point(1, 3, 0));
    /**
     * A plane z = -1 (below the XY plane).
     */
    private static final Plane PLANE = new Plane(
            new Point(1, 1, -1),
            new Vector(0, 0, 1));

    /**
     * Test method for {@link Geometries#findIntersections(Ray)}.
     * Verifies correct intersection counts for all EP and BVA cases.
     */
    @Test
    void testFindIntersections() {

        // =============== Boundary Values Tests ==================

        // TC11: Empty collection — no geometries at all (null)
        assertNull(new Geometries().findIntersections(new Ray(new Point(1, 0, 2), new Vector(0, 0, -1))),
                "Empty collection should return null");

        // TC12: No geometry is hit (null)
        assertNull(new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(new Ray(new Point(10, 10, 10), new Vector(0, 0, 1))),
                "Ray that misses all geometries should return null");

        // TC13: Only one geometry is hit — horizontal ray at z=0.5 hits only the sphere (2 points)
        assertEquals(2, new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(new Ray(new Point(-2, 0, 0.5), new Vector(1, 0, 0))).size(),
                "Ray that hits only the sphere should return 2 points");

        // TC14: All geometries are hit (sum of all intersections)
        assertEquals(4, new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(new Ray(new Point(1, 0, 2), new Vector(0, 0, -1))).size(),
                "Ray that hits sphere (2) + triangle (1) + plane (1) should return 4 points");

        // ============ Equivalence Partitions Tests ==============

        // TC01: Some (but not all) geometries are hit — ray misses sphere, hits triangle and plane (2 points)
        assertEquals(2, new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(new Ray(new Point(0, -0.5, -2), new Vector(0, 0, 1))).size(),
                "Ray that hits triangle (1) + plane (1) but misses sphere should return 2 points");
    }
}

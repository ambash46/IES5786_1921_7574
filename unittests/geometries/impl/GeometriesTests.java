package geometries.impl;

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
 * <li>{@link Geometries#Geometries(geometries.api.Intersectable...)}</li>
 * <li>{@link Geometries#add(geometries.api.Intersectable...)}</li>
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
     * A ray through the center of the scene that hits all three geometries:
     * sphere (2 points), triangle (1 point), plane (1 point) = 4 total.
     */
    private static final Ray RAY_ALL = new Ray(new Point(1, 0, 2), new Vector(0, 0, -1));

    /**
     * Error message for wrong constructor result.
     */
    private static final String ERROR_CONSTRUCTOR =
            "Geometries constructor did not register the expected geometries";
    /**
     * Error message for wrong add result.
     */
    private static final String ERROR_ADD =
            "Geometries.add() did not register the expected geometries";

    /**
     * Test method for {@link Geometries#Geometries(geometries.api.Intersectable...)}.
     * Verifies that geometries passed to the constructor are all registered in the
     * collection and become searchable via {@link Geometries#findIntersections(Ray)}.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Constructor with multiple geometries — all are registered (4 intersections)
        assertEquals(4,
                new Geometries(SPHERE, TRIANGLE, PLANE).findIntersections(RAY_ALL).size(),
                ERROR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================

        // TC11: Empty constructor — collection is empty, findIntersections returns null
        assertNull(new Geometries().findIntersections(RAY_ALL), ERROR_CONSTRUCTOR);

        // TC12: Constructor with a single geometry — that geometry is registered (2 intersections)
        assertEquals(2,
                new Geometries(SPHERE).findIntersections(RAY_ALL).size(),
                ERROR_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Geometries#add(geometries.api.Intersectable...)}.
     * Verifies that {@code add} correctly appends geometries to the collection,
     * that adding multiple geometries at once works, that repeated calls accumulate,
     * and that calling {@code add} with no arguments leaves the collection unchanged.
     */
    @Test
    void testAdd() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Add a single geometry to an empty collection (2 intersections)
        Geometries g1 = new Geometries();
        g1.add(SPHERE);
        assertEquals(2, g1.findIntersections(RAY_ALL).size(), ERROR_ADD);

        // TC02: Add multiple geometries at once (4 intersections)
        Geometries g2 = new Geometries();
        g2.add(SPHERE, TRIANGLE, PLANE);
        assertEquals(4, g2.findIntersections(RAY_ALL).size(), ERROR_ADD);

        // TC03: Add geometries one by one to a non-empty collection — results accumulate (4 intersections)
        Geometries g3 = new Geometries(SPHERE);
        g3.add(TRIANGLE);
        g3.add(PLANE);
        assertEquals(4, g3.findIntersections(RAY_ALL).size(), ERROR_ADD);

        // =============== Boundary Values Tests ==================

        // TC11: Add zero geometries — collection is unchanged (2 intersections, same as before)
        Geometries g4 = new Geometries(SPHERE);
        g4.add();
        assertEquals(2, g4.findIntersections(RAY_ALL).size(), ERROR_ADD);
    }

    /**
     * Test method for {@link Geometries#findIntersections(Ray)}.
     * Verifies correct intersection counts for all EP and BVA cases.
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Some (but not all) geometries are hit — ray misses sphere, hits triangle and plane (2 points)
        assertEquals(2, new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(new Ray(new Point(0, -0.5, -2), new Vector(0, 0, 1))).size(),
                "Ray that hits triangle (1) + plane (1) but misses sphere should return 2 points");

        // TC02: Only one geometry is hit — horizontal ray at z=0.5 hits only the sphere (2 points)
        assertEquals(2, new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(new Ray(new Point(-2, 0, 0.5), new Vector(1, 0, 0))).size(),
                "Ray that hits only the sphere should return 2 points");

        // TC03: All geometries are hit (sum of all intersections)
        assertEquals(4, new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(RAY_ALL).size(),
                "Ray that hits sphere (2) + triangle (1) + plane (1) should return 4 points");

        // =============== Boundary Values Tests ==================

        // TC11: Empty collection — no geometries at all (null)
        assertNull(new Geometries().findIntersections(new Ray(new Point(1, 0, 2), new Vector(0, 0, -1))),
                "Empty collection should return null");

        // TC12: No geometry is hit (null)
        assertNull(new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(new Ray(new Point(10, 10, 10), new Vector(0, 0, 1))),
                "Ray that misses all geometries should return null");
    }
}

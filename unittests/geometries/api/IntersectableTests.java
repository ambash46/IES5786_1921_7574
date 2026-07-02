package geometries.api;

import static geometries.api.Intersectable.Intersection;

import geometries.impl.Plane;
import geometries.impl.Sphere;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link Intersectable}, including its nested {@link Intersection}
 * plain-data class.
 * The tests verify:
 * <ul>
 * <li>{@link Intersection#Intersection(Geometry, Point)}</li>
 * <li>{@link Intersection#equals(Object)}</li>
 * <li>{@link Intersection#toString()}</li>
 * <li>{@link Intersectable#calcIntersections(primitives.Ray, double)}
 *     (bounding-box pre-check correctness)</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class IntersectableTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    IntersectableTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Geometry used to build intersections in these tests.
     */
    private static final Sphere SPHERE = new Sphere(new Point(0, 0, 0), 1d);
    /**
     * A second, independent instance with the same value as {@link #SPHERE}
     * (used to test that {@link Intersection#equals(Object)} compares the
     * geometry by reference, not by value).
     */
    private static final Sphere SPHERE_SAME_VALUE = new Sphere(new Point(0, 0, 0), 1d);
    /**
     * Point used to build intersections in these tests.
     */
    private static final Point POINT = new Point(1, 0, 0);

    /**
     * Error message for wrong equals() result.
     */
    private static final String EQUALS_ERROR = "Intersection.equals() returned an unexpected result";
    /**
     * Error message for wrong hashCode() behavior.
     */
    private static final String HASHCODE_ERROR = "Intersection.hashCode() should be equal for equal objects";

    /**
     * Test method for {@link Intersection#Intersection(Geometry, Point)}.
     * Verifies that the material is taken from the geometry, or defaults to a
     * new {@link primitives.Material} when the geometry is {@code null}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: A real geometry — material is taken from it
        Intersection withGeometry = new Intersection(SPHERE, POINT);
        assertSame(SPHERE.getMaterial(), withGeometry.material,
                "Intersection should take its material from the geometry");
        assertSame(SPHERE, withGeometry.geometry, "Intersection should store the given geometry");
        assertSame(POINT, withGeometry.point, "Intersection should store the given point");

        // =============== Boundary Values Tests ==================
        // TC11: A null geometry — a default material is used instead of throwing
        Intersection withoutGeometry = new Intersection(null, POINT);
        assertNotNull(withoutGeometry.material, "Intersection with a null geometry should still have a material");
    }

    /**
     * Test method for {@link Intersection#equals(Object)} and {@link Intersection#hashCode()}.
     */
    @Test
    void testEqualsAndHashCode() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Same geometry reference and an equal (but independent) point
        Intersection i1 = new Intersection(SPHERE, POINT);
        Intersection i2 = new Intersection(SPHERE, new Point(1, 0, 0));
        assertEquals(i1, i2, EQUALS_ERROR);
        assertEquals(i1.hashCode(), i2.hashCode(), HASHCODE_ERROR);
        // TC02: Different points on the same geometry
        assertNotEquals(i1, new Intersection(SPHERE, new Point(-1, 0, 0)), EQUALS_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Same point, but a *different geometry instance* that is equal
        // by value (Sphere.equals()) — Intersection compares geometry by
        // reference (==), so these must NOT be equal even though the spheres
        // themselves are.
        assertEquals(SPHERE, SPHERE_SAME_VALUE, "Test fixture sanity check: the two spheres should be value-equal");
        Intersection i3 = new Intersection(SPHERE_SAME_VALUE, POINT);
        assertNotEquals(i1, i3, "Intersection.equals() should compare geometry by reference, not by value");
        // TC12: Both geometries null — equal, and hashCode must not throw (identityHashCode(null) is safe)
        Intersection iNull1 = new Intersection(null, POINT);
        Intersection iNull2 = new Intersection(null, new Point(1, 0, 0));
        assertEquals(iNull1, iNull2, EQUALS_ERROR);
        assertEquals(iNull1.hashCode(), iNull2.hashCode(), HASHCODE_ERROR);
        // TC13: An intersection equals itself
        assertEquals(i1, i1, EQUALS_ERROR);
        // TC14: Not equal to null / a different type
        assertNotEquals(i1, null, EQUALS_ERROR);
        assertNotEquals(i1, "not an Intersection", EQUALS_ERROR);
    }

    /**
     * Test method for {@link Intersection#toString()}.
     */
    @Test
    void testToString() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: String representation mentions both the geometry and the point
        String result = new Intersection(SPHERE, POINT).toString();
        assertTrue(result.contains(POINT.toString()), "Intersection.toString() should include the point");
    }

    /**
     * Test method for {@link Intersectable#calcIntersections(Ray, double)}.
     * Verifies that the (always-active) bounding-box pre-check does not
     * change correctness: real hits are still found, and misses are still
     * rejected, for both a finite geometry (with a real box) and an infinite
     * one (null box, which always falls through to the exact calculation).
     */
    @Test
    void testBoundingBoxPreCheck() {
        Sphere sphere = new Sphere(new Point(0, 0, 0), 1d);
        Ray hitRay = new Ray(new Point(-5, 0, 0), Vector.AXIS_X);
        Ray missRay = new Ray(new Point(-5, 10, 0), Vector.AXIS_X);

        // ============ Equivalence Partitions Tests ==============
        // TC01: A finite geometry — the box pre-check must not reject a real hit
        assertNotNull(sphere.calcIntersections(hitRay), "A hitting ray should find intersections");
        // TC02: A finite geometry — a ray missing the box must find nothing
        assertNull(sphere.calcIntersections(missRay), "A missing ray should find no intersections");

        // =============== Boundary Values Tests ==================
        // TC11: An infinite geometry (null bounding box) always falls through to the exact check
        Plane plane = new Plane(Point.ZERO, Vector.AXIS_Z);
        assertNull(plane.getBoundingBox(), "Test fixture sanity check: a plane should be infinite");
        assertNotNull(plane.calcIntersections(new Ray(new Point(0, 0, 5), new Vector(0, 0, -1))),
                "An infinite geometry should still be checked exactly, regardless of its null box");
    }
}

package primitives;

import static geometries.api.Intersectable.Intersection;

import geometries.impl.Sphere;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for class {@link Ray}.
 * The tests verify:
 * <ul>
 * <li>{@link Ray#Ray(Point, Vector)}</li>
 * <li>{@link Ray#Ray(Point, Vector, Vector)}</li>
 * <li>{@link Ray#getPoint(double)}</li>
 * <li>{@link Ray#findClosestPoint(List)}</li>
 * <li>{@link Ray#equals(Object)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class RayTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    RayTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-10;

    /**
     * Error message for wrong ray origin.
     */
    private static final String ORIGIN_ERROR = "Ray constructor stored an unexpected origin";
    /**
     * Error message for wrong ray direction.
     */
    private static final String DIRECTION_VALUE_ERROR =
            "Ray constructor stored the wrong direction vector after normalization";
    /**
     * Error message for wrong ray direction normalization.
     */
    private static final String DIRECTION_NORMALIZATION_ERROR =
            "Ray constructor should normalize the direction vector to unit length";

    /**
     * Error message for wrong getPoint result.
     */
    private static final String GET_POINT_ERROR = "Ray.getPoint() returned the wrong point";

    /**
     * Error message for wrong findClosestPoint result.
     */
    private static final String CLOSEST_POINT_ERROR = "Ray.findClosestPoint() returned the wrong point";

    /**
     * Error message for wrong findClosestIntersection result.
     */
    private static final String CLOSEST_INTERSECTION_ERROR =
            "Ray.findClosestIntersection() returned the wrong intersection";

    /**
     * Error message for a wrong offset-origin ray.
     */
    private static final String OFFSET_ORIGIN_ERROR =
            "Ray(Point, Vector, Vector) constructor produced an unexpected origin offset";

    /**
     * Error message for wrong equals() result.
     */
    private static final String EQUALS_ERROR = "Ray.equals() returned an unexpected result";

    /**
     * Origin point for ray constructor tests.
     */
    private static final Point ORIGIN = new Point(1, 2, 3);

    // --- findClosestPoint test fixtures ---

    /** Ray used in findClosestPoint tests: origin at (0,0,0), direction along +X. */
    private static final Ray   RAY_CLOSEST = new Ray(Point.ZERO, Vector.AXIS_X);

    /** Close point — distance 1 from origin along X. */
    private static final Point P_CLOSE     = new Point(1, 0, 0);

    /** Medium point — distance 3 from origin along X. */
    private static final Point P_MEDIUM    = new Point(3, 0, 0);

    /** Far point — distance 5 from origin along X. */
    private static final Point P_FAR       = new Point(5, 0, 0);

    // --- findClosestIntersection test fixtures ---

    /** Geometry placed at the close point. */
    private static final Sphere SPHERE_CLOSE  = new Sphere(P_CLOSE,  1);
    /** Geometry placed at the medium point. */
    private static final Sphere SPHERE_MEDIUM = new Sphere(P_MEDIUM, 1);
    /** Geometry placed at the far point. */
    private static final Sphere SPHERE_FAR    = new Sphere(P_FAR,    1);

    /** Intersection at the close point. */
    private static final Intersection I_CLOSE  = new Intersection(SPHERE_CLOSE,  P_CLOSE);
    /** Intersection at the medium point. */
    private static final Intersection I_MEDIUM = new Intersection(SPHERE_MEDIUM, P_MEDIUM);
    /** Intersection at the far point. */
    private static final Intersection I_FAR    = new Intersection(SPHERE_FAR,    P_FAR);
    /**
     * Non-unit direction for constructor tests
     */
    private static final Vector DIAGONAL_DIRECTION = new Vector(0, 3, 4);
    /**
     * Ray used in getPoint tests: origin (1,2,3), direction (0,0.6,0.8) (normalized from (0,3,4))
     */
    private static final Ray RAY = new Ray(ORIGIN, DIAGONAL_DIRECTION);

    /**
     * Test method for {@link Ray#Ray(Point, Vector)}.
     */
    @Test
    void testConstructor() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Constructing a ray with a non-unit direction
        Ray ray = assertDoesNotThrow(() -> new Ray(ORIGIN, DIAGONAL_DIRECTION), DIRECTION_VALUE_ERROR);
        assertEquals(ORIGIN, ray.origin(), ORIGIN_ERROR);
        assertEquals(new Vector(0, 0.6, 0.8), ray.direction(), DIRECTION_VALUE_ERROR);
        assertEquals(1d, ray.direction().length(), DELTA, DIRECTION_NORMALIZATION_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Constructing a ray with an already normalized direction
        Ray axisRay = assertDoesNotThrow(() -> new Ray(Point.ZERO, Vector.AXIS_Z), DIRECTION_VALUE_ERROR);
        assertEquals(Point.ZERO, axisRay.origin(), ORIGIN_ERROR);
        assertEquals(Vector.AXIS_Z, axisRay.direction(), DIRECTION_VALUE_ERROR);
        assertEquals(1d, axisRay.direction().length(), DELTA, DIRECTION_NORMALIZATION_ERROR);
    }

    /**
     * Test method for {@link Ray#getPoint(double)}.
     * Verifies that the correct point along the ray is returned for various t values.
     */
    @Test
    void testGetPoint() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Positive t — point in front of the origin (1 + 0*5, 2 + 0.6*5, 3 + 0.8*5) = (1, 5, 7)
        assertEquals(new Point(1, 5, 7), RAY.getPoint(5), GET_POINT_ERROR);

        // TC02: Negative t — point behind the origin (1, 2 - 3, 3 - 4) = (1, -1, -1)
        assertEquals(new Point(1, -1, -1), RAY.getPoint(-5), GET_POINT_ERROR);

        // =============== Boundary Values Tests ==================

        // TC11: t = 0 — should return the origin exactly
        assertEquals(ORIGIN, RAY.getPoint(0), GET_POINT_ERROR);
    }

    /**
     * Test method for {@link Ray#findClosestPoint(List)}.
     * Verifies that the point closest to the ray's origin is returned correctly.
     */
    @Test
    void testFindClosestPoint() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Middle point in the list is the closest
        assertEquals(P_CLOSE, RAY_CLOSEST.findClosestPoint(List.of(P_FAR, P_CLOSE, P_MEDIUM)),
                CLOSEST_POINT_ERROR);

        // =============== Boundary Values Tests ==================

        // TC11: Null list — should return null
        assertNull(RAY_CLOSEST.findClosestPoint(null), CLOSEST_POINT_ERROR);

        // TC12: First point in the list is the closest
        assertEquals(P_CLOSE, RAY_CLOSEST.findClosestPoint(List.of(P_CLOSE, P_MEDIUM, P_FAR)),
                CLOSEST_POINT_ERROR);

        // TC13: Last point in the list is the closest
        assertEquals(P_CLOSE, RAY_CLOSEST.findClosestPoint(List.of(P_MEDIUM, P_FAR, P_CLOSE)),
                CLOSEST_POINT_ERROR);
    }

    /**
     * Test method for {@link Ray#findClosestIntersection(List)}.
     * Verifies that the intersection whose point is closest to the ray's origin
     * is returned, and that the returned geometry object is the exact same
     * reference (checked with {@code assertSame}).
     */
    @Test
    void testFindClosestIntersection() {
        // ============ Equivalence Partitions Tests ==============

        // TC01: Closest intersection is in the middle of the list
        assertSame(SPHERE_CLOSE,
                RAY_CLOSEST.findClosestIntersection(List.of(I_FAR, I_CLOSE, I_MEDIUM)).geometry,
                CLOSEST_INTERSECTION_ERROR);

        // =============== Boundary Values Tests ==================

        // TC11: Null list — should return null
        assertNull(RAY_CLOSEST.findClosestIntersection(null), CLOSEST_INTERSECTION_ERROR);

        // TC12: Closest intersection is first in the list
        assertSame(SPHERE_CLOSE,
                RAY_CLOSEST.findClosestIntersection(List.of(I_CLOSE, I_MEDIUM, I_FAR)).geometry,
                CLOSEST_INTERSECTION_ERROR);

        // TC13: Closest intersection is last in the list
        assertSame(SPHERE_CLOSE,
                RAY_CLOSEST.findClosestIntersection(List.of(I_MEDIUM, I_FAR, I_CLOSE)).geometry,
                CLOSEST_INTERSECTION_ERROR);
    }

    /**
     * Test method for {@link Ray#Ray(Point, Vector, Vector)}.
     * Verifies that the origin is offset by DELTA along the normal, in the
     * direction indicated by the sign of {@code direction·normal}.
     * <p>
     * Asserts directly against {@code Ray.DELTA} (package-private, readable
     * here since this test lives in the same {@code primitives} package)
     * instead of duplicating its value, so the test can't silently drift out
     * of sync if the constant changes.
     */
    @Test
    void testConstructorWithNormal() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: direction·normal > 0 -> origin offset forward along the normal
        Ray outward = assertDoesNotThrow(() -> new Ray(Point.ZERO, Vector.AXIS_Z, Vector.AXIS_Z), OFFSET_ORIGIN_ERROR);
        assertEquals(new Point(0, 0, Ray.DELTA), outward.origin(), OFFSET_ORIGIN_ERROR);
        assertEquals(Vector.AXIS_Z, outward.direction(), DIRECTION_VALUE_ERROR);

        // TC02: direction·normal < 0 -> origin offset backward along the normal
        Ray inward = assertDoesNotThrow(() -> new Ray(Point.ZERO, new Vector(0, 0, -1), Vector.AXIS_Z), OFFSET_ORIGIN_ERROR);
        assertEquals(new Point(0, 0, -Ray.DELTA), inward.origin(), OFFSET_ORIGIN_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: direction is perpendicular to normal (dot == 0) -> treated as the
        // non-positive case, offset backward along the normal
        Ray grazing = assertDoesNotThrow(() -> new Ray(Point.ZERO, Vector.AXIS_X, Vector.AXIS_Z), OFFSET_ORIGIN_ERROR);
        assertEquals(new Point(0, 0, -Ray.DELTA), grazing.origin(), OFFSET_ORIGIN_ERROR);
    }

    /**
     * Test method for {@link Ray#equals(Object)}.
     */
    @Test
    void testEquals() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Equal rays constructed independently
        assertEquals(new Ray(ORIGIN, DIAGONAL_DIRECTION), RAY, EQUALS_ERROR);
        // TC02: Rays with different origins are not equal
        assertNotEquals(RAY, new Ray(Point.ZERO, DIAGONAL_DIRECTION), EQUALS_ERROR);
        // TC03: Rays with different directions are not equal
        assertNotEquals(RAY, new Ray(ORIGIN, Vector.AXIS_X), EQUALS_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: Not equal to null / a different type
        assertNotEquals(RAY, null, EQUALS_ERROR);
        assertNotEquals(RAY, "not a Ray", EQUALS_ERROR);
    }
}

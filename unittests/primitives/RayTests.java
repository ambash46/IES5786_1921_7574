package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link Ray}.
 * The tests verify:
 * <ul>
 * <li>{@link Ray#Ray(Point, Vector)}</li>
 * <li>{@link Ray#getPoint(double)}</li>
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
     * Origin point for ray constructor tests
     */
    private static final Point ORIGIN = new Point(1, 2, 3);
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
}

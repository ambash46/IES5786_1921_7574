package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link Ray}.
 * The tests verify:
 * <ul>
 * <li>{@link Ray#Ray(Point, Vector)}</li>
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
     * Origin point for ray constructor tests
     */
    private static final Point ORIGIN = new Point(1, 2, 3);
    /**
     * Non-unit direction for constructor tests
     */
    private static final Vector DIAGONAL_DIRECTION = new Vector(0, 3, 4);

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
}

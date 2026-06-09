package renderer;

import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link PointLight}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link PointLight#getL(Point)}</li>
 * <li>{@link PointLight#getIntensity(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class PointLightTests {

    /** Default constructor to satisfy JavaDoc generator */
    PointLightTests() { /* to satisfy JavaDoc generator */ }

    /** Color intensity used for all tests */
    private static final Color      INTENSITY = new Color(200, 150, 100);
    /** Position of the point light source */
    private static final Point      POSITION  = new Point(0, 0, 10);
    /** The point light under test */
    private static final PointLight LIGHT     = new PointLight(INTENSITY, POSITION);

    /**
     * Test method for {@link PointLight#getL(Point)}.
     * The direction is computed from the light position toward the illuminated point.
     */
    @Test
    void testGetL() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: general point — returns normalized direction from light position to point
        assertEquals(new Vector(0, 0, -1), LIGHT.getL(Point.ZERO),
                "PointLight.getL() should return normalized direction from light to point");

        // =============== Boundary Values Tests ==================

        // TC11: point coincides with the light position — zero vector, must throw
        assertThrows(IllegalArgumentException.class, () -> LIGHT.getL(POSITION),
                "PointLight.getL() should throw when the point coincides with the light position");
    }

    /**
     * Test method for {@link PointLight#getIntensity(Point)}.
     * With default attenuation coefficients (kC=1, kL=0, kQ=0) the intensity
     * at any point equals the source intensity.
     */
    @Test
    void testGetIntensity() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: general point at some distance — default coefficients yield full source intensity
        assertEquals(INTENSITY, LIGHT.getIntensity(Point.ZERO),
                "PointLight.getIntensity() should return source intensity with default coefficients");

        // =============== Boundary Values Tests ==================

        // TC11: point coincides with the light position (d=0) — denominator = kC = 1,
        //       so the returned intensity equals the original source intensity
        assertEquals(INTENSITY, LIGHT.getIntensity(POSITION),
                "PointLight.getIntensity() should return original intensity when point coincides with light position");
    }
}

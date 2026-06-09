package renderer;

import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link SpotLight}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link SpotLight#getL(Point)}</li>
 * <li>{@link SpotLight#getIntensity(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class SpotLightTests {

    /** Default constructor to satisfy JavaDoc generator */
    SpotLightTests() { /* to satisfy JavaDoc generator */ }

    /** Color intensity used for all tests */
    private static final Color     INTENSITY = new Color(200, 150, 100);
    /** Position of the spot light source */
    private static final Point     POSITION  = new Point(0, 0, 10);
    /**
     * Spot light pointing directly toward the origin (down the −Z axis).
     * The stored direction is (0, 0, −1).
     */
    private static final SpotLight LIGHT     = new SpotLight(INTENSITY, POSITION,
                                                              new Vector(0, 0, -1));

    /**
     * Test method for {@link SpotLight#getL(Point)}.
     * The direction from the light to a point is identical to that of a
     * {@link lighting.PointLight}; the cone affects only the intensity.
     */
    @Test
    void testGetL() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: general point — returns normalized direction from light position to point
        assertEquals(new Vector(0, 0, -1), LIGHT.getL(Point.ZERO),
                "SpotLight.getL() should return normalized direction from light to point");

        // =============== Boundary Values Tests ==================

        // TC11: point coincides with the light position — zero vector, must throw
        assertThrows(IllegalArgumentException.class, () -> LIGHT.getL(POSITION),
                "SpotLight.getL() should throw when the point coincides with the light position");
    }

    /**
     * Test method for {@link SpotLight#getIntensity(Point)}.
     * The intensity is scaled by {@code max(0, dir · l)}: full in the beam
     * direction, zero for points at or past 90° to the beam.
     */
    @Test
    void testGetIntensity() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: point in front of the spotlight — cos(0°) = 1, full intensity
        assertEquals(INTENSITY, LIGHT.getIntensity(Point.ZERO),
                "SpotLight.getIntensity() should return full intensity for a point in the beam direction");

        // TC02: point behind the spotlight — dir · l < 0, max(0, negative) = 0, zero intensity
        assertEquals(Color.BLACK, LIGHT.getIntensity(new Point(0, 0, 20)),
                "SpotLight.getIntensity() should return black for a point behind the light");

        // =============== Boundary Values Tests ==================

        // TC11: point at 90° to the beam direction — dir · l = 0, max(0, 0) = 0, zero intensity
        assertEquals(Color.BLACK, LIGHT.getIntensity(new Point(5, 0, 10)),
                "SpotLight.getIntensity() should return black for a point perpendicular to the beam");

        // TC12: point coincides with the light position (d=0) — denominator = kC = 1,
        //       beam factor handled separately, returns original source intensity
        assertEquals(INTENSITY, LIGHT.getIntensity(POSITION),
                "SpotLight.getIntensity() should return original intensity when point coincides with light position");
    }
}

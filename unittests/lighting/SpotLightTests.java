package lighting;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link SpotLight}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link SpotLight#getL(Point)}</li>
 * <li>{@link SpotLight#getIntensity(Point)}</li>
 * <li>{@link SpotLight#setNarrowBeam(int)}</li>
 * <li>{@link SpotLight#setKC(double)} / {@link SpotLight#setKl(double)} / {@link SpotLight#setKq(double)}
 *     (covariant overrides)</li>
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

    /**
     * Test method for {@link SpotLight#setNarrowBeam(int)}.
     * Verifies the beam-narrowing exponent's effect on {@link SpotLight#getIntensity(Point)}
     * and the setter's validation.
     */
    @Test
    void testSetNarrowBeam() {
        // Direction (0,0,-1) from the origin; p=(sqrt(3),0,-1) is at distance 2,
        // giving beam factor dir·L(p) = 0.5 (60° off-axis).
        SpotLight light = new SpotLight(new Color(200, 150, 100), Point.ZERO, new Vector(0, 0, -1));
        Point offAxisPoint = new Point(Math.sqrt(3), 0, -1);

        // ============ Equivalence Partitions Tests ==============

        // TC01: narrowBeam=1 (default) — intensity scaled by factor^1 = 0.5
        assertEquals(new Color(100, 75, 50), light.getIntensity(offAxisPoint),
                "Default narrowBeam should scale intensity by the beam factor to the power of 1");

        // TC02: narrowBeam=2 — intensity scaled by factor^2 = 0.25, same instance returned (chaining)
        assertSame(light, light.setNarrowBeam(2), "setNarrowBeam() should return the same instance for chaining");
        assertEquals(new Color(50, 37.5, 25), light.getIntensity(offAxisPoint),
                "setNarrowBeam(2) should scale intensity by the beam factor squared");

        // =============== Boundary Values Tests ==================

        // TC11: the boundary value 1 is allowed
        assertDoesNotThrow(() -> light.setNarrowBeam(1), "setNarrowBeam(1) should be allowed (boundary is inclusive)");

        // TC12: a value below 1 is rejected
        assertThrows(IllegalArgumentException.class, () -> light.setNarrowBeam(0),
                "setNarrowBeam() should reject a value below 1");
    }

    /**
     * Test method for the covariant {@link SpotLight#setKC(double)}, {@link SpotLight#setKl(double)}
     * and {@link SpotLight#setKq(double)} overrides.
     * Verifies they return a {@code SpotLight} (not just the inherited {@code PointLight})
     * and that the underlying attenuation is applied.
     */
    @Test
    void testCovariantSetters() {
        SpotLight light = new SpotLight(new Color(200, 150, 100), Point.ZERO, new Vector(0, 0, -1));

        // ============ Equivalence Partitions Tests ==============

        // TC01: setKC returns a SpotLight and chains
        SpotLight afterKC = light.setKC(2);
        assertSame(light, afterKC, "SpotLight.setKC() should return the same instance for chaining");

        // TC02: setKl returns a SpotLight and chains
        assertSame(light, light.setKl(0.1), "SpotLight.setKl() should return the same instance for chaining");

        // TC03: setKq returns a SpotLight and chains
        assertSame(light, light.setKq(0.1), "SpotLight.setKq() should return the same instance for chaining");

        // The combined attenuation is reflected in getIntensity() for a point straight ahead
        // (beam factor 1): distance 2, denominator = 2 + 0.1*2 + 0.1*4 = 2.6
        assertEquals(new Color(200 / 2.6, 150 / 2.6, 100 / 2.6), light.getIntensity(new Point(0, 0, -2)),
                "Covariant setters should still apply PointLight's attenuation formula");
    }
}

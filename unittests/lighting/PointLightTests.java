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
 * Unit tests for class {@link PointLight}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link PointLight#getL(Point)}</li>
 * <li>{@link PointLight#getIntensity(Point)}</li>
 * <li>{@link PointLight#getDistance(Point)}</li>
 * <li>{@link PointLight#getPosition()}</li>
 * <li>{@link PointLight#setKC(double)}</li>
 * <li>{@link PointLight#setKl(double)}</li>
 * <li>{@link PointLight#setKq(double)}</li>
 * <li>{@link PointLight#setRadius(double)} / {@link PointLight#getRadius()}</li>
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

    /**
     * Test method for {@link PointLight#getIntensity(Point)} with non-default
     * attenuation coefficients, exercising the full {@code 1/(kC+kL·d+kQ·d²)} formula.
     */
    @Test
    void testGetIntensityAttenuation() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: linear attenuation only — kC=2, kL=1, kQ=0, d=3 -> denominator = 2+3 = 5
        PointLight linear = new PointLight(new Color(200, 150, 100), Point.ZERO).setKC(2).setKl(1);
        assertEquals(new Color(40, 30, 20), linear.getIntensity(new Point(3, 0, 0)),
                "PointLight.getIntensity() did not apply linear attenuation correctly");

        // TC02: quadratic attenuation only — kC=1, kL=0, kQ=1, d=2 -> denominator = 1+4 = 5
        PointLight quadratic = new PointLight(new Color(200, 150, 100), Point.ZERO).setKq(1);
        assertEquals(new Color(40, 30, 20), quadratic.getIntensity(new Point(0, 2, 0)),
                "PointLight.getIntensity() did not apply quadratic attenuation correctly");
    }

    /**
     * Test method for {@link PointLight#getDistance(Point)}.
     */
    @Test
    void testGetDistance() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: distance to a general point
        assertEquals(10d, LIGHT.getDistance(Point.ZERO), 1e-10,
                "PointLight.getDistance() returned an unexpected distance");

        // =============== Boundary Values Tests ==================

        // TC11: distance to the light's own position is zero
        assertEquals(0d, LIGHT.getDistance(POSITION), 1e-10,
                "PointLight.getDistance() should be zero at the light's own position");
    }

    /**
     * Test method for {@link PointLight#getPosition()}.
     */
    @Test
    void testGetPosition() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: returns the exact position passed to the constructor
        assertEquals(POSITION, LIGHT.getPosition(), "PointLight.getPosition() returned an unexpected position");
    }

    /**
     * Test method for {@link PointLight#setKC(double)}.
     */
    @Test
    void testSetKC() {
        PointLight light = new PointLight(INTENSITY, POSITION);

        // ============ Equivalence Partitions Tests ==============

        // TC01: a valid value above the minimum returns the same instance (chaining) and takes effect
        assertSame(light, light.setKC(2), "setKC() should return the same instance for chaining");
        assertEquals(new Color(100, 75, 50), light.getIntensity(POSITION),
                "setKC() did not change the attenuation denominator");

        // =============== Boundary Values Tests ==================

        // TC11: the boundary value 1 is allowed
        assertDoesNotThrow(() -> light.setKC(1), "setKC(1) should be allowed (boundary is inclusive)");

        // TC12: a value below 1 is rejected
        assertThrows(IllegalArgumentException.class, () -> light.setKC(0.999),
                "setKC() should reject a value below 1");
    }

    /**
     * Test method for {@link PointLight#setKl(double)}.
     */
    @Test
    void testSetKl() {
        PointLight light = new PointLight(INTENSITY, POSITION);

        // ============ Equivalence Partitions Tests ==============

        // TC01: a valid positive value returns the same instance (chaining)
        assertSame(light, light.setKl(0.5), "setKl() should return the same instance for chaining");

        // =============== Boundary Values Tests ==================

        // TC11: zero is allowed (the default)
        assertDoesNotThrow(() -> light.setKl(0), "setKl(0) should be allowed (boundary is inclusive)");

        // TC12: a negative value is rejected
        assertThrows(IllegalArgumentException.class, () -> light.setKl(-0.1),
                "setKl() should reject a negative value");
    }

    /**
     * Test method for {@link PointLight#setKq(double)}.
     */
    @Test
    void testSetKq() {
        PointLight light = new PointLight(INTENSITY, POSITION);

        // ============ Equivalence Partitions Tests ==============

        // TC01: a valid positive value returns the same instance (chaining)
        assertSame(light, light.setKq(0.5), "setKq() should return the same instance for chaining");

        // =============== Boundary Values Tests ==================

        // TC11: zero is allowed (the default)
        assertDoesNotThrow(() -> light.setKq(0), "setKq(0) should be allowed (boundary is inclusive)");

        // TC12: a negative value is rejected
        assertThrows(IllegalArgumentException.class, () -> light.setKq(-0.1),
                "setKq() should reject a negative value");
    }

    /**
     * Test method for {@link PointLight#setRadius(double)} and {@link PointLight#getRadius()}.
     */
    @Test
    void testSetAndGetRadius() {
        PointLight light = new PointLight(INTENSITY, POSITION);

        // ============ Equivalence Partitions Tests ==============

        // TC01: a valid positive radius returns the same instance (chaining) and is stored
        assertSame(light, light.setRadius(5), "setRadius() should return the same instance for chaining");
        assertEquals(5d, light.getRadius(), 1e-10, "getRadius() did not return the radius that was set");

        // =============== Boundary Values Tests ==================

        // TC11: zero is allowed (the default — hard shadows)
        assertDoesNotThrow(() -> light.setRadius(0), "setRadius(0) should be allowed (boundary is inclusive)");
        assertEquals(0d, light.getRadius(), 1e-10, "getRadius() should reflect radius 0");

        // TC12: a negative radius is rejected
        assertThrows(IllegalArgumentException.class, () -> light.setRadius(-1),
                "setRadius() should reject a negative value");

        // TC13: a freshly constructed light defaults to radius 0
        assertEquals(0d, new PointLight(INTENSITY, POSITION).getRadius(), 1e-10,
                "A new PointLight should default to radius 0");
    }
}

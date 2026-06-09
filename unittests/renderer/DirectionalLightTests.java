package renderer;

import lighting.DirectionalLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link DirectionalLight}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link DirectionalLight#getL(Point)}</li>
 * <li>{@link DirectionalLight#getIntensity(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * <p>
 * Since both the direction and intensity of a directional light are constant
 * and independent of the target point, one EP case is sufficient for each
 * method (no relevant boundary values exist).
 *
 * @author Ambash and Elyasaf
 */
class DirectionalLightTests {

    /** Default constructor to satisfy JavaDoc generator */
    DirectionalLightTests() { /* to satisfy JavaDoc generator */ }

    /** Color intensity used for all tests */
    private static final Color            INTENSITY  = new Color(200, 150, 100);
    /** Direction of the light (normalized in constructor) */
    private static final Vector           DIRECTION  = new Vector(0, 0, 1);
    /** The directional light under test */
    private static final DirectionalLight LIGHT      = new DirectionalLight(INTENSITY, DIRECTION);

    /**
     * Test method for {@link DirectionalLight#getL(Point)}.
     * A directional light has no position, so the direction toward any point
     * is always the same constant vector.
     */
    @Test
    void testGetL() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: getL returns the same normalized direction regardless of the target point
        assertEquals(new Vector(0, 0, 1), LIGHT.getL(new Point(3, 4, 5)),
                "DirectionalLight.getL() should return constant direction for any point");
    }

    /**
     * Test method for {@link DirectionalLight#getIntensity(Point)}.
     * A directional light does not attenuate with distance, so the intensity
     * at every point equals the original source intensity.
     */
    @Test
    void testGetIntensity() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: intensity does not change with distance — returns full source intensity
        assertEquals(INTENSITY, LIGHT.getIntensity(new Point(100, 200, 300)),
                "DirectionalLight.getIntensity() should return constant intensity for any point");
    }
}

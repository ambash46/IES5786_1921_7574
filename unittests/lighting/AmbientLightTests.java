package lighting;

import org.junit.jupiter.api.Test;
import primitives.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link AmbientLight}.
 * <p>
 * The tests verify:
 * <ul>
 * <li>{@link AmbientLight#AmbientLight(Color)}</li>
 * <li>{@link AmbientLight#getIntensity()} (inherited from {@link Light})</li>
 * <li>{@link AmbientLight#NONE}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class AmbientLightTests {

    /** Default constructor to satisfy JavaDoc generator */
    AmbientLightTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Test method for {@link AmbientLight#AmbientLight(Color)} and
     * {@link AmbientLight#getIntensity()}.
     */
    @Test
    void testConstructorAndGetIntensity() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: a regular intensity color is stored and returned as-is
        Color intensity = new Color(20, 30, 40);
        assertEquals(intensity, new AmbientLight(intensity).getIntensity(),
                "AmbientLight.getIntensity() should return the intensity passed to the constructor");

        // =============== Boundary Values Tests ==================

        // TC11: black intensity (no ambient light contribution)
        assertEquals(Color.BLACK, new AmbientLight(Color.BLACK).getIntensity(),
                "AmbientLight constructed with black should report black intensity");
    }

    /**
     * Test method for {@link AmbientLight#NONE}.
     */
    @Test
    void testNone() {

        // =============== Boundary Values Tests ==================

        // TC11: the NONE constant has black (zero) intensity
        assertEquals(Color.BLACK, AmbientLight.NONE.getIntensity(),
                "AmbientLight.NONE should have black intensity");
    }
}

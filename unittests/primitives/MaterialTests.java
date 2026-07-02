package primitives;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Material}.
 * The tests verify:
 * <ul>
 * <li>{@link Material#setKA(Double3)} / {@link Material#setKA(double)}</li>
 * <li>{@link Material#setKD(Double3)} / {@link Material#setKD(double)}</li>
 * <li>{@link Material#setKS(Double3)} / {@link Material#setKS(double)}</li>
 * <li>{@link Material#setKT(Double3)} / {@link Material#setKT(double)}</li>
 * <li>{@link Material#setKR(Double3)} / {@link Material#setKR(double)}</li>
 * <li>{@link Material#setShininess(int)}</li>
 * <li>{@link Material#setKGlossy(double)}</li>
 * <li>{@link Material#setKDiffuseGlass(double)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class MaterialTests {
    /**
     * Default constructor to satisfy JavaDoc generator
     */
    MaterialTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Error message for a setter that did not store the expected value.
     */
    private static final String SET_VALUE_ERROR = "Material setter did not store the expected value";
    /**
     * Error message for a setter that did not return {@code this} for chaining.
     */
    private static final String CHAINING_ERROR = "Material setter should return the same instance for chaining";
    /**
     * Error message for a missing validation exception.
     */
    private static final String VALIDATION_ERROR = "Material setter should reject an out-of-range value";

    /**
     * Test method for {@link Material#setKA(Double3)} and {@link Material#setKA(double)}.
     */
    @Test
    void testSetKA() {
        // ============ Equivalence Partitions Tests ==============
        Material m = new Material();
        // TC01: Setting a valid Double3 value
        assertSame(m, m.setKA(new Double3(0.5, 0.6, 0.7)), CHAINING_ERROR);
        assertEquals(new Double3(0.5, 0.6, 0.7), m.kA, SET_VALUE_ERROR);
        // TC02: Setting a valid scalar value
        assertSame(m, m.setKA(0.4), CHAINING_ERROR);
        assertEquals(new Double3(0.4), m.kA, SET_VALUE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: A component equal to exactly 1 is allowed
        assertDoesNotThrow(() -> m.setKA(1.0), VALIDATION_ERROR);
        assertDoesNotThrow(() -> m.setKA(Double3.ONE), VALIDATION_ERROR);
        // TC12: A scalar greater than 1 is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKA(1.1), VALIDATION_ERROR);
        // TC13: A Double3 with one component greater than 1 is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKA(new Double3(1.1, 0.5, 0.5)), VALIDATION_ERROR);
        assertThrows(IllegalArgumentException.class, () -> m.setKA(new Double3(0.5, 1.1, 0.5)), VALIDATION_ERROR);
        assertThrows(IllegalArgumentException.class, () -> m.setKA(new Double3(0.5, 0.5, 1.1)), VALIDATION_ERROR);
        // TC14: The lower bound (0) is allowed
        assertDoesNotThrow(() -> m.setKA(0.0), VALIDATION_ERROR);
        // TC15: A negative scalar is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKA(-0.1), VALIDATION_ERROR);
        // TC16: A Double3 with one negative component is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKA(new Double3(-0.1, 0.5, 0.5)), VALIDATION_ERROR);
    }

    /**
     * Test method for {@link Material#setKD(Double3)} and {@link Material#setKD(double)}.
     */
    @Test
    void testSetKD() {
        // ============ Equivalence Partitions Tests ==============
        Material m = new Material();
        // TC01: Setting a valid Double3 value
        assertSame(m, m.setKD(new Double3(0.1, 0.2, 0.3)), CHAINING_ERROR);
        assertEquals(new Double3(0.1, 0.2, 0.3), m.kD, SET_VALUE_ERROR);
        // TC02: Setting a valid scalar value
        assertSame(m, m.setKD(0.5), CHAINING_ERROR);
        assertEquals(new Double3(0.5), m.kD, SET_VALUE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: The bounds (0 and 1) are allowed
        assertDoesNotThrow(() -> m.setKD(0.0), VALIDATION_ERROR);
        assertDoesNotThrow(() -> m.setKD(1.0), VALIDATION_ERROR);
        // TC12: A negative value is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKD(-0.1), VALIDATION_ERROR);
        // TC13: A value above 1 is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKD(1.1), VALIDATION_ERROR);
    }

    /**
     * Test method for {@link Material#setKS(Double3)} and {@link Material#setKS(double)}.
     */
    @Test
    void testSetKS() {
        // ============ Equivalence Partitions Tests ==============
        Material m = new Material();
        // TC01: Setting a valid Double3 value
        assertSame(m, m.setKS(new Double3(0.1, 0.2, 0.3)), CHAINING_ERROR);
        assertEquals(new Double3(0.1, 0.2, 0.3), m.kS, SET_VALUE_ERROR);
        // TC02: Setting a valid scalar value
        assertSame(m, m.setKS(0.5), CHAINING_ERROR);
        assertEquals(new Double3(0.5), m.kS, SET_VALUE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: The bounds (0 and 1) are allowed
        assertDoesNotThrow(() -> m.setKS(0.0), VALIDATION_ERROR);
        assertDoesNotThrow(() -> m.setKS(1.0), VALIDATION_ERROR);
        // TC12: A negative value is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKS(-0.1), VALIDATION_ERROR);
        // TC13: A value above 1 is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKS(1.1), VALIDATION_ERROR);
    }

    /**
     * Test method for {@link Material#setKT(Double3)} and {@link Material#setKT(double)}.
     */
    @Test
    void testSetKT() {
        // ============ Equivalence Partitions Tests ==============
        Material m = new Material();
        // TC01: Setting a valid Double3 value
        assertSame(m, m.setKT(new Double3(0.1, 0.2, 0.3)), CHAINING_ERROR);
        assertEquals(new Double3(0.1, 0.2, 0.3), m.kT, SET_VALUE_ERROR);
        // TC02: Setting a valid scalar value
        assertSame(m, m.setKT(0.5), CHAINING_ERROR);
        assertEquals(new Double3(0.5), m.kT, SET_VALUE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: The bounds (0 and 1) are allowed
        assertDoesNotThrow(() -> m.setKT(0.0), VALIDATION_ERROR);
        assertDoesNotThrow(() -> m.setKT(1.0), VALIDATION_ERROR);
        // TC12: A negative value is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKT(-0.1), VALIDATION_ERROR);
        // TC13: A value above 1 is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKT(1.1), VALIDATION_ERROR);
    }

    /**
     * Test method for {@link Material#setKR(Double3)} and {@link Material#setKR(double)}.
     */
    @Test
    void testSetKR() {
        // ============ Equivalence Partitions Tests ==============
        Material m = new Material();
        // TC01: Setting a valid Double3 value
        assertSame(m, m.setKR(new Double3(0.1, 0.2, 0.3)), CHAINING_ERROR);
        assertEquals(new Double3(0.1, 0.2, 0.3), m.kR, SET_VALUE_ERROR);
        // TC02: Setting a valid scalar value
        assertSame(m, m.setKR(0.5), CHAINING_ERROR);
        assertEquals(new Double3(0.5), m.kR, SET_VALUE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: The bounds (0 and 1) are allowed
        assertDoesNotThrow(() -> m.setKR(0.0), VALIDATION_ERROR);
        assertDoesNotThrow(() -> m.setKR(1.0), VALIDATION_ERROR);
        // TC12: A negative value is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKR(-0.1), VALIDATION_ERROR);
        // TC13: A value above 1 is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKR(1.1), VALIDATION_ERROR);
    }

    /**
     * Test method for {@link Material#setShininess(int)}.
     */
    @Test
    void testSetShininess() {
        // ============ Equivalence Partitions Tests ==============
        Material m = new Material();
        // TC01: Setting a regular positive shininess value
        assertSame(m, m.setShininess(100), CHAINING_ERROR);
        assertEquals(100, m.nShininess, SET_VALUE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: The lower bound (1) is allowed
        assertDoesNotThrow(() -> m.setShininess(1), VALIDATION_ERROR);
        assertEquals(1, m.nShininess, SET_VALUE_ERROR);

        // TC12: Zero is rejected — an exponent of 0 collapses the specular term to a
        // constant (x^0=1), flooding the surface with flat kS instead of a highlight
        assertThrows(IllegalArgumentException.class, () -> m.setShininess(0), VALIDATION_ERROR);

        // TC13: A negative value is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setShininess(-5), VALIDATION_ERROR);
    }

    /**
     * Test method for {@link Material#setKGlossy(double)}.
     */
    @Test
    void testSetKGlossy() {
        // ============ Equivalence Partitions Tests ==============
        Material m = new Material();
        // TC01: Setting a mid-range valid value
        assertSame(m, m.setKGlossy(0.5), CHAINING_ERROR);
        assertEquals(0.5, m.kGlossy, SET_VALUE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: The lower bound (0) is allowed
        assertDoesNotThrow(() -> m.setKGlossy(0.0), VALIDATION_ERROR);
        // TC12: The upper bound (1) is allowed
        assertDoesNotThrow(() -> m.setKGlossy(1.0), VALIDATION_ERROR);
        // TC13: A negative value is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKGlossy(-0.1), VALIDATION_ERROR);
        // TC14: A value above 1 is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKGlossy(1.1), VALIDATION_ERROR);
    }

    /**
     * Test method for {@link Material#setKDiffuseGlass(double)}.
     */
    @Test
    void testSetKDiffuseGlass() {
        // ============ Equivalence Partitions Tests ==============
        Material m = new Material();
        // TC01: Setting a mid-range valid value
        assertSame(m, m.setKDiffuseGlass(0.5), CHAINING_ERROR);
        assertEquals(0.5, m.kDiffuseGlass, SET_VALUE_ERROR);

        // =============== Boundary Values Tests ==================
        // TC11: The lower bound (0) is allowed
        assertDoesNotThrow(() -> m.setKDiffuseGlass(0.0), VALIDATION_ERROR);
        // TC12: The upper bound (1) is allowed
        assertDoesNotThrow(() -> m.setKDiffuseGlass(1.0), VALIDATION_ERROR);
        // TC13: A negative value is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKDiffuseGlass(-0.1), VALIDATION_ERROR);
        // TC14: A value above 1 is rejected
        assertThrows(IllegalArgumentException.class, () -> m.setKDiffuseGlass(1.1), VALIDATION_ERROR);
    }

    /**
     * Test method verifying the default field values of a freshly constructed {@link Material}.
     */
    @Test
    void testDefaults() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Default construction produces the documented default coefficients
        Material m = new Material();
        assertEquals(Double3.ONE, m.kA, SET_VALUE_ERROR);
        assertEquals(Double3.ZERO, m.kD, SET_VALUE_ERROR);
        assertEquals(Double3.ZERO, m.kS, SET_VALUE_ERROR);
        assertEquals(Double3.ZERO, m.kT, SET_VALUE_ERROR);
        assertEquals(Double3.ZERO, m.kR, SET_VALUE_ERROR);
        assertEquals(1, m.nShininess, SET_VALUE_ERROR);
        assertEquals(0.0, m.kGlossy, SET_VALUE_ERROR);
        assertEquals(0.0, m.kDiffuseGlass, SET_VALUE_ERROR);
    }
}

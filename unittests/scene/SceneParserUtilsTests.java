package scene;

import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link SceneParserUtils}.
 * The tests verify:
 * <ul>
 * <li>{@link SceneParserUtils#parseColor(String)}</li>
 * <li>{@link SceneParserUtils#parsePoint(String)}</li>
 * <li>{@link SceneParserUtils#parseVector(String)}</li>
 * <li>{@link SceneParserUtils#parseRay(String, String)}</li>
 * <li>{@link SceneParserUtils#parseDouble3(String)}</li>
 * <li>{@link SceneParserUtils#parseDoubles(String)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class SceneParserUtilsTests {

    /** Default constructor to satisfy JavaDoc generator */
    SceneParserUtilsTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Test method for {@link SceneParserUtils#parseDoubles(String)}.
     */
    @Test
    void testParseDoubles() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: three space-separated values
        assertArrayEquals(new double[]{1, 2, 3}, SceneParserUtils.parseDoubles("1 2 3"), 1e-10,
                "parseDoubles() did not parse three values correctly");

        // TC02: extra whitespace between values is tolerated
        assertArrayEquals(new double[]{1, 2, 3}, SceneParserUtils.parseDoubles("1   2\t3"), 1e-10,
                "parseDoubles() should tolerate extra whitespace between values");

        // =============== Boundary Values Tests ==================

        // TC11: a single value
        assertArrayEquals(new double[]{5}, SceneParserUtils.parseDoubles("5"), 1e-10,
                "parseDoubles() did not parse a single value correctly");

        // TC12: a malformed value throws
        assertThrows(NumberFormatException.class, () -> SceneParserUtils.parseDoubles("1 x 3"),
                "parseDoubles() should throw for a non-numeric token");
    }

    /**
     * Test method for {@link SceneParserUtils#parseColor(String)}.
     */
    @Test
    void testParseColor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: a regular "r g b" string
        assertEquals(new Color(10, 20, 30), SceneParserUtils.parseColor("10 20 30"),
                "parseColor() returned an unexpected color");
    }

    /**
     * Test method for {@link SceneParserUtils#parsePoint(String)}.
     */
    @Test
    void testParsePoint() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: a regular "x y z" string
        assertEquals(new Point(1, 2, 3), SceneParserUtils.parsePoint("1 2 3"),
                "parsePoint() returned an unexpected point");
    }

    /**
     * Test method for {@link SceneParserUtils#parseVector(String)}.
     */
    @Test
    void testParseVector() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: a regular "x y z" string
        assertEquals(new Vector(1, 0, 0), SceneParserUtils.parseVector("1 0 0"),
                "parseVector() returned an unexpected vector");

        // =============== Boundary Values Tests ==================

        // TC11: a zero vector string is rejected (matches Vector's own constructor contract)
        assertThrows(IllegalArgumentException.class, () -> SceneParserUtils.parseVector("0 0 0"),
                "parseVector() should reject the zero vector");
    }

    /**
     * Test method for {@link SceneParserUtils#parseRay(String, String)}.
     */
    @Test
    void testParseRay() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: a regular origin/direction pair — direction gets normalized by the Ray constructor
        Ray ray = SceneParserUtils.parseRay("1 2 3", "0 0 5");
        assertEquals(new Point(1, 2, 3), ray.origin(), "parseRay() returned an unexpected origin");
        assertEquals(new Vector(0, 0, 1), ray.direction(), "parseRay() should normalize the parsed direction");
    }

    /**
     * Test method for {@link SceneParserUtils#parseDouble3(String)}.
     */
    @Test
    void testParseDouble3() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: three space-separated values
        assertEquals(new Double3(0.2, 0.6, 0.4), SceneParserUtils.parseDouble3("0.2 0.6 0.4"),
                "parseDouble3() did not parse three values correctly");

        // =============== Boundary Values Tests ==================

        // TC11: a single scalar value is broadcast to all three components
        assertEquals(new Double3(0.5), SceneParserUtils.parseDouble3("0.5"),
                "parseDouble3() should broadcast a single scalar to all three components");
    }
}

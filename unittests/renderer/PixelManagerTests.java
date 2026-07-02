package renderer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for class {@link PixelManager}.
 * The tests verify:
 * <ul>
 * <li>{@link PixelManager#nextPixel()}</li>
 * <li>{@link PixelManager#pixelDone()}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class PixelManagerTests {

    /** Default constructor to satisfy JavaDoc generator */
    PixelManagerTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Test method for {@link PixelManager#nextPixel()}.
     * Verifies every pixel is returned exactly once, in row-major order, and
     * that {@code null} is returned once all pixels are exhausted.
     */
    @Test
    void testNextPixel() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: a 2x3 grid (2 rows, 3 columns) yields all 6 pixels in row-major order
        PixelManager pm = new PixelManager(2, 3);
        assertEquals(new PixelManager.Pixel(0, 0), pm.nextPixel(), "First pixel should be (col=0,row=0)");
        assertEquals(new PixelManager.Pixel(1, 0), pm.nextPixel(), "Second pixel should be (col=1,row=0)");
        assertEquals(new PixelManager.Pixel(2, 0), pm.nextPixel(), "Third pixel should be (col=2,row=0)");
        assertEquals(new PixelManager.Pixel(0, 1), pm.nextPixel(), "Fourth pixel should wrap to (col=0,row=1)");
        assertEquals(new PixelManager.Pixel(1, 1), pm.nextPixel(), "Fifth pixel should be (col=1,row=1)");
        assertEquals(new PixelManager.Pixel(2, 1), pm.nextPixel(), "Sixth pixel should be (col=2,row=1)");

        // =============== Boundary Values Tests ==================

        // TC11: once all pixels are exhausted, further calls return null (repeatedly)
        assertNull(pm.nextPixel(), "nextPixel() should return null once all pixels are exhausted");
        assertNull(pm.nextPixel(), "nextPixel() should keep returning null after exhaustion");
    }

    /**
     * Test method for {@link PixelManager#nextPixel()}.
     * A 1x1 grid yields exactly one pixel.
     */
    @Test
    void testNextPixelSinglePixelGrid() {

        // =============== Boundary Values Tests ==================

        // TC11: a single-pixel image
        PixelManager pm = new PixelManager(1, 1);
        assertEquals(new PixelManager.Pixel(0, 0), pm.nextPixel(), "The only pixel should be (0,0)");
        assertNull(pm.nextPixel(), "A 1x1 grid should have no pixels left after the first");
    }

    /**
     * Test method for {@link PixelManager#pixelDone()}.
     * Verifies it can be called once per pixel without throwing, for a full grid.
     */
    @Test
    void testPixelDone() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: calling pixelDone() once per pixel of a 2x2 grid should not throw
        PixelManager pm = new PixelManager(2, 2);
        for (int i = 0; i < 4; i++) pm.pixelDone();
    }
}

package renderer;

import org.junit.jupiter.api.Test;

import primitives.Color;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ImageWriter}.
 * The tests verify:
 * <ul>
 * <li>{@link ImageWriter#ImageWriter(int, int)}</li>
 * <li>{@link ImageWriter#writePixel(int, int, Color)}</li>
 * <li>{@link ImageWriter#writeToImage(String)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class ImageWriterTests {

    /** Default constructor — required by documentation tools. */
    ImageWriterTests() { }

    /**
     * Test method for {@link ImageWriter#ImageWriter(int, int)}.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: a regular positive resolution is accepted
        assertDoesNotThrow(() -> new ImageWriter(10, 10), "A positive resolution should be accepted");

        // =============== Boundary Values Tests ==================

        // TC11: a single-pixel image is accepted
        assertDoesNotThrow(() -> new ImageWriter(1, 1), "A 1x1 resolution should be accepted");

        // TC12: zero or negative width/height are rejected
        assertThrows(IllegalArgumentException.class, () -> new ImageWriter(0, 10),
                "Zero width should be rejected");
        assertThrows(IllegalArgumentException.class, () -> new ImageWriter(10, 0),
                "Zero height should be rejected");
        assertThrows(IllegalArgumentException.class, () -> new ImageWriter(-5, 10),
                "Negative width should be rejected");
        assertThrows(IllegalArgumentException.class, () -> new ImageWriter(10, -5),
                "Negative height should be rejected");
    }

    /**
     * Test method for {@link ImageWriter#writePixel(int, int, Color)} and
     * {@link ImageWriter#writeToImage(String)}.
     * Writes a small image with distinct per-pixel colors, then reads the
     * saved PNG back from disk and verifies each pixel round-trips correctly.
     */
    @Test
    void testWritePixelAndWriteToImageRoundTrip() throws IOException {

        // ============ Equivalence Partitions Tests ==============

        // TC01: four distinct colors in a 2x2 image round-trip through the PNG file
        ImageWriter imageWriter = new ImageWriter(2, 2);
        imageWriter.writePixel(0, 0, new Color(255, 0, 0));
        imageWriter.writePixel(1, 0, new Color(0, 255, 0));
        imageWriter.writePixel(0, 1, new Color(0, 0, 255));
        imageWriter.writePixel(1, 1, new Color(255, 255, 255));
        imageWriter.writeToImage("imagewriter_readback_test");

        File file = new File(System.getProperty("user.dir") + "/images/imagewriter_readback_test.png");
        BufferedImage read = ImageIO.read(file);
        assertEquals(2, read.getWidth(), "Written image width should match the requested resolution");
        assertEquals(2, read.getHeight(), "Written image height should match the requested resolution");
        assertEquals(new java.awt.Color(255, 0, 0).getRGB(), read.getRGB(0, 0), "Pixel (0,0) did not round-trip correctly");
        assertEquals(new java.awt.Color(0, 255, 0).getRGB(), read.getRGB(1, 0), "Pixel (1,0) did not round-trip correctly");
        assertEquals(new java.awt.Color(0, 0, 255).getRGB(), read.getRGB(0, 1), "Pixel (0,1) did not round-trip correctly");
        assertEquals(new java.awt.Color(255, 255, 255).getRGB(), read.getRGB(1, 1), "Pixel (1,1) did not round-trip correctly");
    }

    /**
     * Test method for {@link ImageWriter#writePixel(int, int, Color)}.
     * A color component above 255 (a valid light-intensity value in this
     * project's {@link Color}) is clamped to 255 in the written image.
     */
    @Test
    void testWritePixelClampsOverbrightColor() throws IOException {

        // =============== Boundary Values Tests ==================

        // TC11: a component of 500 (over-bright) clamps to 255 in the PNG
        ImageWriter imageWriter = new ImageWriter(1, 1);
        imageWriter.writePixel(0, 0, new Color(500, 0, 0));
        imageWriter.writeToImage("imagewriter_clamp_test");

        File file = new File(System.getProperty("user.dir") + "/images/imagewriter_clamp_test.png");
        BufferedImage read = ImageIO.read(file);
        assertEquals(new java.awt.Color(255, 0, 0).getRGB(), read.getRGB(0, 0),
                "An over-bright color component should clamp to 255, not overflow or wrap");
    }

    /** Horizontal resolution of the test image in pixels. */
    private static final int NX = 800;

    /** Vertical resolution of the test image in pixels. */
    private static final int NY = 500;

    /** Spacing between grid lines in pixels. */
    private static final int GRID_STEP = 50;

    /** Background color filling the image interior. */
    private static final Color BACKGROUND = new Color(255, 255, 0);

    /** Color of the grid lines. */
    private static final Color GRID_COLOR = new Color(255, 0, 0);

    /**
     * Renders a plain yellow background with a red grid and writes the result
     * to the images directory.
     * <p>
     * Expected output: an 800×500 PNG file with a yellow fill and red grid lines
     * every {@value GRID_STEP} pixels, matching the reference image in the
     * assignment specification.
     * </p>
     */
    @Test
    void testImageWriter() {
        ImageWriter imageWriter = new ImageWriter(NX, NY);

        for (int i = 0; i < NY; i++)
            for (int j = 0; j < NX; j++)
                imageWriter.writePixel(j, i, (j % GRID_STEP == 0 || i % GRID_STEP == 0) ? GRID_COLOR : BACKGROUND);

        imageWriter.writeToImage("yellow_grid_test");
    }
}

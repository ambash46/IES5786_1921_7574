package renderer;

import org.junit.jupiter.api.Test;

import primitives.Color;

/**
 * Unit tests for {@link ImageWriter}.
 *
 * @author Ambash and Elyasaf
 */
class ImageWriterTests {

    /** Default constructor — required by documentation tools. */
    ImageWriterTests() { }

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

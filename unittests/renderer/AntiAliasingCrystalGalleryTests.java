package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.XmlSceneLoader;

/**
 * Renders the Crystal Gallery scene (stage8CrystalGallery.xml) to demonstrate
 * anti-aliasing across patterns and resolutions, plus a side-view reference.
 * <ul>
 *   <li>1 side-view reference image</li>
 *   <li>3 baseline images (no AA) at three resolutions</li>
 *   <li>GRID   pattern × 3 sample levels × 3 resolutions = 9 images</li>
 *   <li>RANDOM pattern × 3 sample levels × 3 resolutions = 9 images</li>
 *   <li>JITTER pattern × 3 sample levels × 3 resolutions = 9 images</li>
 * </ul>
 *
 * <p>Sample levels:
 * <ul>
 *   <li><b>s9</b>   — debug quality  (3×3 = 9 rays / pixel)</li>
 *   <li><b>s81</b>  — demo quality   (9×9 = 81 rays / pixel)</li>
 *   <li><b>s289</b> — final quality  (17×17 = 289 rays / pixel)</li>
 * </ul>
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class AntiAliasingCrystalGalleryTests {

    /** Default constructor to satisfy JavaDoc generator. */
    AntiAliasingCrystalGalleryTests() { /* no-op */ }

    /** Scene loaded once; all tests share the same read-only scene instance. */
    private static final Scene SCENE = new XmlSceneLoader().load("stage8CrystalGallery");

    /** Sample-count constants for readability. */
    private static final int S9   = 9;
    private static final int S81  = 81;
    private static final int S289 = 289;

    /** Resolution constants. */
    private static final int LOW  = 250;
    private static final int MID  = 400;
    private static final int HIGH = 500;

    // ══════════════════════════════════════════════════════════════════════════
    //  Side-view reference image
    // ══════════════════════════════════════════════════════════════════════════

    /** Crystal Gallery from the side — rotated 20° for a dramatic angle. */
    @Test
    void crystalGallery_sideView() {
        Camera.getBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620)
                .setVpDistance(1000)
                .setResolution(HIGH, HIGH)
                .build().renderImage().writeToImage("antiAliasing/crystalGallery_sideView");
    }

    // ── helper ────────────────────────────────────────────────────────────────

    /**
     * Builds and renders one image of the Crystal Gallery scene.
     *
     * @param res        pixel resolution (square image)
     * @param numSamples rays per pixel (1 = no AA)
     * @param pattern    sampling pattern, or {@code null} for no AA
     * @param name       output image file name (no extension)
     */
    private void render(int res, int numSamples, SamplingPattern pattern, String name) {
        Camera.getBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620)
                .setVpDistance(1000)
                .setResolution(res, res)
                .setAntiAliasing(numSamples, pattern != null ? pattern : SamplingPatterns.GRID)
                .build().renderImage().writeToImage("antiAliasing/" + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 1 — No Anti-Aliasing  (3 images)
    // ══════════════════════════════════════════════════════════════════════════

    /** Baseline, low resolution. */
    @Test void noAA_low()  { render(LOW,  1, null, "aa_noAA_250"); }

    /** Baseline, medium resolution. */
    @Test void noAA_mid()  { render(MID,  1, null, "aa_noAA_400"); }

    /** Baseline, full resolution (matches original crystalGallery render). */
    @Test void noAA_high() { render(HIGH, 1, null, "aa_noAA_500"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 2 — GRID sampling  (9 images = 3 resolutions × 3 levels)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void grid_s9_low()   { render(LOW,  S9,   SamplingPatterns.GRID, "aa_grid_s9_250");   }
    @Test void grid_s81_low()  { render(LOW,  S81,  SamplingPatterns.GRID, "aa_grid_s81_250");  }
    @Test void grid_s289_low() { render(LOW,  S289, SamplingPatterns.GRID, "aa_grid_s289_250"); }

    @Test void grid_s9_mid()   { render(MID,  S9,   SamplingPatterns.GRID, "aa_grid_s9_400");   }
    @Test void grid_s81_mid()  { render(MID,  S81,  SamplingPatterns.GRID, "aa_grid_s81_400");  }
    @Test void grid_s289_mid() { render(MID,  S289, SamplingPatterns.GRID, "aa_grid_s289_400"); }

    @Test void grid_s9_high()   { render(HIGH, S9,   SamplingPatterns.GRID, "aa_grid_s9_500");   }
    @Test void grid_s81_high()  { render(HIGH, S81,  SamplingPatterns.GRID, "aa_grid_s81_500");  }
    @Test void grid_s289_high() { render(HIGH, S289, SamplingPatterns.GRID, "aa_grid_s289_500"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 3 — RANDOM sampling  (9 images = 3 resolutions × 3 levels)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void random_s9_low()   { render(LOW,  S9,   SamplingPatterns.RANDOM, "aa_random_s9_250");   }
    @Test void random_s81_low()  { render(LOW,  S81,  SamplingPatterns.RANDOM, "aa_random_s81_250");  }
    @Test void random_s289_low() { render(LOW,  S289, SamplingPatterns.RANDOM, "aa_random_s289_250"); }

    @Test void random_s9_mid()   { render(MID,  S9,   SamplingPatterns.RANDOM, "aa_random_s9_400");   }
    @Test void random_s81_mid()  { render(MID,  S81,  SamplingPatterns.RANDOM, "aa_random_s81_400");  }
    @Test void random_s289_mid() { render(MID,  S289, SamplingPatterns.RANDOM, "aa_random_s289_400"); }

    @Test void random_s9_high()   { render(HIGH, S9,   SamplingPatterns.RANDOM, "aa_random_s9_500");   }
    @Test void random_s81_high()  { render(HIGH, S81,  SamplingPatterns.RANDOM, "aa_random_s81_500");  }
    @Test void random_s289_high() { render(HIGH, S289, SamplingPatterns.RANDOM, "aa_random_s289_500"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 4 — JITTERED sampling  (9 images = 3 resolutions × 3 levels)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void jitter_s9_low()   { render(LOW,  S9,    SamplingPatterns.JITTERED, "aa_jitter_s9_250");   }
    @Test void jitter_s81_low()  { render(LOW,  S81,   SamplingPatterns.JITTERED, "aa_jitter_s81_250");  }
    @Test void jitter_s289_low() { render(LOW,  S289,  SamplingPatterns.JITTERED, "aa_jitter_s289_250"); }

    @Test void jitter_s9_mid()   { render(MID,  S9,    SamplingPatterns.JITTERED, "aa_jitter_s9_400");   }
    @Test void jitter_s81_mid()  { render(MID,  S81,   SamplingPatterns.JITTERED, "aa_jitter_s81_400");  }
    @Test void jitter_s289_mid() { render(MID,  S289,  SamplingPatterns.JITTERED, "aa_jitter_s289_400"); }

    @Test void jitter_s9_high()   { render(HIGH, S9,   SamplingPatterns.JITTERED, "aa_jitter_s9_500");   }
    @Test void jitter_s81_high()  { render(HIGH, S81,  SamplingPatterns.JITTERED, "aa_jitter_s81_500");  }
    @Test void jitter_s289_high() { render(HIGH, S289, SamplingPatterns.JITTERED, "aa_jitter_s289_500"); }
}

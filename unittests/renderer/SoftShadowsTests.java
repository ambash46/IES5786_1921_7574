package renderer;

import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.XmlSceneLoader;

/**
 * Compares hard shadows (light radius = 0) against soft shadows at increasing
 * sample levels, using GRID, RANDOM, and JITTERED patterns.
 *
 * <ul>
 *   <li>3 hard-shadow baselines at three resolutions</li>
 *   <li>GRID    pattern × 3 sample levels × 3 resolutions = 9 images</li>
 *   <li>RANDOM  pattern × 3 sample levels × 3 resolutions = 9 images</li>
 *   <li>JITTER  pattern × 3 sample levels × 3 resolutions = 9 images</li>
 * </ul>
 *
 * <p>Scene: the Crystal Gallery XML scene, lit by its existing lights.
 * For soft-shadow tests all PointLight sources receive the configured radius.
 *
 * <p>Images are written to {@code images/softShadows/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class SoftShadowsTests {

    /** Default constructor to satisfy JavaDoc generator. */
    SoftShadowsTests() { /* no-op */ }

    /** Resolution constants. */
    private static final int LOW  = 250;
    private static final int MID  = 400;
    private static final int HIGH = 500;

    /** Sample-count constants for readability. */
    private static final int S9   = 9;
    private static final int S81  = 81;
    private static final int S289 = 289;

    /** Radius applied to every PointLight for the soft-shadow tests. */
    private static final double LIGHT_RADIUS = 60;

    // ── scene factory ──────────────────────────────────────────────────────────

    /**
     * Loads the Crystal Gallery scene and optionally sets a radius on every
     * {@link PointLight} source (including SpotLights, which extend PointLight).
     *
     * @param lightRadius 0 = hard shadows, &gt;0 = area light
     */
    private Scene loadScene(double lightRadius) {
        Scene scene = new XmlSceneLoader().load("stage8CrystalGallery");
        if (lightRadius > 0)
            scene.lights.stream()
                    .filter(l -> l instanceof PointLight)
                    .map(l -> (PointLight) l)
                    .forEach(l -> l.setRadius(lightRadius));
        return scene;
    }

    // ── camera / render helper ─────────────────────────────────────────────────

    private void render(double lightRadius, int numSamples, SamplingPattern pattern,
                        int res, String name) {
        Scene scene = loadScene(lightRadius);
        SimpleRayTracer tracer = new SimpleRayTracer(scene);
        if (lightRadius > 0)
            tracer.setShadowSamples(numSamples, pattern);

        Camera.getBuilder()
                .setRayTracer(tracer)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620)
                .setVpDistance(1000)
                .setResolution(res, res)
                .build().renderImage().writeToImage("softShadows/" + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Reference image
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void softShadows_reference() {
        render(0, 1, SamplingPatterns.GRID, HIGH, "softShadows_reference");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 1 — Hard shadow baseline (radius = 0)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void hard_low()  { render(0, 1, SamplingPatterns.GRID, LOW,  "hard_250"); }
    @Test void hard_mid()  { render(0, 1, SamplingPatterns.GRID, MID,  "hard_400"); }
    @Test void hard_high() { render(0, 1, SamplingPatterns.GRID, HIGH, "hard_500"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 2 — Soft shadows, GRID pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void grid_s9_low()    { render(LIGHT_RADIUS, S9,   SamplingPatterns.GRID, LOW,  "soft_grid_s9_250");   }
    @Test void grid_s81_low()   { render(LIGHT_RADIUS, S81,  SamplingPatterns.GRID, LOW,  "soft_grid_s81_250");  }
    @Test void grid_s289_low()  { render(LIGHT_RADIUS, S289, SamplingPatterns.GRID, LOW,  "soft_grid_s289_250"); }

    @Test void grid_s9_mid()    { render(LIGHT_RADIUS, S9,   SamplingPatterns.GRID, MID,  "soft_grid_s9_400");   }
    @Test void grid_s81_mid()   { render(LIGHT_RADIUS, S81,  SamplingPatterns.GRID, MID,  "soft_grid_s81_400");  }
    @Test void grid_s289_mid()  { render(LIGHT_RADIUS, S289, SamplingPatterns.GRID, MID,  "soft_grid_s289_400"); }

    @Test void grid_s9_high()   { render(LIGHT_RADIUS, S9,   SamplingPatterns.GRID, HIGH, "soft_grid_s9_500");   }
    @Test void grid_s81_high()  { render(LIGHT_RADIUS, S81,  SamplingPatterns.GRID, HIGH, "soft_grid_s81_500");  }
    @Test void grid_s289_high() { render(LIGHT_RADIUS, S289, SamplingPatterns.GRID, HIGH, "soft_grid_s289_500"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 3 — Soft shadows, RANDOM pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void random_s9_low()    { render(LIGHT_RADIUS, S9,   SamplingPatterns.RANDOM, LOW,  "soft_random_s9_250");   }
    @Test void random_s81_low()   { render(LIGHT_RADIUS, S81,  SamplingPatterns.RANDOM, LOW,  "soft_random_s81_250");  }
    @Test void random_s289_low()  { render(LIGHT_RADIUS, S289, SamplingPatterns.RANDOM, LOW,  "soft_random_s289_250"); }

    @Test void random_s9_mid()    { render(LIGHT_RADIUS, S9,   SamplingPatterns.RANDOM, MID,  "soft_random_s9_400");   }
    @Test void random_s81_mid()   { render(LIGHT_RADIUS, S81,  SamplingPatterns.RANDOM, MID,  "soft_random_s81_400");  }
    @Test void random_s289_mid()  { render(LIGHT_RADIUS, S289, SamplingPatterns.RANDOM, MID,  "soft_random_s289_400"); }

    @Test void random_s9_high()   { render(LIGHT_RADIUS, S9,   SamplingPatterns.RANDOM, HIGH, "soft_random_s9_500");   }
    @Test void random_s81_high()  { render(LIGHT_RADIUS, S81,  SamplingPatterns.RANDOM, HIGH, "soft_random_s81_500");  }
    @Test void random_s289_high() { render(LIGHT_RADIUS, S289, SamplingPatterns.RANDOM, HIGH, "soft_random_s289_500"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 4 — Soft shadows, JITTERED pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void jitter_s9_low()    { render(LIGHT_RADIUS, S9,   SamplingPatterns.JITTERED, LOW,  "soft_jitter_s9_250");   }
    @Test void jitter_s81_low()   { render(LIGHT_RADIUS, S81,  SamplingPatterns.JITTERED, LOW,  "soft_jitter_s81_250");  }
    @Test void jitter_s289_low()  { render(LIGHT_RADIUS, S289, SamplingPatterns.JITTERED, LOW,  "soft_jitter_s289_250"); }

    @Test void jitter_s9_mid()    { render(LIGHT_RADIUS, S9,   SamplingPatterns.JITTERED, MID,  "soft_jitter_s9_400");   }
    @Test void jitter_s81_mid()   { render(LIGHT_RADIUS, S81,  SamplingPatterns.JITTERED, MID,  "soft_jitter_s81_400");  }
    @Test void jitter_s289_mid()  { render(LIGHT_RADIUS, S289, SamplingPatterns.JITTERED, MID,  "soft_jitter_s289_400"); }

    @Test void jitter_s9_high()   { render(LIGHT_RADIUS, S9,   SamplingPatterns.JITTERED, HIGH, "soft_jitter_s9_500");   }
    @Test void jitter_s81_high()  { render(LIGHT_RADIUS, S81,  SamplingPatterns.JITTERED, HIGH, "soft_jitter_s81_500");  }
    @Test void jitter_s289_high() { render(LIGHT_RADIUS, S289, SamplingPatterns.JITTERED, HIGH, "soft_jitter_s289_500"); }
}

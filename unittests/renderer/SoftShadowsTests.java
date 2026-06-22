package renderer;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Compares hard shadows (light radius = 0) against soft shadows at increasing
 * sample levels, using GRID and JITTERED patterns.
 *
 * <p>Scene: a large opaque sphere hovering above a flat floor, lit by an area
 * light from above-left. The penumbra is clearly visible at the shadow edge.
 *
 * <p>Images are written to {@code images/softShadows/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class SoftShadowsTests {

    /** Default constructor to satisfy JavaDoc generator. */
    SoftShadowsTests() { /* no-op */ }

    private static final int LOW  = 250;
    private static final int MID  = 400;
    private static final int HIGH = 500;

    private static final int S9  = 9;
    private static final int S25 = 25;
    private static final int S81 = 81;

    /** Radius of the area light — large enough to produce a visible penumbra. */
    private static final double LIGHT_RADIUS = 60;

    // ── scene factory ──────────────────────────────────────────────────────────

    /**
     * Builds the test scene: a large sphere hovering above a flat floor.
     * <ul>
     *   <li>Floor at y = -200 — two large triangles covering the visible area</li>
     *   <li>Sphere at (0, 60, -150), radius 80 — casts a clear shadow downward</li>
     *   <li>PointLight at (-200, 450, -50) — from above-left</li>
     * </ul>
     *
     * @param lightRadius 0 = point light (hard shadow), &gt;0 = area light
     */
    private Scene buildScene(double lightRadius) {
        Scene scene = new Scene("soft-shadow demo")
                .setAmbientLight(new AmbientLight(new Color(20, 20, 20)));

        Material floorMat  = new Material().setKD(0.6).setKS(0.2).setShininess(15);
        Material sphereMat = new Material().setKD(0.5).setKS(0.5).setShininess(80);

        scene.geometries.add(
                // Floor — two triangles covering a wide horizontal area
                new Triangle(
                        new Point(-600, -200, 400),
                        new Point( 600, -200, 400),
                        new Point( 600, -200, -700))
                        .setEmission(new Color(30, 25, 20)).setMaterial(floorMat),
                new Triangle(
                        new Point(-600, -200, 400),
                        new Point(-600, -200, -700),
                        new Point( 600, -200, -700))
                        .setEmission(new Color(30, 25, 20)).setMaterial(floorMat),
                // Sphere — large, opaque, hovering 220 units above floor
                new Sphere(new Point(0, 60, -150), 80d)
                        .setEmission(new Color(15, 15, 80))
                        .setMaterial(sphereMat)
        );

        scene.lights.add(
                new PointLight(new Color(800, 650, 500), new Point(-200, 450, -50))
                        .setKl(0.000005).setKq(0.0000005)
                        .setRadius(lightRadius)
        );

        return scene;
    }

    // ── camera / render helper ─────────────────────────────────────────────────

    /**
     * Renders one image from a camera positioned above the scene, looking
     * slightly downward so both the sphere and its floor shadow are visible.
     *
     * @param lightRadius  0 = hard shadow, &gt;0 = soft shadow
     * @param numSamples   shadow samples (ignored when radius = 0)
     * @param pattern      sampling pattern (ignored when radius = 0)
     * @param res          pixel resolution (square)
     * @param name         output file name without extension
     */
    private void render(double lightRadius, int numSamples, SamplingPattern pattern,
                        int res, String name) {
        var builder = Camera.getBuilder()
                .setRayTracer(buildScene(lightRadius), RayTracerType.SIMPLE)
                .setLocation(new Point(0, 250, 700))
                .setDirection(new Point(0, -100, -200), Vector.AXIS_Y)
                .setVpSize(500, 500)
                .setVpDistance(650)
                .setResolution(res, res);

        if (lightRadius > 0)
            builder.setSoftShadows(numSamples, pattern);

        builder.build().renderImage().writeToImage("softShadows/" + name);
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

    @Test void grid_s9_low()   { render(LIGHT_RADIUS, S9,  SamplingPatterns.GRID, LOW,  "soft_grid_s9_250");  }
    @Test void grid_s9_mid()   { render(LIGHT_RADIUS, S9,  SamplingPatterns.GRID, MID,  "soft_grid_s9_400");  }
    @Test void grid_s9_high()  { render(LIGHT_RADIUS, S9,  SamplingPatterns.GRID, HIGH, "soft_grid_s9_500");  }

    @Test void grid_s25_low()  { render(LIGHT_RADIUS, S25, SamplingPatterns.GRID, LOW,  "soft_grid_s25_250"); }
    @Test void grid_s25_mid()  { render(LIGHT_RADIUS, S25, SamplingPatterns.GRID, MID,  "soft_grid_s25_400"); }
    @Test void grid_s25_high() { render(LIGHT_RADIUS, S25, SamplingPatterns.GRID, HIGH, "soft_grid_s25_500"); }

    @Test void grid_s81_low()  { render(LIGHT_RADIUS, S81, SamplingPatterns.GRID, LOW,  "soft_grid_s81_250"); }
    @Test void grid_s81_mid()  { render(LIGHT_RADIUS, S81, SamplingPatterns.GRID, MID,  "soft_grid_s81_400"); }
    @Test void grid_s81_high() { render(LIGHT_RADIUS, S81, SamplingPatterns.GRID, HIGH, "soft_grid_s81_500"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 3 — Soft shadows, JITTERED pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void jitter_s9_low()   { render(LIGHT_RADIUS, S9,  SamplingPatterns.JITTERED, LOW,  "soft_jitter_s9_250");  }
    @Test void jitter_s9_mid()   { render(LIGHT_RADIUS, S9,  SamplingPatterns.JITTERED, MID,  "soft_jitter_s9_400");  }
    @Test void jitter_s9_high()  { render(LIGHT_RADIUS, S9,  SamplingPatterns.JITTERED, HIGH, "soft_jitter_s9_500");  }

    @Test void jitter_s25_low()  { render(LIGHT_RADIUS, S25, SamplingPatterns.JITTERED, LOW,  "soft_jitter_s25_250"); }
    @Test void jitter_s25_mid()  { render(LIGHT_RADIUS, S25, SamplingPatterns.JITTERED, MID,  "soft_jitter_s25_400"); }
    @Test void jitter_s25_high() { render(LIGHT_RADIUS, S25, SamplingPatterns.JITTERED, HIGH, "soft_jitter_s25_500"); }

    @Test void jitter_s81_low()  { render(LIGHT_RADIUS, S81, SamplingPatterns.JITTERED, LOW,  "soft_jitter_s81_250"); }
    @Test void jitter_s81_mid()  { render(LIGHT_RADIUS, S81, SamplingPatterns.JITTERED, MID,  "soft_jitter_s81_400"); }
    @Test void jitter_s81_high() { render(LIGHT_RADIUS, S81, SamplingPatterns.JITTERED, HIGH, "soft_jitter_s81_500"); }
}

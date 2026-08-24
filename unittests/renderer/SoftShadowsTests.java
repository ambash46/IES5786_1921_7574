package renderer;

import geometries.api.Geometry;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Soft-shadow sweep on a minimal purpose-built scene: one sphere floating
 * above a plane, lit by a single {@link PointLight} whose radius and sample
 * count/pattern control penumbra softness.
 *
 * <p>Anti-aliasing and multithreading stay on at a modest fixed setting
 * throughout (not maxed), so every image also reflects realistic combined
 * usage rather than the feature shown in total isolation.
 *
 * <p>Images are written to {@code images/features/softShadows/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class SoftShadowsTests {

    /** Default constructor to satisfy JavaDoc generator. */
    SoftShadowsTests() { /* no-op */ }

    private static final int HIGH = 600;
    private static final int S9   = 9;
    private static final int S81  = 81;

    /** Radius applied to the light for every soft-shadow test (0 = hard shadow). */
    private static final double LIGHT_RADIUS = 25;

    // ── scene factory ──────────────────────────────────────────────────────────

    private Scene buildScene(double lightRadius) {
        Scene scene = new Scene("Soft Shadows Demo")
                .setAmbientLight(new AmbientLight(new Color(20, 20, 25)))
                .setBackground(new Color(15, 15, 25));

        Geometry ground = new Plane(new Point(0, -10, 0), new Vector(0, 1, 0))
                .setEmission(new Color(30, 30, 35))
                .setMaterial(new Material().setKD(0.6).setKS(0.2).setShininess(30));

        Geometry occluder = new Sphere(new Point(0, 30, -100), 15)
                .setEmission(new Color(90, 40, 40))
                .setMaterial(new Material().setKD(0.5).setKS(0.3).setShininess(50));

        scene.setGeometries(new Geometries(ground, occluder).buildBVH());

        scene.lights.add(new PointLight(new Color(700, 700, 700), new Point(0, 150, -40))
                .setKl(0.0005).setKq(0.0001).setRadius(lightRadius));
        return scene;
    }

    // ── camera / render helper ─────────────────────────────────────────────────

    private void render(double lightRadius, int numSamples, SamplingPattern pattern, String name) {
        SimpleRayTracer tracer = new SimpleRayTracer(buildScene(lightRadius))
                .setShadowSamples(numSamples, pattern);

        Camera.getBuilder()
                .setRayTracer(tracer)
                .setAntiAliasing(S9, SamplingPatterns.GRID)
                .setMultithreadingAuto()
                .setLocation(new Point(0, 60, 150))
                .setDirection(new Point(0, 10, -100), Vector.AXIS_Y)
                .setVpSize(150, 150)
                .setVpDistance(200)
                .setResolution(HIGH, HIGH)
                .build().renderImage().writeToImage("features/softShadows/" + name);
    }

    private void renderAdaptive(int maxDepth, double threshold, String name) {
        SimpleRayTracer tracer = new SimpleRayTracer(buildScene(LIGHT_RADIUS))
                .setAdaptiveShadowSampling(maxDepth, threshold);

        Camera.getBuilder()
                .setRayTracer(tracer)
                .setAntiAliasing(S9, SamplingPatterns.GRID)
                .setMultithreadingAuto()
                .setLocation(new Point(0, 60, 150))
                .setDirection(new Point(0, 10, -100), Vector.AXIS_Y)
                .setVpSize(150, 150)
                .setVpDistance(200)
                .setResolution(HIGH, HIGH)
                .build().renderImage().writeToImage("features/softShadows/" + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Hard shadow baseline (radius = 0)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void hard() { render(0, 1, SamplingPatterns.GRID, "hard"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Soft shadows — GRID pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void grid_s9()  { render(LIGHT_RADIUS, S9,  SamplingPatterns.GRID, "grid_s9");  }
    @Test void grid_s81() { render(LIGHT_RADIUS, S81, SamplingPatterns.GRID, "grid_s81"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Soft shadows — RANDOM pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void random_s9()  { render(LIGHT_RADIUS, S9,  SamplingPatterns.RANDOM, "random_s9");  }
    @Test void random_s81() { render(LIGHT_RADIUS, S81, SamplingPatterns.RANDOM, "random_s81"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Soft shadows — JITTERED pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void jitter_s9()  { render(LIGHT_RADIUS, S9,  SamplingPatterns.JITTERED, "jitter_s9");  }
    @Test void jitter_s81() { render(LIGHT_RADIUS, S81, SamplingPatterns.JITTERED, "jitter_s81"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Soft shadows — Adaptive super-sampling (quadtree, corner-driven)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void adaptive_shallow() { renderAdaptive(2, 10, "adaptive_shallow"); }
    @Test void adaptive_deep()    { renderAdaptive(4, 10, "adaptive_deep");    }
}

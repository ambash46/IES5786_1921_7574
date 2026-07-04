package renderer;

import geometries.api.Geometry;
import geometries.impl.Geometries;
import geometries.impl.Polygon;
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
 * Diffuse-glass (frosted transparency) sweep on a minimal purpose-built
 * scene: one translucent sphere in front of five colored vertical stripes,
 * so the refraction blur is directly visible as the stripes soften.
 *
 * <p>Anti-aliasing, soft shadows, and multithreading stay on at a modest
 * fixed setting throughout (not maxed), so every image also reflects
 * realistic combined usage rather than the feature shown in total isolation.
 *
 * <p>Images are written to {@code images/features/diffuseGlass/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class DiffuseGlassTests {

    /** Default constructor to satisfy JavaDoc generator. */
    DiffuseGlassTests() { /* no-op */ }

    private static final int HIGH = 600;
    private static final int S9   = 9;
    private static final int S81  = 81;

    /** Blur width applied to the glass sphere's material for every test. */
    private static final double K_DIFFUSE_GLASS = 0.35;

    // ── scene factory ──────────────────────────────────────────────────────────

    private static Geometry stripe(double xCenter, Color color) {
        double width = 32, y0 = -40, y1 = 40, z = -150;
        double x0 = xCenter - width / 2, x1 = xCenter + width / 2;
        return new Polygon(
                new Point(x0, y0, z), new Point(x1, y0, z),
                new Point(x1, y1, z), new Point(x0, y1, z))
                .setEmission(color)
                .setMaterial(new Material().setKD(0.7).setKS(0.1).setShininess(20));
    }

    private Scene buildScene() {
        Scene scene = new Scene("Diffuse Glass Demo")
                .setAmbientLight(new AmbientLight(new Color(20, 20, 25)))
                .setBackground(new Color(15, 15, 25));

        Geometries stripes = new Geometries(
                stripe(-80, new Color(150, 30, 30)),
                stripe(-40, new Color(220, 220, 220)),
                stripe(0,   new Color(30, 140, 30)),
                stripe(40,  new Color(220, 220, 220)),
                stripe(80,  new Color(30, 30, 150)));

        Geometry glass = new Sphere(new Point(0, 0, -80), 30)
                .setEmission(new Color(10, 10, 15))
                .setMaterial(new Material().setKD(0.05).setKS(0.2).setKT(0.85)
                        .setKDiffuseGlass(K_DIFFUSE_GLASS).setShininess(200));

        scene.setGeometries(new Geometries(stripes, glass).buildBVH());

        scene.lights.add(new PointLight(new Color(700, 700, 700), new Point(0, 100, 50))
                .setKl(0.0005).setKq(0.0001).setRadius(15));
        return scene;
    }

    // ── camera / render helper ─────────────────────────────────────────────────

    private void render(int numSamples, SamplingPattern pattern, String name) {
        SimpleRayTracer tracer = new SimpleRayTracer(buildScene())
                .setShadowSamples(S9, SamplingPatterns.GRID)
                .setDiffuseSamples(numSamples, pattern);

        Camera.getBuilder()
                .setRayTracer(tracer)
                .setAntiAliasing(S9, SamplingPatterns.GRID)
                .setMultithreadingAuto()
                .setLocation(new Point(0, 20, 120))
                .setDirection(new Point(0, 0, -80), Vector.AXIS_Y)
                .setVpSize(150, 150)
                .setVpDistance(200)
                .setResolution(HIGH, HIGH)
                .build().renderImage().writeToImage("features/diffuseGlass/" + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Sharp refraction baseline (numSamples = 1)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void sharp() { render(1, SamplingPatterns.GRID, "sharp"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Diffuse glass — GRID pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void grid_s9()  { render(S9,  SamplingPatterns.GRID, "grid_s9");  }
    @Test void grid_s81() { render(S81, SamplingPatterns.GRID, "grid_s81"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Diffuse glass — RANDOM pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void random_s9()  { render(S9,  SamplingPatterns.RANDOM, "random_s9");  }
    @Test void random_s81() { render(S81, SamplingPatterns.RANDOM, "random_s81"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Diffuse glass — JITTERED pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void jitter_s9()  { render(S9,  SamplingPatterns.JITTERED, "jitter_s9");  }
    @Test void jitter_s81() { render(S81, SamplingPatterns.JITTERED, "jitter_s81"); }
}

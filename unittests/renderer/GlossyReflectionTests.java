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
 * Glossy-reflection sweep on a minimal purpose-built scene: one large,
 * blurred-mirror sphere facing three small colored spheres, so the blur
 * spread is directly visible in the reflection.
 *
 * <p>Anti-aliasing, soft shadows, and multithreading stay on at a modest
 * fixed setting throughout (not maxed), so every image also reflects
 * realistic combined usage rather than the feature shown in total isolation.
 *
 * <p>Images are written to {@code images/features/glossyReflection/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class GlossyReflectionTests {

    /** Default constructor to satisfy JavaDoc generator. */
    GlossyReflectionTests() { /* no-op */ }

    private static final int HIGH = 600;
    private static final int S9   = 9;
    private static final int S81  = 81;

    /** Blur width applied to the reflective sphere's material for every test. */
    private static final double K_GLOSSY = 0.35;

    // ── scene factory ──────────────────────────────────────────────────────────

    private Scene buildScene() {
        Scene scene = new Scene("Glossy Reflection Demo")
                .setAmbientLight(new AmbientLight(new Color(20, 20, 25)))
                .setBackground(new Color(15, 15, 25));

        Geometry ground = new Plane(new Point(0, -30, 0), new Vector(0, 1, 0))
                .setEmission(new Color(25, 25, 30))
                .setMaterial(new Material().setKD(0.6).setKS(0.2).setShininess(30));

        Geometry mirror = new Sphere(new Point(0, 0, -120), 30)
                .setEmission(new Color(10, 10, 15))
                .setMaterial(new Material().setKD(0.1).setKS(0.4).setKR(0.85)
                        .setKGlossy(K_GLOSSY).setShininess(300));

        Geometry redBall   = new Sphere(new Point(-45, 20, -70), 10)
                .setEmission(new Color(120, 20, 20))
                .setMaterial(new Material().setKD(0.6).setKS(0.3).setShininess(80));
        Geometry greenBall = new Sphere(new Point(45, 20, -70), 10)
                .setEmission(new Color(20, 120, 20))
                .setMaterial(new Material().setKD(0.6).setKS(0.3).setShininess(80));
        Geometry blueBall  = new Sphere(new Point(0, 50, -60), 10)
                .setEmission(new Color(20, 20, 120))
                .setMaterial(new Material().setKD(0.6).setKS(0.3).setShininess(80));

        scene.setGeometries(new Geometries(ground, mirror, redBall, greenBall, blueBall).buildBVH());

        scene.lights.add(new PointLight(new Color(700, 700, 700), new Point(0, 150, 50))
                .setKl(0.0005).setKq(0.0001).setRadius(15));
        return scene;
    }

    // ── camera / render helper ─────────────────────────────────────────────────

    private void render(int numSamples, SamplingPattern pattern, String name) {
        SimpleRayTracer tracer = new SimpleRayTracer(buildScene())
                .setShadowSamples(S9, SamplingPatterns.GRID)
                .setGlossySamples(numSamples, pattern);

        Camera.getBuilder()
                .setRayTracer(tracer)
                .setAntiAliasing(S9, SamplingPatterns.GRID)
                .setMultithreadingAuto()
                .setLocation(new Point(0, 30, 150))
                .setDirection(new Point(0, 0, -120), Vector.AXIS_Y)
                .setVpSize(150, 150)
                .setVpDistance(200)
                .setResolution(HIGH, HIGH)
                .build().renderImage().writeToImage("features/glossyReflection/" + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Sharp reflection baseline (numSamples = 1)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void sharp() { render(1, SamplingPatterns.GRID, "sharp"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Glossy reflection — GRID pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void grid_s9()  { render(S9,  SamplingPatterns.GRID, "grid_s9");  }
    @Test void grid_s81() { render(S81, SamplingPatterns.GRID, "grid_s81"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Glossy reflection — RANDOM pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void random_s9()  { render(S9,  SamplingPatterns.RANDOM, "random_s9");  }
    @Test void random_s81() { render(S81, SamplingPatterns.RANDOM, "random_s81"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Glossy reflection — JITTERED pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void jitter_s9()  { render(S9,  SamplingPatterns.JITTERED, "jitter_s9");  }
    @Test void jitter_s81() { render(S81, SamplingPatterns.JITTERED, "jitter_s81"); }
}

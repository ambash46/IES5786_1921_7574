package renderer;

import geometries.api.Geometry;
import geometries.impl.Geometries;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Anti-aliasing sweep on a minimal purpose-built scene: a checkerboard floor
 * viewed at a raking angle (sharp diagonal grid edges) plus two spheres
 * (sharp curved silhouettes) — both classic aliasing stress patterns.
 *
 * <p>Soft shadows and multithreading stay on at a modest fixed setting
 * throughout (not maxed), so every image also reflects realistic combined
 * usage rather than the feature shown in total isolation.
 *
 * <p>Images are written to {@code images/features/antiAliasing/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class AntiAliasingTests {

    /** Default constructor to satisfy JavaDoc generator. */
    AntiAliasingTests() { /* no-op */ }

    private static final int HIGH = 600;
    private static final int S9   = 9;
    private static final int S81  = 81;

    // ── scene factory ──────────────────────────────────────────────────────────

    /** One checkerboard floor tile spanning [x0,x0+size] x [z0-size,z0]. */
    private static Geometry cell(double x0, double z0, double size, boolean white) {
        double y = -20;
        Color color = white ? new Color(210, 210, 210) : new Color(15, 15, 15);
        return new Polygon(
                new Point(x0, y, z0), new Point(x0 + size, y, z0),
                new Point(x0 + size, y, z0 - size), new Point(x0, y, z0 - size))
                .setEmission(color)
                .setMaterial(new Material().setKD(0.7).setKS(0.1).setShininess(20));
    }

    private Scene buildScene() {
        Scene scene = new Scene("Anti-Aliasing Demo")
                .setAmbientLight(new AmbientLight(new Color(25, 25, 30)))
                .setBackground(new Color(15, 15, 25));

        int    cells    = 6;
        double cellSize = 25;
        Geometries floor = new Geometries();
        for (int i = 0; i < cells; i++)
            for (int j = 0; j < cells; j++)
                floor.add(cell(-75 + i * cellSize, -40 - j * cellSize, cellSize, (i + j) % 2 == 0));

        Geometry redBall  = new Sphere(new Point(-30, 15, -110), 20)
                .setEmission(new Color(150, 30, 30))
                .setMaterial(new Material().setKD(0.6).setKS(0.3).setShininess(80));
        Geometry blueBall = new Sphere(new Point(35, 20, -150), 15)
                .setEmission(new Color(30, 30, 150))
                .setMaterial(new Material().setKD(0.6).setKS(0.3).setShininess(80));

        scene.setGeometries(new Geometries(floor, redBall, blueBall).buildBVH());

        scene.lights.add(new DirectionalLight(new Color(300, 300, 300), new Vector(-1, -2, -1)));
        scene.lights.add(new PointLight(new Color(400, 400, 400), new Point(0, 120, 40))
                .setKl(0.0005).setKq(0.0001).setRadius(15));
        return scene;
    }

    // ── camera / render helper ─────────────────────────────────────────────────

    private void render(int numSamples, SamplingPattern pattern, String name) {
        SimpleRayTracer tracer = new SimpleRayTracer(buildScene())
                .setShadowSamples(S9, SamplingPatterns.GRID);

        Camera.getBuilder()
                .setRayTracer(tracer)
                .setAntiAliasing(numSamples, pattern)
                .setMultithreadingAuto()
                .setLocation(new Point(0, 40, 60))
                .setDirection(new Point(0, -10, -150), Vector.AXIS_Y)
                .setVpSize(150, 150)
                .setVpDistance(200)
                .setResolution(HIGH, HIGH)
                .build().renderImage().writeToImage("features/antiAliasing/" + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  No anti-aliasing baseline (numSamples = 1)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void noAA() { render(1, SamplingPatterns.GRID, "noAA"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Anti-aliasing — GRID pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void grid_s9()  { render(S9,  SamplingPatterns.GRID, "grid_s9");  }
    @Test void grid_s81() { render(S81, SamplingPatterns.GRID, "grid_s81"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Anti-aliasing — RANDOM pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void random_s9()  { render(S9,  SamplingPatterns.RANDOM, "random_s9");  }
    @Test void random_s81() { render(S81, SamplingPatterns.RANDOM, "random_s81"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  Anti-aliasing — JITTERED pattern
    // ══════════════════════════════════════════════════════════════════════════

    @Test void jitter_s9()  { render(S9,  SamplingPatterns.JITTERED, "jitter_s9");  }
    @Test void jitter_s81() { render(S81, SamplingPatterns.JITTERED, "jitter_s81"); }
}

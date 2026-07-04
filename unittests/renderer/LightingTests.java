package renderer;

import geometries.api.Geometry;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Light-source sweep on a minimal purpose-built scene: one sphere, a ground
 * plane, and a triangle panel, lit in turn by each light type alone and then
 * by all of them combined.
 *
 * <p>Anti-aliasing, soft shadows, and multithreading stay on at a modest
 * fixed setting throughout (not maxed), so every image also reflects
 * realistic combined usage rather than the feature shown in total isolation.
 *
 * <p>Images are written to {@code images/features/lighting/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class LightingTests {

    /** Default constructor to satisfy JavaDoc generator. */
    LightingTests() { /* no-op */ }

    private static final int HIGH = 600;
    private static final int S9   = 9;

    /** Position shared by the point/spot lights (top-right of the scene). */
    private static final Point LIGHT_POS = new Point(60, 80, 50);

    /** Sphere center — every light's direction points toward here. */
    private static final Point TARGET = new Point(0, 0, -100);

    // ── scene factory ──────────────────────────────────────────────────────────

    private Scene baseScene(Color ambient) {
        Scene scene = new Scene("Lighting Demo")
                .setAmbientLight(new AmbientLight(ambient))
                .setBackground(new Color(15, 15, 25));

        Geometry sphere = new Sphere(TARGET, 30)
                .setEmission(new Color(20, 20, 30))
                .setMaterial(new Material().setKD(0.5).setKS(0.4).setShininess(100));

        Geometry ground = new Plane(new Point(0, -30, 0), new Vector(0, 1, 0))
                .setEmission(new Color(25, 25, 30))
                .setMaterial(new Material().setKD(0.6).setKS(0.2).setShininess(30));

        Geometry panel = new Triangle(
                new Point(60, -30, -150), new Point(130, -30, -80), new Point(95, 60, -115))
                .setEmission(new Color(30, 20, 20))
                .setMaterial(new Material().setKD(0.5).setKS(0.4).setShininess(100));

        scene.setGeometries(new Geometries(sphere, ground, panel).buildBVH());
        return scene;
    }

    // ── camera / render helper ─────────────────────────────────────────────────

    private void render(Scene scene, String name) {
        SimpleRayTracer tracer = new SimpleRayTracer(scene)
                .setShadowSamples(S9, SamplingPatterns.GRID);

        Camera.getBuilder()
                .setRayTracer(tracer)
                .setAntiAliasing(S9, SamplingPatterns.GRID)
                .setMultithreadingAuto()
                .setLocation(new Point(0, 40, 120))
                .setDirection(TARGET, Vector.AXIS_Y)
                .setVpSize(150, 150)
                .setVpDistance(200)
                .setResolution(HIGH, HIGH)
                .build().renderImage().writeToImage("features/lighting/" + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Single light-source types
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void ambientOnly() {
        render(baseScene(new Color(80, 80, 80)), "ambientOnly");
    }

    @Test
    void directionalOnly() {
        Scene scene = baseScene(new Color(10, 10, 10));
        scene.lights.add(new DirectionalLight(new Color(500, 500, 500), new Vector(-1, -2, -1)));
        render(scene, "directionalOnly");
    }

    @Test
    void pointOnly() {
        Scene scene = baseScene(new Color(10, 10, 10));
        scene.lights.add(new PointLight(new Color(600, 600, 600), LIGHT_POS)
                .setKl(0.0004).setKq(0.00008));
        render(scene, "pointOnly");
    }

    @Test
    void spotOnly() {
        Scene scene = baseScene(new Color(10, 10, 10));
        scene.lights.add(new SpotLight(new Color(700, 700, 700), LIGHT_POS, TARGET.subtract(LIGHT_POS))
                .setKl(0.0004).setKq(0.00008).setNarrowBeam(8));
        render(scene, "spotOnly");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  All light-source types combined
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void allCombined() {
        Scene scene = baseScene(new Color(15, 15, 15));
        scene.lights.add(new DirectionalLight(new Color(200, 200, 200), new Vector(-1, -2, -1)));
        scene.lights.add(new PointLight(new Color(400, 400, 400), LIGHT_POS)
                .setKl(0.0004).setKq(0.00008));
        Point spotPos = new Point(-60, 80, 50);
        scene.lights.add(new SpotLight(new Color(500, 500, 500), spotPos, TARGET.subtract(spotPos))
                .setKl(0.0004).setKq(0.00008).setNarrowBeam(8));
        render(scene, "allCombined");
    }
}

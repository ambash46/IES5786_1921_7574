package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.XmlSceneLoader;

/**
 * Demonstrates glossy reflections and diffuse glass on the Crystal Gallery
 * scene from the side-view angle, across quality levels and coefficient variants.
 *
 * <p>Images are written to {@code images/glossyDiffuse/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class GlossyDiffuseTests {

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    GlossyDiffuseTests() { /* no-op */ }

    private static final Scene SCENE = new XmlSceneLoader().load("stage8CrystalGallery");

    private static final int LOW = 250;
    private static final int MID = 400;
    private static final int HIGH = 500;
    private static final int S9 = 9;
    private static final int S25 = 25;
    private static final int S81 = 81;

    // ── shared camera builder ─────────────────────────────────────────────────

    private Camera.Builder builder(int res) {
        return Camera.getBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620)
                .setVpDistance(1000)
                .setResolution(res, res)
                .setMultithreading(4)
                .setAntiAliasing(25)
                .setSoftShadows(25);

    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Reference — no effects (single ray, existing behavior)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void reference() {
        builder(HIGH).build().renderImage().writeToImage("glossyDiffuse/reference");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 1 — Glossy Reflection only
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void glossy_s9_low() {
        builder(LOW).setGlossyReflection(S9, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/glossy_s9_250");
    }

    @Test
    void glossy_s9_mid() {
        builder(MID).setGlossyReflection(S9, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/glossy_s9_400");
    }

    @Test
    void glossy_s25_mid() {
        builder(MID).setGlossyReflection(S25, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/glossy_s25_400");
    }

    @Test
    void glossy_s81_high() {
        builder(HIGH).setGlossyReflection(S81, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/glossy_s81_500");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 2 — Diffuse Glass only
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void diffuse_s9_low() {
        builder(LOW).setDiffuseGlass(S9, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/diffuse_s9_250");
    }

    @Test
    void diffuse_s9_mid() {
        builder(MID).setDiffuseGlass(S9, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/diffuse_s9_400");
    }

    @Test
    void diffuse_s25_mid() {
        builder(MID).setDiffuseGlass(S25, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/diffuse_s25_400");
    }

    @Test
    void diffuse_s81_high() {
        builder(HIGH).setDiffuseGlass(S81, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/diffuse_s81_500");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 3 — Both effects together
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void both_s9_low() {
        builder(LOW).setGlossyReflection(S9, SamplingPatterns.JITTERED).setDiffuseGlass(S9, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/both_s9_250");
    }

    @Test
    void both_s25_mid() {
        builder(MID).setGlossyReflection(S25, SamplingPatterns.JITTERED).setDiffuseGlass(S25, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/both_s25_400");
    }

    @Test
    void both_s81_high() {
        builder(HIGH).setGlossyReflection(S81, SamplingPatterns.JITTERED).setDiffuseGlass(S81, SamplingPatterns.JITTERED).build().renderImage().writeToImage("glossyDiffuse/both_s81_500");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Premium — high resolution + AA + soft shadows + glossy + diffuse
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void premium() {
        Camera.getBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620)
                .setVpDistance(1000)
                .setResolution(800, 800)
                .setMultithreading(4)
                .setAntiAliasing(S81, SamplingPatterns.GRID)
                .setSoftShadows(S81, SamplingPatterns.GRID)
                .setGlossyReflection(S81, SamplingPatterns.GRID)
                .setDiffuseGlass(S81, SamplingPatterns.GRID)
                .build().renderImage().writeToImage("glossyDiffuse/premium");
    }
}

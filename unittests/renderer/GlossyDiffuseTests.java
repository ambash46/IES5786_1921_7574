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

    // ── shared helpers ────────────────────────────────────────────────────────

    private SimpleRayTracer baseTracer() {
        return new SimpleRayTracer(SCENE)
                .setShadowSamples(25, SamplingPatterns.GRID);
    }

    private Camera.Builder builder(int res, SimpleRayTracer tracer) {
        return Camera.getBuilder()
                .setRayTracer(tracer)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620)
                .setVpDistance(1000)
                .setResolution(res, res)
                .setMultithreading(4)
                .setAntiAliasing(25);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Reference — no effects (single ray, existing behavior)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void reference() {
        builder(HIGH, baseTracer()).build().renderImage().writeToImage("glossyDiffuse/reference");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 1 — Glossy Reflection only
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void glossy_s9_low() {
        builder(LOW, baseTracer().setGlossySamples(S9, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/glossy_s9_250");
    }

    @Test
    void glossy_s9_mid() {
        builder(MID, baseTracer().setGlossySamples(S9, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/glossy_s9_400");
    }

    @Test
    void glossy_s25_mid() {
        builder(MID, baseTracer().setGlossySamples(S25, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/glossy_s25_400");
    }

    @Test
    void glossy_s81_high() {
        builder(HIGH, baseTracer().setGlossySamples(S81, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/glossy_s81_500");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 2 — Diffuse Glass only
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void diffuse_s9_low() {
        builder(LOW, baseTracer().setDiffuseSamples(S9, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/diffuse_s9_250");
    }

    @Test
    void diffuse_s9_mid() {
        builder(MID, baseTracer().setDiffuseSamples(S9, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/diffuse_s9_400");
    }

    @Test
    void diffuse_s25_mid() {
        builder(MID, baseTracer().setDiffuseSamples(S25, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/diffuse_s25_400");
    }

    @Test
    void diffuse_s81_high() {
        builder(HIGH, baseTracer().setDiffuseSamples(S81, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/diffuse_s81_500");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 3 — Both effects together
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void both_s9_low() {
        builder(LOW, baseTracer().setGlossySamples(S9, SamplingPatterns.JITTERED).setDiffuseSamples(S9, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/both_s9_250");
    }

    @Test
    void both_s25_mid() {
        builder(MID, baseTracer().setGlossySamples(S25, SamplingPatterns.JITTERED).setDiffuseSamples(S25, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/both_s25_400");
    }

    @Test
    void both_s81_high() {
        builder(HIGH, baseTracer().setGlossySamples(S81, SamplingPatterns.JITTERED).setDiffuseSamples(S81, SamplingPatterns.JITTERED)).build().renderImage().writeToImage("glossyDiffuse/both_s81_500");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  Premium — high resolution + AA + soft shadows + glossy + diffuse
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void premium() {
        SimpleRayTracer tracer = new SimpleRayTracer(SCENE)
                .setShadowSamples(S81, SamplingPatterns.GRID)
                .setGlossySamples(S81, SamplingPatterns.GRID)
                .setDiffuseSamples(S81, SamplingPatterns.GRID);

        Camera.getBuilder()
                .setRayTracer(tracer)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620)
                .setVpDistance(1000)
                .setResolution(800, 800)
                .setMultithreading(4)
                .setAntiAliasing(S81, SamplingPatterns.GRID)
                .build().renderImage().writeToImage("glossyDiffuse/premium");
    }
}

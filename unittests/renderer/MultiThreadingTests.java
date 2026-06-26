package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.XmlSceneLoader;

/**
 * Compares rendering time across all multi-threading configurations.
 * All tests render the same scene at the same resolution and quality so
 * timings are directly comparable.
 *
 * <p>Approximate render time without threading: ~1 minute.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class MultiThreadingTests {

    /** Default constructor to satisfy JavaDoc generator. */
    MultiThreadingTests() { /* no-op */ }

    /** Shared scene — loaded once so all tests use identical geometry and lights. */
    private static final Scene SCENE = new XmlSceneLoader().load("stage8CrystalGallery");

    private static final int RESOLUTION = 400;
    private static final int SAMPLES    = 25;

    // ── shared camera builder ─────────────────────────────────────────────────

    private Camera.Builder builder() {
        return Camera.getBuilder()
                .setRayTracer(SCENE, RayTracerType.SIMPLE)
                .setAntiAliasing(SAMPLES, SamplingPatterns.GRID)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620)
                .setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 1 — No multi-threading (baseline)
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void singleThread() {
        builder()
                .setMultithreading(1)
                .build().renderImage().writeToImage("multiThreading/singleThread");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 2 — Raw threads
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void rawThreads_2() {
        builder()
                .setMultithreading(2)
                .build().renderImage().writeToImage("multiThreading/rawThreads_2");
    }

    @Test
    void rawThreads_4() {
        builder()
                .setMultithreading(4)
                .build().renderImage().writeToImage("multiThreading/rawThreads_4");
    }

    @Test
    void rawThreads_8() {
        builder()
                .setMultithreading(8)
                .build().renderImage().writeToImage("multiThreading/rawThreads_8");
    }

    @Test
    void rawThreadsAuto() {
        builder()
                .setMultithreadingAuto()
                .build().renderImage().writeToImage("multiThreading/rawThreads_auto");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 3 — Parallel stream
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void parallelStream() {
        builder()
                .setParallelStreaming()
                .build().renderImage().writeToImage("multiThreading/parallelStream");
    }
}

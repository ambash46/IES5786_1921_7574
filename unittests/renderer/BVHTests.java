package renderer;

import geometries.api.AABB;
import geometries.api.Intersectable;
import geometries.impl.Geometries;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.XmlSceneLoader;

import java.util.List;

/**
 * BVH acceleration benchmark, using the Crystal Gallery scene so the
 * comparison is on real authored content rather than a synthetic grid.
 *
 * <p>All 6 tests render the same scene at the same low resolution and
 * quality — only the geometry organisation (flat / manual / auto) and the
 * multi-threading flag differ; the bounding-box pre-check is always active.
 *
 * <p>Images saved to {@code images/acceleration/bvh/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class BVHTests {

    /** Default constructor to satisfy JavaDoc generator. */
    BVHTests() { /* no-op */ }

    private static final int RESOLUTION = 200;
    private static final int SHADOW_S   = 9;
    private static final int STRIPS     = 5;

    // ── shared geometry (built once) ───────────────────────────────────────────
    /** All Crystal Gallery leaf geometries in a single flat Geometries. */
    private static final Geometries FLAT;
    /** Same content organised into Z-depth strips (manual BVH). */
    private static final Geometries MANUAL;

    static {
        FLAT   = new XmlSceneLoader().load("stage8CrystalGallery").geometries.flatten();
        MANUAL = buildManual(FLAT);
    }

    // ── manual hierarchy builder ───────────────────────────────────────────────

    /**
     * Organises the flat scene into Z-depth strips (manual BVH). Infinite
     * objects (no bounding box) stay at the root. Generic over any
     * {@link Geometries} — the Z range is derived from the children's own
     * bounding boxes rather than assumed.
     */
    private static Geometries buildManual(Geometries flat) {
        Geometries[] strip = new Geometries[STRIPS];
        for (int i = 0; i < STRIPS; i++) strip[i] = new Geometries();

        Geometries root = new Geometries();
        List<Intersectable> children = flat.getChildren();

        double zMin = Double.POSITIVE_INFINITY, zMax = Double.NEGATIVE_INFINITY;
        for (Intersectable child : children) {
            AABB box = child.getBoundingBox();
            if (box == null) continue;
            double z = box.midpoint(2);
            zMin = Math.min(zMin, z);
            zMax = Math.max(zMax, z);
        }
        double zRange = Math.max(zMax - zMin, 1e-6);

        for (Intersectable child : children) {
            AABB box = child.getBoundingBox();
            if (box == null) { root.add(child); continue; }
            double z   = box.midpoint(2);
            int    idx = (int) Math.min(STRIPS - 1, Math.max(0, (z - zMin) / zRange * STRIPS));
            strip[idx].add(child);
        }

        for (Geometries s : strip) root.add(s);
        return root;
    }

    // ── camera / render helper ─────────────────────────────────────────────────

    private void render(Geometries geos, boolean mt, String name) {
        Scene scene = new XmlSceneLoader().load("stage8CrystalGallery").setGeometries(geos);
        SimpleRayTracer tracer = new SimpleRayTracer(scene)
                .setShadowSamples(SHADOW_S, SamplingPatterns.GRID);

        var builder = Camera.getBuilder()
                .setRayTracer(tracer)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620)
                .setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION);
        if (mt) builder.setMultithreadingAuto();
        builder.build().renderImage().writeToImage("acceleration/bvh/" + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 1 — Flat scene
    // ══════════════════════════════════════════════════════════════════════════

    @Test void flat_noMT()   { render(FLAT, false, "flat_noMT");   }
    @Test void flat_withMT() { render(FLAT, true,  "flat_withMT"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 2 — Manual hierarchy (Z-depth strips)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void manual_noMT()   { render(MANUAL, false, "manual_noMT");   }
    @Test void manual_withMT() { render(MANUAL, true,  "manual_withMT"); }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 3 — Automatic BVH (median split)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void auto_noMT()   { render(FLAT.buildBVH(), false, "auto_noMT");   }
    @Test void auto_withMT() { render(FLAT.buildBVH(), true,  "auto_withMT"); }
}

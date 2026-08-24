package renderer;

import geometries.api.AABB;
import geometries.api.Intersectable;
import geometries.impl.Geometries;
import geometries.impl.Sphere;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.XmlSceneLoader;

import java.util.List;

/**
 * BVH acceleration benchmark, using the Crystal Gallery scene so the
 * comparison is on real authored content rather than a synthetic grid.
 *
 * <p>The Crystal Gallery XML itself has only ~13 primitives — far too few
 * for linear intersection testing to ever be a bottleneck, so flat vs. BVH
 * shows no measurable difference at that scale. To make the benchmark
 * actually demonstrate BVH's benefit, the scene is padded with a large field
 * of small filler spheres placed well outside the camera's frustum (so the
 * rendered image is pixel-identical to the unpadded gallery) — this gives
 * every ray hundreds of extra intersection candidates to test, which is
 * exactly the regime BVH is meant to accelerate.
 *
 * <p>All 6 tests render the same padded scene at the same low resolution and
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

    /** Side length of the filler-sphere grid (400 extra objects). */
    private static final int    FILLER_GRID    = 20;
    /** Spacing between filler spheres. */
    private static final double FILLER_SPACING = 30;
    /** Y placed well below the visible ground plane, out of camera view. */
    private static final double FILLER_Y       = -300;

    // ── shared geometry (built once) ───────────────────────────────────────────
    /** Crystal Gallery leaf geometries plus filler, in a single flat Geometries. */
    private static final Geometries FLAT;
    /** Same content organised into Z-depth strips (manual BVH). */
    private static final Geometries MANUAL;

    static {
        FLAT   = loadPaddedGallery();
        MANUAL = buildManual(FLAT);
    }

    /**
     * Loads the Crystal Gallery and adds a grid of small, out-of-frame
     * filler spheres so the total object count (~13 -> ~413) is large enough
     * for BVH traversal to actually outperform linear search.
     */
    private static Geometries loadPaddedGallery() {
        Geometries gallery = new XmlSceneLoader().load("stage8CrystalGallery").geometries.flatten();
        Material fillerMat = new Material().setKD(0.5).setKS(0.2).setShininess(30);
        for (int i = 0; i < FILLER_GRID; i++)
            for (int j = 0; j < FILLER_GRID; j++)
                gallery.add(new Sphere(
                        new Point((i - FILLER_GRID / 2.0) * FILLER_SPACING, FILLER_Y,
                                (j - FILLER_GRID / 2.0) * FILLER_SPACING), 1)
                        .setEmission(new Color(10, 10, 10)).setMaterial(fillerMat));
        return gallery;
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

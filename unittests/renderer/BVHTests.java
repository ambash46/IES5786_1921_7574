package renderer;

import geometries.api.Intersectable;
import geometries.impl.Cylinder;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.*;
import scene.Scene;

import java.util.List;

/**
 * BVH acceleration benchmark.
 *
 * <p>All 12 tests render the same 500+ geometry scene at the same resolution
 * and quality. Only the geometry organisation (flat / manual / auto) and the
 * CBR / multi-threading flags differ.
 *
 * <p>Measurement table:
 * <pre>
 *   Configuration             | without MT | with MT
 *   --------------------------+------------+--------
 *   flat,   no CBR            |     1      |   2
 *   manual, no CBR            |     3      |   4
 *   auto,   no CBR            |     5      |   6
 *   flat,   CBR               |     7      |   8
 *   manual, CBR  (manual BVH) |     9      |  10
 *   auto,   CBR  (auto   BVH) |    11      |  12
 * </pre>
 *
 * <p>Images saved to {@code images/bvh/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class BVHTests {

    // ── scene constants ────────────────────────────────────────────────────────
    private static final int    ROWS    = 20;
    private static final int    COLS    = 25;
    private static final double SPACING = 4.0;
    private static final double SPHERE_R = 0.9;
    private static final int    RESOLUTION = 400;
    private static final int    SHADOW_S   = 9;

    // ── shared geometry (built once) ───────────────────────────────────────────
    /** 500 + floor + cylinders in a single flat Geometries. */
    private static final Geometries FLAT;
    /** Same content organised into 5 spatial row-strips (manual BVH). */
    private static final Geometries MANUAL;

    static {
        FLAT   = buildFlat();
        MANUAL = buildManual(FLAT);
    }

    // ── flat builder ───────────────────────────────────────────────────────────

    private static Geometries buildFlat() {
        Geometries g = new Geometries();

        g.add(new Plane(new Point(0, -5, 0), new Vector(0, 1, 0))
                .setMaterial(new Material().setKD(0.5).setKS(0.3).setKR(0.25).setShininess(30))
                .setEmission(new Color(20, 15, 10)));

        Material reflective  = new Material().setKD(0.1).setKS(0.8).setKR(0.7).setShininess(300);
        Material transparent = new Material().setKD(0.05).setKS(0.3).setKT(0.8).setShininess(150);
        Material mRed        = new Material().setKD(0.6).setKS(0.4).setShininess(80);
        Material mBlue       = new Material().setKD(0.6).setKS(0.4).setShininess(80);
        Material mGreen      = new Material().setKD(0.6).setKS(0.4).setShininess(80);

        for (int row = 0; row < ROWS; row++)
            for (int col = 0; col < COLS; col++) {
                double x = (col - COLS / 2.0) * SPACING;
                double z = -(row * SPACING) - 20;
                int    t = (row * COLS + col) % 5;
                Material mat = switch (t) {
                    case 0  -> reflective;
                    case 1  -> transparent;
                    case 2  -> mRed;
                    case 3  -> mBlue;
                    default -> mGreen;
                };
                Color em = switch (t) {
                    case 0  -> new Color(5, 5, 5);
                    case 1  -> new Color(0, 0, 0);
                    case 2  -> new Color(100, 20, 20);
                    case 3  -> new Color(20, 20, 100);
                    default -> new Color(20, 80, 20);
                };
                g.add(new Sphere(new Point(x, 0, z), SPHERE_R).setEmission(em).setMaterial(mat));
            }

        Material pillar = new Material().setKD(0.3).setKS(0.7).setKR(0.3).setShininess(200);
        for (int i = 0; i < 4; i++) {
            double ang = i * Math.PI / 2;
            g.add(new Cylinder(SPHERE_R * 2,
                    new Ray(new Point(Math.cos(ang) * 20, -5, Math.sin(ang) * 20 - 50),
                            new Vector(0, 1, 0)), 15)
                    .setEmission(new Color(30, 30, 50)).setMaterial(pillar));
        }

        return g;
    }

    // ── manual hierarchy builder ───────────────────────────────────────────────

    /**
     * Organises the flat scene into 5 spatial strips along the Z axis.
     * Infinite objects (Plane) stay at the root — they have no bounding box.
     */
    private static Geometries buildManual(Geometries flat) {
        int STRIPS = 5;
        Geometries[] strip = new Geometries[STRIPS];
        for (int i = 0; i < STRIPS; i++) strip[i] = new Geometries();

        Geometries root = new Geometries();
        List<Intersectable> children = flat.getChildren();

        double zMin = -(ROWS * SPACING) - 20;
        double zMax = -20;
        double zRange = zMax - zMin;

        for (Intersectable child : children) {
            geometries.api.AABB box = child.getBoundingBox();
            if (box == null) { root.add(child); continue; }
            double z   = box.midpoint(2);
            int    idx = (int) Math.min(STRIPS - 1, Math.max(0,
                    (z - zMin) / zRange * STRIPS));
            strip[idx].add(child);
        }

        for (Geometries s : strip) root.add(s);
        return root;
    }

    // ── scene / camera helpers ─────────────────────────────────────────────────

    private static Scene buildScene(Geometries geos) {
        Scene scene = new Scene("BVH Benchmark")
                .setAmbientLight(new AmbientLight(new Color(15, 15, 15)));
        scene.lights.add(new DirectionalLight(new Color(80, 80, 120), new Vector(1, -2, -3)));
        scene.lights.add(new PointLight(new Color(500, 400, 300), new Point(0, 50, -50))
                .setKl(0.0001).setKq(0.00001).setRadius(10));
        scene.lights.add(new PointLight(new Color(300, 300, 500), new Point(-50, 30, -80))
                .setKl(0.0001).setKq(0.00001).setRadius(8));
        scene.lights.add(new SpotLight(new Color(400, 350, 250),
                new Point(50, 50, -30), new Vector(-1, -1, -2))
                .setKl(0.0001).setKq(0.00001).setNarrowBeam(3));
        scene.lights.add(new PointLight(new Color(200, 400, 200), new Point(0, 80, -120))
                .setKl(0.0001).setKq(0.00001));
        scene.setGeometries(geos);
        return scene;
    }

    private void render(Geometries geos, boolean cbr, boolean mt, String name) {
        if (cbr) Intersectable.setCBR(2); else Intersectable.disableCBR();
        var builder = Camera.getBuilder()
                .setRayTracer(buildScene(geos), RayTracerType.SIMPLE)
                .setSoftShadows(SHADOW_S, SamplingPatterns.GRID)
                .setLocation(new Point(0, 15, 15))
                .setDirection(new Point(0, 0, -50), Vector.AXIS_Y)
                .setVpSize(100, 100)
                .setVpDistance(80)
                .setResolution(RESOLUTION, RESOLUTION);
        if (mt) builder.setMultithreadingAuto();
        builder.build().renderImage().writeToImage("bvh/" + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 1 — Flat scene
    // ══════════════════════════════════════════════════════════════════════════

    @Test void flat_noCBR_noMT()   { render(FLAT, false, false, "flat_noCBR_noMT");   }
    @Test void flat_noCBR_withMT() { render(FLAT, false, true,  "flat_noCBR_withMT"); }
    @Test void flat_CBR_noMT()     { render(FLAT, true,  false, "flat_CBR_noMT");     }
    @Test void flat_CBR_withMT()   { render(FLAT, true,  true,  "flat_CBR_withMT");   }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 2 — Manual hierarchy (5 Z-strips)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void manual_noCBR_noMT()   { render(MANUAL, false, false, "manual_noCBR_noMT");   }
    @Test void manual_noCBR_withMT() { render(MANUAL, false, true,  "manual_noCBR_withMT"); }
    @Test void manual_CBR_noMT()     { render(MANUAL, true,  false, "manual_CBR_noMT");     }
    @Test void manual_CBR_withMT()   { render(MANUAL, true,  true,  "manual_CBR_withMT");   }

    // ══════════════════════════════════════════════════════════════════════════
    //  GROUP 3 — Automatic BVH (median split)
    // ══════════════════════════════════════════════════════════════════════════

    @Test void auto_noCBR_noMT()   { render(FLAT.buildBVH(), false, false, "auto_noCBR_noMT");   }
    @Test void auto_noCBR_withMT() { render(FLAT.buildBVH(), false, true,  "auto_noCBR_withMT"); }
    @Test void auto_CBR_noMT()     { render(FLAT.buildBVH(), true,  false, "auto_CBR_noMT");     }
    @Test void auto_CBR_withMT()   { render(FLAT.buildBVH(), true,  true,  "auto_CBR_withMT");   }
}

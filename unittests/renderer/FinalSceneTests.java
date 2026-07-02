package renderer;

import geometries.api.Intersectable;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import java.io.File;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.ObjLoader;
import scene.Scene;

/**
 * Desk + PC scene — compares three geometry organisations (the bounding-box
 * pre-check is always active).
 *
 * <pre>
 *   desk_flat   — flat mesh
 *   desk_manual — manual spatial groups
 *   desk_bvh    — automatic BVH
 * </pre>
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class FinalSceneTests {

    /** Default constructor to satisfy JavaDoc generator. */
    FinalSceneTests() { /* no-op */ }

    // ── constants ──────────────────────────────────────────────────────────────
    private static final int    RESOLUTION = 300;
    private static final int    SHADOW_S   = 9;
    private static final double SCALE      = 42.0;
    private static final String MODELS_PATH =
            System.getProperty("user.dir") + "/scenes/models/";
    private static final String DESK_OBJ = "Главный стол+ПК.obj";

    // ── mesh helpers ───────────────────────────────────────────────────────────

    /** Loads the desk OBJ and applies glass material to the desk-top slab only. */
    private static Geometries loadRawDesk() {
        Geometries raw = ObjLoader.load(
                new File(MODELS_PATH + DESK_OBJ), SCALE, 0, 0, 0, 1);

        Material deskTopMat = new Material()
                .setKD(0.10).setKS(0.65).setKT(0.60).setKR(0.14)
                .setKGlossy(0.22).setShininess(900);

        for (Intersectable child : raw.getChildren()) {
            geometries.api.AABB box = child.getBoundingBox();
            if (box == null) continue;
            double yMid  = box.midpoint(1);
            double ySize = box.size(1);
            if (yMid > 22 && yMid < 38 && ySize < 2) {
                primitives.Double3 kd =
                        ((geometries.api.Geometry) child).getMaterial().kD;
                if (kd._d1() > 0.75 && kd._d1() < 0.90)
                    ((geometries.api.Geometry) child).setMaterial(deskTopMat);
            }
        }
        return raw;
    }

    /** Groups the flat desk mesh into bottom (Y < 0) and top (Y ≥ 0) spatial groups. */
    private static Geometries buildManualDesk(Geometries flat) {
        Geometries root   = new Geometries();
        Geometries bottom = new Geometries();
        Geometries top    = new Geometries();

        for (Intersectable child : flat.getChildren()) {
            geometries.api.AABB box = child.getBoundingBox();
            if (box == null) { root.add(child); continue; }
            (box.midpoint(1) < 0 ? bottom : top).add(child);
        }
        root.add(bottom, top);
        return root;
    }

    // ── scene / camera ─────────────────────────────────────────────────────────

    private static Scene buildDeskScene(Geometries mesh) {
        Scene scene = new Scene("Desk Scene")
                .setAmbientLight(new AmbientLight(new Color(55, 52, 48)));

        scene.lights.add(new DirectionalLight(
                new Color(220, 200, 170), new Vector(1, -1, -1)));
        scene.lights.add(new DirectionalLight(
                new Color(120, 130, 180), new Vector(-1, -0.8, -0.5)));
        scene.lights.add(new DirectionalLight(
                new Color(60, 55, 50), new Vector(0, 1, -0.2)));
        scene.lights.add(new PointLight(new Color(280, 260, 230),
                new Point(0, 300, 500))
                .setKl(0.000004).setKq(0.0000004).setRadius(40));
        scene.lights.add(new PointLight(new Color(100, 120, 250),
                new Point(400, 200, 300))
                .setKl(0.000006).setKq(0.0000006).setRadius(25));
        scene.lights.add(new SpotLight(new Color(320, 300, 260),
                new Point(50, 500, 150), new Vector(-0.1, -1, -0.4))
                .setKl(0.00003).setKq(0.000003).setNarrowBeam(3).setRadius(55));

        scene.geometries.add(new Plane(new Point(0, -210, 0), new Vector(0, 1, 0))
                .setEmission(new Color(170, 165, 158))
                .setMaterial(new Material().setKD(0.4).setKS(0.05).setShininess(10)));
        scene.geometries.add(new Plane(new Point(0, 0, -320), new Vector(0, 0, 1))
                .setEmission(new Color(110, 50, 15))
                .setMaterial(new Material().setKD(0.3).setKS(0).setShininess(1)));
        scene.geometries.add(new Plane(new Point(-310, 0, 0), new Vector(1, 0, 0))
                .setEmission(new Color(105, 48, 14))
                .setMaterial(new Material().setKD(0.3).setKS(0).setShininess(1)));
        scene.geometries.add(new Plane(new Point(600, 0, 0), new Vector(-1, 0, 0))
                .setEmission(new Color(105, 48, 14))
                .setMaterial(new Material().setKD(0.3).setKS(0).setShininess(1)));
        scene.geometries.add(new Plane(new Point(0, 500, 0), new Vector(0, -1, 0))
                .setEmission(new Color(170, 165, 158))
                .setMaterial(new Material().setKD(0.4).setKS(0).setShininess(1)));
        scene.geometries.add(mesh);
        return scene;
    }

    private void renderDesk(Geometries mesh, String name) {
        SimpleRayTracer tracer = new SimpleRayTracer(buildDeskScene(mesh))
                .setShadowSamples(SHADOW_S, SamplingPatterns.GRID);

        Camera.getBuilder()
                .setRayTracer(tracer)
                .setLocation(new Point(470, 275, 400))
                .setDirection(new Point(-30, -90, 0), Vector.AXIS_Y)
                .setVpSize(500, 500).setVpDistance(420)
                .setAntiAliasing(9)
                .setResolution(RESOLUTION, RESOLUTION)
                .setMultithreading(6)
                .build().renderImage().writeToImage("finalScene/" + name);
    }

    // ── tests ──────────────────────────────────────────────────────────────────

    /** Flat mesh — baseline. */
    @Test
    void desk_flat() {
        renderDesk(loadRawDesk(), "desk_flat");
    }

    /** Manual spatial groups (bottom / top). */
    @Test
    void desk_manual() {
        renderDesk(buildManualDesk(loadRawDesk()), "desk_manual");
    }

    /** Automatic BVH — fastest configuration. */
    @Test
    void desk_bvh() {
        renderDesk(loadRawDesk().buildBVH(), "desk_bvh");
    }
}

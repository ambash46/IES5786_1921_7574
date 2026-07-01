package renderer;

import geometries.api.Intersectable;
import geometries.impl.Cylinder;
import geometries.impl.Geometries;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import java.io.File;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.ObjLoader;
import scene.Scene;

/**
 * Final BVH benchmark scene — demonstrates all supported geometry types,
 * light types, and effects alongside the female head mesh.
 *
 * <p><b>Scene contents:</b>
 * <ul>
 *   <li>Female head mesh (OBJ, ~thousands of triangles)</li>
 *   <li>Sphere, Triangle, Plane, Cylinder, Tube, Polygon</li>
 *   <li>All light types: Directional, Point (×3), SpotLight</li>
 *   <li>Soft shadows (MP1), reflection, transparency</li>
 * </ul>
 *
 * <p><b>Measurement table (12 configurations):</b>
 * <pre>
 *   Configuration                 | without MT | with MT
 *   ------------------------------+------------+--------
 *   flat,   no CBR                |     1      |   2
 *   manual, no CBR                |     3      |   4
 *   auto,   no CBR                |     5      |   6
 *   flat,   CBR                   |     7      |   8
 *   manual, CBR  (manual BVH)     |     9      |  10
 *   auto,   CBR  (auto BVH)       |    11      |  12
 * </pre>
 *
 * <p>Images saved to {@code images/finalScene/}.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class FinalSceneTests {

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    FinalSceneTests() { /* no-op */ }

    // ── constants ──────────────────────────────────────────────────────────────
    private static final int RESOLUTION = 300;
    private static final int SHADOW_S = 9;
    private static final String MODELS_PATH = System.getProperty("user.dir") + "/scenes/models/";

    // ── geometry ───────────────────────────────────────────────────────────────

    /**
     * Full flat scene — all leaf geometries, no hierarchy.
     */
    private static final Geometries FLAT;
    /**
     * Same content organised into spatial groups (manual BVH).
     */
    private static final Geometries MANUAL;

    static {
        FLAT = buildFlat();
        MANUAL = buildManual(FLAT);
    }

    // ── scene builder ──────────────────────────────────────────────────────────

    private static Geometries buildFlat() {
        Geometries g = new Geometries();

        // ── [1] head mesh (floats inside the crystal sphere) ─────────────────
        var headMesh = ObjLoader.load(
                new File(MODELS_PATH + "11091_FemaleHead_v4.obj"),
                22, 0, 60, -150, 1);
        Material skinMat   = new Material().setKD(0.65).setKS(0.15).setShininess(30);
        Color    skinColor = new Color(210, 148, 108);
        for (var tri : headMesh.getChildren())
            ((geometries.api.Geometry) tri).setEmission(skinColor).setMaterial(skinMat);
        g.add(headMesh.getChildren().toArray(new Intersectable[0]));

        // ── [2] Sphere — crystal ball containing the head ─────────────────────
        g.add(new Sphere(new Point(0, 60, -150), 90)
                .setEmission(new Color(5, 10, 35))
                .setMaterial(new Material()
                        .setKD(0.03).setKS(0.45).setKT(0.82).setShininess(900)));

        // ── [3] Cylinder — pedestal pillar ───────────────────────────────────
        g.add(new Cylinder(22,
                new Ray(new Point(0, -200, -150), new Vector(0, 1, 0)), 260)
                .setEmission(new Color(32, 20, 55))
                .setMaterial(new Material()
                        .setKD(0.3).setKS(0.5).setKR(0.18).setShininess(350)));

        // ── [4] Polygon — wide base of pedestal ──────────────────────────────
        g.add(new Polygon(
                new Point(-80, -200, -230), new Point(80, -200, -230),
                new Point(80,  -200, -70),  new Point(-80, -200, -70))
                .setEmission(new Color(20, 13, 40))
                .setMaterial(new Material()
                        .setKD(0.35).setKS(0.45).setKR(0.2).setShininess(200)));

        // ── [5] Polygon — upper cap of pedestal (connects pillar to sphere) ───
        g.add(new Polygon(
                new Point(-28, 60, -178), new Point(28, 60, -178),
                new Point(28,  60, -122), new Point(-28, 60, -122))
                .setEmission(new Color(40, 25, 65))
                .setMaterial(new Material()
                        .setKD(0.2).setKS(0.65).setKR(0.25).setShininess(600)));

        // ── [6] Triangle — crystal shards flanking the base ──────────────────
        Material shard = new Material().setKD(0.15).setKS(0.55).setKT(0.45).setShininess(600);
        g.add(new Triangle(
                new Point(-100, -200, -150), new Point(-35, -200, -210),
                new Point(-70,   -80, -180))
                .setEmission(new Color(80, 30, 170)).setMaterial(shard));
        g.add(new Triangle(
                new Point(35, -200, -210), new Point(100, -200, -150),
                new Point(70,   -80, -180))
                .setEmission(new Color(30, 80, 200)).setMaterial(shard));

        // ── [7] Tube — glowing nimbus arc above the scene ────────────────────
        g.add(new Tube(4,
                new Ray(new Point(-350, 310, -200), new Vector(1, 0, 0)))
                .setEmission(new Color(170, 150, 255))
                .setMaterial(new Material().setKD(0.1).setKS(0.8).setShininess(700)));

        // ── Ring of cylinders (8 vertical, radius 122 around sphere center) ──
        // positions: (122*cos(i*45°), 60, -150 + 122*sin(i*45°))
        Material ringCyl = new Material().setKD(0.2).setKS(0.7).setKR(0.1).setShininess(500);
        int[][] cp = {
            { 122, -150}, { 86,  -64}, {  0,  -28}, {-86,  -64},
            {-122, -150}, {-86, -236}, {  0, -272}, { 86, -236}
        };
        Color[] ce = {
            new Color(200, 70, 10),  new Color(170, 155, 10), new Color(10, 170, 100),
            new Color(10, 100, 200), new Color(115, 10, 200), new Color(200, 10, 115),
            new Color(10, 200, 155), new Color(155, 200, 10)
        };
        for (int i = 0; i < 8; i++)
            g.add(new Cylinder(5,
                    new Ray(new Point(cp[i][0], 35, cp[i][1]), new Vector(0, 1, 0)), 50)
                    .setEmission(ce[i]).setMaterial(ringCyl));

        // ── Ring of spheres (10 spheres, radius 158 around sphere center) ────
        // positions: (158*cos(i*36°), 60, -150 + 158*sin(i*36°))
        Material ringSph = new Material().setKD(0.25).setKS(0.5).setKR(0.2).setShininess(250);
        int[][] sp = {
            { 158, -150}, { 128,  -57}, {  49,   7}, {-49,   7}, {-128,  -57},
            {-158, -150}, {-128, -243}, {-49, -307}, { 49, -307}, { 128, -243}
        };
        Color[] se = {
            new Color(230, 40, 40),  new Color(230, 135, 20), new Color(165, 230, 20),
            new Color(40, 230, 85),  new Color(20, 210, 230), new Color(40, 85, 250),
            new Color(135, 30, 250), new Color(230, 30, 175), new Color(250, 115, 30),
            new Color(30, 230, 175)
        };
        for (int i = 0; i < 10; i++)
            g.add(new Sphere(new Point(sp[i][0], 60, sp[i][1]), 12)
                    .setEmission(se[i]).setMaterial(ringSph));

        // ── Plane — reflective floor ──────────────────────────────────────────
        g.add(new Plane(new Point(0, -200, 0), new Vector(0, 1, 0))
                .setEmission(new Color(8, 6, 16))
                .setMaterial(new Material()
                        .setKD(0.4).setKS(0.3).setKR(0.45).setShininess(80)));

        // ── Plane — dark back wall ────────────────────────────────────────────
        g.add(new Plane(new Point(0, 0, -650), new Vector(0, 0, 1))
                .setEmission(new Color(4, 4, 12))
                .setMaterial(new Material().setKD(0.8).setKS(0.1).setShininess(5)));

        // ── Inverted-V mirrors (/\ shape behind scene) ────────────────────────
        // Left panel: normal points right+forward → (0.707, 0, 0.707)
        // Right panel: normal points left+forward → (-0.707, 0, 0.707)
        // They "open" toward the camera and reflect each other's contents.
        Material bigMirror = new Material()
                .setKD(0.01).setKS(0.99).setKR(0.92).setShininess(3000);
        g.add(new Plane(new Point(-240, 0, -420), new Vector(1, 0, 1))
                .setEmission(new Color(3, 3, 7)).setMaterial(bigMirror));
        g.add(new Plane(new Point( 240, 0, -420), new Vector(-1, 0, 1))
                .setEmission(new Color(3, 3, 7)).setMaterial(bigMirror));

        return g;
    }

    private static Geometries buildManual(Geometries flat) {
        Geometries root = new Geometries();
        Geometries left = new Geometries();   // x < -100
        Geometries center = new Geometries();   // -100 <= x <= 100
        Geometries right = new Geometries();   // x > 100

        for (Intersectable child : flat.getChildren()) {
            geometries.api.AABB box = child.getBoundingBox();
            if (box == null) {
                root.add(child);
                continue;
            }
            double cx = box.midpoint(0);
            if (cx < -100) left.add(child);
            else if (cx > 100) right.add(child);
            else center.add(child);
        }

        root.add(left, center, right);
        return root;
    }

    // ── lights / scene ─────────────────────────────────────────────────────────

    private static Scene buildScene(Geometries geos) {
        Scene scene = new Scene("Crystal Pedestal")
                .setAmbientLight(new AmbientLight(new Color(8, 7, 12)));

        // soft blue-white fill
        scene.lights.add(new DirectionalLight(
                new Color(28, 30, 48), new Vector(1, -1, -1)));

        // main spotlight — focused on pedestal + head (warm white, soft shadows)
        scene.lights.add(new SpotLight(new Color(210, 185, 155),
                new Point(0, 600, 200), new Vector(0, -1, -0.35))
                .setKl(0.00001).setKq(0.000001).setNarrowBeam(4).setRadius(22));

        // warm orange accent from left
        scene.lights.add(new PointLight(new Color(175, 95, 30),
                new Point(-400, 300, 280))
                .setKl(0.00002).setKq(0.000002).setRadius(18));

        // cool blue accent from right
        scene.lights.add(new PointLight(new Color(30, 70, 200),
                new Point(400, 300, 280))
                .setKl(0.00002).setKq(0.000002).setRadius(18));

        // purple uplight from below (dramatic)
        scene.lights.add(new SpotLight(new Color(100, 55, 210),
                new Point(0, -140, 200), new Vector(0, 1, -0.5))
                .setKl(0.00002).setKq(0.000002).setNarrowBeam(5).setRadius(12));

        scene.setGeometries(geos);
        return scene;
    }

    // ── camera / render ────────────────────────────────────────────────────────

    private void render(Geometries geos, boolean cbr, boolean mt, String name) {
        if (cbr) Intersectable.setCBR();
        else Intersectable.disableCBR();
        Scene scene = buildScene(geos);

        var builder = Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setAntiAliasing(SHADOW_S, SamplingPatterns.JITTERED)
                .setSoftShadows(SHADOW_S, SamplingPatterns.GRID)
                .setGlossyReflection(SHADOW_S, SamplingPatterns.JITTERED)
                .setDiffuseGlass(SHADOW_S, SamplingPatterns.JITTERED)
                .setLocation(new Point(0, 50, 500))
                .setDirection(new Point(-200, 0, -200), Vector.AXIS_Y)
                .setVpSize(500, 500)
                .setVpDistance(450)
                .setResolution(RESOLUTION, RESOLUTION);

        if (mt) builder.setMultithreadingAuto();
        builder.build().renderImage().writeToImage("finalScene/" + name);
    }

    // ── threading helper ───────────────────────────────────────────────────────

    private void renderRaw(Geometries geos, boolean cbr, String name) {
        if (cbr) Intersectable.setCBR();
        else Intersectable.disableCBR();
        Scene scene = buildScene(geos);
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setAntiAliasing(SHADOW_S, SamplingPatterns.JITTERED)
                .setSoftShadows(SHADOW_S, SamplingPatterns.GRID)
                .setGlossyReflection(SHADOW_S, SamplingPatterns.JITTERED)
                .setDiffuseGlass(SHADOW_S, SamplingPatterns.JITTERED)
                .setLocation(new Point(0, 130, 580))
                .setDirection(new Point(0, 60, -150), Vector.AXIS_Y)
                .setVpSize(500, 500).setVpDistance(480)
                .setResolution(RESOLUTION, RESOLUTION)
                .setMultithreading(4)
                .build().renderImage().writeToImage("finalScene/" + name);
    }

    private void renderParallel(Geometries geos, boolean cbr, String name) {
        if (cbr) Intersectable.setCBR();
        else Intersectable.disableCBR();
        Scene scene = buildScene(geos);
        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setAntiAliasing(SHADOW_S, SamplingPatterns.JITTERED)
                .setSoftShadows(SHADOW_S, SamplingPatterns.GRID)
                .setGlossyReflection(SHADOW_S, SamplingPatterns.JITTERED)
                .setDiffuseGlass(SHADOW_S, SamplingPatterns.JITTERED)
                .setLocation(new Point(0, 130, 580))
                .setDirection(new Point(0, 60, -150), Vector.AXIS_Y)
                .setVpSize(500, 500).setVpDistance(480)
                .setResolution(RESOLUTION, RESOLUTION)
                .setParallelStreaming()
                .build().renderImage().writeToImage("finalScene/" + name);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ללא האצה
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * ללא BVH/CBR — Raw Threads
     */
    @Test
    void noAccel_rawThreads() {
        renderRaw(FLAT, false, "noAccel_rawThreads");
    }

    /**
     * ללא BVH/CBR — Parallel Stream
     */
    @Test
    void noAccel_parallel() {
        renderParallel(FLAT, false, "noAccel_parallel");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  עם BVH אוטומטי + CBR
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * BVH + CBR — Raw Threads
     */
    @Test
    void bvhCBR_rawThreads() {
        renderRaw(FLAT.buildBVH(), true, "bvhCBR_rawThreads");
    }

    /**
     * BVH + CBR — Parallel Stream
     */
    @Test
    void bvhCBR_parallel() {
        renderParallel(FLAT.buildBVH(), true, "bvhCBR_parallel");
    }
}

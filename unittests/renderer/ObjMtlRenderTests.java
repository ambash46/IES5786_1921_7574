package renderer;

import geometries.impl.Geometries;
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

import java.io.File;

/**
 * Renders an OBJ + MTL file exported from Blender / 3ds Max / etc.
 *
 * <h2>How to use</h2>
 * <ol>
 *   <li>Export your scene as <b>Wavefront OBJ</b> from your 3D software.</li>
 *   <li>Place both the <b>.obj</b> and its companion <b>.mtl</b> file in
 *       {@code <project-root>/scenes/models/}.</li>
 *   <li>Set {@link #OBJ_FILE} to the filename (without path).</li>
 *   <li>Adjust {@link #SCALE}, {@link #TRANSLATE_Y}, {@link #TRANSLATE_Z}
 *       to fit your model in the scene (start with scale=1 and adjust).</li>
 *   <li>Run {@link #renderWithMtl()} or {@link #renderWithMtlBvh()} (faster).</li>
 * </ol>
 *
 * <h2>Supported MTL properties</h2>
 * <ul>
 *   <li>{@code Ka} → ambient coefficient</li>
 *   <li>{@code Kd} → diffuse coefficient (surface colour)</li>
 *   <li>{@code Ks} → specular coefficient</li>
 *   <li>{@code Ns} → shininess (0–1000)</li>
 *   <li>{@code d} / {@code Tr} → transparency</li>
 *   <li>{@code Ke} → self-emission colour</li>
 * </ul>
 * Textures ({@code map_Kd}, etc.) are not supported — only flat colours.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class ObjMtlRenderTests {

    /** Default constructor. */
    ObjMtlRenderTests() { }

    // ── ✏ CHANGE THESE ───────────────────────────────────────────────────────

    /** OBJ filename (must be in {@code scenes/models/}). */
    private static final String OBJ_FILE = "myScene.obj";

    /**
     * Uniform scale applied after centering.
     * Start with 1.0 and increase/decrease until the model fills the frame.
     */
    private static final double SCALE = 1.0;

    /** Y translation (move model up/down). */
    private static final double TRANSLATE_Y = 0;
    /** Z translation (move model forward/backward). */
    private static final double TRANSLATE_Z = 0;

    /** Resolution (pixels). */
    private static final int RESOLUTION = 500;

    // ── internals ─────────────────────────────────────────────────────────────

    private static final String MODELS_PATH =
            System.getProperty("user.dir") + "/scenes/models/";

    // ── helpers ───────────────────────────────────────────────────────────────

    private static Geometries loadMesh() {
        return ObjLoader.load(
                new File(MODELS_PATH + OBJ_FILE),
                SCALE, 0, TRANSLATE_Y, TRANSLATE_Z, 1);
    }

    private static Scene buildScene(Geometries geos) {
        Scene scene = new Scene("OBJ+MTL Scene")
                .setAmbientLight(new AmbientLight(new Color(25, 25, 25)));

        // soft fill from above-left
        scene.lights.add(new DirectionalLight(
                new Color(120, 120, 140), new Vector(1, -1, -1)));

        // key light (warm) from front-left
        scene.lights.add(new PointLight(new Color(400, 350, 280),
                new Point(-300, 400, 400))
                .setKl(0.00003).setKq(0.000003));

        // cool rim from right
        scene.lights.add(new PointLight(new Color(180, 200, 380),
                new Point(400, 200, 300))
                .setKl(0.00004).setKq(0.000004));

        // spotlight from above (focused, soft-shadow capable)
        scene.lights.add(new SpotLight(new Color(350, 320, 280),
                new Point(0, 600, 300), new Vector(0, -1, -0.4))
                .setKl(0.00001).setKq(0.000001).setNarrowBeam(3));

        scene.setGeometries(geos);
        return scene;
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    /**
     * Renders the OBJ scene with its MTL materials as a flat mesh (no BVH
     * grouping), 4 raw threads. Good first test — run this to check
     * proportions and lighting.
     */
    @Test
    void renderWithMtl() {
        Geometries mesh = loadMesh();
        SimpleRayTracer tracer = new SimpleRayTracer(buildScene(mesh));
        Camera.getBuilder()
                .setRayTracer(tracer)
                .setLocation(new Point(0, 0, 500))
                .setDirection(new Point(0, 0, 0), Vector.AXIS_Y)
                .setVpSize(500, 500).setVpDistance(450)
                .setResolution(RESOLUTION, RESOLUTION)
                .setMultithreading(4)
                .build().renderImage().writeToImage("objMtl/noAccel");
    }

    /**
     * Same scene organized into a BVH — much faster for large meshes.
     */
    @Test
    void renderWithMtlBvh() {
        Geometries mesh = loadMesh().buildBVH();
        SimpleRayTracer tracer = new SimpleRayTracer(buildScene(mesh));
        Camera.getBuilder()
                .setRayTracer(tracer)
                .setLocation(new Point(0, 0, 500))
                .setDirection(new Point(0, 0, 0), Vector.AXIS_Y)
                .setVpSize(500, 500).setVpDistance(450)
                .setResolution(RESOLUTION, RESOLUTION)
                .setMultithreadingAuto()
                .build().renderImage().writeToImage("objMtl/bvh");
    }

    /**
     * High-quality render: BVH + soft shadows + anti-aliasing.
     * Slow — run last after confirming the scene looks right.
     */
    @Test
    void renderWithMtlHQ() {
        Geometries mesh = loadMesh().buildBVH();
        SimpleRayTracer tracer = new SimpleRayTracer(buildScene(mesh))
                .setShadowSamples(9, SamplingPatterns.GRID);

        Camera.getBuilder()
                .setRayTracer(tracer)
                .setAntiAliasing(9, SamplingPatterns.JITTERED)
                .setLocation(new Point(0, 0, 500))
                .setDirection(new Point(0, 0, 0), Vector.AXIS_Y)
                .setVpSize(500, 500).setVpDistance(450)
                .setResolution(RESOLUTION, RESOLUTION)
                .setMultithreadingAuto()
                .build().renderImage().writeToImage("objMtl/hq");
    }
}

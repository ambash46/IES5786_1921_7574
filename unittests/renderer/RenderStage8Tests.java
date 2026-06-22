package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.XmlSceneLoader;

/**
 * Renders all stage-8 scenes from XML files.
 *
 * <p>Each test loads its scene from an XML file; only the camera is configured
 * in Java. The bonus-2 camera-angle tests reuse the same gallery XML file with
 * a different camera position / {@code rotate()} angle each time.
 *
 * <p>XML files:
 * <ul>
 *   <li>{@code stage8ShadowSphereTriangle.xml}  — sphere + triangle shadow</li>
 *   <li>{@code stage8ShadowTrianglesSphere.xml}  — two triangles + sphere shadow</li>
 *   <li>{@code stage8CrystalScene.xml}           — mandatory 4-body scene</li>
 *   <li>{@code stage8CrystalGallery.xml}         — bonus-1 gallery, 13 bodies</li>
 * </ul>
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class RenderStage8Tests {

    /** Default constructor to satisfy JavaDoc generator. */
    RenderStage8Tests() { /* no-op */ }

    /** Pixel resolution used for all test renders (width = height). */
    private static final int RESOLUTION = 500;

    /**
     * Loads a scene from the named XML file via {@link XmlSceneLoader}.
     *
     * @param name base name of the XML file (no extension, no path prefix)
     * @return the loaded {@link Scene}
     */
    private Scene load(String name) {
        return new XmlSceneLoader().load(name);
    }

    // ── Shadow tests ──────────────────────────────────────────────────────────

    /**
     * Sphere + triangle with spotlight — replicates
     * {@link ShadowTests#testSphereTriangleInitial()}.
     */
    @Test
    void testShadowSphereTriangle() {
        Camera.getBuilder()
                .setRayTracer(load("stage8ShadowSphereTriangle"), RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(200, 200).setVpDistance(1000)
                .setResolution(600, 600)
                .build()
                .renderImage()
                .writeToImage("xml_shadowSphereTriangle");
    }

    /**
     * Two triangles + sphere with spotlight — replicates
     * {@link ShadowTests#testTrianglesSphere()}.
     */
    @Test
    void testShadowTrianglesSphere() {
        Camera.getBuilder()
                .setRayTracer(load("stage8ShadowTrianglesSphere"), RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(new Point(0, 0, -1), Vector.AXIS_Y)
                .setVpSize(200, 200).setVpDistance(1000)
                .setResolution(600, 600)
                .build()
                .renderImage()
                .writeToImage("xml_shadowTrianglesSphere");
    }

    // ── Crystal scene tests ───────────────────────────────────────────────────

    /**
     * Mandatory 4-body scene — replicates {@link Stage8Tests#testCrystalScene()}.
     */
    @Test
    void testCrystalScene() {
        Camera.getBuilder()
                .setRayTracer(load("stage8CrystalScene"), RayTracerType.SIMPLE)
                .setLocation(new Point(0, 50, 1000))
                .setDirection(new Point(0, -100, -100), Vector.AXIS_Y)
                .setVpSize(560, 560).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("xml_crystalScene");
    }

    /**
     * Bonus-1 gallery (13 bodies, all geometry types) — replicates
     * {@link Stage8Tests#testCrystalGallery()}.
     */
    @Test
    void testCrystalGallery() {
        Camera.getBuilder()
                .setRayTracer(load("stage8CrystalGallery"), RayTracerType.SIMPLE)
                .setLocation(new Point(0, 50, 1000))
                .setDirection(new Point(0, -100, -100), Vector.AXIS_Y)
                .setVpSize(580, 580).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("xml_crystalGallery");
    }

    // ── Bonus 2 — same gallery XML, different camera angles ──────────────────

    /**
     * Bonus-2a: bird's-eye overhead view of the gallery.
     * Camera placed directly above the scene; up-vector {@code (0,0,-1)}
     * orients the image so the far end appears at the top of the frame.
     * Replicates {@link Stage8Tests#testCrystalGalleryTopView()}.
     */
    @Test
    void testCrystalGalleryTopView() {
        Camera.getBuilder()
                .setRayTracer(load("stage8CrystalGallery"), RayTracerType.SIMPLE)
                .setLocation(new Point(0, 950, -350))
                .setDirection(new Point(0, -200, -350), new Vector(0, 0, -1))
                .setVpSize(620, 620).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("xml_crystalGallery_topView");
    }

    /**
     * Bonus-2b: diagonal side view from the right with a 20° camera roll
     * applied via {@link Camera.Builder#rotate(double)}.
     * Replicates {@link Stage8Tests#testCrystalGallerySideView()}.
     */
    @Test
    void testCrystalGallerySideView() {
        Camera.getBuilder()
                .setRayTracer(load("stage8CrystalGallery"), RayTracerType.SIMPLE)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("xml_crystalGallery_sideView");
    }

    /**
     * Bonus-2c: close-up frontal view focused on the crystal sphere and pedestal.
     * Camera moved closer and zoomed in (smaller viewport = tighter FOV).
     */
    @Test
    void testCrystalGalleryCloseUp() {
        Camera.getBuilder()
                .setRayTracer(load("stage8CrystalGallery"), RayTracerType.SIMPLE)
                .setLocation(new Point(-170, 80, 500))
                .setDirection(new Point(-170, -120, -100), Vector.AXIS_Y)
                .setVpSize(300, 300).setVpDistance(600)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("xml_crystalGallery_closeUp");
    }

    /**
     * Bonus-2d: wide low-angle view from behind the pyramid looking toward
     * the spheres — creates a dramatic reverse perspective.
     * Camera rolled −15° to add dynamism.
     */
    @Test
    void testCrystalGalleryRearView() {
        Camera.getBuilder()
                .setRayTracer(load("stage8CrystalGallery"), RayTracerType.SIMPLE)
                .setLocation(new Point(0, -80, -600))
                .setDirection(new Point(0, -50, 200), Vector.AXIS_Y)
                .rotate(-15)
                .setVpSize(650, 650).setVpDistance(800)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("xml_crystalGallery_rearView");
    }
}

package renderer;

import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
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
import scene.Scene;

/**
 * Stage-8 custom scenes: mandatory 4-body scene + bonus gallery (10+ bodies,
 * all geometry types, all stage-8 effects) rendered from three camera angles.
 *
 * <p>Scene concept — "Crystal Gallery": a night exhibition space with a
 * reflective marble floor, a transparent crystal orb on a metallic pedestal,
 * a mirror ball, a golden pyramid, accent spheres, a chrome horizontal bar,
 * and a decorative wall panel.
 *
 * <ul>
 *   <li>{@link #testCrystalScene}        — mandatory (4 bodies)</li>
 *   <li>{@link #testCrystalGallery}      — bonus 1 (13 bodies, all types)</li>
 *   <li>{@link #testCrystalGalleryTopView} — bonus 2a (bird's-eye angle)</li>
 *   <li>{@link #testCrystalGallerySideView} — bonus 2b (diagonal + 20° roll)</li>
 * </ul>
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class Stage8Tests {

    private static final int RESOLUTION = 500;

    // ─────────────────────────────────────────────────────────────────────────
    //  Shared scene builder  (13 bodies, 6 geometry types)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Builds the full "Crystal Gallery" scene used by all bonus tests.
     * Contains 13 distinct geometry instances spanning all 6 implemented types.
     */
    private static Scene buildGalleryScene() {
        Scene scene = new Scene("Crystal Gallery")
                .setBackground(new Color(3, 4, 12))
                .setAmbientLight(new AmbientLight(new Color(15, 11, 9)));

        // ── Materials  (kD + kS + kR + kT ≤ 0.9 → energy-conserving) ──────
        Material floorMat    = new Material().setKD(0.50).setKS(0.15).setKR(0.25).setShininess(25);
        Material wallMat     = new Material().setKD(0.70).setShininess(5);
        // kT only — no kR: avoids trapping rays inside a closed surface
        Material crystalMat  = new Material().setKD(0.05).setKS(0.20).setKT(0.65).setShininess(600);
        // kR only — no kT
        Material mirrorMat   = new Material().setKD(0.05).setKS(0.10).setKR(0.75).setShininess(1000);
        Material pedestalMat = new Material().setKD(0.20).setKS(0.30).setKR(0.35).setShininess(400);
        Material goldMat     = new Material().setKD(0.45).setKS(0.35).setShininess(300);
        Material chromeMat   = new Material().setKD(0.10).setKS(0.65).setShininess(700);
        Material tileMat     = new Material().setKD(0.50).setKS(0.25).setShininess(60);

        // ── PLANES (2) ─────────────────────────────────────────────────────

        // 1. Warm terra-cotta marble floor
        scene.geometries.add(
                new Plane(new Point(0, -200, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(28, 16, 7))
                        .setMaterial(floorMat));

        // 2. Deep indigo back wall
        scene.geometries.add(
                new Plane(new Point(0, 0, -500), new Vector(0, 0, 1))
                        .setEmission(new Color(10, 10, 40))
                        .setMaterial(wallMat));

        // ── SPHERES (4) ────────────────────────────────────────────────────

        // 3. Crystal sphere — vivid icy-blue emission
        scene.geometries.add(
                new Sphere(new Point(-170, -100, -100), 100)
                        .setEmission(new Color(6, 18, 75))
                        .setMaterial(crystalMat));

        // 4. Mirror sphere — rich deep-blue emission
        scene.geometries.add(
                new Sphere(new Point(210, -100, -180), 100)
                        .setEmission(new Color(5, 10, 45))
                        .setMaterial(mirrorMat));

        // 5. Small accent sphere — bright ember-red
        scene.geometries.add(
                new Sphere(new Point(50, -185, -300), 15)
                        .setEmission(new Color(120, 30, 4))
                        .setMaterial(new Material().setKD(0.7).setKS(0.2).setShininess(20)));

        // 6. Small accent sphere — bright cyan-teal
        scene.geometries.add(
                new Sphere(new Point(-50, -190, -440), 10)
                        .setEmission(new Color(4, 100, 120))
                        .setMaterial(new Material().setKD(0.4).setKS(0.5).setShininess(200)));

        // ── TRIANGLES — pyramid, each face a distinct warm hue ─────────────
        Point apex = new Point(0, 80, -370);

        // 7. Front face — bright gold
        scene.geometries.add(
                new Triangle(new Point(-100, -200, -280), new Point(100, -200, -280), apex)
                        .setEmission(new Color(80, 55, 6))
                        .setMaterial(goldMat));

        // 8. Right face — orange-gold
        scene.geometries.add(
                new Triangle(new Point(100, -200, -280), new Point(100, -200, -460), apex)
                        .setEmission(new Color(90, 38, 5))
                        .setMaterial(goldMat));

        // 9. Back face — deep amber
        scene.geometries.add(
                new Triangle(new Point(100, -200, -460), new Point(-100, -200, -460), apex)
                        .setEmission(new Color(55, 28, 4))
                        .setMaterial(goldMat));

        // 10. Left face — warm gold
        scene.geometries.add(
                new Triangle(new Point(-100, -200, -460), new Point(-100, -200, -280), apex)
                        .setEmission(new Color(75, 48, 6))
                        .setMaterial(goldMat));

        // ── CYLINDER (1) ───────────────────────────────────────────────────

        // 11. Pedestal — vivid purple-indigo
        scene.geometries.add(
                new Cylinder(28, new Ray(new Point(-170, -200, -100), new Vector(0, 1, 0)), 100)
                        .setEmission(new Color(18, 8, 70))
                        .setMaterial(pedestalMat));

        // ── TUBE (1) ───────────────────────────────────────────────────────

        // 12. Chrome bar — cool green-silver
        scene.geometries.add(
                new Tube(6, new Ray(new Point(0, 100, -475), new Vector(1, 0, 0)))
                        .setEmission(new Color(22, 50, 22))
                        .setMaterial(chromeMat));

        // ── POLYGON (1) ────────────────────────────────────────────────────

        // 13. Wall panel — rich copper-orange
        scene.geometries.add(
                new Polygon(
                        new Point(-160, 200, -498),
                        new Point(-160, -140, -498),
                        new Point(160, -140, -498),
                        new Point(160, 200, -498))
                        .setEmission(new Color(60, 24, 6))
                        .setMaterial(tileMat));

        // ── Lights ─────────────────────────────────────────────────────────

        // Vivid blue directional — cool moonlight from upper-right
        scene.lights.add(new DirectionalLight(
                new Color(120, 130, 300), new Vector(1, -2, -1)));

        // Rich orange point light from front-left — warm firelight fill
        scene.lights.add(new PointLight(
                new Color(500, 260, 50), new Point(-380, 380, 320))
                .setKl(0.00002).setKq(0.000002));

        // Saturated purple-blue spotlight on the crystal sphere
        scene.lights.add(new SpotLight(
                new Color(80, 60, 430), new Point(-170, 450, 80), new Vector(0, -1, -0.3))
                .setKl(0.00003).setKq(0.000003).setNarrowBeam(4));

        // Vivid gold spotlight on the pyramid — treasure glow
        scene.lights.add(new SpotLight(
                new Color(460, 290, 20), new Point(0, 500, -220), new Vector(0, -1, -0.5))
                .setKl(0.00002).setKq(0.000002).setNarrowBeam(2));

        return scene;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Tests
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * MANDATORY — 4 bodies, all three new effects:
     * <ul>
     *   <li>Plane  — reflective marble floor (kR → reflection)</li>
     *   <li>Sphere — transparent crystal orb (kT → partial transparency in shadow)</li>
     *   <li>Sphere — mirror ball            (kR → reflection)</li>
     *   <li>Triangle — opaque gold sail      (casts hard + partial shadows)</li>
     * </ul>
     */
    @Test
    void testCrystalScene() {
        Scene scene = new Scene("Crystal Scene")
                .setBackground(new Color(3, 4, 12))
                .setAmbientLight(new AmbientLight(new Color(15, 11, 9)));

        // 1. Warm terra-cotta floor  (kD+kS+kR = 0.9)
        scene.geometries.add(
                new Plane(new Point(0, -200, 0), new Vector(0, 1, 0))
                        .setEmission(new Color(28, 16, 7))
                        .setMaterial(new Material().setKD(0.50).setKS(0.15).setKR(0.25).setShininess(25)));

        // 2. Vivid icy-blue crystal sphere — kT only, no kR  (kD+kS+kT = 0.9)
        scene.geometries.add(
                new Sphere(new Point(-150, -100, -100), 100)
                        .setEmission(new Color(6, 18, 75))
                        .setMaterial(new Material().setKD(0.05).setKS(0.20).setKT(0.65).setShininess(600)));

        // 3. Deep-blue mirror sphere — kR only, no kT  (kD+kS+kR = 0.9)
        scene.geometries.add(
                new Sphere(new Point(200, -100, -150), 100)
                        .setEmission(new Color(5, 10, 45))
                        .setMaterial(new Material().setKD(0.05).setKS(0.10).setKR(0.75).setShininess(1000)));

        // 4. Bright gold triangle — shadow caster  (kD+kS = 0.8)
        scene.geometries.add(
                new Triangle(
                        new Point(0, -200, -400),
                        new Point(-300, 200, -400),
                        new Point(300, 200, -400))
                        .setEmission(new Color(80, 52, 5))
                        .setMaterial(new Material().setKD(0.45).setKS(0.35).setShininess(100)));

        // Vivid blue directional — cool moonlight
        scene.lights.add(new DirectionalLight(new Color(120, 130, 300), new Vector(1, -2, -1)));
        // Rich orange from front-left — warm fill
        scene.lights.add(new PointLight(new Color(500, 260, 50), new Point(-300, 380, 300))
                .setKl(0.00002).setKq(0.000002));
        // Saturated purple-blue spotlight on the crystal sphere
        scene.lights.add(new SpotLight(new Color(80, 60, 430), new Point(0, 420, 80),
                new Vector(0, -1, -0.4)).setKl(0.00003).setKq(0.000003).setNarrowBeam(4));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 50, 1000))
                .setDirection(new Point(0, -100, -100), Vector.AXIS_Y)
                .setVpSize(560, 560).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("crystalScene");
    }

    /**
     * BONUS 1 — 13 bodies covering all 6 implemented geometry types:
     * Plane (2), Sphere (4), Triangle (4), Cylinder (1), Tube (1), Polygon (1).
     * Demonstrates shadows, partial transparency, and reflection together.
     */
    @Test
    void testCrystalGallery() {
        Camera.getBuilder()
                .setRayTracer(buildGalleryScene(), RayTracerType.SIMPLE)
                .setLocation(new Point(0, 50, 1000))
                .setDirection(new Point(0, -100, -100), Vector.AXIS_Y)
                .setVpSize(580, 580).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("crystalGallery");
    }

    /**
     * BONUS 2a — same gallery from a bird's-eye overhead angle.
     * Camera is placed directly above the scene center looking straight down;
     * the non-default up-vector {@code (0,0,-1)} orients the image so the
     * far end of the gallery appears at the top of the frame.
     */
    @Test
    void testCrystalGalleryTopView() {
        Camera.getBuilder()
                .setRayTracer(buildGalleryScene(), RayTracerType.SIMPLE)
                .setLocation(new Point(0, 950, -350))
                .setDirection(new Point(0, -200, -350), new Vector(0, 0, -1))
                .setVpSize(620, 620).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("crystalGallery_topView");
    }

    /**
     * BONUS 2b — diagonal side view from the right with a 20° camera roll
     * applied via {@link Camera.Builder#rotate(double)}.
     * The roll tilts the horizon for a dynamic artistic framing.
     */
    @Test
    void testCrystalGallerySideView() {
        Camera.getBuilder()
                .setRayTracer(buildGalleryScene(), RayTracerType.SIMPLE)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION)
                .build()
                .renderImage()
                .writeToImage("crystalGallery_sideView");
    }
}

package renderer;

import geometries.impl.Cylinder;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;
import scene.XmlSceneLoader;

/**
 * Renders all stage-7 lighting scenes from XML files.
 * <p>
 * Each test mirrors a test in {@link LightsTests} or
 * {@link LightsAllSourcesTests}, but loads the scene entirely from an XML
 * file instead of building it in code.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
class RenderStage7Tests {

    /**
     * Default constructor to satisfy JavaDoc generator
     */
    RenderStage7Tests() { /* to satisfy JavaDoc generator */ }

    /**
     * Resolution for all rendered images
     */
    private static final int RESOLUTION = 500;

    /**
     * Camera builder aimed at the sphere scenes (vpSize 150×150).
     *
     * @param scene the scene to attach to the ray tracer
     * @return a configured {@link Camera.Builder} ready to {@code .build()}
     */
    private Camera.Builder sphereCamera(Scene scene) {
        return Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(150, 150).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION);
    }

    /**
     * Camera builder aimed at the triangle scenes (vpSize 200×200).
     *
     * @param scene the scene to attach to the ray tracer
     * @return a configured {@link Camera.Builder} ready to {@code .build()}
     */
    private Camera.Builder trianglesCamera(Scene scene) {
        return Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(200, 200).setVpDistance(1000)
                .setResolution(RESOLUTION, RESOLUTION);
    }

    /**
     * Loads a scene from {@code scenes/<name>.xml}.
     *
     * @param name the base file name (without path or extension)
     * @return the fully parsed {@link Scene}
     */
    private Scene load(String name) {
        return new XmlSceneLoader().load(name);
    }

    // ── Sphere tests ──────────────────────────────────────────────────────────

    /**
     * Sphere with directional light (XML).
     */
    @Test
    void testSphereDirectional() {
        Scene scene = load("stage7SphereDirectional");
        sphereCamera(scene).build().renderImage().writeToImage("xml_lightSphereDirectional");
    }

    /**
     * Sphere with point light (XML).
     */
    @Test
    void testSpherePoint() {
        Scene scene = load("stage7SpherePoint");
        sphereCamera(scene).build().renderImage().writeToImage("xml_lightSpherePoint");
    }

    /**
     * Sphere with spotlight (XML).
     */
    @Test
    void testSphereSpot() {
        Scene scene = load("stage7SphereSpot");
        sphereCamera(scene).build().renderImage().writeToImage("xml_lightSphereSpot");
    }

    /**
     * Sphere with all three light sources combined (XML).
     */
    @Test
    void testSphereAllLights() {
        Scene scene = load("stage7SphereAllLights");
        sphereCamera(scene).build().renderImage().writeToImage("xml_lightSphereAllSources");
    }

    // ── Triangles tests ───────────────────────────────────────────────────────

    /**
     * Two triangles with directional light (XML).
     */
    @Test
    void testTrianglesDirectional() {
        Scene scene = load("stage7TrianglesDirectional");
        trianglesCamera(scene).build().renderImage().writeToImage("xml_lightTrianglesDirectional");
    }

    /**
     * Two triangles with point light (XML).
     */
    @Test
    void testTrianglesPoint() {
        Scene scene = load("stage7TrianglesPoint");
        trianglesCamera(scene).build().renderImage().writeToImage("xml_lightTrianglesPoint");
    }

    /**
     * Two triangles with spotlight (XML).
     */
    @Test
    void testTrianglesSpot() {
        Scene scene = load("stage7TrianglesSpot");
        trianglesCamera(scene).build().renderImage().writeToImage("xml_lightTrianglesSpot");
    }

    /**
     * Two triangles with all three light sources combined (XML).
     */
    @Test
    void testTrianglesAllLights() {
        Scene scene = load("stage7TrianglesAllLights");
        trianglesCamera(scene).build().renderImage().writeToImage("xml_lightTrianglesAllSources");
    }

    /**
     * Showcase scene using every available geometry type (except Plane):
     * Sphere, Triangle, Cylinder, Tube, Polygon — with four light sources.
     */
    @Test
    @SuppressWarnings("java:S109")
    void testShinyShowcase() {
        Scene scene = new Scene("Shiny showcase – all shapes")
                .setBackground(new Color(5, 5, 15))
                .setAmbientLight(new AmbientLight(new Color(15, 15, 20)));

        // ── Sphere: large central mirror-like sphere ──────────────────────────
        scene.geometries.add(
                new Sphere(new Point(0, 20, -100), 50)
                        .setEmission(new Color(15, 15, 70))
                        .setMaterial(new Material().setKD(0.15).setKS(0.85).setShininess(1000)));

        // ── Sphere: small red sphere left ─────────────────────────────────────
        scene.geometries.add(
                new Sphere(new Point(-90, 0, -130), 28)
                        .setEmission(new Color(70, 8, 8))
                        .setMaterial(new Material().setKD(0.4).setKS(0.6).setShininess(500)));

        // ── Sphere: small green sphere right ──────────────────────────────────
        scene.geometries.add(
                new Sphere(new Point(90, 0, -130), 28)
                        .setEmission(new Color(8, 55, 15))
                        .setMaterial(new Material().setKD(0.4).setKS(0.6).setShininess(500)));

        // ── Triangle: golden decorative element above the central sphere ──────
        // Vertices chosen so all three project inside the ±125 viewport:
        // at z=-80, y_max = 125 × 1080/1000 = 135; apex y=120 → vy≈111 ✓
        scene.geometries.add(
                new Triangle(
                        new Point(-55, 80, -80),
                        new Point(55, 80, -80),
                        new Point(0, 120, -80))
                        .setEmission(new Color(60, 45, 5))
                        .setMaterial(new Material().setKD(0.4).setKS(0.6).setShininess(400)));

        // ── Cylinder: dark metallic pedestal beneath the central sphere ───────
        scene.geometries.add(
                new Cylinder(22, new Ray(new Point(0, -130, -100), new Vector(0, 1, 0)), 80)
                        .setEmission(new Color(20, 20, 20))
                        .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(300)));

        // ── Tube: horizontal chrome pipe in the background ────────────────────
        scene.geometries.add(
                new Tube(7, new Ray(new Point(0, -80, -190), new Vector(1, 0, 0)))
                        .setEmission(new Color(40, 40, 30))
                        .setMaterial(new Material().setKD(0.3).setKS(0.7).setShininess(700)));

        // ── Polygon: horizontal floor panel ───────────────────────────────────
        // Laid flat at y=-130 (below cylinder base), clearly visible at the
        // bottom of the frame as a distinct glossy surface.
        scene.geometries.add(
                new Polygon(
                        new Point(-160, -130, -50),
                        new Point(160, -130, -50),
                        new Point(160, -130, -220),
                        new Point(-160, -130, -220))
                        .setEmission(new Color(10, 20, 35))
                        .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(150)));

        // ── Lights ────────────────────────────────────────────────────────────
        // Cool-white directional from upper right
        scene.lights.add(
                new DirectionalLight(new Color(300, 300, 400), new Vector(1, -1, -2)));
        // Warm-yellow point light from front-left
        scene.lights.add(
                new PointLight(new Color(600, 500, 200), new Point(-120, 120, 100))
                        .setKl(0.0001).setKq(0.00005));
        // Intense narrow spotlight from above onto the central sphere
        scene.lights.add(
                new SpotLight(new Color(700, 700, 700), new Point(0, 250, 0),
                        new Vector(0, -1, -0.4))
                        .setKl(0.0001).setKq(0.00002).setNarrowBeam(3));
        // Red accent spotlight from the right
        scene.lights.add(
                new SpotLight(new Color(500, 80, 80), new Point(220, 60, 50),
                        new Vector(-2, -1, -2))
                        .setKl(0.0002).setKq(0.00005));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 1000))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(250, 250).setVpDistance(1000)
                .setResolution(600, 600)
                .build()
                .renderImage()
                .writeToImage("shinyShowcase");
    }

    /**
     * Renders a molecular-structure scene: a naphthalene-like pair of fused
     * hexagonal rings with carbon and hydrogen atoms connected by cylindrical
     * bonds, against a solid dark-blue background.
     */
    @Test
    @Disabled
    @SuppressWarnings("java:S109")
    void testMolecule() {
        Material carbonMat = new Material().setKD(0.1).setKS(0.9).setShininess(800);
        Material hydrogenMat = new Material().setKD(0.05).setKS(0.95).setShininess(1200);
        Material bondMat = new Material().setKD(0.3).setKS(0.7).setShininess(400);

        Color carbonColor = new Color(40, 100, 165);
        Color hydrogenColor = new Color(160, 215, 255);
        Color bondColor = new Color(0, 65, 125);

        Scene scene = new Scene("Molecule")
                .setBackground(new Color(10, 25, 55))
                .setAmbientLight(new AmbientLight(new Color(8, 18, 35)));

        // Two fused hexagonal rings (naphthalene-like), tilted 30° around X-axis.
        // Bond length ≈ 50 units; molecule centred at (0, 0, −150).
        Point C1 = new Point(-43, 43, -128);  // ring-1 top
        Point C2 = new Point(0, 22, -139);  // shared upper junction
        Point C3 = new Point(0, -22, -161);  // shared lower junction
        Point C4 = new Point(-43, -43, -172);  // ring-1 bottom
        Point C5 = new Point(-87, -22, -161);  // ring-1 lower-left
        Point C6 = new Point(-87, 22, -139);  // ring-1 upper-left
        Point C7 = new Point(43, 43, -128);  // ring-2 top
        Point C8 = new Point(87, 22, -139);  // ring-2 upper-right
        Point C9 = new Point(87, -22, -161);  // ring-2 lower-right
        Point C10 = new Point(43, -43, -172);  // ring-2 bottom

        // Hydrogen atoms — 20 units beyond each peripheral carbon
        Point H1 = new Point(-43, 61, -119);
        Point H4 = new Point(-43, -61, -181);
        Point H5 = new Point(-104, -31, -165);
        Point H6 = new Point(-104, 31, -135);
        Point H7 = new Point(43, 61, -119);
        Point H8 = new Point(104, 31, -135);
        Point H9 = new Point(104, -31, -165);
        Point H10 = new Point(43, -61, -181);

        // Carbon atoms (larger spheres)
        for (Point c : new Point[]{C1, C2, C3, C4, C5, C6, C7, C8, C9, C10})
            scene.geometries.add(new Sphere(c, 13).setEmission(carbonColor).setMaterial(carbonMat));

        // Hydrogen atoms (smaller spheres)
        for (Point h : new Point[]{H1, H4, H5, H6, H7, H8, H9, H10})
            scene.geometries.add(new Sphere(h, 7).setEmission(hydrogenColor).setMaterial(hydrogenMat));

        // C-C ring bonds (radius 4)
        for (Point[] b : new Point[][]{{C1, C2}, {C2, C3}, {C3, C4}, {C4, C5}, {C5, C6}, {C6, C1},
                {C7, C2}, {C7, C8}, {C8, C9}, {C9, C10}, {C10, C3}})
            scene.geometries.add(makeBond(b[0], b[1], 4, bondColor, bondMat));

        // C-H bonds (radius 2.5)
        for (Point[] b : new Point[][]{{C1, H1}, {C4, H4}, {C5, H5}, {C6, H6},
                {C7, H7}, {C8, H8}, {C9, H9}, {C10, H10}})
            scene.geometries.add(makeBond(b[0], b[1], 2.5, bondColor, bondMat));

        // Bright cool-white directional from upper-right
        scene.lights.add(new DirectionalLight(new Color(500, 500, 650), new Vector(1, -1, -2)));
        // Soft frontal fill
        scene.lights.add(new PointLight(new Color(300, 330, 450), new Point(0, 0, 300))
                .setKl(0.0001).setKq(0.00002));
        // Narrow spotlight from upper-left — creates bright specular highlights
        scene.lights.add(new SpotLight(new Color(700, 700, 900), new Point(-80, 160, 200),
                new Vector(0.6, -1.0, -1.5))
                .setKl(0.0002).setKq(0.00003).setNarrowBeam(2));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 500))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(210, 210).setVpDistance(500)
                .setResolution(4000, 4000)
                .build()
                .renderImage()
                .writeToImage("molecule");
    }


    /**
     * Creates a cylindrical bond between two atom positions.
     *
     * @param a     start atom centre
     * @param b     end atom centre
     * @param r     radius of the bond cylinder
     * @param color emission color of the bond
     * @param mat   material of the bond
     * @return a {@link Cylinder} spanning from {@code a} to {@code b}
     */
    private static Cylinder makeBond(Point a, Point b, double r, Color color, Material mat) {
        Vector dir = b.subtract(a).normalize();
        Cylinder c = new Cylinder(r, new Ray(a, dir), a.distance(b));
        c.setEmission(color);
        c.setMaterial(mat);
        return c;
    }
}

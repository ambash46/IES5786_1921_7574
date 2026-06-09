package renderer;

import static java.awt.Color.BLUE;

import org.junit.jupiter.api.Test;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import primitives.*;
import scene.Scene;

/**
 * Tests rendering scenes illuminated by all three external light source types
 * simultaneously: directional, point, and spotlight.
 * <p>
 * Geometry data is copied from {@link LightsTests} but each test creates its
 * own local instances to avoid shared-state side-effects between test cases.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class LightsAllSourcesTests {

    /** Default constructor to satisfy JavaDoc generator */
    LightsAllSourcesTests() { /* to satisfy JavaDoc generator */ }

    // ── Geometry data (copied from LightsTests) ──────────────────────────────

    /** Image resolution */
    private static final int     RESOLUTION    = 500;
    /** Shininess exponent */
    private static final int     SHININESS     = 301;
    /** Scalar diffuse coefficient for the sphere */
    private static final double  KD            = 0.5;
    /** Scalar specular coefficient for the sphere */
    private static final double  KS            = 0.5;
    /** Per-channel diffuse coefficients for the triangles */
    private static final Double3 KD3           = new Double3(0.2, 0.6, 0.4);
    /** Per-channel specular coefficients for the triangles */
    private static final Double3 KS3           = new Double3(0.2, 0.4, 0.3);
    /** Center of the test sphere */
    private static final Point   SPHERE_CENTER = new Point(0, 0, -50);
    /** Radius of the test sphere */
    private static final double  SPHERE_RADIUS = 50D;
    /** Base emission color of the test sphere */
    private static final Color   SPHERE_COLOR  = new Color(BLUE).reduce(2);
    /** Triangle vertices */
    private static final Point[] VERTICES      = {
        new Point(-110, -110, -150),  // shared left-bottom
        new Point(95,   100,  -150),  // shared right-top
        new Point(110,  -110, -150),  // right-bottom
        new Point(-75,  78,   100)    // left-top
    };

    // ── Tests ─────────────────────────────────────────────────────────────────

    /**
     * Renders a sphere lit by all three external light sources with different
     * colors, positions, and directions.
     */
    @Test
    void testSphereAllLights() {
        Scene scene = new Scene("Sphere – all light sources");

        // Fresh instance — not shared with other tests
        scene.geometries.add(
            new Sphere(SPHERE_CENTER, SPHERE_RADIUS)
                .setEmission(SPHERE_COLOR)
                .setMaterial(new Material().setKD(KD).setKS(KS).setShininess(SHININESS)));

        // Warm directional light from upper-right
        scene.lights.add(
            new DirectionalLight(new Color(400, 200, 50), new Vector(1, 1, -2)));

        // Cool blue point light positioned in front-left, moderate attenuation
        scene.lights.add(
            new PointLight(new Color(50, 150, 500), new Point(30, 30, 50))
                .setKl(0.001).setKq(0.0002));

        // Green-yellow spotlight from below, narrow focused beam
        scene.lights.add(
            new SpotLight(new Color(150, 400, 50), new Point(-60, -60, 20),
                          new Vector(1, 1, -1))
                .setKl(0.001).setKq(0.00004));

        Camera.getBuilder()
            .setRayTracer(scene, RayTracerType.SIMPLE)
            .setLocation(new Point(0, 0, 1000))
            .setDirection(Point.ZERO, Vector.AXIS_Y)
            .setVpSize(150, 150).setVpDistance(1000)
            .setResolution(RESOLUTION, RESOLUTION)
            .build()
            .renderImage()
            .writeToImage("lightSphereAllSources");
    }

    /**
     * Renders two angled triangles lit by all three external light sources
     * with different colors, positions, and directions.
     */
    @Test
    void testTrianglesAllLights() {
        Scene scene = new Scene("Triangles – all light sources")
            .setAmbientLight(new AmbientLight(new Color(38, 38, 38)));

        // Fresh instances — not shared with other tests
        Material mat = new Material().setKD(KD3).setKS(KS3).setShininess(SHININESS);
        scene.geometries.add(
            new Triangle(VERTICES[0], VERTICES[1], VERTICES[2]).setMaterial(mat),
            new Triangle(VERTICES[0], VERTICES[1], VERTICES[3]).setMaterial(mat));

        // Warm orange directional light from upper-left
        scene.lights.add(
            new DirectionalLight(new Color(400, 250, 50), new Vector(-1, -1, -2)));

        // Cool blue point light near the shared edge of the triangles
        scene.lights.add(
            new PointLight(new Color(50, 150, 500), new Point(-30, -10, -100))
                .setKl(0.0005).setKq(0.0005));

        // Green spotlight from upper-right, aimed at the lower triangle
        scene.lights.add(
            new SpotLight(new Color(100, 450, 100), new Point(80, 80, 50),
                          new Vector(-2, -2, -3))
                .setKl(0.0005).setKq(0.00004));

        Camera.getBuilder()
            .setRayTracer(scene, RayTracerType.SIMPLE)
            .setLocation(new Point(0, 0, 1000))
            .setDirection(Point.ZERO, Vector.AXIS_Y)
            .setVpSize(200, 200).setVpDistance(1000)
            .setResolution(RESOLUTION, RESOLUTION)
            .build()
            .renderImage()
            .writeToImage("lightTrianglesAllSources");
    }
}

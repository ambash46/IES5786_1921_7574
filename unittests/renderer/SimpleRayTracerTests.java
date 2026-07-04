package renderer;

import geometries.api.Geometry;
import geometries.impl.Geometries;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for class {@link SimpleRayTracer}.
 * <p>
 * Uses minimal, hand-computable scenes (one sphere, at most one light) so the
 * expected {@link Color} of {@link SimpleRayTracer#traceRay(Ray)} can be
 * verified exactly, rather than only rendering a full image without
 * asserting specific pixel values (as the other renderer test suites do).
 * <p>
 * All test scenes use a camera-independent setup: a unit sphere at the
 * origin, a ray from {@code (0,0,5)} toward {@code (0,0,-1)} hitting the
 * sphere at {@code (0,0,1)} with normal {@code (0,0,1)}.
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class SimpleRayTracerTests {

    /** Default constructor to satisfy JavaDoc generator */
    SimpleRayTracerTests() { /* to satisfy JavaDoc generator */ }

    /** The ray used by all tests: hits the sphere head-on at (0,0,1). */
    private static final Ray RAY = new Ray(new Point(0, 0, 5), new Vector(0, 0, -1));

    /**
     * Test method for {@link SimpleRayTracer#traceRay(Ray)}.
     * A ray that hits nothing returns the scene's background color.
     */
    @Test
    void testTraceRayMissReturnsBackground() {

        // =============== Boundary Values Tests ==================

        // TC11: empty scene -> the ray always misses
        Scene scene = new Scene("empty").setBackground(new Color(10, 20, 30));
        Color result = new SimpleRayTracer(scene).traceRay(RAY);
        assertEquals(new Color(10, 20, 30), result, "A ray that hits nothing should return the background color");
    }

    /**
     * Test method for {@link SimpleRayTracer#traceRay(Ray)}.
     * With only ambient light (no external lights, no emission), the color is
     * exactly {@code ambientIntensity * kA}.
     */
    @Test
    void testTraceRayAmbientOnly() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: ambient(100,100,100) * kA(0.5,0.5,0.5) = (50,50,50)
        Geometry sphere = new Sphere(new Point(0, 0, 0), 1)
                .setMaterial(new Material().setKA(0.5));
        Scene scene = new Scene("s")
                .setAmbientLight(new AmbientLight(new Color(100, 100, 100)))
                .setGeometries(new Geometries(sphere));
        Color result = new SimpleRayTracer(scene).traceRay(RAY);
        assertEquals(new Color(50, 50, 50), result, "Ambient-only color should equal ambientIntensity * kA");
    }

    /**
     * Test method for {@link SimpleRayTracer#traceRay(Ray)}.
     * Combined diffuse + specular contribution from a single point light
     * positioned so the reflection vector points straight back at the camera
     * (a head-on specular highlight), against a black background/ambient.
     */
    @Test
    void testTraceRayDiffuseAndSpecular() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: diffuse = kD*|l·n| = 0.5, specular = kS*(-v·r)^n = 0.3*1^2 = 0.3
        // color = lightIntensity * (diffuse + specular) = (100,100,100) * 0.8 = (80,80,80)
        Geometry sphere = new Sphere(new Point(0, 0, 0), 1)
                .setMaterial(new Material().setKD(0.5).setKS(0.3).setShininess(2));
        Scene scene = new Scene("s")
                .setGeometries(new Geometries(sphere));
        scene.lights.add(new PointLight(new Color(100, 100, 100), new Point(0, 0, 10)));
        Color result = new SimpleRayTracer(scene).traceRay(RAY);
        assertEquals(new Color(80, 80, 80), result, "Diffuse+specular color did not match the hand-computed Phong formula");
    }

    /**
     * Test method for {@link SimpleRayTracer#traceRay(Ray)}.
     * Ambient and a direct light combine additively.
     */
    @Test
    void testTraceRayAmbientPlusDiffuse() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: ambient(50,50,50)*kA(0.2) = (10,10,10); diffuse = (100,100,100)*0.5 = (50,50,50)
        // total = (60,60,60)
        Geometry sphere = new Sphere(new Point(0, 0, 0), 1)
                .setMaterial(new Material().setKA(0.2).setKD(0.5));
        Scene scene = new Scene("s")
                .setAmbientLight(new AmbientLight(new Color(50, 50, 50)))
                .setGeometries(new Geometries(sphere));
        scene.lights.add(new PointLight(new Color(100, 100, 100), new Point(0, 0, 10)));
        Color result = new SimpleRayTracer(scene).traceRay(RAY);
        assertEquals(new Color(60, 60, 60), result, "Ambient and diffuse contributions should add");
    }

    /**
     * Test method for {@link SimpleRayTracer#traceRay(Ray)}.
     * An opaque blocker directly between the surface point and the light
     * fully shadows it — the diffuse contribution is not added.
     */
    @Test
    void testTraceRayFullShadow() {

        // =============== Boundary Values Tests ==================

        // TC11: opaque (default kT=0) blocker between the lit point and the light.
        // Placed at z=7 (between the point at z=1 and the light at z=10), clear of the
        // primary ray's own path (camera z=5 -> hit point z=1), so only the shadow ray sees it.
        Geometry sphere = new Sphere(new Point(0, 0, 0), 1)
                .setMaterial(new Material().setKD(0.5));
        Sphere blocker = new Sphere(new Point(0, 0, 7), 0.5); // opaque by default
        Scene scene = new Scene("s")
                .setGeometries(new Geometries(sphere, blocker));
        scene.lights.add(new PointLight(new Color(100, 100, 100), new Point(0, 0, 10)));
        Color result = new SimpleRayTracer(scene).traceRay(RAY);
        assertEquals(Color.BLACK, result, "A point fully blocked from the light should get no diffuse contribution");
    }

    /**
     * Test method for {@link SimpleRayTracer#traceRay(Ray)}.
     * A semi-transparent blocker attenuates (but does not fully block) the
     * light contribution, proportionally to its {@code kT}. The shadow ray
     * crosses the spherical blocker's near and far surfaces (2 intersections),
     * so the attenuation is {@code kT}² — consistent with how the same
     * two-surface crossing attenuates a primary ray via {@code calcTransparency}.
     */
    @Test
    void testTraceRayPartialShadow() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: blocker kT=0.5, crossed twice (entry+exit) by the shadow ray -> kT^2=0.25
        // (100,100,100) * 0.5(diffuse) * 0.25(kT^2) = (12.5,12.5,12.5)
        // Placed at z=7, clear of the primary ray's own path (camera z=5 -> hit point z=1).
        Geometry sphere = new Sphere(new Point(0, 0, 0), 1)
                .setMaterial(new Material().setKD(0.5));
        Geometry blocker = new Sphere(new Point(0, 0, 7), 0.5)
                .setMaterial(new Material().setKT(0.5));
        Scene scene = new Scene("s")
                .setGeometries(new Geometries(sphere, blocker));
        scene.lights.add(new PointLight(new Color(100, 100, 100), new Point(0, 0, 10)));
        Color result = new SimpleRayTracer(scene).traceRay(RAY);
        assertEquals(new Color(12.5, 12.5, 12.5), result,
                "A semi-transparent blocker crossed twice should scale the light contribution by kT^2");
    }

    /**
     * Test method for {@link SimpleRayTracer#traceRay(Ray)}.
     * A partially reflective surface with nothing to reflect shows the
     * background scaled by {@code kR} — not the full, unscaled background.
     */
    @Test
    void testTraceRayReflectionToBackgroundIsScaled() {

        // =============== Boundary Values Tests ==================

        // TC11: kR=0.05 against a white background -> 5% of white, not the full background
        Geometry sphere = new Sphere(new Point(0, 0, 0), 1)
                .setMaterial(new Material().setKR(0.05));
        Scene scene = new Scene("s")
                .setBackground(new Color(255, 255, 255))
                .setGeometries(new Geometries(sphere));
        Color result = new SimpleRayTracer(scene).traceRay(RAY);
        assertEquals(new Color(12.75, 12.75, 12.75), result,
                "A partially reflective surface's reflection of empty space should be scaled by kR, not the full background");
    }

    /**
     * Test method for {@link SimpleRayTracer#traceRay(Ray)}.
     * A fully (kR=1) mirror-like surface with nothing to reflect shows the
     * full background — the boundary opposite of the partial-reflection case.
     */
    @Test
    void testTraceRayFullMirrorToBackground() {

        // =============== Boundary Values Tests ==================

        // TC11: kR=1 (perfect mirror) against a colored background -> full background
        Geometry sphere = new Sphere(new Point(0, 0, 0), 1)
                .setMaterial(new Material().setKR(1));
        Scene scene = new Scene("s")
                .setBackground(new Color(10, 20, 30))
                .setGeometries(new Geometries(sphere));
        Color result = new SimpleRayTracer(scene).traceRay(RAY);
        assertEquals(new Color(10, 20, 30), result, "A perfect mirror reflecting empty space should show the full background");
    }
}

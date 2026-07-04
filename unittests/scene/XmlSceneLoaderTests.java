package scene;

import geometries.impl.Sphere;
import geometries.impl.Triangle;
import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for class {@link XmlSceneLoader}.
 * The tests verify:
 * <ul>
 * <li>{@link XmlSceneLoader#load(String)}</li>
 * </ul>
 * Loads real XML files under {@code scenes/} rather than synthetic ones, since
 * the loader reads from a fixed folder by scene name.
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class XmlSceneLoaderTests {

    /** Default constructor to satisfy JavaDoc generator */
    XmlSceneLoaderTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Test method for {@link XmlSceneLoader#load(String)}.
     * Verifies background color, ambient light, geometry count/type, and
     * material attribute parsing (both scalar and vector {@code ka}) using
     * {@code scenes/ambientRenderTest.xml}.
     */
    @Test
    void testLoadParsesBackgroundAmbientAndGeometries() {

        // ============ Equivalence Partitions Tests ==============

        Scene scene = new XmlSceneLoader().load("ambientRenderTest");

        // TC01: background color and ambient light are parsed
        assertEquals(Color.BLACK, scene.background, "Failed to parse background-color");
        assertEquals(new Color(255, 255, 255), scene.ambientLight.getIntensity(),
                "Failed to parse ambient-light color");

        // TC02: all four geometries (1 sphere + 3 triangles) are registered
        assertEquals(4, scene.geometries.getChildren().size(),
                "Expected 4 geometries (1 sphere + 3 triangles) to be registered");
        assertInstanceOf(Sphere.class, scene.geometries.getChildren().get(0),
                "First geometry should be the parsed <sphere>");
        assertInstanceOf(Triangle.class, scene.geometries.getChildren().get(1),
                "Second geometry should be the parsed <triangle>");

        // TC03: a scalar "ka" attribute is broadcast to all three components
        assertEquals(new primitives.Double3(0.4),
                ((geometries.api.Geometry) scene.geometries.getChildren().get(0)).getMaterial().kA,
                "Scalar ka=\"0.4\" should be broadcast to all three components");

        // TC04: a vector "ka" attribute and "emission" are both applied
        geometries.api.Geometry greenTriangle = (geometries.api.Geometry) scene.geometries.getChildren().get(1);
        assertEquals(new primitives.Double3(0, 0.8, 0), greenTriangle.getMaterial().kA,
                "Vector ka=\"0 0.8 0\" was not parsed correctly");
        assertEquals(new Color(0, 255, 0), greenTriangle.getEmission(), "emission=\"0 255 0\" was not applied");
    }

    /**
     * Test method for {@link XmlSceneLoader#load(String)}.
     * Verifies light-source parsing (kl/kq attributes) using
     * {@code scenes/stage7SpherePoint.xml}.
     */
    @Test
    void testLoadParsesLights() {

        // ============ Equivalence Partitions Tests ==============

        Scene scene = new XmlSceneLoader().load("stage7SpherePoint");

        // TC01: exactly one point light is registered
        assertEquals(1, scene.lights.size(), "Expected exactly one light source");
        PointLight light = assertInstanceOf(PointLight.class, scene.lights.get(0),
                "The parsed light should be a PointLight");
        assertEquals(new Color(800, 500, 0), light.getIntensity(),
                "Light color was not parsed correctly");
    }

    /**
     * Test method for {@link XmlSceneLoader#load(String)}.
     * Verifies error handling for malformed or missing input.
     */
    @Test
    void testLoadErrorHandling() {

        // =============== Boundary Values Tests ==================

        // TC11: a non-existent scene file
        assertThrows(IllegalStateException.class, () -> new XmlSceneLoader().load("no_such_scene_xyz"),
                "load() should wrap a missing file in IllegalStateException");
    }

    /**
     * Test method for {@link XmlSceneLoader#load(String)}.
     * Verifies that a scene with no {@code background-color}/{@code ambient-light}/
     * {@code lights} elements still loads successfully with sensible defaults
     * (backward compatibility with pre-stage-8 XML files, as documented).
     */
    @Test
    void testLoadDefaultsWhenElementsMissing() {

        // =============== Boundary Values Tests ==================

        // TC11: stage7SpherePoint has no background-color/ambient-light — defaults apply
        Scene scene = new XmlSceneLoader().load("stage7SpherePoint");
        assertEquals(Color.BLACK, scene.background, "Missing background-color should default to black");
        assertTrue(scene.ambientLight.getIntensity().equals(Color.BLACK),
                "Missing ambient-light should default to no ambient contribution");
    }
}

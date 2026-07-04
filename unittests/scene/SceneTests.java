package scene;

import geometries.impl.Geometries;
import geometries.impl.Sphere;
import lighting.AmbientLight;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Point;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for class {@link Scene}.
 * The tests verify:
 * <ul>
 * <li>{@link Scene#Scene(String)}</li>
 * <li>{@link Scene#setBackground(Color)}</li>
 * <li>{@link Scene#setAmbientLight(AmbientLight)}</li>
 * <li>{@link Scene#setGeometries(Geometries)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class SceneTests {

    /** Default constructor to satisfy JavaDoc generator */
    SceneTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Test method for {@link Scene#Scene(String)}.
     * Verifies the name is stored and all other fields default correctly.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: The name is stored as given, and defaults are applied to everything else
        Scene scene = new Scene("myScene");
        assertEquals("myScene", scene.name, "Scene constructor did not store the given name");
        assertEquals(Color.BLACK, scene.background, "A new scene should default to a black background");
        assertSame(AmbientLight.NONE, scene.ambientLight, "A new scene should default to no ambient light");
        assertTrue(scene.geometries.getChildren().isEmpty(), "A new scene should default to an empty geometries collection");
        assertTrue(scene.lights.isEmpty(), "A new scene should default to an empty lights list");
    }

    /**
     * Test method for {@link Scene#setBackground(Color)}.
     */
    @Test
    void testSetBackground() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: setBackground returns the same instance (chaining) and stores the value
        Scene scene = new Scene("s");
        Color color = new Color(10, 20, 30);
        assertSame(scene, scene.setBackground(color), "setBackground() should return the same instance for chaining");
        assertEquals(color, scene.background, "setBackground() did not store the given background color");
    }

    /**
     * Test method for {@link Scene#setAmbientLight(AmbientLight)}.
     */
    @Test
    void testSetAmbientLight() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: setAmbientLight returns the same instance (chaining) and stores the value
        Scene scene = new Scene("s");
        AmbientLight ambient = new AmbientLight(new Color(5, 5, 5));
        assertSame(scene, scene.setAmbientLight(ambient), "setAmbientLight() should return the same instance for chaining");
        assertSame(ambient, scene.ambientLight, "setAmbientLight() did not store the given ambient light");
    }

    /**
     * Test method for {@link Scene#setGeometries(Geometries)}.
     */
    @Test
    void testSetGeometries() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: setGeometries returns the same instance (chaining) and stores the value
        Scene scene = new Scene("s");
        Geometries geometries = new Geometries(new Sphere(Point.ZERO, 1d));
        assertSame(scene, scene.setGeometries(geometries), "setGeometries() should return the same instance for chaining");
        assertSame(geometries, scene.geometries, "setGeometries() did not store the given geometries collection");
    }
}

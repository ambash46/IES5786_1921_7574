package scene;

import java.util.ArrayList;
import java.util.List;

import geometries.impl.Geometries;
import lighting.AmbientLight;
import lighting.LightSource;
import primitives.Color;

/**
 * Represents a 3D scene to be rendered.
 * <p>
 * A scene is a Plain Data Structure (PDS): all fields are {@code public} and
 * there are no getters. Setters return {@code this} to allow method chaining.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public class Scene {

    /** The name of the scene. */
    public final String name;

    /** The background color rendered where no geometry is hit. Defaults to black. */
    public Color background = Color.BLACK;

    /** The ambient light applied uniformly to all objects. Defaults to no ambient light. */
    public AmbientLight ambientLight = AmbientLight.NONE;

    /** The collection of geometric objects in the scene. */
    public Geometries           geometries = new Geometries();

    /** The external light sources illuminating the scene. */
    public List<LightSource>    lights     = new ArrayList<>();

    /**
     * Constructs a scene with the given name.
     * Background, ambient light, and geometries are initialized to their defaults.
     *
     * @param name the name of the scene
     */
    public Scene(String name) {
        this.name = name;
    }

    /**
     * Sets the background color of the scene.
     *
     * @param background the background color
     * @return this Scene, for method chaining
     */
    public Scene setBackground(Color background) {
        this.background = background;
        return this;
    }

    /**
     * Sets the ambient light of the scene.
     *
     * @param ambientLight the ambient light
     * @return this Scene, for method chaining
     */
    public Scene setAmbientLight(AmbientLight ambientLight) {
        this.ambientLight = ambientLight;
        return this;
    }

    /**
     * Sets the geometries collection of the scene.
     *
     * @param geometries the geometries to render
     * @return this Scene, for method chaining
     */
    public Scene setGeometries(Geometries geometries) {
        this.geometries = geometries;
        return this;
    }
}

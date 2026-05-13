package geometries.api;

import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a geometric object in three-dimensional space.
 * <p>
 * A geometry can provide a normal vector at a given point on its surface.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public abstract class Geometry extends Intersectable {

    /** The emission color of this geometry. Defaults to black (no emission). */
    private Color    _emission = Color.BLACK;
    /** The material of this geometry. Defaults to a new default material. */
    private Material _material = new Material();

    /**
     * Returns the emission color of this geometry.
     *
     * @return the emission color
     */
    public Color getEmission() {
        return _emission;
    }

    /**
     * Sets the emission color of this geometry.
     *
     * @param emission the emission color
     * @return this geometry, for method chaining
     */
    public Geometry setEmission(Color emission) {
        _emission = emission;
        return this;
    }

    /**
     * Returns the material of this geometry.
     *
     * @return the material
     */
    public Material getMaterial() {
        return _material;
    }

    /**
     * Sets the material of this geometry.
     *
     * @param material the material
     * @return this geometry, for method chaining
     */
    public Geometry setMaterial(Material material) {
        _material = material;
        return this;
    }

    /**
     * Default constructor for use by subclasses.
     */
    protected Geometry() { /* no-op */ }

    /**
     * Returns the normal vector to the geometry at a given point.
     *
     * @param point a point on the geometry
     * @return the normal vector at the given point
     */
    public abstract Vector getNormal(Point point);
}

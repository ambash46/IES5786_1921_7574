package geometries.impl;

import geometries.api.RadialGeometry;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Represents an infinite tube in a three-dimensional Cartesian coordinate
 * system.
 * <p>
 * A tube is defined by a central axis ray and a radius.
 * </p>
 * @author Ambash and Elyasaf
 */
public class Tube extends RadialGeometry {
    /** The axis ray of the tube. */
    protected final Ray _axis;

    /**
     * Constructs a tube from a radius and an axis ray.
     * @param radius the radius of the tube
     * @param axis the axis ray of the tube
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        this._axis = axis;
    }

    /**
     * Returns the normal vector of the tube at the given point.
     * @param point a point on the tube
     * @return the normal vector at the given point
     */
    @Override
    public Vector getNormal(Point point) {
        return null;
    }
}

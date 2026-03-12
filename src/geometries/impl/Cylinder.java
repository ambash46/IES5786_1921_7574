package geometries.impl;

import primitives.Ray;
import primitives.Util;

/**
 * Represents a finite cylinder in a three-dimensional Cartesian coordinate
 * system.
 * <p>
 * A cylinder is defined by an axis ray, a radius, and a height.
 * </p>
 * @author Ambash and Elyasaf
 */
public final class Cylinder extends Tube {
    /** The height of the cylinder. */
    private final double _height;

    /**
     * Constructs a cylinder from a radius, an axis ray, and a height.
     * @param radius the radius of the cylinder
     * @param axis the axis ray of the cylinder
     * @param height the height of the cylinder
     * @throws IllegalArgumentException if the height is zero
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        this._height = height;
        if (Util.isZero(height))
            throw new IllegalArgumentException("Zero height not allowed");
    }
}

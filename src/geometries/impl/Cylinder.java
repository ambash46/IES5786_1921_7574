package geometries.impl;

import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

import static primitives.Util.isZero;

/**
 * Represents a finite cylinder in a three-dimensional Cartesian coordinate
 * system.
 * <p>
 * A cylinder is defined by an axis ray, a radius, and a height.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public final class Cylinder extends Tube {
    /**
     * The height of the cylinder.
     */
    private final double _height;

    /**
     * Constructs a cylinder from a radius, an axis ray, and a height.
     *
     * @param radius the radius of the cylinder
     * @param axis   the axis ray of the cylinder
     * @param height the height of the cylinder
     * @throws IllegalArgumentException if the height is zero
     */
    public Cylinder(double radius, Ray axis, double height) {
        super(radius, axis);
        this._height = height;
        if (Util.isZero(height) || height < 0)
            throw new IllegalArgumentException("Height must be positive");
    }

    @Override
    public Vector getNormal(Point point) {
        Point origin = _axis.origin();
        Vector direction = _axis.direction();
        double projection = direction.dotProduct(point.subtract(origin));

        if (isZero(projection)) {
            return direction.scale(-1);
        }

        if (isZero(projection - _height)) {
            return direction;
        }

        return super.getNormal(point);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        Cylinder other = (Cylinder) obj;
        return isZero(other._height - _height);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), _height);
    }

    @Override
    public String toString() {
        return "Cylinder{axis=" + _axis + ", radius=" + _radius + ", height=" + _height + "}";
    }
}

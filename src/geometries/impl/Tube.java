package geometries.impl;

import geometries.api.RadialGeometry;
import java.util.Objects;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.isZero;

/**
 * Represents an infinite tube in a three-dimensional Cartesian coordinate
 * system.
 * <p>
 * A tube is defined by a central axis ray and a radius.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public class Tube extends RadialGeometry {
    /**
     * The axis ray of the tube.
     */
    protected final Ray _axis;

    /**
     * Constructs a tube from a radius and an axis ray.
     *
     * @param radius the radius of the tube
     * @param axis   the axis ray of the tube
     */
    public Tube(double radius, Ray axis) {
        super(radius);
        this._axis = axis;
    }

    /**
     * Returns the normal vector of the tube at the given point.
     *
     * @param point a point on the tube
     * @return the normal vector at the given point
     */
    @Override
    public Vector getNormal(Point point) {
        Point origin = _axis.origin();
        Vector direction = _axis.direction();
        double projection = direction.dotProduct(point.subtract(origin));
        Point axisPoint = isZero(projection) ? origin : origin.add(direction.scale(projection));
        return point.subtract(axisPoint).normalize();
    }

    /**
     * Compares this tube with another object.
     *
     * @param obj the object to compare with
     * @return {@code true} if the other object is a tube with equal axis and
     * radius
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Tube other = (Tube) obj;
        return isZero(other._radius - _radius)
                && Objects.equals(_axis, other._axis);
    }

    /**
     * Returns a hash code for this tube.
     *
     * @return the hash code of the axis ray and radius
     */
    @Override
    public int hashCode() {
        return Objects.hash(_axis, _radius);
    }

    /**
     * Returns a string representation of this tube.
     *
     * @return the tube axis and radius
     */
    @Override
    public String toString() {
        return "Tube{axis=" + _axis + ", radius=" + _radius + "}";
    }
}

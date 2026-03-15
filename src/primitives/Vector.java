package primitives;

import java.util.Objects;

/**
 * Represents a non-zero vector in a three-dimensional Cartesian coordinate
 * system.
 * <p>
 * A vector supports standard vector operations such as addition, scaling,
 * dot product, cross product, and normalization.
 * </p>
 * @author Ambash and Elyasaf
 */
public final class Vector extends Point {

    /** The unit vector along the x axis. */
    public static final Vector AXIS_X = new Vector(1, 0, 0);
    /** The unit vector along the y axis. */
    public static final Vector AXIS_Y = new Vector(0, 1, 0);
    /** The unit vector along the z axis. */
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /**
     * Constructs a vector from three coordinate values.
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @throws IllegalArgumentException if the coordinates represent the zero vector
     */
    public Vector(double x, double y, double z) {
        super(x, y, z);
        if (_xyz.equals(Double3.ZERO))
            throw new IllegalArgumentException("Zero vector not allowed");
    }

    /**
     * Constructs a vector from a coordinate triplet.
     * @param xyz the coordinates of the vector
     * @throws IllegalArgumentException if the coordinates represent the zero vector
     */
    public Vector(Double3 xyz) {
        super(xyz);
        if (xyz.equals(Double3.ZERO))
            throw new IllegalArgumentException("Zero vector not allowed");
    }

    /**
     * Adds another vector to this vector.
     * @param other the vector to add
     * @return a new vector representing the sum
     * @throws IllegalArgumentException if the result is the zero vector
     */
    public final Vector add(Vector other) {
        return new Vector(this._xyz.add(other._xyz));
    }

    /**
     * Scales this vector by a scalar value.
     * @param sklar the scaling factor
     * @return a new vector scaled by the given factor
     * @throws IllegalArgumentException if the result is the zero vector
     */
    public final Vector scale(double sklar) {
        return new Vector(this._xyz.scale(sklar));
    }

    /**
     * Calculates the dot product of this vector with another vector.
     * @param other the other vector
     * @return the dot product value
     */
    public final double dotProduct(Vector other) {
        Double3 product = this._xyz.product(other._xyz);
        return product._d1() + product._d2() + product._d3();
    }

    /**
     * Calculates the cross product of this vector with another vector.
     * @param other the other vector
     * @return a vector orthogonal to both vectors
     * @throws IllegalArgumentException if the result is the zero vector
     */
    public final Vector crossProduct(Vector other) {
        double x = this._xyz._d2() * other._xyz._d3() - this._xyz._d3() * other._xyz._d2();
        double y = this._xyz._d3() * other._xyz._d1() - this._xyz._d1() * other._xyz._d3();
        double z = this._xyz._d1() * other._xyz._d2() - this._xyz._d2() * other._xyz._d1();
        return new Vector(x, y, z);
    }

    /**
     * Calculates the squared length of the vector.
     * @return the squared length of the vector
     */
    public final double lengthSquared() {
        return this.distanceSquared(ZERO);
    }

    /**
     * Calculates the length of the vector.
     * @return the length of the vector
     */
    public final double length() {
        return Math.sqrt(lengthSquared());
    }


    /**
     * Returns a unit vector in the same direction as this vector.
     * @return a normalized vector parallel to this vector
     */
    public final Vector normalize() {
        return new Vector(this._xyz.divide(this.length()));
    }

    @Override
    public final String toString() {
        return "->" + super.toString();
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return super.equals(obj);
    }
}

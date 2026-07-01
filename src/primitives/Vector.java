package primitives;

/**
 * Represents a non-zero vector in a three-dimensional Cartesian coordinate
 * system.
 * <p>
 * A vector supports standard vector operations such as addition, scaling,
 * dot product, cross product, and normalization.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public final class Vector extends Point {

    /**
     * The unit vector along the x-axis.
     */
    public static final Vector AXIS_X = new Vector(1, 0, 0);
    /**
     * The unit vector along the y-axis.
     */
    public static final Vector AXIS_Y = new Vector(0, 1, 0);
    /**
     * The unit vector along the z-axis.
     */
    public static final Vector AXIS_Z = new Vector(0, 0, 1);

    /**
     * Constructs a vector from three coordinate values.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     * @throws IllegalArgumentException if the coordinates represent the zero vector
     */
    public Vector(double x, double y, double z) {
        super(x, y, z);
        if (_xyz.equals(Double3.ZERO)) throw new IllegalArgumentException("Zero vector not allowed");
    }

    /**
     * Constructs a vector from a coordinate triplet.
     *
     * @param xyz the coordinates of the vector
     * @throws IllegalArgumentException if the coordinates represent the zero vector
     */
    public Vector(Double3 xyz) {
        super(xyz);
        if (xyz.equals(Double3.ZERO)) throw new IllegalArgumentException("Zero vector not allowed");
    }

    /**
     * Adds another vector to this vector.
     *
     * @param other the vector to add
     * @return a new vector representing the sum
     * @throws IllegalArgumentException if the result is the zero vector
     */
    public Vector add(Vector other) {
        return new Vector(this._xyz.add(other._xyz));
    }

    /**
     * Scales this vector by a scalar value.
     *
     * @param scalar the scaling factor
     * @return a new vector scaled by the given factor
     * @throws IllegalArgumentException if the result is the zero vector
     */
    public Vector scale(double scalar) {
        return new Vector(this._xyz.scale(scalar));
    }

    /**
     * Calculates the dot product of this vector with another vector.
     *
     * @param other the other vector
     * @return the dot product value
     */
    public double dotProduct(Vector other) {
        Double3 product = this._xyz.product(other._xyz);
        return product._d1() + product._d2() + product._d3();
    }

    /**
     * Calculates the cross product of this vector with another vector.
     *
     * @param other the other vector
     * @return a vector orthogonal to both vectors
     * @throws IllegalArgumentException if the result is the zero vector
     */
    public Vector crossProduct(Vector other) {
        if (isParallel(other)) {
            throw new IllegalArgumentException("Cross product is undefined for parallel vectors");
        }
        double x = this._xyz._d2() * other._xyz._d3() - this._xyz._d3() * other._xyz._d2();
        double y = this._xyz._d3() * other._xyz._d1() - this._xyz._d1() * other._xyz._d3();
        double z = this._xyz._d1() * other._xyz._d2() - this._xyz._d2() * other._xyz._d1();
        return new Vector(x, y, z);
    }

    /**
     * Checks whether this vector is parallel to another vector.
     * Parallel vectors may point in the same or in opposite directions.
     *
     * @param other the other vector
     * @return {@code true} if the vectors are parallel, {@code false} otherwise
     */
    public boolean isParallel(Vector other) {
        double x = this._xyz._d2() * other._xyz._d3() - this._xyz._d3() * other._xyz._d2();
        double y = this._xyz._d3() * other._xyz._d1() - this._xyz._d1() * other._xyz._d3();
        double z = this._xyz._d1() * other._xyz._d2() - this._xyz._d2() * other._xyz._d1();
        return Util.isZero(x) && Util.isZero(y) && Util.isZero(z);
    }

    /**
     * Calculates the squared length of the vector.
     *
     * @return the squared length of the vector
     */
    public double lengthSquared() {
        return this.distanceSquared(ZERO);
    }

    /**
     * Calculates the length of the vector.
     *
     * @return the length of the vector
     */
    public double length() {
        return Math.sqrt(lengthSquared());
    }


    /**
     * Returns a unit vector in the same direction as this vector.
     *
     * @return a normalized vector parallel to this vector
     */
    public Vector normalize() {
        return new Vector(this._xyz.divide(this.length()));
    }

    /**
     * Builds two unit vectors that are perpendicular to this vector and to each
     * other, forming an orthonormal basis with it.
     *
     * @return array {@code [vX, vY]} — two vectors orthogonal to this vector
     *         and to each other
     */
    public Vector[] buildOrthogonalBasis() {
        Vector vX = Math.abs(dotProduct(AXIS_Y)) < 0.9
                ? crossProduct(AXIS_Y).normalize()
                : crossProduct(AXIS_X).normalize();
        return new Vector[]{ vX, crossProduct(vX).normalize() };
    }

    @Override
    public String toString() {
        return "->" + super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return super.equals(obj);
    }
}

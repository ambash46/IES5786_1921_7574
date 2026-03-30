package primitives;


/**
 * Represents a point in a three-dimensional Cartesian coordinate system.
 * <p>
 * A point stores its location and provides basic geometric services such as
 * subtraction, translation by a vector, and distance calculations.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public class Point {
    /**
     * The coordinates of the point.
     */
    final protected Double3 _xyz;
    /**
     * The origin point (0,0,0).
     */
    public static final Point ZERO = new Point(Double3.ZERO);

    /**
     * Constructs a point from a coordinate triplet.
     *
     * @param _xyz the coordinates of the point
     */
    public Point(Double3 _xyz) {
        this._xyz = _xyz;
    }

    /**
     * Constructs a point from three coordinate values.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @param z the z coordinate
     */
    public Point(double x, double y, double z) {
        _xyz = new Double3(x, y, z);
    }

    /**
     * Subtracts another point from this point.
     * <p>
     * The returned vector points from the given point to this point.
     * </p>
     *
     * @param other the point to subtract
     * @return the vector from {@code other} to this point
     * @throws IllegalArgumentException if the result is the zero vector
     */
    public final Vector subtract(Point other) {
        return new Vector(this._xyz.subtract(other._xyz));
    }

    /**
     * Translates this point by a vector.
     *
     * @param vector the vector to add
     * @return a new point obtained by moving this point by the given vector
     */
    public Point add(Vector vector) {
        return new Point(this._xyz.add(vector._xyz));
    }

    /**
     * Calculates the squared distance between this point and another point.
     *
     * @param other the other point
     * @return the squared distance between the two points
     */
    public double distanceSquared(Point other) {
        double x = this._xyz._d1() - other._xyz._d1();
        double y = this._xyz._d2() - other._xyz._d2();
        double z = this._xyz._d3() - other._xyz._d3();
        return x * x + y * y + z * z;
    }

    /**
     * Calculates the distance between this point and another point.
     *
     * @param other the other point
     * @return the distance between the two points
     */
    public double distance(Point other) {
        return Math.sqrt(distanceSquared(other));
    }

    /**
     * Returns a string representation of this point.
     *
     * @return the point coordinates as a string
     */
    @Override
    public String toString() {
        return "" + _xyz;
    }

    /**
     * Compares this point with another object.
     *
     * @param obj the object to compare with
     * @return {@code true} if the other object is a point with equal
     * coordinates
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return _xyz.equals(((Point) obj)._xyz);
    }

    /**
     * Returns a hash code for this point.
     *
     * @return the hash code of the point coordinates
     */
    @Override
    public int hashCode() {
        return _xyz.hashCode();

    }


}

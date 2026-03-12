package geometries.impl;

import primitives.Point;

/**
 * Represents a triangle in a three-dimensional Cartesian coordinate system.
 * <p>
 * A triangle is a polygon defined by exactly three vertices.
 * </p>
 * @author Ambash and Elyasaf
 */
public final class Triangle extends Polygon {
    /**
     * Constructs a triangle from three vertices.
     * @param p1 the first vertex
     * @param p2 the second vertex
     * @param p3 the third vertex
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }

}

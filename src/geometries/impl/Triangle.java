package geometries.impl;

import java.util.List;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a triangle in a three-dimensional Cartesian coordinate system.
 * <p>
 * A triangle is a polygon defined by exactly three vertices.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public final class Triangle extends Polygon {
    /**
     * Constructs a triangle from three vertices.
     *
     * @param p1 the first vertex
     * @param p2 the second vertex
     * @param p3 the third vertex
     */
    public Triangle(Point p1, Point p2, Point p3) {
        super(p1, p2, p3);
    }

    /**
     * Finds all intersection points between the triangle and the given ray.
     *
     * @param ray the ray to intersect with
     * @return a list containing the single intersection point, or {@code null}
     * if there is no intersection
     */
    @Override
    public List<Point> findIntersections(Ray ray) {
        Point vertex0 = _vertices.get(0);
        Point vertex1 = _vertices.get(1);
        Point vertex2 = _vertices.get(2);

        Vector edge1 = vertex1.subtract(vertex0);
        Vector edge2 = vertex2.subtract(vertex0);
        Vector direction = ray.direction();

        Vector pVec;
        try {
            pVec = direction.crossProduct(edge2);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
        double determinant = alignZero(edge1.dotProduct(pVec));
        if (isZero(determinant)) return null;

        double inverseDeterminant = 1d / determinant;
        if (ray.origin().equals(vertex0)) return null;

        Vector tVec = ray.origin().subtract(vertex0);
        double u = alignZero(tVec.dotProduct(pVec) * inverseDeterminant);
        if (u <= 0 || u >= 1) return null;

        Vector qVec;
        try {
            qVec = tVec.crossProduct(edge1);
        } catch (IllegalArgumentException ignore) {
            return null;
        }
        double v = alignZero(direction.dotProduct(qVec) * inverseDeterminant);
        if (v <= 0 || alignZero(u + v) >= 1) return null;

        double t = alignZero(edge2.dotProduct(qVec) * inverseDeterminant);
        return t <= 0 ? null : List.of(ray.origin().add(direction.scale(t)));
    }

}

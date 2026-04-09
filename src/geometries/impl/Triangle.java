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
     * <p>
     * The implementation uses the Möller–Trumbore barycentric test:
     * first it builds two triangle edges from one reference vertex,
     * then it computes the barycentric coordinates of the ray-plane hit.
     * An intersection is accepted only when the ray reaches the triangle plane
     * in front of the ray origin and the barycentric coordinates place the hit
     * strictly inside the triangle, excluding edges and vertices.
     * </p>
     *
     * @param ray the ray to intersect with
     * @return a list containing the single intersection point, or {@code null}
     * if there is no intersection
     */
    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public List<Point> findIntersections(Ray ray) {
        Point vertex0 = _vertices.get(0);
        Point vertex1 = _vertices.get(1);
        Point vertex2 = _vertices.get(2);

        Vector edge1 = vertex1.subtract(vertex0);
        Vector edge2 = vertex2.subtract(vertex0);
        Vector direction = ray.direction();

        // If the ray direction is parallel to one triangle edge in this setup,
        // the determinant becomes zero and the ray cannot produce a valid
        // interior hit in the Möller–Trumbore test.
        if (direction.isParallel(edge2)) return null;
        Vector pVec = direction.crossProduct(edge2);
        double determinant = alignZero(edge1.dotProduct(pVec));
        if (isZero(determinant)) return null;

        double inverseDeterminant = 1d / determinant;
        if (ray.origin().equals(vertex0)) return null;

        // u is the first barycentric coordinate; values outside (0,1)
        // place the hit outside the triangle or exactly on its boundary.
        Vector tVec = ray.origin().subtract(vertex0);
        double u = alignZero(tVec.dotProduct(pVec) * inverseDeterminant);
        if (u <= 0 || u >= 1) return null;

        // qVec helps compute the second barycentric coordinate v.
        // Parallelism here means the candidate hit degenerates to an edge case.
        if (tVec.isParallel(edge1)) return null;
        Vector qVec = tVec.crossProduct(edge1);
        double v = alignZero(direction.dotProduct(qVec) * inverseDeterminant);
        if (v <= 0 || alignZero(u + v) >= 1) return null;

        // t is the signed distance along the ray direction.
        // Only positive t values represent intersections in front of the ray.
        double t = alignZero(edge2.dotProduct(qVec) * inverseDeterminant);
        return t <= 0 ? null : List.of(ray.getPoint(t));
    }

}

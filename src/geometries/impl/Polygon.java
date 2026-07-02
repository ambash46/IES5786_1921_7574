package geometries.impl;

import static geometries.api.Intersectable.Intersection;

import geometries.api.Geometry;
import java.util.List;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

/**
 * Represents a convex polygon in a 3D Cartesian coordinate system.
 * <p>
 * The polygon is defined by an ordered sequence of vertices.
 * All vertices must lie in the same plane and be arranged along the
 * polygon edge path.
 * </p>
 * <p>
 * The polygon must be convex.
 * </p>
 *
 * @author Dan Zilberstein
 */
public class Polygon extends Geometry {
    /**
     * Ordered list of polygon vertices
     */
    protected final List<Point> _vertices;
    /**
     * Plane containing the polygon
     */
    protected final Plane _plane;
    /**
     * Number of vertices
     */
    private final int _size;

    /**
     * Constructs a convex polygon from ordered vertices.
     * <p>
     * The vertices must:
     * </p>
     * <ul>
     * <li>Contain at least three points</li>
     * <li>Be ordered along the polygon edge path</li>
     * <li>Lie in the same plane</li>
     * <li>Form a convex polygon</li>
     * </ul>
     *
     * @param vertices polygon vertices in edge order
     * @throws IllegalArgumentException if the vertices do not form a valid convex
     *                                  polygon
     */
    public Polygon(Point... vertices) {
        if (vertices.length < 3)
            throw new IllegalArgumentException("A polygon can't have less than 3 vertices");
        _vertices = List.of(vertices);
        _size = vertices.length;

        // Check for duplicate vertices
        for (int i = 0; i < _size; i++)
            for (int j = i + 1; j < _size; j++)
                if (vertices[i].equals(vertices[j]))
                    throw new IllegalArgumentException("Polygon vertices must be distinct (duplicate at indices " + i + " and " + j + ")");

        // Create the supporting plane using the first three vertices.
        // The plane stores the constant normal of the polygon.
        _plane = new Plane(vertices[0], vertices[1], vertices[2]);
        if (_size == 3) return; // no need for more tests for a Triangle

        Vector n = _plane.getNormal(vertices[0]);
        // Subtracting identical vertices would create a zero vector (illegal)
        Vector edge1 = vertices[_size - 1].subtract(vertices[_size - 2]);
        Vector edge2 = vertices[0].subtract(vertices[_size - 1]);

        // Cross product of consecutive edges determines orientation.
        // All edge pairs must produce the same sign relative to the normal,
        // otherwise the polygon is concave or vertices are unordered.
        boolean positive = alignZero(edge1.crossProduct(edge2).dotProduct(n)) > 0;
        for (var i = 1; i < _size; ++i) {
            // Test that the point is in the same plane as calculated originally
            if (!isZero(vertices[i].subtract(vertices[0]).dotProduct(n)))
                throw new IllegalArgumentException("All vertices of a polygon must lie in the same plane");
            // Test that consecutive edge cross products maintain the same orientation.
            edge1 = edge2;
            edge2 = vertices[i].subtract(vertices[i - 1]);
            if (positive != (alignZero(edge1.crossProduct(edge2).dotProduct(n)) > 0))
                throw new IllegalArgumentException("All vertices must be ordered and the polygon must be convex");
        }
    }

    /**
     * Returns the normal vector of the polygon at the given point.
     * <p>
     * Delegates to the supporting plane, since the polygon is flat.
     * </p>
     *
     * @param point a point on the polygon (used by the plane delegate)
     * @return the unit normal vector of the polygon's plane
     */
    @Override
    public Vector getNormal(Point point) {
        return _plane.getNormal(point);
    }

    @Override
    protected geometries.api.AABB calcBoundingBox() {
        return geometries.api.AABB.fromPoints(_vertices.toArray(new Point[0]));
    }

    /**
     * Finds all intersection points between the polygon and the given ray.
     * <p>
     * The algorithm works in two stages.
     * First, the ray is intersected with the polygon's supporting plane.
     * If there is no plane intersection, there is no polygon intersection.
     * Second, the candidate point is tested against every polygon edge:
     * for a convex polygon, the cross products between each edge and the vector
     * from that edge to the candidate point must all have the same orientation
     * relative to the polygon normal.
     * Points on edges, on vertices, or on edge extensions are rejected.
     * </p>
     *
     * @param ray the ray to intersect with
     * @return a list containing the plane intersection point if it lies strictly
     * inside the polygon, or {@code null} otherwise
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        // Step 1: intersect with the infinite supporting plane (respects maxDistance).
        var planeHits = _plane.calcIntersections(ray, maxDistance);
        if (planeHits == null) return null;

        Point intersection = planeHits.getFirst().point;
        Vector normal = _plane.getNormal(intersection);
        boolean positive = false;

        // Step 2: verify that the candidate point stays on the same side of
        // every directed edge. A sign flip means the point is outside.
        for (int i = 0; i < _size; i++) {
            Point current = _vertices.get(i);
            Point next = _vertices.get((i + 1) % _size);

            // Exact vertex hits are considered boundary cases and are rejected.
            if (intersection.equals(current) || intersection.equals(next)) return null;

            Vector edge = next.subtract(current);
            Vector toIntersection = intersection.subtract(current);
            // If the candidate lies on the edge line, the hit is on an edge or
            // its extension, so it is not considered an interior intersection.
            if (edge.isParallel(toIntersection)) return null;

            double sign = alignZero(edge.crossProduct(toIntersection).dotProduct(normal));

            if (isZero(sign)) return null;
            if (i == 0) {
                positive = sign > 0;
            } else if (positive != (sign > 0)) {
                return null;
            }
        }

        return List.of(new Intersection(this, intersection));
    }

    /**
     * Compares this polygon with another object.
     *
     * @param obj the object to compare with
     * @return {@code true} if the other object is a polygon with equal
     * vertices and size
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Polygon other = (Polygon) obj;
        if (_size != other._size) return false;

        int startIndex = other._vertices.indexOf(_vertices.getFirst());
        if (startIndex < 0) return false;

        for (int i = 0; i < _size; i++) {
            if (!_vertices.get(i).equals(other._vertices.get((startIndex + i) % _size))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the index of the vertex that sorts first under
     * {@link #compareQuantized(Double3, Double3)} — a canonical starting
     * point for hashing that is the same regardless of which cyclic rotation
     * of the vertex list the polygon was constructed with, matching
     * {@link #equals(Object)}'s cyclic-rotation tolerance.
     *
     * @return the index of the canonical starting vertex
     */
    private int canonicalStartIndex() {
        int best = 0;
        for (int i = 1; i < _size; i++)
            if (compareQuantized(_vertices.get(i).getCoordinates(), _vertices.get(best).getCoordinates()) < 0)
                best = i;
        return best;
    }

    /**
     * Lexicographically compares two quantized triads.
     *
     * @param  a first triad
     * @param  b second triad
     * @return   negative, zero, or positive per the usual comparator contract
     */
    private static int compareQuantized(Double3 a, Double3 b) {
        int c = Long.compare(Double3.quantize(a._d1()), Double3.quantize(b._d1()));
        if (c != 0) return c;
        c = Long.compare(Double3.quantize(a._d2()), Double3.quantize(b._d2()));
        if (c != 0) return c;
        return Long.compare(Double3.quantize(a._d3()), Double3.quantize(b._d3()));
    }

    /**
     * Returns a hash code for this polygon.
     * <p>
     * Mirrors {@link #equals(Object)}: hashes the vertices starting from a
     * canonical (rotation-independent) index instead of relying on the
     * vertex list's own position-dependent {@code hashCode()}, so cyclic
     * rotations of the same vertex sequence — accepted as equal — also hash
     * identically.
     *
     * @return the hash code of the vertices (in canonical order) and size
     */
    @Override
    public int hashCode() {
        int start = canonicalStartIndex();
        int result = _size;
        for (int i = 0; i < _size; i++) {
            Double3 c = _vertices.get((start + i) % _size).getCoordinates();
            result = 31 * result + Long.hashCode(Double3.quantize(c._d1()));
            result = 31 * result + Long.hashCode(Double3.quantize(c._d2()));
            result = 31 * result + Long.hashCode(Double3.quantize(c._d3()));
        }
        return result;
    }

    /**
     * Returns a string representation of this polygon.
     *
     * @return the polygon vertices
     */
    @Override
    public String toString() {
        return "Polygon{vertices=" + _vertices + "}";
    }
}

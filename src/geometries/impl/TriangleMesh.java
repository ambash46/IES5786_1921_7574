package geometries.impl;

import static primitives.Util.alignZero;
import static primitives.Util.isZero;

import geometries.api.Geometry;
import geometries.api.Intersectable.Intersection;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A surface made of triangular faces defined by a shared vertex list and a
 * list of index triples (faces). Supports smooth (Phong) shading: the normal
 * at each vertex is the average of the normals of all triangles that share it,
 * and the normal at an interior hit point is the inverse-distance-weighted
 * average of its face's three vertex normals.
 *
 * <p>Example:
 * <pre>{@code
 * List<Point> verts = List.of(
 *     new Point(0,0,0), new Point(1,0,0),
 *     new Point(1,1,0), new Point(0,1,0)
 * );
 * int[][] faces = { {0,1,2}, {0,2,3} };   // quad split into two triangles
 * TriangleMesh mesh = new TriangleMesh(verts, faces)
 *         .setMaterial(new Material().setKD(0.5).setKS(0.3).setShininess(40));
 * }</pre>
 *
 * @author Ambash and Elyasaf
 */
public class TriangleMesh extends Geometry {

    /** Positions of all vertices in the mesh. */
    private final Point[]    _vertices;

    /**
     * Index triples: {@code _faceIndices[f]} holds the three vertex indices of
     * face {@code f}, in the same order that the corresponding Triangle was built.
     */
    private final int[][]    _faceIndices;

    /** One Triangle object per face, in the same order as {@link #_faceIndices}. */
    private final Triangle[] _triangles;

    /**
     * Smooth (averaged) normal for each vertex, precomputed at construction.
     * {@code _vertexNormals[i]} is the unit vector obtained by averaging the
     * flat normals of every triangle that contains vertex {@code i}.
     */
    private final Vector[]   _vertexNormals;

    // ─── Constructors ────────────────────────────────────────────────────────

    /**
     * Constructs a triangle mesh from a vertex list and a face-index table.
     *
     * @param vertices ordered list of vertex positions
     * @param faces    each element is an {@code int[3]} whose values are indices
     *                 into {@code vertices}; every triple defines one triangle
     * @throws IllegalArgumentException if any argument is {@code null}, if the
     *                                  face list is empty, if any face does not
     *                                  have exactly 3 indices, or if any index is
     *                                  out of range
     */
    public TriangleMesh(List<Point> vertices, int[][] faces) {
        if (vertices == null)  throw new IllegalArgumentException("vertices must not be null");
        if (faces    == null)  throw new IllegalArgumentException("faces must not be null");
        if (faces.length == 0) throw new IllegalArgumentException("mesh must have at least one face");

        _vertices    = vertices.toArray(new Point[0]);
        _faceIndices = new int[faces.length][];
        _triangles   = new Triangle[faces.length];

        for (int i = 0; i < faces.length; i++) {
            if (faces[i].length != 3)
                throw new IllegalArgumentException("each face must have exactly 3 vertex indices");
            _faceIndices[i] = faces[i].clone();
            _triangles[i]   = new Triangle(
                _vertices[faces[i][0]],
                _vertices[faces[i][1]],
                _vertices[faces[i][2]]
            );
        }

        _vertexNormals = buildVertexNormals();
    }

    /**
     * Convenience overload that accepts the face list as a {@code List<int[]>}.
     *
     * @param vertices ordered list of vertex positions
     * @param faces    each element is an {@code int[3]} face
     */
    public TriangleMesh(List<Point> vertices, List<int[]> faces) {
        this(vertices, faces == null ? null : faces.toArray(new int[0][]));
    }

    /**
     * Constructs a triangle mesh from an ordered vertex list alone, using fan
     * triangulation anchored at vertex 0.
     * <p>
     * The vertices must be given in order (CCW or CW) around the surface, and
     * the shape must be convex so that vertex 0 can "see" every other vertex.
     * N vertices produce N-2 triangles: {0,1,2}, {0,2,3}, …, {0,N-2,N-1}.
     * </p>
     *
     * @param vertices ordered list of vertex positions (at least 3)
     * @throws IllegalArgumentException if {@code vertices} is {@code null} or
     *                                  has fewer than 3 points
     */
    public TriangleMesh(List<Point> vertices) {
        this(vertices, fanFaces(vertices == null ? 0 : vertices.size()));
    }

    /**
     * Builds the face-index table for a fan triangulation anchored at vertex 0.
     *
     * @param n number of vertices
     * @return array of {@code n-2} faces, each an {@code int[3]}
     */
    private static int[][] fanFaces(int n) {
        if (n < 3) throw new IllegalArgumentException("fan triangulation needs at least 3 vertices");
        int[][] faces = new int[n - 2][];
        for (int k = 0; k < n - 2; k++)
            faces[k] = new int[]{0, k + 1, k + 2};
        return faces;
    }

    // ─── Geometry ────────────────────────────────────────────────────────────

    /**
     * Returns the smooth normal at the given point.
     *
     * <p>The point must lie on one of the mesh's triangular faces (as is
     * guaranteed for any intersection point produced by this mesh).
     * The method identifies the containing face, then interpolates its three
     * precomputed vertex normals using inverse-distance weights:
     * <pre>
     *   w_i = 1 / distance(point, vertex_i)
     *   normal = normalize( w0·n0 + w1·n1 + w2·n2 )
     * </pre>
     * If the point coincides exactly with a vertex the vertex's own normal is
     * returned directly (avoids division by zero).
     *
     * @param point a point on the mesh surface
     * @return interpolated unit normal at {@code point}
     */
    @Override
    public Vector getNormal(Point point) {
        for (int f = 0; f < _triangles.length; f++) {
            int[] idx = _faceIndices[f];
            Point v0  = _vertices[idx[0]];
            Point v1  = _vertices[idx[1]];
            Point v2  = _vertices[idx[2]];

            if (!isPointInFace(point, v0, v1, v2)) continue;

            Vector n0 = _vertexNormals[idx[0]];
            Vector n1 = _vertexNormals[idx[1]];
            Vector n2 = _vertexNormals[idx[2]];

            double d0 = point.distance(v0);
            double d1 = point.distance(v1);
            double d2 = point.distance(v2);

            // Point coincides with a vertex → return that vertex's normal directly.
            if (isZero(d0)) return n0;
            if (isZero(d1)) return n1;
            if (isZero(d2)) return n2;

            double w0 = 1.0 / d0;
            double w1 = 1.0 / d1;
            double w2 = 1.0 / d2;

            return n0.scale(w0).add(n1.scale(w1)).add(n2.scale(w2)).normalize();
        }
        // Fallback: not reached for valid ray-intersection points.
        return _triangles[0].getNormal(point);
    }

    // ─── Intersectable ───────────────────────────────────────────────────────

    /**
     * Collects ray–triangle intersections from all faces. Each returned
     * {@link Intersection} reports {@code this} mesh as its geometry, so the
     * mesh's own material and emission are applied uniformly across all faces.
     *
     * @param ray the ray to test
     * @return list of intersections, or {@code null} if the ray misses every face
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> result = null;
        for (Triangle triangle : _triangles) {
            List<Intersection> hits = triangle.calcIntersections(ray, maxDistance);
            if (hits != null) {
                if (result == null) result = new ArrayList<>();
                for (Intersection hit : hits)
                    result.add(new Intersection(this, hit.point));
            }
        }
        return result;
    }

    // ─── Subdivision ─────────────────────────────────────────────────────────

    /**
     * Returns a new, denser mesh where every triangle has been uniformly
     * subdivided into {@code n²} smaller triangles.
     * <p>
     * Each edge is split into {@code n} equal segments; the resulting grid
     * points are shared between adjacent faces so smooth vertex-normals remain
     * continuous across seams.  The returned mesh has the same shape as this
     * one but carries no emission or material — set those on the result as
     * needed.
     * </p>
     *
     * <pre>
     *  n=1 → 1 triangle (identity)   n=2 → 4   n=3 → 9   n=4 → 16
     * </pre>
     *
     * @param n subdivision factor (≥ 2)
     * @return a new {@link TriangleMesh} with {@code n²} triangles per original face
     * @throws IllegalArgumentException if {@code n < 2}
     */
    public TriangleMesh subdivide(int n) {
        if (n < 2) throw new IllegalArgumentException("subdivision factor must be at least 2");

        int F = _faceIndices.length;
        List<Point> verts    = new ArrayList<>();
        List<int[]> newFaces = new ArrayList<>(F * n * n);
        // Key: (lo << 32 | hi) → n-1 intermediate point indices, stored lo→hi
        Map<Long, int[]> edgePts = new HashMap<>(3 * F);

        for (Point v : _vertices) verts.add(v);

        for (int f = 0; f < F; f++) {
            int va = _faceIndices[f][0], vb = _faceIndices[f][1], vc = _faceIndices[f][2];

            // grid[r][c] = global vertex index; r+c ≤ n
            // grid[0][0]=va, grid[0][n]=vb, grid[n][0]=vc
            int[][] grid = new int[n + 1][];
            for (int r = 0; r <= n; r++) grid[r] = new int[n + 1 - r];

            // Border: three edges
            for (int k = 0; k <= n; k++) {
                grid[0][k]     = edgePt(va, vb, k, n, verts, edgePts); // va→vb
                grid[k][0]     = edgePt(va, vc, k, n, verts, edgePts); // va→vc
                grid[k][n - k] = edgePt(vb, vc, k, n, verts, edgePts); // vb→vc
            }

            // Interior: r=1..n-2, c=1..n-1-r  (strictly inside, not on any edge)
            Point   p0  = _vertices[va];
            Vector e01 = _vertices[vb].subtract(p0);
            Vector e02 = _vertices[vc].subtract(p0);
            for (int r = 1; r <= n - 2; r++) {
                for (int c = 1; c <= n - 1 - r; c++) {
                    grid[r][c] = verts.size();
                    verts.add(p0.add(e01.scale((double) c / n))
                                .add(e02.scale((double) r / n)));
                }
            }

            // n² triangles: upward-pointing + downward fill-ins
            for (int r = 0; r < n; r++) {
                for (int c = 0; c < n - r; c++) {
                    newFaces.add(new int[]{grid[r][c], grid[r][c + 1], grid[r + 1][c]});
                    if (c < n - 1 - r)
                        newFaces.add(new int[]{grid[r][c + 1], grid[r + 1][c + 1], grid[r + 1][c]});
                }
            }
        }

        return new TriangleMesh(verts, newFaces);
    }

    /**
     * Returns the global vertex index of the {@code k}-th point (0 ≤ k ≤ n)
     * along the directed edge {@code va → vb}.  k=0 gives {@code va}, k=n
     * gives {@code vb}; intermediate points are allocated on first use and
     * stored canonically from {@code min(va,vb)} to {@code max(va,vb)}.
     */
    private int edgePt(int va, int vb, int k, int n,
                       List<Point> verts, Map<Long, int[]> edgePts) {
        if (k == 0) return va;
        if (k == n) return vb;
        int  lo  = Math.min(va, vb), hi = Math.max(va, vb);
        long key = (long) lo << 32 | hi;
        int[] pts = edgePts.get(key);
        if (pts == null) {
            pts = new int[n - 1];
            Vector edgeVec = _vertices[hi].subtract(_vertices[lo]);
            for (int i = 1; i < n; i++) {
                pts[i - 1] = verts.size();
                verts.add(_vertices[lo].add(edgeVec.scale((double) i / n)));
            }
            edgePts.put(key, pts);
        }
        // pts[i-1] = point at i/n from lo→hi; reverse if va>vb
        return (va < vb) ? pts[k - 1] : pts[n - 1 - k];
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    /**
     * Precomputes one smooth normal per vertex by accumulating the flat normals
     * of every triangle that shares that vertex and normalizing the sum.
     *
     * @return array of unit normals, indexed by vertex index
     */
    private Vector[] buildVertexNormals() {
        int n    = _vertices.length;
        double[] accX = new double[n];
        double[] accY = new double[n];
        double[] accZ = new double[n];

        for (int f = 0; f < _triangles.length; f++) {
            // getNormal returns the constant flat normal for the whole triangle.
            Vector triNormal = _triangles[f].getNormal(_vertices[_faceIndices[f][0]]);
            double nx = triNormal.dotProduct(Vector.AXIS_X);
            double ny = triNormal.dotProduct(Vector.AXIS_Y);
            double nz = triNormal.dotProduct(Vector.AXIS_Z);
            for (int vi : _faceIndices[f]) {
                accX[vi] += nx;
                accY[vi] += ny;
                accZ[vi] += nz;
            }
        }

        Vector[] normals = new Vector[n];
        for (int i = 0; i < n; i++) {
            // A vertex with no faces produces a zero accumulator; use a safe default.
            if (isZero(accX[i]) && isZero(accY[i]) && isZero(accZ[i]))
                normals[i] = Vector.AXIS_Y;
            else
                normals[i] = new Vector(accX[i], accY[i], accZ[i]).normalize();
        }
        return normals;
    }

    /**
     * Tests whether {@code p} lies inside (or on the boundary of) the triangle
     * {@code v0}–{@code v1}–{@code v2} using the same-side cross-product test.
     *
     * @param p  point to test
     * @param v0 first vertex
     * @param v1 second vertex
     * @param v2 third vertex
     * @return {@code true} if {@code p} is inside the triangle
     */
    private static boolean isPointInFace(Point p, Point v0, Point v1, Point v2) {
        try {
            Vector n = v1.subtract(v0).crossProduct(v2.subtract(v0));
            // Coplanarity guard: reject points that lie off this triangle's plane.
            if (!isZero(alignZero(p.subtract(v0).dotProduct(n)))) return false;
            double d0 = alignZero(v1.subtract(v0).crossProduct(p.subtract(v0)).dotProduct(n));
            double d1 = alignZero(v2.subtract(v1).crossProduct(p.subtract(v1)).dotProduct(n));
            double d2 = alignZero(v0.subtract(v2).crossProduct(p.subtract(v2)).dotProduct(n));
            return d0 >= 0 && d1 >= 0 && d2 >= 0;
        } catch (IllegalArgumentException e) {
            // p coincides with a vertex or lies on a degenerate edge → not a match here.
            return false;
        }
    }
}

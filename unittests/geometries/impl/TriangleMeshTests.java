package geometries.impl;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for class {@link TriangleMesh}.
 * <p>
 * Tests verify:
 * <ul>
 *   <li>{@link TriangleMesh#TriangleMesh(List, int[][])}</li>
 *   <li>{@link TriangleMesh#getNormal(Point)}</li>
 *   <li>{@link TriangleMesh#findIntersections(Ray)}</li>
 * </ul>
 * Tests follow the methodology of Equivalence Partitions (EP) and
 * Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
@Disabled("TriangleMesh tests are temporarily disabled")
class TriangleMeshTests {

    /** Default constructor to satisfy JavaDoc generator. */
    TriangleMeshTests() { /* to satisfy JavaDoc generator */ }

    // ─── Flat quad in the XY plane (z = 0) ──────────────────────────────────
    // v0=(0,0,0)  v1=(2,0,0)  v2=(2,2,0)  v3=(0,2,0)
    // Face 0: {0,1,2} = lower-right triangle
    // Face 1: {0,2,3} = upper-left  triangle

    private static final List<Point> QUAD_VERTS = List.of(
            new Point(0, 0, 0), new Point(2, 0, 0),
            new Point(2, 2, 0), new Point(0, 2, 0)
    );
    private static final int[][] QUAD_FACES = {{0, 1, 2}, {0, 2, 3}};

    // ─── Two parallel quads forming a "tunnel" (front z=0, back z=-4) ────────
    // Used for two-hit intersection tests.

    private static final List<Point> TUNNEL_VERTS = List.of(
            new Point(0, 0,  0), new Point(2, 0,  0),
            new Point(2, 2,  0), new Point(0, 2,  0),
            new Point(0, 0, -4), new Point(2, 0, -4),
            new Point(2, 2, -4), new Point(0, 2, -4)
    );
    private static final int[][] TUNNEL_FACES = {
            {0, 1, 2}, {0, 2, 3},   // front face (z = 0)
            {4, 5, 6}, {4, 6, 7}    // back  face (z = -4)
    };

    /** Floating-point comparison tolerance. */
    private static final double DELTA = 1e-6;

    // ─── Error messages ──────────────────────────────────────────────────────

    private static final String ERR_CTOR        = "TriangleMesh should accept valid vertices and faces";
    private static final String ERR_NULL_VERTS  = "TriangleMesh should reject null vertex list";
    private static final String ERR_NULL_FACES  = "TriangleMesh should reject null face array";
    private static final String ERR_EMPTY_FACES = "TriangleMesh should reject an empty face list";
    private static final String ERR_BAD_FACE    = "TriangleMesh should reject a face that is not a triple";
    private static final String ERR_NORMAL_LEN  = "getNormal() must return a unit vector";
    private static final String ERR_NORMAL_PERP = "getNormal() must be perpendicular to both face edges";
    private static final String ERR_HIT         = "findIntersections() returned wrong result";

    // ═══════════════════════════════════════════════════════════════════════
    //  Constructor
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link TriangleMesh#TriangleMesh(List, int[][])}.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Valid two-triangle quad mesh — must not throw
        assertDoesNotThrow(() -> new TriangleMesh(QUAD_VERTS, QUAD_FACES), ERR_CTOR);

        // =============== Boundary Values Tests ==================

        // TC11: Null vertex list
        assertThrows(IllegalArgumentException.class,
                () -> new TriangleMesh(null, QUAD_FACES), ERR_NULL_VERTS);

        // TC12: Null face array
        assertThrows(IllegalArgumentException.class,
                () -> new TriangleMesh(QUAD_VERTS, (int[][]) null), ERR_NULL_FACES);

        // TC13: Empty face array (no triangles)
        assertThrows(IllegalArgumentException.class,
                () -> new TriangleMesh(QUAD_VERTS, new int[0][]), ERR_EMPTY_FACES);

        // TC14: Face with only two indices (not a triangle)
        assertThrows(IllegalArgumentException.class,
                () -> new TriangleMesh(QUAD_VERTS, new int[][]{{0, 1}}), ERR_BAD_FACE);

        // TC15: Face with four indices (not a triangle)
        assertThrows(IllegalArgumentException.class,
                () -> new TriangleMesh(QUAD_VERTS, new int[][]{{0, 1, 2, 3}}), ERR_BAD_FACE);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  getNormal
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link TriangleMesh#getNormal(Point)}.
     * <p>
     * For a flat mesh every vertex normal equals the face normal, so the
     * interpolated result must also equal the face normal (0,0,±1).
     * </p>
     */
    @Test
    void testGetNormal() {
        TriangleMesh mesh = new TriangleMesh(QUAD_VERTS, QUAD_FACES);

        // ============ Equivalence Partitions Tests ==============

        // TC01: Interior point of face 0 — normal must be unit and perpendicular to XY
        Point pFace0 = new Point(1.5, 0.5, 0);
        Vector n0 = mesh.getNormal(pFace0);
        assertEquals(1.0, n0.length(), DELTA, ERR_NORMAL_LEN);
        assertEquals(0.0, n0.dotProduct(new Vector(1, 0, 0)), DELTA, ERR_NORMAL_PERP);
        assertEquals(0.0, n0.dotProduct(new Vector(0, 1, 0)), DELTA, ERR_NORMAL_PERP);

        // TC02: Interior point of face 1 — same expectations
        Point pFace1 = new Point(0.5, 1.5, 0);
        Vector n1 = mesh.getNormal(pFace1);
        assertEquals(1.0, n1.length(), DELTA, ERR_NORMAL_LEN);
        assertEquals(0.0, n1.dotProduct(new Vector(1, 0, 0)), DELTA, ERR_NORMAL_PERP);
        assertEquals(0.0, n1.dotProduct(new Vector(0, 1, 0)), DELTA, ERR_NORMAL_PERP);

        // TC03: Both normals must point in the same direction (consistent face orientation)
        assertEquals(n0, n1, "Both faces lie in the same plane; their normals must be equal");

        // =============== Boundary Values Tests ==================

        // TC11: Smooth normals on a bent mesh — vertex normal must NOT equal either flat face normal
        //
        // Bent mesh: two triangles sharing edge v0-v1, one in XZ plane, one in XY plane.
        //   T1: v0=(0,0,0), v1=(2,0,0), v2=(1,0,1)  →  flat normal = (0,-1,0)
        //   T2: v0=(0,0,0), v1=(2,0,0), v3=(1,1,0)  →  flat normal = (0,0,1)
        // Vertex normal at v0 and v1 = normalize((0,-1,0)+(0,0,1)) = (0,-1/√2,1/√2)
        // An interior point in T2 will be pulled toward those blended vertex normals.
        List<Point> bentVerts = List.of(
                new Point(0, 0, 0),  // v0 — shared
                new Point(2, 0, 0),  // v1 — shared
                new Point(1, 0, 1),  // v2 — T1 only
                new Point(1, 1, 0)   // v3 — T2 only
        );
        int[][] bentFaces = {{0, 1, 2}, {0, 1, 3}};  // share edge v0-v1
        TriangleMesh bentMesh = new TriangleMesh(bentVerts, bentFaces);

        // Point strictly inside T2:
        // T2 vertices: (0,0,0),(2,0,0),(1,1,0). Centre ≈ (1, 1/3, 0).
        Point pBent = new Point(1, 0.333, 0);
        Vector nBent = bentMesh.getNormal(pBent);
        assertEquals(1.0, nBent.length(), DELTA, ERR_NORMAL_LEN);

        // The smooth normal must differ from both flat face normals (0,-1,0) and (0,0,1).
        assertNotEquals(new Vector(0, -1, 0), nBent,
                "Smooth normal at a bent-mesh interior must be blended, not the raw face normal");
        assertNotEquals(new Vector(0, 0, 1), nBent,
                "Smooth normal at a bent-mesh interior must be blended, not the raw face normal");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  findIntersections
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Test method for {@link TriangleMesh#findIntersections(Ray)}.
     * <p>
     * Tests are organised into three groups:
     * <ol>
     *   <li>Ray perpendicular to the mesh plane</li>
     *   <li>Ray parallel to the mesh plane (always misses)</li>
     *   <li>Two-face hit (tunnel mesh)</li>
     * </ol>
     */
    @Test
    void testFindIntersections() {
        TriangleMesh quad   = new TriangleMesh(QUAD_VERTS,   QUAD_FACES);
        TriangleMesh tunnel = new TriangleMesh(TUNNEL_VERTS, TUNNEL_FACES);

        // ============ Group 1: Ray perpendicular to mesh plane ============
        // ============ Equivalence Partitions Tests ==============

        // TC01: Ray from below hits face-0 interior → 1 point
        assertEquals(List.of(new Point(1, 0.5, 0)),
                quad.findIntersections(new Ray(new Point(1, 0.5, -1), new Vector(0, 0, 1))),
                ERR_HIT);

        // TC02: Ray from below hits face-1 interior → 1 point
        assertEquals(List.of(new Point(0.5, 1.5, 0)),
                quad.findIntersections(new Ray(new Point(0.5, 1.5, -1), new Vector(0, 0, 1))),
                ERR_HIT);

        // TC03: Ray from above (negative direction) hits face-0 interior → 1 point
        assertEquals(List.of(new Point(1, 0.5, 0)),
                quad.findIntersections(new Ray(new Point(1, 0.5, 1), new Vector(0, 0, -1))),
                ERR_HIT);

        // TC04: Ray completely misses the mesh → null
        assertNull(quad.findIntersections(new Ray(new Point(5, 5, -1), new Vector(0, 0, 1))),
                ERR_HIT);

        // =============== Boundary Values Tests ==================

        // TC11: Ray hits the shared edge between face-0 and face-1
        //       Möller–Trumbore rejects boundary points → null
        assertNull(quad.findIntersections(new Ray(new Point(1, 1, -1), new Vector(0, 0, 1))),
                ERR_HIT);

        // TC12: Ray hits a mesh vertex → null (boundary rejection)
        assertNull(quad.findIntersections(new Ray(new Point(2, 2, -1), new Vector(0, 0, 1))),
                ERR_HIT);

        // TC13: Ray origin lies on the mesh plane → t = 0 is excluded → null
        assertNull(quad.findIntersections(new Ray(new Point(1, 0.5, 0), new Vector(0, 0, 1))),
                ERR_HIT);

        // ============ Group 2: Ray parallel to mesh plane ============
        // ============ Equivalence Partitions Tests ==============

        // TC21: Ray travels in X-direction above the mesh plane → null
        assertNull(quad.findIntersections(new Ray(new Point(-1, 0.5, 1), new Vector(1, 0, 0))),
                ERR_HIT);

        // TC22: Ray travels in Y-direction coplanar with the mesh → null
        assertNull(quad.findIntersections(new Ray(new Point(-1, -1, 0), new Vector(0, 1, 0))),
                ERR_HIT);

        // ============ Group 3: Two-face hit (tunnel mesh) ============
        // ============ Equivalence Partitions Tests ==============

        // TC31: Ray passes through both parallel faces → 2 points
        List<Point> twoHits = tunnel.findIntersections(
                new Ray(new Point(1, 0.5, 2), new Vector(0, 0, -1)));
        assertNotNull(twoHits, ERR_HIT);
        assertEquals(2, twoHits.size(), ERR_HIT);
        assertTrue(twoHits.contains(new Point(1, 0.5,  0)), ERR_HIT);
        assertTrue(twoHits.contains(new Point(1, 0.5, -4)), ERR_HIT);

        // =============== Boundary Values Tests ==================

        // TC32: Ray passes through both faces — starts exactly on the front face → 1 point
        List<Point> oneHit = tunnel.findIntersections(
                new Ray(new Point(1, 0.5, 0), new Vector(0, 0, -1)));
        // Front face: t = 0 (excluded). Back face: t = 4 → 1 point.
        assertNotNull(oneHit, ERR_HIT);
        assertEquals(1, oneHit.size(), ERR_HIT);
        assertEquals(new Point(1, 0.5, -4), oneHit.get(0), ERR_HIT);
    }
}

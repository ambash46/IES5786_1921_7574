package geometries.impl;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Polygon}.
 * The tests verify:
 * <ul>
 * <li>Polygon constructor validity</li>
 * <li>{@link Polygon#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 */
class PolygonTests {
    /**
     * Applies an affine transform that maps the easy plane z=1
     * to the general plane z=x+2y+1.
     *
     * @param x original x coordinate
     * @param y original y coordinate
     * @param z original z coordinate
     * @return transformed point
     */
    private static Point p(double x, double y, double z) {
        return new Point(x, y, z + x + 2 * y);
    }

    /**
     * Applies the linear part of the affine transform used for test rays.
     *
     * @param x original x component
     * @param y original y component
     * @param z original z component
     * @return transformed vector
     */
    private static Vector v(double x, double y, double z) {
        return new Vector(x, y, z + x + 2 * y);
    }

    /**
     * Default constructor to satisfy JavaDoc generator
     */
    PolygonTests() { /* to satisfy JavaDoc generator */ }

    /**
     * Vertex (1,0,0) used in polygon tests
     */
    private static final Point POINT_X = new Point(1, 0, 0);
    /**
     * Vertex (0,1,0) used in polygon tests
     */
    private static final Point POINT_Y = new Point(0, 1, 0);
    /**
     * Vertex (0,0,1) used in polygon tests
     */
    private static final Point POINT_Z = new Point(0, 0, 1);

    /**
     * Additional vertex used for valid polygon construction
     */
    private static final Point POINT1 = new Point(-1, 1, 1);
    /**
     * Point not in the polygon plane
     */
    private static final Point POINT2 = new Point(0, 2, 2);
    /**
     * Point that creates a concave polygon
     */
    private static final Point POINT3 = new Point(0.5, 0.25, 0.5);
    /**
     * Point located on one of the polygon edges
     */
    private static final Point POINT4 = new Point(0, 0.5, 0.5);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for wrong plane intersection
     */
    private static final String ERROR_PLANE = "ERROR: wrong intersection with plane";
    /**
     * Error message for wrong polygon intersection
     */
    private static final String ERROR_POLYGON = "ERROR: wrong polygon intersection";

    /**
     * Test method for {@link Polygon#Polygon(Point...)}.
     * Verifies correct and incorrect polygon constructions.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct convex quadrilateral with vertices in correct order
        assertDoesNotThrow(() -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT1),
                "Failed constructing a correct polygon");

        // TC02: Wrong vertices order
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_Y, POINT_X, POINT1),
                "Constructed a polygon with wrong order of vertices");

        // TC03: Vertices not in the same plane
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT2),
                "Constructed a polygon with vertices that are not in the same plane");

        // TC04: Concave quadrilateral
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT3),
                "Constructed a concave polygon");

        // =============== Boundary Values Tests ==================

        // TC11: Vertex on a side
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT4),
                "Constructed a polygon with a vertex on a side");

        // TC12: Last point equals first point
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_Z),
                "Constructed a polygon with duplicate first/last vertex");

        // TC13: Co-located points
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_Y),
                "Constructed a polygon with co-located vertices");
    }

    /**
     * Test method for {@link Polygon#getNormal(Point)}.
     * Verifies that the returned normal vector is unit length and orthogonal
     * to all polygon edges.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        Point[] pts =
                {POINT_Z, POINT_X, POINT_Y, POINT1};
        Polygon polygon = new Polygon(pts);
        // Ensure method does not throw exception
        assertDoesNotThrow(() -> polygon.getNormal(POINT_Z), "getNormal() threw unexpected exception");
        Vector result = polygon.getNormal(POINT_Z);
        // Ensure |n| = 1
        assertEquals(1, result.length(), DELTA, "Polygon normal is not a unit vector");
        // Ensure normal is orthogonal to all edges
        for (int i = 0; i < pts.length; ++i) {
            Vector edge = pts[i].subtract(pts[i == 0 ? pts.length - 1 : i - 1]);
            assertEquals(0d, result.dotProduct(edge), DELTA, "Polygon normal is not orthogonal to an edge");
        }
    }

    /**
     * Test method for {@link Polygon#findIntersections(primitives.Ray)}.
     * Verifies rays perpendicular to the polygon normal
     * (parallel to the polygon plane).
     */
    @Test
    void testFindIntersectionsPerpendicularToNormal() {
        Polygon polygon = new Polygon(
                p(2, 2, 1),
                p(7, 2, 1),
                p(9, 5, 1),
                p(5, 8, 1),
                p(1, 5, 1));

        // ============ Equivalence Partitions Tests ==============

        // A. Ray start above the plane
        // TC01: Projected line passes through the polygon, start projects before the crossing segment
        assertNull(polygon.findIntersections(new Ray(p(1, 4, 2), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC02: Projected line passes through the polygon, start projects inside the crossing segment
        assertNull(polygon.findIntersections(new Ray(p(4, 4, 2), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC03: Projected line passes through the polygon, start projects after the crossing segment
        assertNull(polygon.findIntersections(new Ray(p(9, 4, 2), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC04: Projected line does not pass through the polygon
        assertNull(polygon.findIntersections(new Ray(p(1, 9, 2), v(1, 0, 0))),
                ERROR_POLYGON);

        // B. Ray start below the plane
        // TC05: Projected line passes through the polygon, start projects before the crossing segment
        assertNull(polygon.findIntersections(new Ray(p(1, 4, 3), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC06: Projected line passes through the polygon, start projects inside the crossing segment
        assertNull(polygon.findIntersections(new Ray(p(4, 4, 3), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC07: Projected line passes through the polygon, start projects after the crossing segment
        assertNull(polygon.findIntersections(new Ray(p(9, 4, 3), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC08: Projected line does not pass through the polygon
        assertNull(polygon.findIntersections(new Ray(p(1, 9, 3), v(1, 0, 0))),
                ERROR_POLYGON);

        // =============== Boundary Values Tests ==================

        // C. Ray start on the polygon plane, line crosses two edges
        // TC09: Start before the first edge
        assertNull(polygon.findIntersections(new Ray(p(1, 4, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC10: Start on the first edge
        assertNull(polygon.findIntersections(new Ray(p(4d / 3d, 4, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC11: Start inside the polygon
        assertNull(polygon.findIntersections(new Ray(p(4, 4, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC12: Start on the second edge
        assertNull(polygon.findIntersections(new Ray(p(25d / 3d, 4, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC13: Start after the second edge
        assertNull(polygon.findIntersections(new Ray(p(9, 4, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // D. Ray start on the plane, line enters through a vertex and exits through an edge
        // TC14: Start before the vertex
        assertNull(polygon.findIntersections(new Ray(p(1, 1.25, 1), v(6, 3.5, 0))),
                ERROR_POLYGON);

        // TC15: Start on the entry vertex
        assertNull(polygon.findIntersections(new Ray(p(2, 2, 1), v(6, 3.5, 0))),
                ERROR_POLYGON);

        // TC16: Start inside the polygon
        assertNull(polygon.findIntersections(new Ray(p(5, 3.75, 1), v(6, 3.5, 0))),
                ERROR_POLYGON);

        // TC17: Start on the exit edge
        assertNull(polygon.findIntersections(new Ray(p(8, 5.5, 1), v(6, 3.5, 0))),
                ERROR_POLYGON);

        // TC18: Start after the exit edge
        assertNull(polygon.findIntersections(new Ray(p(9, 6.083333333333333, 1), v(6, 3.5, 0))),
                ERROR_POLYGON);

        // E. Ray start on the plane, line enters through an edge and exits through a vertex
        // TC19: Start before the entry edge
        assertNull(polygon.findIntersections(new Ray(p(9, 6.083333333333333, 1), v(-6, -3.5, 0))),
                ERROR_POLYGON);

        // TC20: Start on the entry edge
        assertNull(polygon.findIntersections(new Ray(p(8, 5.5, 1), v(-6, -3.5, 0))),
                ERROR_POLYGON);

        // TC21: Start inside the polygon
        assertNull(polygon.findIntersections(new Ray(p(5, 3.75, 1), v(-6, -3.5, 0))),
                ERROR_POLYGON);

        // TC22: Start on the exit vertex
        assertNull(polygon.findIntersections(new Ray(p(2, 2, 1), v(-6, -3.5, 0))),
                ERROR_POLYGON);

        // TC23: Start after the exit vertex
        assertNull(polygon.findIntersections(new Ray(p(1, 1.25, 1), v(-6, -3.5, 0))),
                ERROR_POLYGON);

        // F. Ray start on the plane, line does not enter the polygon at all
        // TC24: Entire ray stays outside the polygon
        assertNull(polygon.findIntersections(new Ray(p(1, 9, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // G. Ray start on the plane, ray lies on a polygon edge
        // TC25: Start before the edge
        assertNull(polygon.findIntersections(new Ray(p(1, 2, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC26: Start on the first edge vertex
        assertNull(polygon.findIntersections(new Ray(p(2, 2, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC27: Start inside the edge
        assertNull(polygon.findIntersections(new Ray(p(4, 2, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC28: Start on the second edge vertex
        assertNull(polygon.findIntersections(new Ray(p(7, 2, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC29: Start after the edge
        assertNull(polygon.findIntersections(new Ray(p(8, 2, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // H. Ray start on the plane, line enters through a vertex and exits through a non-adjacent vertex
        // TC30: Start before the first vertex
        assertNull(polygon.findIntersections(new Ray(p(0, 5, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC31: Start on the entry vertex
        assertNull(polygon.findIntersections(new Ray(p(1, 5, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC32: Start inside the polygon between the non-adjacent vertices
        assertNull(polygon.findIntersections(new Ray(p(5, 5, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC33: Start on the non-adjacent exit vertex
        assertNull(polygon.findIntersections(new Ray(p(9, 5, 1), v(1, 0, 0))),
                ERROR_POLYGON);

        // TC34: Start after the non-adjacent exit vertex
        assertNull(polygon.findIntersections(new Ray(p(10, 5, 1), v(1, 0, 0))),
                ERROR_POLYGON);
    }

    /**
     * Test method for {@link Polygon#findIntersections(primitives.Ray)}.
     * Verifies general rays that are neither parallel nor perpendicular
     * to the polygon normal.
     */
    @Test
    void testFindIntersectionsGeneralRay() {
        Polygon polygon = new Polygon(
                p(2, 2, 1),
                p(7, 2, 1),
                p(9, 5, 1),
                p(5, 8, 1),
                p(1, 5, 1));

        Point inside = p(5, 4, 1);
        Point outside = p(10, 4, 1);
        Point onEdge = p(4, 2, 1);
        Point onVertex = p(2, 2, 1);

        Vector plus = v(1, 0, 1);
        Vector minus = v(1, 0, -1);

        // ============ Equivalence Partitions Tests ==============

        // TC01: + direction, start above plane, intersection inside polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(p(6, 4, 2), plus)),
                ERROR_POLYGON);

        // TC02: + direction, start above plane, intersection outside polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(p(11, 4, 2), plus)),
                ERROR_POLYGON);

        // TC03: + direction, start above plane, intersection on polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(p(5, 2, 2), plus)),
                ERROR_POLYGON);

        // TC04: + direction, start above plane, intersection on polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(p(3, 2, 2), plus)),
                ERROR_POLYGON);

        // TC05: + direction, start below plane, intersection inside polygon (1 point)
        assertEquals(java.util.List.of(inside),
                polygon.findIntersections(new Ray(p(4, 4, 0), plus)),
                ERROR_POLYGON);

        // TC06: + direction, start below plane, intersection outside polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(p(9, 4, 0), plus)),
                ERROR_POLYGON);

        // TC07: + direction, start below plane, intersection on polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(p(3, 2, 0), plus)),
                ERROR_POLYGON);

        // TC08: + direction, start below plane, intersection on polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(p(1, 2, 0), plus)),
                ERROR_POLYGON);

        // TC09: - direction, start above plane, intersection inside polygon (1 point)
        assertEquals(java.util.List.of(inside),
                polygon.findIntersections(new Ray(p(4, 4, 2), minus)),
                ERROR_POLYGON);

        // TC10: - direction, start above plane, intersection outside polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(p(9, 4, 2), minus)),
                ERROR_POLYGON);

        // TC11: - direction, start above plane, intersection on polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(p(3, 2, 2), minus)),
                ERROR_POLYGON);

        // TC12: - direction, start above plane, intersection on polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(p(1, 2, 2), minus)),
                ERROR_POLYGON);

        // TC13: - direction, start below plane, intersection inside polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(p(6, 4, 0), minus)),
                ERROR_POLYGON);

        // TC14: - direction, start below plane, intersection outside polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(p(11, 4, 0), minus)),
                ERROR_POLYGON);

        // TC15: - direction, start below plane, intersection on polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(p(5, 2, 0), minus)),
                ERROR_POLYGON);

        // TC16: - direction, start below plane, intersection on polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(p(3, 2, 0), minus)),
                ERROR_POLYGON);

        // =============== Boundary Values Tests ==================

        // TC17: + direction, start on plane inside polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(inside, plus)),
                ERROR_POLYGON);

        // TC18: + direction, start on plane outside polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(outside, plus)),
                ERROR_POLYGON);

        // TC19: + direction, start on plane on polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdge, plus)),
                ERROR_POLYGON);

        // TC20: + direction, start on plane on polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(onVertex, plus)),
                ERROR_POLYGON);

        // TC21: - direction, start on plane inside polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(inside, minus)),
                ERROR_POLYGON);

        // TC22: - direction, start on plane outside polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(outside, minus)),
                ERROR_POLYGON);

        // TC23: - direction, start on plane on polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdge, minus)),
                ERROR_POLYGON);

        // TC24: - direction, start on plane on polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(onVertex, minus)),
                ERROR_POLYGON);
    }
}

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
     * Verifies polygon-ray intersections in three groups:
     * rays parallel to the normal, rays perpendicular to the normal,
     * and general rays.
     */
    @Test
    void testFindIntersections() {
        Polygon polygon = new Polygon(
                new Point(2, 2, 8),
                new Point(7, 2, 13),
                new Point(9, 5, 21),
                new Point(5, 8, 23),
                new Point(1, 5, 13));

        Vector normal = new Vector(1, 2, -1);
        Vector AB = new Vector(1, 0, 1);
        Vector BC = new Vector(2, 3, 8);
        Vector regular = new Vector(1, 0, 0);

        Point inside = new Point(5, 4, 15);
        Point outside = new Point(10, 4, 20);
        Point onEdge = new Point(4, 2, 10);
        Point onVertex = new Point(2, 2, 8);
        Point onEdgeExtension = new Point(8, 2, 14);

        Point overPolygonAbove = inside.add(normal);
        Point outsideAbove = outside.add(normal);
        Point overPolygonBelow = inside.add(normal.scale(-1));
        Point outsideBelow = outside.add(normal.scale(-1));

        // ============ Group 1: Ray parallel to normal ============
        // ============ Equivalence Partitions Tests ==============

        // TC01: Starts above the plane, projected hit is on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdge.add(normal), normal)),
                ERROR_POLYGON);

        // TC02: Starts above the plane, projected hit is on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(onVertex.add(normal), normal)),
                ERROR_POLYGON);

        // TC03: Starts above the plane, projected hit is inside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(inside.add(normal), normal)),
                ERROR_POLYGON);

        // TC04: Starts above the plane, projected hit is outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(outside.add(normal), normal)),
                ERROR_POLYGON);

        // TC05: Starts above the plane, projected hit is on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdgeExtension.add(normal), normal)),
                ERROR_POLYGON);

        // TC06: Starts below the plane, projected hit is on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdge.add(normal.scale(-1)), normal)),
                ERROR_POLYGON);

        // TC07: Starts below the plane, projected hit is on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(onVertex.add(normal.scale(-1)), normal)),
                ERROR_POLYGON);

        // TC08: Starts below the plane, projected hit is inside the polygon (1 point)
        assertEquals(java.util.List.of(inside),
                polygon.findIntersections(new Ray(inside.add(normal.scale(-1)), normal)),
                ERROR_POLYGON);

        // TC09: Starts below the plane, projected hit is outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(outside.add(normal.scale(-1)), normal)),
                ERROR_POLYGON);

        // TC10: Starts below the plane, projected hit is on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdgeExtension.add(normal.scale(-1)), normal)),
                ERROR_POLYGON);

        // =============== Boundary Values Tests ==================

        // TC11: Starts on the plane on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdge, normal)),
                ERROR_POLYGON);

        // TC12: Starts on the plane on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(onVertex, normal)),
                ERROR_POLYGON);

        // TC13: Starts on the plane inside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(inside, normal)),
                ERROR_POLYGON);

        // TC14: Starts on the plane outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(outside, normal)),
                ERROR_POLYGON);

        // TC15: Starts on the plane on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdgeExtension, normal)),
                ERROR_POLYGON);

        // ============ Group 2: Ray perpendicular to normal ============
        // ============ Equivalence Partitions Tests ==============

        // TC16: Starts above the plane, projected line passes over the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(overPolygonAbove, AB)),
                ERROR_POLYGON);

        // TC17: Starts above the plane, projected line does not pass over the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(outsideAbove, BC)),
                ERROR_POLYGON);

        // TC18: Starts below the plane, projected line passes over the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(overPolygonBelow, AB)),
                ERROR_POLYGON);

        // TC19: Starts below the plane, projected line does not pass over the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(outsideBelow, BC)),
                ERROR_POLYGON);

        // =============== Boundary Values Tests ==================

        // TC20: Ray lies on an edge, start before the first vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(1, 2, 7), AB)),
                ERROR_POLYGON);

        // TC21: Ray lies on an edge, start on the first vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(2, 2, 8), AB)),
                ERROR_POLYGON);

        // TC22: Ray lies on an edge, start inside the edge segment (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(4, 2, 10), AB)),
                ERROR_POLYGON);

        // TC23: Ray lies on an edge, start on the second vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(7, 2, 13), AB)),
                ERROR_POLYGON);

        // TC24: Ray lies on an edge, start after the second vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(8, 2, 14), AB)),
                ERROR_POLYGON);

        // TC25: Ray is tangent at a vertex, start before the tangent vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(2, 8, 20), AB)),
                ERROR_POLYGON);

        // TC26: Ray is tangent at a vertex, start on the tangent vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(5, 8, 23), AB)),
                ERROR_POLYGON);

        // TC27: Ray is tangent at a vertex, start after the tangent vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(8, 8, 26), AB)),
                ERROR_POLYGON);

        // TC28: Ray crosses the polygon, start before entering it (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(1, 4, 11), AB)),
                ERROR_POLYGON);

        // TC29: Ray crosses the polygon, start on the first border (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(4d / 3d, 4, 34d / 3d), AB)),
                ERROR_POLYGON);

        // TC30: Ray crosses the polygon, start inside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(4, 4, 14), AB)),
                ERROR_POLYGON);

        // TC31: Ray crosses the polygon, start on the second border (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(25d / 3d, 4, 55d / 3d), AB)),
                ERROR_POLYGON);

        // TC32: Ray crosses the polygon, start after leaving it (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(9, 4, 19), AB)),
                ERROR_POLYGON);

        // TC33: Ray stays outside the polygon entirely (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(1, 9, 21), new Vector(0, 1, 2))),
                ERROR_POLYGON);

        // TC34: Ray is parallel to an edge and does not hit the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(new Point(0, 1, 4), AB)),
                ERROR_POLYGON);

        // ============ Group 3: General ray ============
        // ============ Equivalence Partitions Tests ==============

        // TC35: Starts above the plane, projected hit is inside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(inside.add(regular), regular)),
                ERROR_POLYGON);

        // TC36: Starts above the plane, projected hit is outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(outside.add(regular), regular)),
                ERROR_POLYGON);

        // TC37: Starts above the plane, projected hit is on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdge.add(regular), regular)),
                ERROR_POLYGON);

        // TC38: Starts above the plane, projected hit is on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(onVertex.add(regular), regular)),
                ERROR_POLYGON);

        // TC39: Starts above the plane, projected hit is on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdgeExtension.add(regular), regular)),
                ERROR_POLYGON);

        // TC40: Starts below the plane, projected hit is inside the polygon (1 point)
        assertEquals(java.util.List.of(inside),
                polygon.findIntersections(new Ray(inside.add(regular.scale(-1)), regular)),
                ERROR_POLYGON);

        // TC41: Starts below the plane, projected hit is outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(outside.add(regular.scale(-1)), regular)),
                ERROR_POLYGON);

        // TC42: Starts below the plane, projected hit is on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdge.add(regular.scale(-1)), regular)),
                ERROR_POLYGON);

        // TC43: Starts below the plane, projected hit is on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(onVertex.add(regular.scale(-1)), regular)),
                ERROR_POLYGON);

        // TC44: Starts below the plane, projected hit is on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdgeExtension.add(regular.scale(-1)), regular)),
                ERROR_POLYGON);

        // =============== Boundary Values Tests ==================

        // TC45: Starts on the plane inside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(inside, regular)),
                ERROR_POLYGON);

        // TC46: Starts on the plane outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(outside, regular)),
                ERROR_POLYGON);

        // TC47: Starts on the plane on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdge, regular)),
                ERROR_POLYGON);

        // TC48: Starts on the plane on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(onVertex, regular)),
                ERROR_POLYGON);

        // TC49: Starts on the plane on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(onEdgeExtension, regular)),
                ERROR_POLYGON);
    }
}

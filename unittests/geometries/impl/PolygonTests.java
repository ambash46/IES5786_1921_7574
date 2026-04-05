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
     * Point inside the polygon for intersection tests.
     */
    private static final Point INSIDE_POINT = new Point(5, 4, 15);
    /**
     * Point outside the polygon for intersection tests.
     */
    private static final Point OUTSIDE_POINT = new Point(10, 4, 20);
    /**
     * Point on a polygon edge for intersection tests.
     */
    private static final Point ON_EDGE_POINT = new Point(4, 2, 10);
    /**
     * Vertex used in polygon intersection tests.
     */
    private static final Point INTERSECTION_VERTEX = new Point(2, 2, 8);
    /**
     * Point on an extension of a polygon edge for intersection tests.
     */
    private static final Point ON_EDGE_EXTENSION_POINT = new Point(8, 2, 14);
    /**
     * Point on the AB edge before the first vertex.
     */
    private static final Point AB_BEFORE_FIRST_POINT = new Point(1, 2, 7);
    /**
     * Point on the AB edge at the second vertex.
     */
    private static final Point AB_SECOND_VERTEX_POINT = new Point(7, 2, 13);
    /**
     * Point before the tangent vertex along AB.
     */
    private static final Point TANGENT_BEFORE_VERTEX_POINT = new Point(2, 8, 20);
    /**
     * Point at the tangent vertex.
     */
    private static final Point TANGENT_VERTEX_POINT = new Point(5, 8, 23);
    /**
     * Point after the tangent vertex along AB.
     */
    private static final Point TANGENT_AFTER_VERTEX_POINT = new Point(8, 8, 26);
    /**
     * Point before entering the polygon along the crossing ray.
     */
    private static final Point CROSSING_BEFORE_ENTRY_POINT = new Point(1, 4, 11);
    /**
     * Point on the first border of the crossing ray.
     */
    private static final Point CROSSING_FIRST_BORDER_POINT = new Point(4d / 3d, 4, 34d / 3d);
    /**
     * Point inside the polygon along the crossing ray.
     */
    private static final Point CROSSING_INSIDE_POINT = new Point(4, 4, 14);
    /**
     * Point on the second border of the crossing ray.
     */
    private static final Point CROSSING_SECOND_BORDER_POINT = new Point(25d / 3d, 4, 55d / 3d);
    /**
     * Point after leaving the polygon along the crossing ray.
     */
    private static final Point CROSSING_AFTER_EXIT_POINT = new Point(9, 4, 19);
    /**
     * Point for a crossing ray that stays outside the polygon.
     */
    private static final Point CROSSING_OUTSIDE_POINT = new Point(1, 9, 21);
    /**
     * Point for a ray parallel to an edge and missing the polygon.
     */
    private static final Point PARALLEL_MISS_POINT = new Point(0, 1, 4);
    /**
     * Normal direction used in polygon intersection tests.
     */
    private static final Vector INTERSECTION_NORMAL = new Vector(1, 2, -1);
    /**
     * Direction along the AB line used in polygon intersection tests.
     */
    private static final Vector AB_DIRECTION = new Vector(1, 0, 1);
    /**
     * Direction along the BC line used in polygon intersection tests.
     */
    private static final Vector BC_DIRECTION = new Vector(2, 3, 8);
    /**
     * Direction used for crossing rays that stay outside the polygon.
     */
    private static final Vector OUTSIDE_CROSSING_DIRECTION = new Vector(0, 1, 2);
    /**
     * Point above the polygon plane over the inside point.
     */
    private static final Point ABOVE_INSIDE_POINT = INSIDE_POINT.add(INTERSECTION_NORMAL);
    /**
     * Point above the polygon plane over the outside point.
     */
    private static final Point ABOVE_OUTSIDE_POINT = OUTSIDE_POINT.add(INTERSECTION_NORMAL);
    /**
     * Point below the polygon plane under the inside point.
     */
    private static final Point BELOW_INSIDE_POINT = INSIDE_POINT.add(INTERSECTION_NORMAL.scale(-1));
    /**
     * Point below the polygon plane under the outside point.
     */
    private static final Point BELOW_OUTSIDE_POINT = OUTSIDE_POINT.add(INTERSECTION_NORMAL.scale(-1));

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for wrong polygon intersection
     */
    private static final String ERROR_POLYGON_INTERSECTION_PARALLEL =
            "Polygon.findIntersections() returned an unexpected result for rays parallel to the polygon normal";
    /**
     * Error message for wrong polygon intersection result on plane-boundary cases.
     */
    private static final String ERROR_POLYGON_INTERSECTION_ON_PLANE =
            "Polygon.findIntersections() returned an unexpected result for rays starting on the polygon plane";
    /**
     * Error message for wrong polygon intersection result for perpendicular rays.
     */
    private static final String ERROR_POLYGON_INTERSECTION_PERPENDICULAR =
            "Polygon.findIntersections() returned an unexpected result for rays perpendicular to the polygon normal";
    /**
     * Error message for wrong polygon intersection result for general rays.
     */
    private static final String ERROR_POLYGON_INTERSECTION_GENERAL =
            "Polygon.findIntersections() returned an unexpected result for general polygon-ray cases";

    /**
     * Test method for {@link Polygon#Polygon(Point...)}.
     * Verifies correct and incorrect polygon constructions.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct convex quadrilateral with vertices in correct order
        assertDoesNotThrow(() -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT1),
                "Polygon constructor should accept a valid convex quadrilateral with ordered vertices");

        // TC02: Wrong vertices order
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_Y, POINT_X, POINT1),
                "Polygon constructor should reject vertices given in the wrong order");

        // TC03: Vertices not in the same plane
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT2),
                "Polygon constructor should reject vertices that are not coplanar");

        // TC04: Concave quadrilateral
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT3),
                "Polygon constructor should reject a concave quadrilateral");

        // =============== Boundary Values Tests ==================

        // TC11: Vertex on a side
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT4),
                "Constructed a polygon with a vertex on a side");

        // TC12: Last point equals first point
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_Z),
                "Constructed a polygon with duplicate first/last vertex");

        // TC13: Co-located adjacent points
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Y, POINT_Y),
                "Constructed a polygon with co-located adjacent vertices");

        // TC14: Co-located non-adjacent points (indices 0 and 2)
        assertThrows(IllegalArgumentException.class, () -> new Polygon(POINT_Z, POINT_X, POINT_Z, POINT1),
                "Constructed a polygon with co-located non-adjacent vertices");
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

        Vector regular = Vector.AXIS_X;

        // ============ Group 1: Ray parallel to normal ============
        // ============ Equivalence Partitions Tests ==============

        // TC01: Starts above the plane, projected hit is on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_POINT.add(INTERSECTION_NORMAL), INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_PARALLEL);

        // TC02: Starts above the plane, projected hit is on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(INTERSECTION_VERTEX.add(INTERSECTION_NORMAL), INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_PARALLEL);

        // TC03: Starts above the plane, projected hit is inside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(INSIDE_POINT.add(INTERSECTION_NORMAL), INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_PARALLEL);

        // TC04: Starts above the plane, projected hit is outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(OUTSIDE_POINT.add(INTERSECTION_NORMAL), INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_PARALLEL);

        // TC05: Starts above the plane, projected hit is on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT.add(INTERSECTION_NORMAL), INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_PARALLEL);

        // TC06: Starts below the plane, projected hit is on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_POINT.add(INTERSECTION_NORMAL.scale(-1)), INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_PARALLEL);

        // TC07: Starts below the plane, projected hit is on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(INTERSECTION_VERTEX.add(INTERSECTION_NORMAL.scale(-1)), INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_PARALLEL);

        // TC08: Starts below the plane, projected hit is inside the polygon (1 point)
        assertEquals(java.util.List.of(INSIDE_POINT),
                polygon.findIntersections(new Ray(INSIDE_POINT.add(INTERSECTION_NORMAL.scale(-1)), INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_PARALLEL);

        // TC09: Starts below the plane, projected hit is outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(OUTSIDE_POINT.add(INTERSECTION_NORMAL.scale(-1)), INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_PARALLEL);

        // TC10: Starts below the plane, projected hit is on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT.add(INTERSECTION_NORMAL.scale(-1)), INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_PARALLEL);

        // =============== Boundary Values Tests ==================

        // TC11: Starts on the plane on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_POINT, INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_ON_PLANE);

        // TC12: Starts on the plane on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(INTERSECTION_VERTEX, INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_ON_PLANE);

        // TC13: Starts on the plane inside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(INSIDE_POINT, INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_ON_PLANE);

        // TC14: Starts on the plane outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(OUTSIDE_POINT, INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_ON_PLANE);

        // TC15: Starts on the plane on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT, INTERSECTION_NORMAL)),
                ERROR_POLYGON_INTERSECTION_ON_PLANE);

        // ============ Group 2: Ray perpendicular to normal ============
        // ============ Equivalence Partitions Tests ==============

        // TC16: Starts above the plane, projected line passes over the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(ABOVE_INSIDE_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC17: Starts above the plane, projected line does not pass over the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(ABOVE_OUTSIDE_POINT, BC_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC18: Starts below the plane, projected line passes over the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(BELOW_INSIDE_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC19: Starts below the plane, projected line does not pass over the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(BELOW_OUTSIDE_POINT, BC_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // =============== Boundary Values Tests ==================

        // TC20: Ray lies on an edge, start before the first vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(AB_BEFORE_FIRST_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC21: Ray lies on an edge, start on the first vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(INTERSECTION_VERTEX, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC22: Ray lies on an edge, start inside the edge segment (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC23: Ray lies on an edge, start on the second vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(AB_SECOND_VERTEX_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC24: Ray lies on an edge, start after the second vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC25: Ray is tangent at a vertex, start before the tangent vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(TANGENT_BEFORE_VERTEX_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC26: Ray is tangent at a vertex, start on the tangent vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(TANGENT_VERTEX_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC27: Ray is tangent at a vertex, start after the tangent vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(TANGENT_AFTER_VERTEX_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC28: Ray crosses the polygon, start before entering it (0 points)
        assertNull(polygon.findIntersections(new Ray(CROSSING_BEFORE_ENTRY_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC29: Ray crosses the polygon, start on the first border (0 points)
        assertNull(polygon.findIntersections(new Ray(CROSSING_FIRST_BORDER_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC30: Ray crosses the polygon, start inside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(CROSSING_INSIDE_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_PERPENDICULAR);

        // TC31: Ray crosses the polygon, start on the second border (0 points)
        assertNull(polygon.findIntersections(new Ray(CROSSING_SECOND_BORDER_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC32: Ray crosses the polygon, start after leaving it (0 points)
        assertNull(polygon.findIntersections(new Ray(CROSSING_AFTER_EXIT_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC33: Ray stays outside the polygon entirely (0 points)
        assertNull(polygon.findIntersections(new Ray(CROSSING_OUTSIDE_POINT, OUTSIDE_CROSSING_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC34: Ray is parallel to an edge and does not hit the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(PARALLEL_MISS_POINT, AB_DIRECTION)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // ============ Group 3: General ray ============
        // ============ Equivalence Partitions Tests ==============

        // TC35: Starts above the plane, projected hit is inside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(INSIDE_POINT.add(regular), regular)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC36: Starts above the plane, projected hit is outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(OUTSIDE_POINT.add(regular), regular)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC37: Starts above the plane, projected hit is on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_POINT.add(regular), regular)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC38: Starts above the plane, projected hit is on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(INTERSECTION_VERTEX.add(regular), regular)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC39: Starts above the plane, projected hit is on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT.add(regular), regular)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC40: Starts below the plane, projected hit is inside the polygon (1 point)
        assertEquals(java.util.List.of(INSIDE_POINT),
                polygon.findIntersections(new Ray(INSIDE_POINT.add(regular.scale(-1)), regular)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC41: Starts below the plane, projected hit is outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(OUTSIDE_POINT.add(regular.scale(-1)), regular)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC42: Starts below the plane, projected hit is on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_POINT.add(regular.scale(-1)), regular)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC43: Starts below the plane, projected hit is on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(INTERSECTION_VERTEX.add(regular.scale(-1)), regular)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // TC44: Starts below the plane, projected hit is on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT.add(regular.scale(-1)), regular)),
                ERROR_POLYGON_INTERSECTION_GENERAL);

        // =============== Boundary Values Tests ==================

        // TC45: Starts on the plane inside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(INSIDE_POINT, regular)),
                ERROR_POLYGON_INTERSECTION_ON_PLANE);

        // TC46: Starts on the plane outside the polygon (0 points)
        assertNull(polygon.findIntersections(new Ray(OUTSIDE_POINT, regular)),
                ERROR_POLYGON_INTERSECTION_ON_PLANE);

        // TC47: Starts on the plane on a polygon edge (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_POINT, regular)),
                ERROR_POLYGON_INTERSECTION_ON_PLANE);

        // TC48: Starts on the plane on a polygon vertex (0 points)
        assertNull(polygon.findIntersections(new Ray(INTERSECTION_VERTEX, regular)),
                ERROR_POLYGON_INTERSECTION_ON_PLANE);

        // TC49: Starts on the plane on an edge extension (0 points)
        assertNull(polygon.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT, regular)),
                ERROR_POLYGON_INTERSECTION_ON_PLANE);
    }
}

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
 * Unit tests for class {@link Triangle}.
 * The tests verify:
 * <ul>
 * <li>{@link Triangle#Triangle(Point, Point, Point)}</li>
 * <li>{@link Triangle#getNormal(Point)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *  * @author Ambash and Elyasaf
 */
class TriangleTests {
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
    TriangleTests() { /* to satisfy JavaDoc generator */ }

    /**
     * First vertex used in triangle tests
     */
    private static final Point POINT1 = new Point(1, 0, 0);
    /**
     * Second vertex used in triangle tests
     */
    private static final Point POINT2 = new Point(0, 1, 0);
    /**
     * Third vertex used in triangle tests
     */
    private static final Point POINT3 = new Point(0, 0, 1);
    /**
     * Point inside the triangle and not on a vertex
     */
    private static final Point POINT4 = new Point(0.25, 0.25, 0.5);

    /**
     * Point equal to the first vertex
     */
    private static final Point POINT5 = new Point(1, 0, 0);
    /**
     * Point located on the line through the first two vertices
     */
    private static final Point POINT6 = new Point(2, -1, 0);
    /**
     * Vertex A used in triangle intersection tests.
     */
    private static final Point TRIANGLE_A = new Point(2, 2, 7);
    /**
     * Vertex B used in triangle intersection tests.
     */
    private static final Point TRIANGLE_B = new Point(8, 2, 13);
    /**
     * Vertex C used in triangle intersection tests.
     */
    private static final Point TRIANGLE_C = new Point(4, 7, 19);

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for wrong triangle construction
     */
    private static final String ERROR_CONSTRUCTOR = "Failed constructing a correct triangle";
    /**
     * Error message for duplicate points in triangle construction
     */
    private static final String ERROR_DUPLICATE_POINTS = "Constructed a triangle with duplicate points";
    /**
     * Error message for collinear points in triangle construction
     */
    private static final String ERROR_COLLINEAR_POINTS = "Constructed a triangle with collinear points";
    /**
     * Error message for wrong getNormal execution
     */
    private static final String ERROR_GET_NORMAL = "getNormal() threw unexpected exception";
    /**
     * Error message for non-unit triangle normal
     */
    private static final String ERROR_NORMAL_LENGTH = "Triangle normal is not a unit vector";
    /**
     * Error message for wrong orthogonality to the first edge
     */
    private static final String ERROR_ORTHOGONAL_EDGE1 = "Triangle normal is not orthogonal to edge 1";
    /**
     * Error message for wrong orthogonality to the second edge
     */
    private static final String ERROR_ORTHOGONAL_EDGE2 = "Triangle normal is not orthogonal to edge 2";
    /**
     * Error message for wrong triangle intersection result.
     */
    private static final String ERROR_TRIANGLE_INTERSECTION = "Wrong triangle intersection result";

    /**
     * Test method for {@link Triangle#Triangle(Point, Point, Point)}.
     * Verifies correct and incorrect triangle constructions.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Correct triangle from three non-collinear points
        assertDoesNotThrow(() -> new Triangle(POINT1, POINT2, POINT3), ERROR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================

        // TC11: First and second points are the same
        assertThrows(IllegalArgumentException.class, () -> new Triangle(POINT1, POINT5, POINT3),
                ERROR_DUPLICATE_POINTS);

        // TC12: All points are on the same line
        assertThrows(IllegalArgumentException.class, () -> new Triangle(POINT1, POINT2, POINT6),
                ERROR_COLLINEAR_POINTS);
    }

    /**
     * Test method for {@link Triangle#getNormal(Point)}.
     * Verifies that the returned normal vector is unit length and orthogonal
     * to the triangle edges.
     */
    @Test
    void testGetNormal() {
        // ============ Equivalence Partitions Tests ==============
        Triangle triangle = new Triangle(POINT1, POINT2, POINT3);
        // TC01: A point inside the triangle that is not a vertex
        assertDoesNotThrow(() -> triangle.getNormal(POINT4), ERROR_GET_NORMAL);
        Vector result = triangle.getNormal(POINT4);
        // Ensure |n| = 1
        assertEquals(1d, result.length(), DELTA, ERROR_NORMAL_LENGTH);
        // Ensure normal is orthogonal to the triangle edges
        assertEquals(0d, result.dotProduct(POINT2.subtract(POINT1)), DELTA, ERROR_ORTHOGONAL_EDGE1);
        assertEquals(0d, result.dotProduct(POINT3.subtract(POINT1)), DELTA, ERROR_ORTHOGONAL_EDGE2);
    }

    /**
     * Test method for {@link Triangle#findIntersections(primitives.Ray)}.
     * Verifies triangle-ray intersections in three groups:
     * rays parallel to the normal, rays perpendicular to the normal,
     * and general rays.
     */
    @Test
    void testFindIntersections() {
        Triangle triangle = new Triangle(TRIANGLE_A, TRIANGLE_B, TRIANGLE_C);

        Vector normal = new Vector(1, 2, -1);
        Vector AB = new Vector(1, 0, 1);
        Vector AC = new Vector(2, 5, 12);
        Vector regular = v(1, 0, -1);
        Vector on = new Vector(1, 1, 3);

        Point inside = new Point(4, 3, 11);
        Point outside = new Point(8, 4, 17);
        Point onEdge = new Point(5, 2, 10);
        Point onVertex = TRIANGLE_A;
        Point onEdgeExtension = new Point(9, 2, 14);

        Point overTriangleAbove = inside.add(normal);
        Point outsideAbove = onEdgeExtension.add(normal);
        Point overTriangleBelow = inside.add(normal.scale(-1));
        Point outsideBelow = onEdgeExtension.add(normal.scale(-1));

        // ============ Group 1: Ray parallel to normal ============
        // ============ Equivalence Partitions Tests ==============

        // TC01: Starts above the plane, projected hit is on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(onEdge.add(normal), normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC02: Starts above the plane, projected hit is on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(onVertex.add(normal), normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC03: Starts above the plane, projected hit is inside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(inside.add(normal), normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC04: Starts above the plane, projected hit is outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(outside.add(normal), normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC05: Starts above the plane, projected hit is on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(onEdgeExtension.add(normal), normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC06: Starts below the plane, projected hit is on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(onEdge.add(normal.scale(-1)), normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC07: Starts below the plane, projected hit is on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(onVertex.add(normal.scale(-1)), normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC08: Starts below the plane, projected hit is inside the triangle (1 point)
        assertEquals(java.util.List.of(inside),
                triangle.findIntersections(new Ray(inside.add(normal.scale(-1)), normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC09: Starts below the plane, projected hit is outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(outside.add(normal.scale(-1)), normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC10: Starts below the plane, projected hit is on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(onEdgeExtension.add(normal.scale(-1)), normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC11: Starts on the plane on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(onEdge, normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC12: Starts on the plane on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(onVertex, normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC13: Starts on the plane inside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(inside, normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC14: Starts on the plane outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(outside, normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC15: Starts on the plane on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(onEdgeExtension, normal)),
                ERROR_TRIANGLE_INTERSECTION);

        // ============ Group 2: Ray perpendicular to normal ============
        // ============ Equivalence Partitions Tests ==============

        // TC16: Starts above the plane, projected line passes over the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(overTriangleAbove, AB)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC17: Starts above the plane, projected line does not pass over the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(outsideAbove, AB)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC18: Starts below the plane, projected line passes over the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(overTriangleBelow, AB)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC19: Starts below the plane, projected line does not pass over the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(outsideBelow, AB)),
                ERROR_TRIANGLE_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC20: Ray lies on an edge, start before the first vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(new Point(1, -0.5, 1), AC)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC21: Ray lies on an edge, start on the first vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(new Point(2, 2, 7), AC)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC22: Ray lies on an edge, start inside the edge segment (0 points)
        assertNull(triangle.findIntersections(new Ray(p(3, 4.5, 13), AC)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC23: Ray lies on an edge, start on the second vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(p(4, 7, 19), AC)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC24: Ray lies on an edge, start after the second vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(p(6, 12, 31), AC)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC25: Ray is tangent at a vertex, start before the tangent vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(p(2, 7, 17), AB)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC26: Ray is tangent at a vertex, start on the tangent vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(p(4, 7, 19), AB)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC27: Ray is tangent at a vertex, start after the tangent vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(p(9, 7, 24), AB)),
                ERROR_TRIANGLE_INTERSECTION);
//
        // TC28: Ray crosses the triangle, start before entering it (0 points)
        assertNull(triangle.findIntersections(new Ray(p(4, 1, 7), on)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC29: Ray crosses the triangle, start on the first border (0 points)
        assertNull(triangle.findIntersections(new Ray(p(5, 2, 10), on)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC30: Ray crosses the triangle, start inside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(p(6, 3, 13), on)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC31: Ray crosses the triangle, start on the second border (0 points)
        assertNull(triangle.findIntersections(new Ray(p(6 + 2 / 3, 3 + 2 / 3, 15), on)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC32: Ray crosses the triangle, start after leaving it (0 points)
        assertNull(triangle.findIntersections(new Ray(p(8, 5, 19), on)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC33: Ray stays outside the triangle entirely (0 points)
        assertNull(triangle.findIntersections(new Ray(p(10, 0, 10), on)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC34: Ray is parallel to an edge and does not hit the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(p(0, 0, 1), AB)),
                ERROR_TRIANGLE_INTERSECTION);

        // ============ Group 3: General ray ============
        // ============ Equivalence Partitions Tests ==============

        // TC35: Starts above the plane, projected hit is inside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(p(6, 3, 9), regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC36: Starts above the plane, projected hit is outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(p(9, 4, 16), regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC37: Starts above the plane, projected hit is on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(p(6, 2, 9), regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC38: Starts above the plane, projected hit is on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(p(3, 2, 6), regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC39: Starts above the plane, projected hit is on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(p(10, 2, 13), regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC40: Starts below the plane, projected hit is inside the triangle (1 point)
        assertEquals(java.util.List.of(inside),
                triangle.findIntersections(new Ray(new Point(2, 3, 11), regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC41: Starts below the plane, projected hit is outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(p(7, 4, 18), regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC42: Starts below the plane, projected hit is on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(p(4, 2, 11), regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC43: Starts below the plane, projected hit is on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(p(1, 2, 8), regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC44: Starts below the plane, projected hit is on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(p(8, 2, 15), regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // =============== Boundary Values Tests ==================

        // TC45: Starts on the plane inside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(inside, regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC46: Starts on the plane outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(outside, regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC47: Starts on the plane on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(onEdge, regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC48: Starts on the plane on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(onVertex, regular)),
                ERROR_TRIANGLE_INTERSECTION);

        // TC49: Starts on the plane on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(onEdgeExtension, regular)),
                ERROR_TRIANGLE_INTERSECTION);
    }
}

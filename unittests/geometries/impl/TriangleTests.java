package geometries.impl;

import geometries.api.AABB;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for class {@link Triangle}.
 * The tests verify:
 * <ul>
 * <li>{@link Triangle#Triangle(Point, Point, Point)}</li>
 * <li>{@link Triangle#getNormal(Point)}</li>
 * <li>{@link Triangle#equals(Object)} (inherited from {@link Polygon})</li>
 * <li>{@link geometries.api.Geometry#setEmission(Color)}</li>
 * <li>{@link geometries.api.Geometry#setMaterial(Material)}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 * @author Ambash and Elyasaf
 */
class TriangleTests {
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
     * Point inside the triangle for intersection tests.
     */
    private static final Point INSIDE_POINT = new Point(4, 3, 11);
    /**
     * Point outside the triangle for intersection tests.
     */
    private static final Point OUTSIDE_POINT = new Point(8, 4, 17);
    /**
     * Point on a triangle edge for intersection tests.
     */
    private static final Point ON_EDGE_POINT = new Point(5, 2, 10);
    /**
     * Point on an extension of a triangle edge for intersection tests.
     */
    private static final Point ON_EDGE_EXTENSION_POINT = new Point(9, 2, 14);
    /**
     * Point on the AC edge before the first vertex.
     */
    private static final Point AC_BEFORE_FIRST_POINT = new Point(1, -0.5, 1);
    /**
     * Point on the AC edge inside the segment.
     */
    private static final Point AC_INSIDE_SEGMENT_POINT = new Point(3, 4.5, 13);
    /**
     * Point on the AC edge after the second vertex.
     */
    private static final Point AC_AFTER_SECOND_POINT = new Point(6, 12, 31);
    /**
     * Point before the tangent vertex along AB.
     */
    private static final Point TANGENT_BEFORE_VERTEX_POINT = new Point(2, 7, 17);
    /**
     * Point after the tangent vertex along AB.
     */
    private static final Point TANGENT_AFTER_VERTEX_POINT = new Point(9, 7, 24);
    /**
     * Point before entering the triangle along the crossing ray.
     */
    private static final Point CROSSING_BEFORE_ENTRY_POINT = new Point(4, 1, 7);
    /**
     * Point on the first border of the crossing ray.
     */
    private static final Point CROSSING_FIRST_BORDER_POINT = new Point(5, 2, 10);
    /**
     * Point inside the triangle along the crossing ray.
     */
    private static final Point CROSSING_INSIDE_POINT = new Point(6, 3, 13);
    /**
     * Point on the second border of the crossing ray.
     */
    private static final Point CROSSING_SECOND_BORDER_POINT = new Point(6 + 2d / 3, 3 + 2d / 3, 15);
    /**
     * Point after leaving the triangle along the crossing ray.
     */
    private static final Point CROSSING_AFTER_EXIT_POINT = new Point(8, 5, 19);
    /**
     * Point for a crossing ray that stays outside the triangle.
     */
    private static final Point CROSSING_OUTSIDE_POINT = new Point(10, 0, 10);
    /**
     * Point for a ray parallel to an edge and missing the triangle.
     */
    private static final Point PARALLEL_MISS_POINT = new Point(0, 0, 1);
    /**
     * Point above the plane with an inside projected hit for the general ray.
     */
    private static final Point GENERAL_ABOVE_INSIDE_POINT = new Point(6, 3, 9);
    /**
     * Point above the plane with an outside projected hit for the general ray.
     */
    private static final Point GENERAL_ABOVE_OUTSIDE_POINT = new Point(9, 4, 16);
    /**
     * Point above the plane with a projected hit on a triangle edge.
     */
    private static final Point GENERAL_ABOVE_EDGE_POINT = new Point(6, 2, 9);
    /**
     * Point above the plane with a projected hit on a triangle vertex.
     */
    private static final Point GENERAL_ABOVE_VERTEX_POINT = new Point(3, 2, 6);
    /**
     * Point above the plane with a projected hit on an edge extension.
     */
    private static final Point GENERAL_ABOVE_EDGE_EXTENSION_POINT = new Point(10, 2, 13);
    /**
     * Point below the plane with an outside projected hit for the general ray.
     */
    private static final Point GENERAL_BELOW_OUTSIDE_POINT = new Point(7, 4, 18);
    /**
     * Point below the plane with a projected hit on a triangle edge.
     */
    private static final Point GENERAL_BELOW_EDGE_POINT = new Point(4, 2, 11);
    /**
     * Point below the plane with a projected hit on a triangle vertex.
     */
    private static final Point GENERAL_BELOW_VERTEX_POINT = new Point(1, 2, 8);
    /**
     * Point below the plane with a projected hit on an edge extension.
     */
    private static final Point GENERAL_BELOW_EDGE_EXTENSION_POINT = new Point(8, 2, 15);
    /**
     * Normal direction used in triangle intersection tests.
     */
    private static final Vector INTERSECTION_NORMAL = new Vector(1, 2, -1);
    /**
     * Direction along the AB line used in triangle intersection tests.
     */
    private static final Vector AB_DIRECTION = new Vector(1, 0, 1);
    /**
     * Direction along the AC line used in triangle intersection tests.
     */
    private static final Vector AC_DIRECTION = new Vector(2, 5, 12);
    /**
     * General direction used in triangle intersection tests.
     */
    private static final Vector GENERAL_DIRECTION = new Vector(1, 0, -1);
    /**
     * Crossing direction used in triangle intersection tests.
     */
    private static final Vector CROSSING_DIRECTION = new Vector(1, 1, 3);
    /**
     * Point above the triangle plane over the inside point.
     */
    private static final Point ABOVE_INSIDE_POINT = INSIDE_POINT.add(INTERSECTION_NORMAL);
    /**
     * Point above the triangle plane over the edge extension point.
     */
    private static final Point ABOVE_EDGE_EXTENSION_POINT = ON_EDGE_EXTENSION_POINT.add(INTERSECTION_NORMAL);
    /**
     * Point below the triangle plane under the inside point.
     */
    private static final Point BELOW_INSIDE_POINT = INSIDE_POINT.add(INTERSECTION_NORMAL.scale(-1));
    /**
     * Point below the triangle plane under the edge extension point.
     */
    private static final Point BELOW_EDGE_EXTENSION_POINT = ON_EDGE_EXTENSION_POINT.add(INTERSECTION_NORMAL.scale(-1));

    /**
     * Delta value for accuracy when comparing double values.
     */
    private static final double DELTA = 1e-6;

    /**
     * Error message for wrong triangle construction
     */
    private static final String ERROR_CONSTRUCTOR =
            "Triangle constructor should accept three distinct non-collinear vertices";
    /**
     * Error message for duplicate points in triangle construction
     */
    private static final String ERROR_DUPLICATE_POINTS =
            "Triangle constructor should reject duplicate vertices";
    /**
     * Error message for collinear points in triangle construction
     */
    private static final String ERROR_COLLINEAR_POINTS =
            "Triangle constructor should reject three collinear vertices";
    /**
     * Error message for wrong getNormal execution
     */
    private static final String ERROR_GET_NORMAL =
            "Triangle.getNormal() should not throw for a point on the triangle plane";
    /**
     * Error message for non-unit triangle normal
     */
    private static final String ERROR_NORMAL_LENGTH =
            "Triangle.getNormal() should return a unit-length normal vector";
    /**
     * Error message for wrong orthogonality to the first edge
     */
    private static final String ERROR_ORTHOGONAL_EDGE1 =
            "Triangle.getNormal() result is not orthogonal to edge POINT1->POINT2";
    /**
     * Error message for wrong orthogonality to the second edge
     */
    private static final String ERROR_ORTHOGONAL_EDGE2 =
            "Triangle.getNormal() result is not orthogonal to edge POINT1->POINT3";
    /**
     * Error message for wrong triangle intersection result.
     */
    private static final String ERROR_TRIANGLE_INTERSECTION_PARALLEL =
            "Triangle.findIntersections() returned an unexpected result for rays parallel to the triangle normal";
    /**
     * Error message for wrong triangle intersection result on plane-boundary cases.
     */
    private static final String ERROR_TRIANGLE_INTERSECTION_ON_PLANE =
            "Triangle.findIntersections() returned an unexpected result for rays starting on the triangle plane";
    /**
     * Error message for wrong triangle intersection result for perpendicular rays.
     */
    private static final String ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR =
            "Triangle.findIntersections() returned an unexpected result for rays perpendicular to the triangle normal";
    /**
     * Error message for wrong triangle intersection result for general rays.
     */
    private static final String ERROR_TRIANGLE_INTERSECTION_GENERAL =
            "Triangle.findIntersections() returned an unexpected result for general triangle-ray cases";

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

        // ============ Group 1: Ray parallel to normal ============
        // ============ Equivalence Partitions Tests ==============

        // TC01: Starts above the plane, projected hit is on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(ON_EDGE_POINT.add(INTERSECTION_NORMAL), INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_PARALLEL);

        // TC02: Starts above the plane, projected hit is on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(TRIANGLE_A.add(INTERSECTION_NORMAL), INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_PARALLEL);

        // TC03: Starts above the plane, projected hit is inside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(INSIDE_POINT.add(INTERSECTION_NORMAL), INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_PARALLEL);

        // TC04: Starts above the plane, projected hit is outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(OUTSIDE_POINT.add(INTERSECTION_NORMAL), INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_PARALLEL);

        // TC05: Starts above the plane, projected hit is on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT.add(INTERSECTION_NORMAL), INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_PARALLEL);

        // TC06: Starts below the plane, projected hit is on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(ON_EDGE_POINT.add(INTERSECTION_NORMAL.scale(-1)), INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_PARALLEL);

        // TC07: Starts below the plane, projected hit is on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(TRIANGLE_A.add(INTERSECTION_NORMAL.scale(-1)), INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_PARALLEL);

        // TC08: Starts below the plane, projected hit is inside the triangle (1 point)
        assertEquals(java.util.List.of(INSIDE_POINT),
                triangle.findIntersections(new Ray(INSIDE_POINT.add(INTERSECTION_NORMAL.scale(-1)), INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_PARALLEL);

        // TC09: Starts below the plane, projected hit is outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(OUTSIDE_POINT.add(INTERSECTION_NORMAL.scale(-1)), INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_PARALLEL);

        // TC10: Starts below the plane, projected hit is on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT.add(INTERSECTION_NORMAL.scale(-1)), INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_PARALLEL);

        // =============== Boundary Values Tests ==================

        // TC11: Starts on the plane on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(ON_EDGE_POINT, INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_ON_PLANE);

        // TC12: Starts on the plane on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(TRIANGLE_A, INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_ON_PLANE);

        // TC13: Starts on the plane inside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(INSIDE_POINT, INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_ON_PLANE);

        // TC14: Starts on the plane outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(OUTSIDE_POINT, INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_ON_PLANE);

        // TC15: Starts on the plane on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT, INTERSECTION_NORMAL)),
                ERROR_TRIANGLE_INTERSECTION_ON_PLANE);

        // ============ Group 2: Ray perpendicular to normal ============
        // ============ Equivalence Partitions Tests ==============

        // TC16: Starts above the plane, projected line passes over the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(ABOVE_INSIDE_POINT, AB_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC17: Starts above the plane, projected line does not pass over the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(ABOVE_EDGE_EXTENSION_POINT, AB_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC18: Starts below the plane, projected line passes over the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(BELOW_INSIDE_POINT, AB_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC19: Starts below the plane, projected line does not pass over the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(BELOW_EDGE_EXTENSION_POINT, AB_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // =============== Boundary Values Tests ==================

        // TC20: Ray lies on an edge, start before the first vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(AC_BEFORE_FIRST_POINT, AC_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC21: Ray lies on an edge, start on the first vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(TRIANGLE_A, AC_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC22: Ray lies on an edge, start inside the edge segment (0 points)
        assertNull(triangle.findIntersections(new Ray(AC_INSIDE_SEGMENT_POINT, AC_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC23: Ray lies on an edge, start on the second vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(TRIANGLE_C, AC_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC24: Ray lies on an edge, start after the second vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(AC_AFTER_SECOND_POINT, AC_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC25: Ray is tangent at a vertex, start before the tangent vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(TANGENT_BEFORE_VERTEX_POINT, AB_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC26: Ray is tangent at a vertex, start on the tangent vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(TRIANGLE_C, AB_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC27: Ray is tangent at a vertex, start after the tangent vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(TANGENT_AFTER_VERTEX_POINT, AB_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);
//
        // TC28: Ray crosses the triangle, start before entering it (0 points)
        assertNull(triangle.findIntersections(new Ray(CROSSING_BEFORE_ENTRY_POINT, CROSSING_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC29: Ray crosses the triangle, start on the first border (0 points)
        assertNull(triangle.findIntersections(new Ray(CROSSING_FIRST_BORDER_POINT, CROSSING_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC30: Ray crosses the triangle, start inside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(CROSSING_INSIDE_POINT, CROSSING_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC31: Ray crosses the triangle, start on the second border (0 points)
        assertNull(triangle.findIntersections(new Ray(CROSSING_SECOND_BORDER_POINT, CROSSING_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC32: Ray crosses the triangle, start after leaving it (0 points)
        assertNull(triangle.findIntersections(new Ray(CROSSING_AFTER_EXIT_POINT, CROSSING_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC33: Ray stays outside the triangle entirely (0 points)
        assertNull(triangle.findIntersections(new Ray(CROSSING_OUTSIDE_POINT, CROSSING_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // TC34: Ray is parallel to an edge and does not hit the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(PARALLEL_MISS_POINT, AB_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_PERPENDICULAR);

        // ============ Group 3: General ray ============
        // ============ Equivalence Partitions Tests ==============

        // TC35: Starts above the plane, projected hit is inside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(GENERAL_ABOVE_INSIDE_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_GENERAL);

        // TC36: Starts above the plane, projected hit is outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(GENERAL_ABOVE_OUTSIDE_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_GENERAL);

        // TC37: Starts above the plane, projected hit is on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(GENERAL_ABOVE_EDGE_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_GENERAL);

        // TC38: Starts above the plane, projected hit is on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(GENERAL_ABOVE_VERTEX_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_GENERAL);

        // TC39: Starts above the plane, projected hit is on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(GENERAL_ABOVE_EDGE_EXTENSION_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_GENERAL);

        // TC40: Starts below the plane, projected hit is inside the triangle (1 point)
        assertEquals(java.util.List.of(INSIDE_POINT),
                triangle.findIntersections(new Ray(INSIDE_POINT.add(GENERAL_DIRECTION.scale(-1)), GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_GENERAL);

        // TC41: Starts below the plane, projected hit is outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(GENERAL_BELOW_OUTSIDE_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_GENERAL);

        // TC42: Starts below the plane, projected hit is on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(GENERAL_BELOW_EDGE_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_GENERAL);

        // TC43: Starts below the plane, projected hit is on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(GENERAL_BELOW_VERTEX_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_GENERAL);

        // TC44: Starts below the plane, projected hit is on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(GENERAL_BELOW_EDGE_EXTENSION_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_GENERAL);

        // =============== Boundary Values Tests ==================

        // TC45: Starts on the plane inside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(INSIDE_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_ON_PLANE);

        // TC46: Starts on the plane outside the triangle (0 points)
        assertNull(triangle.findIntersections(new Ray(OUTSIDE_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_ON_PLANE);

        // TC47: Starts on the plane on a triangle edge (0 points)
        assertNull(triangle.findIntersections(new Ray(ON_EDGE_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_ON_PLANE);

        // TC48: Starts on the plane on a triangle vertex (0 points)
        assertNull(triangle.findIntersections(new Ray(TRIANGLE_A, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_ON_PLANE);

        // TC49: Starts on the plane on an edge extension (0 points)
        assertNull(triangle.findIntersections(new Ray(ON_EDGE_EXTENSION_POINT, GENERAL_DIRECTION)),
                ERROR_TRIANGLE_INTERSECTION_ON_PLANE);
    }

    /**
     * Test method for {@link Triangle#equals(Object)} and {@link Triangle#hashCode()}
     * (both inherited from {@link Polygon}).
     */
    @Test
    void testEqualsAndHashCode() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Equal triangles constructed independently
        Triangle t1 = new Triangle(POINT1, POINT2, POINT3);
        Triangle t2 = new Triangle(POINT1, POINT2, POINT3);
        assertEquals(t1, t2, "Equal triangles should compare equal");
        assertEquals(t1.hashCode(), t2.hashCode(), "Equal triangles should have equal hashCode");
        // TC02: Same vertices, cyclically rotated starting point
        Triangle rotated = new Triangle(POINT2, POINT3, POINT1);
        assertEquals(t1, rotated, "Cyclically rotated vertices should be equal");
        assertEquals(t1.hashCode(), rotated.hashCode(), "Cyclically rotated vertices should have equal hashCode");
        // TC03: A genuinely different triangle
        assertNotEquals(t1, new Triangle(POINT1, POINT2, POINT4), "Different triangles should not be equal");

        // =============== Boundary Values Tests ==================
        // TC11: A triangle equals itself
        assertEquals(t1, t1, "A triangle should equal itself");
        // TC12: Not equal to null / a different type
        assertNotEquals(t1, null, "A triangle should not equal null");
        assertNotEquals(t1, "not a Triangle", "A triangle should not equal an object of a different type");
    }

    /**
     * Test method for the inherited {@link geometries.api.Geometry#setEmission(Color)},
     * {@link geometries.api.Geometry#getEmission()}, {@link geometries.api.Geometry#setMaterial(Material)}
     * and {@link geometries.api.Geometry#getMaterial()}.
     */
    @Test
    void testEmissionAndMaterial() {
        // ============ Equivalence Partitions Tests ==============
        Triangle triangle = new Triangle(POINT1, POINT2, POINT3);
        Color emission = new Color(10, 20, 30);
        Material material = new Material().setKD(0.5);

        // TC01: setEmission returns the same instance (chaining) and stores the value
        assertSame(triangle, triangle.setEmission(emission), "setEmission() should return the same instance for chaining");
        assertEquals(emission, triangle.getEmission(), "getEmission() did not return the emission color that was set");

        // TC02: setMaterial returns the same instance (chaining) and stores the value
        assertSame(triangle, triangle.setMaterial(material), "setMaterial() should return the same instance for chaining");
        assertSame(material, triangle.getMaterial(), "getMaterial() did not return the material that was set");

        // =============== Boundary Values Tests ==================
        // TC11: A freshly constructed geometry defaults to black emission and a default material
        Triangle fresh = new Triangle(POINT1, POINT2, POINT3);
        assertEquals(Color.BLACK, fresh.getEmission(), "A new geometry should default to black emission");
        assertNotNull(fresh.getMaterial(), "A new geometry should default to a non-null material");
    }

    /**
     * Test method for {@link Triangle#getBoundingBox()} (inherited from {@link Polygon}).
     */
    @Test
    void testGetBoundingBox() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: The box is the tight enclosure of the three vertices
        // Vertices: POINT1=(1,0,0), POINT2=(0,1,0), POINT3=(0,0,1)
        AABB box = new Triangle(POINT1, POINT2, POINT3).getBoundingBox();
        assertNotNull(box, "A triangle is finite and should have a non-null bounding box");
        assertEquals(1d, box.size(0), DELTA, "Bounding box X size should span from 0 to 1");
        assertEquals(1d, box.size(1), DELTA, "Bounding box Y size should span from 0 to 1");
        assertEquals(1d, box.size(2), DELTA, "Bounding box Z size should span from 0 to 1");
        assertEquals(0.5, box.midpoint(0), DELTA, "Bounding box X midpoint should be 0.5");
        assertEquals(0.5, box.midpoint(1), DELTA, "Bounding box Y midpoint should be 0.5");
        assertEquals(0.5, box.midpoint(2), DELTA, "Bounding box Z midpoint should be 0.5");
    }
}

package geometries.impl;

import geometries.api.AABB;
import geometries.api.Intersectable;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for class {@link Geometries}.
 * The tests verify:
 * <ul>
 * <li>{@link Geometries#Geometries(geometries.api.Intersectable...)}</li>
 * <li>{@link Geometries#add(geometries.api.Intersectable...)}</li>
 * <li>{@link Geometries#findIntersections(Ray)}</li>
 * <li>{@link Geometries#getChildren()}</li>
 * <li>{@link Geometries#flatten()}</li>
 * <li>{@link Geometries#fromMesh(List, int[][])}</li>
 * <li>{@link Geometries#buildBVH()}</li>
 * <li>{@link Geometries#getBoundingBox()}</li>
 * </ul>
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class GeometriesTests {
    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    GeometriesTests() { /* to satisfy JavaDoc generator */ }

    /**
     * A sphere centered at (1,0,0) with radius 1, tangent to the YZ plane.
     */
    private static final Sphere SPHERE = new Sphere(new Point(1, 0, 0), 1d);
    /**
     * A triangle in the XY plane around the origin.
     */
    private static final Triangle TRIANGLE = new Triangle(
            new Point(-1, -1, 0),
            new Point(3, -1, 0),
            new Point(1, 3, 0));
    /**
     * A plane z = -1 (below the XY plane).
     */
    private static final Plane PLANE = new Plane(
            new Point(1, 1, -1),
            new Vector(0, 0, 1));

    /**
     * A ray through the center of the scene that hits all three geometries:
     * sphere (2 points), triangle (1 point), plane (1 point) = 4 total.
     */
    private static final Ray RAY_ALL = new Ray(new Point(1, 0, 2), new Vector(0, 0, -1));

    /**
     * Error message for wrong constructor result.
     */
    private static final String ERROR_CONSTRUCTOR =
            "Geometries constructor did not register the expected geometries";
    /**
     * Error message for wrong add result.
     */
    private static final String ERROR_ADD =
            "Geometries.add() did not register the expected geometries";

    /**
     * Test method for {@link Geometries#Geometries(geometries.api.Intersectable...)}.
     * Verifies that geometries passed to the constructor are all registered in the
     * collection and become searchable via {@link Geometries#findIntersections(Ray)}.
     */
    @Test
    void testConstructor() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Constructor with multiple geometries — all are registered (4 intersections)
        assertEquals(4,
                new Geometries(SPHERE, TRIANGLE, PLANE).findIntersections(RAY_ALL).size(),
                ERROR_CONSTRUCTOR);

        // =============== Boundary Values Tests ==================

        // TC11: Empty constructor — collection is empty, findIntersections returns null
        assertNull(new Geometries().findIntersections(RAY_ALL), ERROR_CONSTRUCTOR);

        // TC12: Constructor with a single geometry — that geometry is registered (2 intersections)
        assertEquals(2,
                new Geometries(SPHERE).findIntersections(RAY_ALL).size(),
                ERROR_CONSTRUCTOR);
    }

    /**
     * Test method for {@link Geometries#add(geometries.api.Intersectable...)}.
     * Verifies that {@code add} correctly appends geometries to the collection,
     * that adding multiple geometries at once works, that repeated calls accumulate,
     * and that calling {@code add} with no arguments leaves the collection unchanged.
     */
    @Test
    void testAdd() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Add a single geometry to an empty collection (2 intersections)
        Geometries g1 = new Geometries();
        g1.add(SPHERE);
        assertEquals(2, g1.findIntersections(RAY_ALL).size(), ERROR_ADD);

        // TC02: Add multiple geometries at once (4 intersections)
        Geometries g2 = new Geometries();
        g2.add(SPHERE, TRIANGLE, PLANE);
        assertEquals(4, g2.findIntersections(RAY_ALL).size(), ERROR_ADD);

        // TC03: Add geometries one by one to a non-empty collection — results accumulate (4 intersections)
        Geometries g3 = new Geometries(SPHERE);
        g3.add(TRIANGLE);
        g3.add(PLANE);
        assertEquals(4, g3.findIntersections(RAY_ALL).size(), ERROR_ADD);

        // =============== Boundary Values Tests ==================

        // TC11: Add zero geometries — collection is unchanged (2 intersections, same as before)
        Geometries g4 = new Geometries(SPHERE);
        g4.add();
        assertEquals(2, g4.findIntersections(RAY_ALL).size(), ERROR_ADD);
    }

    /**
     * Test method for {@link Geometries#findIntersections(Ray)}.
     * Verifies correct intersection counts for all EP and BVA cases.
     */
    @Test
    void testFindIntersections() {

        // ============ Equivalence Partitions Tests ==============

        // TC01: Some (but not all) geometries are hit — ray misses sphere, hits triangle and plane (2 points)
        assertEquals(2, new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(new Ray(new Point(0, -0.5, -2), new Vector(0, 0, 1))).size(),
                "Ray that hits triangle (1) + plane (1) but misses sphere should return 2 points");

        // TC02: Only one geometry is hit — horizontal ray at z=0.5 hits only the sphere (2 points)
        assertEquals(2, new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(new Ray(new Point(-2, 0, 0.5), new Vector(1, 0, 0))).size(),
                "Ray that hits only the sphere should return 2 points");

        // TC03: All geometries are hit (sum of all intersections)
        assertEquals(4, new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(RAY_ALL).size(),
                "Ray that hits sphere (2) + triangle (1) + plane (1) should return 4 points");

        // =============== Boundary Values Tests ==================

        // TC11: Empty collection — no geometries at all (null)
        assertNull(new Geometries().findIntersections(new Ray(new Point(1, 0, 2), new Vector(0, 0, -1))),
                "Empty collection should return null");

        // TC12: No geometry is hit (null)
        assertNull(new Geometries(SPHERE, TRIANGLE, PLANE)
                        .findIntersections(new Ray(new Point(10, 10, 10), new Vector(0, 0, 1))),
                "Ray that misses all geometries should return null");
    }

    /**
     * Test method for {@link Geometries#getChildren()}.
     */
    @Test
    void testGetChildren() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Children reflect what was added, in order
        Geometries g = new Geometries(SPHERE, TRIANGLE);
        List<Intersectable> children = g.getChildren();
        assertEquals(List.of(SPHERE, TRIANGLE), children,
                "getChildren() should reflect the added geometries in order");

        // =============== Boundary Values Tests ==================
        // TC11: The returned list is an unmodifiable view
        assertThrows(UnsupportedOperationException.class, () -> children.add(PLANE),
                "getChildren() should return an unmodifiable view");
        // TC12: An empty collection has no children
        assertTrue(new Geometries().getChildren().isEmpty(), "A new empty Geometries should have no children");
    }

    /**
     * Test method for {@link Geometries#flatten()}.
     */
    @Test
    void testFlatten() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: Nested groups are dissolved, only leaf geometries remain
        Geometries inner = new Geometries(TRIANGLE, PLANE);
        Geometries outer = new Geometries(SPHERE, inner);
        Geometries flat = outer.flatten();
        assertEquals(3, flat.getChildren().size(), "flatten() should produce one entry per leaf geometry");
        for (Intersectable child : flat.getChildren())
            assertFalse(child instanceof Geometries, "flatten() should not leave any nested Geometries groups");

        // =============== Boundary Values Tests ==================
        // TC11: Flattening an already-flat collection changes nothing
        assertEquals(3, new Geometries(SPHERE, TRIANGLE, PLANE).flatten().getChildren().size(),
                "Flattening an already-flat collection should keep all leaves");
        // TC12: Flattening an empty collection stays empty
        assertTrue(new Geometries().flatten().getChildren().isEmpty(),
                "Flattening an empty collection should stay empty");
    }

    /**
     * Test method for {@link Geometries#fromMesh(List, int[][])}.
     */
    @Test
    void testFromMesh() {
        List<Point> vertices = List.of(
                new Point(-2, -2, 0), new Point(2, -2, 0),
                new Point(2, 2, 0), new Point(-2, 2, 0));
        int[][] faces = {{0, 1, 2}, {0, 2, 3}};

        // ============ Equivalence Partitions Tests ==============
        // TC01: One Triangle is created per face
        Geometries mesh = Geometries.fromMesh(vertices, faces);
        assertEquals(2, mesh.getChildren().size(), "fromMesh() should create one Triangle per face");
        for (Intersectable child : mesh.getChildren())
            assertTrue(child instanceof Triangle, "fromMesh() should only produce Triangle geometries");
        // A ray through the interior of one face (clear of the shared diagonal) hits exactly one triangle
        assertEquals(1, mesh.findIntersections(new Ray(new Point(1, -1, 5), new Vector(0, 0, -1))).size(),
                "A ray through the mesh interior should hit exactly one triangle");

        // =============== Boundary Values Tests ==================
        // TC11: No faces — an empty mesh
        assertTrue(Geometries.fromMesh(vertices, new int[0][]).getChildren().isEmpty(),
                "fromMesh() with no faces should produce an empty collection");
    }

    /**
     * Test method for {@link Geometries#buildBVH()} and {@link Geometries#buildBVH(int)}.
     */
    @Test
    void testBuildBVH() {
        // ============ Equivalence Partitions Tests ==============
        // TC01: buildBVH() preserves correctness — same total intersection count as the flat
        // structure, including the infinite PLANE (whose null bounding box makes it bypass grouping)
        Geometries flatGroup = new Geometries(SPHERE, TRIANGLE, PLANE);
        Geometries bvh = flatGroup.buildBVH();
        assertEquals(flatGroup.findIntersections(RAY_ALL).size(), bvh.findIntersections(RAY_ALL).size(),
                "buildBVH() should preserve the total intersection count");
        // TC02: A small custom maxLeafSize still preserves correctness
        assertEquals(4, flatGroup.buildBVH(1).findIntersections(RAY_ALL).size(),
                "buildBVH(1) should still find all 4 intersections");

        // =============== Boundary Values Tests ==================
        // TC11: buildBVH() on an empty collection stays empty
        assertNull(new Geometries().buildBVH().findIntersections(RAY_ALL),
                "buildBVH() on an empty collection should find nothing");
        // TC12: A ray that misses everything still finds nothing after buildBVH()
        assertNull(bvh.findIntersections(new Ray(new Point(100, 100, 100), Vector.AXIS_X)),
                "buildBVH() should not introduce false intersections");
    }

    /**
     * Test method for {@link Geometries#getBoundingBox()}.
     */
    @Test
    void testGetBoundingBox() {
        Sphere s1 = new Sphere(new Point(0, 0, 0), 1d);
        Sphere s2 = new Sphere(new Point(10, 0, 0), 1d);

        // ============ Equivalence Partitions Tests ==============
        // TC01: The bounding box is the union of the finite children's boxes
        AABB box = new Geometries(s1, s2).getBoundingBox();
        assertNotNull(box, "A group of finite geometries should have a finite bounding box");
        // A ray crossing the gap between the two spheres (inside the union box, outside either sphere)
        // must still register as hitting the box.
        assertTrue(box.intersects(new Ray(new Point(5, 0, 5), new Vector(0, 0, -1))),
                "The union bounding box should span the full region between the finite children");
        // A ray far from both spheres must miss the box entirely.
        assertFalse(box.intersects(new Ray(new Point(100, 100, 100), Vector.AXIS_X)),
                "A ray far from all children should miss the bounding box");

        // =============== Boundary Values Tests ==================
        // TC11: Any infinite child (null box) makes the whole group's box null
        assertNull(new Geometries(s1, PLANE).getBoundingBox(),
                "A group containing an infinite geometry should have a null bounding box");
        // TC12: An empty collection has a null bounding box
        assertNull(new Geometries().getBoundingBox(), "An empty collection should have a null bounding box");
    }
}

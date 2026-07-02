package geometries.api;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AABB} — construction, union, fromPoints, size, midpoint,
 * and the slab-method ray intersection test.
 *
 * @author Ambash and Elyasaf
 */
class AABBTests {

    /** Default constructor to satisfy JavaDoc generator. */
    AABBTests() { /* no-op */ }

    /** Box from (0,0,0) to (2,2,2) — used in most tests. */
    private static final AABB BOX = new AABB(new Point(0, 0, 0), new Point(2, 2, 2));

    // ══════════════════════════════════════════════════════════════════════════
    //  fromPoints
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void fromPoints_threePoints() {
        AABB box = AABB.fromPoints(
                new Point(-1, 3, 0),
                new Point(2, -1, 5),
                new Point(0, 1, -2));
        // ray through the middle should hit
        assertTrue(box.intersects(new Ray(new Point(0, 1, 10), new Vector(0, 0, -1))),
                "ray through centre should hit");
        // ray far to the side should miss
        assertFalse(box.intersects(new Ray(new Point(10, 1, 0), new Vector(0, 0, -1))),
                "ray far to the side should miss");
    }

    @Test
    void fromPoints_noPoints_throws() {
        // BVA: zero points has no valid box to compute — must reject rather
        // than silently producing an inverted min/max box.
        assertThrows(IllegalArgumentException.class, AABB::fromPoints,
                "fromPoints() with no points should be rejected");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  union
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void union_twoSeparateBoxes() {
        AABB b1 = new AABB(new Point(0, 0, 0), new Point(1, 1, 1));
        AABB b2 = new AABB(new Point(3, 3, 3), new Point(4, 4, 4));
        AABB u  = b1.union(b2);

        // ray straight through both → must hit union
        assertTrue(u.intersects(new Ray(new Point(0.5, 0.5, -1), new Vector(0, 0, 1))),
                "ray through b1 hits union");
        assertTrue(u.intersects(new Ray(new Point(3.5, 3.5, -1), new Vector(0, 0, 1))),
                "ray through b2 hits union");
        // ray between the two boxes but outside union → miss
        assertFalse(u.intersects(new Ray(new Point(5, 5, 5), new Vector(1, 0, 0))),
                "ray outside union should miss");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  size
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void size_axes() {
        AABB box = new AABB(new Point(0, 2, 4), new Point(4, 8, 16));
        assertEquals(4.0, box.size(0), 1e-9, "size X");
        assertEquals(6.0, box.size(1), 1e-9, "size Y");
        assertEquals(12.0, box.size(2), 1e-9, "size Z");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  midpoint
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void midpoint_axes() {
        AABB box = new AABB(new Point(0, 2, 4), new Point(4, 8, 10));
        assertEquals(2.0, box.midpoint(0), 1e-9, "midpoint X");
        assertEquals(5.0, box.midpoint(1), 1e-9, "midpoint Y");
        assertEquals(7.0, box.midpoint(2), 1e-9, "midpoint Z");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  intersects — hits
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void intersects_rayFromFrontHitsCenter() {
        // ray along -Z, aimed at centre of face z=2
        assertTrue(BOX.intersects(new Ray(new Point(1, 1, 5), new Vector(0, 0, -1))),
                "ray from front should hit");
    }

    @Test
    void intersects_rayFromInsideBox() {
        // ray origin inside the box
        assertTrue(BOX.intersects(new Ray(new Point(1, 1, 1), new Vector(1, 0, 0))),
                "ray from inside should hit");
    }

    @Test
    void intersects_rayDiagonal() {
        // diagonal ray entering through one corner region
        assertTrue(BOX.intersects(new Ray(new Point(-1, -1, -1), new Vector(1, 1, 1))),
                "diagonal ray through corner should hit");
    }

    @Test
    void intersects_rayGrazesEdge() {
        // ray that just grazes the box exactly at y=2 edge
        assertTrue(BOX.intersects(new Ray(new Point(1, 4, 1), new Vector(0, -1, 0))),
                "ray aimed through box should hit");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  intersects — misses
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    void intersects_rayPassesBeside_X() {
        // ray parallel to Z but offset in X far beyond the box
        assertFalse(BOX.intersects(new Ray(new Point(5, 1, 5), new Vector(0, 0, -1))),
                "ray beside box in X should miss");
    }

    @Test
    void intersects_rayPassesBeside_Y() {
        assertFalse(BOX.intersects(new Ray(new Point(1, 5, 5), new Vector(0, 0, -1))),
                "ray beside box in Y should miss");
    }

    @Test
    void intersects_rayBehindBox() {
        // ray pointing away from box (tMax < 0)
        assertFalse(BOX.intersects(new Ray(new Point(1, 1, 5), new Vector(0, 0, 1))),
                "ray pointing away from box should miss");
    }

    @Test
    void intersects_rayParallelToAxisOutsideSlab() {
        // ray parallel to Z axis but x=5 — outside X slab [0,2]
        assertFalse(BOX.intersects(new Ray(new Point(5, 1, 0), new Vector(0, 0, 1))),
                "ray parallel to Z but outside X slab should miss");
    }

    @Test
    void intersects_rayParallelToAxisInsideSlab() {
        // ray parallel to Z axis with x=1 — inside X and Y slabs → should hit
        assertTrue(BOX.intersects(new Ray(new Point(1, 1, 5), new Vector(0, 0, -1))),
                "ray parallel to Z and inside X+Y slab should hit");
    }
}

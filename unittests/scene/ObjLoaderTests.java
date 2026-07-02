package scene;

import geometries.api.AABB;
import geometries.api.Geometry;
import geometries.impl.Geometries;
import geometries.impl.Triangle;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import primitives.Color;
import primitives.Double3;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for class {@link ObjLoader}.
 * The tests verify:
 * <ul>
 * <li>{@link ObjLoader#load(File, double, double, double, double, int)}</li>
 * </ul>
 * Uses small synthetic OBJ/MTL files written to a JUnit-managed temp
 * directory, rather than the large real-world mesh used elsewhere in the
 * renderer test suite.
 * Tests follow the methodology of
 * Equivalence Partitions (EP) and Boundary Values (BVA).
 *
 * @author Ambash and Elyasaf
 */
class ObjLoaderTests {

    /** Default constructor to satisfy JavaDoc generator */
    ObjLoaderTests() { /* to satisfy JavaDoc generator */ }

    /** JUnit-managed temporary directory, cleaned up automatically after each test. */
    @TempDir
    Path tempDir;

    /**
     * Writes {@code content} to a file named {@code name} inside the temp directory.
     *
     * @param  name    the file name
     * @param  content the file content
     * @return         the written file
     */
    private File writeFile(String name, String content) throws IOException {
        Path path = tempDir.resolve(name);
        Files.writeString(path, content);
        return path.toFile();
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * A single triangular face produces exactly one {@link Triangle}.
     */
    @Test
    void testLoadSimpleTriangle() throws IOException {

        // ============ Equivalence Partitions Tests ==============

        // TC01: one triangular face -> one Triangle
        File obj = writeFile("tri.obj", "v 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3\n");
        Geometries g = ObjLoader.load(obj, 1, 0, 0, 0, 1);
        assertEquals(1, g.getChildren().size(), "A single triangular face should produce exactly one Triangle");
        assertInstanceOf(Triangle.class, g.getChildren().get(0), "The loaded geometry should be a Triangle");
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * Verifies fan triangulation for quads and n-gons.
     */
    @Test
    void testFanTriangulation() throws IOException {

        // ============ Equivalence Partitions Tests ==============

        // TC01: a quad face fan-triangulates into 2 triangles
        File quad = writeFile("quad.obj", "v 0 0 0\nv 1 0 0\nv 1 1 0\nv 0 1 0\nf 1 2 3 4\n");
        assertEquals(2, ObjLoader.load(quad, 1, 0, 0, 0, 1).getChildren().size(),
                "A quad face should fan-triangulate into 2 triangles");

        // TC02: a pentagon face fan-triangulates into 3 triangles
        File pentagon = writeFile("pentagon.obj",
                "v 0 0 0\nv 1 0 0\nv 1 1 0\nv 0.5 1.5 0\nv 0 1 0\nf 1 2 3 4 5\n");
        assertEquals(3, ObjLoader.load(pentagon, 1, 0, 0, 0, 1).getChildren().size(),
                "A pentagon face should fan-triangulate into 3 triangles");
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * Verifies negative (relative) face indices, per the OBJ spec.
     */
    @Test
    void testNegativeRelativeIndices() throws IOException {

        // =============== Boundary Values Tests ==================

        // TC11: -1/-2/-3 refer to the 3 vertices just declared, in reverse order
        File obj = writeFile("neg.obj", "v 0 0 0\nv 1 0 0\nv 0 1 0\nf -3 -2 -1\n");
        Geometries g = ObjLoader.load(obj, 1, 0, 0, 0, 1);
        assertEquals(1, g.getChildren().size(), "Negative/relative face indices should resolve correctly");
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * Verifies the {@code decimate} parameter keeps only one out of every N faces.
     */
    @Test
    void testDecimate() throws IOException {
        File obj = writeFile("decimate.obj",
                "v 0 0 0\nv 1 0 0\nv 0 1 0\n"
                        + "f 1 2 3\nf 1 2 3\nf 1 2 3\nf 1 2 3\n");

        // ============ Equivalence Partitions Tests ==============

        // TC01: decimate=1 keeps all 4 faces
        assertEquals(4, ObjLoader.load(obj, 1, 0, 0, 0, 1).getChildren().size(),
                "decimate=1 should keep all faces");

        // TC02: decimate=2 keeps every other face
        assertEquals(2, ObjLoader.load(obj, 1, 0, 0, 0, 2).getChildren().size(),
                "decimate=2 should keep every other face");
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * A degenerate (collinear) face is skipped silently rather than propagating
     * the {@link Triangle} constructor's exception.
     */
    @Test
    void testDegenerateTriangleSkipped() throws IOException {

        // =============== Boundary Values Tests ==================

        // TC11: three collinear points cannot form a triangle
        File obj = writeFile("degenerate.obj", "v 0 0 0\nv 1 0 0\nv 2 0 0\nf 1 2 3\n");
        Geometries g = assertDoesNotThrow(() -> ObjLoader.load(obj, 1, 0, 0, 0, 1),
                "A degenerate face should be skipped silently, not thrown");
        assertTrue(g.getChildren().isEmpty(), "A degenerate (collinear) face should not produce a Triangle");
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * A malformed numeric line is wrapped in {@link IllegalStateException}
     * rather than leaking a raw {@link NumberFormatException}.
     */
    @Test
    void testMalformedLineThrowsIllegalStateException() throws IOException {

        // =============== Boundary Values Tests ==================

        // TC11: a non-numeric vertex coordinate
        File obj = writeFile("bad.obj", "v 0 0 notanumber\n");
        assertThrows(IllegalStateException.class, () -> ObjLoader.load(obj, 1, 0, 0, 0, 1),
                "A malformed vertex line should be wrapped in IllegalStateException");
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * A missing OBJ file throws {@link IllegalStateException}.
     */
    @Test
    void testMissingFileThrowsIllegalStateException() {

        // =============== Boundary Values Tests ==================

        // TC11: the file does not exist at all
        File missing = new File(tempDir.toFile(), "does_not_exist.obj");
        assertThrows(IllegalStateException.class, () -> ObjLoader.load(missing, 1, 0, 0, 0, 1),
                "A missing OBJ file should throw IllegalStateException");
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * Verifies that {@code mtllib}/{@code usemtl}/{@code newmtl} correctly apply
     * material and emission properties to the corresponding faces.
     */
    @Test
    void testMaterialAndEmissionFromMtl() throws IOException {
        writeFile("mat.mtl", "newmtl red\nKd 0.8 0.1 0.1\nKs 0.5 0.5 0.5\nNs 100\nKe 1 0 0\n");
        File obj = writeFile("withmtl.obj",
                "mtllib mat.mtl\nv 0 0 0\nv 1 0 0\nv 0 1 0\nusemtl red\nf 1 2 3\n");

        // ============ Equivalence Partitions Tests ==============

        Geometries g = ObjLoader.load(obj, 1, 0, 0, 0, 1);
        assertEquals(1, g.getChildren().size());
        Geometry tri = (Geometry) g.getChildren().get(0);

        // TC01: Kd/Ks/Ns are applied to the material
        assertEquals(new Double3(0.8, 0.1, 0.1), tri.getMaterial().kD, "Kd was not applied from the MTL");
        assertEquals(new Double3(0.5, 0.5, 0.5), tri.getMaterial().kS, "Ks was not applied from the MTL");
        assertEquals(100, tri.getMaterial().nShininess, "Ns was not applied from the MTL");

        // TC02: a non-zero Ke sets the emission color (scaled 0..1 -> 0..255)
        assertEquals(new Color(255, 0, 0), tri.getEmission(), "Ke should set the emission color");
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * An out-of-range MTL coefficient is clamped to {@code [0,1]}, not rejected
     * (leaving the material at its zero default) or applied unclamped.
     */
    @Test
    void testOutOfRangeMtlValueIsClamped() throws IOException {
        writeFile("clamp.mtl", "newmtl shiny\nKs 2.0 2.0 2.0\n");
        File obj = writeFile("clampobj.obj",
                "mtllib clamp.mtl\nv 0 0 0\nv 1 0 0\nv 0 1 0\nusemtl shiny\nf 1 2 3\n");

        // =============== Boundary Values Tests ==================

        // TC11: Ks=2.0 (out of [0,1]) clamps to 1.0
        Geometries g = ObjLoader.load(obj, 1, 0, 0, 0, 1);
        Geometry tri = (Geometry) g.getChildren().get(0);
        assertEquals(new Double3(1, 1, 1), tri.getMaterial().kS,
                "An out-of-range Ks should be clamped to [0,1]");
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * A referenced but missing MTL file is tolerated (geometry loads with
     * default materials) rather than treated as a fatal error.
     */
    @Test
    void testMissingMtlFileDoesNotThrow() throws IOException {

        // =============== Boundary Values Tests ==================

        // TC11: mtllib points at a file that does not exist
        File obj = writeFile("nomtl.obj",
                "mtllib does_not_exist.mtl\nv 0 0 0\nv 1 0 0\nv 0 1 0\nf 1 2 3\n");
        Geometries g = assertDoesNotThrow(() -> ObjLoader.load(obj, 1, 0, 0, 0, 1),
                "A missing referenced MTL file should not be fatal");
        assertEquals(1, g.getChildren().size());
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * Verifies bounding-box centering, {@code scale}, {@code translate}, and the
     * default (3ds Max) Z-up-to-Y-up axis remap.
     */
    @Test
    void testCenteringScaleTranslateAndAxisRemap() throws IOException {
        // Raw bbox center: cx=0, cy=1, cz=0. Default (no "# Blender") axis remap:
        //   scene.x = (obj.x - cx)*scale + tx
        //   scene.y = (obj.z - cz)*scale + ty   (OBJ Z-up -> scene Y-up)
        //   scene.z = (obj.y - cy)*scale + tz
        File obj = writeFile("center.obj", "v -2 0 0\nv 2 0 0\nv 0 2 0\nf 1 2 3\n");

        // ============ Equivalence Partitions Tests ==============

        Geometries g = ObjLoader.load(obj, 2, 10, 0, 0, 1);
        AABB box = g.getChildren().get(0).getBoundingBox();
        // Expected vertices: (6,0,-2), (14,0,-2), (10,0,2) -> box [6,0,-2]-[14,0,2]
        assertEquals(8d, box.size(0), 1e-9, "X size should reflect centering + scale");
        assertEquals(0d, box.size(1), 1e-9, "Y size should be 0 (all points share the same remapped Y)");
        assertEquals(4d, box.size(2), 1e-9, "Z size should reflect the remapped OBJ-Y axis, scaled");
        assertEquals(10d, box.midpoint(0), 1e-9, "X midpoint should reflect the translate offset");
    }

    /**
     * Test method for {@link ObjLoader#load(File, double, double, double, double, int)}.
     * Verifies the Blender (Y-up) axis convention, selected via a
     * {@code "# Blender"} header comment, leaves axes unswapped.
     */
    @Test
    void testBlenderYUpAxisRemap() throws IOException {
        // Same raw vertices as testCenteringScaleTranslateAndAxisRemap, but with the
        // "# Blender" header: no axis swap, so Y and Z stay as declared in the OBJ.
        File obj = writeFile("blender.obj", "# Blender v2.90\nv -2 0 0\nv 2 0 0\nv 0 2 0\nf 1 2 3\n");

        // =============== Boundary Values Tests ==================

        Geometries g = ObjLoader.load(obj, 1, 0, 0, 0, 1);
        AABB box = g.getChildren().get(0).getBoundingBox();
        // cx=0, cy=1, cz=0; no swap: scene.y = obj.y - cy, scene.z = obj.z - cz
        // Expected vertices: (-2,-1,0), (2,-1,0), (0,1,0) -> box [-2,-1,0]-[2,1,0]
        assertEquals(4d, box.size(0), 1e-9, "X size unaffected by the Blender convention");
        assertEquals(2d, box.size(1), 1e-9, "Y size should reflect the un-swapped OBJ-Y axis");
        assertEquals(0d, box.size(2), 1e-9, "Z size should be 0 (all points share the same OBJ Z)");
    }
}

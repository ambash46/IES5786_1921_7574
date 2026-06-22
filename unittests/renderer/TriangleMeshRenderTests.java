package renderer;

import geometries.impl.Sphere;
import geometries.impl.TriangleMesh;
import java.util.ArrayList;
import java.util.List;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import primitives.Color;
import primitives.Material;
import primitives.Point;
import primitives.Vector;
import scene.Scene;

/**
 * Rendering tests for {@link TriangleMesh}.
 * <p>
 * Three scenes with increasing complexity:
 * <ol>
 *   <li>Flat quad  — verifies basic integration with the ray tracer.</li>
 *   <li>Pyramid    — solid mesh viewed from an angle; checks material/emission.</li>
 *   <li>Smooth cylinder — 32-segment cylinder approximation that demonstrates
 *       inverse-distance-weighted smooth normals: the silhouette looks round
 *       rather than faceted.</li>
 * </ol>
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
@Disabled("zzz")
class TriangleMeshRenderTests {

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    TriangleMeshRenderTests() { /* to satisfy JavaDoc generator */ }

    // ═══════════════════════════════════════════════════════════════════════
    //  Test 0: Basic mesh image
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Minimal render test: a 4-sided pyramid mesh with one spotlight.
     * Produces {@code meshBasic.png}. Run this first to verify the mesh works.
     */
    @Test
    void testBasicMesh() {
        Scene scene = new Scene("Basic mesh");

        scene.geometries.add(
                new TriangleMesh(
                        List.of(new Point(-2, 0, -2), new Point(2, 0, -2),
                                new Point(2, 0, 2), new Point(-2, 0, 2),
                                new Point(0, 4, 0)),
                        new int[][]{{4, 0, 1}, {4, 1, 2}, {4, 2, 3}, {4, 3, 0}}
                ).setEmission(new Color(20, 50, 80))
                        .setMaterial(new Material().setKD(0.6).setKS(0.4).setShininess(100)));

        scene.lights.add(new SpotLight(
                new Color(400, 400, 500), new Point(0, 10, 10), new Vector(0, -1, -1))
                .setKl(0.0002).setKq(0.00003));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 5, 12))
                .setDirection(new Point(0, 1, 0), Vector.AXIS_Y)
                .setVpSize(8, 8).setVpDistance(10)
                .setResolution(400, 400)
                .build()
                .renderImage()
                .writeToImage("meshBasic");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Test 1: Flat quad mesh
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * A single two-triangle quad mesh lit by a point light from above.
     * Verifies that TriangleMesh integrates correctly with the scene,
     * the ray tracer, and material/emission handling.
     */
    @Test
    void testFlatQuad() {
        Scene scene = new Scene("Flat quad mesh")
                .setBackground(new Color(15, 15, 30))
                .setAmbientLight(new AmbientLight(new Color(10, 10, 10)));

        List<Point> verts = List.of(
                new Point(-3, 0, -3),
                new Point(3, 0, -3),
                new Point(3, 0, 3),
                new Point(-3, 0, 3)
        );
        int[][] faces = {{0, 1, 2}, {0, 2, 3}};

        scene.geometries.add(
                new TriangleMesh(verts, faces)
                        .setEmission(new Color(10, 40, 80))
                        .setMaterial(new Material().setKD(0.6).setKS(0.4).setShininess(80)));

        scene.lights.add(
                new PointLight(new Color(300, 300, 400), new Point(0, 8, 0))
                        .setKl(0.001).setKq(0.0002));
        scene.lights.add(
                new DirectionalLight(new Color(50, 50, 75), new Vector(1, -1, -1)));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 8, 10))
                .setDirection(new Point(0, 0, 0), Vector.AXIS_Y)
                .setVpSize(10, 10).setVpDistance(10)
                .setResolution(500, 500)
                .build()
                .renderImage()
                .writeToImage("meshFlatQuad");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Test 2: Pyramid
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * A square pyramid (4 triangular sides + square base) with smooth normals.
     * The smooth shading at the apex shows the averaged vertex normal.
     */
    @Test
    void testPyramid() {
        Scene scene = new Scene("Pyramid mesh")
                .setBackground(new Color(10, 10, 20))
                .setAmbientLight(new AmbientLight(new Color(8, 8, 8)));

        // Apex + 4 base corners (base raised slightly to avoid z-fighting with floor)
        List<Point> verts = List.of(
                new Point(0, 4, 0),  // v0 apex
                new Point(2, 0, 2),  // v1 front-right
                new Point(-2, 0, 2),  // v2 front-left
                new Point(-2, 0, -2),  // v3 back-left
                new Point(2, 0, -2)   // v4 back-right
        );
        // Winding order chosen so that edge1×edge2 points OUTWARD for every face.
        // front: (0,-2,+2) normal≈(0,+,+z) | left: normal≈(-x,+y,0)
        // back:  normal≈(0,+,-z)            | right: normal≈(+x,+y,0)
        // base:  normal = (0,-1,0) downward
        int[][] faces = {
                {0, 2, 1},   // front side
                {0, 3, 2},   // left  side
                {0, 4, 3},   // back  side
                {0, 1, 4},   // right side
                {1, 3, 4},   // base  (triangle A)
                {1, 2, 3}    // base  (triangle B)
        };

        scene.geometries.add(
                new TriangleMesh(verts, faces)
                        .setEmission(new Color(50, 15, 5))
                        .setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(200)));

        // Floor plane made of a large TriangleMesh quad
        List<Point> floorVerts = List.of(
                new Point(-10, 0, -10), new Point(10, 0, -10),
                new Point(10, 0, 10), new Point(-10, 0, 10)
        );
        scene.geometries.add(
                new TriangleMesh(floorVerts, new int[][]{{0, 1, 2}, {0, 2, 3}})
                        .setEmission(new Color(5, 12, 5))
                        .setMaterial(new Material().setKD(0.7).setKS(0.3).setShininess(30)));

        scene.lights.add(
                new DirectionalLight(new Color(200, 200, 250), new Vector(1, -2, -1)));
        scene.lights.add(
                new PointLight(new Color(300, 250, 150), new Point(-6, 8, 6))
                        .setKl(0.0005).setKq(0.0001));
        scene.lights.add(
                new SpotLight(new Color(250, 250, 350), new Point(0, 12, 0),
                        new Vector(0, -1, 0))
                        .setKl(0.001).setKq(0.0002).setNarrowBeam(3));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(8, 7, 12))
                .setDirection(new Point(0, 2, 0), Vector.AXIS_Y)
                .setVpSize(14, 14).setVpDistance(12)
                .setResolution(600, 600)
                .build()
                .renderImage()
                .writeToImage("meshPyramid");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Test 3: Smooth cylinder (demonstrates smooth-normal interpolation)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * A cylinder approximated by 32 triangulated segments.
     * <p>
     * With flat normals the silhouette would show 32 visible facets.
     * With inverse-distance-weighted smooth normals the specular highlight
     * spreads continuously around the surface, giving a visually round result.
     * </p>
     * A reference sphere of the same radius is placed next to the mesh
     * cylinder so that the smoothness can be directly compared.
     */
    @Test
    void testSmoothCylinder() {
        Scene scene = new Scene("Smooth cylinder mesh")
                .setBackground(new Color(8, 8, 18))
                .setAmbientLight(new AmbientLight(new Color(6, 6, 9)));

        Material meshMat = new Material().setKD(0.2).setKS(0.8).setShininess(600);
        Color meshEmit = new Color(10, 30, 70);

        // ── Build triangle-mesh cylinder, N=32 segments ─────────────────
        int N = 32;
        scene.geometries.add(buildCylinder(N, 1.5, 3.0,
                -2.5, -1.5, -5, meshEmit, meshMat));

        // ── Reference: a true sphere of the same radius ─────────────────
        scene.geometries.add(
                new Sphere(new Point(2.5, 0, -5), 1.5)
                        .setEmission(new Color(10, 30, 70))
                        .setMaterial(new Material().setKD(0.2).setKS(0.8).setShininess(600)));

        // ── Lights ───────────────────────────────────────────────────────
        // Narrow spotlight from the right — produces a sharp specular stripe
        // that reveals whether the normals are smooth or faceted.
        scene.lights.add(
                new SpotLight(new Color(450, 450, 600), new Point(8, 4, 4),
                        new Vector(-1, -0.5, -1))
                        .setKl(0.0001).setKq(0.00003).setNarrowBeam(2));
        scene.lights.add(
                new DirectionalLight(new Color(100, 100, 150), new Vector(-1, -1, -2)));
        scene.lights.add(
                new PointLight(new Color(150, 150, 200), new Point(0, 6, 2))
                        .setKl(0.0003).setKq(0.00005));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 10))
                .setDirection(new Point(0, 0, -1), Vector.AXIS_Y)
                .setVpSize(14, 8).setVpDistance(10)
                .setResolution(700, 400)
                .build()
                .renderImage()
                .writeToImage("meshSmoothCylinder");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Test 4: Showcase — smooth-shaded terrain patch
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * A 10×10 grid mesh with a sinusoidal height field, placed as a "terrain"
     * and lit by three light sources. The smooth normals produce a naturally
     * flowing surface rather than visible triangle edges.
     */
    @Test
    void testTerrainMesh() {
        Scene scene = new Scene("Terrain mesh")
                .setBackground(new Color(30, 50, 90))
                .setAmbientLight(new AmbientLight(new Color(10, 15, 20)));

        scene.geometries.add(buildTerrain(20, 20, 8.0, 8.0, 0.8));

        scene.lights.add(
                new DirectionalLight(new Color(250, 240, 200), new Vector(1, -2, -1)));
        scene.lights.add(
                new PointLight(new Color(200, 175, 125), new Point(-4, 5, 5))
                        .setKl(0.002).setKq(0.0004));
        scene.lights.add(
                new SpotLight(new Color(300, 300, 400), new Point(0, 8, 0),
                        new Vector(0, -1, -0.3))
                        .setKl(0.001).setKq(0.0002).setNarrowBeam(4));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 10, 14))
                .setDirection(new Point(0, 0, 0), Vector.AXIS_Y)
                .setVpSize(16, 12).setVpDistance(12)
                .setResolution(800, 600)
                .build()
                .renderImage()
                .writeToImage("meshTerrain");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Test 5: Mesh sphere
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * UV-sphere mesh (24 slices × 16 stacks) next to a true Sphere of the same
     * radius. A narrow spotlight reveals whether smooth normals make the mesh
     * look round instead of faceted.
     */
    @Test
    void testMeshSphere() {
        Scene scene = new Scene("Mesh sphere")
                .setBackground(new Color(5, 5, 15))
                .setAmbientLight(new AmbientLight(new Color(5, 5, 8)));

        Material mat = new Material().setKD(0.2).setKS(0.8).setShininess(400);
        Color emit = new Color(20, 40, 100);

        TriangleMesh sphere = buildEllipsoid(24, 16, 2, 2, 2);
        sphere.setEmission(emit);
        sphere.setMaterial(mat);
        scene.geometries.add(sphere);

        scene.lights.add(new SpotLight(new Color(500, 500, 650),
                new Point(8, 6, 8), new Vector(-1, -0.8, -1))
                .setKl(0.00005).setKq(0.00001).setNarrowBeam(2));
        scene.lights.add(new DirectionalLight(new Color(75, 75, 100),
                new Vector(-1, -0.5, -1)));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 0, 10))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(6, 6).setVpDistance(10)
                .setResolution(500, 500)
                .build().renderImage().writeToImage("meshSphere");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Test 6: Mesh ellipsoid
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Renders the same ellipsoid (rx=3, ry=1.5, rz=2) three times with
     * increasing mesh density to show how {@link TriangleMesh#subdivide(int)}
     * improves smoothness:
     * <ol>
     *   <li>{@code meshEllipsoidCoarse} — 6×4 base mesh (36 triangles):
     *       polygon facets clearly visible.</li>
     *   <li>{@code meshEllipsoidMedium} — one {@code subdivide(2)}: 144 triangles,
     *       edges soften but some faceting remains.</li>
     *   <li>{@code meshEllipsoidFine}   — two {@code subdivide(2)}: 576 triangles,
     *       surface looks nearly smooth.</li>
     * </ol>
     * Same scene, lights, and camera in every render so the comparison is fair.
     */
    @Test
    void testMeshEllipsoid() {
        Material mat = new Material().setKD(0.25).setKS(0.75).setShininess(400);
        Color emit = new Color(50, 20, 80);

        // Level 0: 6 slices × 4 stacks → 36 triangles (very coarse)
        TriangleMesh coarse = buildEllipsoid(6, 4, 3, 1.5, 2);
        coarse.setEmission(emit);
        coarse.setMaterial(mat);

        // Level 1: each triangle → 4  →  144 triangles total
        TriangleMesh medium = coarse.subdivide(2);
        medium.setEmission(emit);
        medium.setMaterial(mat);

        // Level 2: each of those → 4  →  576 triangles total
        TriangleMesh fine = medium.subdivide(2);
        fine.setEmission(emit);
        fine.setMaterial(mat);

        TriangleMesh[] meshes = {coarse, medium, fine};
        String[] names = {"meshEllipsoidCoarse", "meshEllipsoidMedium", "meshEllipsoidFine"};

        for (int i = 0; i < 3; i++) {
            Scene scene = new Scene("Mesh ellipsoid – level " + i)
                    .setBackground(new Color(8, 5, 12))
                    .setAmbientLight(new AmbientLight(new Color(6, 5, 8)));

            scene.geometries.add(meshes[i]);

            // Narrow spotlight from front-right — reveals faceting vs. smoothness
            scene.lights.add(new SpotLight(new Color(450, 400, 550),
                    new Point(7, 5, 9), new Vector(-1, -0.7, -1))
                    .setKl(0.00008).setKq(0.00002).setNarrowBeam(2));
            scene.lights.add(new DirectionalLight(new Color(60, 50, 80),
                    new Vector(1, -1, -1)));
            scene.lights.add(new PointLight(new Color(100, 75, 125),
                    new Point(-5, 3, 4)).setKl(0.001).setKq(0.0002));

            Camera.getBuilder()
                    .setRayTracer(scene, RayTracerType.SIMPLE)
                    .setLocation(new Point(0, 2, 12))
                    .setDirection(new Point(0, 0, 0), Vector.AXIS_Y)
                    .setVpSize(10, 6).setVpDistance(12)
                    .setResolution(700, 420)
                    .build().renderImage().writeToImage(names[i]);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Test 7: Mesh bowl
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * A hemispherical bowl (bottom half of a sphere), open at the top.
     * Camera is placed above and in front so both the outer curved surface
     * and the inner cavity are visible.
     */
    @Test
    void testMeshBowl() {
        Scene scene = new Scene("Mesh bowl")
                .setBackground(new Color(20, 15, 10))
                .setAmbientLight(new AmbientLight(new Color(10, 9, 8)));

        TriangleMesh bowl = buildBowl(32, 16, 3);
        bowl.setEmission(new Color(80, 40, 10));
        bowl.setMaterial(new Material().setKD(0.5).setKS(0.5).setShininess(150));
        scene.geometries.add(bowl);

        scene.lights.add(new PointLight(new Color(175, 150, 100),
                new Point(0, 6, 0)).setKl(0.001).setKq(0.0002));
        scene.lights.add(new DirectionalLight(new Color(75, 63, 50),
                new Vector(1, -1, -0.5)));
        scene.lights.add(new SpotLight(new Color(150, 125, 75),
                new Point(-5, 8, 5), new Vector(0.5, -1, -0.5))
                .setKl(0.0005).setKq(0.0001).setNarrowBeam(4));

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setLocation(new Point(0, 6, 10))
                .setDirection(new Point(0, -1, 0), Vector.AXIS_Y)
                .setVpSize(9, 7).setVpDistance(10)
                .setResolution(1000, 700)
                .build().renderImage().writeToImage("meshBowl");
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Helper builders
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Builds a closed triangle-mesh cylinder (mantle + top cap + bottom cap).
     *
     * @param n        number of angular segments
     * @param radius   cylinder radius
     * @param height   cylinder height
     * @param bx       X coordinate of the bottom-centre
     * @param by       Y coordinate of the bottom-centre
     * @param bz       Z coordinate of the bottom-centre
     * @param emission emission color
     * @param mat      material
     * @return the assembled {@link TriangleMesh}
     */
    private static TriangleMesh buildCylinder(int n, double radius, double height,
                                              double bx, double by, double bz,
                                              Color emission, Material mat) {
        List<Point> verts = new ArrayList<>(2 * n + 2);
        List<int[]> faces = new ArrayList<>(4 * n);

        // Bottom ring (indices 0..n-1), top ring (indices n..2n-1)
        for (int k = 0; k < n; k++) {
            double angle = 2 * Math.PI * k / n;
            double cx = bx + radius * Math.cos(angle);
            double cz = bz + radius * Math.sin(angle);
            verts.add(new Point(cx, by, cz));   // bottom
            verts.add(new Point(cx, by + height, cz));   // top
        }
        int botCenter = 2 * n;   // index of bottom-cap centre
        int topCenter = 2 * n + 1;
        verts.add(new Point(bx, by, bz));
        verts.add(new Point(bx, by + height, bz));

        for (int k = 0; k < n; k++) {
            int kb = 2 * k;          // bottom vertex k
            int kt = 2 * k + 1;      // top    vertex k
            int kb1 = 2 * ((k + 1) % n);
            int kt1 = 2 * ((k + 1) % n) + 1;

            // Side quad → 2 triangles
            faces.add(new int[]{kb, kt, kt1});
            faces.add(new int[]{kb, kt1, kb1});
            // Bottom cap (winding: centre → k+1 → k so normal points down)
            faces.add(new int[]{botCenter, kb1, kb});
            // Top cap (winding: centre → k → k+1 so normal points up)
            faces.add(new int[]{topCenter, kt, kt1});
        }

        TriangleMesh mesh = new TriangleMesh(verts, faces);
        mesh.setEmission(emission);
        mesh.setMaterial(mat);
        return mesh;
    }

    /**
     * Builds a UV-sphere mesh (poles + latitude rings connected by quads).
     * Shorthand for a uniform ellipsoid with rx = ry = rz = radius.
     */
    private static TriangleMesh buildUVSphere(int slices, int stacks, double radius) {
        return buildEllipsoid(slices, stacks, radius, radius, radius);
    }

    /**
     * Builds an ellipsoid mesh with independent radii along each axis.
     * <p>
     * Vertex layout:
     * <pre>
     *   index 0              → north pole  (0, ry, 0)
     *   indices 1 … S*N      → latitude rings (S = stacks-1 rings, N = slices)
     *   last index           → south pole  (0, -ry, 0)
     * </pre>
     * Face layout:
     * <ul>
     *   <li>North cap  : N triangles  {pole, j, j+1}</li>
     *   <li>Middle band: 2*(S-1)*N triangles  (each quad → 2 triangles)</li>
     *   <li>South cap  : N triangles  {pole, j+1, j}</li>
     * </ul>
     *
     * @param slices longitude divisions
     * @param stacks latitude  divisions
     * @param rx     radius along X
     * @param ry     radius along Y (pole axis)
     * @param rz     radius along Z
     */
    private static TriangleMesh buildEllipsoid(int slices, int stacks,
                                               double rx, double ry, double rz) {
        List<Point> verts = new ArrayList<>(2 + (stacks - 1) * slices);
        List<int[]> faces = new ArrayList<>(2 * slices * stacks);

        // ── North pole ────────────────────────────────────────────────────
        verts.add(new Point(0, ry, 0));   // index 0

        // ── Latitude rings ────────────────────────────────────────────────
        for (int s = 1; s < stacks; s++) {
            double theta = Math.PI * s / stacks;           // 0 → π  (top → bottom)
            double sinT = Math.sin(theta);
            double cosT = Math.cos(theta);
            for (int j = 0; j < slices; j++) {
                double phi = 2 * Math.PI * j / slices;    // 0 → 2π (longitude)
                verts.add(new Point(rx * sinT * Math.cos(phi),
                        ry * cosT,
                        rz * sinT * Math.sin(phi)));
            }
        }

        // ── South pole ────────────────────────────────────────────────────
        int botIdx = verts.size();
        verts.add(new Point(0, -ry, 0));

        // ── North cap triangles ───────────────────────────────────────────
        for (int j = 0; j < slices; j++) {
            int a = 1 + j;
            int b = 1 + (j + 1) % slices;
            faces.add(new int[]{0, a, b});
        }

        // ── Middle band quads → 2 triangles each ─────────────────────────
        for (int s = 0; s < stacks - 2; s++) {
            for (int j = 0; j < slices; j++) {
                int tl = 1 + s * slices + j;
                int tr = 1 + s * slices + (j + 1) % slices;
                int bl = 1 + (s + 1) * slices + j;
                int br = 1 + (s + 1) * slices + (j + 1) % slices;
                faces.add(new int[]{tl, bl, br});
                faces.add(new int[]{tl, br, tr});
            }
        }

        // ── South cap triangles ───────────────────────────────────────────
        int lastRing = 1 + (stacks - 2) * slices;
        for (int j = 0; j < slices; j++) {
            int a = lastRing + j;
            int b = lastRing + (j + 1) % slices;
            faces.add(new int[]{botIdx, b, a});   // reversed winding → outward normal
        }

        return new TriangleMesh(verts, faces);
    }

    /**
     * Builds an open hemispherical bowl (bottom half of a sphere, open at top).
     * <p>
     * The rim sits at y = 0 and the deepest point is at y = -radius.
     * The outer surface has outward-pointing normals; the visible interior
     * (looking from above) shows smooth concave shading via the same normals.
     * </p>
     *
     * @param slices longitude divisions
     * @param stacks number of latitude rings from rim to bottom pole
     * @param radius bowl radius
     */
    private static TriangleMesh buildBowl(int slices, int stacks, double radius) {
        List<Point> verts = new ArrayList<>((stacks + 1) * slices + 1);
        List<int[]> faces = new ArrayList<>(2 * stacks * slices + slices);

        // ── Latitude rings: theta from π/2 (rim) up to (but not including) π ──
        // Stopping before π prevents all slices from collapsing to the same pole point.
        for (int s = 0; s < stacks; s++) {
            double theta = Math.PI / 2.0 + Math.PI / 2.0 * s / stacks;
            double sinT = Math.sin(theta);
            double cosT = Math.cos(theta);
            for (int j = 0; j < slices; j++) {
                double phi = 2 * Math.PI * j / slices;
                verts.add(new Point(radius * sinT * Math.cos(phi),
                        radius * cosT,
                        radius * sinT * Math.sin(phi)));
            }
        }

        // ── South pole — single vertex ──────────────────────────────────────
        int botIdx = verts.size();
        verts.add(new Point(0, -radius, 0));

        // ── Side quads (stacks-1 bands) ─────────────────────────────────────
        for (int s = 0; s < stacks - 1; s++) {
            for (int j = 0; j < slices; j++) {
                int tl = s * slices + j;
                int tr = s * slices + (j + 1) % slices;
                int bl = (s + 1) * slices + j;
                int br = (s + 1) * slices + (j + 1) % slices;
                faces.add(new int[]{tl, bl, br});
                faces.add(new int[]{tl, br, tr});
            }
        }

        // ── Bottom cap — last ring fans into single pole vertex ─────────────
        int lastRing = (stacks - 1) * slices;
        for (int j = 0; j < slices; j++) {
            int a = lastRing + j;
            int b = lastRing + (j + 1) % slices;
            faces.add(new int[]{botIdx, b, a});
        }

        return new TriangleMesh(verts, faces);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  Test 8: Human head mesh
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Renders a complete 3-D head mesh three times with increasing subdivision.
     * <p>
     * The head is a closed ellipsoid whose surface vertices are displaced along
     * their outward normals by a sum of 2-D Gaussian "bumps" and "dents" that
     * sculpt the facial features.  Features are smoothly faded to zero on the
     * sides and back so the skull remains clean.
     * </p>
     * <ol>
     *   <li>{@code meshFaceCoarse} — 20×16 base ellipsoid (~640 tri)</li>
     *   <li>{@code meshFaceMedium} — {@code subdivide(2)}: ~2 560 tri</li>
     *   <li>{@code meshFaceFine}   — {@code subdivide(2)} twice: ~10 240 tri</li>
     * </ol>
     */
    @Test
    void testFaceMesh() {
        Material mat = new Material().setKD(0.6).setKS(0.4).setShininess(20);
        Color emit = new Color(40, 25, 15);

        // Base: 64 slices × 48 stacks — enough vertices for every feature to land
        // on ≥3 vertices (phi-res≈0.098 rad vs smallest sp_phi=0.17 → ~3.5 verts)
        TriangleMesh base = buildHead(64, 48);
        base.setEmission(emit);
        base.setMaterial(mat);

        TriangleMesh smooth = base.subdivide(2);
        smooth.setEmission(emit);
        smooth.setMaterial(mat);

        TriangleMesh fine = smooth.subdivide(2);
        fine.setEmission(emit);
        fine.setMaterial(mat);

        TriangleMesh[] meshes = {base, smooth, fine};
        String[] names = {"meshFaceBase", "meshFaceSmooth", "meshFaceFine"};

        for (int i = 0; i < 3; i++) {
            Scene scene = new Scene("Human head – level " + i)
                    .setBackground(new Color(8, 8, 12))
                    .setAmbientLight(new AmbientLight(new Color(12, 8, 6)));

            scene.geometries.add(meshes[i]);
            addFaceFeatures(scene, mat, emit);

            // Rembrandt key light: rakes the face at 45° from upper-left,
            // creating shadows in eye sockets, under nose and lips
            scene.lights.add(new SpotLight(new Color(550, 420, 310),
                    new Point(-4, 6, 9), new Vector(0.4, -0.6, -1))
                    .setKl(0.00008).setKq(0.00002).setNarrowBeam(2));
            // Soft fill from the right — lifts shadows on the unlit side
            scene.lights.add(new PointLight(new Color(90, 65, 50),
                    new Point(5, 1, 7)).setKl(0.002).setKq(0.0005));
            // Cool bounce from below — subtle under-chin light
            scene.lights.add(new DirectionalLight(new Color(18, 18, 28),
                    new Vector(0, 1, -1)));

            Camera.getBuilder()
                    .setRayTracer(scene, RayTracerType.SIMPLE)
                    .setLocation(new Point(0, 0.5, 9))
                    .setDirection(new Point(0, 0.3, 0), Vector.AXIS_Y)
                    .setVpSize(5, 7).setVpDistance(7)
                    .setResolution(900, 1260)
                    .build().renderImage().writeToImage(names[i]);
        }
    }

    /**
     * Adds coloured feature geometry — eyes, eyebrows, nostrils and lips —
     * anchored directly onto the head surface.  Every feature is a
     * {@link TriangleMesh}; no {@link Sphere} primitives are used except for
     * the nostril cavities, which need an inset sphere to simulate depth.
     * <p>
     * <ul>
     *   <li>Eyes — oriented-ellipsoid sclera mesh seated in the socket (t=0.47,
     *       cp=±0.38), with a flat iris disc and a smaller pupil disc placed at
     *       the forward face of the eyeball.</li>
     *   <li>Eyebrows — thin curved surface-patch ribbons that follow the brow
     *       ridge arc (t≈0.44, cp=±0.38), conforming to the skin.</li>
     *   <li>Nostrils — near-black sphere recessed inside the geometric dimple
     *       that {@link #headDisp} carves at (t≈0.61, cp=±0.11).</li>
     *   <li>Lips — two surface-patch bands (upper/lower) with rosy emission
     *       laid over the existing lip bump (t≈0.65–0.70).</li>
     * </ul>
     *
     * @param scene     scene to receive the feature geometry
     * @param skinMat   material shared with the head mesh
     * @param skinColor emission colour of the skin (unused after eyelid spheres removed)
     */
    private static void addFaceFeatures(Scene scene, Material skinMat, Color skinColor) {
        Material eyeMat = new Material().setKD(0.25).setKS(0.75).setShininess(150);
        Material browMat = new Material().setKD(0.80).setKS(0.10).setShininess(8);
        Material lipMat = new Material().setKD(0.70).setKS(0.30).setShininess(30);

        // ── Eyes: oriented sclera mesh + iris disc + pupil disc ───────────────
        double eyeTheta = Math.PI * 0.51, eyePhi = 0.38, eyeR = 0.19;
        for (int side = -1; side <= 1; side += 2) {
            double phi = Math.PI / 2 + side * eyePhi;
            Point socket = headSurfacePoint(eyeTheta, phi);
            Vector n = headSurfaceNormal(eyeTheta, phi);
            double dotNY = Vector.AXIS_Y.dotProduct(n);
            // If n ≈ ±AXIS_Y the Gram-Schmidt projection is a zero vector; fall back to AXIS_Z.
            Vector upTan = (Math.abs(Math.abs(dotNY) - 1.0) < 1e-9)
                    ? Vector.AXIS_Z
                    : Vector.AXIS_Y.subtract(n.scale(dotNY)).normalize();
            Vector right = upTan.crossProduct(n).normalize();

            // Sclera: eyeball mesh centred behind the socket; axisY = n so the
            // "north pole" of the ellipsoid (theta=0 side) faces the camera.
            Point sclCenter = socket.add(n.scale(-eyeR * 0.50));
            TriangleMesh sclera = buildOrientedEllipsoid(
                    sclCenter, right, n, upTan, eyeR, eyeR, eyeR, 16, 10);
            sclera.setEmission(new Color(225, 220, 210));
            sclera.setMaterial(eyeMat);
            scene.geometries.add(sclera);

            // Iris: flat disc in the skin-tangent plane, at the front face of sclera
            Point irisCenter = sclCenter.add(n.scale(eyeR * 0.92));
            TriangleMesh iris = buildOrientedDisc(irisCenter, right, upTan, eyeR * 0.44, 18);
            iris.setEmission(new Color(68, 48, 26));
            iris.setMaterial(eyeMat);
            scene.geometries.add(iris);

            // Pupil: slightly forward of the iris disc
            Point pupilCenter = sclCenter.add(n.scale(eyeR * 0.94));
            TriangleMesh pupil = buildOrientedDisc(pupilCenter, right, upTan, eyeR * 0.18, 14);
            pupil.setEmission(new Color(5, 5, 5));
            pupil.setMaterial(eyeMat);
            scene.geometries.add(pupil);
        }

        // ── Eyebrows: thin, arched surface-patch ribbons ──────────────────────
        Color browColor = new Color(35, 22, 13);
        double browCenterTheta = Math.PI * 0.475;
        double browPhiHalf = 0.18;
        double browPhiCenter = 0.38;
        double browThetaHalf = 0.018;
        double browArch = 0.035;
        for (int side = -1; side <= 1; side += 2) {
            double phiCenter = Math.PI / 2 + side * browPhiCenter;
            TriangleMesh brow = buildSurfacePatch(
                    browCenterTheta - browThetaHalf,
                    browCenterTheta + browThetaHalf,
                    phiCenter, browPhiHalf,
                    6, 14, 0.012, browArch);
            brow.setEmission(browColor);
            brow.setMaterial(browMat);
            scene.geometries.add(brow);
        }

        // ── Nostrils: near-black sphere seated in the geometric dimple ────────
        double nostrilTheta = Math.PI * 0.685, nostrilPhi = 0.09, nostrilR = 0.05;
        Color nostrilColor = new Color(10, 6, 4);
        for (int side = -1; side <= 1; side += 2) {
            double phi = Math.PI / 2 + side * nostrilPhi;
            Point p = headSurfacePoint(nostrilTheta, phi);
            Vector n = headSurfaceNormal(nostrilTheta, phi);
            scene.geometries.add(
                    new Sphere(p.add(n.scale(-nostrilR * 0.85)), nostrilR)
                            .setEmission(nostrilColor).setMaterial(skinMat));
        }

        // ── Lips: two surface-patch bands with rosy emission ─────────────────
        // Upper lip widened and given more lift than the lower lip for fuller
        // volume, with ranges sized close to the lower lip for symmetry.
        TriangleMesh upperLip = buildSurfacePatch(
                Math.PI * 0.678, Math.PI * 0.707,
                Math.PI / 2, 0.20, 4, 12, 0.024);
        upperLip.setEmission(new Color(150, 64, 70));
        upperLip.setMaterial(lipMat);
        scene.geometries.add(upperLip);

        TriangleMesh lowerLip = buildSurfacePatch(
                Math.PI * 0.710, Math.PI * 0.743,
                Math.PI / 2, 0.20, 4, 12, 0.020);
        lowerLip.setEmission(new Color(180, 80, 86));
        lowerLip.setMaterial(lipMat);
        scene.geometries.add(lowerLip);
    }

    /**
     * Builds a UV ellipsoid mesh centred at {@code center} with the given
     * orientation axes and semi-radii.  The "north pole" (theta=0) points in
     * the {@code axisY} direction, making it easy to align the forward face of
     * an eyeball toward the camera: pass the surface outward normal as axisY.
     */
    private static TriangleMesh buildOrientedEllipsoid(
            Point center, Vector axisX, Vector axisY, Vector axisZ,
            double rx, double ry, double rz, int slices, int stacks) {
        // Pre-extract axis components as raw doubles so the vertex loop never
        // calls Vector.scale(0), which throws when cosPhi or sinPhi is exactly 0.
        double xxX = axisX.dotProduct(Vector.AXIS_X), xxY = axisX.dotProduct(Vector.AXIS_Y), xxZ = axisX.dotProduct(Vector.AXIS_Z);
        double yxX = axisY.dotProduct(Vector.AXIS_X), yxY = axisY.dotProduct(Vector.AXIS_Y), yxZ = axisY.dotProduct(Vector.AXIS_Z);
        double zxX = axisZ.dotProduct(Vector.AXIS_X), zxY = axisZ.dotProduct(Vector.AXIS_Y), zxZ = axisZ.dotProduct(Vector.AXIS_Z);
        // Center coordinates via subtract from origin (safe; center is always on the face surface, never at world origin).
        Vector cv = center.subtract(Point.ZERO);
        double cX = cv.dotProduct(Vector.AXIS_X), cY = cv.dotProduct(Vector.AXIS_Y), cZ = cv.dotProduct(Vector.AXIS_Z);

        List<Point> verts = new ArrayList<>(2 + (stacks - 1) * slices);
        List<int[]> faces = new ArrayList<>(2 * slices * stacks);

        verts.add(new Point(cX + yxX * ry, cY + yxY * ry, cZ + yxZ * ry));  // north pole

        for (int s = 1; s < stacks; s++) {
            double theta = Math.PI * s / stacks;
            double sinT = Math.sin(theta), cosT = Math.cos(theta);
            for (int j = 0; j < slices; j++) {
                double phi = 2 * Math.PI * j / slices;
                double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);
                double wx = rx * sinT * cosPhi, wy = ry * cosT, wz = rz * sinT * sinPhi;
                verts.add(new Point(cX + xxX * wx + yxX * wy + zxX * wz,
                        cY + xxY * wx + yxY * wy + zxY * wz,
                        cZ + xxZ * wx + yxZ * wy + zxZ * wz));
            }
        }

        int botIdx = verts.size();
        verts.add(new Point(cX - yxX * ry, cY - yxY * ry, cZ - yxZ * ry));  // south pole

        for (int j = 0; j < slices; j++) {
            int a = 1 + j, b = 1 + (j + 1) % slices;
            faces.add(new int[]{0, a, b});
        }
        for (int s = 0; s < stacks - 2; s++) {
            for (int j = 0; j < slices; j++) {
                int tl = 1 + s * slices + j;
                int tr = 1 + s * slices + (j + 1) % slices;
                int bl = 1 + (s + 1) * slices + j;
                int br = 1 + (s + 1) * slices + (j + 1) % slices;
                faces.add(new int[]{tl, bl, br});
                faces.add(new int[]{tl, br, tr});
            }
        }
        int lastRing = 1 + (stacks - 2) * slices;
        for (int j = 0; j < slices; j++) {
            int a = lastRing + j, b = lastRing + (j + 1) % slices;
            faces.add(new int[]{botIdx, b, a});
        }

        return new TriangleMesh(verts, faces);
    }

    /**
     * Builds a flat circular disc mesh centred at {@code center}, lying in
     * the plane spanned by {@code axisX} and {@code axisZ}.  All face normals
     * point in the direction {@code axisX × axisZ} (i.e., toward the camera
     * when axisX = right and axisZ = upTan at a front-face surface point).
     * Used for the iris and pupil of the eye.
     */
    private static TriangleMesh buildOrientedDisc(
            Point center, Vector axisX, Vector axisZ, double radius, int segments) {
        List<Point> verts = new ArrayList<>(segments + 1);
        List<int[]> faces = new ArrayList<>(segments);

        // Pre-extract components for the same reason as buildOrientedEllipsoid —
        // cos/sin of angle is exactly 0 at k=0 and k=segments/4, causing scale(0) to throw.
        double axX = axisX.dotProduct(Vector.AXIS_X), axY = axisX.dotProduct(Vector.AXIS_Y), axZ = axisX.dotProduct(Vector.AXIS_Z);
        double azX = axisZ.dotProduct(Vector.AXIS_X), azY = axisZ.dotProduct(Vector.AXIS_Y), azZ = axisZ.dotProduct(Vector.AXIS_Z);
        Vector dcv = center.subtract(Point.ZERO);
        double dcX = dcv.dotProduct(Vector.AXIS_X), dcY = dcv.dotProduct(Vector.AXIS_Y), dcZ = dcv.dotProduct(Vector.AXIS_Z);

        verts.add(center);  // index 0 = centre
        for (int k = 0; k < segments; k++) {
            double angle = 2 * Math.PI * k / segments;
            double cosA = Math.cos(angle), sinA = Math.sin(angle);
            verts.add(new Point(dcX + axX * radius * cosA + azX * radius * sinA,
                    dcY + axY * radius * cosA + azY * radius * sinA,
                    dcZ + axZ * radius * cosA + azZ * radius * sinA));
        }
        for (int k = 0; k < segments; k++)
            faces.add(new int[]{0, 1 + k, 1 + (k + 1) % segments});

        return new TriangleMesh(verts, faces);
    }

    /**
     * Builds a curved quad-grid surface patch that follows the head skin via
     * {@link #headSurfacePoint} / {@link #headSurfaceNormal}.  Used for
     * eyebrow ribbons and lip bands — thin, conforming overlays that carry
     * their own emission colour without any sphere primitives.
     *
     * @param thetaMin  lower latitude bound (0 … π)
     * @param thetaMax  upper latitude bound
     * @param phiCenter longitude of the patch centre
     * @param phiHalf   half-width in longitude
     * @param rows      number of latitude subdivisions
     * @param cols      number of longitude subdivisions
     * @param liftOff   outward displacement along the local surface normal
     */
    private static TriangleMesh buildSurfacePatch(
            double thetaMin, double thetaMax,
            double phiCenter, double phiHalf,
            int rows, int cols, double liftOff) {
        return buildSurfacePatch(thetaMin, thetaMax, phiCenter, phiHalf, rows, cols, liftOff, 0);
    }

    /**
     * Same as {@link #buildSurfacePatch(double, double, double, double, int, int, double)}
     * but with an additional upward arch toward the centre of the patch —
     * used to give eyebrow ribbons their natural curved shape.
     *
     * @param arch peak latitude offset (subtracted from theta) at the patch's
     *             horizontal centre, fading to 0 at its left/right edges
     */
    private static TriangleMesh buildSurfacePatch(
            double thetaMin, double thetaMax,
            double phiCenter, double phiHalf,
            int rows, int cols, double liftOff, double arch) {
        int W = cols + 1;
        List<Point> verts = new ArrayList<>((rows + 1) * W);
        List<int[]> faces = new ArrayList<>(2 * rows * cols);

        for (int r = 0; r <= rows; r++) {
            double thetaBase = thetaMin + (double) r / rows * (thetaMax - thetaMin);
            for (int c = 0; c <= cols; c++) {
                double archShift = arch * Math.sin(Math.PI * c / cols);
                double theta = thetaBase - archShift;
                double phi = phiCenter - phiHalf + (double) c / cols * 2 * phiHalf;
                Point p = headSurfacePoint(theta, phi);
                Vector n = headSurfaceNormal(theta, phi);
                verts.add(p.add(n.scale(liftOff)));
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int tl = r * W + c, tr = r * W + c + 1;
                int bl = (r + 1) * W + c, br = (r + 1) * W + c + 1;
                faces.add(new int[]{tl, bl, tr});
                faces.add(new int[]{bl, br, tr});
            }
        }

        return new TriangleMesh(verts, faces);
    }

    /**
     * Builds a complete 3-D head mesh from a displaced ellipsoid.
     * <p>
     * Each surface vertex of a 20-slice × 16-stack ellipsoid (rx=1.8, ry=2.5,
     * rz=2.0) is pushed outward or inward along its normal by
     * {@link #headDisp(double, double)}.  Features are placed in the front
     * hemisphere and fade smoothly to zero on the sides and back.
     * </p>
     *
     * @param slices longitude divisions
     * @param stacks latitude  divisions
     */
    private static TriangleMesh buildHead(int slices, int stacks) {
        double rx = 1.72, ry = 2.40, rz = 1.85;

        List<Point> verts = new ArrayList<>(2 + (stacks - 1) * slices);
        List<int[]> faces = new ArrayList<>(2 * slices * stacks);

        // North pole
        verts.add(new Point(0, ry, 0));

        // Latitude rings — displace each vertex along its outward normal
        for (int s = 1; s < stacks; s++) {
            double theta = Math.PI * s / stacks;
            double sinT = Math.sin(theta), cosT = Math.cos(theta);
            // Skull-shaped radii: width and depth vary by latitude
            double effRx = rx * skullRx(theta);
            double effRz = rz * skullRz(theta);
            for (int j = 0; j < slices; j++) {
                double phi = 2 * Math.PI * j / slices;
                double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);
                // Skull base position (non-uniform ellipsoid)
                double bx = effRx * sinT * cosPhi;
                double by = ry * cosT;
                double bz = effRz * sinT * sinPhi;
                // Outward direction: approximate normal = normalise(position)
                double len = Math.sqrt(bx * bx + by * by + bz * bz);
                double nx = bx / len, ny = by / len, nz = bz / len;
                double d = headDisp(theta, phi);
                verts.add(new Point(bx + d * nx, by + d * ny, bz + d * nz));
            }
        }

        // South pole
        int botIdx = verts.size();
        verts.add(new Point(0, -ry, 0));

        // North cap
        for (int j = 0; j < slices; j++) {
            int a = 1 + j, b = 1 + (j + 1) % slices;
            faces.add(new int[]{0, a, b});
        }
        // Middle band
        for (int s = 0; s < stacks - 2; s++) {
            for (int j = 0; j < slices; j++) {
                int tl = 1 + s * slices + j;
                int tr = 1 + s * slices + (j + 1) % slices;
                int bl = 1 + (s + 1) * slices + j;
                int br = 1 + (s + 1) * slices + (j + 1) % slices;
                faces.add(new int[]{tl, bl, br});
                faces.add(new int[]{tl, br, tr});
            }
        }
        // South cap
        int lastRing = 1 + (stacks - 2) * slices;
        for (int j = 0; j < slices; j++) {
            int a = lastRing + j, b = lastRing + (j + 1) % slices;
            faces.add(new int[]{botIdx, b, a});
        }

        return new TriangleMesh(verts, faces);
    }

    /**
     * World-space point on the displaced head surface at the given spherical
     * coordinates — the exact same position {@link #buildHead} would generate
     * for a vertex there. Lets feature markers (eyes, eyebrows, nostrils) be
     * anchored precisely on the skin, using the very same {@code (theta, phi)}
     * centres that {@link #headDisp} uses for its Gaussian bumps.
     *
     * @param theta latitude  (0 … π)
     * @param phi   longitude (0 … 2π)
     */
    private static Point headSurfacePoint(double theta, double phi) {
        double rx = 1.72, ry = 2.40, rz = 1.85;
        double sinT = Math.sin(theta), cosT = Math.cos(theta);
        double effRx = rx * skullRx(theta);
        double effRz = rz * skullRz(theta);
        double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);
        double bx = effRx * sinT * cosPhi;
        double by = ry * cosT;
        double bz = effRz * sinT * sinPhi;
        Vector outward = new Vector(bx, by, bz).normalize();
        double d = headDisp(theta, phi);
        return new Point(bx, by, bz).add(outward.scale(d));
    }

    /**
     * Outward unit normal of the base skull ellipsoid at the given spherical
     * coordinates — the same direction {@link #buildHead} displaces along.
     * Used to push feature markers slightly in/out of the skin so they read
     * as sitting on (eyebrows, nostrils) or recessed into (eyes) the face.
     *
     * @param theta latitude  (0 … π)
     * @param phi   longitude (0 … 2π)
     */
    private static Vector headSurfaceNormal(double theta, double phi) {
        double rx = 1.72, ry = 2.40, rz = 1.85;
        double sinT = Math.sin(theta), cosT = Math.cos(theta);
        double effRx = rx * skullRx(theta);
        double effRz = rz * skullRz(theta);
        double cosPhi = Math.cos(phi), sinPhi = Math.sin(phi);
        return new Vector(effRx * sinT * cosPhi, ry * cosT, effRz * sinT * sinPhi).normalize();
    }

    /**
     * Returns the signed radial displacement (in world units) to apply to the
     * ellipsoid surface at the given spherical coordinates.
     * <p>
     * Positive values push the surface outward (nose, brow, cheeks, lips);
     * negative values push it inward (eye sockets).  Everything is multiplied
     * by a {@code cos²(fp)} fade so features vanish on the sides and back.
     * </p>
     * <p>
     * Coordinate conventions:
     * <pre>
     *   theta = 0           → top of head (north pole)
     *   theta = π           → bottom of head (south pole)
     *   phi   = π/2         → front of head (+z toward camera)
     *   fp    = phi − π/2   → signed angle from front (0 = directly facing)
     * </pre>
     * Proportions calibrated against a reference female head scan (latitude
     * fractions t = theta/π measured from the crown):
     * <pre>
     *   UPPER  THIRD  0.36 forehead dome   0.46 brow ridge (near-flat)
     *   EYES          0.51 eye sockets (cp=±0.38)
     *   MIDDLE THIRD  0.575 cheekbones (cp=±0.56, widest point)
     *                 0.58–0.665 nasal root→bridge→tip   0.66 buccal hollow
     *                 0.685 nostrils
     *   LOWER  THIRD  0.70 lips   0.82 chin
     * </pre>
     *
     * @param theta latitude  (0 … π)
     * @param phi   longitude (0 … 2π)
     * @return displacement magnitude
     */
    private static double headDisp(double theta, double phi) {
        // fp = signed angle from the front face (+z direction)
        double fp = phi - Math.PI / 2;
        if (fp > Math.PI) fp -= 2 * Math.PI;
        if (fp < -Math.PI) fp += 2 * Math.PI;

        double front = Math.cos(fp);
        if (front <= 0.0) return 0.0;
        double fade = front * front;   // smooth falloff toward sides

        double d = 0;

        // st >= 0.10 (= 1.5x theta_res at 48 stacks), sp >= 0.14 (= 1.4x phi_res at 64 slices)
        // Features narrower than one grid step create knife-cut facet artifacts.

        // All features shifted -0.03 in t so that eyes land at the true
        // midpoint of the visible head (crown→chin), not just the ellipse equator.

        // ── Forehead shelf — taller, smooth dome ──────────────────────────────
        d += 0.13 * g2(theta, fp, Math.PI * 0.36, 0.00, 0.11, 0.50);

        // ── Brow ridges — softened almost flat, no bone bumps over the eyes ───
        d += 0.06 * (g2(theta, fp, Math.PI * 0.46, -0.38, 0.10, 0.18)
                + g2(theta, fp, Math.PI * 0.46, 0.38, 0.10, 0.18));

        // ── Eye sockets — widened to seat larger eyes; t=0.51 matches reference ─
        d -= 0.42 * (g2(theta, fp, Math.PI * 0.51, -0.38, 0.12, 0.25)
                + g2(theta, fp, Math.PI * 0.51, 0.38, 0.12, 0.25));

        // ── Nasal bridge — thinner and lower-profile ──────────────────────────
        d += 0.30 * g2(theta, fp, Math.PI * 0.58, 0.00, 0.10, 0.08);
        // Nose tip (most protruding point) — peak at t≈0.665, matches reference apex
        d += 0.60 * g2(theta, fp, Math.PI * 0.665, 0.00, 0.10, 0.14);
        // Nostril dimples — shallow inward recesses flanking the nose base;
        // addFaceFeatures() seats a dark sphere at the floor of each one so
        // the opening reads as an actual hollow rather than a surface dot.
        d -= 0.11 * (g2(theta, fp, Math.PI * 0.685, -0.09, 0.10, 0.12)
                + g2(theta, fp, Math.PI * 0.685, 0.09, 0.10, 0.12));

        // ── Cheekbones (zygomatic arch) — peak at t≈0.575, matches reference ──
        d += 0.38 * (g2(theta, fp, Math.PI * 0.575, -0.56, 0.11, 0.15)
                + g2(theta, fp, Math.PI * 0.575, 0.56, 0.11, 0.15));
        // Buccal hollow (below cheekbones)
        d -= 0.18 * (g2(theta, fp, Math.PI * 0.66, -0.50, 0.10, 0.13)
                + g2(theta, fp, Math.PI * 0.66, 0.50, 0.10, 0.13));

        // ── Lips ──────────────────────────────────────────────────────────────
        d += 0.52 * g2(theta, fp, Math.PI * 0.70, 0.00, 0.095, 0.22);

        // ── Chin (mental eminence) — smaller and more rounded ─────────────────
        d += 0.18 * g2(theta, fp, Math.PI * 0.82, 0.00, 0.09, 0.16);

        return d * fade;
    }

    /**
     * Evaluates a 2-D Gaussian centred at {@code (ct, cp)} with standard
     * deviations {@code (st, sp)}.
     */
    private static double g2(double t, double p,
                             double ct, double cp,
                             double st, double sp) {
        double dt = (t - ct) / st, dp = (p - cp) / sp;
        return Math.exp(-(dt * dt + dp * dp));
    }

    /**
     * Lateral (X-axis) radius scale factor as a function of skull latitude.
     * <p>
     * Models the non-ellipsoidal silhouette of the human skull when viewed
     * from the front:
     * <ul>
     *   <li>Parietal  (t≈0.22): very subtle cranial width</li>
     *   <li>Temporal  (t≈0.35): gentle flattening at temples</li>
     *   <li>Zygomatic (t≈0.575): cheekbone zone — widest face point</li>
     *   <li>Mandible  (t≈0.70): jaw taper</li>
     *   <li>Chin      (t≈0.87): oval taper</li>
     * </ul>
     *
     * @param theta latitude in [0, π]
     * @return multiplicative scale factor for the X radius
     */
    private static double skullRx(double theta) {
        double t = theta / Math.PI;
        // Loomis + low-poly planes: parietal wide, temples clearly narrower,
        // zygomatic arch = widest facial point, jaw tapers to chin.
        // sin(theta) narrows toward the crown — compensate so the cranium looks
        // like a smooth oval, not a mushroom dome sitting on a wider face.
        // At t=0.25: sin=0.707 → +33% boost → effective width = 94% of cheeks
        // At t=0.35: sin=0.891 → +11% boost → effective width = 100% of cheeks
        // At t=0.50: sin=1.000 → 0% boost  → effective width = 100% (baseline)
        return 1.0
                + 0.33 * g1(t, 0.25, 0.10)   // parietal: compensates ellipse narrowing
                - 0.16 * g1(t, 0.70, 0.12)   // jaw: narrower taper
                - 0.27 * g1(t, 0.87, 0.08);  // chin: rounder, narrower taper
    }

    /**
     * Front-to-back (Z-axis) radius scale factor as a function of skull latitude.
     * Occipital region is deeper (back-of-skull volume); jaw/chin is shallower.
     *
     * @param theta latitude in [0, π]
     * @return multiplicative scale factor for the Z radius
     */
    private static double skullRz(double theta) {
        double t = theta / Math.PI;
        return 1.0
                + 0.08 * g1(t, 0.22, 0.12)   // occipital: back-of-skull depth
                - 0.08 * g1(t, 0.82, 0.10);  // jaw/chin: shallower front-to-back
    }

    /**
     * 1-D Gaussian centred at {@code mu} with standard deviation {@code sigma}.
     */
    private static double g1(double x, double mu, double sigma) {
        double d = (x - mu) / sigma;
        return Math.exp(-d * d);
    }

    /**
     * Builds a sinusoidal height-field terrain mesh.
     *
     * @param rows  number of grid rows
     * @param cols  number of grid columns
     * @param width total width (X extent) centred at origin
     * @param depth total depth (Z extent) centred at origin
     * @param amp   wave amplitude (Y)
     * @return the assembled {@link TriangleMesh}
     */
    private static TriangleMesh buildTerrain(int rows, int cols,
                                             double width, double depth, double amp) {
        List<Point> verts = new ArrayList<>((rows + 1) * (cols + 1));
        List<int[]> faces = new ArrayList<>(2 * rows * cols);

        for (int r = 0; r <= rows; r++) {
            for (int c = 0; c <= cols; c++) {
                double x = -width / 2 + width * c / cols;
                double z = -depth / 2 + depth * r / rows;
                double y = amp * Math.sin(x * 0.8) * Math.cos(z * 0.8);
                verts.add(new Point(x, y, z));
            }
        }

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int tl = r * (cols + 1) + c;
                int tr = tl + 1;
                int bl = tl + (cols + 1);
                int br = bl + 1;
                faces.add(new int[]{tl, bl, br});
                faces.add(new int[]{tl, br, tr});
            }
        }

        TriangleMesh mesh = new TriangleMesh(verts, faces);
        mesh.setEmission(new Color(15, 40, 20));
        mesh.setMaterial(new Material().setKD(0.6).setKS(0.4).setShininess(120));
        return mesh;
    }
}

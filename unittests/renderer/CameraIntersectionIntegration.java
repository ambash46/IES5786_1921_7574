package renderer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import geometries.api.Intersectable;
import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Integration tests that verify the interaction between {@link Camera} ray
 * construction and geometric intersection calculations.
 *
 * <p>Each test method fires a ray through every pixel of a shared 3×3 camera,
 * accumulates the total intersection count with a given geometry, and asserts
 * the result against the expected value derived from the course presentation.</p>
 *
 * <p>The three required test methods are:
 * <ul>
 *   <li>{@link #testCameraRaySphereIntegration()} — five standard sphere cases</li>
 *   <li>{@link #testCameraRayPlaneIntegration()} — three standard plane cases</li>
 *   <li>{@link #testCameraRayTriangleIntegration()} — two standard triangle cases</li>
 * </ul>
 * Additional tests for {@link Polygon}, {@link Tube}, and {@link Cylinder} are
 * included as bonus coverage.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
class CameraIntersectionIntegration {

    /** Number of pixel columns in the shared test camera. */
    private static final int NX = 3;

    /** Number of pixel rows in the shared test camera. */
    private static final int NY = 3;

    /** Physical width and height of the shared test camera's view plane. */
    private static final double VP_SIZE = 3;

    /** Distance from the shared test camera to its view plane. */
    private static final double VP_DISTANCE = 1;

    /**
     * Camera shared by all integration tests.
     * <p>Positioned at the origin, aimed along the negative Z axis, with a
     * {@value NX}×{@value NY} pixel grid and a {@value VP_SIZE}×{@value VP_SIZE}
     * view plane at distance {@value VP_DISTANCE}.</p>
     */
    private static final Camera CAMERA = Camera.getBuilder()
            .setLocation(Point.ZERO)
            .setDirection(new Vector(0, 0, -1), new Vector(0, 1, 0))
            .setVpDistance(VP_DISTANCE)
            .setVpSize(VP_SIZE, VP_SIZE)
            .setResolution(NX, NY)
            .build();

    /** Default constructor — required by documentation tools. */
    CameraIntersectionIntegration() { }

    /**
     * Fires a ray through every pixel of {@code camera}, accumulates all
     * intersections with {@code geometry}, and asserts the total equals
     * {@code expected}.
     *
     * <p>Only the intersection <em>count</em> is verified; the actual
     * intersection points are not examined.</p>
     *
     * @param camera   the camera whose rays are constructed
     * @param geometry the geometry to intersect against each ray
     * @param expected the expected total number of intersections across all pixels
     * @param testName descriptive name used in the assertion failure message
     */
    private void assertIntersectionsCount(Camera camera, Intersectable geometry,
                                          int expected, String testName) {
        int count = 0;
        for (int i = 0; i < camera.getNY(); i++)
            for (int j = 0; j < camera.getNX(); j++) {
                List<Point> intersections = geometry.findIntersections(camera.constructRay(j, i));
                if (intersections != null)
                    count += intersections.size();
            }
        assertEquals(expected, count, testName);
    }

    /**
     * Integration tests for {@link Camera} ray construction with {@link Sphere}.
     *
     * <p>Covers all five standard cases from the course presentation:
     * <ol>
     *   <li>Small sphere in front — only the center ray hits (2 intersections)</li>
     *   <li>Large sphere — all 9 rays enter and exit (18 intersections)</li>
     *   <li>Medium sphere — 5 rays hit (10 intersections), corner rays miss</li>
     *   <li>Camera inside sphere — each ray exits through the far side (9 intersections)</li>
     *   <li>Sphere behind camera — no ray reaches it (0 intersections)</li>
     * </ol>
     * </p>
     */
    @Test
    void testCameraRaySphereIntegration() {
        // TC01: Small sphere — only center ray hits (2 intersections)
        assertIntersectionsCount(CAMERA, new Sphere(new Point(0, 0, -3), 1), 2,
                "TC01: Small sphere");

        // TC02: Large sphere — all 9 rays hit twice (18 intersections)
        assertIntersectionsCount(CAMERA, new Sphere(new Point(0, 0, -3), 2.5), 18,
                "TC02: Large sphere");

        // TC03: Medium sphere — center + 4 edge rays hit twice, corners miss (10 intersections)
        assertIntersectionsCount(CAMERA, new Sphere(new Point(0, 0, -2.5), 2), 10,
                "TC03: Medium sphere");

        // TC04: Camera inside sphere — each ray exits once (9 intersections)
        assertIntersectionsCount(CAMERA, new Sphere(new Point(0, 0, -0.5), 1), 9,
                "TC04: Camera inside sphere");

        // TC05: Sphere behind camera — 0 intersections
        assertIntersectionsCount(CAMERA, new Sphere(new Point(0, 0, 1), 0.5), 0,
                "TC05: Sphere behind camera");
    }

    /**
     * Integration tests for {@link Camera} ray construction with {@link Plane}.
     *
     * <p>Covers all three standard cases from the course presentation:
     * <ol>
     *   <li>Plane parallel to the view plane — all 9 rays hit (9 intersections)</li>
     *   <li>Plane slightly tilted toward the camera — all 9 rays still hit (9 intersections)</li>
     *   <li>Plane steeply tilted — the top row of rays passes above the plane (6 intersections)</li>
     * </ol>
     * </p>
     */
    @Test
    void testCameraRayPlaneIntegration() {
        // TC01: Plane parallel to view plane — all 9 rays hit
        assertIntersectionsCount(CAMERA,
                new Plane(new Point(0, 0, -5), new Vector(0, 0, 1)), 9,
                "TC01: Plane parallel to view plane");

        // TC02: Plane slightly tilted forward — all 9 rays still hit
        assertIntersectionsCount(CAMERA,
                new Plane(new Point(0, 0, -5), new Vector(0, 1, 2)), 9,
                "TC02: Plane slightly tilted");

        // TC03: Plane steeply tilted — top row misses (6 intersections)
        assertIntersectionsCount(CAMERA,
                new Plane(new Point(0, 0, -2), new Vector(0, 1, 1)), 6,
                "TC03: Plane steeply tilted");
    }

    /**
     * Integration tests for {@link Camera} ray construction with {@link Triangle}.
     *
     * <p>Covers both standard cases from the course presentation:
     * <ol>
     *   <li>Small triangle — fits within a single pixel; only the center ray hits (1 intersection)</li>
     *   <li>Tall narrow triangle — top vertex is far up; center and top-center rays hit (2 intersections)</li>
     * </ol>
     * </p>
     */
    @Test
    void testCameraRayTriangleIntegration() {
        // TC01: Small triangle — only center ray hits (1 intersection)
        assertIntersectionsCount(CAMERA,
                new Triangle(
                        new Point(0, 1, -2),
                        new Point(1, -1, -2),
                        new Point(-1, -1, -2)),
                1, "TC01: Small triangle");

        // TC02: Tall triangle — center + top-center rays hit (2 intersections)
        assertIntersectionsCount(CAMERA,
                new Triangle(
                        new Point(0, 20, -2),
                        new Point(1, -1, -2),
                        new Point(-1, -1, -2)),
                2, "TC02: Tall triangle");
    }

    /**
     * Integration tests for {@link Camera} ray construction with {@link Polygon}.
     *
     * <p>Rays from the origin through a 3×3 grid scaled by VP_DISTANCE reach the
     * z=-2 plane at hit points {@code (2*(j-1), 2*(1-i), -2)} for pixel (j, i).
     * The polygon bounds determine which pixels register a hit.</p>
     */
    @Test
    void testCameraRayPolygonIntegration() {
        // TC01: Large square — all 9 rays hit
        assertIntersectionsCount(CAMERA,
                new Polygon(
                        new Point(-3, 3, -2), new Point(3, 3, -2),
                        new Point(3, -3, -2), new Point(-3, -3, -2)),
                9, "TC01: Large polygon");

        // TC02: Small square — only center ray hits (1 intersection)
        assertIntersectionsCount(CAMERA,
                new Polygon(
                        new Point(-1.5, 1.5, -2), new Point(1.5, 1.5, -2),
                        new Point(1.5, -1.5, -2), new Point(-1.5, -1.5, -2)),
                1, "TC02: Small polygon");

        // TC03: Wide, short rectangle — center row of 3 rays hits (3 intersections)
        assertIntersectionsCount(CAMERA,
                new Polygon(
                        new Point(-3, 1.5, -2), new Point(3, 1.5, -2),
                        new Point(3, -1.5, -2), new Point(-3, -1.5, -2)),
                3, "TC03: Wide short polygon");
    }

    /**
     * Integration tests for {@link Camera} ray construction with an infinite {@link Tube}.
     *
     * <p>The tube axis runs along the Y axis at (x=0, z=-3). Each ray's distance
     * to this axis in the XZ plane determines whether it intersects the tube surface.</p>
     */
    @Test
    void testCameraRayTubeIntegration() {
        // TC01: Narrow tube (r=1) — center column rays hit (6 intersections)
        assertIntersectionsCount(CAMERA,
                new Tube(1, new Ray(new Point(0, 0, -3), new Vector(0, 1, 0))),
                6, "TC01: Narrow tube");

        // TC02: Wide tube (r=2.5) — all 9 rays hit twice (18 intersections)
        assertIntersectionsCount(CAMERA,
                new Tube(2.5, new Ray(new Point(0, 0, -3), new Vector(0, 1, 0))),
                18, "TC02: Wide tube");

        // TC03: Tube behind camera — 0 intersections
        assertIntersectionsCount(CAMERA,
                new Tube(0.5, new Ray(new Point(0, 0, 1), new Vector(0, 1, 0))),
                0, "TC03: Tube behind camera");

        // TC04: Camera inside tube — each ray exits once (9 intersections)
        assertIntersectionsCount(CAMERA,
                new Tube(2, new Ray(new Point(0, 0, 0), new Vector(0, 1, 0))),
                9, "TC04: Camera inside tube");
    }

    /**
     * Integration tests for {@link Camera} ray construction with a finite {@link Cylinder}.
     *
     * <p>The cylinder axis is the same as in the tube tests; the height is large
     * enough that the end caps do not interfere with the lateral surface intersections.</p>
     */
    @Test
    void testCameraRayCylinderIntegration() {
        // TC01: Narrow cylinder, tall — center column rays hit (6 intersections)
        assertIntersectionsCount(CAMERA,
                new Cylinder(1, new Ray(new Point(0, -5, -3), new Vector(0, 1, 0)), 10),
                6, "TC01: Narrow cylinder");

        // TC02: Wide cylinder, tall — all 9 rays hit twice (18 intersections)
        assertIntersectionsCount(CAMERA,
                new Cylinder(2.5, new Ray(new Point(0, -5, -3), new Vector(0, 1, 0)), 10),
                18, "TC02: Wide cylinder");

        // TC03: Cylinder behind camera — 0 intersections
        assertIntersectionsCount(CAMERA,
                new Cylinder(0.5, new Ray(new Point(0, 0, 1), new Vector(0, 1, 0)), 2),
                0, "TC03: Cylinder behind camera");

        // TC04: Camera inside cylinder — each ray exits once (9 intersections)
        assertIntersectionsCount(CAMERA,
                new Cylinder(2, new Ray(new Point(0, -5, 0), new Vector(0, 1, 0)), 10),
                9, "TC04: Camera inside cylinder");
    }
}

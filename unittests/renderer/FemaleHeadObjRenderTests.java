package renderer;

import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;
import scene.XmlSceneLoader;

/**
 * Renders the reference female-head OBJ model
 * ({@code scenes/models/11091_FemaleHead_v4.obj}) loaded via the
 * {@code <mesh>} element of {@link XmlSceneLoader} (see
 * {@code scenes/femaleHeadObj.xml}).
 *
 * <p>{@link SimpleRayTracer} has no acceleration structure, and the scene
 * uses the full ~99.9K-triangle mesh ({@code decimate="1"}) so that the
 * surface has no holes; the resolution is kept very low to compensate.
 *
 * @author Ambash and Elyasaf
 */
class FemaleHeadObjRenderTests {

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    FemaleHeadObjRenderTests() { /* no-op */ }

    /**
     * Loads {@code scenes/femaleHeadObj.xml} and renders the female-head mesh.
     */
    @Test
    void testFemaleHeadObj() {
        Camera.getBuilder()
                .setRayTracer(new XmlSceneLoader().load("femaleHeadObj"), RayTracerType.SIMPLE)
                .setLocation(new Point(100, 70, 100))
                .setDirection(Point.ZERO, Vector.AXIS_Y)
                .setVpSize(320, 320).setVpDistance(160)
                .setResolution(2000, 2000)
                .build()
                .renderImage()
                .writeToImage("xml_femaleHeadObj");
    }
}

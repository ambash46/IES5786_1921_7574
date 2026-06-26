package renderer;

import lighting.PointLight;
import org.junit.jupiter.api.Test;
import primitives.Point;
import primitives.Vector;
import scene.Scene;
import scene.XmlSceneLoader;

/**
 * High-quality render (1000×1000, JITTERED, 289 samples) of the Crystal
 * Gallery scene. The three PointLight sources each receive a different radius
 * so a single image shows hard, medium, and wide penumbras simultaneously.
 *
 * <p>Saved to the root {@code images/} folder.
 *
 * @author Ambash and Elyasaf
 */
@SuppressWarnings("java:S109")
class HighQualityTests {

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    HighQualityTests() { /* no-op */ }

    @Test
    void softShadows_highQuality() {
        Scene scene = new XmlSceneLoader().load("stage8CrystalGallery");

        // Assign a different radius to each PointLight in the scene
        double[] radii = {0, 50, 100};
        int i = 0;
        for (var light : scene.lights)
            if (light instanceof PointLight pl)
                pl.setRadius(radii[Math.min(i++, radii.length - 1)]);

        Camera.getBuilder()
                .setRayTracer(scene, RayTracerType.SIMPLE)
                .setSoftShadows(289, SamplingPatterns.JITTERED)
                .setAntiAliasing(289, SamplingPatterns.JITTERED)
                .setLocation(new Point(750, 150, 300))
                .setDirection(new Point(0, -100, -280), Vector.AXIS_Y)
                .rotate(20)
                .setVpSize(620, 620)
                .setVpDistance(1000)
                .setResolution(1000, 1000)
                .setMultithreading(4)
                .build().renderImage().writeToImage("softShadows_highQuality_jitter_s289");
    }
}

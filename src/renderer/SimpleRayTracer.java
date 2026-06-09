package renderer;

import static geometries.api.Intersectable.Intersection;
import static primitives.Util.alignZero;

import lighting.LightSource;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

/**
 * A ray tracer implementing the Phong reflection model with multiple
 * external light sources.
 *
 * @author Ambash and Elyasaf
 */
class SimpleRayTracer extends RayTracerBase {

    private static final double DELTA = 0.1;

    /**
     * Constructs a simple ray tracer for the given scene.
     *
     * @param scene the scene to trace rays against
     */
    SimpleRayTracer(Scene scene) {
        super(scene);
    }

    /**
     * Computes the color at the given intersection using ambient light and
     * local Phong effects.
     *
     * @param intersection the closest intersection along the ray
     * @param v            the normalized ray direction
     * @return the color at the intersection
     */
    private Color calcColor(Intersection intersection, Vector v) {
        return !preprocessIntersection(intersection, v) ? Color.BLACK
                : _scene.ambientLight.getIntensity().scale(intersection.material.kA)
                        .add(calcLocalEffects(intersection));
    }

    @Override
    Color traceRay(Ray ray) {
        var intersections = _scene.geometries.calcIntersections(ray);
        Intersection closest = ray.findClosestIntersection(intersections);
        return closest == null ? _scene.background : calcColor(closest, ray.direction());
    }

    /**
     * Returns {@code true} if the intersection point has line-of-sight to its
     * active light source (i.e. is not in shadow).
     * <p>
     * A shadow ray is fired from the surface point — offset by {@link #DELTA}
     * along the normal to avoid self-intersection — in the direction opposite to
     * {@code l} (i.e. toward the light). If the ray hits any geometry the point
     * is considered shadowed.
     * </p>
     *
     * @param intersection the preprocessed intersection (uses {@code point},
     *                     {@code n}, {@code l}, {@code ln})
     * @return {@code true} if unshaded, {@code false} if occluded
     */
    private boolean unshaded(Intersection intersection) {
        Point offsetPoint = intersection.point.add(
                intersection.n.scale(intersection.ln < 0 ? DELTA : -DELTA));
        Ray shadowRay = new Ray(offsetPoint, intersection.l.scale(-1));
        double lightDistance = intersection.light.getDistance(intersection.point);
        return _scene.geometries.calcIntersections(shadowRay, lightDistance) == null;
    }

    /**
     * Computes the sum of all external light source contributions at the
     * intersection point (emission + diffuse + specular for each light).
     *
     * @param intersection the preprocessed intersection
     * @return the total local-effects color at the point
     */
    private Color calcLocalEffects(Intersection intersection) {
        Color color = intersection.geometry.getEmission();
        for (LightSource lightSource : _scene.lights)
            if (preprocessLightSource(intersection, lightSource) && unshaded(intersection))
                color = color.add(lightSource.getIntensity(intersection.point)
                        .scale(calcDiffuse(intersection).add(calcSpecular(intersection))));
        return color;
    }

    /**
     * Computes the diffuse reflection coefficient for the current light source.
     * <p>
     * Formula: {@code kD * |l · n|}
     *
     * @param intersection the preprocessed intersection (uses {@code ln}, {@code material.kD})
     * @return the diffuse factor as a {@link Double3}
     */
    private Double3 calcDiffuse(Intersection intersection) {
        return intersection.material.kD.scale(Math.abs(intersection.ln));
    }

    /**
     * Computes the specular reflection coefficient for the current light source.
     * <p>
     * Formula: {@code kS * max(0, -v · r)^nShininess}
     * where {@code r = l - 2*(l·n)*n} is the reflection of {@code l} around {@code n}.
     *
     * @param intersection the preprocessed intersection (uses {@code l}, {@code n},
     *                     {@code ln}, {@code v}, {@code material})
     * @return the specular factor as a {@link Double3}
     */
    private Double3 calcSpecular(Intersection intersection) {
        Vector r        = intersection.l.subtract(intersection.n.scale(2 * intersection.ln));
        double minusVR  = alignZero(-intersection.v.dotProduct(r));
        return minusVR <= 0 ? Double3.ZERO
                : intersection.material.kS.scale(Math.pow(minusVR, intersection.material.nShininess));
    }
}

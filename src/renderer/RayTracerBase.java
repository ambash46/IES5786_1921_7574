package renderer;

import static geometries.api.Intersectable.Intersection;
import static primitives.Util.alignZero;

import lighting.LightSource;
import primitives.Color;
import primitives.Ray;
import primitives.Vector;
import scene.Scene;

/**
 * Abstract base class for ray tracer implementations.
 * <p>
 * A ray tracer receives a ray and returns the color seen along that ray,
 * taking the scene's geometry, lighting, and background into account.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
abstract class RayTracerBase {

    /** The scene this ray tracer operates on. */
    protected final Scene _scene;

    /**
     * Constructs a ray tracer for the given scene.
     *
     * @param scene the scene to trace rays against
     */
    RayTracerBase(Scene scene) {
        _scene = scene;
    }

    /**
     * Computes the color seen along the given ray.
     *
     * @param ray the ray to trace
     * @return the color contributed by this ray
     */
    abstract Color traceRay(Ray ray);

    /**
     * Initializes the intersection-related cache fields of the given
     * {@link Intersection}: the incoming ray direction {@code v}, the surface
     * normal {@code n}, and their dot product {@code vn}.
     *
     * @param intersection the intersection to preprocess
     * @param v            the normalized direction of the incoming ray
     * @return {@code true} if {@code vn ≠ 0} (ray is not tangent to the surface),
     *         {@code false} otherwise — caller should skip local lighting
     */
    protected boolean preprocessIntersection(Intersection intersection, Vector v) {
        intersection.v  = v;
        intersection.n  = intersection.geometry.getNormal(intersection.point);
        intersection.vn = alignZero(intersection.v.dotProduct(intersection.n));
        return intersection.vn != 0;
    }

    /**
     * Initializes the light-source-related cache fields of the given
     * {@link Intersection}: the active light source, the direction {@code l}
     * from the source to the point, and the dot product {@code ln}.
     *
     * @param intersection the intersection to preprocess
     * @param light        the light source being evaluated
     * @return {@code true} if the light and the viewer are on the same side of
     *         the surface ({@code ln * vn > 0}), {@code false} if the light
     *         contribution should be discarded
     */
    protected boolean preprocessLightSource(Intersection intersection, LightSource light) {
        intersection.light = light;
        intersection.l     = light.getL(intersection.point);
        intersection.ln    = alignZero(intersection.l.dotProduct(intersection.n));
        return intersection.ln * intersection.vn > 0;
    }
}

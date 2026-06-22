package renderer;

import static geometries.api.Intersectable.Intersection;
import static primitives.Util.alignZero;

import java.util.List;
import lighting.PointLight;
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

    /** Maximum recursion depth for reflection/transparency color calculation. */
    private static final int     MAX_CALC_COLOR_LEVEL = 10;
    /** Minimum accumulated attenuation factor; rays below this are discarded. */
    private static final double  MIN_CALC_COLOR_K     = 0.001;
    /** Initial attenuation factor for the primary ray (full intensity). */
    private static final Double3 INITIAL_K            = Double3.ONE;

    /** Blackboard used for distributing shadow rays across the light-source disk. */
    private Blackboard _shadowBlackboard = new Blackboard();

    /**
     * Constructs a simple ray tracer for the given scene.
     *
     * @param scene the scene to trace rays against
     */
    SimpleRayTracer(Scene scene) {
        super(scene);
    }

    /**
     * Configures the number of shadow-ray samples used for soft shadows.
     * Call before rendering; has no effect on lights with {@code radius == 0}.
     *
     * @param numSamples rays per light sample (1 = hard shadows)
     * @param pattern    the sampling pattern for the light disk
     * @return this tracer, for method chaining
     */
    SimpleRayTracer setShadowSamples(int numSamples, SamplingPattern pattern) {
        _shadowBlackboard = new Blackboard().setNumSamples(numSamples).setStrategy(pattern);
        return this;
    }

    /**
     * Entry-point color calculation: prepares the intersection and delegates to
     * the recursive overload with the initial level and attenuation factor.
     *
     * @param intersection the closest intersection along the ray
     * @param v            the normalized ray direction
     * @return the color at the intersection
     */
    private Color calcColor(Intersection intersection, Vector v) {
        return !preprocessIntersection(intersection, v) ? Color.BLACK
                : _scene.ambientLight.getIntensity().scale(intersection.material.kA)
                        .add(calcColor(intersection, MAX_CALC_COLOR_LEVEL, INITIAL_K));
    }

    /**
     * Recursive color calculation for global effects (reflection / transparency).
     * At each level computes local Phong effects; global effects will be added here
     * in subsequent steps.
     *
     * @param intersection the preprocessed intersection
     * @param level        remaining recursion depth (stops at 1)
     * @param k            accumulated attenuation factor (stops when below {@link #MIN_CALC_COLOR_K})
     * @return the color contribution at this intersection
     */
    private Color calcColor(Intersection intersection, int level, Double3 k) {
        Color color = calcLocalEffects(intersection);
        return level == 1 ? color : color.add(calcGlobalEffects(intersection, level, k));
    }

    @Override
    Color traceRay(Ray ray) {
        Intersection closest = findClosestIntersection(ray);
        return closest == null ? _scene.background : calcColor(closest, ray.direction());
    }

    /**
     * Returns {@code true} if the intersection point has line-of-sight to its
     * active light source (i.e. is not in shadow).
     * <p>
     * A shadow ray is fired from the surface point — offset by {@code Ray.DELTA}
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
        Ray shadowRay = new Ray(intersection.point, intersection.l.scale(-1), intersection.n);
        double lightDistance = intersection.light.getDistance(intersection.point);
        var blockers = _scene.geometries.calcIntersections(shadowRay, lightDistance);
        return blockers == null
                || blockers.stream().noneMatch(i -> i.material.kT.isLowerThan(MIN_CALC_COLOR_K));
    }

    /**
     * Computes the accumulated transparency factor between the intersection point
     * and its active light source.
     * <p>
     * Each occluder along the shadow ray reduces the factor by its own {@code kT}.
     * If the product drops below {@link #MIN_CALC_COLOR_K} the point is considered
     * fully in shadow and {@link Double3#ZERO} is returned immediately.
     * </p>
     *
     * @param intersection the preprocessed intersection (uses {@code point}, {@code n},
     *                     {@code l}, {@code light})
     * @return accumulated transparency coefficient in [0,1]³,
     *         {@link Double3#ONE} if nothing blocks the light
     */
    private Double3 transparency(Intersection intersection) {
        if (intersection.light instanceof PointLight pl && pl.getRadius() > 0)
            return softTransparency(intersection, pl);

        Ray shadowRay = new Ray(intersection.point, intersection.l.scale(-1), intersection.n);
        double lightDistance = intersection.light.getDistance(intersection.point);
        var blockers = _scene.geometries.calcIntersections(shadowRay, lightDistance);
        if (blockers == null) return Double3.ONE;
        Double3 ktr = Double3.ONE;
        for (var blocker : blockers) {
            ktr = ktr.product(blocker.material.kT);
            if (ktr.isLowerThan(MIN_CALC_COLOR_K)) return Double3.ZERO;
        }
        return ktr;
    }

    /**
     * Computes a soft-shadow transparency factor by sampling N points on the
     * light-source disk and averaging the transparency of each shadow ray.
     * Points on the disk are distributed using {@link #_shadowBlackboard}.
     *
     * @param intersection the preprocessed intersection
     * @param light        the area point light (must have radius &gt; 0)
     * @return averaged transparency coefficient across all light-disk samples
     */
    private Double3 softTransparency(Intersection intersection, PointLight light) {
        // Build two axes that span the plane perpendicular to the shadow direction
        Vector toLight = intersection.l.scale(-1);
        Vector vX = Math.abs(toLight.dotProduct(Vector.AXIS_Y)) < 0.9
                ? toLight.crossProduct(Vector.AXIS_Y).normalize()
                : toLight.crossProduct(Vector.AXIS_X).normalize();
        Vector vY = toLight.crossProduct(vX).normalize();

        List<Point> samples = _shadowBlackboard.generateTargetPoints(
                light.getPosition(), vX, vY, light.getRadius() * 2);

        Double3 total = Double3.ZERO;
        for (Point sample : samples) {
            Vector dir   = sample.subtract(intersection.point);
            double dist  = dir.length();
            Ray shadowRay = new Ray(intersection.point, dir.normalize(), intersection.n);
            var blockers  = _scene.geometries.calcIntersections(shadowRay, dist);
            Double3 ktr   = Double3.ONE;
            if (blockers != null)
                for (var blocker : blockers) {
                    ktr = ktr.product(blocker.material.kT);
                    if (ktr.isLowerThan(MIN_CALC_COLOR_K)) { ktr = Double3.ZERO; break; }
                }
            total = total.add(ktr);
        }
        return total.scale(1.0 / samples.size());
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
        for (LightSource lightSource : _scene.lights) {
            if (!preprocessLightSource(intersection, lightSource)) continue;
            Double3 ktr = transparency(intersection);
            if (ktr.isGreaterThan(MIN_CALC_COLOR_K))
                color = color.add(lightSource.getIntensity(intersection.point)
                        .scale(ktr.product(calcDiffuse(intersection).add(calcSpecular(intersection)))));
        }
        return color;
    }

    /**
     * Finds the closest intersection of the given ray with the scene geometries.
     *
     * @param ray the ray to test
     * @return the closest {@link Intersection}, or {@code null} if the ray hits nothing
     */
    private Intersection findClosestIntersection(Ray ray) {
        return ray.findClosestIntersection(_scene.geometries.calcIntersections(ray));
    }

    /**
     * Constructs the specular reflection ray at the intersection point.
     * The origin is offset along the normal toward the reflected direction
     * to avoid self-intersection.
     *
     * @param intersection the preprocessed intersection (uses {@code point}, {@code n}, {@code v})
     * @return the reflected secondary ray
     */
    private Ray constructReflectionRay(Intersection intersection) {
        double vn = intersection.v.dotProduct(intersection.n);
        Vector r = intersection.v.subtract(intersection.n.scale(2 * vn));
        return new Ray(intersection.point, r, intersection.n);
    }

    /**
     * Constructs the transparency (refraction) ray at the intersection point.
     * The origin is offset along the normal toward the transmitted direction
     * (i.e. into the geometry) to avoid self-intersection.
     *
     * @param intersection the preprocessed intersection (uses {@code point}, {@code n}, {@code v})
     * @return the transmitted secondary ray
     */
    private Ray constructTransparencyRay(Intersection intersection) {
        return new Ray(intersection.point, intersection.v, intersection.n);
    }

    /**
     * Computes the color contribution of one global effect (reflection or transparency)
     * by tracing the given secondary ray recursively.
     *
     * @param ray   the secondary ray (reflection or transparency)
     * @param level remaining recursion depth
     * @param k     accumulated attenuation so far
     * @param kx    the material's attenuation coefficient for this effect (kR or kT)
     * @return the color contribution scaled by {@code kx}, or the background if no hit
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkx = k.product(kx);
        if (kkx.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
        Intersection intersection = findClosestIntersection(ray);
        if (intersection == null) return _scene.background;
        return preprocessIntersection(intersection, ray.direction())
                ? calcColor(intersection, level - 1, kkx).scale(kx)
                : Color.BLACK;
    }

    /**
     * Computes the combined color contribution of both global effects
     * (reflection and transparency) at the intersection point.
     *
     * @param intersection the preprocessed intersection
     * @param level        remaining recursion depth
     * @param k            accumulated attenuation so far
     * @return sum of reflection and transparency color contributions
     */
    private Color calcGlobalEffects(Intersection intersection, int level, Double3 k) {
        return calcGlobalEffect(constructReflectionRay(intersection),  level, k, intersection.material.kR)
              .add(calcGlobalEffect(constructTransparencyRay(intersection), level, k, intersection.material.kT));
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

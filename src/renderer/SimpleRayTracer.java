package renderer;

import java.util.List;
import java.util.function.Function;
import lighting.LightSource;
import lighting.PointLight;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;
import scene.Scene;

import static geometries.api.Intersectable.Intersection;
import static primitives.Util.alignZero;

/**
 * A ray tracer implementing the Phong reflection model with multiple
 * external light sources.
 *
 * @author Ambash and Elyasaf
 */
class SimpleRayTracer extends RayTracerBase {

    /**
     * Maximum recursion depth for reflection/transparency color calculation.
     */
    private static final int MAX_CALC_COLOR_LEVEL = 10;
    /**
     * Minimum accumulated attenuation factor; rays below this are discarded.
     */
    private static final double MIN_CALC_COLOR_K = 0.001;
    /**
     * Initial attenuation factor for the primary ray (full intensity).
     */
    private static final Double3 INITIAL_K = Double3.ONE;

    /**
     * Blackboard used for distributing shadow rays across the light-source disk.
     */
    private Blackboard _shadowBlackboard = new Blackboard();
    /**
     * Blackboard used for distributing glossy reflection rays around the mirror direction.
     */
    private Blackboard _glossyBlackboard = new Blackboard();
    /**
     * Blackboard used for distributing diffuse glass rays around the refraction direction.
     */
    private Blackboard _diffuseBlackboard = new Blackboard();

    /** Max quadtree depth for adaptive soft shadows (0 = use {@link #_shadowBlackboard} instead). */
    private int    _shadowAdaptiveDepth     = 0;
    /** Max per-channel (0-255-equivalent) transparency difference tolerated before subdividing further. */
    private double _shadowAdaptiveThreshold = 0;

    /** Max quadtree depth for adaptive glossy reflection (0 = use {@link #_glossyBlackboard} instead). */
    private int    _glossyAdaptiveDepth     = 0;
    /** Max per-channel (0-255) color difference tolerated before subdividing further. */
    private double _glossyAdaptiveThreshold = 0;

    /** Max quadtree depth for adaptive diffuse glass (0 = use {@link #_diffuseBlackboard} instead). */
    private int    _diffuseAdaptiveDepth     = 0;
    /** Max per-channel (0-255) color difference tolerated before subdividing further. */
    private double _diffuseAdaptiveThreshold = 0;

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

    SimpleRayTracer setGlossySamples(int numSamples, SamplingPattern pattern) {
        _glossyBlackboard = new Blackboard().setNumSamples(numSamples).setStrategy(pattern);
        return this;
    }

    SimpleRayTracer setDiffuseSamples(int numSamples, SamplingPattern pattern) {
        _diffuseBlackboard = new Blackboard().setNumSamples(numSamples).setStrategy(pattern);
        return this;
    }

    /**
     * Enables adaptive soft shadows: instead of a fixed number of shadow rays,
     * the light disk is recursively subdivided only where sampled points
     * disagree about occlusion by more than {@code threshold}. Overrides any
     * {@link #setShadowSamples} setting.
     *
     * @param maxDepth  maximum quadtree subdivision depth (0 = disabled)
     * @param threshold maximum per-channel (0-255-equivalent) transparency
     *                  difference tolerated across a region's corners
     * @return this tracer, for method chaining
     */
    SimpleRayTracer setAdaptiveShadowSampling(int maxDepth, double threshold) {
        if (maxDepth < 0) throw new IllegalArgumentException("maxDepth must be >= 0");
        if (threshold < 0) throw new IllegalArgumentException("threshold must be >= 0");
        _shadowAdaptiveDepth = maxDepth;
        _shadowAdaptiveThreshold = threshold;
        return this;
    }

    /**
     * Enables adaptive glossy reflection sampling. Overrides any
     * {@link #setGlossySamples} setting.
     *
     * @param maxDepth  maximum quadtree subdivision depth (0 = disabled)
     * @param threshold maximum per-channel (0-255) color difference tolerated
     *                  across a region's corners
     * @return this tracer, for method chaining
     */
    SimpleRayTracer setAdaptiveGlossySampling(int maxDepth, double threshold) {
        if (maxDepth < 0) throw new IllegalArgumentException("maxDepth must be >= 0");
        if (threshold < 0) throw new IllegalArgumentException("threshold must be >= 0");
        _glossyAdaptiveDepth = maxDepth;
        _glossyAdaptiveThreshold = threshold;
        return this;
    }

    /**
     * Enables adaptive diffuse-glass sampling. Overrides any
     * {@link #setDiffuseSamples} setting.
     *
     * @param maxDepth  maximum quadtree subdivision depth (0 = disabled)
     * @param threshold maximum per-channel (0-255) color difference tolerated
     *                  across a region's corners
     * @return this tracer, for method chaining
     */
    SimpleRayTracer setAdaptiveDiffuseSampling(int maxDepth, double threshold) {
        if (maxDepth < 0) throw new IllegalArgumentException("maxDepth must be >= 0");
        if (threshold < 0) throw new IllegalArgumentException("threshold must be >= 0");
        _diffuseAdaptiveDepth = maxDepth;
        _diffuseAdaptiveThreshold = threshold;
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
     * {@link Double3#ONE} if nothing blocks the light
     */
    private Double3 transparency(Intersection intersection) {
        if (intersection.light instanceof PointLight pl && pl.getRadius() > 0) {
            if (_shadowAdaptiveDepth > 0) return adaptiveTransparency(intersection, pl);
            if (_shadowBlackboard.isMultiSample()) return softTransparency(intersection, pl);
        }

        Ray shadowRay = new Ray(intersection.point, intersection.l.scale(-1), intersection.n);
        return singleRayTransparency(shadowRay, intersection.light.getDistance(intersection.point));
    }

    /**
     * Computes the transparency factor along one shadow ray up to {@code maxDist}.
     * Multiplies the {@code kT} of every occluder; early-exits with ZERO when
     * the product drops below {@link #MIN_CALC_COLOR_K}.
     *
     * @param shadowRay the shadow ray
     * @param maxDist   maximum distance to consider (stops at the light)
     * @return accumulated transparency in [0,1]³
     */
    private Double3 singleRayTransparency(Ray shadowRay, double maxDist) {
        var blockers = _scene.geometries.calcIntersections(shadowRay, maxDist);
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
        Vector toLight = intersection.l.scale(-1);
        Vector[] basis = toLight.buildOrthogonalBasis();
        Vector vX = basis[0], vY = basis[1];

        List<Point> samples = _shadowBlackboard.generateTargetPoints(
                light.getPosition(), vX, vY, light.getRadius() * 2);

        Double3 total = Double3.ZERO;
        for (Point sample : samples) {
            Vector dir = sample.subtract(intersection.point);
            double dist = dir.length();
            Ray shadowRay = new Ray(intersection.point, dir.normalize(), intersection.n);
            total = total.add(singleRayTransparency(shadowRay, dist));
        }
        return total.scale(1.0 / samples.size());
    }

    /**
     * Adaptive counterpart of {@link #softTransparency}: recursively
     * subdivides the light disk via {@link AdaptiveSampler} instead of
     * sampling a fixed number of points.
     *
     * @param intersection the preprocessed intersection
     * @param light        the area point light (must have radius &gt; 0)
     * @return adaptively averaged transparency coefficient across the light disk
     */
    private Double3 adaptiveTransparency(Intersection intersection, PointLight light) {
        Vector toLight = intersection.l.scale(-1);
        Vector[] basis = toLight.buildOrthogonalBasis();
        Vector vX = basis[0], vY = basis[1];
        double size = light.getRadius() * 2;

        Function<Point, Double3> evaluate = sample -> {
            Vector dir = sample.subtract(intersection.point);
            Ray shadowRay = new Ray(intersection.point, dir.normalize(), intersection.n);
            return singleRayTransparency(shadowRay, dir.length());
        };

        return AdaptiveSampler.forTransparency(evaluate, vX, vY, _shadowAdaptiveDepth, _shadowAdaptiveThreshold)
                .sample(light.getPosition(), size, size);
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
            if (ktr.isNotLowerThan(MIN_CALC_COLOR_K))
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
     * @return the color contribution scaled by {@code kx} — either the recursively
     *         traced hit color, or the background if the secondary ray hits nothing
     */
    private Color calcGlobalEffect(Ray ray, int level, Double3 k, Double3 kx) {
        Double3 kkx = k.product(kx);
        if (kkx.isLowerThan(MIN_CALC_COLOR_K)) return Color.BLACK;
        Intersection intersection = findClosestIntersection(ray);
        if (intersection == null) return _scene.background.scale(kx);
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
        return calcReflection(intersection, level, k)
                .add(calcTransparency(intersection, level, k));
    }

    /**
     * Computes the glossy reflection contribution. Reads {@code kR} and
     * {@code kGlossy} directly from the intersection's material.
     * When {@code kGlossy == 0} (default) a single mirror ray is used.
     */
    private Color calcReflection(Intersection intersection, int level, Double3 k) {
        double blur = level == MAX_CALC_COLOR_LEVEL ? intersection.material.kGlossy : 0;
        return sampleBlurredRay(constructReflectionRay(intersection), blur, _glossyBlackboard,
                _glossyAdaptiveDepth, _glossyAdaptiveThreshold, level, k, intersection.material.kR);
    }

    private Color calcTransparency(Intersection intersection, int level, Double3 k) {
        double blur = level == MAX_CALC_COLOR_LEVEL ? intersection.material.kDiffuseGlass : 0;
        return sampleBlurredRay(constructTransparencyRay(intersection), blur, _diffuseBlackboard,
                _diffuseAdaptiveDepth, _diffuseAdaptiveThreshold, level, k, intersection.material.kT);
    }

    /**
     * Sends a beam of rays around {@code centralRay} and averages their colors.
     * The beam disk is perpendicular to the ray direction; its radius equals
     * {@code blur}. When {@code blur == 0} only the central ray is traced.
     * When {@code adaptiveDepth > 0} the disk is recursively subdivided via
     * {@link AdaptiveSampler} instead of using {@code blackboard}'s fixed-N samples.
     */
    private Color sampleBlurredRay(Ray centralRay, double blur, Blackboard blackboard,
                                   int adaptiveDepth, double adaptiveThreshold,
                                   int level, Double3 k, Double3 kx) {
        if (Util.isZero(blur)) return calcGlobalEffect(centralRay, level, k, kx);

        Vector dir = centralRay.direction();
        Vector[] basis = dir.buildOrthogonalBasis();
        Vector vX = basis[0], vY = basis[1];
        Point origin = centralRay.origin();

        Function<Point, Color> evaluate = sample -> {
            // GRID sampling always includes an exact dead-center sample when
            // sqrt(numSamples) is odd (e.g. 9, 25, 81 -> 3x3, 5x5, 9x9), and the
            // adaptive sampler's own recursion always re-visits the exact disk
            // center too: for that sample, sample == origin, so subtract() would
            // build the zero vector. A zero offset means "no deviation", so just
            // reuse the unperturbed central direction instead of computing it.
            Vector newDir = sample.equals(origin) ? dir : dir.add(sample.subtract(origin)).normalize();
            return calcGlobalEffect(new Ray(origin, newDir), level, k, kx);
        };

        if (adaptiveDepth > 0)
            return AdaptiveSampler.forColor(evaluate, vX, vY, adaptiveDepth, adaptiveThreshold)
                    .sample(origin, blur, blur);

        if (!blackboard.isMultiSample()) return calcGlobalEffect(centralRay, level, k, kx);

        List<Point> samples = blackboard.generateTargetPoints(origin, vX, vY, blur);
        Color total = Color.BLACK;
        for (Point sample : samples) total = total.add(evaluate.apply(sample));
        return total.reduce(samples.size());
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
        Vector r = intersection.l.subtract(intersection.n.scale(2 * intersection.ln));
        double minusVR = alignZero(-intersection.v.dotProduct(r));
        return minusVR <= 0 ? Double3.ZERO
                : intersection.material.kS.scale(Math.pow(minusVR, intersection.material.nShininess));
    }
}

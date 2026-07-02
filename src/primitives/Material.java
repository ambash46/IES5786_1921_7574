package primitives;

/**
 * Represents the material properties of a geometry.
 * <p>
 * Plain Data Structure: all fields are {@code public}.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public class Material {

    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    public Material() { /* no-op */ }

    /** Ambient attenuation coefficient. */
    public Double3 kA         = Double3.ONE;
    /** Diffuse attenuation coefficient. */
    public Double3 kD         = Double3.ZERO;
    /** Specular attenuation coefficient. */
    public Double3 kS         = Double3.ZERO;
    /** Transparency attenuation coefficient. */
    public Double3 kT         = Double3.ZERO;
    /** Reflection attenuation coefficient. */
    public Double3 kR         = Double3.ZERO;
    /** Shininess exponent for the specular highlight. Must be ≥ 1: the Phong
     * specular formula raises a value in (0,1] to this power, and an exponent
     * of 0 collapses it to a constant 1 — flooding the whole surface with a
     * flat kS contribution instead of a falloff-limited highlight. */
    public int    nShininess    = 1;
    /** Glossy reflection blur: 0 = perfect mirror, 1 = fully diffuse reflection. */
    public double kGlossy       = 0.0;
    /** Diffuse glass blur: 0 = perfectly clear, 1 = fully milky transparency. */
    public double kDiffuseGlass = 0.0;

    /**
     * Validates that every component of an attenuation coefficient lies within
     * the physically meaningful range [0,1].
     * <p>
     * Components are aligned via {@link Util#alignZero(double)} before the
     * bounds check, so floating-point noise (e.g. a component of
     * {@code 1.0000000000000002} produced by upstream arithmetic) is not
     * mistaken for a real out-of-range value.
     *
     * @param k    the coefficient to validate
     * @param name the coefficient's name, used in the exception message
     * @throws IllegalArgumentException if any component is outside [0,1]
     */
    private static void validateCoefficient(Double3 k, String name) {
        if (isOutOfRange(k._d1()) || isOutOfRange(k._d2()) || isOutOfRange(k._d3()))
            throw new IllegalArgumentException(name + " components must be in [0,1]");
    }

    /**
     * Checks whether a single coefficient component is outside [0,1], tolerating
     * floating-point noise around the two bounds.
     *
     * @param component the component to check
     * @return {@code true} if the component is meaningfully below 0 or above 1
     */
    private static boolean isOutOfRange(double component) {
        return Util.alignZero(component) < 0 || Util.alignZero(component - 1) > 0;
    }

    /**
     * Sets the ambient attenuation coefficient.
     *
     * @param kA the ambient coefficient as a {@link Double3}
     * @return this material, for method chaining
     */
    public Material setKA(Double3 kA) {
        validateCoefficient(kA, "kA");
        this.kA = kA;
        return this;
    }

    /**
     * Sets the ambient attenuation coefficient uniformly across all components.
     *
     * @param kA the ambient coefficient as a scalar
     * @return this material, for method chaining
     */
    public Material setKA(double kA) {
        return setKA(new Double3(kA));
    }

    /**
     * Sets the diffuse attenuation coefficient.
     *
     * @param kD the diffuse coefficient as a {@link Double3}
     * @return this material, for method chaining
     */
    public Material setKD(Double3 kD) {
        validateCoefficient(kD, "kD");
        this.kD = kD;
        return this;
    }

    /**
     * Sets the diffuse attenuation coefficient uniformly across all components.
     *
     * @param kD the diffuse coefficient as a scalar
     * @return this material, for method chaining
     */
    public Material setKD(double kD) {
        return setKD(new Double3(kD));
    }

    /**
     * Sets the specular attenuation coefficient.
     *
     * @param kS the specular coefficient as a {@link Double3}
     * @return this material, for method chaining
     */
    public Material setKS(Double3 kS) {
        validateCoefficient(kS, "kS");
        this.kS = kS;
        return this;
    }

    /**
     * Sets the specular attenuation coefficient uniformly across all components.
     *
     * @param kS the specular coefficient as a scalar
     * @return this material, for method chaining
     */
    public Material setKS(double kS) {
        return setKS(new Double3(kS));
    }

    /**
     * Sets the transparency attenuation coefficient.
     *
     * @param kT the transparency coefficient as a {@link Double3}
     * @return this material, for method chaining
     */
    public Material setKT(Double3 kT) {
        validateCoefficient(kT, "kT");
        this.kT = kT;
        return this;
    }

    /**
     * Sets the transparency attenuation coefficient uniformly across all components.
     *
     * @param kT the transparency coefficient as a scalar
     * @return this material, for method chaining
     */
    public Material setKT(double kT) {
        return setKT(new Double3(kT));
    }

    /**
     * Sets the reflection attenuation coefficient.
     *
     * @param kR the reflection coefficient as a {@link Double3}
     * @return this material, for method chaining
     */
    public Material setKR(Double3 kR) {
        validateCoefficient(kR, "kR");
        this.kR = kR;
        return this;
    }

    /**
     * Sets the reflection attenuation coefficient uniformly across all components.
     *
     * @param kR the reflection coefficient as a scalar
     * @return this material, for method chaining
     */
    public Material setKR(double kR) {
        return setKR(new Double3(kR));
    }

    /**
     * Sets the shininess exponent for the specular highlight.
     *
     * @param  nShininess               the shininess exponent (must be ≥ 1)
     * @return                          this material, for method chaining
     * @throws IllegalArgumentException if {@code nShininess} is less than 1
     */
    public Material setShininess(int nShininess) {
        if (nShininess < 1) throw new IllegalArgumentException("nShininess must be >= 1");
        this.nShininess = nShininess;
        return this;
    }

    /**
     * Sets the glossy reflection blur factor (0 = perfect mirror, 1 = fully diffuse).
     *
     * @param kGlossy blur factor in [0,1]
     * @return this material, for method chaining
     */
    public Material setKGlossy(double kGlossy) {
        if (kGlossy < 0 || kGlossy > 1)
            throw new IllegalArgumentException("kGlossy must be in [0,1]");
        this.kGlossy = kGlossy;
        return this;
    }

    /**
     * Sets the diffuse glass blur factor (0 = perfectly clear, 1 = fully milky).
     *
     * @param kDiffuseGlass blur factor in [0,1]
     * @return this material, for method chaining
     */
    public Material setKDiffuseGlass(double kDiffuseGlass) {
        if (kDiffuseGlass < 0 || kDiffuseGlass > 1)
            throw new IllegalArgumentException("kDiffuseGlass must be in [0,1]");
        this.kDiffuseGlass = kDiffuseGlass;
        return this;
    }
}

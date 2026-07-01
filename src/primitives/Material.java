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
    /** Shininess exponent for the specular highlight. */
    public int    nShininess    = 0;
    /** Glossy reflection blur: 0 = perfect mirror, 1 = fully diffuse reflection. */
    public double kGlossy       = 0.0;
    /** Diffuse glass blur: 0 = perfectly clear, 1 = fully milky transparency. */
    public double kDiffuseGlass = 0.0;

    /**
     * Sets the ambient attenuation coefficient.
     *
     * @param kA the ambient coefficient as a {@link Double3}
     * @return this material, for method chaining
     */
    public Material setKA(Double3 kA) {
        if (kA._d1() > 1 || kA._d2() > 1 || kA._d3() > 1)
            throw new IllegalArgumentException("kA components must not exceed 1");
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
        if (kA > 1) throw new IllegalArgumentException("kA must not exceed 1");
        this.kA = new Double3(kA);
        return this;
    }

    /**
     * Sets the diffuse attenuation coefficient.
     *
     * @param kD the diffuse coefficient as a {@link Double3}
     * @return this material, for method chaining
     */
    public Material setKD(Double3 kD) {
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
        this.kD = new Double3(kD);
        return this;
    }

    /**
     * Sets the specular attenuation coefficient.
     *
     * @param kS the specular coefficient as a {@link Double3}
     * @return this material, for method chaining
     */
    public Material setKS(Double3 kS) {
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
        this.kS = new Double3(kS);
        return this;
    }

    /**
     * Sets the transparency attenuation coefficient.
     *
     * @param kT the transparency coefficient as a {@link Double3}
     * @return this material, for method chaining
     */
    public Material setKT(Double3 kT) {
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
        this.kT = new Double3(kT);
        return this;
    }

    /**
     * Sets the reflection attenuation coefficient.
     *
     * @param kR the reflection coefficient as a {@link Double3}
     * @return this material, for method chaining
     */
    public Material setKR(Double3 kR) {
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
        this.kR = new Double3(kR);
        return this;
    }

    /**
     * Sets the shininess exponent for the specular highlight.
     *
     * @param nShininess the shininess exponent
     * @return this material, for method chaining
     */
    public Material setShininess(int nShininess) {
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

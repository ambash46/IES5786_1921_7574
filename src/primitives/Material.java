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
    /** Shininess exponent for the specular highlight. */
    public int     nShininess = 0;

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
     * Sets the shininess exponent for the specular highlight.
     *
     * @param nShininess the shininess exponent
     * @return this material, for method chaining
     */
    public Material setShininess(int nShininess) {
        this.nShininess = nShininess;
        return this;
    }
}

package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

import static primitives.Util.alignZero;

/**
 * Represents a spot light source — a point light that emits light in a focused
 * cone toward a specific direction.
 *
 * @author Ambash and Elyasaf
 */
public final class SpotLight extends PointLight {

    /** The normalized direction the spot light points toward. */
    private final Vector _direction;

    /**
     * Exponent applied to the beam factor to narrow the light cone.
     * A value of 1 gives a regular spotlight; higher values produce a tighter beam.
     */
    private int _narrowBeam = 1;

    /**
     * Constructs a spot light with the given intensity, position, and direction.
     *
     * @param intensity the color intensity of the light source
     * @param position  the position of the light source
     * @param direction the direction the spot light points toward (will be normalized)
     */
    public SpotLight(Color intensity, Point position, Vector direction) {
        super(intensity, position);
        _direction = direction.normalize();
    }

    @Override
    public SpotLight setKC(double kC) {
        return (SpotLight) super.setKC(kC);
    }

    @Override
    public SpotLight setKl(double kL) {
        return (SpotLight) super.setKl(kL);
    }

    @Override
    public SpotLight setKq(double kQ) {
        return (SpotLight) super.setKq(kQ);
    }

    /**
     * Sets the beam-narrowing exponent.
     *
     * @param  narrowBeam               the exponent applied to the beam factor (must be ≥ 1)
     * @return                          this SpotLight, for method chaining
     * @throws IllegalArgumentException if {@code narrowBeam} is less than 1
     */
    public SpotLight setNarrowBeam(int narrowBeam) {
        if (narrowBeam < 1) throw new IllegalArgumentException("narrowBeam must be >= 1");
        _narrowBeam = narrowBeam;
        return this;
    }

    @Override
    public Color getIntensity(Point p) {
        if (_position.equals(p))
            return super.getIntensity(p);
        double factor = alignZero(_direction.dotProduct(getL(p)));
        return factor <= 0 ? Color.BLACK
                : super.getIntensity(p).scale(Math.pow(factor, _narrowBeam));
    }
}

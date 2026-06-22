package lighting;

import primitives.Color;
import primitives.Point;
import primitives.Vector;

/**
 * Represents a point light source — a light that emits equally in all
 * directions from a fixed position in the scene.
 *
 * @author Ambash and Elyasaf
 */
public class PointLight extends Light implements LightSource {

    /** The position of the light source in the scene. */
    protected final Point _position;

    /** Constant attenuation coefficient — ensures the denominator is always ≥ 1. */
    private double _kC = 1;
    /** Linear attenuation coefficient. */
    private double _kL = 0;
    /** Quadratic attenuation coefficient. */
    private double _kQ = 0;
    /** Radius of the light source disk (0 = point light / hard shadows). */
    private double _radius = 0;

    /**
     * Constructs a point light with the given intensity and position.
     *
     * @param intensity the color intensity of the light source
     * @param position  the position of the light source
     */
    public PointLight(Color intensity, Point position) {
        super(intensity);
        _position = position;
    }

    /**
     * Sets the constant attenuation coefficient.
     *
     * @param kC constant attenuation factor (must be ≥ 1 to keep denominator &gt; 1)
     * @return this PointLight, for method chaining
     */
    public PointLight setKC(double kC) {
        _kC = kC;
        return this;
    }

    /**
     * Sets the linear attenuation coefficient.
     *
     * @param kL linear attenuation factor
     * @return this PointLight, for method chaining
     */
    public PointLight setKl(double kL) {
        _kL = kL;
        return this;
    }

    /**
     * Sets the quadratic attenuation coefficient.
     *
     * @param kQ quadratic attenuation factor
     * @return this PointLight, for method chaining
     */
    public PointLight setKq(double kQ) {
        _kQ = kQ;
        return this;
    }

    /**
     * Sets the radius of the light source disk for soft-shadow sampling.
     * A radius of 0 (the default) produces hard shadows.
     *
     * @param radius the disk radius in world units (must be ≥ 0)
     * @return this PointLight, for method chaining
     */
    public PointLight setRadius(double radius) {
        _radius = radius;
        return this;
    }

    /** Returns the light source disk radius (0 = hard shadows). */
    public double getRadius() { return _radius; }

    /** Returns the position of this light source. */
    public Point getPosition() { return _position; }

    @Override
    public Vector getL(Point p) {
        return p.subtract(_position).normalize();
    }

    @Override
    public Color getIntensity(Point p) {
        double d = _position.distance(p);
        return _intensity.scale(1d / (_kC + _kL * d + _kQ * d * d));
    }

    @Override
    public double getDistance(Point point) {
        return _position.distance(point);
    }
}

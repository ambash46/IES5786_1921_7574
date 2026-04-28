package renderer;

import java.util.MissingResourceException;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;

/**
 * Represents a camera in a three-dimensional scene.
 * <p>
 * A camera is defined by its location, orientation vectors, and view-plane
 * parameters. Use {@link #getBuilder()} to obtain a {@link Builder} and
 * construct an instance.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
public class Camera implements Cloneable {

    /**
     * The camera location in the scene.
     */
    private Point _p0;

    /**
     * The forward direction vector (toward the scene).
     */
    private Vector _vTo;

    /**
     * The up direction vector.
     */
    private Vector _vUp;

    /**
     * The right direction vector (computed from vTo and vUp).
     */
    private Vector _vRight;

    /**
     * The width of the view plane.
     */
    private double _width;

    /**
     * The height of the view plane.
     */
    private double _height;

    /**
     * The distance from the camera to the view plane.
     */
    private double _distance;

    /**
     * The number of columns (pixels) in the view plane.
     */
    private int _nX = 1;

    /**
     * The number of rows (pixels) in the view plane.
     */
    private int _nY = 1;

    /**
     * The center point of the view plane.
     */
    private Point _vpCenter;

    /**
     * The width of a single pixel.
     */
    private double _pixelWidth;

    /**
     * The height of a single pixel.
     */
    private double _pixelHeight;

    /**
     * Private constructor — use {@link #getBuilder()} instead.
     */
    private Camera() {
    }


    /**
     * Returns a new {@link Builder} for constructing a {@link Camera}.
     *
     * @return a fresh Builder instance
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Returns the number of pixel columns in the view plane.
     *
     * @return the horizontal pixel count (nX)
     */
    public int getNX() {
        return _nX;
    }

    /**
     * Returns the number of pixel rows in the view plane.
     *
     * @return the vertical pixel count (nY)
     */
    public int getNY() {
        return _nY;
    }

    /**
     * Constructs the ray that passes through the center of pixel (xIndex, yIndex)
     * on the view plane.
     *
     * @param xIndex the column index of the pixel (0-based)
     * @param yIndex the row index of the pixel (0-based)
     * @return the ray through the given pixel
     */
    public Ray constructRay(int xIndex, int yIndex) {
        // offset from center pixel: index 0 is left/top, (nX-1)/2 is the center
        double xOffset = (xIndex - (_nX - 1) / 2.0) * _pixelWidth;
        // Y-axis is inverted: pixel index grows downward, vUp points up — hence the subtraction
        double yOffset = ((_nY - 1) / 2.0 - yIndex) * _pixelHeight;
        Point pixelCenter = _vpCenter;
        if (!Util.isZero(xOffset)) pixelCenter = pixelCenter.add(_vRight.scale(xOffset));
        if (!Util.isZero(yOffset)) pixelCenter = pixelCenter.add(_vUp.scale(yOffset));
        return new Ray(_p0, pixelCenter.subtract(_p0));
    }

    /**
     * Builder for {@link Camera}.
     * <p>
     * Holds a {@link Camera} instance internally and populates its fields
     * through fluent setter methods. Call {@link #build()} to obtain the
     * finished, validated camera.
     * </p>
     * <p>
     * Obtain an instance via {@link Camera#getBuilder()} rather than calling
     * this constructor directly.
     * </p>
     */
    public static class Builder {

        /**
         * Default constructor — use {@link Camera#getBuilder()} to obtain an instance.
         */
        public Builder() {
        }

        /**
         * The camera being assembled.
         */
        private final Camera _camera = new Camera();

        /**
         * Auxiliary: the explicit forward direction vector (null if a target point
         * was given instead).
         */
        private Vector _vTo = null;

        /**
         * Auxiliary: the target point the camera looks at (null if an explicit
         * direction vector was given instead).
         */
        private Point _target = null;

        /**
         * Auxiliary: the general up vector; defaults to {@link Vector#AXIS_Y} when
         * not provided by the caller.
         */
        private Vector _vUp = Vector.AXIS_Y;

        /**
         * Sets the camera location.
         *
         * @param location the position of the camera in the scene
         * @return this Builder
         */
        public Builder setLocation(Point location) {
            _camera._p0 = location;
            return this;
        }

        /**
         * Sets the camera orientation using explicit forward and up vectors.
         *
         * @param vTo the forward direction vector
         * @param vUp the up direction vector
         * @return this Builder
         */
        public Builder setDirection(Vector vTo, Vector vUp) {
            _vTo = vTo;
            _target = null;
            _vUp = vUp;
            return this;
        }

        /**
         * Sets the camera orientation by pointing toward a target point with a
         * given up vector.
         *
         * @param target the point the camera looks at
         * @param vUp    the up direction vector
         * @return this Builder
         */
        public Builder setDirection(Point target, Vector vUp) {
            _vTo = null;
            _target = target;
            _vUp = vUp;
            return this;
        }

        /**
         * Sets the camera orientation by pointing toward a target point; the up
         * vector defaults to {@link Vector#AXIS_Y}.
         *
         * @param target the point the camera looks at
         * @return this Builder
         */
        public Builder setDirection(Point target) {
            _vTo = null;
            _target = target;
            _vUp = Vector.AXIS_Y;
            return this;
        }

        /**
         * Sets the distance from the camera to the view plane.
         *
         * @param distance the view-plane distance (must be positive)
         * @return this Builder
         */
        public Builder setVpDistance(double distance) {
            _camera._distance = distance;
            return this;
        }

        /**
         * Sets the physical size of the view plane.
         *
         * @param width  the view-plane width (must be positive)
         * @param height the view-plane height (must be positive)
         * @return this Builder
         */
        public Builder setVpSize(double width, double height) {
            _camera._width = width;
            _camera._height = height;
            return this;
        }

        /**
         * Sets the pixel resolution of the rendered image.
         *
         * @param nX the number of pixel columns (must be positive)
         * @param nY the number of pixel rows (must be positive)
         * @return this Builder
         */
        public Builder setResolution(int nX, int nY) {
            _camera._nX = nX;
            _camera._nY = nY;
            return this;
        }

        /**
         * Validates that the resolution values are positive.
         *
         * @throws IllegalArgumentException if nX or nY are not positive
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0)
                throw new IllegalArgumentException("Resolution values must be positive");
        }

        /**
         * Validates the camera location and direction, then computes and stores the
         * three orthonormal orientation vectors ({@code _vTo}, {@code _vRight},
         * {@code _vUp}) in the camera.
         *
         * @throws MissingResourceException if the location or direction info is absent
         * @throws IllegalArgumentException if the direction and up vectors are parallel
         */
        private void checkLocationAndDirection() {
            if (_camera._p0 == null)
                throw new MissingResourceException(
                        "Camera location is missing", Camera.class.getName(), "location");

            if (_vTo == null) {
                if (_target == null)
                    throw new MissingResourceException(
                            "Camera direction is missing", Camera.class.getName(), "direction");
                try {
                    _vTo = _target.subtract(_camera._p0);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Camera location cannot be the same as the target point");
                }
            }

            // vRight = vTo × vUp: perpendicular to both, points right in a right-handed system
            try {
                _camera._vRight = _vTo.crossProduct(_vUp).normalize();
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                        "Direction vector and up vector must not be parallel");
            }

            _camera._vTo = _vTo.normalize();
            // true vUp = vRight × vTo: guarantees full orthogonality of the basis,
            // because the caller's vUp is not necessarily perpendicular to vTo
            _camera._vUp = _camera._vRight.crossProduct(_camera._vTo);
        }

        /**
         * Validates the view-plane parameters, then computes the view-plane center
         * and per-pixel dimensions.
         *
         * @throws IllegalArgumentException if distance, width, or height are not positive
         */
        private void checkViewPlane() {
            if (_camera._distance < 0 || _camera._width < 0 || _camera._height < 0 || Util.isZero(_camera._distance) || Util.isZero(_camera._width) || Util.isZero(_camera._height))
                throw new IllegalArgumentException("View plane values must be positive");

            // view-plane center = p0 + vTo * distance (shift along the viewing axis)
            _camera._vpCenter = _camera._p0.add(_camera._vTo.scale(_camera._distance));
            _camera._pixelWidth = _camera._width / _camera._nX;
            _camera._pixelHeight = _camera._height / _camera._nY;
        }

        /**
         * Validates all camera parameters, computes derived fields, and returns
         * the assembled {@link Camera}.
         *
         * <p>Validation order: resolution → location and direction → view-plane
         * dimensions. The returned camera is a defensive copy so subsequent
         * Builder calls cannot mutate it.</p>
         *
         * @return the fully constructed and validated Camera
         * @throws IllegalArgumentException if any numeric parameter is non-positive,
         *                                  or if the direction and up vectors are parallel
         * @throws MissingResourceException if the location or direction has not been set
         */
        public Camera build() {
            checkResolution();
            checkLocationAndDirection();
            checkViewPlane();

            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException _) {
                return null;
            }
        }
    }
}

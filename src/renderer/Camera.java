package renderer;

import java.util.LinkedList;
import java.util.MissingResourceException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import primitives.Color;
import primitives.Double3;
import primitives.Point;
import primitives.Ray;
import primitives.Util;
import primitives.Vector;
import scene.Scene;

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
     * The image writer used to record rendered pixel colors.
     * Created in {@link Builder#checkResolution()}.
     */
    private ImageWriter _imageWriter;

    /**
     * The ray tracer used to compute the color of each ray.
     * Set via {@link Builder#setRayTracer(Scene, RayTracerType)}.
     */
    private RayTracerBase _rayTracer;

    /**
     * Blackboard for beam generation. Default numSamples=1 produces a single
     * center-pixel ray, which is equivalent to no anti-aliasing.
     */
    private Blackboard _aaBlackboard = new Blackboard();

    /**
     * 0 = parallel stream, 1+ = raw thread count (default 1 = single thread)
     */
    private int _threadsCount = 1;

    /**
     * Cores reserved for the JVM, JUnit, and GC during multi-threaded rendering.
     */
    private static final int SPARE_THREADS = 2;

    /**
     * Manages pixel distribution and progress reporting across threads.
     */
    private PixelManager _pixelManager;

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
     * Returns the 3D center point of pixel (xIndex, yIndex) on the view plane.
     *
     * @param xIndex the column index of the pixel (0-based)
     * @param yIndex the row index of the pixel (0-based)
     * @return the center point of the pixel on the view plane
     */
    private Point getPixelCenter(int xIndex, int yIndex) {
        double xOffset = (xIndex - (_nX - 1) / 2.0) * _pixelWidth;
        double yOffset = ((_nY - 1) / 2.0 - yIndex) * _pixelHeight;
        Point center = _vpCenter;
        if (!Util.isZero(xOffset)) center = center.add(_vRight.scale(xOffset));
        if (!Util.isZero(yOffset)) center = center.add(_vUp.scale(yOffset));
        return center;
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
        Point pixelCenter = getPixelCenter(xIndex, yIndex);
        return new Ray(_p0, pixelCenter.subtract(_p0));
    }

    /**
     * Renders the scene. Creates a {@link PixelManager} and dispatches to the
     * appropriate rendering strategy based on {@code _threadsCount}.
     *
     * @return this Camera, for method chaining
     */
    public Camera renderImage() {
        _pixelManager = new PixelManager(_nY, _nX);
        return _threadsCount == 0 ? renderImageStream() : renderImageRawThreads();
    }

    /**
     * Renders using Java parallel streams; the JVM manages the thread pool.
     */
    private Camera renderImageStream() {
        IntStream.range(0, _nY).parallel()
                .forEach(i -> IntStream.range(0, _nX).parallel()
                        .forEach(j -> castRay(j, i)));
        return this;
    }

    /**
     * Renders using a fixed pool of raw threads. Each thread requests the next
     * pixel from {@link #_pixelManager} until none remain.
     */
    private Camera renderImageRawThreads() {
        var threads = new LinkedList<Thread>();
        // An uncaught exception in a raw Thread terminates just that thread;
        // it is never propagated to the joining thread. Capture it here so a
        // worker-thread failure fails the render loudly instead of silently
        // leaving the rest of the image unwritten.
        var error = new AtomicReference<RuntimeException>();
        int count = _threadsCount;
        while (count-- > 0)
            threads.add(new Thread(() -> {
                try {
                    PixelManager.Pixel pixel;
                    while ((pixel = _pixelManager.nextPixel()) != null)
                        castRay(pixel.col(), pixel.row());
                } catch (RuntimeException e) {
                    error.compareAndSet(null, e);
                }
            }));
        for (var thread : threads) thread.start();
        try {
            for (var thread : threads) thread.join();
        } catch (InterruptedException ignored) {
        }
        if (error.get() != null) throw error.get();
        return this;
    }

    /**
     * Constructs a ray through pixel (xIndex, yIndex), traces it, and writes
     * the resulting color to the image writer.
     * When anti-aliasing is enabled a beam of rays is cast and their colors averaged.
     *
     * @param xIndex the pixel column index (0-based)
     * @param yIndex the pixel row index (0-based)
     */
    private void castRay(int xIndex, int yIndex) {
        _imageWriter.writePixel(xIndex, yIndex, castAaBeam(xIndex, yIndex));
        _pixelManager.pixelDone();
    }

    /**
     * Casts a beam of rays through the pixel area and returns the averaged color.
     * Sample points are distributed across the pixel by {@link #_aaBlackboard};
     * axes are {@code _vRight} (X) and {@code _vUp} (Y), size is one pixel.
     *
     * @param xIndex the pixel column index (0-based)
     * @param yIndex the pixel row index (0-based)
     * @return the averaged color of all beam rays
     */
    private Color castAaBeam(int xIndex, int yIndex) {
        Point pixelCenter = getPixelCenter(xIndex, yIndex);
        java.util.List<Point> samples = _aaBlackboard.generateTargetPoints(
                pixelCenter, _vRight, _vUp, _pixelWidth, _pixelHeight);
        Color total = Color.BLACK;
        for (Point sample : samples) {
            Color sampleColor = _rayTracer.traceRay(new Ray(_p0, sample.subtract(_p0)));
            total = total.add(sampleColor);
        }
        return total.reduce(samples.size());
    }

    /**
     * Overlays a grid on the rendered image by coloring pixels that fall on a
     * grid line.
     *
     * @param interval the spacing between grid lines in pixels
     * @param color    the color of the grid lines
     * @return this Camera, for method chaining
     */
    public Camera printGrid(int interval, Color color) {
        for (int i = 0; i < _nY; i++)
            for (int j = 0; j < _nX; j++)
                if (j % interval == 0 || i % interval == 0)
                    _imageWriter.writePixel(j, i, color);
        return this;
    }

    /**
     * Writes the current image to a PNG file in the images directory.
     *
     * @param fileName the output file name, without the {@code .png} extension
     * @return this Camera, for method chaining
     */
    public Camera writeToImage(String fileName) {
        _imageWriter.writeToImage(fileName);
        return this;
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
         * Accumulated roll angle in degrees (rotation around the forward vTo axis).
         * Positive values rotate counter-clockwise when looking along vTo.
         */
        private double _rotationAngle = 0;

        /**
         * Configures the camera to render the given scene using the specified ray
         * tracing strategy.
         *
         * @param scene the scene to render
         * @param type  the ray tracing strategy to use ({@link RayTracerType#SIMPLE}
         *              is the only supported type at this stage)
         * @return this Builder
         * @throws IllegalArgumentException if the requested type is not yet supported
         */
        public Builder setRayTracer(Scene scene, RayTracerType type) {
            _camera._rayTracer = switch (type) {
                case SIMPLE -> new SimpleRayTracer(scene);
                default -> throw new IllegalArgumentException("Unsupported ray tracer type: " + type);
            };
            return this;
        }

        Builder setRayTracer(RayTracerBase tracer) {
            _camera._rayTracer = tracer;
            return this;
        }

        /**
         * Enables anti-aliasing with the given number of samples using the
         * default {@link SamplingPatterns#GRID} pattern.
         *
         * @param numSamples number of rays per pixel (1 = disabled)
         * @return this Builder
         */
        public Builder setAntiAliasing(int numSamples) {
            _camera._aaBlackboard = new Blackboard().setNumSamples(numSamples);
            return this;
        }

        /**
         * Activates raw-thread rendering with the given number of threads (must be ≥ 1).
         *
         * @param numThreads number of raw threads
         * @return this Builder
         * @throws IllegalArgumentException if {@code numThreads} is not positive
         */
        public Builder setMultithreading(int numThreads) {
            if (numThreads < 1)
                throw new IllegalArgumentException("numThreads must be >= 1");
            _camera._threadsCount = numThreads;
            return this;
        }

        /**
         * Sets thread count automatically: logical cores − {@value Camera#SPARE_THREADS}.
         *
         * @return this Builder
         */
        public Builder setMultithreadingAuto() {
            int cores = Runtime.getRuntime().availableProcessors() - SPARE_THREADS;
            _camera._threadsCount = Math.max(1, cores);
            return this;
        }


        /**
         * Activates parallel-stream rendering; the JVM manages the thread pool.
         *
         * @return this Builder
         */
        public Builder setParallelStreaming() {
            _camera._threadsCount = 0;
            return this;
        }


        /**
         * Enables anti-aliasing with the given number of samples and pattern.
         *
         * @param numSamples number of rays per pixel (1 = disabled)
         * @param pattern    the sampling pattern to use
         * @return this Builder
         */
        public Builder setAntiAliasing(int numSamples, SamplingPattern pattern) {
            _camera._aaBlackboard = new Blackboard().setNumSamples(numSamples).setStrategy(pattern);
            return this;
        }

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
         * Rotates the camera around its forward ({@code vTo}) axis by the given angle.
         * <p>
         * This is a "roll" transformation: the viewing direction is unchanged, but
         * the up and right vectors are rotated in the view plane.
         * Positive angles rotate counter-clockwise when looking along {@code vTo}.
         * Calls may be chained; angles accumulate.
         * </p>
         *
         * @param angleDegrees the roll angle in degrees
         * @return this Builder
         */
        public Builder rotate(double angleDegrees) {
            _rotationAngle += angleDegrees;
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
         * Validates that the resolution values are positive, then creates the
         * {@link ImageWriter} sized to the validated resolution.
         *
         * @throws IllegalArgumentException if nX or nY are not positive
         */
        private void checkResolution() {
            if (_camera._nX <= 0 || _camera._nY <= 0)
                throw new IllegalArgumentException("Resolution values must be positive");
            _camera._imageWriter = new ImageWriter(_camera._nX, _camera._nY);
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

            // Apply roll rotation around the vTo axis (Rodrigues' formula, k·v=0 case)
            if (!Util.isZero(_rotationAngle)) {
                double rad = Math.toRadians(_rotationAngle);
                double cos = Math.cos(rad);
                double sin = Math.sin(rad);
                // vUp_new = vUp*cos + vRight*sin — combined via raw Double3 arithmetic,
                // not Vector.scale()/add(): at a 90-degree roll, cos≈0 makes the
                // intermediate term vUp*cos a near-zero vector (rejected by Vector's
                // zero-vector check) even though the final sum is a valid unit vector.
                Double3 up    = _camera._vUp.getCoordinates();
                Double3 right = _camera._vRight.getCoordinates();
                Vector rotatedUp = new Vector(up.scale(cos).add(right.scale(sin)));
                _camera._vRight = _camera._vTo.crossProduct(rotatedUp).normalize();
                _camera._vUp = _camera._vRight.crossProduct(_camera._vTo);
            }
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

            if (_camera._rayTracer == null)
                setRayTracer(new Scene("test"), RayTracerType.SIMPLE);

            try {
                return (Camera) _camera.clone();
            } catch (CloneNotSupportedException _) {
                return null;
            }
        }
    }
}

package scene;

import primitives.Color;
import primitives.Point;
import primitives.Ray;
import primitives.Vector;

/**
 * Shared parsing utilities for {@link XmlSceneLoader}.
 * <p>
 * Centralises the conversion of space-separated string representations of
 * colors, points, and vectors.
 * </p>
 *
 * @author Ambash and Elyasaf
 */
class SceneParserUtils {

    /** Utility class — no instances. */
    private SceneParserUtils() { }

    /**
     * Parses a space-separated {@code "r g b"} string into a {@link Color}.
     *
     * @param s the attribute string
     * @return the parsed color
     */
    static Color parseColor(String s) {
        double[] v = parseDoubles(s);
        return new Color(v[0], v[1], v[2]);
    }

    /**
     * Parses a space-separated {@code "x y z"} string into a {@link Point}.
     *
     * @param s the attribute string
     * @return the parsed point
     */
    static Point parsePoint(String s) {
        double[] v = parseDoubles(s);
        return new Point(v[0], v[1], v[2]);
    }

    /**
     * Parses a space-separated {@code "x y z"} string into a {@link Vector}.
     *
     * @param s the attribute string
     * @return the parsed vector
     */
    static Vector parseVector(String s) {
        double[] v = parseDoubles(s);
        return new Vector(v[0], v[1], v[2]);
    }

    /**
     * Parses two consecutive space-separated {@code "x y z"} triplets into a
     * {@link Ray}.  The first triplet is the ray origin and the second is the
     * direction vector.
     *
     * @param origin    the {@code "x y z"} string for the ray origin
     * @param direction the {@code "x y z"} string for the ray direction
     * @return the parsed ray
     */
    static Ray parseRay(String origin, String direction) {
        return new Ray(parsePoint(origin), parseVector(direction));
    }

    /**
     * Parses a space-separated string into a {@link primitives.Double3}.
     * Accepts either a single scalar ({@code "0.5"}) or three values
     * ({@code "0.2 0.6 0.4"}).
     *
     * @param s the attribute string
     * @return the parsed {@link primitives.Double3}
     */
    static primitives.Double3 parseDouble3(String s) {
        double[] v = parseDoubles(s);
        return v.length == 1 ? new primitives.Double3(v[0])
                             : new primitives.Double3(v[0], v[1], v[2]);
    }

    /**
     * Splits a whitespace-separated string into an array of {@code double} values.
     *
     * @param s the input string
     * @return the parsed values
     */
    static double[] parseDoubles(String s) {
        String[] parts = s.trim().split("\\s+");
        double[] result = new double[parts.length];
        for (int i = 0; i < parts.length; i++)
            result[i] = Double.parseDouble(parts[i]);
        return result;
    }
}

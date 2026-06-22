package scene;

import geometries.impl.TriangleMesh;
import primitives.Point;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Minimal Wavefront OBJ loader used by {@link XmlSceneLoader} for the
 * {@code <mesh>} geometry element.
 * <p>
 * Reads {@code v} (vertex) and {@code f} (face) lines only; texture
 * coordinates ({@code vt}) and normals ({@code vn}) are ignored, since
 * {@link TriangleMesh} computes its own smooth normals. Faces with more than
 * three vertices (quads / n-gons) are split into triangles by fan
 * triangulation around their first vertex.
 * <p>
 * Source models are typically authored with Z pointing "up" (e.g. 3ds Max
 * exports), while this project's scenes use a Y-up convention with the
 * camera looking toward {@code -Z}. This loader therefore remaps axes
 * (OBJ X,Y,Z &rarr; scene X,Z,Y), centers the mesh on its bounding-box
 * center, then applies a uniform scale and translation.
 *
 * @author Ambash and Elyasaf
 */
final class ObjLoader {

    /** Utility class — no instances. */
    private ObjLoader() { }

    /**
     * Loads an OBJ file and builds a {@link TriangleMesh} from its vertices and
     * (triangulated) faces, remapped into scene coordinates.
     *
     * @param file     the {@code .obj} file to read
     * @param scale    uniform scale factor applied after centering
     * @param tx       translation added to the (scaled) X coordinate
     * @param ty       translation added to the (scaled) Y coordinate
     * @param tz       translation added to the (scaled) Z coordinate
     * @param decimate keep only one out of every {@code decimate} faces
     *                 (1 = keep all faces)
     * @return the constructed {@link TriangleMesh}
     * @throws IllegalStateException if the file cannot be read
     */
    static TriangleMesh load(File file, double scale, double tx, double ty, double tz, int decimate) {
        List<double[]> rawVerts = new ArrayList<>();
        List<int[]> rawFaces = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int faceIndex = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("v ")) {
                    double[] xyz = SceneParserUtils.parseDoubles(line.substring(2));
                    rawVerts.add(xyz);
                } else if (line.startsWith("f ")) {
                    boolean keep = decimate <= 1 || faceIndex % decimate == 0;
                    faceIndex++;
                    if (!keep) continue;
                    String[] tokens = line.substring(2).trim().split("\\s+");
                    int[] idx = new int[tokens.length];
                    for (int i = 0; i < tokens.length; i++)
                        idx[i] = Integer.parseInt(tokens[i].split("/")[0]) - 1;
                    for (int i = 1; i + 1 < idx.length; i++)
                        rawFaces.add(new int[] { idx[0], idx[i], idx[i + 1] });
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read OBJ file: " + file, ex);
        }

        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        double minZ = Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (double[] v : rawVerts) {
            minX = Math.min(minX, v[0]); maxX = Math.max(maxX, v[0]);
            minY = Math.min(minY, v[1]); maxY = Math.max(maxY, v[1]);
            minZ = Math.min(minZ, v[2]); maxZ = Math.max(maxZ, v[2]);
        }
        double cx = (minX + maxX) / 2;
        double cy = (minY + maxY) / 2;
        double cz = (minZ + maxZ) / 2;

        List<Point> vertices = new ArrayList<>(rawVerts.size());
        for (double[] v : rawVerts) {
            double x = (v[0] - cx) * scale + tx;
            double y = (v[2] - cz) * scale + ty; // OBJ Z (up)    -> scene Y (up)
            double z = (v[1] - cy) * scale + tz; // OBJ Y (front) -> scene Z (toward camera)
            vertices.add(new Point(x, y, z));
        }

        return new TriangleMesh(vertices, rawFaces.toArray(new int[0][]));
    }
}

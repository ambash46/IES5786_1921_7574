package scene;

import geometries.impl.Geometries;
import geometries.impl.Triangle;
import primitives.Color;
import primitives.Double3;
import primitives.Material;
import primitives.Point;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal Wavefront OBJ + MTL loader.
 *
 * <p>Reads {@code v} (vertex) and {@code f} (face) lines from the OBJ file.
 * Face indices may be positive (1-based, absolute) or negative (relative to
 * the number of vertices read so far, e.g. {@code -1} = the most recently
 * declared vertex), per the OBJ spec.
 * If the OBJ references an MTL file via {@code mtllib}, that file is loaded
 * automatically from the same directory. {@code usemtl} directives assign
 * materials to subsequent faces.
 *
 * <p>MTL properties mapped:
 * <ul>
 *   <li>{@code Ka} → {@link Material#kA}</li>
 *   <li>{@code Kd} → {@link Material#kD}</li>
 *   <li>{@code Ks} → {@link Material#kS}</li>
 *   <li>{@code Ns} → {@link Material#nShininess}</li>
 *   <li>{@code d} / {@code Tr} → {@link Material#kT} (d=opacity, Tr=transparency)</li>
 *   <li>{@code Ke} → geometry emission color (non-zero values only)</li>
 * </ul>
 * Unsupported properties ({@code map_Kd}, {@code Ni}, {@code illum}, etc.) are
 * silently ignored.
 *
 * <p>Source models are typically authored with Z pointing "up" (e.g. 3ds Max
 * exports). This loader remaps axes (OBJ X,Y,Z → scene X,Z,Y), centers the
 * mesh on its bounding-box center, then applies a uniform {@code scale} and
 * translation.
 *
 * @author Ambash and Elyasaf
 */
public final class ObjLoader {

    /** Utility class — no instances. */
    private ObjLoader() { }

    // ── MTL entry ─────────────────────────────────────────────────────────────

    /** Pairs a parsed {@link Material} with an optional emission {@link Color}. */
    private record MtlEntry(Material mat, Color emission) { }

    // ── public API ────────────────────────────────────────────────────────────

    /**
     * Loads an OBJ file (and its companion MTL if referenced) and returns a flat
     * {@link Geometries} of {@link Triangle}s with materials applied.
     *
     * @param file     the {@code .obj} file to read
     * @param scale    uniform scale factor applied after centering
     * @param tx       X translation added after scaling
     * @param ty       Y translation added after scaling
     * @param tz       Z translation added after scaling
     * @param decimate keep only one out of every {@code decimate} faces (1 = all)
     * @return the loaded geometry
     * @throws IllegalStateException if the file cannot be read, or contains a
     *                               malformed vertex/face line
     */
    public static Geometries load(File file,
                                  double scale,
                                  double tx, double ty, double tz,
                                  int decimate) {
        List<double[]> rawVerts = new ArrayList<>();
        List<int[]>    rawFaces = new ArrayList<>();
        List<String>   faceMats = new ArrayList<>();

        String  mtlFileName = null;
        String  currentMat  = null;
        boolean blenderYUp  = false; // true → OBJ already Y-up (no axis swap needed)

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int faceIndex = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("# Blender")) {
                    blenderYUp = true;
                } else if (line.startsWith("mtllib ")) {
                    mtlFileName = line.substring(7).trim();
                } else if (line.startsWith("usemtl ")) {
                    currentMat = line.substring(7).trim();
                } else if (line.startsWith("v ")) {
                    double[] xyz = SceneParserUtils.parseDoubles(line.substring(2));
                    rawVerts.add(xyz);
                } else if (line.startsWith("f ")) {
                    boolean keep = decimate <= 1 || faceIndex % decimate == 0;
                    faceIndex++;
                    if (!keep) continue;
                    String[] tokens = line.substring(2).trim().split("\\s+");
                    int[] idx = new int[tokens.length];
                    for (int i = 0; i < tokens.length; i++) {
                        int raw = Integer.parseInt(tokens[i].split("/")[0]);
                        // OBJ face indices are 1-based; a negative index is relative to
                        // the number of vertices declared so far (-1 = the last one read).
                        idx[i] = raw > 0 ? raw - 1 : rawVerts.size() + raw;
                    }
                    // fan triangulation for quads / n-gons
                    for (int i = 1; i + 1 < idx.length; i++) {
                        rawFaces.add(new int[]{ idx[0], idx[i], idx[i + 1] });
                        faceMats.add(currentMat);
                    }
                }
            }
        } catch (IOException | NumberFormatException ex) {
            throw new IllegalStateException("Failed to read OBJ file: " + file, ex);
        }

        // ── center + scale vertices ───────────────────────────────────────────
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
            double y, z;
            if (blenderYUp) {
                // Blender exports Y-up — no axis swap needed
                y = (v[1] - cy) * scale + ty; // OBJ Y (up)    → scene Y (up)
                z = (v[2] - cz) * scale + tz; // OBJ Z (depth) → scene Z
            } else {
                // 3ds Max / Z-up convention
                y = (v[2] - cz) * scale + ty; // OBJ Z (up)    → scene Y (up)
                z = (v[1] - cy) * scale + tz; // OBJ Y (front) → scene Z
            }
            vertices.add(new Point(x, y, z));
        }

        // ── load MTL ──────────────────────────────────────────────────────────
        Map<String, MtlEntry> materials = Collections.emptyMap();
        if (mtlFileName != null) {
            File mtlFile = new File(file.getParent(), mtlFileName);
            if (mtlFile.exists())
                materials = parseMtl(mtlFile);
            else
                System.err.println("[ObjLoader] MTL not found: " + mtlFile);
        }

        // ── build triangles with materials ────────────────────────────────────
        Geometries g = new Geometries();
        int skipped = 0;
        for (int fi = 0; fi < rawFaces.size(); fi++) {
            int[]    f   = rawFaces.get(fi);
            Triangle tri;
            try {
                tri = new Triangle(vertices.get(f[0]),
                                   vertices.get(f[1]),
                                   vertices.get(f[2]));
            } catch (IllegalArgumentException e) {
                skipped++; // degenerate (collinear) triangle — skip silently
                continue;
            }
            String matName = faceMats.get(fi);
            if (matName != null) {
                MtlEntry entry = materials.get(matName);
                if (entry != null) {
                    if (entry.mat()      != null) tri.setMaterial(entry.mat());
                    if (entry.emission() != null) tri.setEmission(entry.emission());
                }
            }
            g.add(tri);
        }
        if (skipped > 0)
            System.err.println("[ObjLoader] Skipped " + skipped + " degenerate triangle(s) in " + file.getName());
        return g;
    }

    // ── MTL parser ────────────────────────────────────────────────────────────

    /**
     * Parses a Wavefront MTL file and returns a map from material name to
     * {@link MtlEntry}.  Parsing errors on individual lines are skipped silently
     * so that partial MTL files are handled gracefully.
     */
    private static Map<String, MtlEntry> parseMtl(File mtlFile) {
        Map<String, MtlEntry> map = new LinkedHashMap<>();
        String   currentName     = null;
        Material currentMat      = null;
        Color    currentEmission = null;

        try (BufferedReader r = new BufferedReader(new FileReader(mtlFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                if (line.startsWith("newmtl ")) {
                    // save previous material
                    if (currentName != null)
                        map.put(currentName, new MtlEntry(currentMat, currentEmission));
                    currentName     = line.substring(7).trim();
                    currentMat      = new Material();
                    currentEmission = null;

                } else if (currentMat != null) {
                    try {
                        if (line.startsWith("Kd ")) {
                            double[] c = parseRGB(line.substring(3));
                            currentMat.setKD(clamp01(c[0], c[1], c[2]));

                        } else if (line.startsWith("Ks ")) {
                            double[] c = parseRGB(line.substring(3));
                            currentMat.setKS(clamp01(c[0], c[1], c[2]));

                        } else if (line.startsWith("Ka ")) {
                            double[] c = parseRGB(line.substring(3));
                            currentMat.setKA(clamp01(c[0], c[1], c[2]));

                        } else if (line.startsWith("Ke ")) {
                            double[] c = parseRGB(line.substring(3));
                            if (c[0] > 0 || c[1] > 0 || c[2] > 0)
                                currentEmission = new Color(
                                        c[0] * 255, c[1] * 255, c[2] * 255);

                        } else if (line.startsWith("Ns ")) {
                            int ns = (int) Double.parseDouble(line.substring(3).trim());
                            currentMat.setShininess(Math.max(1, ns));

                        } else if (line.startsWith("d ")) {
                            // d = dissolve (opacity): d=1 → opaque, d=0 → transparent
                            double d = Double.parseDouble(line.substring(2).trim());
                            if (d < 0.999)
                                currentMat.setKT(clamp01(1 - d, 1 - d, 1 - d));

                        } else if (line.startsWith("Tr ")) {
                            // Tr = transparency (inverse of d): Tr=0 → opaque
                            double tr = Double.parseDouble(line.substring(3).trim());
                            if (tr > 0.001)
                                currentMat.setKT(clamp01(tr, tr, tr));

                        } else if (line.startsWith("Kr ")) {
                            double[] c = parseRGB(line.substring(3));
                            currentMat.setKR(clamp01(c[0], c[1], c[2]));

                        } else if (line.startsWith("Kglossy ")) {
                            double g = Double.parseDouble(line.substring(8).trim());
                            currentMat.setKGlossy(Math.max(0, Math.min(1, g)));
                        }
                        // map_Kd, Ni, illum, etc. → ignored
                    } catch (Exception ignored) {
                        // malformed line — skip
                    }
                }
            }
            // save last material
            if (currentName != null)
                map.put(currentName, new MtlEntry(currentMat, currentEmission));

        } catch (IOException ex) {
            System.err.println("[ObjLoader] Warning: could not read MTL: " + mtlFile
                               + " — " + ex.getMessage());
        }
        return map;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Parses {@code "r g b"} or {@code "grey"} (single value) from an MTL color line.
     * Values are expected in [0,1].
     */
    private static double[] parseRGB(String s) {
        String[] parts = s.trim().split("\\s+");
        double r = Double.parseDouble(parts[0]);
        double g = parts.length > 1 ? Double.parseDouble(parts[1]) : r;
        double b = parts.length > 2 ? Double.parseDouble(parts[2]) : r;
        return new double[]{ r, g, b };
    }

    /**
     * Clamps each component to {@code [0,1]} before it reaches a validated
     * {@link Material} setter. MTL files are external, third-party-authored
     * data — real-world exports have been observed with components outside
     * the nominal {@code [0,1]} range (e.g. a highlight-boosted {@code Ks}).
     * Clamping preserves the previous accept-anything behavior for such
     * files instead of silently dropping the whole line.
     *
     * @param  r red/first component
     * @param  g green/second component
     * @param  b blue/third component
     * @return   a {@link Double3} with every component clamped to {@code [0,1]}
     */
    private static Double3 clamp01(double r, double g, double b) {
        return new Double3(
                Math.max(0, Math.min(1, r)),
                Math.max(0, Math.min(1, g)),
                Math.max(0, Math.min(1, b)));
    }
}

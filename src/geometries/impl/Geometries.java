package geometries.impl;

import geometries.api.Intersectable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import primitives.Ray;

/**
 * Represents a collection of geometric objects (Composite design pattern).
 * Aggregates any number of {@link Intersectable} objects and treats the whole
 * collection as a single {@link Intersectable} scene element.
 *
 * @author Ambash and Elyasaf
 */
public final class Geometries extends Intersectable {
    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    public Geometries() { /* no-op */ }

    /**
     * The list of geometric objects in this collection.
     */
    private final List<Intersectable> geometries = new ArrayList<>();

    /**
     * Constructs a collection pre-populated with the given geometries.
     *
     * @param geometries zero or more {@link Intersectable} objects to add
     */
    public Geometries(Intersectable... geometries) {
        add(geometries);
    }

    /**
     * Adds one or more geometric objects to this collection.
     *
     * @param geometries the objects to add
     */
    public void add(Intersectable... geometries) {
        Collections.addAll(this.geometries, geometries);
    }

    /**
     * Returns an unmodifiable view of the direct children of this node.
     */
    public List<Intersectable> getChildren() {
        return Collections.unmodifiableList(geometries);
    }

    /**
     * Returns a new flat {@link Geometries} containing every leaf geometry
     * reachable from this node. Nested {@link Geometries} groups are dissolved;
     * only primitive geometries appear in the result.
     *
     * @return a single-level Geometries with all leaves
     */
    public Geometries flatten() {
        Geometries flat = new Geometries();
        collectLeaves(flat);
        return flat;
    }

    /**
     * Creates a flat {@link Geometries} of {@link Triangle}s from a vertex list
     * and a face-index table.  Each {@code int[3]} face entry holds the indices
     * of three vertices in {@code vertices}.
     *
     * @param vertices ordered list of vertex positions
     * @param faces    each element is an {@code int[3]} face
     * @return a Geometries containing one Triangle per face
     */
    public static Geometries fromMesh(List<primitives.Point> vertices, int[][] faces) {
        Geometries g = new Geometries();
        for (int[] f : faces)
            g.add(new Triangle(vertices.get(f[0]), vertices.get(f[1]), vertices.get(f[2])));
        return g;
    }

    /**
     * Builds an automatic BVH hierarchy using median split with the default
     * maximum of 2 geometries per leaf node.
     *
     * @return a new {@link Geometries} organised as a hierarchy
     */
    public Geometries buildBVH() {
        return buildBVH(2);
    }

    /**
     * Flattens this hierarchy and builds an automatic BVH using median split.
     * Flattening first ensures the algorithm starts from a clean list of
     * primitives regardless of any pre-existing grouping.
     * Infinite geometries (null box) bypass grouping and stay at the root.
     *
     * @param maxLeafSize maximum number of primitives in a leaf node
     * @return a new {@link Geometries} organised as a hierarchy
     */
    public Geometries buildBVH(int maxLeafSize) {
        return buildBVHRecursive(new ArrayList<>(flatten().geometries), maxLeafSize);
    }

    private static Geometries buildBVHRecursive(List<Intersectable> items, int maxLeafSize) {
        List<Intersectable> finite = new ArrayList<>();
        List<Intersectable> infinite = new ArrayList<>();
        for (Intersectable item : items)
            (item.getBoundingBox() == null ? infinite : finite).add(item);

        Geometries result = new Geometries();
        infinite.forEach(result::add);

        if (finite.isEmpty()) return result;

        if (finite.size() <= maxLeafSize) {
            finite.forEach(result::add);
            return result;
        }

        int axis = longestCentroidAxis(finite);
        finite.sort(Comparator.comparingDouble(g -> g.getBoundingBox().midpoint(axis)));

        int mid = finite.size() / 2;
        result.add(buildBVHRecursive(finite.subList(0, mid), maxLeafSize));
        result.add(buildBVHRecursive(finite.subList(mid, finite.size()), maxLeafSize));
        return result;
    }

    private static int longestCentroidAxis(List<Intersectable> items) {
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        for (Intersectable item : items) {
            geometries.api.AABB box = item.getBoundingBox();
            double cx = box.midpoint(0), cy = box.midpoint(1), cz = box.midpoint(2);
            minX = Math.min(minX, cx);
            maxX = Math.max(maxX, cx);
            minY = Math.min(minY, cy);
            maxY = Math.max(maxY, cy);
            minZ = Math.min(minZ, cz);
            maxZ = Math.max(maxZ, cz);
        }
        double spanX = maxX - minX, spanY = maxY - minY, spanZ = maxZ - minZ;
        if (spanX >= spanY && spanX >= spanZ) return 0;
        return spanY >= spanZ ? 1 : 2;
    }

    private void collectLeaves(Geometries target) {
        for (Intersectable child : geometries) {
            if (child instanceof Geometries g)
                g.collectLeaves(target);
            else
                target.add(child);
        }
    }

    @Override
    protected geometries.api.AABB calcBoundingBox() {
        geometries.api.AABB box = null;
        for (Intersectable child : geometries) {
            geometries.api.AABB childBox = child.getBoundingBox();
            if (childBox == null) return null; // infinite child → whole group is infinite
            box = box == null ? childBox : box.union(childBox);
        }
        return box;
    }

    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray, double maxDistance) {
        List<Intersection> result = null;
        for (Intersectable g : geometries) {
            List<Intersection> hits = g.calcIntersections(ray, maxDistance);
            if (hits != null) {
                if (result == null)
                    result = new ArrayList<>(hits);
                else
                    result.addAll(hits);
            }
        }
        return result;
    }
}

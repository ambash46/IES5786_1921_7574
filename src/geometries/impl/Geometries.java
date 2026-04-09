package geometries.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import geometries.api.Intersectable;
import primitives.Point;
import primitives.Ray;

/**
 * Represents a collection of geometric objects (Composite design pattern).
 * Aggregates any number of {@link Intersectable} objects and treats the whole
 * collection as a single {@link Intersectable} scene element.
 *
 * @author Ambash and Elyasaf
 */
public class Geometries extends Intersectable {
    /**
     * Default constructor to satisfy JavaDoc generator.
     */
    Geometries() { /* to satisfy JavaDoc generator */ }

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
     * Finds all intersection points between the ray and every geometry in the
     * collection.
     * <p>
     * Each geometry is queried independently (delegation). Results from all
     * geometries are merged into a single list. If no geometry is hit, returns
     * {@code null}.
     * </p>
     *
     * @param ray the ray to intersect with
     * @return a list of all intersection points, or {@code null} if there are none
     */
    @Override
    public List<Point> findIntersections(Ray ray) {
        List<Point> result = null;
        for (Intersectable g : geometries) {
            List<Point> hits = g.findIntersections(ray);
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

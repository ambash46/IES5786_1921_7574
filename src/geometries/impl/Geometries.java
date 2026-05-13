package geometries.impl;

import static geometries.api.Intersectable.Intersection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import geometries.api.Intersectable;
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
     * Collects all intersections from every geometry in the collection.
     *
     * @param ray the ray to intersect with
     * @return a combined list of {@link Intersection}s, or {@code null} if none
     */
    @Override
    protected List<Intersection> calcIntersectionsHelper(Ray ray) {
        List<Intersection> result = null;
        for (Intersectable g : geometries) {
            List<Intersection> hits = g.calcIntersections(ray);
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

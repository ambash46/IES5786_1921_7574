package scene;

import geometries.api.Geometry;
import geometries.impl.Cylinder;
import geometries.impl.Plane;
import geometries.impl.Polygon;
import geometries.impl.Sphere;
import geometries.impl.Triangle;
import geometries.impl.Tube;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lighting.AmbientLight;
import lighting.DirectionalLight;
import lighting.PointLight;
import lighting.SpotLight;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import primitives.Material;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Loads a {@link Scene} from an XML file located in the {@code scenes/}
 * directory under the project root.
 *
 * <h2>Expected XML format</h2>
 * <pre>{@code
 * <scene background-color="r g b">
 *     <ambient-light color="r g b"/>
 *     <lights>
 *         <directional color="r g b" direction="x y z"/>
 *         <point       color="r g b" position="x y z" kl="0.001" kq="0.0002"/>
 *         <spotlight   color="r g b" position="x y z" direction="x y z"
 *                      kl="0.001" kq="0.0001" narrow-beam="1"/>
 *     </lights>
 *     <geometries>
 *         <sphere   center="x y z" radius="r"
 *                   emission="r g b" kd="..." ks="..." kt="..." kr="..." shininess="n"/>
 *         <triangle p0="x y z" p1="x y z" p2="x y z"
 *                   emission="r g b" kd="..." ks="..." kt="..." kr="..." shininess="n"/>
 *         <plane    point="x y z" normal="x y z"
 *                   emission="r g b" kd="..." ks="..." kr="..." shininess="n"/>
 *         <polygon  p0="x y z" p1="x y z" p2="x y z" p3="x y z" ...
 *                   emission="r g b" kd="..." ks="..." shininess="n"/>
 *         <tube     axis-origin="x y z" axis-dir="x y z" radius="r"/>
 *         <cylinder axis-origin="x y z" axis-dir="x y z" radius="r" height="h"/>
 *     </geometries>
 * </scene>
 * }</pre>
 *
 * <p>Material attributes ({@code ka}/{@code kd}/{@code ks}/{@code kt}/{@code kr})
 * accept either a single scalar or three space-separated values.
 * {@code kt} sets the transparency coefficient (stage 8), {@code kr} sets the
 * reflection coefficient (stage 8).
 * Files from previous stages without {@code <lights>} or material attributes
 * remain fully compatible.</p>
 *
 * @author Ambash and Elyasaf
 */
public class XmlSceneLoader {

    /** Directory where scene XML files are stored, relative to the project root. */
    private static final String SCENES_FOLDER =
            System.getProperty("user.dir") + "/scenes/";

    /** Constructs a new {@code XmlSceneLoader}. */
    public XmlSceneLoader() { }

    /**
     * Loads a scene from {@code scenes/<name>.xml}.
     *
     * @param name the XML file name without the {@code .xml} extension
     * @return the fully populated scene
     * @throws IllegalStateException if the file cannot be found or parsed
     */
    public Scene load(String name) {
        try {
            File file = new File(SCENES_FOLDER + name + ".xml");
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(file);
            doc.getDocumentElement().normalize();

            Element root  = doc.getDocumentElement();
            Scene   scene = new Scene(name);

            if (root.hasAttribute("background-color"))
                scene.setBackground(SceneParserUtils.parseColor(
                        root.getAttribute("background-color")));

            NodeList ambientNodes = root.getElementsByTagName("ambient-light");
            if (ambientNodes.getLength() > 0)
                scene.setAmbientLight(new AmbientLight(SceneParserUtils.parseColor(
                        ((Element) ambientNodes.item(0)).getAttribute("color"))));

            NodeList lightsContainers = root.getElementsByTagName("lights");
            if (lightsContainers.getLength() > 0)
                parseLights((Element) lightsContainers.item(0), scene);

            NodeList geomContainers = root.getElementsByTagName("geometries");
            if (geomContainers.getLength() > 0)
                parseGeometries((Element) geomContainers.item(0), scene);

            return scene;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load XML scene: " + name, e);
        }
    }

    /**
     * Parses the {@code <lights>} element and adds each light source to the scene.
     *
     * @param lights the {@code <lights>} element
     * @param scene  the scene to populate
     */
    private void parseLights(Element lights, Scene scene) {
        NodeList children = lights.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            Element e = (Element) node;
            switch (e.getTagName()) {
                case "directional" -> scene.lights.add(
                        new DirectionalLight(
                                SceneParserUtils.parseColor(e.getAttribute("color")),
                                SceneParserUtils.parseVector(e.getAttribute("direction"))));
                case "point" -> {
                    PointLight pl = new PointLight(
                            SceneParserUtils.parseColor(e.getAttribute("color")),
                            SceneParserUtils.parsePoint(e.getAttribute("position")));
                    if (e.hasAttribute("kl")) pl.setKl(Double.parseDouble(e.getAttribute("kl")));
                    if (e.hasAttribute("kq")) pl.setKq(Double.parseDouble(e.getAttribute("kq")));
                    if (e.hasAttribute("kc")) pl.setKC(Double.parseDouble(e.getAttribute("kc")));
                    scene.lights.add(pl);
                }
                case "spotlight" -> {
                    SpotLight sl = new SpotLight(
                            SceneParserUtils.parseColor(e.getAttribute("color")),
                            SceneParserUtils.parsePoint(e.getAttribute("position")),
                            SceneParserUtils.parseVector(e.getAttribute("direction")));
                    if (e.hasAttribute("kl")) sl.setKl(Double.parseDouble(e.getAttribute("kl")));
                    if (e.hasAttribute("kq")) sl.setKq(Double.parseDouble(e.getAttribute("kq")));
                    if (e.hasAttribute("kc")) sl.setKC(Double.parseDouble(e.getAttribute("kc")));
                    if (e.hasAttribute("narrow-beam"))
                        sl.setNarrowBeam(Integer.parseInt(e.getAttribute("narrow-beam")));
                    scene.lights.add(sl);
                }
                default -> throw new IllegalArgumentException(
                        "Unknown light element: <" + e.getTagName() + ">");
            }
        }
    }

    /**
     * Iterates the children of the {@code <geometries>} element and adds each
     * recognized geometry to the scene.
     *
     * @param geoms the {@code <geometries>} element
     * @param scene the scene to populate
     */
    private void parseGeometries(Element geoms, Scene scene) {
        NodeList children = geoms.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            Element  e = (Element) node;
            Geometry g = switch (e.getTagName()) {
                case "sphere"   -> new Sphere(
                        SceneParserUtils.parsePoint(e.getAttribute("center")),
                        Double.parseDouble(e.getAttribute("radius")));
                case "triangle" -> new Triangle(
                        SceneParserUtils.parsePoint(e.getAttribute("p0")),
                        SceneParserUtils.parsePoint(e.getAttribute("p1")),
                        SceneParserUtils.parsePoint(e.getAttribute("p2")));
                case "plane"    -> new Plane(
                        SceneParserUtils.parsePoint(e.getAttribute("point")),
                        SceneParserUtils.parseVector(e.getAttribute("normal")));
                case "tube"     -> new Tube(
                        Double.parseDouble(e.getAttribute("radius")),
                        SceneParserUtils.parseRay(e.getAttribute("axis-origin"),
                                e.getAttribute("axis-dir")));
                case "cylinder" -> new Cylinder(
                        Double.parseDouble(e.getAttribute("radius")),
                        SceneParserUtils.parseRay(e.getAttribute("axis-origin"),
                                e.getAttribute("axis-dir")),
                        Double.parseDouble(e.getAttribute("height")));
                case "polygon" -> {
                    List<primitives.Point> pts = new ArrayList<>();
                    for (int idx = 0; e.hasAttribute("p" + idx); idx++)
                        pts.add(SceneParserUtils.parsePoint(e.getAttribute("p" + idx)));
                    yield new Polygon(pts.toArray(new primitives.Point[0]));
                }
                default -> throw new IllegalArgumentException(
                        "Unknown geometry element: <" + e.getTagName() + ">");
            };
            if (e.hasAttribute("emission"))
                g.setEmission(SceneParserUtils.parseColor(e.getAttribute("emission")));
            Material mat = parseMaterial(e);
            if (mat != null) g.setMaterial(mat);
            scene.geometries.add(g);
        }
    }

    /**
     * Parses material attributes from a geometry element.
     * Supports {@code ka}, {@code kd}, {@code ks}, {@code kt} (transparency,
     * stage 8), {@code kr} (reflection, stage 8), and {@code shininess}.
     * Returns {@code null} if none are present (backward compatible with
     * pre-stage-8 XML files).
     *
     * @param e the geometry element
     * @return a new {@link Material} if any attribute is present, otherwise null
     */
    private static Material parseMaterial(Element e) {
        boolean any = e.hasAttribute("ka") || e.hasAttribute("kd")
                   || e.hasAttribute("ks") || e.hasAttribute("kt")
                   || e.hasAttribute("kr") || e.hasAttribute("shininess");
        if (!any) return null;
        Material mat = new Material();
        if (e.hasAttribute("ka"))       mat.kA = SceneParserUtils.parseDouble3(e.getAttribute("ka"));
        if (e.hasAttribute("kd"))       mat.kD = SceneParserUtils.parseDouble3(e.getAttribute("kd"));
        if (e.hasAttribute("ks"))       mat.kS = SceneParserUtils.parseDouble3(e.getAttribute("ks"));
        if (e.hasAttribute("kt"))       mat.kT = SceneParserUtils.parseDouble3(e.getAttribute("kt"));
        if (e.hasAttribute("kr"))       mat.kR = SceneParserUtils.parseDouble3(e.getAttribute("kr"));
        if (e.hasAttribute("shininess")) mat.nShininess = Integer.parseInt(e.getAttribute("shininess"));
        return mat;
    }
}

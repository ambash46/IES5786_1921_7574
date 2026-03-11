package primitives;

public class Point {
    Double3 _xyz;
    public static final Point ZERO= new Point(0,0,0);
    public Point(Double3 _xyz){this._xyz=_xyz;}
    public Point(double x, double y, double z) {_xyz=new Double3(x,y,z);}
    public Vector subtract(Point other)
    {
        return new Vector(this._xyz.subtract(other._xyz));
    }
    public Point add (Vector vector)
    {
        return new Point(this._xyz.add(vector._xyz));
    }
    public double distanceSquared(Point other)
    {
        double x=this._xyz._d1()-other._xyz._d1();
        double y=this._xyz._d2()-other._xyz._d2();
        double z=this._xyz._d3()-other._xyz._d3();
        return x*x+y*y+z*z;
    }
    public double distance (Point other){return Math.sqrt(distanceSquared(other));}


}

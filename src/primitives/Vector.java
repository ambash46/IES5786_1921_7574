package primitives;

public class Vector extends Point{
    public Vector(double x, double y, double z){
        super(x,y,z);
        if(super._xyz.equals(Double3.ZERO))
            throw new IllegalArgumentException("Zero vector not allowed");
    }

    public Vector(Double3 xyz){
        super(xyz);
        if(xyz.equals(Double3.ZERO))
            throw new IllegalArgumentException("Zero vector not allowed");
    }
}

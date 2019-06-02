package engine.graphics.weather;

import org.joml.Vector3f;

public class Fog {

    //Data
    private boolean active;
    private float density;
    private Vector3f color;

    //No Fog Constant
    public static Fog NOFOG = new Fog();

    //Constructors
    public Fog() { //default
        this(false, new Vector3f(0, 0, 0), 0);
    }

    public Fog(boolean active, Vector3f color, float density) { //full
        this.color = color;
        this.density = density;
        this.active = active;
    }

    //Accessors
    public boolean isActive() { return this.active; }
    public float getDensity() { return this.density; }
    public Vector3f getColor() { return this.color; }

    //Mutators
    public void setActive(boolean active) { this.active = active; }
    public void setColor(Vector3f color) { this.color = color; }
    public void setDensity(float density) { this.density = density; }
}

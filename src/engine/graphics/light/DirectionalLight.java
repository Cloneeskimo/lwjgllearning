package engine.graphics.light;

import org.joml.Vector3f;

public class DirectionalLight {

    //Data
    private Vector3f color;
    private Vector3f direction;
    private float intensity;

    //Constructors
    public DirectionalLight(Vector3f color, Vector3f direction, float intensity) { //full constructor
        this.color = color;
        this.direction = direction;
        this.intensity = intensity;
    }

    public DirectionalLight(DirectionalLight other) { //copy constructor
        this(new Vector3f(other.getColor()), new Vector3f(other.getDirection()), other.getIntensity());
    }

    //Accessors
    public Vector3f getColor() { return this.color; }
    public Vector3f getDirection() { return this.direction; }
    public float getIntensity() { return this.intensity; }

    //Mutators
    public void setColor(Vector3f color) { this.color = color; }
    public void setDirection(Vector3f direction) { this.direction = direction; }
    public void setIntensity(float intensity) { this.intensity = intensity; }
}

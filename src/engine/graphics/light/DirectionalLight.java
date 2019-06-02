package engine.graphics.light;

import org.joml.Vector3f;

public class DirectionalLight {

    //Data
    private OrthoCoords orthoCoords;
    private Vector3f color;
    private Vector3f direction;
    private float intensity;
    private float shadowPosMult;

    //Constructors
    public DirectionalLight(Vector3f color, Vector3f direction, float intensity) { //full constructor
        this.color = color;
        this.direction = direction;
        this.intensity = intensity;
        this.shadowPosMult = 1;
        this.orthoCoords = new OrthoCoords();
    }

    public DirectionalLight(DirectionalLight other) { //copy constructor
        this(new Vector3f(other.getColor()), new Vector3f(other.getDirection()), other.getIntensity());
    }

    //Accessors
    public Vector3f getColor() { return this.color; }
    public Vector3f getDirection() { return this.direction; }
    public float getIntensity() { return this.intensity; }
    public float getShadowPosMult() { return this.shadowPosMult; }
    public OrthoCoords getOrthoCoords() { return this.orthoCoords; }

    //Mutators
    public void setColor(Vector3f color) { this.color = color; }
    public void setDirection(Vector3f direction) { this.direction = direction; }
    public void setIntensity(float intensity) { this.intensity = intensity; }
    public void setShadowPosMult(float shadowPosMult) { this.shadowPosMult = shadowPosMult; }
    public void setOrthoCoords(float left, float right, float bottom, float top, float near, float far) {
        this.orthoCoords.left = left;
        this.orthoCoords.right = right;
        this.orthoCoords.bottom = bottom;
        this.orthoCoords.top = top;
        this.orthoCoords.near = near;
        this.orthoCoords.far = far;
    }

    //Orthographic Coordinates Class
    public static class OrthoCoords {

        //Data
        public float left;
        public float right;
        public float bottom;
        public float top;
        public float near;
        public float far;
    }
}

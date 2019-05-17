package engine.graphics.light;

import org.joml.Vector3f;

public class SpotLight {

    //Data
    private LightPoint lightPoint;
    private Vector3f direction;
    private float cutOff;

    //Constructor
    public SpotLight(LightPoint lightPoint, Vector3f direction, float cuttOffAngle) { //full constructor
        this.lightPoint = lightPoint;
        this.direction = direction;
        this.setCutOffAngle(cuttOffAngle);
    }

    public SpotLight(SpotLight other) { //copy constructor
        this(new LightPoint(other.getLightPoint()), new Vector3f(other.getDirection()), 0);
        this.setCutOff(other.getCutOff());
    }

    //Accessors
    public LightPoint getLightPoint() { return this.lightPoint; }
    public Vector3f getDirection() { return this.direction; }
    public float getCutOff() { return this.cutOff; }

    //Mutators
    public void setCutOffAngle(float cutOffAngle) { this.setCutOff((float)Math.cos(Math.toRadians(cutOffAngle))); }
    public void setCutOff(float cutOff) { this.cutOff = cutOff; }
    public void setDirection(Vector3f direction) { this.direction = direction; }
    public void setLightPoint(LightPoint lightPoint) { this.lightPoint = lightPoint; }
}

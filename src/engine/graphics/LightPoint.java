package engine.graphics;

import org.joml.Vector3f;

public class LightPoint {

    //Instance Data
    private Vector3f color;
    private Vector3f position;
    private float intensity;
    private Attenuation attenuation;

    //Constructors
    public LightPoint(Vector3f color, Vector3f position, float intensity) { //default attenuation constructor
        attenuation = new Attenuation(1, 0, 0);
        this.color = color;
        this.position = position;
        this.intensity = intensity;
    }

    public LightPoint(Vector3f color, Vector3f position, float intensity, Attenuation attenuation) { //full constructor
        this(color, position, intensity);
        this.attenuation = attenuation;
    }

    public LightPoint(LightPoint lightPoint) { //copy constructor
        this(new Vector3f(lightPoint.getColor()), new Vector3f(lightPoint.getPosition()), lightPoint.getIntensity(), lightPoint.getAttenuation());
    }

    //Accessors
    public Vector3f getColor() { return this.color; }
    public Vector3f getPosition() { return this.position; }
    public float getIntensity() { return this.intensity; }
    public Attenuation getAttenuation() { return this.attenuation; }

    //Mutators
    public void setColor(Vector3f color) { this.color = color; }
    public void setPosition(Vector3f position) { this.position = position; }
    public void setIntensity(float intensity) { this.intensity = intensity; }
    public void setAttenuation(Attenuation attenuation) { this.attenuation = attenuation; }

    //Static Attenuation Inner Class
    public static class Attenuation {

        //Data
        private float constant;
        private float linear;
        private float exponent;

        //Constructor
        public Attenuation(float constant, float linear, float exponent) { //full constructor
            this.constant = constant;
            this.linear = linear;
            this.exponent = exponent;
        }

        //Accessors
        public float getConstant() { return this.constant; }
        public float getLinear() { return this.linear; }
        public float getExponent() { return this.exponent; }

        //Mutators
        public void setConstant(float constant) { this.constant = constant; }
        public void setLinear(float linear) { this.linear = linear; }
        public void setExponent(float exponent) { this.exponent = exponent; }
    }

}

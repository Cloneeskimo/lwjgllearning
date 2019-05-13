package engine.graphics;

import org.joml.Vector4f;

public class Material {

    //Static Data
    private static final Vector4f DEFAULT_COLOR = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

    //Instance Data
    private Vector4f ambientColor;
    private Vector4f diffuseColor;
    private Vector4f specularColor;
    private float reflectance;
    private Texture texture;

    //Constructors
    public Material() { //default constructor
        this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, null, 0);
    }

    public Material(Vector4f color, float reflectance) { //color and reflectance constructor
        this(color, color, color, null, reflectance);
    }

    public Material(Texture texture) { //texture constructor
        this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, texture, 0);
    }

    public Material(Texture texture, float reflectance) { //texture and reflectance constructor
        this(DEFAULT_COLOR, DEFAULT_COLOR, DEFAULT_COLOR, texture, reflectance);
    }

    public Material(Vector4f ambientColor, Vector4f diffuseColor, Vector4f specularColor, Texture texture, float reflectance) { //full constructor
        this.ambientColor = ambientColor;
        this.diffuseColor = diffuseColor;
        this.specularColor = specularColor;
        this.texture = texture;
        this.reflectance = reflectance;
    }

    //Cleanup Method
    public void cleanup() {
        if (this.texture != null) this.texture.cleanup();
    }

    //Accessors
    public Vector4f getAmbientColor() { return this.ambientColor; }
    public Vector4f getDiffuseColor() { return this.diffuseColor; }
    public Vector4f getSpecularColor() { return this.specularColor; }
    public float getReflectance() { return this.reflectance; }
    public boolean isTextured() { return this.texture != null; }
    public Texture getTexture() { return this.texture; }

    //Mutators
    public void setAmbientColor(Vector4f ambientColor) { this.ambientColor = ambientColor; }
    public void setDiffuseColor(Vector4f diffuseColor) { this.diffuseColor = diffuseColor; }
    public void setSpecularColor(Vector4f specularColor) { this.specularColor = specularColor; }
    public void setReflectance(float reflectance) { this.reflectance = reflectance; }
    public void setTexture(Texture texture) { this.texture = texture; }

}

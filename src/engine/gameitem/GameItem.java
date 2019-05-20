package engine.gameitem;

import engine.graphics.Mesh;
import org.joml.Vector3f;

public class GameItem {

    //Data
    private final Vector3f position;
    private final Vector3f rotation;
    private float scale;
    private Mesh mesh;

    //Constructors
    public GameItem() { //default constructor
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = 1;
    }

    public GameItem(Mesh mesh) { //mesh constructor
        this.mesh = mesh;
        this.position = new Vector3f(0, 0, 0);
        this.scale = 1;
        this.rotation = new Vector3f(0, 0, 0);
    }

    //Accessors
    public float getScale() { return this.scale; }
    public Mesh getMesh() { return this.mesh; }
    public Vector3f getPosition() { return this.position; }
    public Vector3f getRotation() { return this.rotation; }

    //Mutators
    public void setMesh(Mesh mesh) { this.mesh = mesh; }
    public void setScale(float scale) { this.scale = scale; }
    public void setPosition(float x, float y, float z) {
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }
    public void setPosition(Vector3f xyz) {
        this.position.x = xyz.x;
        this.position.y = xyz.y;
        this.position.z = xyz.z;
    }
    public void setRotation(float x, float y, float z) {
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }
    public void setRotation(Vector3f xyz) {
        this.rotation.x = xyz.x;
        this.rotation.y = xyz.y;
        this.rotation.z = xyz.z;
    }

    //Other methods
    public void cleanup() { this.mesh.cleanup(); }
}

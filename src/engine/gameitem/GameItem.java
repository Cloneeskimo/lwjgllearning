package engine.gameitem;

import engine.graphics.Mesh;
import org.joml.Vector3f;

public class GameItem {

    //Data
    private final Vector3f position;
    private final Vector3f rotation;
    private float scale;
    private Mesh[] meshes;

    //Default Constructor
    public GameItem() {
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = 1;
    }

    //Static Constructor
    public GameItem(Mesh mesh) {
        this();
        this.meshes = new Mesh[]{ mesh };
    }

    //Animation Constructor
    public GameItem(Mesh[] meshes) {
        this();
        this.meshes = meshes;
    }

    //Accessors
    public float getScale() { return this.scale; }
    public Mesh getMesh() { return this.meshes[0]; }
    public Mesh[] getMeshes() { return this.meshes; }
    public Vector3f getPosition() { return this.position; }
    public Vector3f getRotation() { return this.rotation; }

    //Mutators
    public void setMesh(Mesh mesh, boolean cleanup) {
        if (cleanup) this.cleanup();
        this.meshes = new Mesh[]{ mesh };
    }
    public void setMeshes(Mesh[] meshes) { this.meshes = meshes; }
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

    //Cleanup Method
    public void cleanup() {
        if (this.meshes != null) {
            for (int i = 0; i < this.meshes.length; i++) {
                if (this.meshes[i] != null) this.meshes[i].cleanup();
            }
        }
    }
}

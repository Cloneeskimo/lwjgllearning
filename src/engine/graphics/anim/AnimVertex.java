package engine.graphics.anim;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class AnimVertex {

    //Data
    public Vector3f position;
    public Vector2f texCoords;
    public Vector3f normal;
    public float[] weights;
    public int[] jointIndices;

    //Constructor
    public AnimVertex() {
        super();
        this.normal = new Vector3f(0, 0, 0);
    }
}

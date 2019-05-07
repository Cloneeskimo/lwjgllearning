package engine.graphics;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

    //Data
    //Projection Matrix Formula:
    // [ (1/a*tan(fov/2), 0, 0, 0 ]
    // [ 0, 1/tan(fov/2), 0, 0 ]
    // [ 0, 0, -zp/zm, -(2*ZFar*ZNear)/zm ]
    // [ 0, 0, -1, 0 ]
    // a = aspect ratio
    // fov = field of view
    // zm = ZFar - ZNear
    // zp = zFar + ZNear
    //Luckily, JOML will create this for us.
    private final Matrix4f projection;
    private final Matrix4f world;

    //Constructor
    public Transformation() {
        this.projection = new Matrix4f();
        this.world = new Matrix4f();
    }

    //Other Methods
    public final Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        this.projection.identity().perspective(fov, aspectRatio, zNear, zFar); //projection
        return this.projection;
    }

    public Matrix4f getWorldMatrix(Vector3f offset, Vector3f rotation, float scale) {
        this.world.identity().translate(offset). //translation
                rotateX((float)Math.toRadians(rotation.x)). //rotate x
                rotateY((float)Math.toRadians(rotation.y)). //rotate y
                rotateZ((float)Math.toRadians(rotation.z)). //rotate z
                scale(scale); //scale
        return this.world;
    }
}

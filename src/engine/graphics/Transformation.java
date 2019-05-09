package engine.graphics;

import engine.GameItem;
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
    private final Matrix4f view;
    private final Matrix4f modelView;

    //Constructor
    public Transformation() {
        this.projection = new Matrix4f();
        this.view = new Matrix4f();
        this.modelView = new Matrix4f();
    }

    //Projection Matrix
    //The projection matrix projects world coordinates into screen coordinates
    public final Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        this.projection.identity().perspective(fov, aspectRatio, zNear, zFar); //projection
        return this.projection;
    }

    //View Matrix
    //The view matrix transforms items from the world into coordinates relative to a given camera
    public Matrix4f getViewMatrix(Camera camera) {
        Vector3f cameraPos = camera.getPosition();
        Vector3f cameraRot = camera.getRotation();

        //Must rotate before translating so that we can rotate over its position
        this.view.identity(); //identity
        this.view.rotate((float)Math.toRadians(cameraRot.x), new Vector3f(1, 0, 0)); //rotate x axis
        this.view.rotate((float)Math.toRadians(cameraRot.y), new Vector3f(0, 1, 0)); //rotate y axis
        this.view.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z); //translate

        return this.view;
    }

    //Model View Matrix (combination of worldMatrix and viewMatrix for a single GameItem)
    public Matrix4f getModelViewMatrix(GameItem gameItem, Matrix4f viewMatrix) {

        //World Matrix
        Vector3f gameItemRotation = gameItem.getRotation();
        this.modelView.identity().translate(gameItem.getPosition());
        this.modelView.rotateX((float)Math.toRadians(-gameItemRotation.x));
        this.modelView.rotateY((float)Math.toRadians(-gameItemRotation.y));
        this.modelView.rotateZ((float)Math.toRadians(-gameItemRotation.z));

        //View Matrix
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        return viewCurr.mul(this.modelView); //Model View Matrix (view * world)

    }
}

package engine.graphics;

import engine.gameitem.GameItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

    //Data
    private final Matrix4f projection;
    private final Matrix4f view;
    private final Matrix4f modelView;
    private final Matrix4f ortho;

    //Constructor
    public Transformation() {
        this.projection = new Matrix4f();
        this.view = new Matrix4f();
        this.modelView = new Matrix4f();
        this.ortho = new Matrix4f();
    }

    //Projection Matrix
    //The projection matrix projects world coordinates into screen coordinates
    public final Matrix4f getProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {

        //calculate aspect ratio and then the projection matrix
        float aspectRatio = width / height;
        this.projection.identity().perspective(fov, aspectRatio, zNear, zFar); //projection
        return this.projection;
    }

    //View Matrix
    //The view matrix transforms items from the world into coordinates relative to a given camera
    public Matrix4f getViewMatrix(Camera camera) {

        //get camera position and rotation
        Vector3f cameraPos = camera.getPosition();
        Vector3f cameraRot = camera.getRotation();

        //rotate before translating so that we can rotate over its position
        this.view.identity(); //identity
        this.view.rotate((float)Math.toRadians(cameraRot.x), new Vector3f(1, 0, 0)); //rotate x axis
        this.view.rotate((float)Math.toRadians(cameraRot.y), new Vector3f(0, 1, 0)); //rotate y axis
        this.view.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z); //translate

        //return matrix
        return this.view;
    }

    //Orthographic Projection Matrix
    public final Matrix4f getOrthoProjectionMatrix(float left, float right, float bottom, float top) {
        this.ortho.identity();
        this.ortho.setOrtho2D(left, right, bottom, top);
        return this.ortho;
    }

    //Model View Matrix
    //is a combination of worldMatrix and viewMatrix for a single GameItem
    public Matrix4f getModelViewMatrix(GameItem gameItem, Matrix4f viewMatrix) {

        //world matrix
        Vector3f gameItemRotation = gameItem.getRotation();
        this.modelView.identity().translate(gameItem.getPosition());
        this.modelView.rotateX((float)Math.toRadians(-gameItemRotation.x));
        this.modelView.rotateY((float)Math.toRadians(-gameItemRotation.y));
        this.modelView.rotateZ((float)Math.toRadians(-gameItemRotation.z));
        this.modelView.scale(gameItem.getScale());

        //view matrix
        Matrix4f viewCurr = new Matrix4f(viewMatrix);
        return viewCurr.mul(this.modelView); //Model View Matrix (view * world)
    }

    //Orthographic Projection Model Matrix
    public Matrix4f getOrthoProjectionModelMatrix(GameItem gameItem, Matrix4f orthoProjectionMatrix) {
        Vector3f rotation = gameItem.getRotation();
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        Matrix4f orthoProjectionCopy = new Matrix4f(orthoProjectionMatrix);
        orthoProjectionCopy.mul(modelMatrix);
        return orthoProjectionCopy;
    }
}

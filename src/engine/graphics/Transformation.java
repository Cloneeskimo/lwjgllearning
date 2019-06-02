package engine.graphics;

import engine.gameitem.GameItem;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transformation {

    //Data
    private final Matrix4f projection;
    private final Matrix4f model;
    private final Matrix4f modelView;
    private final Matrix4f modelLight;
    private final Matrix4f modelLightView;
    private final Matrix4f view;
    private final Matrix4f lightView;
    private final Matrix4f orthoProj;
    private final Matrix4f ortho2D;
    private final Matrix4f orthoModel;

    //Constructor
    public Transformation() {
        this.projection = new Matrix4f();
        this.model = new Matrix4f();
        this.modelView = new Matrix4f();
        this.modelLight = new Matrix4f();
        this.modelLightView = new Matrix4f();
        this.view = new Matrix4f();
        this.lightView = new Matrix4f();
        this.orthoProj = new Matrix4f();
        this.ortho2D = new Matrix4f();
        this.orthoModel = new Matrix4f();
    }

    //Matrix Accessors
    public Matrix4f getProjectionMatrix() { return this.projection; }
    public Matrix4f getOrthoProjectionMatrix() { return this.orthoProj; }
    public Matrix4f getViewMatrix() { return this.view; }
    public Matrix4f getLightViewMatrix() { return this.lightView; }

    //Matrix Mutators
    public void setLightViewMatrix(Matrix4f lightViewMatrix) { this.lightView.set(lightViewMatrix); }

    //Projection Matrix
    public Matrix4f updateProjectionMatrix(float fov, float width, float height, float zNear, float zFar) {
        float aspectRatio = width / height;
        this.projection.identity().perspective(fov, aspectRatio, zNear, zFar); //projection
        return this.projection;
    }

    //Orthographic Projection Matrix
    public Matrix4f updateOrthoProjectionMatrix(float left, float right, float bottom, float top, float zNear, float zFar) {
        this.orthoProj.identity();
        this.orthoProj.setOrtho(left, right, bottom, top, zNear, zFar);
        return this.orthoProj;
    }

    //View Matrix
    public Matrix4f updateViewMatrix(Camera camera) {
        return updateGenericViewMatrix(camera.getPosition(), camera.getRotation(), this.view);
    }

    //Light View Matrix
    public Matrix4f updateLightViewMatrix(Vector3f position, Vector3f rotation) {
        return updateGenericViewMatrix(position, rotation, this.lightView);
    }

    //Generic View Matrix
    public Matrix4f updateGenericViewMatrix(Vector3f position, Vector3f rotation, Matrix4f matrix) {
        matrix.identity();
        matrix.rotate((float)Math.toRadians(rotation.x), new Vector3f(1, 0, 0)); //rotate x axis
        matrix.rotate((float)Math.toRadians(rotation.y), new Vector3f(0, 1, 0)); //rotate y axis
        matrix.translate(-position.x, -position.y, -position.z); //translate
        return matrix;
    }

    //Orthographic 2D Projection Matrix
    public final Matrix4f updateOrtho2DProjectionMatrix(float left, float right, float bottom, float top) {
        this.ortho2D.identity();
        this.ortho2D.setOrtho2D(left, right, bottom, top);
        return this.ortho2D;
    }

    //Model View Matrix
    //is a combination of worldMatrix and viewMatrix for a single GameItem
    public Matrix4f updateModelViewMatrix(GameItem gameItem, Matrix4f viewMatrix) {

        //world matrix
        Vector3f rotation = gameItem.getRotation();
        this.model.identity().translate(gameItem.getPosition());
        this.model.rotateX((float)Math.toRadians(-rotation.x));
        this.model.rotateY((float)Math.toRadians(-rotation.y));
        this.model.rotateZ((float)Math.toRadians(-rotation.z));
        this.model.scale(gameItem.getScale());

        //view matrix
        this.modelView.set(viewMatrix);
        return this.modelView.mul(this.model);
    }

    //Model Light View Matrix
    public Matrix4f updateModelLightViewMatrix(GameItem gameItem, Matrix4f matrix) {
        Vector3f rotation = gameItem.getRotation();
        this.modelLight.identity().translate(gameItem.getPosition())
                .rotateX((float)Math.toRadians(-rotation.x))
                .rotateY((float)Math.toRadians(-rotation.y))
                .rotateZ((float)Math.toRadians(-rotation.z))
                .scale(gameItem.getScale());
        this.modelLightView.set(matrix);
        return this.modelLightView.mul((this.modelLight));
    }

    //Orthographic Projection Model Matrix
    public Matrix4f updateOrthoProjModelMatrix(GameItem gameItem, Matrix4f orthoMatrix) {
        Vector3f rotation = gameItem.getRotation();
        this.model.identity().translate(gameItem.getPosition()).
                rotateX((float)Math.toRadians(-rotation.x)).
                rotateY((float)Math.toRadians(-rotation.y)).
                rotateZ((float)Math.toRadians(-rotation.z)).
                scale(gameItem.getScale());
        this.orthoModel.set(orthoMatrix);
        this.orthoModel.mul(this.model);
        return this.orthoModel;
    }
}

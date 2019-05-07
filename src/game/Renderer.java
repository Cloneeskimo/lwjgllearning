package game;

import engine.Utils;
import engine.Window;
import engine.graphics.Mesh;
import engine.graphics.ShaderProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    //Data
    ShaderProgram shaderProgram;
    private static final float FOV = (float)Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;
    private Matrix4f projectionMatrix;

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

    //Constructor
    public Renderer() {}

    //Initializer
    public void init(Window window) throws Exception {

        //Create Shader Program
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResources("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(Utils.loadResources("/shaders/fragment.glsl"));
        shaderProgram.link();

        //Create Projection Matrix
        float aspectRatio = (float)window.getWidth() / (float)window.getHeight();
        projectionMatrix = new Matrix4f().perspective(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.createUniform("projection");
    }

    //Other Methods
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Mesh mesh) {

        //clear
        clear();

        //handle resize
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        //bind shader program
        shaderProgram.bind();

        //set uniforms
        shaderProgram.setUniform("projection", this.projectionMatrix);

        //Bind to the VAO
        glBindVertexArray(mesh.getVaoID());
        glEnableVertexAttribArray(0); //enable our position vbo at index 0 in the vao
        glEnableVertexAttribArray(1); //enable our color vbo at index 1 in the vao

        //Draw Mesh
        //(@mode) - specifies the primitives for rendering, triangles in this case.
        //(@count) - specifies the number of elements to be rendered.
        //(@type) - specifies the type of value in the indices data, integers in this case
        //(@indicies) - specifies the offset to apply to the indices data to start rendering
        glDrawElements(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0);

        //Restore state (unbind)
        glDisableVertexAttribArray(0); //disable our position vbo at index 0 in the vao
        glDisableVertexAttribArray(1); //disable our color vbo at index 1 in the vao
        glBindVertexArray(0);
        shaderProgram.unbind();
    }

    public void cleanup() { if (shaderProgram != null) shaderProgram.cleanup(); }
}

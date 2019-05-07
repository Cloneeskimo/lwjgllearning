package game;

import engine.GameItem;
import engine.Utils;
import engine.Window;
import engine.graphics.Mesh;
import engine.graphics.ShaderProgram;
import engine.graphics.Transformation;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    //Data
    private static final float FOV = (float)Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;
    private Transformation transformation;
    private ShaderProgram shaderProgram;

    //Constructor
    public Renderer() {
        this.transformation = new Transformation();
    }

    //Initializer
    public void init(Window window) throws Exception {

        //Create Shader Program
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResources("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(Utils.loadResources("/shaders/fragment.glsl"));
        shaderProgram.link();

        //Create Projection Matrix
        shaderProgram.createUniform("projection");
        shaderProgram.createUniform("world");
    }

    //Other Methods
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, GameItem[] gameItems) {

        //clear
        clear();

        //handle resize
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        //bind shader program
        shaderProgram.bind();

        //projection transformation (same for each GameItem)
        //we update this every render call to allow for resizing
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projection", projectionMatrix);

        //world transformation (different for each GameItem)
        for (GameItem gameItem : gameItems) {
            Matrix4f worldMatrix = transformation.getWorldMatrix(gameItem.getPosition(), gameItem.getRotation(), gameItem.getScale());
            shaderProgram.setUniform("world", worldMatrix);

            gameItem.getMesh().render();
        }

        shaderProgram.unbind();
    }

    public void cleanup() { if (shaderProgram != null) shaderProgram.cleanup(); }
}

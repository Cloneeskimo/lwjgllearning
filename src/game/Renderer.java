package game;

import engine.GameItem;
import engine.Utils;
import engine.Window;
import engine.graphics.*;
import engine.graphics.light.DirectionalLight;
import engine.graphics.light.LightPoint;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    //Data
    private static final float FOV = (float)Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;
    private Transformation transformation;
    private ShaderProgram shaderProgram;
    private float specularPower;

    //Constructor
    public Renderer() {
        this.transformation = new Transformation();
        this.specularPower = 10f;
    }

    //Initializer
    public void init(Window window) throws Exception {

        //Create Shader Program
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(Utils.loadResources("/shaders/vertex.glsl"));
        shaderProgram.createFragmentShader(Utils.loadResources("/shaders/fragment.glsl"));
        shaderProgram.link();

        //Create Matrix and Texture Sampler Uniforms
        shaderProgram.createUniform("projection");
        shaderProgram.createUniform("modelView");
        shaderProgram.createUniform("textureSampler");

        //Create Lighting/Material Uniforms
        shaderProgram.createMaterialUniform("material");
        shaderProgram.createLightPointUniform("lightPoint");
        shaderProgram.createDirectionalLightUniform("sun");
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createUniform("specularPower");
    }

    //Other Methods
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Camera camera, List<GameItem> gameItems, Vector3f ambientLight, LightPoint lightPoint, DirectionalLight sun) {

        //clear
        clear();

        //handle resize
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        //bind shader program
        shaderProgram.bind();

        //set the texture sampler to 0, in texture unit 0 of the graphics card
        shaderProgram.setUniform("textureSampler", 0);

        //projection transformation (same for each GameItem)
        //we update this every render call to allow for resizing
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projection", projectionMatrix);

        //view transformation (same for each GameItem, changes depending on camera)
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        //update light uniforms
        shaderProgram.setUniform("ambientLight", ambientLight);
        shaderProgram.setUniform("specularPower", this.specularPower);

        //get a copy of the light point and transform its position to view coordinates
        LightPoint light = new LightPoint(lightPoint); //copy light point
        Vector3f lightPos = light.getPosition(); //get position
        Vector4f aux = new Vector4f(lightPos, 1);
        aux.mul(viewMatrix);
        lightPos.x = aux.x;
        lightPos.y = aux.y;
        lightPos.z = aux.z;
        shaderProgram.setUniform("lightPoint", light);

        //get a copy of the sun (directional light) and transform its position to view coordinates
        DirectionalLight sunCopy = new DirectionalLight(sun);
        Vector4f direction = new Vector4f(sunCopy.getDirection(), 0);
        direction.mul(viewMatrix);
        sunCopy.setDirection(new Vector3f(direction.x, direction.y, direction.z));
        shaderProgram.setUniform("sun", sunCopy);

        //world transformation (different for each GameItem)
        for (GameItem gameItem : gameItems) {

            //Get reference to gameItem's mesh
            Mesh mesh = gameItem.getMesh();

            //Set model view matrix
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem, viewMatrix);
            shaderProgram.setUniform("modelView", modelViewMatrix);

            //Set material uniform
            shaderProgram.setUniform("material", mesh.getMaterial());

            //Render mesh
            mesh.render();
        }

        //Unbind shader program
        shaderProgram.unbind();
    }

    public void cleanup() { if (shaderProgram != null) shaderProgram.cleanup(); }
}

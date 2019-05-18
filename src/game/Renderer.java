package game;

import engine.GameItem;
import engine.Utils;
import engine.Window;
import engine.graphics.*;
import engine.graphics.light.DirectionalLight;
import engine.graphics.light.LightPoint;
import engine.graphics.light.SpotLight;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class Renderer {

    //Static Data
    private static final float FOV = (float)Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.f;
    private static final int MAX_LIGHT_POINTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;

    //Instance Data
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
        shaderProgram.createUniform("ambientLight");
        shaderProgram.createUniform("specularPower");
        shaderProgram.createLightPointListUniform("lightPoints", MAX_LIGHT_POINTS);
        shaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        shaderProgram.createDirectionalLightUniform("directionalLight");
    }

    //Other Methods
    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Camera camera, List<GameItem> gameItems, Vector3f ambientLight, LightPoint[] lightPoints, SpotLight[] spotLights, DirectionalLight directionalLight) {

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

        //render lights
        renderLights(viewMatrix, ambientLight, lightPoints, spotLights, directionalLight);

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

    //A Private Light Rendering Method
    //REQUIREMENT: all light arrays must be either full or empty - no null elements
    private void renderLights(Matrix4f viewMatrix, Vector3f ambientLight, LightPoint[] lightPoints, SpotLight[] spotLights, DirectionalLight directionalLight) {

        //set ambient light and specular power
        this.shaderProgram.setUniform("ambientLight", ambientLight);
        this.shaderProgram.setUniform("specularPower", specularPower);

        //render light points
        int count = lightPoints != null ? lightPoints.length : 0;
        for (int i = 0; i < count; i++) {

            //create copy and multipy by view matrix
            LightPoint lpCopy = new LightPoint(lightPoints[i]);
            Vector3f pos = lpCopy.getPosition();
            Vector4f posT = new Vector4f(pos, 1);
            posT.mul(viewMatrix);
            pos.x = posT.x;
            pos.y = posT.y;
            pos.z = posT.z;
            shaderProgram.setUniform("lightPoints[" + i + "]", lpCopy);
        }

        //render spot lights
        count = spotLights != null ? spotLights.length : 0;
        for (int i = 0; i < count; i++) {

            //create copy and multiply by view matrix
            SpotLight slCopy = new SpotLight(spotLights[i]);
            Vector4f dirT = new Vector4f(slCopy.getDirection(), 0);
            dirT.mul(viewMatrix);
            slCopy.setDirection(new Vector3f(dirT.x, dirT.y, dirT.z));
            Vector3f pos = slCopy.getLightPoint().getPosition();
            Vector4f posT = new Vector4f(pos, 1);
            posT.mul(viewMatrix);
            pos.x = posT.x;
            pos.y = posT.y;
            pos.z = posT.z;
            shaderProgram.setUniform("spotLights[" + i + "]", slCopy);
        }

        //render directional light
        DirectionalLight dlCopy = new DirectionalLight(directionalLight);
        Vector4f dirT = new Vector4f(dlCopy.getDirection(), 0);
        dirT.mul(viewMatrix);
        dlCopy.setDirection(new Vector3f(dirT.x, dirT.y, dirT.z));
        shaderProgram.setUniform("directionalLight", dlCopy);
    }

    //Cleanup Method
    public void cleanup() { if (shaderProgram != null) shaderProgram.cleanup(); }
}

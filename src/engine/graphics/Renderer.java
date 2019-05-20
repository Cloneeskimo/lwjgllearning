package engine.graphics;

import engine.IHud;
import engine.gameitem.GameItem;
import engine.Utils;
import engine.Window;
import engine.graphics.light.DirectionalLight;
import engine.graphics.light.LightPoint;
import engine.graphics.light.SceneLighting;
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
    private ShaderProgram sceneShaderProgram;
    private ShaderProgram hudShaderProgram;
    private float specularPower;

    //Constructor
    public Renderer() {
        this.transformation = new Transformation();
        this.specularPower = 10f;
    }

    //Initializer
    public void init(Window window) throws Exception {
        this.setupSceneShader();
        this.setupHudShader();
    }

    //Scene Shader Setup Method
    private void setupSceneShader() throws Exception {

        //create shader program
        this.sceneShaderProgram = new ShaderProgram();
        this.sceneShaderProgram.createVertexShader(Utils.loadResource("/shaders/sceneV.glsl"));
        this.sceneShaderProgram.createFragmentShader(Utils.loadResource("/shaders/sceneF.glsl"));
        this.sceneShaderProgram.link();

        //create matrix and texture sampler uniforms
        this.sceneShaderProgram.createUniform("projection");
        this.sceneShaderProgram.createUniform("modelView");
        this.sceneShaderProgram.createUniform("textureSampler");

        //create lighting/material uniforms
        this.sceneShaderProgram.createMaterialUniform("material");
        this.sceneShaderProgram.createUniform("ambientLight");
        this.sceneShaderProgram.createUniform("specularPower");
        this.sceneShaderProgram.createLightPointListUniform("lightPoints", MAX_LIGHT_POINTS);
        this.sceneShaderProgram.createSpotLightListUniform("spotLights", MAX_SPOT_LIGHTS);
        this.sceneShaderProgram.createDirectionalLightUniform("directionalLight");
    }

    //HUD Shader Setup Method
    private void setupHudShader() throws Exception {

        //create shader program
        this.hudShaderProgram = new ShaderProgram();
        this.hudShaderProgram.createVertexShader(Utils.loadResource("/shaders/hudV.glsl"));
        this.hudShaderProgram.createFragmentShader(Utils.loadResource("/shaders/hudF.glsl"));
        this.hudShaderProgram.link();

        //create uniforms
        this.hudShaderProgram.createUniform("projectionModel");
        this.hudShaderProgram.createUniform("color");
        this.hudShaderProgram.createUniform("hasTexture");
    }

    //Clear Method
    public void clear() { glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); }

    //Render Method
    public void render(Window window, Camera camera, List<GameItem> gameItems, SceneLighting sceneLighting, IHud hud) {

        //clear screen
        this.clear();

        //account for window resizing
        if (window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        //render
        this.renderScene(window, camera, gameItems, sceneLighting);
        this.renderHud(window, hud);
    }

    //Scene Rendering Method
    private void renderScene(Window window, Camera camera, List<GameItem> gameItems, SceneLighting lighting) {

        //bind shader program
        sceneShaderProgram.bind();

        //set the texture sampler to 0, in texture unit 0 of the graphics card
        sceneShaderProgram.setUniform("textureSampler", 0);

        //projection transformation (same for each GameItem)
        //we update this every render call to allow for resizing
        Matrix4f projectionMatrix = transformation.getProjectionMatrix(FOV, window.getWidth(), window.getHeight(), Z_NEAR, Z_FAR);
        sceneShaderProgram.setUniform("projection", projectionMatrix);

        //view transformation (same for each GameItem, changes depending on camera)
        Matrix4f viewMatrix = transformation.getViewMatrix(camera);

        //render lights
        renderLights(viewMatrix, lighting);

        //world transformation (different for each GameItem)
        System.out.println(gameItems.size());
        for (GameItem gameItem : gameItems) {

            //Get reference to gameItem's mesh
            Mesh mesh = gameItem.getMesh();

            //Set model view matrix
            Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem, viewMatrix);
            sceneShaderProgram.setUniform("modelView", modelViewMatrix);

            //Set material uniform
            sceneShaderProgram.setUniform("material", mesh.getMaterial());

            //Render mesh
            mesh.render();
        }

        //Unbind shader program
        sceneShaderProgram.unbind();
    }

    //A Private Light Rendering Method
    //REQUIREMENT: all light arrays must be either full or empty - no null elements
    private void renderLights(Matrix4f viewMatrix, SceneLighting lighting) {

        //set ambient light and specular power
        this.sceneShaderProgram.setUniform("ambientLight", lighting.getAmbientLight());
        this.sceneShaderProgram.setUniform("specularPower", this.specularPower);

        //render light points
        LightPoint[] lightPoints = lighting.getLightPoints();
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
            sceneShaderProgram.setUniform("lightPoints[" + i + "]", lpCopy);
        }

        //render spot lights
        SpotLight[] spotLights = lighting.getSpotLights();
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
            sceneShaderProgram.setUniform("spotLights[" + i + "]", slCopy);
        }

        //render directional light
        DirectionalLight dlCopy = new DirectionalLight(lighting.getDirectionalLight());
        Vector4f dirT = new Vector4f(dlCopy.getDirection(), 0);
        dirT.mul(viewMatrix);
        dlCopy.setDirection(new Vector3f(dirT.x, dirT.y, dirT.z));
        sceneShaderProgram.setUniform("directionalLight", dlCopy);
    }

    //HUD Rendering Method
    private void renderHud(Window window, IHud hud) {

        //bind shader program
        hudShaderProgram.bind();

        //calculate orthographic projection matrix
        Matrix4f ortho = transformation.getOrthoProjectionMatrix(0, window.getWidth(), window.getHeight(), 0);

        //render each hud item
        for (GameItem gameItem : hud.getGameItems()) {

            //calculate model matrix and set uniforms
            Matrix4f projectionModelMatrix = this.transformation.getOrthoProjectionModelMatrix(gameItem, ortho);
            this.hudShaderProgram.setUniform("projectionModel", projectionModelMatrix);
            this.hudShaderProgram.setUniform("color", gameItem.getMesh().getMaterial().getAmbientColor());
            this.hudShaderProgram.setUniform("hasTexture", gameItem.getMesh().getMaterial().isTextured() ? 1 : 0);

            //render
            gameItem.getMesh().render();
        }

        //unbind
        this.hudShaderProgram.unbind();
    }

    //Cleanup Method
    public void cleanup() {
        if (sceneShaderProgram != null) sceneShaderProgram.cleanup();
        if (hudShaderProgram != null) hudShaderProgram.cleanup();
    }
}
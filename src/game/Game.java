package game;

import engine.*;
import engine.gameitem.GameItem;
import engine.gameitem.SkyBox;
import engine.gameitem.Terrain;
import engine.graphics.*;
import engine.graphics.light.DirectionalLight;
import engine.graphics.light.LightPoint;
import engine.graphics.light.SceneLighting;
import engine.graphics.light.SpotLight;
import engine.graphics.weather.Fog;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;

public class Game implements IGameLogic {

    //Static Data
    private static final float MOUSE_SENSITIVITY = 0.45f;
    private static final float CAMERA_SPEED = 0.05f;

    //Non-Lighting Instance Data
    private final Vector3f cameraVelocity;
    private final Renderer renderer;
    private final Camera camera;

    //Scene and HUD
    private Scene scene;
    private Terrain terrain;
    private Hud hud;
    private float directionalLightAngle;

    //Constructor
    public Game() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraVelocity = new Vector3f();
        this.directionalLightAngle = -90;
    }

    //Initialization Method
    @Override
    public void init(Window window) throws Exception {

        //initialize renderer and create scene
        this.renderer.init(window);
        this.scene = new Scene();

        //create terrain
        float terrainScale = 15;
        int terrainSize = 3;
        float minY = -0.1f;
        float maxY = 0.1f;
        int textureInc = 40;
        this.terrain = new Terrain(terrainSize, terrainScale, minY, maxY, "/textures/heightmap.png", "/textures/terrain.png", textureInc);
        this.scene.setGameItems(terrain.getChunks());

        //create fog
        this.scene.setFog(new Fog(true, new Vector3f(0.5f, 0.5f, 0.5f), 0.15f));

//        //create sky box
//        float skyBoxScale = 50.0f;
//        SkyBox skyBox = new SkyBox("/models/skybox.obj", "/textures/skybox.png");
//        skyBox.setScale(skyBoxScale);
//        scene.setSkyBox(skyBox);

        //change window clear color for fog
        glClearColor(0.5f, 0.5f, 0.5f, 0.0f);

        //setup lights
        setupLights();

        //create hud
        this.hud = new Hud("FPS: N/A POS: N/A");
        this.hud.updateSize(window);

        //move camera
        this.camera.getPosition().x = 10.0f;
        this.camera.getPosition().y = -0.2f;
        this.camera.getPosition().z = 0.0f;
    }

    //Light Setup Method
    private void setupLights() {

        //create lighting
        SceneLighting lighting = new SceneLighting();
        this.scene.setLighting(lighting);

        //ambient light
        lighting.setAmbientLight(new Vector3f(1.0f, 1.0f, 1.0f));

        //directional light
        lighting.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(-1, 0, 0), 1.0f));
    }

    //Input Method
    @Override
    public void input(Window window) {

        //camera movement
        this.cameraVelocity.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) cameraVelocity.z -= 1;
        if (window.isKeyPressed(GLFW_KEY_S)) cameraVelocity.z += 1;
        if (window.isKeyPressed(GLFW_KEY_A)) cameraVelocity.x -= 1;
        if (window.isKeyPressed(GLFW_KEY_D)) cameraVelocity.x += 1;
        if (window.isKeyPressed(GLFW_KEY_SPACE)) cameraVelocity.y += 1;
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) cameraVelocity.y -= 1;
        if (window.isKeyPressed(GLFW_KEY_3)) this.directionalLightAngle = -85;
    }

    //Update Method
    @Override
    public void update(float interval, MouseInput mouseInput) {

        //update camera position
        Vector3f prevPosition = new Vector3f(camera.getPosition());
        camera.movePosition(cameraVelocity.x * CAMERA_SPEED, cameraVelocity.y * CAMERA_SPEED, cameraVelocity.z * CAMERA_SPEED);
        float height = this.terrain.getHeight(camera.getPosition());
        if (camera.getPosition().y <= height) {
            camera.getPosition().y = height;
        }

        //update camera rotation and compass
        if (Window.MOUSE_GRABBED) {
            Vector2f rotVec = mouseInput.getDisplVec();
            this.camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            this.hud.setCompassRotation(camera.getRotation().y);
        }

        //update directional light color and intensity
        SceneLighting lighting = this.scene.getLighting();
        DirectionalLight directionalLight = lighting.getDirectionalLight();
        this.directionalLightAngle += 0.3f;
        if (this.directionalLightAngle > 90) {
            directionalLight.setIntensity(0);
            if (this.directionalLightAngle > 360) {
                this.directionalLightAngle = -90;
            }
            lighting.getAmbientLight().set(0.4f, 0.4f, 0.4f);
        } else if (this.directionalLightAngle <= -70 || this.directionalLightAngle >= 70) {
            float factor = 1 - (float)(Math.abs(this.directionalLightAngle) - 70) / 20.0f;
            float ambientFactor = Math.max(factor - 0.1f, 0.4f);
            lighting.getAmbientLight().set(ambientFactor, ambientFactor, ambientFactor);
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            lighting.getAmbientLight().set(0.9f, 0.9f, 0.9f);
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }

        //update directional light direction
        double angle = Math.toRadians(this.directionalLightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angle);
        directionalLight.getDirection().y = (float) Math.cos(angle);

        //update text
        Vector3f cameraPos = this.camera.getPosition();
        this.hud.setStatusText("FPS: " + GameEngine.CURRENT_FPS + " POS: " + cameraPos.x + ", " + cameraPos.y + ", " + cameraPos.z + " HEIGHT: " + height);
    }

    //Render Method
    @Override
    public void render(Window window) {
        if (window.isResized()) this.hud.updateSize(window);
        this.renderer.render(window, this.camera, this.scene, this.hud);
    }

    //Cleanup Method
    @Override
    public void cleanup() {
        this.renderer.cleanup();
        Map<Mesh, List<GameItem>> meshMap = this.scene.getMeshMap();
        for (Mesh mesh : meshMap.keySet()) mesh.cleanup();
        this.hud.cleanup();
    }
}
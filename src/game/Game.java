package game;

import engine.*;
import engine.gameitem.GameItem;
import engine.gameitem.SkyBox;
import engine.graphics.*;
import engine.graphics.light.DirectionalLight;
import engine.graphics.light.LightPoint;
import engine.graphics.light.SceneLighting;
import engine.graphics.light.SpotLight;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    //Static Data
    private static final float MOUSE_SENSITIVITY = 0.45f;
    private static final float CAMERA_SPEED = 0.2f;

    //Non-Lighting Instance Data
    private final Vector3f cameraVelocity;
    private final Renderer renderer;
    private final Camera camera;

    //Scene and HUD
    private Scene scene;
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

        //setup blocks
        float r = 1f;
        Mesh m = OBJLoader.loadMesh("/models/cube.obj");
        Texture t = new Texture("/textures/grass.png");
        m.setMaterial(new Material(t, r));

        //settings
        float blockScale = 0.5f;
        float skyBoxScale = 50.0f;
        float extension = 2.0f;

        //pre-calculations
        float startx = extension * (-skyBoxScale + blockScale);
        float startz = extension * (skyBoxScale - blockScale);
        float starty = -1.0f;
        float inc = blockScale * 2;

        //loop settings
        float posx = startx;
        float posz = startz;
        float incy = 0.0f;
        int NUM_ROWS = (int)(extension * skyBoxScale * 2 /inc);
        int NUM_COLS = (int)(extension * skyBoxScale * 2 /inc);
        GameItem[] gameItems = new GameItem[NUM_ROWS * NUM_COLS];
        for (int i = 0; i < NUM_ROWS; i++) {
            for (int j = 0; j < NUM_COLS; j++) {
                GameItem gameItem = new GameItem(m);
                gameItem.setScale(blockScale);
                incy = Math.random() > 0.9f ? blockScale * 2 : 0f;
                gameItem.setPosition(posx, starty + incy, posz);
                gameItems[i * NUM_COLS + j] = gameItem;

                posx += inc;
            }

            posx = startx;
            posz -= inc;
        }
        scene.setGameItems(gameItems);

        //setup skybox
        SkyBox skyBox = new SkyBox("/models/skybox.obj", "/textures/skybox.png");
        skyBox.setScale(skyBoxScale);
        scene.setSkyBox(skyBox);

        //setup lights
        setupLights();

        //create hud
        this.hud = new Hud("FPS: N/A POS: N/A");
        this.hud.updateSize(window);

        //set camera position
        camera.getPosition().x = 0.65f;
        camera.getPosition().y = 1.15f;
        camera.getPosition().z = 4.34f;
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
    }

    //Update Method
    @Override
    public void update(float interval, MouseInput mouseInput) {

        //update camera position
        camera.movePosition(cameraVelocity.x * CAMERA_SPEED, cameraVelocity.y * CAMERA_SPEED, cameraVelocity.z * CAMERA_SPEED);

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
        this.hud.setStatusText("FPS: " + GameEngine.CURRENT_FPS + " POS: " + cameraPos.x + ", " + cameraPos.y + ", " + cameraPos.z);
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
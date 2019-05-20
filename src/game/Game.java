package game;

import engine.GameEngine;
import engine.gameitem.GameItem;
import engine.IGameLogic;
import engine.MouseInput;
import engine.Window;
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

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    //Static Data
    private static final float MOUSE_SENSITIVITY = 0.4f;
    private static final float CAMERA_SPEED = 0.15f;
    private static final float SUN_SPEED = 1.1f;

    //Non-Lighting Instance Data
    private final Vector3f cameraVelocity;
    private final Renderer renderer;
    private final Camera camera;
    private List<GameItem> gameItems;
    private Hud hud;

    //Lighting Data
    private float spotLightAngleVelocity = 1;
    private float directionalLightAngle;
    private float spotLightAngle = 0;
    private SceneLighting sceneLighting;

    //Constructor
    public Game() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraVelocity = new Vector3f();
        this.gameItems = new ArrayList<>();
        this.directionalLightAngle = -90;
    }

    @Override
    public void init(Window window) throws Exception {

        //initialize renderer
        this.renderer.init(window);

        //set reflectance
        float reflectance = 1f;

        //create grass block
        Mesh grassMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture grassTexture = new Texture("/textures/grass.png");
        Material grassMaterial = new Material(grassTexture, reflectance);
        grassMesh.setMaterial(grassMaterial);
        GameItem grassBlock = new GameItem(grassMesh);
        grassBlock.setScale(0.5f);
        grassBlock.setPosition(0, 0, -2);
        this.gameItems.add(grassBlock);

        //create scene lighting and set ambient light
        this.sceneLighting = new SceneLighting();
        this.sceneLighting.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));

        //setup light point
        Vector3f lightPosition = new Vector3f(0, 1, 1);
        float lightIntensity = 1.0f;
        LightPoint lightPoint = new LightPoint(new Vector3f(1, 1, 0), lightPosition, lightIntensity);
        lightPoint.setAttenuation(new LightPoint.Attenuation(0.0f, 0.0f, 1.0f));
        this.sceneLighting.setLightPoints(new LightPoint[]{ lightPoint });

        //setup light point object
        Mesh lightMesh = OBJLoader.loadMesh("/models/cube.obj");
        Material lightMaterial = new Material(new Vector4f(1.0f, 1.0f, 0.0f, 0.5f), reflectance);
        lightMesh.setMaterial(lightMaterial);
        GameItem lightPointItem = new GameItem(lightMesh);
        lightPointItem.setScale(0.2f);
        lightPointItem.setPosition(0, 1, 1);
        this.gameItems.add(lightPointItem);

        //setup spot lights
        lightPosition = new Vector3f(0, 0, 10);
        lightPoint = new LightPoint(new Vector3f(1, 1, 0), lightPosition, lightIntensity);
        lightPoint.setAttenuation(new LightPoint.Attenuation(0.0f, 0.0f, 0.02f));
        Vector3f lightDirection = new Vector3f(0, 0, -1);
        float lightCutoff = (float)Math.cos(Math.toRadians(140));
        SpotLight spotLight = new SpotLight(lightPoint, lightDirection, lightCutoff);
        this.sceneLighting.setSpotLights(new SpotLight[]{ spotLight, new SpotLight(spotLight) });

        //setup directional light
        lightPosition = new Vector3f(-1, 0, 0);
        this.sceneLighting.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), lightPosition, lightIntensity));

        //create hud
        this.hud = new Hud("FPS: N/A");
        this.hud.updateSize(window);
    }

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

        //light point movement
        Vector3f lightPointPos = this.sceneLighting.getLightPoints()[0].getPosition();
        if (window.isKeyPressed(GLFW_KEY_N)) {
            this.gameItems.get(1).getPosition().z += 0.1f;
            lightPointPos.z += 0.1f;
        }
        if (window.isKeyPressed(GLFW_KEY_M)) {
            this.gameItems.get(1).getPosition().z -= 0.1f;
            lightPointPos.z -= 0.1f;
        }

        //spot light movement
        Vector3f spotLightPos = this.sceneLighting.getSpotLights()[0].getLightPoint().getPosition();
        if (window.isKeyPressed(GLFW_KEY_J)) spotLightPos.z += 0.1f;
        if (window.isKeyPressed(GLFW_KEY_K)) spotLightPos.z -= 0.1f;
    }

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

        //update spot light direction
        spotLightAngle += spotLightAngleVelocity * 0.05f;
        if (spotLightAngle > 2) spotLightAngleVelocity = -1;
        if (spotLightAngle < -2) spotLightAngleVelocity = 1;
        double spotLightAngleRad = Math.toRadians(spotLightAngle);
        Vector3f coneDir = this.sceneLighting.getSpotLights()[0].getDirection();
        coneDir.y = (float)Math.sin(spotLightAngleRad);

        //update directional light color and intensity
        DirectionalLight directionalLight = this.sceneLighting.getDirectionalLight();
        this.directionalLightAngle += SUN_SPEED;
        if (this.directionalLightAngle > 90) {
            directionalLight.setIntensity(0);
            if (this.directionalLightAngle > 360) {
                this.directionalLightAngle = -90;
            }
        } else if (this.directionalLightAngle <= -80 || this.directionalLightAngle >= 80) {
            float factor = 1 - (float)(Math.abs(this.directionalLightAngle) - 80) / 10.0f;
            directionalLight.setIntensity(factor);
            directionalLight.getColor().y = Math.max(factor, 0.9f);
            directionalLight.getColor().z = Math.max(factor, 0.5f);
        } else {
            directionalLight.setIntensity(1);
            directionalLight.getColor().x = 1;
            directionalLight.getColor().y = 1;
            directionalLight.getColor().z = 1;
        }

        //update sun direction
        double angle = Math.toRadians(this.directionalLightAngle);
        directionalLight.getDirection().x = (float) Math.sin(angle);
        directionalLight.getDirection().y = (float) Math.cos(angle);

        //set text
        this.hud.setStatusText("FPS: " + GameEngine.CURRENT_FPS);
    }

    @Override
    public void render(Window window) {
        if (window.isResized()) this.hud.updateSize(window);
        renderer.render(window, camera, this.gameItems, this.sceneLighting, this.hud);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        for (GameItem gameItem : this.gameItems) gameItem.cleanup();
        this.hud.cleanup();
    }
}
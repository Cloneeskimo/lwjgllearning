package game;

import engine.GameItem;
import engine.IGameLogic;
import engine.MouseInput;
import engine.Window;
import engine.graphics.*;
import engine.graphics.light.DirectionalLight;
import engine.graphics.light.LightPoint;
import engine.graphics.light.SpotLight;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    //Static Data
    private static final float MOUSE_SENSITIVITY = 0.4f;
    private static final float CAMERA_SPEED = 0.15f;
    private static final float SUN_SPEED = 1.1f;

    //Instance Data
    private final Renderer renderer;
    private final Camera camera;
    private final Vector3f cameraVelocity;
    private List<GameItem> gameItems;

    //Light Data
    private LightPoint light;
    private DirectionalLight sun;
    private SpotLight spotLight;
    private float spotAngle = 0;
    private float spotAngleVelocity = 1;
    private float sunAngle;
    private Vector3f ambientLight;

    //Constructor
    public Game() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraVelocity = new Vector3f();
        this.gameItems = new ArrayList<>();
        this.sunAngle = -90;
    }

    @Override
    public void init(Window window) throws Exception {

        //initialize renderer
        this.renderer.init(window);

        //light point item setup
        Mesh lightMesh = OBJLoader.loadMesh("/models/cube.obj");
        Material lightMaterial = new Material(new Vector4f(1.0f, 1.0f, 0.0f, 0.0f), 1.0f);
        lightMesh.setMaterial(lightMaterial);
        GameItem lightPointItem = new GameItem(lightMesh);
        lightPointItem.setScale(0.2f);
        lightPointItem.setPosition(2, 1, 2);
        gameItems.add(lightPointItem);

        //spot light item setup
        GameItem spotLightItem = new GameItem(lightMesh);
        spotLightItem.setScale(0.2f);
        spotLightItem.setPosition(2, 0, 5);
        gameItems.add(spotLightItem);

        //setup light point
        ambientLight = new Vector3f(0.3f, 0.3f, 0.3f); //set ambient light (quite dim for now)
        Vector3f lightColor = new Vector3f(1, 1, 0); //white light for now
        Vector3f lightPosition = new Vector3f(2, 1, 2); //light position
        float lightIntensity = 1.0f; //full light intensity
        this.light = new LightPoint(lightColor, lightPosition, lightIntensity); //set light
        this.light.setAttenuation(new LightPoint.Attenuation(0.0f, 0.0f, 1.0f)); //setup light attenuation

        //setup spot light
        lightPosition = new Vector3f(2, 0, 5);
        LightPoint lightPoint = new LightPoint(new Vector3f(1, 1, 1), lightPosition, lightIntensity);
        lightPoint.setAttenuation(new LightPoint.Attenuation(0, 0, 1));
        Vector3f coneDirection = new Vector3f(0, 0, -1);
        this.spotLight = new SpotLight(lightPoint, coneDirection, 35);

        //setup sun
        lightPosition = new Vector3f(-1, 0, 0); //position of sun as a unit vector
        lightColor = new Vector3f(1, 1, 1); //color of sun
        this.sun = new DirectionalLight(lightColor, lightPosition, lightIntensity);

        //create grass block
        Mesh grassMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture grassTexture = new Texture("/textures/grass.png");
        Material grassMaterial = new Material(grassTexture, 1.0f);
        grassMesh.setMaterial(grassMaterial);
        GameItem block = new GameItem(grassMesh);
        block.setPosition(2, 0, 2);
        block.setScale(0.5f);
        this.gameItems.add(block);
    }

    @Override
    public void input(Window window, MouseInput mouseInput) {
        mouseInput.input(window);
        this.cameraVelocity.set(0, 0, 0);
        if (window.isKeyPressed(GLFW_KEY_W)) cameraVelocity.z -= 1;
        if (window.isKeyPressed(GLFW_KEY_S)) cameraVelocity.z += 1;
        if (window.isKeyPressed(GLFW_KEY_A)) cameraVelocity.x -= 1;
        if (window.isKeyPressed(GLFW_KEY_D)) cameraVelocity.x += 1;
        if (window.isKeyPressed(GLFW_KEY_SPACE)) cameraVelocity.y += 1;
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) cameraVelocity.y -= 1;
        Vector3f lightPointPos = this.light.getPosition();
        if (window.isKeyPressed(GLFW_KEY_N)) {
            this.gameItems.get(0).getPosition().z += 0.1f;
            lightPointPos.z += 0.1f;
        }
        if (window.isKeyPressed(GLFW_KEY_M)) {
            this.gameItems.get(0).getPosition().z -= 0.1f;
            lightPointPos.z -= 0.1f;
        }
        Vector3f spotLightPos = spotLight.getLightPoint().getPosition();
        if (window.isKeyPressed(GLFW_KEY_J)) {
            this.gameItems.get(1).getPosition().z += 0.1f;
            spotLightPos.z += 0.1f;
        }
        if (window.isKeyPressed(GLFW_KEY_K)) {
            this.gameItems.get(1).getPosition().z -= 0.1f;
            spotLightPos.z -= 0.1f;
        }
    }

    @Override
    public void update(float interval, MouseInput mouseInput) {

        //update camera position
        camera.movePosition(cameraVelocity.x * CAMERA_SPEED, cameraVelocity.y * CAMERA_SPEED, cameraVelocity.z * CAMERA_SPEED);

        //update camera rotation
        if (Window.MOUSE_GRABBED) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
        }

        //update spot light direction
        spotAngle += spotAngleVelocity * 0.05f;
        if (spotAngle > 2) spotAngleVelocity = -1;
        if (spotAngle < -2) spotAngleVelocity = 1;
        double spotAngleRad = Math.toRadians(spotAngle);
        Vector3f coneDir = this.spotLight.getDirection();
        coneDir.y = (float)Math.sin(spotAngleRad);

        //update sun color and intensity
        this.sunAngle += SUN_SPEED;
        if (this.sunAngle > 90) {
            this.sun.setIntensity(0);
            if (this.sunAngle > 360) {
                this.sunAngle = -90;
            }
        } else if (this.sunAngle <= -80 || this.sunAngle >= 80) {
            float factor = 1 - (float)(Math.abs(this.sunAngle) - 80) / 10.0f;
            this.sun.setIntensity(factor);
            this.sun.getColor().y = Math.max(factor, 0.9f);
            this.sun.getColor().z = Math.max(factor, 0.5f);
        } else {
            this.sun.setIntensity(1);
            this.sun.getColor().x = 1;
            this.sun.getColor().y = 1;
            this.sun.getColor().z = 1;
        }

        //update sun direction
        double angle = Math.toRadians(this.sunAngle);
        this.sun.getDirection().x = (float) Math.sin(angle);
        this.sun.getDirection().y = (float) Math.cos(angle);
    }

    @Override
    public void render(Window window) {
        renderer.render(window, camera, this.gameItems, this.ambientLight, this.light, this.sun, this.spotLight);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        for (GameItem gameItem : this.gameItems) gameItem.cleanup();
    }
}
package game;

import engine.GameItem;
import engine.IGameLogic;
import engine.MouseInput;
import engine.Window;
import engine.graphics.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    //Final Data
    private static final float MOUSE_SENSITIVITY = 0.4f;
    private static final float CAMERA_SPEED = 0.15f;
    private final Renderer renderer;
    private final Camera camera;
    private final Vector3f cameraVelocity;

    //Instance Data
    private List<GameItem> gameItems;
    private LightPoint light;
    private Vector3f ambientLight;

    //Constructor
    public Game() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraVelocity = new Vector3f();
        this.gameItems = new ArrayList<>();
    }

    @Override
    public void init(Window window) throws Exception {

        //initialize renderer
        this.renderer.init(window);

        //game item setup
        float reflectance = 1f;
        Mesh mesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture texture = new Texture("/textures/grass.png");
        Material material = new Material(texture, reflectance);
        mesh.setMaterial(material);

        //game item creation
        GameItem item = new GameItem(mesh);
        item.setScale(0.5f);
        item.setPosition(0, 0, -2);
        this.gameItems.add(item);


        //camera item setup
        Mesh cameraMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture cameraTexture = new Texture("/textures/dirt.png");
        Material cameraMaterial = new Material(cameraTexture, reflectance);
        cameraMesh.setMaterial(cameraMaterial);
        GameItem cameraItem = new GameItem(cameraMesh);
        cameraItem.setScale(0.2f);
        cameraItem.setPosition(0, 0, 1);
        gameItems.add(cameraItem);

        //setup light
        ambientLight = new Vector3f(0.3f, 0.3f, 0.3f); //set ambient light (quite dim for now)
        Vector3f lightColor = new Vector3f(1, 1, 1); //white light for now
        Vector3f lightPosition = new Vector3f(0, 0, 1); //light position
        float lightIntensity = 1.0f; //full light intensity
        this.light = new LightPoint(lightColor, lightPosition, lightIntensity); //set light
        this.light.setAttenuation(new LightPoint.Attenuation(0.0f, 0.0f, 1.0f)); //setup light attenuation

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
        float lightPos = this.light.getPosition().z;
        if (window.isKeyPressed(GLFW_KEY_N)) {
            this.light.getPosition().z = lightPos + 0.1f;
            this.gameItems.get(1).getPosition().z = lightPos + 0.1f;
        }
        if (window.isKeyPressed(GLFW_KEY_M)) {
            this.light.getPosition().z = lightPos - 0.1f;
            this.gameItems.get(1).getPosition().z = lightPos - 0.1f;
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
    }

    @Override
    public void render(Window window) {
        renderer.render(window, camera, this.gameItems, this.ambientLight, this.light);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        for (GameItem gameItem : this.gameItems) gameItem.cleanup();
    }
}
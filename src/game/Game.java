package game;

import engine.GameItem;
import engine.IGameLogic;
import engine.MouseInput;
import engine.Window;
import engine.graphics.Camera;
import engine.graphics.Mesh;
import engine.graphics.OBJLoader;
import engine.graphics.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    //Final Data
    private static final float MOUSE_SENSITIVITY = 0.4f;
    private static final float CAMERA_SPEED = 0.15f;

    //Data
    private final Renderer renderer;
    private final Camera camera;
    private Vector3f cameraVelocity;
    private GameItem[] gameItems;

    //Constructor
    public Game() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraVelocity = new Vector3f();
    }

    @Override
    public void init(Window window) throws Exception {

        //initialize renderer
        this.renderer.init(window);

        //Create Blocks
        Mesh grassMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture grassTexture = new Texture("/textures/grass.png");
        grassMesh.setTexture(grassTexture);
        Mesh dirtMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture dirtTexture = new Texture("/textures/dirt.png");
        dirtMesh.setTexture(dirtTexture);
        GameItem block1 = new GameItem(grassMesh);
        block1.setPosition(0, 0, -2);
        GameItem block2 = new GameItem(grassMesh);
        block2.setPosition(0, 0, -4);
        GameItem block3 = new GameItem(grassMesh);
        block3.setPosition(-2, 0, -2);
        GameItem block4 = new GameItem(grassMesh);
        block4.setPosition(-2, 2, -4);
        GameItem block5 = new GameItem(dirtMesh);
        block5.setPosition(-2, 0, -4);

        //Create Bunny
        Mesh bunnyMesh = OBJLoader.loadMesh("/models/bunny.obj");
        GameItem bunny = new GameItem(bunnyMesh);
        bunny.setScale(1.0f);
        bunny.setPosition(6, 0, -2);

        gameItems = new GameItem[] { block1, block2, block3, block4, block5, bunny };
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
        renderer.render(window, camera, this.gameItems);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        for (GameItem gameItem : this.gameItems) gameItem.cleanup();
    }
}
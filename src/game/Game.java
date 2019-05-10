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
    private static final float MOUSE_SENSITIVITY = 0.2f;
    private static final float CAMERA_SPEED = 0.05f;

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

        //coral pink
        //umber orange
        //yellow-orange
        //sap green
        //dark blue (navy but not too navy)
        //burnt red (but not brown)
        //pastel green
        //baby blue

        //create mesh
        float[] positions = new float[] { //vertices

            //Original Vertices
            -0.5f,  0.5f,  0.5f,    //V0
            -0.5f, -0.5f,  0.5f,    //V1
             0.5f, -0.5f,  0.5f,    //V2
             0.5f,  0.5f,  0.5f,    //V3
            -0.5f,  0.5f, -0.5f,    //V4
             0.5f,  0.5f, -0.5f,    //V5
            -0.5f, -0.5f, -0.5f,    //V6
             0.5f, -0.5f, -0.5f,    //V7

            //Repeat some for texture coordinates on top
            -0.5f,  0.5f, -0.5f, //V8 - V4 repeated
             0.5f,  0.5f, -0.5f, //V9 - V5 repeated
            -0.5f,  0.5f,  0.5f, //V10 - V0 repeated
             0.5f,  0.5f,  0.5f, //V11 - V3 repeated

            //Repeat some for texture coordinates on right
             0.5f,  0.5f,  0.5f,
             0.5f, -0.5f,  0.5f,

            //Repeat some for texture coordinates on left
            -0.5f,  0.5f,  0.5f,
            -0.5f, -0.5f,  0.5f,

            //Repeat some for texture coordinates on bottom
            -0.5f, -0.5f, -0.5f,
             0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f,  0.5f,
             0.5f, -0.5f,  0.5f
        };
        float[] textureCoordinates = new float[] { //texture coordinates
            0.0f, 0.0f,
            0.0f, 0.5f,
            0.5f, 0.5f,
            0.5f, 0.0f,

            0.0f, 0.0f,
            0.5f, 0.0f,
            0.0f, 0.5f,
            0.5f, 0.5f,

            // For text coords in top face
            0.0f, 0.5f,
            0.5f, 0.5f,
            0.0f, 1.0f,
            0.5f, 1.0f,

            // For text coords in right face
            0.0f, 0.0f,
            0.0f, 0.5f,

            // For text coords in left face
            0.5f, 0.0f,
            0.5f, 0.5f,

            // For text coords in bottom face
            0.5f, 0.0f,
            1.0f, 0.0f,
            0.5f, 0.5f,
            1.0f, 0.5f
        };
        int[] indices = new int[] { //indices
            // Front face
            0, 1, 3, 3, 1, 2,
            // Top Face
            8, 10, 11, 9, 8, 11,
            // Right face
            12, 13, 7, 5, 12, 7,
            // Left face
            14, 15, 6, 4, 14, 6,
            // Bottom face
            16, 18, 19, 17, 16, 19,
            // Back face
            4, 6, 7, 5, 4, 7
        };

        //Create Grass Block
        Mesh blockMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture grassTexture = new Texture("/textures/grass.png");
        blockMesh.setTexture(grassTexture);
        GameItem block = new GameItem(blockMesh);
        block.setScale(0.5f);
        block.setPosition(0, 0, -2);

        //Create Bunny
        Mesh bunnyMesh = OBJLoader.loadMesh("/models/bunny.obj");
        GameItem bunny = new GameItem(bunnyMesh);
        bunny.setScale(0.5f);
        bunny.setPosition(5, 0, -2);

        gameItems = new GameItem[] { block, bunny };
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
        if (mouseInput.isRightButtonPressed()) {
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
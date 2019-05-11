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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    //Final Data
    private static final float MOUSE_SENSITIVITY = 0.4f;
    private static final float CAMERA_SPEED = 0.15f;
    private static final int WORLD_WIDTH = 25;
    private static final int WORLD_LENGTH = 25;
    private static final int MAX_WORLD_HEIGHT = 20;
    private static final int BLOCKS_OFFSET = 5;
    private static final boolean DYNAMIC_HEIGHTS = true;

    //Data
    private final Renderer renderer;
    private final Camera camera;
    private Vector3f cameraVelocity;
    private List<GameItem> gameItems;

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

        //Create Block Meshes
        Mesh grassMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture grassTexture = new Texture("/textures/grass.png");
        grassMesh.setTexture(grassTexture);
        Mesh dirtMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture dirtTexture = new Texture("/textures/dirt.png");
        dirtMesh.setTexture(dirtTexture);

        //Randomly create blocks
        Random r = new Random();
        int[][] heightmap = new int[WORLD_LENGTH][WORLD_WIDTH];
        for (int z = 0; z < WORLD_LENGTH; z++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                List<Integer> nearbyHeights = new ArrayList<>();
                if (!(x - 1 < 0)) nearbyHeights.add(heightmap[z][x-1]);
                if (!(z - 1 < 0)) nearbyHeights.add(heightmap[z-1][x]);
                if (!(z + 1 >= WORLD_LENGTH)) {
                    if (heightmap[z+1][x] != 0) nearbyHeights.add(heightmap[z+1][x]);
                }
                if (!(x + 1 >= WORLD_WIDTH)) {
                    if (heightmap[z][x+1] != 0) nearbyHeights.add(heightmap[z][x+1]);
                }
                boolean badPlacement = true;
                int y = 1;
                do {
                    y = r.nextInt(MAX_WORLD_HEIGHT) + 1;
                    if (nearbyHeights.size() == 0) badPlacement = false;
                    else {
                        if (DYNAMIC_HEIGHTS) {
                            badPlacement = true;
                            for (int nearbyHeight : nearbyHeights) {
                                if (y >= nearbyHeight - 1 && y <= nearbyHeight + 1) badPlacement = false;
                            }
                        } else {
                            badPlacement = false;
                            for (int nearbyHeight : nearbyHeights) {
                                if (y < nearbyHeight - 1 || y > nearbyHeight + 1) badPlacement = true;
                            }
                        }
                    }
                } while (badPlacement);
                heightmap[z][x] = y;
            }
        }
        for (int z = 0; z < WORLD_LENGTH; z++) {
            for (int x = 0; x < WORLD_WIDTH; x++) {
                for (int y = 0; y < heightmap[z][x]; y++) {
                    GameItem newBlock = new GameItem(dirtMesh);
                    newBlock.setScale(0.5f);
                    newBlock.setPosition(x + BLOCKS_OFFSET, y, z + BLOCKS_OFFSET);
                    gameItems.add(newBlock);
                }
                GameItem newBlock = new GameItem(grassMesh);
                newBlock.setScale(0.5f);
                newBlock.setPosition(x + BLOCKS_OFFSET, heightmap[z][x], z + BLOCKS_OFFSET);
                gameItems.add(newBlock);
            }
        }

        //Create Bunny
        Mesh bunnyMesh = OBJLoader.loadMesh("/models/bunny.obj");
        GameItem bunny = new GameItem(bunnyMesh);
        bunny.setScale(1.0f);
        bunny.setPosition(BLOCKS_OFFSET, MAX_WORLD_HEIGHT + 3, BLOCKS_OFFSET);
        gameItems.add(bunny);
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
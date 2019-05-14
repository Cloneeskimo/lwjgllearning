package game;

import engine.GameItem;
import engine.IGameLogic;
import engine.MouseInput;
import engine.Window;
import engine.graphics.*;
import engine.graphics.light.DirectionalLight;
import engine.graphics.light.LightPoint;
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
    private static final boolean GENERATE_WORLD = true;

    //Instance Data
    private final Renderer renderer;
    private final Camera camera;
    private final Vector3f cameraVelocity;
    private List<GameItem> gameItems;

    //Light Data
    private LightPoint light;
    private DirectionalLight sun;
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

        //light item setup
        Mesh lightMesh = OBJLoader.loadMesh("/models/cube.obj");
        Material lightMaterial = new Material(new Vector4f(1.0f, 1.0f, 0.0f, 0.0f), 1.0f);
        lightMesh.setMaterial(lightMaterial);
        GameItem lightItem = new GameItem(lightMesh);
        lightItem.setScale(0.2f);
        lightItem.setPosition(0, 0, 0);
        gameItems.add(lightItem);

        //setup light point
        ambientLight = new Vector3f(0.3f, 0.3f, 0.3f); //set ambient light (quite dim for now)
        Vector3f lightColor = new Vector3f(1, 1, 0); //white light for now
        Vector3f lightPosition = new Vector3f(0, 0, 0); //light position
        float lightIntensity = 1.0f; //full light intensity
        this.light = new LightPoint(lightColor, lightPosition, lightIntensity); //set light
        this.light.setAttenuation(new LightPoint.Attenuation(0.0f, 0.0f, 1.0f)); //setup light attenuation

        //generate small voxel world
        if (GENERATE_WORLD) generateWorld();

        //setup sun
        lightPosition = new Vector3f(-1, 0, 0); //position of sun as a unit vector
        lightColor = new Vector3f(1, 1, 1); //color of sun
        this.sun = new DirectionalLight(lightColor, lightPosition, lightIntensity);
    }

    //World Generation Method
    public void generateWorld() throws Exception {

        //grass mesh
        Mesh grassMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture grassTexture = new Texture("/textures/grass.png");
        Material grassMaterial = new Material(grassTexture, 1.0f);
        grassMesh.setMaterial(grassMaterial);

        //dirt mesh
        Mesh dirtMesh = OBJLoader.loadMesh("/models/cube.obj");
        Texture dirtTexture = new Texture("/textures/dirt.png");
        Material dirtMaterial = new Material(dirtTexture, 1.0f);
        dirtMesh.setMaterial(dirtMaterial);

        //static final world modifiers
        final int WORLD_LENGTH = 20;
        final int WORLD_WIDTH = 20;
        final int MAX_WORLD_HEIGHT = 4;
        final int BLOCKS_OFFSET = 5;
        final boolean DYNAMIC_HEIGHTS = false;

        //fix small light
        Vector3f lightPos = new Vector3f(BLOCKS_OFFSET + WORLD_LENGTH / 2, MAX_WORLD_HEIGHT + 1, BLOCKS_OFFSET + WORLD_WIDTH / 2);
        this.light.setPosition(lightPos);
        this.gameItems.get(0).setPosition(lightPos);


        //world generation
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
            this.gameItems.get(0).getPosition().z = lightPos + 0.1f;
        }
        if (window.isKeyPressed(GLFW_KEY_M)) {
            this.light.getPosition().z = lightPos - 0.1f;
            this.gameItems.get(0).getPosition().z = lightPos - 0.1f;
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
        renderer.render(window, camera, this.gameItems, this.ambientLight, this.light, this.sun);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        for (GameItem gameItem : this.gameItems) gameItem.cleanup();
    }
}
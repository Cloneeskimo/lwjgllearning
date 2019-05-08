package game;

import engine.GameItem;
import engine.IGameLogic;
import engine.Window;
import engine.graphics.Mesh;
import engine.graphics.Texture;
import org.joml.Vector3f;
import org.joml.Vector3i;

import static org.lwjgl.glfw.GLFW.*;

public class Game implements IGameLogic {

    //Data
    private Vector3i dPos;
    private int dScale;
    private final Renderer renderer;
    private GameItem[] gameItems;

    //Constructor
    public Game() {
        this.renderer = new Renderer();
        this.dPos = new Vector3i();
        this.dScale = 0;
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
        Texture texture = new Texture("/textures/grass.png");
        GameItem gameItem = new GameItem(new Mesh(positions, textureCoordinates, indices, texture));
        gameItem.setPosition(0, 0, -2);
        gameItems = new GameItem[] { gameItem };
    }

    @Override
    public void input(Window window) {

        //set camera velocity
        this.dPos.x = this.dPos.y = this.dPos.z = this.dScale = 0;
        if (window.isKeyPressed(GLFW_KEY_UP)) this.dPos.y += 1;
        if (window.isKeyPressed(GLFW_KEY_DOWN)) this.dPos.y -= 1;
        if (window.isKeyPressed(GLFW_KEY_LEFT)) this.dPos.x -= 1;
        if (window.isKeyPressed(GLFW_KEY_RIGHT)) this.dPos.x += 1;
        if (window.isKeyPressed(GLFW_KEY_A)) this.dPos.z -= 1;
        if (window.isKeyPressed(GLFW_KEY_Q)) this.dPos.z += 1;
        if (window.isKeyPressed(GLFW_KEY_Z)) this.dScale -= 1;
        if (window.isKeyPressed(GLFW_KEY_X)) this.dScale += 1;

    }

    @Override
    public void update(float interval) {

        //update each game item
        for (GameItem gameItem : this.gameItems) {

            //update position
            Vector3f itemPos = gameItem.getPosition();
            itemPos.x += (dPos.x * 0.01f); //dX
            itemPos.y += (dPos.y * 0.01f); //dY
            itemPos.z += (dPos.z * 0.01f); //dZ
            gameItem.setPosition(itemPos);

            //update scale
            float scale = gameItem.getScale();
            scale += (dScale * 0.05f);
            if (scale < 0) scale = 0;
            gameItem.setScale(scale);

            //update rotation angle
            float rotation = gameItem.getRotation().z + 1.5f;
            if (rotation > 360) rotation = 0;
            gameItem.setRotation(rotation, rotation, rotation);
        }
    }

    @Override
    public void render(Window window) {
        renderer.render(window, this.gameItems);
    }

    @Override
    public void cleanup() {
        this.renderer.cleanup();
        for (GameItem gameItem : this.gameItems) gameItem.cleanup();
    }
}
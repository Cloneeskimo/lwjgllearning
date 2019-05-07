package game;

import engine.GameItem;
import engine.IGameLogic;
import engine.Window;
import engine.graphics.Mesh;
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

        //create mesh
        float[] positions = new float[] { //rectangle
            -0.5f,  0.5f,  -1.05f,
            -0.5f, -0.5f,  -1.05f,
             0.5f, -0.5f,  -1.05f,
             0.5f,  0.5f,  -1.05f
        };
        float[] colors = new float[] { //colors
            0.5f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f,
            0.0f, 0.0f, 0.5f,
            0.0f, 0.5f, 0.5f,
        };
        int[] indices = new int[] { 0, 1, 3, 3, 1, 2 }; //indices for element drawing
        GameItem gameItem = new GameItem(new Mesh(positions, colors, indices));
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
            gameItem.setRotation(0, 0, rotation);
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
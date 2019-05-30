package game;

import engine.*;
import engine.gameitem.GameItem;
import engine.gameitem.SkyBox;
import engine.gameitem.Terrain;
import engine.graphics.*;
import engine.graphics.light.DirectionalLight;
import engine.graphics.light.LightPoint;
import engine.graphics.light.SceneLighting;
import engine.graphics.light.SpotLight;
import engine.graphics.weather.Fog;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;

public class Game implements IGameLogic {

    //Static Data
    private static final float MOUSE_SENSITIVITY = 0.45f;
    private static final float CAMERA_SPEED = 0.05f;

    //Non-Lighting Instance Data
    private final Vector3f cameraVelocity;
    private final Renderer renderer;
    private final Camera camera;

    //Scene and HUD
    private Scene scene;
    private Terrain terrain;
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

        //create terrain
        float terrainScale = 15;
        int terrainSize = 3;
        float minY = -0.1f;
        float maxY = 0.1f;
        int textureInc = 40;
        this.terrain = new Terrain(terrainSize, terrainScale, minY, maxY, "/textures/heightmap.png", "/textures/terrain.png", textureInc);
        this.scene.setGameItems(terrain.getChunks());

        //create fog
        this.scene.setFog(new Fog(true, new Vector3f(0.5f, 0.5f, 0.5f), 0.15f));

        //create quads for normal mapping expo (quad 1)
        Mesh quadMesh1 = OBJLoader.loadMesh("/models/quad.obj");
        Texture texture = new Texture("/textures/rock.png");
        Material quadMaterial1 = new Material(texture, 0.65f);
        quadMesh1.setMaterial(quadMaterial1);
        GameItem quad1 = new GameItem(quadMesh1);
        quad1.setPosition(-3f, 0, 0);
        quad1.setScale(2.0f);
        quad1.setRotation(90, 0, 0);

        //quad 2
        Mesh quadMesh2 = OBJLoader.loadMesh("/models/quad.obj");
        Material quadMaterial2 = new Material(texture, 0.65f);
        quadMaterial2.setNormalMap(new Texture("/textures/rock_normals.png"));
        quadMesh2.setMaterial(quadMaterial2);
        GameItem quad2 = new GameItem(quadMesh2);
        quad2.setPosition(3f, 0, 0);
        quad2.setScale(2.0f);
        quad2.setRotation(90, 0, 0);

        //add quads
        this.scene.setGameItems(new GameItem[] { quad1, quad2 });

        //change window clear color for fog
        glClearColor(0.5f, 0.5f, 0.5f, 0.0f);

        //setup lights
        setupLights();

        //create hud
        this.hud = new Hud("FPS: N/A POS: N/A");
        this.hud.updateSize(window);

        //move camera
        this.camera.getPosition().x = 10.0f;
        this.camera.getPosition().y = -0.2f;
        this.camera.getPosition().z = 0.0f;
    }

    //Light Setup Method
    private void setupLights() {

        //create lighting
        SceneLighting lighting = new SceneLighting();
        this.scene.setLighting(lighting);

        //ambient light
        lighting.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));

        //directional light
        lighting.setDirectionalLight(new DirectionalLight(new Vector3f(1, 1, 1), new Vector3f(1, 1, 0), 1.0f));
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
        if (window.isKeyPressed(GLFW_KEY_LEFT)) this.directionalLightAngle -= 2.5f;
        if (this.directionalLightAngle < -90) this.directionalLightAngle = -90;
        if (window.isKeyPressed(GLFW_KEY_RIGHT)) this.directionalLightAngle += 2.5f;
        if (this.directionalLightAngle > 90) this.directionalLightAngle = 90;
    }

    //Update Method
    @Override
    public void update(float interval, MouseInput mouseInput) {

        //update camera position
        Vector3f prevPosition = new Vector3f(camera.getPosition());
        camera.movePosition(cameraVelocity.x * CAMERA_SPEED, cameraVelocity.y * CAMERA_SPEED, cameraVelocity.z * CAMERA_SPEED);
        float height = this.terrain.getHeight(camera.getPosition());
        if (camera.getPosition().y <= height) {
            camera.getPosition().y = height;
        }

        //update camera rotation and compass
        if (Window.MOUSE_GRABBED) {
            Vector2f rotVec = mouseInput.getDisplVec();
            this.camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            Vector3f rotation = this.camera.getRotation();
            if (rotation.x > 90) rotation.x = 90;
            if (rotation.x < -90) rotation.x = -90;
            this.hud.setCompassRotation(camera.getRotation().y);
        }

        //update directional light direction
        DirectionalLight dl = this.scene.getLighting().getDirectionalLight();
        double angle = Math.toRadians(this.directionalLightAngle);
        dl.getDirection().x = (float) Math.sin(angle);
        dl.getDirection().y = (float) Math.cos(angle);

        //update text
        Vector3f cameraPos = this.camera.getPosition();
        this.hud.setStatusText("FPS: " + GameEngine.CURRENT_FPS + " POS: " + cameraPos.x + ", " + cameraPos.y + ", " + cameraPos.z + " HEIGHT: " + height);
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
        if (this.scene != null) {
            Map<Mesh, List<GameItem>> meshMap = this.scene.getMeshMap();
            for (Mesh mesh : meshMap.keySet()) mesh.cleanup();
        }
        if (this.hud != null) this.hud.cleanup();
    }
}
